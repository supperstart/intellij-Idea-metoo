package com.metoo.app.view.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.IntegralLogQueryObject;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * <p>
 * Title: MItegralViewAction.java
 * </p>
 * 
 * <p>
 * Description: 我的积分管理控制器
 * </p>
 * 
 * 
 * @author hkk
 * 
 * @data 2020-11-4
 * 
 * @version v1.0 2020版
 *
 */
@Controller
@RequestMapping("/app/")
public class MIntegralViewAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IIntegralLogService integralLogService;
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param currentPage
	 * @param orderType 排序字段
	 * @param orderBy	排序类型
	 * @param type	积分操作类型
	 * @param from	积分来源
	 * @param token	用户登录token
	 * @descript 用户积分列表
	 * @return
	 */
	@RequestMapping(value = "v1/integral.json", produces = {"text/plain;charset=UTF-8"})
	@ResponseBody
	public String integral(HttpServletRequest request, HttpServletResponse response, String currentPage, String orderType,
			String orderBy, String type, String from, String token) {
		int code = -1;
		String msg = "";
		Map<String, Object> map = new HashMap<String, Object>();
		if (!CommUtil.null2String(token).equals("")) {
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (null != user) {
				ModelAndView mv = new JModelAndView("", configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request, response);
				if (orderBy == null && orderBy.equals("")) {
					orderBy = "addTime";
				}
				if (orderType == null || orderType.equals("")) {
					orderType = "desc";
				}
				if (this.configService.getSysConfig().isIntegralStore()) {
					IntegralLogQueryObject iqo = new IntegralLogQueryObject(currentPage, mv, orderBy, orderType);
					if (type != null && !type.equals("")) {
						iqo.addQuery("obj.type", new SysMap("type", type), "=");
					}
					if (from != null && !from.equals("")) {
						iqo.addQuery("obj.from", new SysMap("from", from), "=");
					}
					iqo.addQuery("obj.integral_user", new SysMap("user", user), "=");
					IPageList pList = this.integralLogService.list(iqo);
					List<IntegralLog> integralLogs = pList.getResult();
					List<Map<String, Object>> integralList = new ArrayList<Map<String, Object>>();
					for(IntegralLog integralLog : integralLogs){
						Map<String, Object> integralLogMap = new HashMap<String, Object>();
						integralLogMap.put("integral", integralLog.getIntegral());
						integralLogMap.put("type", integralLog.getType());
						integralLogMap.put("from", integralLog.getIntegral_from());
						integralList.add(integralLogMap);
					}
					map.put("integralLog", integralList);
					code = 4200;
					msg = "Successfully";
				} else {
					code = 4210;
					msg = "Activity not opened";
				}
			}else{
				code = -100;
				msg = "token Invalidation";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, msg, map), JsonFormat.compact());
	}
}
