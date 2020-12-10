package com.metoo.app.view.web.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * <p>
 * Title: MCouponViewAction.class
 * </p>
 * 
 * <p>
 * Description: App List of advertising coupons : 优惠券列表
 * </p>
 * 
 * <p>
 * Company: Metoo 科技有限公司
 * </P>
 * 
 * @author 46075
 *
 */
@Controller
@RequestMapping("/app/v1/")
public class MCouponViewAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;

	//@RequestMapping("/banner/coupon.json")
	@ResponseBody
	public String coupon1(HttpServletRequest request, HttpServletResponse response, String token, String count) {
		Map map = new HashMap();
		User user = null;
		if (token != null && !token.equals("")) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		List<Map<String, Object>> couponsPlatform = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> couponsStore = new ArrayList<Map<String, Object>>();
		List<Coupon> platform = new ArrayList<Coupon>(); // 平台优惠券
		Map params = new HashMap();
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 0);
		params.put("employ_type", 0);
		platform = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=:employ_type "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_amount desc",
				params, CommUtil.null2Int(count) > 0 ? 0 : -1, CommUtil.null2Int(count));
		List<Coupon> store = new ArrayList<Coupon>(); // 店铺优惠券
		params.clear();
		params.put("status", 15);
		List<Store> stores = this.storeService.query("select obj from Store obj where obj.store_status=:status", params,
				-1, -1);
		if (stores.size() > 0) {
			Set<Long> store_ids = new HashSet<Long>();
			for (Store obj : stores) {
				store_ids.add(obj.getId());
			}
			params.clear();
			params.put("store_id", store_ids);
			params.put("coupon_begin_time", new Date());
			params.put("coupon_end_time", new Date());
			params.put("coupon_type", 1);
			store = this.couponService.query(
					"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
							+ "and obj.coupon_begin_time<=:coupon_begin_time "
							+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id in(:store_id) "
							+ "order by obj.coupon_amount desc",
					params, CommUtil.null2Int(count) > 0 ? 0 : -1, CommUtil.null2Int(count));
		}
		if (user == null) {
			String tourists = "";
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("tourists")) {
						tourists = CommUtil.null2String(cookie.getValue());
					}
				}
			}
			if (!tourists.equals("")) {
				for (Coupon platCoupon : platform) {
					params.clear();
					params.put("tourists", tourists);
					params.put("coupon_id", platCoupon.getId());
					params.put("coupon_end_time", new Date());
					List<CouponInfo> plat_couponInfos = this.couponInfoService
							.query("select obj from CouponInfo obj where obj.tourists=:tourists and obj.coupon.id=:coupon_id "
									+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
					if (platCoupon.getCoupon_count() == 0
							|| platCoupon.getCoupon_count() > platCoupon.getCouponinfos().size()) {
						int status = -1;
						for (CouponInfo couponInfo : plat_couponInfos) {
							status = couponInfo.getStatus();
						}
						Map map1 = new HashMap();
						if (plat_couponInfos.size() > 0 && status != 1) {
							map1.put("couponInfoType", status == 0 ? 1 : 0);// 等于0等于未领取
																			// //
																			// 1:为已领取1
							map1.put("coupon_id", platCoupon.getId());
							map1.put("Coupon_name", platCoupon.getCoupon_name());
							map1.put("Coupon_type", platCoupon.getCoupon_type());
							map1.put("Coupon_employ_type", platCoupon.getEmploy_type());
							map1.put("Coupon_amount", platCoupon.getCoupon_amount());
							map1.put("Coupon_order_amount", platCoupon.getCoupon_order_amount());
							map1.put("Coupon_begin_time",
									CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_begin_time()));
							map1.put("Coupon_end_time",
									CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_end_time()));
							map1.put("store_name",
									platCoupon.getStore() == null ? "Soarmall" : platCoupon.getStore().getStore_name());
							map1.put("store_id", platCoupon.getStore() == null ? "" : platCoupon.getStore().getId());
							couponsStore.add(map1);
						} else if (status != 1) {
							map1.put("couponInfoType", 0);
							map1.put("coupon_id", platCoupon.getId());
							map1.put("Coupon_name", platCoupon.getCoupon_name());
							map1.put("Coupon_type", platCoupon.getCoupon_type());
							map1.put("Coupon_employ_type", platCoupon.getEmploy_type());
							map1.put("Coupon_amount", platCoupon.getCoupon_amount());
							map1.put("Coupon_order_amount", platCoupon.getCoupon_order_amount());
							map1.put("Coupon_begin_time",
									CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_begin_time()));
							map1.put("Coupon_end_time",
									CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_end_time()));
							map1.put("store_name",
									platCoupon.getStore() == null ? "Soarmall" : platCoupon.getStore().getStore_name());
							map1.put("store_id", platCoupon.getStore() == null ? "" : platCoupon.getStore().getId());
							couponsStore.add(map1);
						}
					}
				}

				for (Coupon storeCoupon : store) {
					params.clear();
					params.put("tourists", tourists);
					params.put("coupon_id", storeCoupon.getId());
					params.put("coupon_end_time", new Date());
					List<CouponInfo> store_couponInfos = this.couponInfoService
							.query("select obj from CouponInfo obj where obj.tourists=:tourists and obj.coupon.id=:coupon_id "
									+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
					if (storeCoupon.getCoupon_count() == 0
							|| storeCoupon.getCoupon_count() > storeCoupon.getCouponinfos().size()) {
						int status = -1;
						for (CouponInfo couponInfo : store_couponInfos) {
							status = couponInfo.getStatus();
						}
						Map map1 = new HashMap();
						if (store_couponInfos.size() > 0 && status != 1) {
							map1.put("couponInfoType", status == 0 ? 1 : 0);// 等于0等于未领取
																			// //
																			// 1:为已领取1
							map1.put("coupon_id", storeCoupon.getId());
							map1.put("Coupon_name", storeCoupon.getCoupon_name());
							map1.put("Coupon_type", storeCoupon.getCoupon_type());
							map1.put("Coupon_employ_type", storeCoupon.getEmploy_type());
							map1.put("Coupon_amount", storeCoupon.getCoupon_amount());
							map1.put("Coupon_order_amount", storeCoupon.getCoupon_order_amount());
							map1.put("Coupon_begin_time",
									CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_begin_time()));
							map1.put("Coupon_end_time",
									CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_end_time()));
							map1.put("store_name", storeCoupon.getStore() == null ? "Soarmall"
									: storeCoupon.getStore().getStore_name());
							map1.put("store_id", storeCoupon.getStore() == null ? "" : storeCoupon.getStore().getId());
							couponsStore.add(map1);
						} else if (status != 1) {
							map1.put("couponInfoType", 0);
							map1.put("coupon_id", storeCoupon.getId());
							map1.put("Coupon_name", storeCoupon.getCoupon_name());
							map1.put("Coupon_type", storeCoupon.getCoupon_type());
							map1.put("Coupon_employ_type", storeCoupon.getEmploy_type());
							map1.put("Coupon_amount", storeCoupon.getCoupon_amount());
							map1.put("Coupon_order_amount", storeCoupon.getCoupon_order_amount());
							map1.put("Coupon_begin_time",
									CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_begin_time()));
							map1.put("Coupon_end_time",
									CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_end_time()));
							map1.put("store_name", storeCoupon.getStore() == null ? "Soarmall"
									: storeCoupon.getStore().getStore_name());
							map1.put("store_id", storeCoupon.getStore() == null ? "" : storeCoupon.getStore().getId());
							couponsStore.add(map1);
						}

					}
				}
			} else {
				for (Coupon platCoupon : platform) {
					if (platCoupon.getCoupon_count() == 0
							|| platCoupon.getCoupon_count() > platCoupon.getCouponinfos().size()) {
						Map plateformMap = new HashMap();
						plateformMap.put("couponInfoType", 0);// 等于0等于未领取 //
																// 1:为已领取1
						plateformMap.put("coupon_id", platCoupon.getId());
						plateformMap.put("Coupon_name", platCoupon.getCoupon_name());
						plateformMap.put("Coupon_type", platCoupon.getCoupon_type());
						plateformMap.put("Coupon_employ_type", platCoupon.getEmploy_type());
						plateformMap.put("Coupon_amount", platCoupon.getCoupon_amount());
						plateformMap.put("Coupon_order_amount", platCoupon.getCoupon_order_amount());
						plateformMap.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_begin_time()));
						plateformMap.put("Coupon_end_time",
								CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_end_time()));
						plateformMap.put("store_name",
								platCoupon.getStore() == null ? "Soarmall" : platCoupon.getStore().getStore_name());
						plateformMap.put("store_id",
								platCoupon.getStore() == null ? "" : platCoupon.getStore().getId());
						couponsPlatform.add(plateformMap);
					}
				}

				for (Coupon storeCoupon : store) {
					if (storeCoupon.getCoupon_count() == 0
							|| storeCoupon.getCoupon_count() > storeCoupon.getCouponinfos().size()) {
						Map map1 = new HashMap();
						map1.put("couponInfoType", 0);// 等于0等于未领取 // 1:为已领取1
						map1.put("coupon_id", storeCoupon.getId());
						map1.put("Coupon_name", storeCoupon.getCoupon_name());
						map1.put("Coupon_type", storeCoupon.getCoupon_type());
						map1.put("Coupon_employ_type", storeCoupon.getEmploy_type());
						map1.put("Coupon_amount", storeCoupon.getCoupon_amount());
						map1.put("Coupon_order_amount", storeCoupon.getCoupon_order_amount());
						map1.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_begin_time()));
						map1.put("Coupon_end_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_end_time()));
						map1.put("store_name",
								storeCoupon.getStore() == null ? "Soarmall" : storeCoupon.getStore().getStore_name());
						map1.put("store_id", storeCoupon.getStore() == null ? "" : storeCoupon.getStore().getId());
						couponsStore.add(map1);
					}
				}

			}

		} else {
			for (Coupon platCoupon : platform) {
				params.clear();
				params.put("user", user.getId());
				params.put("coupon_id", platCoupon.getId());
				// params.put("status", 0);
				params.put("coupon_end_time", new Date());// + "and
															// obj.status=:status
															// "
				List<CouponInfo> plat_couponInfos = this.couponInfoService
						.query("select obj from CouponInfo obj where obj.user.id=:user and obj.coupon.id=:coupon_id "
								+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
				if (platCoupon.getCoupon_count() == 0
						|| platCoupon.getCoupon_count() > platCoupon.getCouponinfos().size()) {
					int status = -1;
					for (CouponInfo couponInfo : plat_couponInfos) {
						status = couponInfo.getStatus();
					}
					Map map1 = new HashMap();
					if (plat_couponInfos.size() > 0 && status != 1) {
						map1.put("couponInfoType", status == 0 ? 1 : 0);// 等于0等于未领取
																		// //
																		// 1:为已领取1
						map1.put("coupon_id", platCoupon.getId());
						map1.put("Coupon_name", platCoupon.getCoupon_name());
						map1.put("Coupon_type", platCoupon.getCoupon_type());
						map1.put("Coupon_employ_type", platCoupon.getEmploy_type());
						map1.put("Coupon_amount", platCoupon.getCoupon_amount());
						map1.put("Coupon_order_amount", platCoupon.getCoupon_order_amount());
						map1.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_begin_time()));
						map1.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_end_time()));
						map1.put("store_name",
								platCoupon.getStore() == null ? "Soarmall" : platCoupon.getStore().getStore_name());
						map1.put("store_id", platCoupon.getStore() == null ? "" : platCoupon.getStore().getId());
						couponsStore.add(map1);
					} else if (status != 1) {
						map1.put("couponInfoType", 0);
						map1.put("coupon_id", platCoupon.getId());
						map1.put("Coupon_name", platCoupon.getCoupon_name());
						map1.put("Coupon_type", platCoupon.getCoupon_type());
						map1.put("Coupon_employ_type", platCoupon.getEmploy_type());
						map1.put("Coupon_amount", platCoupon.getCoupon_amount());
						map1.put("Coupon_order_amount", platCoupon.getCoupon_order_amount());
						map1.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_begin_time()));
						map1.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", platCoupon.getCoupon_end_time()));
						map1.put("store_name",
								platCoupon.getStore() == null ? "Soarmall" : platCoupon.getStore().getStore_name());
						map1.put("store_id", platCoupon.getStore() == null ? "" : platCoupon.getStore().getId());
						couponsStore.add(map1);
					}
				}
			}

			for (Coupon storeCoupon : store) {
				params.clear();
				params.put("user", user.getId());
				params.put("coupon_id", storeCoupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> store_couponInfos = this.couponInfoService
						.query("select obj from CouponInfo obj where obj.user.id=:user and obj.coupon.id=:coupon_id "
								+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
				if (storeCoupon.getCoupon_count() == 0
						|| storeCoupon.getCoupon_count() > storeCoupon.getCouponinfos().size()) {
					int status = -1;
					for (CouponInfo couponInfo : store_couponInfos) {
						if (couponInfo.getId().equals(storeCoupon.getId())) {
							status = couponInfo.getStatus();
						}
					}
					Map map1 = new HashMap();
					if (store_couponInfos.size() > 0 && status != 1) {
						map1.put("couponInfoType", status == 0 ? 1 : 0);// 等于0等于未领取
																		// //
																		// 1:为已领取1
						map1.put("coupon_id", storeCoupon.getId());
						map1.put("Coupon_name", storeCoupon.getCoupon_name());
						map1.put("Coupon_type", storeCoupon.getCoupon_type());
						map1.put("Coupon_employ_type", storeCoupon.getEmploy_type());
						map1.put("Coupon_amount", storeCoupon.getCoupon_amount());
						map1.put("Coupon_order_amount", storeCoupon.getCoupon_order_amount());
						map1.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_begin_time()));
						map1.put("Coupon_end_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_end_time()));
						map1.put("store_name",
								storeCoupon.getStore() == null ? "Soarmall" : storeCoupon.getStore().getStore_name());
						map1.put("store_id", storeCoupon.getStore() == null ? "" : storeCoupon.getStore().getId());
						couponsStore.add(map1);
					} else if (status != 1) {
						map1.put("couponInfoType", 0);
						map1.put("coupon_id", storeCoupon.getId());
						map1.put("Coupon_name", storeCoupon.getCoupon_name());
						map1.put("Coupon_type", storeCoupon.getCoupon_type());
						map1.put("Coupon_employ_type", storeCoupon.getEmploy_type());
						map1.put("Coupon_amount", storeCoupon.getCoupon_amount());
						map1.put("Coupon_order_amount", storeCoupon.getCoupon_order_amount());
						map1.put("Coupon_begin_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_begin_time()));
						map1.put("Coupon_end_time",
								CommUtil.formatTime("yyyy.MM.dd", storeCoupon.getCoupon_end_time()));
						map1.put("store_name",
								storeCoupon.getStore() == null ? "Soarmall" : storeCoupon.getStore().getStore_name());
						map1.put("store_id", storeCoupon.getStore() == null ? "" : storeCoupon.getStore().getId());
						couponsStore.add(map1);
					}

				}
			}
		}
		map.put("platform", couponsPlatform);
		map.put("couponsStore", couponsStore);
		return Json.toJson(new Result(4200, "Successfully", map));
	}

	@RequestMapping("/banner/coupon.json")
	@ResponseBody
	public String bannerCouon(HttpServletRequest request, HttpServletResponse response, String token, String count) {
		Map coupon_map = new HashMap();
		User user = null;
		if (!CommUtil.null2String(token).equals("")) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		// 当用户为空时在获取游客身份
		String tourists = "";
		if (user == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("tourists")) {
						tourists = CommUtil.null2String(cookie.getValue());
					}
				}
			}
		}
		// 查询平台、店铺优惠券
		List<Coupon> platform_coupon = new ArrayList<Coupon>(); // 平台优惠券
		Map params = new HashMap();
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 0);
		params.put("employ_type", 0);
		platform_coupon = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=:employ_type "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_amount desc",
				params, CommUtil.null2Int(count) > 0 ? 0 : -1, CommUtil.null2Int(count));
		List<Coupon> store_coupon = new ArrayList<Coupon>(); // 店铺优惠券
		params.clear();
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		store_coupon = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_amount desc",
				params, CommUtil.null2Int(count) > 0 ? 0 : -1, CommUtil.null2Int(count));
		// 用户优惠券 根据时间降序，避免管理员给某一用户直接发放优惠券
		List<CouponInfo> user_coupon = new ArrayList<CouponInfo>();
		if (user != null) {
			params.clear();
			params.put("user_id", user.getId());
			user_coupon = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id order by obj.addTime desc", params, -1, -1);
		} else if (!tourists.equals("")) {
			params.clear();
			params.put("tourists", tourists);
			user_coupon = this.couponInfoService.query(
					"select obj from CouponInfo obj where obj.tourists=:tourists order by obj.addTime desc", params, -1,
					-1);
		}
		Map map = new HashMap();
		for (CouponInfo couponInfo : user_coupon) {
			if (couponInfo.getCoupon() != null) {
				map.put(couponInfo.getCoupon().getId(), couponInfo.getStatus());
			}
		}
		coupon_map.put("platform", this.getProperties(platform_coupon, map));
		coupon_map.put("couponsStore", this.getProperties(store_coupon, map));
		return Json.toJson(new Result(4200, "Successfully", coupon_map));
	}

	public List getProperties(List<Coupon> coupons, Map user_map) {
		List<Map> list = new ArrayList<Map>();
		if (coupons.size() > 0) {
			for (Coupon coupon : coupons) {
				Map map = new HashMap();
				if (!user_map.isEmpty()) {
					if (user_map.get(coupon.getId()) != null) {
						int status = CommUtil.null2Int(user_map.get(coupon.getId()));// 0:已领取
																						// 1：已使用
																						// -1：已过期
																						// -1:
																						// -2:未领取
						if(this.encapsulation_coupon(coupon, status) != null){
							list.add(this.encapsulation_coupon(coupon, status));
						}
					} else {
						if(this.encapsulation_coupon(coupon, -2) != null){
							list.add(this.encapsulation_coupon(coupon, -2));
						}
					};
				} else {
					if(this.encapsulation_coupon(coupon, -2) != null){
						list.add(this.encapsulation_coupon(coupon, -2));
					}
				}
			}
		}
		return list;
	}

	public Map encapsulation_coupon(Coupon coupon, int status) {
		if(status == 0 || status == -2){
			Map map = new HashMap();
			map.put("couponInfoType", status);
			map.put("coupon_id", coupon.getId());
			map.put("Coupon_name", coupon.getCoupon_name());
			map.put("Coupon_type", coupon.getCoupon_type());
			map.put("Coupon_employ_type", coupon.getEmploy_type());
			map.put("Coupon_amount", coupon.getCoupon_amount());
			map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
			map.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
			map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
			map.put("store_name", coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
			map.put("store_id", coupon.getStore() == null ? "" : coupon.getStore().getId());
			return map;
		}
		return null;
	}
}
