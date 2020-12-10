package com.metoo.app.v1.manage.buyer.action;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MICouponInfoService;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.CouponInfoQueryObject;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/app/v1")
public class MCouponBuyerActionV1 {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICouponService couponService;

	@SecurityMapping(title = "买家优惠券列表", value = "", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping(value = "coupon.json", method = RequestMethod.GET)
	@ResponseBody
	public String coupon(HttpServletRequest request, HttpServletResponse response, String token, String currentPage) {
		int code = -1;
		String msg = "";
		Map<String, Object> map = new HashMap<String, Object>();
		User user = this.userService.getObjByProperty(null, "app_login_token", token);
		if (user == null) {
			code = -100;
			msg = "token Invalidation";
		} else {
			ModelAndView mv = new JModelAndView("", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request, response);
			CouponInfoQueryObject qo = new CouponInfoQueryObject(currentPage, mv, "store_id", "asc");
			qo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
			qo.addQuery("obj.status", new SysMap("status", 0), "=");
			qo.addQuery("obj.coupon.coupon_end_time", new SysMap("coupon_end_time", new Date()), ">=");
			IPageList pList = this.couponInfoService.list(qo);
			List<CouponInfo> infos = pList.getResult();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (CouponInfo couponInfo : infos) {
				Coupon coupon = couponInfo.getCoupon();
				Map<String, Object> couponInfoMap = new HashMap<String, Object>();
				couponInfoMap.put("Coupon_id", couponInfo.getId());
				couponInfoMap.put("Coupon_sn", couponInfo.getCoupon_sn());
				couponInfoMap.put("Coupon_amount", coupon.getCoupon_amount());
				couponInfoMap.put("Coupon_order_amount", coupon.getCoupon_order_amount());
				couponInfoMap.put("Coupon_begin_time",
						CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
				couponInfoMap.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
				couponInfoMap.put("coupon_type", coupon.getCoupon_type());
				couponInfoMap.put("Status", couponInfo.getStatus());
				Store store = couponInfo.getCoupon().getStore();
				couponInfoMap.put("store_id", store == null ? "" : store.getId());
				couponInfoMap.put("store_name",
						coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
				list.add(couponInfoMap);
			}
			map.put("couponList", list);
			map.put("poointNum", user.getPointNum());
			code = 0;
			msg = "Successfuly";
		}
		return Json.toJson(new Result(code, msg, map), JsonFormat.compact());
	}

	@RequestMapping("getCoupon.json")
	@ResponseBody
	public String getCoupon(HttpServletRequest request, HttpServletResponse response, String token, String coupon_id) {
		int code = -1;
		String msg = "";
		if (token.equals("")) {
			code = -100;
			msg = "token Invalidation";
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				code = -100;
				msg = "token Invalidation";
			} else {
				Coupon coupon = this.couponService.getObjById(CommUtil.null2Long(coupon_id));
				Store store = coupon.getStore();
				if (coupon != null) {
					if (coupon.getCoupon_count() > 0) {
						if (coupon.getCoupon_count() < coupon.getCouponinfos().size()) {
							code = 3;
							msg = "token Invalidation";
						} else {
							CouponInfo info = new CouponInfo();
							info.setAddTime(new Date());
							info.setCoupon(coupon);
							info.setUser(user);
							info.setCoupon_sn(UUID.randomUUID().toString());
							info.setStore_id(store == null ? -1 : store.getId());
							this.couponInfoService.save(info);
							code = 0;
							msg = "Successfuly";
						}
					} else {
						CouponInfo info = new CouponInfo();
						info.setAddTime(new Date());
						info.setCoupon(coupon);
						info.setUser(user);
						info.setCoupon_sn(UUID.randomUUID().toString());
						info.setStore_id(store == null ? -1 : store.getId());
						this.couponInfoService.save(info);
						code = 0;
						msg = "Successfuly";
					}
				} else {
					code = 4;
					msg = "Successfuly";
				}
			}
		}
		return Json.toJson(new Result(code, msg), JsonFormat.compact());
	}
}
