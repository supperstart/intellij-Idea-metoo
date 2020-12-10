package com.metoo.core.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;

/**
 * 
 * <p>
 * Title: LoginUrlEntryPoint.java
 * </p>
 * 
 * <p>
 * Description: SpringSeurity验证切入点，这里用来辨识是否通过过验证 
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版 
 */
@Component
public class LoginUrlEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(ServletRequest req, ServletResponse res,
			AuthenticationException authException) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		
		
		String userTagertUrl = null; //前台购物用户需要跳转的地址
		
		String targetUrl = null;
		HttpServletRequest request = (HttpServletRequest) req;
		
		HttpSession _session = request.getSession(false);
		if(_session!=null)
			userTagertUrl = (String)request.getSession(false).getAttribute("refererUrl");
		
		HttpServletResponse response = (HttpServletResponse) res;
		String url = request.getRequestURI();
		if (request.getQueryString() != null
				&& !request.getQueryString().equals("")) {
			url = url + "?" + request.getQueryString();
		}
		request.getSession(false).setAttribute("refererUrl", url);
		
		// 取得登陆前的url
		String refererUrl = request.getHeader("Referer");
		// TODO 增加处理逻辑
		targetUrl = refererUrl;
		if (url.indexOf("/admin/") >= 0) {//判断是否为超级管理请求
			targetUrl = request.getContextPath() + "/admin/login.htm";
			request.getSession(false).removeAttribute("refererUrl");
		} else {
			if (url.indexOf("/seller/") >= 0) {//判断是否为商家中心请求
				targetUrl = request.getContextPath() + "/seller/login.htm";
			} else if (url.indexOf("/delivery/") >= 0) {//判断是否为自提点中心请求
				targetUrl = request.getContextPath() + "/delivery/login.htm";
			} else if (url.indexOf("/wap/") >= 0) {//判断是否为wap请求
				
//				targetUrl = request.getContextPath() + "/wap/login.htm";
				request.getSession().setAttribute("returnUrl", CommUtil.get_all_url(request));
				targetUrl = request.getContextPath() + "/wap/weixinLogin.htm";
				
			} else {
				targetUrl = request.getContextPath() + "/user/login.htm";
			}
		}
		
		if(userTagertUrl!=null){
			if(userTagertUrl.indexOf("goods")>0){
				response.sendRedirect(userTagertUrl);
			}else{
				if(userTagertUrl.indexOf("buyer")>0 || userTagertUrl.indexOf("seller")>0){
					response.sendRedirect(targetUrl);
				}
			}
		}else{
		
			response.sendRedirect(targetUrl);
		}
	}
}
