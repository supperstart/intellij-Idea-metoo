package com.metoo.php.manage.admin.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreGrade;
import com.metoo.foundation.domain.StorePoint;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IRoleService;
import com.metoo.foundation.service.IStoreGradeService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.IUserService;

@Controller
@RequestMapping("/php/admin/")
public class MStoreManageAction {

	@Autowired
	private IStoreService storeService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IStoreGradeService storeGradService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;

	@RequestMapping(value = "store.json", method = RequestMethod.POST)
	public void save(HttpServletRequest request, HttpServletResponse response, String id, String validity,
			String store_status, String user_id, String area_id, String grade_id, String gc_main_id) {
		Result result = null;
		WebForm wf = new WebForm();
		Store store = null;
		if (id.equals("")) {
			store = wf.toPo(request, Store.class);
			store.setAddTime(new Date());
		} else {
			Store obj = this.storeService.getObjById(Long.parseLong(id));
			store = (Store) wf.toPo(request, obj);
		}
		if (store_status != null && !store_status.equals("")) {
			if (store_status.equals("5") || store_status.equals("10")) {// 入驻审核中
				store.setStore_status(CommUtil.null2Int(store_status));
			} else if (store_status.equals("6") || store_status.equals("11")) {// 入驻审核失败
				store.setStore_status(CommUtil.null2Int(store_status));
			} else if (store_status.equals("15")) {// 入驻成功，给用户赋予卖家权限
				if (user_id != null && !user_id.equals("")) {
					User user = this.userService.getObjById(Long.parseLong(user_id));
					store.setUser(user);
					Area area = this.areaService.getObjById(Long.parseLong(area_id));
					store.setArea(area);
					StoreGrade storeGrade = this.storeGradService.getObjById(Long.parseLong(grade_id));
					store.setGrade(storeGrade);
					store.setGc_main_id(Long.parseLong(gc_main_id));
					store.setValidity(CommUtil.formatDate(validity));
					this.storeService.save(store);
					if (store.getPoint() == null) {
						StorePoint sp = new StorePoint();
						sp.setAddTime(new Date());
						sp.setStore(store);
						sp.setStore_evaluate(BigDecimal.valueOf(0));
						sp.setDescription_evaluate(BigDecimal.valueOf(0));
						sp.setService_evaluate(BigDecimal.valueOf(0));
						sp.setShip_evaluate(BigDecimal.valueOf(0));
						this.storePointService.save(sp);
					}
				}
				String store_user_id = CommUtil.null2String(store.getUser().getId());
				if (store_user_id != null && !store_user_id.equals("")) {
					User store_user = this.userService.getObjById(Long.parseLong(store_user_id));
					store_user.setStore(store);
					if (!store_user.getUserRole().equalsIgnoreCase("admin")) {
						store_user.setUserRole("SELLER");
					} else {
						store_user.setUserRole("ADMIN_SELLER");
					}
					Map params = new HashMap();
					params.put("type", "SELLER");
					List<Role> roles = this.roleService.query("select obj from Role obj where obj.type=:type", params,
							-1, -1);
					store_user.getRoles().addAll(roles);
					this.userService.update(store_user);
					if (store.getStore_start_time() == null) {// 开店时间为空，意味着入驻审核通过，成功开店
						store.setStore_start_time(new Date());
						/*
						 * this.send_site_msg(request,
						 * "msg_toseller_store_update_allow_notify", store);
						 */
					}
				}
				store.setStore_status(CommUtil.null2Int(store_status));
			} else if (store_status.equals("20")) {// 关闭违规店铺发送站内信提醒
				store.setStore_status(CommUtil.null2Int(store_status));
				if (!id.equals("") && store.getStore_status() == 20) {
					/*
					 * this.send_site_msg(request,
					 * "msg_toseller_store_closed_notify", store);
					 */
				}
			}
		}
		if (store.isStore_recommend()) {
			store.setStore_recommend_time(new Date());
		} else
			store.setStore_recommend_time(null);
		this.storeService.update(store);
		result = new Result(5200, "Successfully");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "store.json", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "mulitId", required = true) String mulitId) {
		Result result = new Result();
		int code = -1;
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Store store = this.storeService.getObjById(Long.parseLong(id));
				Map params = new HashMap();
				if (store != null) {
					store.getUser().setStore(null);
					User user = store.getUser();
					if (user != null) {
						Set<Role> roles = user.getRoles();
						Set<Role> new_role = new HashSet<Role>();
						for (Role role : roles) {
							if (!role.getType().equals("SELLER")) {
								new_role.add(role);
							}
						}
						user.getRoles().clear();
						user.getRoles().addAll(new_role);
						user.setStore_apply_step(0);
						this.userService.update(user);
						for (User u : user.getChilds()) {
							roles = u.getRoles();
							Set<Role> new_roles2 = new HashSet<Role>();
							for (Role role : roles) {
								if (!role.getType().equals("SELLER")) {
									new_roles2.add(role);
								}
							}
							u.getRoles().clear();
							u.getRoles().addAll(new_roles2);
							u.setStore_apply_step(0);
							this.userService.update(u);
						}
					}
					params.clear();
					params.put("store_id", store.getId());
					List<Coupon> coupons = this.couponService
							.query("select obj from Coupon obj where obj.store.id=:store_id", params, -1, -1);
					for (Coupon coupon : coupons) {
						for (CouponInfo couponinfo : coupon.getCouponinfos()) {
							this.couponInfoService.delete(couponinfo.getId());
						}
						this.couponService.delete(coupon.getId());
					}
					store.setDeleteStatus(-1);
					this.storeService.update(store);
					code = 5200;
				}
			} else {
				code = 5400;
			}
		}
		result = new Result(code, "Successfully");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
