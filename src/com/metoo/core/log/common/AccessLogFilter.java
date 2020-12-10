/**
 * 
 */
package com.metoo.core.log.common;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.metoo.core.log.model.AccessLogModel;
import com.metoo.core.log.model.LogModel;
import com.metoo.core.log.model.LogEnum.Channel;
import com.metoo.core.log.util.AccessLogger;
import com.metoo.core.log.util.DebugLogger;
import com.metoo.core.log.util.LoggerUtil;

/**
 * @author hjw
 * 
 */
public class AccessLogFilter extends HttpServlet implements Filter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3738266651942722293L;

	private boolean isEnable = true;

	private AccessLogger accessLog = LoggerUtil.getAccessLogger();
	private DebugLogger debugLog = LoggerUtil.getDebugLogger();

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		if (isEnable) { // 如果启用
			AccessLogModel accessLogModel = new AccessLogModel(req);
			accessLogModel.request = req;
			accessLogModel.session = req.getSession();
			accessLogModel.setChannel(Channel.WEB);
			accessLogModel.setUrl(req.getRequestURI());
			accessLogModel.setSourceUrl(req.getHeader("Referer"));

			// 获取请求参数
			final Map<String, String[]> paramMap = request.getParameterMap();
			String str = "";
			for (Map.Entry<String, String[]> ent : paramMap.entrySet()) {
				if (ent.getValue().getClass().isArray()) {
					String[] sa = (String[]) ent.getValue();
					// 将数据转换为[value1,value2...]的形式
					str = str + ent.getKey() + "::";
					for (int i = 0; i < sa.length - 1; i++) {
						str = str + sa[i] + ";";
					}
					str = str + sa[sa.length - 1] + ",";

				} else {
					accessLogModel.setParams("");
				}
			}
			if (str != null && !str.equals("")) {
				str = str.substring(0, str.lastIndexOf(","));
			}
			accessLogModel.setParams(str);
			accessLog.info(this.getClass(), accessLogModel);

		} else {
			debugLog.warn(this.getClass(), new LogModel("日志过滤器被未启用，访问日志不会记录！"));
		}

		chain.doFilter(request, response);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		this.isEnable = Boolean.parseBoolean(arg0.getInitParameter("isEnable")); // 是否启用

	}
}
