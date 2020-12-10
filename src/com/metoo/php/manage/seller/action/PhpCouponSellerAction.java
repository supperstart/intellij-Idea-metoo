package com.metoo.php.manage.seller.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("php/seller/")
public class PhpCouponSellerAction {

	@Autowired
	private ICouponService couponService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICouponInfoService couponInfoService;

	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            优惠券ID
	 * @param uid
	 *            用户ID
	 */
	@RequestMapping(value = "coupon.json", method = RequestMethod.POST)
	public void save(HttpServletRequest request, HttpServletResponse response, String id,
			@RequestParam(value = "uid", required = true) String uid) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		if (user != null) {
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			WebForm wf = new WebForm();
			Coupon coupon = null;
			if (id.equals("")) {
				coupon = wf.toPo(request, Coupon.class);
				coupon.setAddTime(new Date());
			} else {
				coupon = this.couponService.getObjById(CommUtil.null2Long(id));
			}
			if (id.equals("")) {
				coupon.setCoupon_type(1);// 设置为商家发布
				coupon.setStore(store);
				this.couponService.save(coupon);
			} else {
				this.couponService.update(coupon);
			}
			result = new Result(5200, "Success");
		} else {
			result = new Result(5400, "Bad request");
		}
		this.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	@RequestMapping(value = "coupon.json", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "id", required = true) String mulitid, String uid) {
		Result result = null;
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		if (user != null) {
			user = user.getParent() == null ? user : user.getParent();
			Store store = user.getStore();
			for (String id : mulitid.split(",")) {
				Coupon coupon = this.couponService.getObjById(CommUtil.null2Long(id));
				if (coupon != null && coupon.getStore().getId().equals(store.getId())) {
					this.couponService.delete(coupon.getId());
				}
			}
			result = new Result(5200, "Success");
		} else {
			result = new Result(5400, "Bad request");
		}
		this.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}

	@RequestMapping("coupon/send.json")
	public void send(HttpServletRequest request, HttpServletResponse response, String type, String id, String users,
			String uid) {
		Result result = null;
		int code = -1;
		Coupon coupon = this.couponService.getObjById(CommUtil.null2Long(id));
		User user = this.userService.getObjById(CommUtil.null2Long(uid));
		List<User> userList = new ArrayList<User>();
		if (coupon != null && user != null) {
			Store store = coupon.getStore();
			if (type.equals("all")) {
				Map params = new HashMap();
				params.put("user.Role", "ADMIN");
				params.put("user_id", store.getUser().getId());
				userList = this.userService.query(
						"select obj from User obj where obj.userRole!=:userRole and obj.id!= :user_id order by obj.user_goods_fee desc",
						params, -1, -1);
			}
			if (type.equals("the")) {
				String[] names = users.split(",");
				for (String name : names) {
					User obj = this.userService.getObjByProperty(null, "userName", name);
					if (obj.getId() != CommUtil.null2Long(store.getUser().getId())) {
						userList.add(user);
					}
				}
			}
			for (int i = 0; i < userList.size(); i++) {
				int size = coupon.getCouponinfos().size();
				if (coupon.getCoupon_count() > 0) {
					if (CommUtil.subtract(coupon.getCoupon_count(), size) > 0) {
						if (CommUtil.subtract(CommUtil.subtract(coupon.getCoupon_count(), size),
								userList.size()) >= 0) {
							CouponInfo info = new CouponInfo();
							info.setAddTime(new Date());
							info.setCoupon(coupon);
							info.setCoupon_sn(UUID.randomUUID().toString());
							info.setUser(userList.get(i));
							info.setStore_id(store.getId());
							this.couponInfoService.save(info);
							code = 5405;
							break;
						}
						code = 5405;
						break;
					}
				} else {
					CouponInfo info = new CouponInfo();
					info.setAddTime(new Date());
					info.setCoupon(coupon);
					info.setCoupon_sn(UUID.randomUUID().toString());
					info.setUser(userList.get(i));
					info.setStore_id(store.getId());
					this.couponInfoService.save(info);
					code = 5200;
				}
			}
		} else {
			code = 5400;
		}
		this.returnJson(Json.toJson(new Result(code), JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @param json
	 * @param response
	 */
	public void returnJson(String json, HttpServletResponse response) {
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
