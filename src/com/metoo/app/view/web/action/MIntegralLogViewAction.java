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

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.IntegralLogQueryObject;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("app/v1/")
public class MIntegralLogViewAction {

	@Autowired
	private IUserService userService;
	@Autowired
	private IIntegralLogService integralLogService;
	
	@RequestMapping("/integralLog.json")
	@ResponseBody
	public String integralLog(HttpServletRequest request, HttpServletResponse response, String token){
		int code = -1;
		String msg = "";
		Map integralMap = new HashMap();
		if(!CommUtil.null2String(token).equals("")){
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user != null){
				IntegralLogQueryObject lq = new IntegralLogQueryObject();
				lq.addQuery("obj.integral_user.id", new SysMap("integral_user", user.getId()), "=");
				lq.addQuery("obj.freeze", new SysMap("freeze", 0), "=");
				IPageList pList = this.integralLogService.list(lq);
				List<IntegralLog> integralLogs = pList.getResult();
				List<Map> list = new ArrayList<Map>();
				for(IntegralLog integralLog : integralLogs){
					Map map = new HashMap();
					map.put("integral", integralLog.getIntegral());
					map.put("type", integralLog.getType());
					map.put("integral_from", integralLog.getIntegral_from());
					if(integralLog.getIntegral_from().equals("order")){
						map.put("order_id", integralLog.getOrder() != null ? integralLog.getOrder().getId() : null);
					}
					list.add(map);
				}
				integralMap.put("integralLog", list);
				integralMap.put("user_integral", user.getIntegral());
				integralMap.put("user_freeze_integral", user.getFreezeIntegral());
				code = 4200;
				msg = "Successfully";
			}else{
				code = -100;
				msg = "token Invalidation";
			}
		}else{
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, msg, integralMap), JsonFormat.compact());
	}
}
