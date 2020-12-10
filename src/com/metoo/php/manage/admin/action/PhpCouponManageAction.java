package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/admin/coupon")
public class PhpCouponManageAction {

	@Autowired
	private ICouponService couponService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICouponInfoService couponInfoService;

	@RequestMapping("/save.json")
	public void save(HttpServletRequest request, HttpServletResponse response, String id) {
		WebForm wf = new WebForm();
		Coupon coupon = (Coupon) wf.toPo(request, Coupon.class);
		coupon.setAddTime(new Date());
		this.couponService.save(coupon);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(new Result(5200, "Successfully"), JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/send.json")
	public void send(HttpServletRequest reuqest, HttpServletResponse response, String id, String users, String type,
			String grades, String order_amount) throws IOException {
		int code = -1;
		List<User> userList = new ArrayList<User>();
		if (type.equals("all_user")) {
			Map params = new HashMap();
			params.put("userRole", "ADMIN");
			userList = this.userService.query(
					"select obj from User obj where obj.userRole!=:userRole order by obj.user_goods_fee desc", params,
					-1, -1);
		}
		if (type.equals("the_user")) {
			String[] user_names = users.split(",");
			for (String user_name : user_names) {
				User user = this.userService.getObjByProperty(null, "userName", user_name);
				userList.add(user);
			}
		}
		if (type.equals("all_store")) {
			userList = this.userService.query(
					"select obj from User obj where obj.store.id is not null order by obj.addTime desc", null, -1, -1);
		}
		if (type.equals("the_store")) {
			Map params = new HashMap();
			Set<Long> store_ids = new TreeSet<Long>();
			for (String grade : grades.split(",")) {
				store_ids.add(Long.parseLong(grade));
			}
			params.put("store_ids", store_ids);
			userList = this.userService.query("select obj from User obj where obj.store.grade.id in(:store_ids)",
					params, -1, -1);
		}
		if (type.equals("the_order")) {
			Map params = new HashMap();
			params.put("userRole", "ADMIN");
			List<User> list = this.userService.query(
					"select obj from User obj where obj.userRole!=:userRole order by obj.user_goods_fee desc", params,
					-1, -1);
			for (int i = 0; i < list.size(); i++) {
				Long user_id = CommUtil.null2Long(list.get(i).getId());
				double order_total_amount = CommUtil.null2Double(list.get(i).getUser_goods_fee());
				if (order_total_amount >= CommUtil.null2Double(order_amount)) {
					User user = this.userService.getObjById(user_id);
					userList.add(user);
				} else {
					break;
				}
			}
		}
		Coupon coupon = this.couponService.getObjById(CommUtil.null2Long(id));
		for (int i = 0; i < userList.size(); i++) {
			int size = coupon.getCouponinfos().size();
			if (coupon.getCoupon_count() > 0) {
				if (CommUtil.subtract(coupon.getCoupon_count(), size) > 0) {
					if (CommUtil.subtract(CommUtil.subtract(coupon.getCoupon_count(), size), userList.size()) >= 0) {
						CouponInfo info = new CouponInfo();
						info.setAddTime(new Date());
						info.setCoupon(coupon);
						info.setCoupon_sn(UUID.randomUUID().toString());
						info.setUser(userList.get(i));
						info.setStore_id(CommUtil.null2Long(-1));
						this.couponInfoService.save(info);
						code = 5200;
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
				info.setStore_id(CommUtil.null2Long(-1));
				this.couponInfoService.save(info);
				code = 5200;
			}
		}
		this.returnJson(Json.toJson(new Result(code), JsonFormat.compact()), response);
	}
	
	@RequestMapping("/del.json")
	public void del2(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "mulitId", required = true) String mulitId) {
		Result result = null;
		if (!mulitId.equals("")) {
			String[] ids = mulitId.split(",");
			for (String id : ids) {
				if (!id.equals("")) {
					this.couponService.testDel(CommUtil.null2Long(id));
				}
			}
			
			result = new Result(5200, "Successfully");
		} else {
			result = new Result(5405, "The resource does not exist");
		}
		this.returnJson(Json.toJson(result, JsonFormat.compact()), response);
	}
	
	/**
	 * 
	 * @param json
	 * @param response
	 */
	public void returnJson(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
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
