package com.metoo.app.view.web.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.constant.Globals;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.ShipAddress;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.IntegralViewTools;

@Component
public class MGoodsViewTools {

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IActivityGoodsService actgoodsService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IUserService userService;
	@Autowired
	private MCartViewTools mcartViewTools;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private LuceneVoTools luceneVoTools;

	/**
	 * 
	 * @description 根据商品id查询商品规格
	 * @param goods_id
	 *            商品id
	 */
	public Map<String, Object> spec(Goods goods) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> colorList = new ArrayList<Map<String, Object>>();
		;
		List<Map<String, Object>> specList = new ArrayList<Map<String, Object>>();
		;
		if (null != goods && goods.getInventory_type().equals("spec")) {
			List<CGoods> cgoodsList = goods.getCgoods();
			Set<Long> set = null;
			List<GoodsSpecification> specs = new ArrayList<GoodsSpecification>();
			if (cgoodsList.size() > 0) {
				colorList = new ArrayList<Map<String, Object>>();
				set = new HashSet<Long>();
				for (CGoods obj : cgoodsList) {
					if (obj.getGoods_inventory() > 0 && obj.getGoods_disabled().equals("0")) {
						Map<String, Object> colorMap = new HashMap<String, Object>();
						if (obj.getSpec_color() != null && !"".equals(obj.getSpec_color())) {
							colorMap.put("value", obj.getSpec_color());
							colorMap.put("photo",
									obj.getGoods_photos().size() > 0
											? this.configService.getSysConfig().getImageWebServer()
													+ "/" + obj.getGoods_photos().get(0).getPath() + "/"
													+ obj.getGoods_photos().get(0).getName()
											: this.configService.getSysConfig().getImageWebServer() + "/"
													+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
													+ this.configService.getSysConfig().getGoodsImage().getName());
							if (!colorList.contains(colorMap)) {
								colorList.add(colorMap);
							}
						}
						if (!"".equals(obj.getCombination_id())) {
							String spec_ids[] = obj.getCombination_id().split("_");
							for (String spec_id : spec_ids) {
								set.add(CommUtil.null2Long(spec_id));
							}
						}
					}
				}
				map.put("color", colorList);
				Iterator<Long> iterator = set.iterator();
				while (iterator.hasNext()) {
					Long id = iterator.next();
					GoodsSpecification spec = this.goodsSpecPropertyService.getObjById(id).getSpec();
					if (!specs.contains(spec)) {
						specs.add(spec);
					}
				}
				java.util.Collections.sort(specs, new Comparator<GoodsSpecification>() {
					@Override
					public int compare(GoodsSpecification gs1, GoodsSpecification gs2) {
						// TODO Auto-generated method stub
						return gs1.getSequence() - gs2.getSequence();
					}
				});

				specList = new ArrayList<Map<String, Object>>();
				for (GoodsSpecification goodsSpecification : specs) {
					Map<String, Object> goodsSpecificationMap = new HashMap<String, Object>();
					goodsSpecificationMap.put("id", goodsSpecification.getId());
					goodsSpecificationMap.put("spec_name", goodsSpecification.getName());
					List<GoodsSpecProperty> goodsSpecPropertyList = goods.getGoods_specs();
					List<Map<String, Object>> gsp_list = new ArrayList<Map<String, Object>>();
					for (GoodsSpecProperty goodsSpecProperty : goodsSpecPropertyList) {
						Map<String, Object> gsp_map = new HashMap<String, Object>();
						if (goodsSpecProperty.getSpec().getId() == goodsSpecification.getId()) {
							gsp_map.put("gsp_id", goodsSpecProperty.getId());
							if (goodsSpecification.getType() != null && !goodsSpecification.getType().equals("")) {
								String tyoe = goodsSpecification.getType();
								if (goodsSpecification.getType().equals("img")) {
									gsp_map.put("gsp_value",
											this.configService.getSysConfig().getImageWebServer() + "/"
													+ goodsSpecProperty.getSpecImage().getPath() + "/"
													+ goodsSpecProperty.getSpecImage().getName());
								}
								if (goodsSpecification.getType().equals("text")) {
									gsp_map.put("gsp_value", goodsSpecProperty.getValue());
								}
							}
							gsp_list.add(gsp_map);
						}
					}
					goodsSpecificationMap.put("gsp_list", gsp_list);
					specList.add(goodsSpecificationMap);
				}
			}
		}
		map.put("color", colorList);
		map.put("spec_info", specList);
		return map;
	}

	/**
	 * @description 根据商品id查询万能属性
	 * 
	 * @param id
	 * @return
	 */
	public Set<Map<String, Object>> goodsColor(String id) {
		Goods goods = null;
		if (id != null && !id.equals("")) {
			goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			List<CGoods> childGoods = goods.getCgoods();
			Set<Map<String, Object>> objList = new HashSet<Map<String, Object>>();
			for (CGoods obj : childGoods) {
				if (obj.getSpec_color() != null && !"".equals(obj.getSpec_color())) {
					Map<String, Object> objMap = new HashMap<String, Object>();
					objMap.put("value", obj.getSpec_color());
					objMap.put("photo",
							obj.getGoods_photos().size() > 0
									? this.configService.getSysConfig().getImageWebServer() + "/"
											+ obj.getGoods_photos().get(0).getPath() + "/"
											+ obj.getGoods_photos().get(0).getName()
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
											+ this.configService.getSysConfig().getGoodsImage().getName());
					if (!objList.contains(objMap)) {
						objList.add(objMap);
					}
				}
			}

			return objList;
		}
		return null;
	}

	public List<Map<String, Object>> photo(String id) {
		Goods goods = null;
		if (id != null && !id.equals("")) {
			goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			List<CGoods> cgoods = goods.getCgoods();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (CGoods obj : cgoods) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("photo",
						obj.getGoods_photos().size() > 0 ? this.configService.getSysConfig().getImageWebServer() + "/"
								+ obj.getGoods_photos().get(0).getPath() + "/" + obj.getGoods_photos().get(0).getName()
								: this.configService.getSysConfig().getImageWebServer() + "/"
										+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
										+ this.configService.getSysConfig().getGoodsImage().getName());
				list.add(map);

			}
			return list;
		}
		return null;
	}

	/**
	 * @description Collections 移动端商品规格信息 将商品属性归类便于前台展示 (弃用：你展示无库存规格)
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> goodsGenericSpec(String id) {
		Map<String, Object> map = new HashMap<String, Object>();
		Goods goods = null;
		List<GoodsSpecification> specs = new ArrayList<GoodsSpecification>();
		if (id != null && !id.equals("")) {
			goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			if ("spec".equals(goods.getInventory_type())) {
				for (GoodsSpecProperty gsp : goods.getGoods_specs()) {
					GoodsSpecification spec = gsp.getSpec();
					if (!specs.contains(spec)) {
						specs.add(spec);
					}
				}
				java.util.Collections.sort(specs, new Comparator<GoodsSpecification>() {
					@Override
					public int compare(GoodsSpecification gs1, GoodsSpecification gs2) {
						// TODO Auto-generated method stub
						return gs1.getSequence() - gs2.getSequence();
					}
				});
			}
		}
		List<Map<String, Object>> spec_list = new ArrayList<Map<String, Object>>();
		for (GoodsSpecification goodsSpecification : specs) {
			Map<String, Object> goodsSpecificationMap = new HashMap<String, Object>();
			goodsSpecificationMap.put("id", goodsSpecification.getId());
			goodsSpecificationMap.put("spec_name", goodsSpecification.getName());
			List<GoodsSpecProperty> goodsSpecPropertyList = goods.getGoods_specs();
			List<Map<String, Object>> gsp_list = new ArrayList<Map<String, Object>>();
			for (GoodsSpecProperty goodsSpecProperty : goodsSpecPropertyList) {
				Map<String, Object> gsp_map = new HashMap<String, Object>();
				if (goodsSpecProperty.getSpec().getId() == goodsSpecification.getId()) {
					gsp_map.put("gsp_id", goodsSpecProperty.getId());
					if (goodsSpecification.getType() != null && !goodsSpecification.getType().equals("")) {
						String tyoe = goodsSpecification.getType();
						if (goodsSpecification.getType().equals("img")) {
							gsp_map.put("gsp_value",
									this.configService.getSysConfig().getImageWebServer() + "/"
											+ goodsSpecProperty.getSpecImage().getPath() + "/"
											+ goodsSpecProperty.getSpecImage().getName());
						}
						if (goodsSpecification.getType().equals("text")) {
							gsp_map.put("gsp_value", goodsSpecProperty.getValue());
						}
					}
					gsp_list.add(gsp_map);
				}
			}
			goodsSpecificationMap.put("gsp_list", gsp_list);
			spec_list.add(goodsSpecificationMap);
		}
		map.put("spec_info", spec_list);
		return map;
	}

	/**
	 * @description 普通优惠券
	 * @param obj
	 * @param user
	 * @param request
	 * @param response
	 * @return
	 */
	public List<Map<String, Object>> coupon(Goods obj, User user, HttpServletRequest request,
			HttpServletResponse response) {
		List<Coupon> storeCoupons = new ArrayList<Coupon>();
		List<Coupon> selfCoupons = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("store_id", obj.getGoods_store().getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		// 未登录用户商家优惠券列表
		storeCoupons = this.couponService
				.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
						+ "order by obj.coupon_order_amount asc", params, -1, -1);
		params.remove("store_id");
		params.put("coupon_type", 0);
		// 未登录用户自营优惠券列表
		selfCoupons = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_order_amount asc",
				params, -1, -1);
		storeCoupons.addAll(selfCoupons);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (user != null) {
			params.clear();
			params.put("user_id", user.getId());
			params.put("status", 0);
			params.put("store_id", obj.getGoods_store().getId());

			List<CouponInfo> store_couponInfos = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.store_id=:store_id " + "and obj.user.id=:user_id "
							+ "and obj.status=:status", params, -1, -1);
			params.put("store_id", null);
			List<CouponInfo> self_couponInfos = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.store_id=:store_id and obj.user.id=:user_id "
							+ "and obj.status=:status ", params, -1, -1);
			store_couponInfos.addAll(self_couponInfos);
			for (CouponInfo couponInfo : store_couponInfos) {
				Coupon coupon = couponInfo.getCoupon();
				Map<String, Object> map = new HashMap<String, Object>();
				if (CommUtil.subtract(coupon.getCoupon_end_time(), new Date()) >= 0) {
					map.put("CouponInfo_id", couponInfo.getId());
					map.put("Coupon_sn", couponInfo.getCoupon_sn());
					map.put("Coupon_name", coupon.getCoupon_name());
					map.put("Coupon_amount", coupon.getCoupon_amount());
					map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
					map.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
					map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
					map.put("coupon_type", coupon.getCoupon_type());
					map.put("Coupon_employ_type", coupon.getEmploy_type());
					map.put("couponInfoType", 1);// 等于0等于未领取 1:为已领取1
					map.put("Status", couponInfo.getStatus()); // 优惠券信息状态，默认为0，,使用后为1,-1为过期
					Store store = couponInfo.getCoupon().getStore();
					map.put("store_id", store == null ? "" : store.getId());
					map.put("store_name", store == null ? "" : store.getStore_name());
					list.add(map);
				}

			}
		}
		List<Map> coupons = new ArrayList<Map>();
		for (Coupon coupon : storeCoupons) {
			Map map = new HashMap();
			Map flag = this.flag(coupon.getId(), user);
			if (flag != null && CommUtil.null2String(flag.get("status")).equals("1")) {
				break;
			}
			if (coupon.getCoupon_count() == 0) {
				if (user == null) {
					map.put("couponInfoType", 0);// 等于0等于未领取 1:为已领取1
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
					list.add(map);
				}
				if (user != null && flag == null) {
					map.put("couponInfoType", 0);// 等于0等于未领取 1:为已领取1
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
					list.add(map);
				}
			} else {
				if (coupon.getCoupon_count() > coupon.getCouponinfos().size()) {
					if (user == null) {
						map.put("couponInfoType", 0);// 等于0等于未领取 1:为已领取1
						map.put("coupon_id", coupon.getId());
						map.put("Coupon_name", coupon.getCoupon_name());
						map.put("Coupon_type", coupon.getCoupon_type());
						map.put("Coupon_employ_type", coupon.getEmploy_type());
						map.put("Coupon_amount", coupon.getCoupon_amount());
						map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
						map.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
						map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
						map.put("store_name",
								coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
						map.put("store_id", coupon.getStore() == null ? "" : coupon.getStore().getId());
						list.add(map);
					}
					if (user != null && flag == null) {
						map.put("couponInfoType", 0);// 等于0等于未领取 1:为已领取1
						map.put("id", coupon.getId());
						map.put("Coupon_name", coupon.getCoupon_name());
						map.put("Coupon_employ_type", coupon.getEmploy_type());
						map.put("Coupon_type", coupon.getCoupon_type());
						map.put("Coupon_amount", coupon.getCoupon_amount());
						map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
						map.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
						map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
						map.put("store_name",
								coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
						map.put("store_id", coupon.getStore() == null ? "" : coupon.getStore().getId());
						list.add(map);
					}
				}
			}

		}
		return list;
	}

	public List<Map> goodsCoupon(Goods obj, User u) {
		if (obj != null) {
			User user = null;
			if (u != null) {
				user = this.userService.getObjById(u.getId());
			}
			Store store = obj.getGoods_store();
			Map params = new HashMap();
			/*
			 * params.put("coupon_order_amount",
			 * BigDecimal.valueOf(CommUtil.null2Double(obj.
			 * getGoods_current_price())));
			 */

			List<Coupon> coupons = new ArrayList<Coupon>();
			params.put("store_id", obj.getGoods_store().getId());
			params.put("coupon_begin_time", new Date());
			params.put("coupon_end_time", new Date());
			params.put("coupon_type", 1);
			params.put("employ_type", 3);
			coupons = this.couponService
					.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type!=:employ_type "
							+ "and obj.coupon_begin_time<=:coupon_begin_time "
							+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
							+ "order by obj.coupon_order_amount asc", params, -1, -1);
			coupons.addAll(this.goodsGeneralCoupon());
			List<Map> infos = new ArrayList<Map>();
			for (Coupon coupon : coupons) {
				Map map = new HashMap();
				Map flag = this.flag(coupon.getId(), user);
				if (flag != null && CommUtil.null2String(flag.get("status")).equals("1")) {
					break;
				}
				if (coupon.getCoupon_count() == 0) {
					if (user != null && flag == null) {
						map.put("couponInfoType", 0);// 等于0等于未领取 1:为已领取1
						map.put("status", flag != null ? flag.get("status") : -2);// 未使用时该值为空
						map.put("id", coupon.getId());
						map.put("Coupon_name", coupon.getCoupon_name());
						map.put("Coupon_type", coupon.getCoupon_type());
						map.put("Coupon_employ_type", coupon.getEmploy_type());
						map.put("Coupon_amount", coupon.getCoupon_amount());
						map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
						map.put("Coupon_begin_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
						map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
						map.put("store_name",
								coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
						map.put("store_id", coupon.getStore() == null ? "" : coupon.getStore().getId());
						infos.add(map);
					}
				} else {
					if (coupon.getCoupon_count() > coupon.getCouponinfos().size()) {
						if (user != null && flag == null) {
							map.put("couponInfoType", 1);// 等于0等于未领取 1:为已领取1
							map.put("status", flag != null ? flag.get("status") : -1);// 未使用时该值为空
							map.put("id", coupon.getId());
							map.put("Coupon_name", coupon.getCoupon_name());
							map.put("Coupon_employ_type", coupon.getEmploy_type());
							map.put("Coupon_type", coupon.getCoupon_type());
							map.put("Coupon_amount", coupon.getCoupon_amount());
							map.put("Coupon_order_amount", coupon.getCoupon_order_amount());
							map.put("Coupon_begin_time",
									CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_begin_time()));
							map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", coupon.getCoupon_end_time()));
							map.put("store_name",
									coupon.getStore() == null ? "Soarmall" : coupon.getStore().getStore_name());
							map.put("store_id", coupon.getStore() == null ? "" : coupon.getStore().getId());
							infos.add(map);
						}
					}
				}

			}
			return infos;
		}
		return null;
	}

	// 平台优惠券
	public List<Coupon> goodsGeneralCoupon() {
		List<Coupon> couponinfos = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 0);
		// params.put("employ_type", 3);
		return couponinfos = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_order_amount asc",
				params, -1, -1);
	}

	//
	public Map flag(Long id, User user) {
		boolean flag = false;
		if (user != null) {
			Map map = new HashMap();
			Map params = new HashMap();
			params.put("user_id", user.getId());
			params.put("coupon_id", id);
			List<CouponInfo> infos = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id and obj.coupon.id=:coupon_id "
							+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
			if (infos == null || infos.size() > 1 || infos.isEmpty()) {
				return null;
			} else {
				CouponInfo couponinfo = infos.get(0);
				map.put("status", couponinfo.getStatus());
				return map;
			}
		} else {
			return null;
		}
	}

	/**
	 * @description 商品属性查询
	 * @param objList
	 * @param resultMap
	 */
	public void goods(List<Goods> goodsList, Map resultMap) {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Set set = new HashSet();
		for (Goods obj : goodsList) {
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			map.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_price());
			map.put("goods_current_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			set.add(map);
		}
		resultMap.put("goodsList", set);
	}
	
	
	public void pointGoods(List<Goods> goodsList, Map resultMap) {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Set set = new HashSet();
		for (Goods obj : goodsList) {
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			map.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_price());
			map.put("goods_current_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			set.add(map);
		}
		resultMap.put("goods", set);
	}
	
	public List pointGoods(List<Goods> goodsList) {
		List<Map> list = new ArrayList<Map>();
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		for (Goods obj : goodsList) {
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			map.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_price());
			map.put("goods_current_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			map.put("goods_point_num", obj.getPointNum());
			list.add(map);
		}
		return list;
	}

	public List goods(List<Goods> goodsList, User user) {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		List<Map> list = new ArrayList<Map>();
		for (Goods obj : goodsList) {
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			map.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_inventory", obj.getGoods_inventory());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_price());
			map.put("goods_current_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			map.put("goods_pointNum", obj.getPointNum());
			map.put("user_pointNum", user == null ? 0 : user.getPointNum());
			list.add(map);
		}
		return list;
	}

	/**
	 * @description 商品属性查询
	 * @param objList
	 * @param resultMap
	 */
	// public void goods(Goods obj, Map map){
	// String imageWebServer =
	// this.configService.getSysConfig().getImageWebServer();
	// if(obj != null){
	// map.put("goods_id", obj.getId());
	// map.put("goods_name", obj.getGoods_name());
	// map.put("goods_photo", obj.getGoods_main_photo() != null
	// ? imageWebServer + "/" + obj.getGoods_main_photo().getPath()
	// + "/"
	// + obj.getGoods_main_photo().getName()
	// + "_middle."
	// + obj.getGoods_main_photo().getExt()
	// : imageWebServer);
	// map.put("goods_type", obj.getGoods_type());
	// map.put("goods_well_evaluate", obj.getWell_evaluate());
	// map.put("goods_price", obj.getGoods_price());
	// map.put("goods_current_price", obj.getGoods_current_price());
	// map.put("store_status", obj.getGoods_store().getStore_status());
	// map.put("goods_collect", obj.getGoods_collect());
	// map.put("goods_status", obj.getGoods_status());
	// map.put("store_status", obj.getGoods_store().getStore_status());
	// }
	// }
	//
	/**
	 * @description 立即下单-商品信息
	 * @param obj
	 * @return
	 */
	public Map orderGoods(Goods obj, int count, String gsp, String color, boolean point, String language) {
		if (obj != null) {
			String imageWebServer = this.configService.getSysConfig().getImageWebServer();
			String[] gsp_ids = gsp.split(",");
			String spec_info = "";
			for (String gsp_id : gsp_ids) {
				GoodsSpecProperty spec_property = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gsp_id));
				if (spec_property != null) {
					spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + "<br> "
							+ spec_info;
				}
			}
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			if ("1".equals(language)) {
				map.put("goods_name", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
						? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
			}
			map.put("ksa_goods_name", obj.getKsa_goods_name());
			map.put("goods_photo",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			map.put("store_status", obj.getGoods_store().getStore_status());
			map.put("color", color);
			map.put("goods_count", count);// 购买数量
			map.put("goods_spec_info", spec_info);
			map.put("goods_gsp", gsp);
			map.put("goods_point", obj.getPoint());
			double goods_current_price = 0;
			int goods_inventory = 0;
			goods_current_price = CommUtil.null2Double(obj.getGoods_current_price());
			goods_inventory = obj.getGoods_inventory();
			if (obj.getInventory_type().equals("spec")) {
				goods_current_price = CommUtil.null2Double(
						this.mcartViewTools.generic_default_info_color(obj, gsp, color).get("goods_current_price"));// 计算商品价格
				goods_inventory = CommUtil
						.null2Int(this.mcartViewTools.generic_default_info_color(obj, gsp, color).get("count"));// 计算商品库存信息
			}
			map.put("goods_current_price", goods_current_price);
			map.put("goods_inventory", goods_inventory);
			return map;
		}
		return null;
	}
	
	/**
	 * @param obj
	 * @return
	 * @description 立即下单-商品信息 v2：删除字段point,
	 */
	public Map orderGoodsV2(Goods obj, int count, String gsp, String color, String language) {
		if (obj != null) {
			String imageWebServer = this.configService.getSysConfig().getImageWebServer();
			String[] gsp_ids = gsp.split(",");
			String spec_info = "";
			for (String gsp_id : gsp_ids) {
				GoodsSpecProperty spec_property = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gsp_id));
				if (spec_property != null) {
					spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + "<br> "
							+ spec_info;
				}
			}
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			if ("1".equals(language)) {
				map.put("goods_name", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
						? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
			}
			map.put("ksa_goods_name", obj.getKsa_goods_name());
			map.put("goods_photo",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_current_price());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			map.put("store_status", obj.getGoods_store().getStore_status());
			map.put("color", color);
			map.put("goods_count", count);// 购买数量
			map.put("goods_spec_info", spec_info);
			map.put("goods_gsp", gsp);
			map.put("goods_point", obj.getPoint());
			double goods_current_price = 0;
			int goods_inventory = 0;
			goods_current_price = CommUtil.null2Double(obj.getGoods_current_price());
			goods_inventory = obj.getGoods_inventory();
			if (obj.getInventory_type().equals("spec")) {
				goods_current_price = CommUtil.null2Double(
						this.mcartViewTools.generic_default_info_color(obj, gsp, color).get("goods_current_price"));// 计算商品价格
				goods_inventory = CommUtil
						.null2Int(this.mcartViewTools.generic_default_info_color(obj, gsp, color).get("count"));// 计算商品库存信息
			}
			map.put("goods_current_price", goods_current_price);
			map.put("goods_inventory", goods_inventory);
			return map;
		}
		return null;
	}

	/**
	 * @description 计算商品满减价格 (该方法不可用)
	 * @param obj
	 */
	public Map enoughReducePrice(Goods obj, EnoughReduce er) {
		Map map = new HashMap();
		if (obj != null && er != null) {
			if (er.getErstatus() == 10 && er.getErbegin_time().before(new Date())) {
				Map er_json = (Map) Json.fromJson(er.getEr_json());
				double goods_price = CommUtil.null2Double(obj.getGoods_current_price());
				double reduce = 0; // 满减价格
				double goods_amount = 0; // 商品总价
				double after = 0; // 减后价格
				String er_string = "";
				for (Object enough : er_json.keySet()) {
					if (goods_price > CommUtil.null2Double(enough)) {
						reduce = CommUtil.null2Double(er_json.get(enough));
						goods_amount = goods_price;
						double af = Math.round((goods_amount - reduce) * 100) / 100.0;
						BigDecimal afbd = new BigDecimal(af);
						BigDecimal afbd2 = afbd.setScale(2, BigDecimal.ROUND_HALF_UP);
						after = CommUtil.null2Double(afbd2);
						map.put("reduce", reduce);
						map.put("goods_amount", goods_amount);
						map.put("after", after);
					}
				}
			}
			return map;
		}
		return null;
	}

	/**
	 * @description 子查询
	 * @param ids
	 * @return
	 * @throws SQLException
	 * 
	 *             String sql =
	 *             "select id,goods_name,goods_price,goods_current_price,goods_main_photo_id from "
	 *             + Globals.DEFAULT_TABLE_SUFFIX +
	 *             "goods where id in(select goods_vice_id from metoo_goods_similar where goods_main_id in("
	 *             + ids + ")"+") " + "limit "+ number;
	 */
	public Set<Map> subQuery(String mulitId, int amount, Map map, HttpServletRequest request,
			HttpServletResponse response, Set<Map> goodsSet) throws SQLException {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		int limit = amount;
		Set<Map> set = new HashSet<Map>();
		String[] ids = mulitId.split(",");
		int sum = ids.length * amount;
		Set<Map> setSimilar = new HashSet<Map>();
		for (String id : ids) {
			String sql = "select id,goods_name,goods_price,goods_current_price,goods_main_photo_id, goods_status, goods_collect from "
					+ Globals.DEFAULT_TABLE_SUFFIX + "goods where id in (select goods_vice_id from "
					+ Globals.DEFAULT_TABLE_SUFFIX + "goods_similar where goods_main_id=" + id
					+ " order by be_similar> " + "0.9" + ")" + " limit " + limit;
			ResultSet res = this.databaseTools.selectIn(sql);
			Long goods_id;
			String name = "";
			String img;
			BigDecimal price;
			BigDecimal current_price;
			int goods_status;
			int goods_collect;
			while (res.next()) {
				Map goodsMap = new HashMap();
				goods_id = res.getLong("id");
				name = res.getString("goods_name");
				price = res.getBigDecimal("goods_price");
				current_price = res.getBigDecimal("goods_current_price");
				goods_status = res.getInt("goods_status");
				goods_collect = res.getInt("goods_collect");
				goodsMap.put("goods_id", goods_id);
				Accessory acc = this.accessoryService.getObjById(res.getLong("goods_main_photo_id"));
				goodsMap.put("goods_id", goods_id);
				goodsMap.put("goods_name", name);
				goodsMap.put("goods_price", price);
				goodsMap.put("goods_current_price", current_price);
				goodsMap.put("goods_status", goods_status);
				goodsMap.put("goods_collect", goods_collect);
				if (acc != null) {
					goodsMap.put("goods_photo", imageWebServer + "/" + acc.getPath() + "/" + acc.getName());
				}
				setSimilar.add(goodsMap);
			}
			res.last();
			int row = res.getRow();
			if (row < limit) {
				limit += limit - row;
			}
		}
		set.addAll(setSimilar);
		List<Goods> objList = null;
		if (sum > setSimilar.size()) {
			int goods_num = sum - setSimilar.size();
			ModelAndView mv = new JModelAndView("", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request, response);
			GoodsQueryObject gqo = new GoodsQueryObject(null, null, mv, "weightiness", "desc");
			gqo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
			gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
			gqo.setPageSize(goods_num);
			IPageList pList = this.goodsService.list(gqo);
			objList = pList.getResult();
			Set<Map> setGoods = new HashSet<Map>();
			for (Goods obj : objList) {
				Map goodsMap = new HashMap();
				goodsMap.put("goods_id", obj.getId());
				goodsMap.put("goods_name", obj.getGoods_name());
				goodsMap.put("goods_price", obj.getGoods_price());
				goodsMap.put("goods_current_price", obj.getGoods_current_price());
				goodsMap.put("goods_photo",
						obj.getGoods_main_photo() != null ? obj.getGoods_main_photo().getPath() + "/"
								+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
								: imageWebServer);
				setGoods.add(goodsMap);
			}
			set.addAll(setGoods);
			map.put("currentPage", pList.getCurrentPage());
			map.put("Pages", pList.getPages());
		}
		set.addAll(goodsSet);
		map.put("result", set);
		return set;
	}

	/**
	 * @description 富文本内容拆分文字和图片
	 * @param goodsDetail
	 * @return
	 */
	public Map pictureCharacterSplit(String goodsDetail) {
		String character = "";
		String picture = "";
		Map map = new HashMap();
		if (goodsDetail != null || !goodsDetail.equals("")) {
			int indexOf = goodsDetail.indexOf("<img src=");
			if (goodsDetail.indexOf("<img src=") > -1) {
				character = goodsDetail.substring(0, indexOf);
				if ("<p>".equals(character)) {
					character = "";
				}
				picture = goodsDetail.substring(indexOf);
			} else {
				character = goodsDetail;
			}
		}
		map.put("character", character);
		map.put("picture", picture);
		return map;
	}

	/**
	 * @description 商品详情页优惠券查询
	 * @param obj
	 * @param user
	 * @param request
	 * @param response
	 * @return
	 * 
	 * 
	 */
	public List<Map<String, Object>> queryCoupon(Store store, User user) {
		List<Coupon> storeCoupons = new ArrayList<Coupon>();
		List<Coupon> selfCoupons = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("store_id", store.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		// 未登录用户商家优惠券列表
		storeCoupons = this.couponService
				.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
						+ "order by obj.coupon_amount desc", params, -1, -1);
		params.remove("store_id");
		params.put("coupon_type", 0);
		params.put("employ_type", 0);
		// 未登录用户自营优惠券列表
		selfCoupons = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=:employ_type "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_amount desc",
				params, -1, -1);
		storeCoupons.addAll(selfCoupons);
		List couponList = new ArrayList();
		List<Coupon> unusedCoupons = new ArrayList<Coupon>();
		List<Coupon> unclaimedCoupons = new ArrayList<Coupon>();
		List<Map> coupons = new ArrayList<Map>();
		if (user != null ) {
			params.clear();
			params.put("user_id", user.getId());
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			params.put("store_id", CommUtil.null2Long(-1));
			List<CouponInfo> selfUserCoupons = this.couponInfoService.query(
					"select obj from CouponInfo obj where obj.store_id=:store_id and obj.user.id=:user_id "
							+ "and obj.status=:status " + "and obj.coupon.coupon_end_time>=:coupon_end_time ",
					params, -1, -1);
			storeUserCoupons.addAll(selfUserCoupons);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}
		for (Coupon coupon : storeCoupons) {
			if (null == user) {
				unclaimedCoupons.add(coupon);
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.user.id=:user_id "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			}
		}
		couponList = this.coupon(unusedCoupons, 1);
		couponList.addAll(this.coupon(unclaimedCoupons, 0));
		return couponList;
	}
	
	/**
	 * 
	 * @param store
	 * @param user
	 * @param tourists
	 * @return
	 * @descript
	 */
	public List<Map<String, Object>> queryCoupon(Store store, User user, String tourists) {
		List<Coupon> storeCoupons = new ArrayList<Coupon>();
		List<Coupon> selfCoupons = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("store_id", store.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		// 未登录用户商家优惠券列表
		storeCoupons = this.couponService
				.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
						+ "order by obj.coupon_amount desc", params, -1, -1);
		params.remove("store_id");
		params.put("coupon_type", 0);
		params.put("employ_type", 0);
		// 未登录用户自营优惠券列表
		selfCoupons = this.couponService.query(
				"select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=:employ_type "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "order by obj.coupon_amount desc",
				params, -1, -1);
		storeCoupons.addAll(selfCoupons);
		List couponList = new ArrayList();
		List<Coupon> unusedCoupons = new ArrayList<Coupon>();
		List<Coupon> unclaimedCoupons = new ArrayList<Coupon>();
		List<Map> coupons = new ArrayList<Map>();
		if (user != null ) {
			params.clear();
			params.put("user_id", user.getId());
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			params.put("store_id", CommUtil.null2Long(-1));
			List<CouponInfo> selfUserCoupons = this.couponInfoService.query(
					"select obj from CouponInfo obj where obj.store_id=:store_id and obj.user.id=:user_id "
							+ "and obj.status=:status " + "and obj.coupon.coupon_end_time>=:coupon_end_time ",
					params, -1, -1);
			storeUserCoupons.addAll(selfUserCoupons);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}else if(tourists != null && !tourists.equals("")){
			params.clear();
			params.put("tourists", tourists);
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.tourists=:tourists "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			params.put("store_id", CommUtil.null2Long(-1));
			List<CouponInfo> selfUserCoupons = this.couponInfoService.query(
					"select obj from CouponInfo obj where obj.store_id=:store_id and obj.tourists=:tourists "
							+ "and obj.status=:status " + "and obj.coupon.coupon_end_time>=:coupon_end_time ",
					params, -1, -1);
			storeUserCoupons.addAll(selfUserCoupons);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}
		for (Coupon coupon : storeCoupons) {
			//用户等于null
			if (null == user && !CommUtil.null2String(tourists).equals("")) {
				params.clear();
				params.put("tourists", tourists);
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.tourists=:tourists "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			} else if(null != user && CommUtil.null2String(tourists).equals("")){
				params.clear();
				params.put("user_id", user.getId());
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.user.id=:user_id "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			}else{
				unclaimedCoupons.add(coupon);
			}
		}
		couponList = this.coupon(unusedCoupons, 0);
		couponList.addAll(this.coupon(unclaimedCoupons, -2));
		return couponList;
	}
	
	public List coupon(List<Coupon> coupons, int flag) {
		List<Map<String, Object>> couponList = new ArrayList<Map<String, Object>>();
		for (Coupon coupon : coupons) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (coupon.getCoupon_count() == 0 || coupon.getCoupon_count() > coupon.getCouponinfos().size()) {
				// CommUtil.subtract(coupon.getCoupon_end_time(), new Date()) >=
				// 0
				map.put("couponInfoType", flag);// 等于0等于未领取 1:为已领取1
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
				couponList.add(map);
			}
		}
		return couponList;
	}

	/**
	 * @description 商品详情页优惠券查询
	 * @param obj
	 * @param user
	 * @param request
	 * @param response
	 * @return
	 */
	public List<Map<String, Object>> storeCoupon(Store store, User user) {
		List<Coupon> storeCoupons = new ArrayList<Coupon>();
		List<Coupon> selfCoupons = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("store_id", store.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		// 未登录用户商家优惠券列表
		storeCoupons = this.couponService
				.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
						+ "order by obj.coupon_order_amount asc", params, -1, -1);
		List couponList = new ArrayList();
		List<Coupon> unusedCoupons = new ArrayList<Coupon>();
		List<Coupon> unclaimedCoupons = new ArrayList<Coupon>();
		List<Map> coupons = new ArrayList<Map>();
		if (user != null) {
			params.clear();
			params.put("user_id", user.getId());
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}
		for (Coupon coupon : storeCoupons) {
			if (null == user) {
				unclaimedCoupons.add(coupon);
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.user.id=:user_id "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			}
		}
		couponList = this.coupon(unusedCoupons, 1);
		couponList.addAll(this.coupon(unclaimedCoupons, 0));
		return couponList;
	}
	
	/**
	 * @description 商品详情页优惠券查询
	 * @param obj
	 * @param user
	 * @param request
	 * @param response
	 * @return
	 */
	public List<Map<String, Object>> storeCouponV2(Store store, User user, String tourists) {
		List<Coupon> storeCoupons = new ArrayList<Coupon>();
		List<Coupon> selfCoupons = new ArrayList<Coupon>();
		Map params = new HashMap();
		params.put("store_id", store.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("coupon_type", 1);
		// 未登录用户商家优惠券列表
		storeCoupons = this.couponService
				.query("select obj from Coupon obj where obj.coupon_type=:coupon_type and obj.employ_type=0 "
						+ "and obj.coupon_begin_time<=:coupon_begin_time "
						+ "and obj.coupon_end_time>=:coupon_end_time " + "and obj.store.id=:store_id "
						+ "order by obj.coupon_order_amount asc", params, -1, -1);
		List couponList = new ArrayList();
		List<Coupon> unusedCoupons = new ArrayList<Coupon>();
		List<Coupon> unclaimedCoupons = new ArrayList<Coupon>();
		List<Map> coupons = new ArrayList<Map>();
		if (user != null) {
			params.clear();
			params.put("user_id", user.getId());
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.user.id=:user_id "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}else{
			params.clear();
			params.put("tourists", tourists);
			params.put("store_id", store.getId());
			params.put("status", 0);
			params.put("coupon_end_time", new Date());
			List<CouponInfo> storeUserCoupons = this.couponInfoService
					.query("select obj from CouponInfo obj where obj.tourists=:tourists "
							+ "and obj.status=:status and obj.store_id=:store_id "
							+ "and obj.coupon.coupon_end_time>=:coupon_end_time", params, -1, -1);
			for (CouponInfo couponinfo : storeUserCoupons) {
				unusedCoupons.add(couponinfo.getCoupon());
			}
		}
		for (Coupon coupon : storeCoupons) {
			if (null == user) {
				params.clear();
				params.put("tourists", tourists);
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.tourists=:tourists "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			} else if(null != user){
				params.clear();
				params.put("user_id", user.getId());
				params.put("coupon_id", coupon.getId());
				params.put("coupon_end_time", new Date());
				List<CouponInfo> couponInfoList = this.couponInfoService
						.query("select obj from CouponInfo obj " + "where obj.user.id=:user_id "
								+ "and obj.coupon.id=:coupon_id " + "and obj.coupon.coupon_end_time>=:coupon_end_time "
								+ "order by obj.coupon.coupon_order_amount asc", params, -1, -1);
				if (couponInfoList == null || couponInfoList.size() == 0) {
					unclaimedCoupons.add(coupon);
				}
			}else{
				unclaimedCoupons.add(coupon);
			}
		}
		couponList = this.coupon(unusedCoupons, 0);
		couponList.addAll(this.coupon(unclaimedCoupons, -2));
		return couponList;
	}
	
	
	/**
	 * 更新商品库存 减
	 * 
	 * @param order
	 */
	public void updateGoodsInventory(OrderForm order) {
		// 付款成功，订单状态更新，同时更新商品库存
		List<Goods> goodsList = this.orderFormTools.queryOfGoods(CommUtil.null2String(order.getId()));
		for (Goods obj : goodsList) {
			int goods_count = this.orderFormTools.queryOfGoodsinventory(CommUtil.null2String(order.getId()),
					CommUtil.null2String(obj.getId()));
			List<String> gsps = new ArrayList<String>();
			List<GoodsSpecProperty> temp_gsp_list = this.orderFormTools
					.queryOfGoodsGsps(CommUtil.null2String(order.getId()), CommUtil.null2String(obj.getId()));
			String spectype = "";
			for (GoodsSpecProperty gsp : temp_gsp_list) {
				gsps.add(gsp.getId().toString());
				spectype += gsp.getSpec().getName() + ":" + gsp.getValue() + " ";
			}
			String[] gsp_list = new String[gsps.size()];
			gsps.toArray(gsp_list);
			obj.setGoods_salenum(obj.getGoods_salenum() + goods_count);
			// 更新商品日志
			GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(obj.getId());
			todayGoodsLog.setGoods_salenum(todayGoodsLog.getGoods_salenum() + goods_count);
			Map<String, Integer> logordermap = (Map<String, Integer>) Json
					.fromJson(todayGoodsLog.getGoods_order_type());
			String ordertype = order.getOrder_type();
			if (logordermap.containsKey(ordertype)) {
				logordermap.put(ordertype, logordermap.get(ordertype) + goods_count);
			} else {
				logordermap.put(ordertype, goods_count);
			}
			todayGoodsLog.setGoods_order_type(Json.toJson(logordermap, JsonFormat.compact()));

			Map<String, Integer> logspecmap = (Map<String, Integer>) Json.fromJson(todayGoodsLog.getGoods_sale_info());

			if (logspecmap.containsKey(spectype)) {
				logspecmap.put(spectype, logspecmap.get(spectype) + goods_count);
			} else {
				logspecmap.put(spectype, goods_count);
			}
			todayGoodsLog.setGoods_sale_info(Json.toJson(logspecmap, JsonFormat.compact()));
			this.goodsLogService.update(todayGoodsLog);

			// 更新店铺日志
			StoreLog storeLog = this.storeLogTools.getTodayStoreLog(CommUtil.null2Long(order.getStore_id()));
			storeLog.setSignfor(storeLog.getSignfor() + 1);
			this.storeLogService.update(storeLog);

			boolean inventory_warn = false;
			String inventory_type = obj.getInventory_type() == null ? "all" : obj.getInventory_type();
			boolean flag = false;// 为true使用海外仓库存
			ShipAddress sa = this.shipAddressService.getObjById(order.getShip_addr_id());
			if (null != sa && "1".equals(sa.getRepository())) {
				flag = true;
			}
			if (inventory_type.equals("all")) {
				if (flag) {
					obj.setOversea_inventory(obj.getOversea_inventory() - goods_count);
				} else {
					obj.setGoods_inventory(obj.getGoods_inventory() - goods_count);
				}
				if (obj.getGoods_inventory() <= obj.getGoods_warn_inventory()) {
					obj.setWarn_inventory_status(-1);// 该商品库存预警状态
				}
			} else {
				Map map = this.orderFormTools.queryOfGoodsgsp(CommUtil.null2String(order.getId()),
						CommUtil.null2String(obj.getId()));
				String color = (String) map.get("color");
				String gsp = (String) map.get("goods_gsp_ids");
				CGoods cobj = this.orderFormTools.queryChildGoods(obj, gsp, color);
				if (cobj != null) {
					if (flag) {
						cobj.setOversea_inventory(cobj.getOversea_inventory() - goods_count);
					} else {
						cobj.setGoods_inventory(cobj.getGoods_inventory() - goods_count);
					}
					if (cobj.getGoods_inventory() <= cobj.getGoods_warn_inventory()) {
						cobj.setWarn_inventory_status(-1);
					}
					this.cGoodsService.update(cobj);
				}
				obj.setGoods_inventory(obj.getGoods_inventory() - goods_count);
			}
			// 更新商品秒杀状态
			if (obj.isStore_deals()) {
				if (CommUtil.subtract(obj.getStore_seckill_inventory(), goods_count) <= 0) {
					obj.setStore_seckill_inventory(obj.getStore_seckill_inventory() - goods_count);
					obj.setStore_deals(false);
				} else {
					obj.setStore_seckill_inventory(obj.getStore_seckill_inventory() - goods_count);
				}
			}
			this.goodsService.update(obj);
			// 更新lucene索引
			String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
					+ File.separator + "goods";
			File file = new File(goods_lucene_path);
			if (!file.exists()) {
				CommUtil.createFolder(goods_lucene_path);
			}
			LuceneUtil lucene = LuceneUtil.instance();
			lucene.setIndex_path(goods_lucene_path);
			lucene.update(CommUtil.null2String(obj.getId()), luceneVoTools.updateGoodsIndex(obj));
		}
	}

	/**
	 * lucene 转换商品折扣率
	 * 
	 * @param goods_price
	 * @param goods_current_price
	 * @return
	 */
	public String getRate(BigDecimal goods_price, BigDecimal goods_current_price) {
		double ret = 0.0;
		if (!"".equals(CommUtil.null2String(goods_price)) && !"".equals(CommUtil.null2String(goods_current_price))) {
			double subtract = CommUtil.subtract(goods_price, goods_current_price);
			if (CommUtil.null2Float(goods_price) > 0)
				ret = CommUtil.null2BigDecimal(subtract).divide(goods_price, 2, BigDecimal.ROUND_DOWN).doubleValue();
		}
		DecimalFormat df = new DecimalFormat("0.00");
		Double re = CommUtil.mul(Double.valueOf(df.format(ret)), 100);
		return re.toString();
	}

	/**
	 * 获取商品折扣率
	 * 
	 * @param goods_price
	 * @param goods_current_price
	 * @return
	 */
	public BigDecimal getGoodsRate(BigDecimal goods_price, BigDecimal goods_current_price) {
		double ret = 0.0;
		if (!"".equals(CommUtil.null2String(goods_price)) && !"".equals(CommUtil.null2String(goods_current_price))) {
			double subtract = CommUtil.subtract(goods_price, goods_current_price);
			if (CommUtil.null2Float(goods_price) > 0)
				ret = CommUtil.null2BigDecimal(subtract).divide(goods_price, 2, BigDecimal.ROUND_DOWN).doubleValue();
		}
		DecimalFormat df = new DecimalFormat("0.00");
		Double re = CommUtil.mul(Double.valueOf(df.format(ret)), 100);
		// BigDecimal rate = new BigDecimal(re); //精准度问题 BigDecimal(String
		// val)构造是靠谱的
		BigDecimal rate = new BigDecimal(re.toString());
		return rate;
	}

	// 数据类型转换
	@Test
	public void dataTypeConversion() {
		// Double 转 BigDecimal
		double str = 10.12;
		DecimalFormat df = new DecimalFormat("0.00");
		Double re1 = Double.valueOf(df.format(str));
		String double_str = re1.toString();
		System.out.println("double_str " + double_str);
	}

	/**
	 * 求Map<K,V>中Value(值)的最小值
	 * 
	 * @param map
	 * @return
	 */
	public static Object getMinValue(Map<BigDecimal, BigDecimal> map) {
		if (map == null)
			return null;
		Collection<BigDecimal> c = map.values();
		Object[] obj = c.toArray();
		Arrays.sort(obj);
		return obj[0];
	}

	/**
	 * 求Map<K,V>中Key(键)的最小值
	 * 
	 * @param map
	 * @return
	 */
	public BigDecimal getMinKey(Map<BigDecimal, BigDecimal> map) {
		if (map == null)
			return null;
		Set<BigDecimal> set = map.keySet();
		Object[] obj = set.toArray();
		Arrays.sort(obj);
		return CommUtil.null2BigDecimal(obj[0]);
	}
	
	
}
