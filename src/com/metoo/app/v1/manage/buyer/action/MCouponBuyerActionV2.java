package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.manage.buyer.tool.MCouponBuyerTools;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/app/v2")
public class MCouponBuyerActionV2 {

	@Autowired
	private IUserService userService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private MCouponBuyerTools couponBuyerTools;
	
	@RequestMapping("getCoupon.json")
	public void getCoupon(HttpServletRequest request, HttpServletResponse response, String coupon_id, String token) {
		//初始化参数错误
		int code = 4400;
		String msg = "parameter error";
		//boolean flag = true;
		User user = null;
		if (token != null && !token.equals("")) {
			 user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		Coupon coupon = this.couponService.getObjById(CommUtil.null2Long(coupon_id));
		if (coupon != null) {
			String tourists = null;
			if (user == null) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals("tourists")) {
							tourists = CommUtil.null2String(cookie.getValue());
						}
					}
				}
				if (null == tourists || tourists.equals("")) {
					tourists = UUID.randomUUID().toString();
					Cookie cookie = new Cookie("tourists", tourists);
					cookie.setMaxAge(60 * 60 * 24 * 30);
					cookie.setPath("/");
					//cookie.setDomain(CommUtil.generic_domain(request));
					response.addCookie(cookie);
				}
			}
			List<CouponInfo> couponInfos = null;
			Map params = new HashMap();
			params.put("coupon_id", coupon.getId());
			if(user == null){
				params.put("tourists", tourists);
				couponInfos = this.couponInfoService.query(
						"select obj from CouponInfo obj where obj.coupon.id=:coupon_id and obj.tourists=:tourists",
						params, -1, -1);
			}else{
				params.put("user_id", user == null ? null :  user.getId());
				couponInfos = this.couponInfoService.query(
						"select obj from CouponInfo obj where obj.coupon.id=:coupon_id and obj.user.id=:user_id",
						params, -1, -1);
			}
			
			//当前优惠券用户已领取或已使用
			if(couponInfos.size() == 0){
				boolean flag = couponBuyerTools.IssueCoupon(coupon, user, tourists);
				if(flag){
					code = 4200;
					msg = "Successfully";
				}
			}else{
				code = 4401;
				msg = "resubmit";
			}
		}
		//return Json.toJson(new Result(code, msg), JsonFormat.compact());
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(new Result(code, msg), JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
