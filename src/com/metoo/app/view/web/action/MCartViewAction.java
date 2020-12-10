package com.metoo.app.view.web.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.mvc.adaptor.Params;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MCartViewTools;
import com.metoo.app.view.web.tool.MobileTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.Address;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.CombinPlan;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.DeliveryAddress;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.PointRecord;
import com.metoo.foundation.domain.ShipAddress;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.VerifyCode;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.IAddressService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IBuyGiftService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.ICombinPlanService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IDeliveryAddressService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IGroupInfoService;
import com.metoo.foundation.service.IGroupLifeGoodsService;
import com.metoo.foundation.service.IIntegralGoodsOrderService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IPointRecordService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IPredepositLogService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.ITransportService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IVerifyCodeService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.admin.tools.PaymentTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.buyer.tools.CartTools;
import com.metoo.manage.delivery.tools.DeliveryAddressTools;
import com.metoo.manage.seller.tools.CombinTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.msg.MsgTools;
import com.metoo.pay.tools.PayTools;
import com.metoo.view.web.tools.ActivityViewTools;
import com.metoo.view.web.tools.BuyGiftViewTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.GroupViewTools;
import com.metoo.view.web.tools.IntegralViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * 
 * @author 46075
 *
 */
@Controller
@RequestMapping("/app/")
public class MCartViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IAddressService addressService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private PaymentTools paymentTools;
	@Autowired
	private PayTools payTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private CartTools cartTools;
	@Autowired
	private IGroupLifeGoodsService groupLifeGoodsService;
	@Autowired
	private IGroupInfoService groupInfoService;
	@Autowired
	private IMessageService messageService;
	@Autowired
	private GroupViewTools groupViewTools;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private UserTools userTools;
	@Autowired
	private IPayoffLogService payoffLogService;
	@Autowired
	private IIntegralGoodsOrderService igorderService;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IBuyGiftService buyGiftService;
	@Autowired
	private ICombinPlanService combinplanService;
	@Autowired
	private CombinTools combinTools;
	@Autowired
	private BuyGiftViewTools buyGiftViewTools;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private IActivityGoodsService actgoodsService;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private ActivityViewTools activityTools;
	@Autowired
	private IDeliveryAddressService deliveryaddrService;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private DeliveryAddressTools DeliveryAddressTools;
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IVerifyCodeService mobileverifycodeService;
	@Autowired
	private MCartViewTools mCartViewTools;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private MobileTools mobileTools;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private IPointRecordService pointRecordService;
	@Autowired
	private IIntegralLogService intergralLogService;

	/**
	 * @description 去重合并用户购物车
	 * @param request
	 * @param user
	 * @param cart_session_id
	 * @return
	 */
	public List<GoodsCart> cartListCalc1(HttpServletRequest request, User user, String cart_session_id) {
		List<GoodsCart> cartsList = new ArrayList<GoodsCart>();
		List<GoodsCart> userCartList = new ArrayList<GoodsCart>();
		List<GoodsCart> cookieCartList = new ArrayList<GoodsCart>();
		Map<String, Object> params = new HashMap<String, Object>();
		if (user != null) {
			user = this.userService.getObjById(user.getId());
			// 获取未登录时用户购物车
			if (!"".equals(cart_session_id)) {
				params.clear();
				params.put("cart_session_id", cart_session_id);
				params.put("cart_status", 0);
				cookieCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status",
						params, -1, -1);
				// 移除购物车中当前用户拥有店铺商品
				if (user.getStore() != null) {
					for (GoodsCart cart : cookieCartList) {
						if (cart.getGoods().getGoods_store().getId().equals(user.getStore().getId())) {
							this.goodsCartService.delete(cart.getId());
						}
					}
				}
				// 查询登陆时用户购物车
				params.clear();
				params.put("user_id", user.getId());
				params.put("cart_status", 0);
				userCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status",
						params, -1, -1);
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("cart_status", 0);
				userCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status",
						params, -1, -1);
			}
		} else {
			if (!"".equals(cart_session_id)) {
				params.clear();
				params.put("cart_session_id", cart_session_id);
				params.put("cart_status", 0);
				cookieCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status",
						params, -1, -1);
			}
		}
		// 将用户购物车与cookie购物车合并去重
		if (user != null) {
			for (GoodsCart userCart : userCartList) {
				cartsList.add(userCart);
			}
			if (userCartList.size() > 0) {
				for (GoodsCart cookieCart : cookieCartList) {
					boolean yes = false;
					boolean no = false;
					GoodsCart goodsCart = null;
					for (GoodsCart userCart : userCartList) {
						if (userCart.getGoods().getId().equals(cookieCart.getGoods().getId())) {
							yes = true;
							goodsCart = userCart;
							break;
						} else {
							no = true;
						}
					}
					if (yes) {
						boolean colorFlag = false;
						boolean gspFlag = false;

						if (null != cookieCart.getColor() && !"".equals(cookieCart.getColor())) {
							if (cookieCart.getColor().equals(goodsCart.getColor())) {
								colorFlag = true;
							}
						}
						if (cookieCart.getSpec_info().equals(goodsCart.getSpec_info())) {
							gspFlag = true;
						}
						if (colorFlag && gspFlag) {
							this.goodsCartService.delete(cookieCart.getId());
						}
						if (!colorFlag || !gspFlag) {
							// 将cookieCart转换为userCart
							cookieCart.setCart_session_id(null);
							cookieCart.setUser(user);
							this.goodsCartService.update(cookieCart);
							cartsList.add(cookieCart);
						}
					} else {
						if (no) {
							cookieCart.setCart_session_id(null);
							cookieCart.setUser(user);
							this.goodsCartService.update(cookieCart);
							cartsList.add(cookieCart);
						}
					}
				}
			} else {
				for (GoodsCart cookieCart : cookieCartList) {
					cookieCart.setCart_session_id(null);
					cookieCart.setUser(user);
					this.goodsCartService.update(cookieCart);
					cartsList.add(cookieCart);
				}
			}
		} else {
			for (GoodsCart cookieCart : cookieCartList) {
				cartsList.add(cookieCart);
			}
		}
		return cartsList;
	}

	/**
	 * 获得商品组合商品价格
	 * 
	 * @param request
	 * @param response
	 */
	private double getGoodsCombinPrice() {
		double combin_price = 0.00;
		return combin_price;
	}

	/**
	 * 根据商品规格获取价格
	 * 
	 * @param request
	 * @param response
	 */
	private String generGspgoodsPrice(String gsp, String id, String user_id) {
		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
		double price = CommUtil.null2Double(goods.getGoods_current_price());
		User user = null;
		if (user_id != null && !user_id.equals("")) {
			user = this.userService.getObjById(CommUtil.null2Long(user_id));
		}
		if (goods.getActivity_status() == 2) {
			if (user != null) {
				Map map = this.activityTools.getActivityGoodsInfo(CommUtil.null2String(goods.getId()),
						CommUtil.null2String(user.getId()));
				price = CommUtil.null2Double(map.get("rate_price"));
			}
		} else {
			if (goods.getInventory_type().equals("spec")) {
				List<HashMap> list = Json.fromJson(ArrayList.class, goods.getGoods_inventory_detail());
				String[] gsp_ids = gsp.split(",");
				for (Map temp : list) {
					String[] temp_ids = CommUtil.null2String(temp.get("id")).split("_");
					Arrays.sort(gsp_ids);
					Arrays.sort(temp_ids);
					if (Arrays.equals(gsp_ids, temp_ids)) {
						price = CommUtil.null2Double(temp.get("price"));
					}
				}
			}
		}
		return CommUtil.null2String(price);
	}

	/**
	 * @description 添加商品到购物车
	 * @param request
	 * @param response
	 * @param id
	 * @param count
	 *            默认为 "1"
	 * @param price
	 *            商品价格
	 * @param color
	 *            商品颜色规格
	 * @param gsp
	 *            商品其他规格
	 * @param token
	 * @param cart_session
	 *            记录未登录用户加购sessionid
	 */
	/*
	 * @RequestMapping("v1/addToCart.json") public void
	 * addToCart(HttpServletRequest request, HttpServletResponse response,
	 * String goods_id, String count, String price, String color, String gsp,
	 * String cart_type, String visitor_id, String token) { int code = -1;
	 * String msg = ""; User user = null; Map map = new HashMap(); Goods obj =
	 * this.goodsService.getObjById(CommUtil.null2Long(goods_id)); if (obj !=
	 * null && obj.getGoods_status() == 0) { if
	 * (obj.getGoods_store().getStore_status() == 15) { // 保留通过商品列表添加购物车生成默认规格
	 * if ("".equals(color) || "".equals(gsp)) { // 生成默认规格 } // 计算商品库存 int
	 * goods_inventory = 0; int oversea_inventory = 0;
	 * 
	 * int goods_inventory = CommUtil
	 * .null2Int(this.mCartViewTools.generic_default_info_color(obj, gsp,
	 * color).get("count"));
	 * 
	 * // 判断优先使用海外仓 if (obj.getInventory_type().equals("all")) { goods_inventory
	 * = obj.getGoods_inventory(); oversea_inventory =
	 * obj.getOversea_inventory(); } else { Map<String, Object> goods =
	 * this.mCartViewTools.generic_default_info_color(obj, gsp, color);
	 * goods_inventory = CommUtil.null2Int(goods.get("count"));
	 * oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory")); }
	 * if (CommUtil.subtract(goods_inventory, count) > 0 ||
	 * CommUtil.subtract(oversea_inventory, count) > 0) { if
	 * (obj.getInventory_type().equals("spec") && "".equals(color) &&
	 * "".equals(gsp)) { code = 4400; msg = "Please select specifications"; }
	 * else { String cart_session_id = ""; if (null != visitor_id &&
	 * !"".equals(visitor_id)) { cart_session_id = visitor_id; } else { Cookie[]
	 * cookies = request.getCookies(); if (cookies != null) { for (Cookie cookie
	 * : cookies) { if (cookie.getName().equals("cart_session_id")) {
	 * cart_session_id = CommUtil.null2String(cookie.getValue()); } } } } if
	 * (null != cart_session_id && cart_session_id.equals("")) { cart_session_id
	 * = UUID.randomUUID().toString(); Cookie cookie = new
	 * Cookie("cart_session_id", cart_session_id);
	 * cookie.setDomain(CommUtil.generic_domain(request));
	 * response.addCookie(cookie); }
	 * 
	 * List<GoodsCart> cartsList = new ArrayList<GoodsCart>();// 用户整体购物车
	 * List<GoodsCart> cookieCartList = new ArrayList<GoodsCart>();// 未登录用户购物车
	 * // pc // List<GoodsCart> mobileCartList = new // ArrayList<GoodsCart>();
	 * // 未登录用户购物车 app List<GoodsCart> userCartList = new
	 * ArrayList<GoodsCart>();// 已登录用户购物车 if (!"".equals(token)) { user =
	 * this.userService.getObjByProperty(null, "app_login_token", token); }
	 * Map<String, Object> params = new HashMap<String, Object>(); if (null !=
	 * user) { // 获取未登录时用户购物车 if (!"".equals(cart_session_id)) { params.clear();
	 * params.put("cart_session_id", cart_session_id); params.put("cart_status",
	 * 0); cookieCartList = this.goodsCartService.query(
	 * "select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status"
	 * , params, -1, -1); // 移除购物车中当前用户拥有店铺商品 if (user.getStore() != null) { for
	 * (GoodsCart cart : cookieCartList) { if
	 * (cart.getGoods().getGoods_store().getId().equals(user.getStore().getId())
	 * ) { this.goodsCartService.delete(cart.getId()); } } } // 查询登陆时用户购物车
	 * params.clear(); params.put("user_id", user.getId());
	 * params.put("cart_status", 0); userCartList = this.goodsCartService.query(
	 * "select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status"
	 * , params, -1, -1); } else { params.clear(); params.put("user_id",
	 * user.getId()); params.put("cart_status", 0); userCartList =
	 * this.goodsCartService.query(
	 * "select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status"
	 * , params, -1, -1); } } else { if (!"".equals(cart_session_id)) {
	 * params.clear(); params.put("cart_session_id", cart_session_id);
	 * params.put("cart_status", 0); cookieCartList =
	 * this.goodsCartService.query(
	 * "select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status"
	 * , params, -1, -1); } }
	 * 
	 * // 将用户购物车与cookie购物车合并去重 if (user != null) { for (GoodsCart userCart :
	 * userCartList) { cartsList.add(userCart); } if (userCartList.size() > 0) {
	 * for (GoodsCart cookieCart : cookieCartList) { for (GoodsCart userCart :
	 * userCartList) { if
	 * (userCart.getGoods().getId().equals(cookieCart.getGoods().getId())) {
	 * boolean colorFlag = false; boolean gspFlag = false; if (null !=
	 * cookieCart.getColor() && !"".equals(cookieCart.getColor())) { if
	 * (cookieCart.getColor().equals(userCart.getColor())) { colorFlag = true; }
	 * } if (cookieCart.getSpec_info().equals(userCart.getSpec_info())) {
	 * gspFlag = true; } if (colorFlag && gspFlag) {
	 * this.goodsCartService.delete(cookieCart.getId()); }
	 * 
	 * if (!colorFlag || !gspFlag) { // 将cookieCart转换为userCart
	 * cookieCart.setCart_session_id(null); cookieCart.setUser(user);
	 * this.goodsCartService.update(cookieCart); cartsList.add(cookieCart); } }
	 * } } } else { for (GoodsCart cookieCart : cookieCartList) {
	 * cookieCart.setCart_session_id(null); cookieCart.setUser(user);
	 * this.goodsCartService.update(cookieCart); cartsList.add(cookieCart); } }
	 * } else { for (GoodsCart cookieCart : cookieCartList) {
	 * cartsList.add(cookieCart); } } boolean gspFlag = false; boolean colorFlag
	 * = false; String[] goods_gsp = CommUtil.null2String(gsp).split(",");
	 * Arrays.sort(goods_gsp); for (GoodsCart goodsCart : cartsList) { if
	 * (goodsCart.getGoods().getId().equals(obj.getId())) { if (goods_gsp !=
	 * null && goods_gsp.length > 0 && goodsCart.getGsps().size() > 0 && null !=
	 * color && !"".equals(color) || !"".equals(goodsCart.getColor())) { if
	 * (goods_gsp != null && goods_gsp.length > 0 && goodsCart.getGsps().size()
	 * > 0) { String[] gc_gsp = new String[goodsCart.getGsps().size()]; for (int
	 * i = 0; i < goodsCart.getGsps().size(); i++) { gc_gsp[i] =
	 * goodsCart.getGsps().get(i) != null ?
	 * goodsCart.getGsps().get(i).getId().toString() : ""; }
	 * Arrays.sort(gc_gsp); if (obj.getId().equals(goodsCart.getGoods().getId())
	 * && gc_gsp.equals(goods_gsp)) { gspFlag = true; } } if (null != color &&
	 * !"".equals(color) || !"".equals(goodsCart.getColor())) { if
	 * (color.toString().equals(goodsCart.getColor())) { colorFlag = true; } } }
	 * else { colorFlag = true; gspFlag = true; } } } if (!gspFlag &&
	 * !colorFlag) { GoodsCart goodsCart = new GoodsCart();
	 * goodsCart.setAddTime(new Date()); goodsCart.setCart_type(cart_type);
	 * goodsCart.setCount(Integer.parseInt(count) == 0 ? 1 :
	 * Integer.parseInt(count)); goodsCart.setColor(color);
	 * goodsCart.setCart_gsp(gsp); goodsCart.setGoods(obj); BigDecimal
	 * goods_price = CommUtil.null2BigDecimal(this.mCartViewTools
	 * .generic_default_info_color(obj, gsp, color).get("goods_current_price"));
	 * goodsCart.setPrice(goods_price); // 记录商品当天日志，加购数 GoodsLog goodsLog =
	 * goodsViewTools.getTodayGoodsLog(obj.getId());
	 * goodsLog.setGoods_cart(goodsLog.getGoods_cart() + 1);
	 * this.goodsLogService.update(goodsLog); if (null == user) {
	 * goodsCart.setCart_session_id(cart_session_id); } else {
	 * goodsCart.setUser(user); } String spec_info = ""; for (String gspId :
	 * goods_gsp) { GoodsSpecProperty goodsSpecProperty =
	 * this.goodsSpecPropertyService .getObjById(CommUtil.null2Long(gspId));
	 * goodsCart.getGsps().add(goodsSpecProperty); if (goodsSpecProperty !=
	 * null) { spec_info = goodsSpecProperty.getSpec().getName() + ":" +
	 * goodsSpecProperty.getValue() + "," + spec_info; } }
	 * goodsCart.setSpec_info(spec_info); this.goodsCartService.save(goodsCart);
	 * } code = 4200; msg = "Successfully"; map.put("cart_session_id",
	 * cart_session_id); } } else { code = 4206; msg = "Goods in short stock"; }
	 * } else { code = 4208; msg = "The merchandise is off the shelves"; } }
	 * else { code = 4403; msg = "The product ID is empty"; }
	 * this.send_json(Json.toJson(new Result(code, msg, map),
	 * JsonFormat.compact()), response); }
	 */
	@RequestMapping("v1/addToCart.json")
	@ResponseBody
	public String addToCart(HttpServletRequest request, HttpServletResponse response, String goods_id, String count,
			String price, String color, String gsp, String cart_type, String visitor_id, String token) {
		int code = -1;
		String msg = "";
		User user = null;
		if (!"".equals(token)) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		Map map = new HashMap();
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		boolean point = true;
		boolean point_record = true;
		boolean point_number = true;
		if (obj.getPoint() == 1 && obj.getPoint_status() == 10) {
			if (user == null) {
				point = false;
			} else {
				Map params = new HashMap();
				params.put("goods_id", obj.getId());
				params.put("user_id", user.getId());
				List<PointRecord> PointRecords = this.pointRecordService.query(
						"SELECT obj FROM PointRecord obj WHERE obj.goods_id=:goods_id and obj.user_id=:user_id", params,
						-1, -1);
				if (PointRecords.size() > 0) {
					point_record = false;
				} else if (user.getPointNum() < obj.getPointNum()) {
					point_number = false;
				}
			}
		}
		if (point) {
			if (point_record) {
				boolean addFlag = false;
				/*
				 * if (user != null && user.getPointNum() >= obj.getPointNum())
				 * { addFlag = true; } else if (user == null) { addFlag = true;
				 * } else
				 */ if (point_number) {
					if (obj != null && obj.getGoods_status() == 0 || obj.getGoods_status() == 4) {
						if (obj.getGoods_store().getStore_status() == 15) {
							// 保留通过商品列表添加购物车生成默认规格
							if ("".equals(color) || "".equals(gsp)) {
								// 生成默认规格
							}
							// 计算商品库存
							int goods_inventory = 0;
							int oversea_inventory = 0;
							/*
							 * int goods_inventory = CommUtil
							 * .null2Int(this.mCartViewTools.
							 * generic_default_info_color(obj, gsp,
							 * color).get("count"));
							 */
							// 判断优先使用海外仓
							if (obj.getInventory_type().equals("all")) {
								goods_inventory = obj.getGoods_inventory();
								oversea_inventory = obj.getOversea_inventory();
							} else {
								Map<String, Object> goods = this.mCartViewTools.generic_default_info_color(obj, gsp,
										color);
								goods_inventory = CommUtil.null2Int(goods.get("count"));
								oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory"));
							}
							if (CommUtil.subtract(goods_inventory, count) > 0
									|| CommUtil.subtract(oversea_inventory, count) > 0) {
								if (obj.getInventory_type().equals("spec") && "".equals(color) && "".equals(gsp)) {
									code = 4400;
									msg = "Please select specifications";
								} else {
									String cart_session_id = "";
									if (null != visitor_id && !"".equals(visitor_id)) {
										cart_session_id = visitor_id;
									} else {
										Cookie[] cookies = request.getCookies();
										if (cookies != null) {
											for (Cookie cookie : cookies) {
												if (cookie.getName().equals("cart_session_id")) {
													cart_session_id = CommUtil.null2String(cookie.getValue());
												}
											}
										}
									}
									if (null == cart_session_id && cart_session_id.equals("")) {
										cart_session_id = UUID.randomUUID().toString();
										Cookie cookie = new Cookie("cart_session_id", cart_session_id);
										// cookie.setDomain(CommUtil.generic_domain(request));
										response.addCookie(cookie);
									}

									List<GoodsCart> cartsList = new ArrayList<GoodsCart>();// 用户整体购物车
									List<GoodsCart> cookieCartList = new ArrayList<GoodsCart>();// 未登录用户购物车
																								// pc
									// List<GoodsCart> mobileCartList = new
									// ArrayList<GoodsCart>(); // 未登录用户购物车 app
									List<GoodsCart> userCartList = new ArrayList<GoodsCart>();// 已登录用户购物车
									/*
									 * if (!"".equals(token)) { user =
									 * this.userService.getObjByProperty(null,
									 * "app_login_token", token); }
									 */
									Map<String, Object> params = new HashMap<String, Object>();
									if (null != user) {
										// 获取未登录时用户购物车
										if (!"".equals(cart_session_id)) {
											params.clear();
											params.put("cart_session_id", cart_session_id);
											params.put("cart_status", 0);
											cookieCartList = this.goodsCartService.query(
													"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status",
													params, -1, -1);
											// 移除购物车中当前用户拥有店铺商品
											if (user.getStore() != null) {
												for (GoodsCart cart : cookieCartList) {
													if (cart.getGoods().getGoods_store().getId()
															.equals(user.getStore().getId())) {
														this.goodsCartService.delete(cart.getId());
													}
												}
											}
											// 查询登陆时用户购物车
											params.clear();
											params.put("user_id", user.getId());
											params.put("cart_status", 0);
											userCartList = this.goodsCartService.query(
													"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status",
													params, -1, -1);
										} else {
											params.clear();
											params.put("user_id", user.getId());
											params.put("cart_status", 0);
											userCartList = this.goodsCartService.query(
													"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status",
													params, -1, -1);
										}
									} else {
										if (!"".equals(cart_session_id)) {
											params.clear();
											params.put("cart_session_id", cart_session_id);
											params.put("cart_status", 0);
											cookieCartList = this.goodsCartService.query(
													"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status",
													params, -1, -1);
										}
									}

									// 将用户购物车与cookie购物车合并去重
									if (user != null) {
										for (GoodsCart userCart : userCartList) {
											cartsList.add(userCart);
										}
										if (userCartList.size() > 0) {
											for (GoodsCart cookieCart : cookieCartList) {
												for (GoodsCart userCart : userCartList) {
													if (userCart.getGoods().getId()
															.equals(cookieCart.getGoods().getId())) {
														boolean colorFlag = false;
														boolean gspFlag = false;
														if (null != cookieCart.getColor()
																&& !"".equals(cookieCart.getColor())) {
															if (cookieCart.getColor().equals(userCart.getColor())) {
																colorFlag = true;
															}
														}
														if (cookieCart.getSpec_info().equals(userCart.getSpec_info())) {
															gspFlag = true;
														}
														if (colorFlag && gspFlag) {
															this.goodsCartService.delete(cookieCart.getId());
														}

														if (!colorFlag || !gspFlag) {
															// 将cookieCart转换为userCart
															cookieCart.setCart_session_id(null);
															cookieCart.setUser(user);
															this.goodsCartService.update(cookieCart);
															cartsList.add(cookieCart);
														}
													}
												}
											}
										} else {
											for (GoodsCart cookieCart : cookieCartList) {
												cookieCart.setCart_session_id(null);
												cookieCart.setUser(user);
												this.goodsCartService.update(cookieCart);
												cartsList.add(cookieCart);
											}
										}
									} else {
										for (GoodsCart cookieCart : cookieCartList) {
											cartsList.add(cookieCart);
										}
									}
									boolean gspFlag = false;
									boolean colorFlag = false;
									String[] goods_gsp = CommUtil.null2String(gsp).split(",");
									Arrays.sort(goods_gsp);
									for (GoodsCart goodsCart : cartsList) {
										if (goodsCart.getGoods().getId().equals(obj.getId())) {
											if (goods_gsp != null && goods_gsp.length > 0
													&& goodsCart.getGsps().size() > 0 && null != color
													&& !"".equals(color) || !"".equals(goodsCart.getColor())) {
												if (goods_gsp != null && goods_gsp.length > 0
														&& goodsCart.getGsps().size() > 0) {
													String[] gc_gsp = new String[goodsCart.getGsps().size()];
													for (int i = 0; i < goodsCart.getGsps().size(); i++) {
														gc_gsp[i] = goodsCart.getGsps().get(i) != null
																? goodsCart.getGsps().get(i).getId().toString() : "";
													}
													Arrays.sort(gc_gsp);
													if (obj.getId().equals(goodsCart.getGoods().getId())
															&& gc_gsp.equals(goods_gsp)) {
														gspFlag = true;
													}
												}
												if (null != color && !"".equals(color)
														|| !"".equals(goodsCart.getColor())) {
													if (color.toString().equals(goodsCart.getColor())) {
														colorFlag = true;
													}
												}
											} else {
												colorFlag = true;
												gspFlag = true;
											}
										}
									}
									if (!gspFlag && !colorFlag) {
										GoodsCart goodsCart = new GoodsCart();
										goodsCart.setAddTime(new Date());
										if (obj.getPoint() == 1 && obj.getPoint_status() == 10) {
											goodsCart.setPrice(CommUtil.null2BigDecimal(0));
											goodsCart.setCart_type("1");
											int pint_num = user.getPointNum() - obj.getPointNum();
											user.setPointNum(pint_num);
											PointRecord pr = new PointRecord();
											pr.setAddTime(new Date());
											pr.setGoods_id(obj.getId());
											pr.setUser_id(user.getId());
											pr.setPoint_num(obj.getPointNum());
											pr.setPay_time(new Date());
											pr.setRemaining_num(pint_num);
											this.pointRecordService.save(pr);
											this.userService.update(user);
										} else {
											BigDecimal goods_price = CommUtil.null2BigDecimal(
													this.mCartViewTools.generic_default_info_color(obj, gsp, color)
															.get("goods_current_price"));
											goodsCart.setPrice(goods_price);
											goodsCart.setCart_type("0");
										}
										goodsCart.setCart_form(cart_type);
										goodsCart.setCount(Integer.parseInt(count) == 0 ? 1 : Integer.parseInt(count));
										goodsCart.setColor(color);
										goodsCart.setCart_gsp(gsp);
										goodsCart.setGoods(obj);

										// 记录商品当天日志，加购数
										GoodsLog goodsLog = goodsViewTools.getTodayGoodsLog(obj.getId());
										goodsLog.setGoods_cart(goodsLog.getGoods_cart() + 1);
										this.goodsLogService.update(goodsLog);
										if (null == user) {
											goodsCart.setCart_session_id(cart_session_id);
										} else {
											goodsCart.setUser(user);
										}
										String spec_info = "";
										for (String gspId : goods_gsp) {
											GoodsSpecProperty goodsSpecProperty = this.goodsSpecPropertyService
													.getObjById(CommUtil.null2Long(gspId));
											goodsCart.getGsps().add(goodsSpecProperty);
											if (goodsSpecProperty != null) {
												spec_info = goodsSpecProperty.getSpec().getName() + ":"
														+ goodsSpecProperty.getValue() + "," + spec_info;
											}
										}
										goodsCart.setSpec_info(spec_info);
										this.goodsCartService.save(goodsCart);
									}
									code = 4200;
									msg = "Successfully";
									map.put("cart_session_id", cart_session_id);
								}
							} else {
								code = 4206;
								msg = "Goods in short stock";
							}
						} else {
							code = 4208;
							msg = "The merchandise is off the shelves";
						}
					} else {
						code = 4403;
						msg = "The product ID is empty";
					}
				} else {
					code = 4211;
					msg = "Invite number unqualified";
				}
			} else {
				code = 4212;
				msg = "You can only change them once per person";
			}
		} else {
			code = -100;
			msg = "token Invalidation";
		}
		return Json.toJson(new Result(code, "success", map), JsonFormat.compact());
		// this.send_json(Json.toJson(new Result(code, msg, map),
		// JsonFormat.compact()), response);
	}

	/**
	 * 添加商品到购物车 弃用
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            添加到购物车的商品id
	 * @param count
	 *            添加到购物车的商品数量
	 * @param price
	 *            添加到购物车的商品的价格,该逻辑会更加gsp再次计算实际价格，避免用户在前端篡改
	 * @param gsp
	 *            商品的属性值，这里传递id值，如12,1,21
	 * @param buy_type
	 *            购买的商品类型，组合销售时用于判断是套装购买还是配件购买,普通商品：不传值，配件组合:parts,组合套装：suit
	 * @param combin_ids
	 *            组合搭配中配件id
	 * @param combin_version
	 *            组合套装中套装版本
	 */
	@RequestMapping("/add_goods_cart.json")
	public void app_add_goods_cart(HttpServletRequest request, HttpServletResponse response, String user_id,
			String token, String cart_mobile_ids, String goods_id, String count, String price, String gsp,
			String buy_type, String combin_ids, String combin_version, String color) {
		List<GoodsCart> carts_list = new ArrayList<GoodsCart>();// 用户整体购物车
		List<GoodsCart> carts_mobile = new ArrayList<GoodsCart>();// 未提交的用户mobile购物车
		List<GoodsCart> carts_user = new ArrayList<GoodsCart>();// 未提交的用户user购物车
		Result result = null;
		Map cart_map = new HashMap();
		Set mark_ids = new TreeSet();
		Map json_map = new HashMap();
		String json = null;
		int next = 0;
		String cart_mobile_id = null;
		int code = 100;// 100成功，-100用户信息错误，
		User user = null;

		if (token.equals("")) {
			code = -100;
		} else {
			Map params = new HashMap();
			user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				code = -100;
			} else {
				code = 100;
			}
		}
		if (code == 100) {
			if (cart_mobile_ids.equals("")) {
				mark_ids.add("0");
			} else {
				String mobile_ids[] = cart_mobile_ids.split(",");
				for (String mobile_id : mobile_ids) {
					if (!mobile_id.equals("")) {
						mark_ids.add(mobile_id);
					}
				}
			}

			if (user != null) {
				cart_map.clear();
				cart_map.put("mark_ids", mark_ids);
				cart_map.put("cart_status", 0);
				carts_mobile = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_mobile_id in (:mark_ids) and obj.cart_status=:cart_status ",
						cart_map, -1, -1);
				// 如果用户拥有自己的店铺，删除carts_mobile购物车中自己店铺中的商品信息
				if (user.getStore() != null) {
					for (GoodsCart gc : carts_mobile) {
						if (gc.getGoods().getGoods_type() == 1) {// 该商品为商家商品
							if (gc.getGoods().getGoods_store().getId().equals(user.getStore().getId())) {
								this.goodsCartService.delete(gc.getId());
							}
						}
					}
				}
				cart_map.clear();
				cart_map.put("user_id", user.getId());
				cart_map.put("cart_status", 0);
				carts_user = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status ",
						cart_map, -1, -1);
			} else {
				cart_map.clear();
				cart_map.put("mark_ids", mark_ids);
				cart_map.put("cart_status", 0);
				carts_mobile = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_mobile_id in (:mark_ids) and obj.cart_status=:cart_status ",
						cart_map, -1, -1);
			}
			// 将mobile购物车与用户user购物车合并，去重
			if (user != null) {
				for (GoodsCart ugc : carts_user) {
					carts_list.add(ugc);
				}
				for (GoodsCart mobile : carts_mobile) {
					boolean add = true;
					for (GoodsCart gc2 : carts_user) {
						if (mobile.getGoods().getId().equals(gc2.getGoods().getId())) {
							if (mobile.getSpec_info().equals(gc2.getSpec_info())) {
								add = false;
								this.goodsCartService.delete(mobile.getId());
							}
						}
					}
					if (add) {// 将carts_mobile转变为user_cart
						mobile.setCart_mobile_id(null);
						mobile.setUser(user);
						this.goodsCartService.update(mobile);
						carts_list.add(mobile);
					}
				}
			} else {
				for (GoodsCart mobile : carts_mobile) {
					carts_list.add(mobile);
				}
			}
			// 新添加购物车,排除没有重复商品后添加到carts_list中
			Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
			if (goods.getGoods_status() == 0) {
				if (goods.getGoods_store().getStore_status() == 15) {
					if (CommUtil.null2String(gsp).equals("")) {// 从商品列表页添加到购车，生成默认的gsp信息
						gsp = this.generic_default_gsp(goods);
					}
					int goods_inventory = CommUtil
							.null2Int(this.mCartViewTools.generic_default_info_color(goods, gsp, color).get("count"));// 计算商品库存信息
					if (goods_inventory > 0) {
						String[] gsp_ids = gsp.split(",");

						String temp_gsp = gsp;
						if ("parts".equals(buy_type)) {
							if (combin_ids != null && !combin_ids.equals("")) {
								next = 1;
							}
						}
						if ("suit".equals(buy_type)) {
							if (combin_ids != null && !combin_ids.equals("")) {
								next = 2;
							}
						}
						boolean add = true;
						boolean combin_add = true;
						if ("suit".equals(buy_type)) {
							combin_add = false;
						}
						for (GoodsCart gc : carts_list) {
							if (gsp_ids != null && gsp_ids.length > 0 && gc.getGsps().size() > 0
									|| !CommUtil.null2String(gc.getColor()).equals("")) {
								String[] gsp_ids1 = new String[gc.getGsps().size()];
								for (int i = 0; i < gc.getGsps().size(); i++) {
									gsp_ids1[i] = gc.getGsps().get(i) != null ? gc.getGsps().get(i).getId().toString()
											: "";
								}
								if (gsp_ids1.length == 0 && "".equals(CommUtil.null2String(gsp))) {
									if (!gc.getColor().equals("")) {
										if (gc.getColor().equals(color)
												&& gc.getGoods().getId().toString().equals(goods_id)) {
											add = false;
											gc.setCount(gc.getCount() + CommUtil.null2Int(count));
											this.goodsCartService.update(gc);
											break;
										}
									}
								} else {
									Arrays.sort(gsp_ids);
									Arrays.sort(gsp_ids1);
									if (gc.getGoods().getId().toString().equals(goods_id)
											&& Arrays.equals(gsp_ids, gsp_ids1) && gc.getColor().equals(color)) {
										if ("combin".equals(gc.getCart_type())) {
											if (!combin_add) {
												add = false;
												break;
											} else {
												add = true;
											}
										} else {
											add = false;
											break;
										}
									}
								}

							} else {
								if (gc.getGoods().getId().toString().equals(goods_id)) {
									if ("combin".equals(gc.getCart_type())) {
										if (!combin_add) {
											add = false;
											break;
										} else {
											add = true;
										}
									} else {
										add = false;
										break;
									}
								}
							}
						}
						cart_mobile_id = CommUtil.randomString(12) + "_mobile_" + CommUtil.formatLongDate(new Date());

						if (add && combin_add) {// 排除购物车中没有重复商品后添加该商品到购物车,并且非组合添加
							GoodsCart obj = new GoodsCart();
							obj.setAddTime(new Date());
							obj.setCart_gsp(gsp);
							obj.setColor(color);
							obj.setCount(CommUtil.null2Int(count));
							// obj.setPrice(BigDecimal.valueOf(CommUtil.null2Double(price)));
							BigDecimal goods_price = CommUtil.null2BigDecimal(this.mCartViewTools
									.generic_default_info_color(goods, gsp, color).get("goods_current_price"));
							obj.setPrice(goods_price);
							obj.setGoods(goods);
							GoodsLog goodsLog = goodsViewTools.getTodayGoodsLog(CommUtil.null2Long(goods_id));
							goodsLog.setGoods_cart(goodsLog.getGoods_cart() + 1);
							this.goodsLogService.update(goodsLog);

							String spec_info = "";
							for (String gsp_id : gsp_ids) {
								GoodsSpecProperty spec_property = this.goodsSpecPropertyService
										.getObjById(CommUtil.null2Long(gsp_id));
								obj.getGsps().add(spec_property);
								if (spec_property != null) {
									spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue()
											+ "<br> " + spec_info;
								}
							}
							if (user == null) {
								obj.setCart_mobile_id(cart_mobile_id);// 设置手机端未登录用户会话Id
							} else {
								obj.setUser(user);
							}
							obj.setSpec_info(spec_info);
							this.goodsCartService.save(obj);
							double cart_total_price = 0;
							for (GoodsCart gc : carts_list) {
								if (CommUtil.null2String(gc.getCart_type()).equals("")) {
									cart_total_price = cart_total_price
											+ CommUtil.null2Double(gc.getGoods().getGoods_current_price())
													* gc.getCount();
								}
								if (CommUtil.null2String(gc.getCart_type()).equals("combin")) { // 如果是组合销售购买，则设置组合价格
									cart_total_price = cart_total_price + this.getGoodsCombinPrice() * gc.getCount();
								}
							}
						}

						if (next == 1) {// 组合配件商品添加
							String part_ids[] = combin_ids.split(",");
							for (String part_id : part_ids) {
								if (!part_id.equals("")) {
									Goods part_goods = this.goodsService.getObjById(CommUtil.null2Long(part_id));
									GoodsCart part_cart = new GoodsCart();
									boolean part_add = true;
									part_cart.setAddTime(new Date());
									String temp_gsp_parts = null;
									temp_gsp_parts = this.generic_default_gsp(part_goods);
									String[] part_gsp_ids = CommUtil.null2String(temp_gsp_parts).split(",");
									Arrays.sort(part_gsp_ids);
									for (GoodsCart gc : carts_list) {
										if (part_gsp_ids != null && part_gsp_ids.length > 0 && gc.getGsps() != null
												&& gc.getGsps().size() > 0) {
											String[] gsp_ids1 = new String[gc.getGsps().size()];
											for (int i = 0; i < gc.getGsps().size(); i++) {
												gsp_ids1[i] = gc.getGsps().get(i) != null
														? gc.getGsps().get(i).getId().toString() : "";
											}
											Arrays.sort(gsp_ids1);
											if (gc.getGoods().getId().toString().equals(part_id)
													&& Arrays.equals(part_gsp_ids, gsp_ids1)) {
												part_add = false;
											}
										} else {
											if (gc.getGoods().getId().toString().equals(part_id)) {
												part_add = false;
											}
										}
									}
									if (part_add) {// 排除购物车中没有重复商品后添加该商品到购物车
										part_cart.setAddTime(new Date());
										part_cart.setCount(CommUtil.null2Int(1));
										String part_price = this.generGspgoodsPrice(temp_gsp_parts, part_id, user_id);
										part_cart.setPrice(BigDecimal.valueOf(CommUtil.null2Double(part_price)));
										part_cart.setGoods(part_goods);
										String spec_info = "";
										for (String gsp_id : part_gsp_ids) {
											GoodsSpecProperty spec_property = this.goodsSpecPropertyService
													.getObjById(CommUtil.null2Long(gsp_id));
											part_cart.getGsps().add(spec_property);
											if (spec_property != null) {
												spec_info = spec_property.getSpec().getName() + ":"
														+ spec_property.getValue() + " " + spec_info;
											}
										}
										if (user == null) {
											part_cart.setCart_mobile_id(cart_mobile_id);// 设置手机端未登录用户会话Id
										} else {
											part_cart.setUser(user);
										}
										part_cart.setSpec_info(spec_info);
										this.goodsCartService.save(part_cart);
									}
								}
							}
						}
						if (next == 2) {// 组合套装商品添加
							boolean suit_add = true;
							Map params = new HashMap();
							params.put("combin_main", 1);
							params.put("cart_type", "combin");
							params.put("gid", goods.getId());
							String hql = "select obj from GoodsCart obj where obj.cart_type=:cart_type and obj.combin_main=:combin_main and obj.goods.id=:gid";
							if (user != null) {
								params.put("user_id", user.getId());
								hql += " and obj.user.id=:user_id";
							} else {
								params.put("cart_mobile_id", token);
								hql += " and obj.cart_mobile_id=:cart_mobile_id";
							}
							params.put("gid", goods.getId());
							List<GoodsCart> suit_carts = this.goodsCartService.query(hql, params, -1, -1);
							if (suit_carts.size() > 0) {
								if (suit_carts.get(0).getCombin_version()
										.contains(CommUtil.null2String(combin_version))) {
									suit_add = false;
								}
							}
							if (suit_add) {
								Map suit_map = null;
								params.clear();
								params.put("main_goods_id", CommUtil.null2Long(goods_id));
								params.put("combin_type", 0);// 组合套装
								params.put("combin_status", 1);
								List<CombinPlan> suits = this.combinplanService.query(
										"select obj from CombinPlan obj where obj.main_goods_id=:main_goods_id and obj.combin_type=:combin_type and obj.combin_status=:combin_status",
										params, -1, -1);
								for (CombinPlan plan : suits) {
									List<Map> map_list = (List<Map>) Json.fromJson(plan.getCombin_plan_info());
									for (Map temp_map : map_list) {
										String ids = this.goodsViewTools.getCombinPlanGoodsIds(temp_map);
										if (ids.equals(combin_ids)) {
											suit_map = temp_map;
											break;
										}
									}
								}
								String combin_mark = "combin" + UUID.randomUUID();
								if (suit_map != null) {
									String suit_ids = "";// 主套装购物车中其他套装购物车id，（不包含自己的id）
									List<Map> goods_list = (List<Map>) suit_map.get("goods_list");
									for (Map good_map : goods_list) {
										Goods suit_goods = this.goodsService
												.getObjById(CommUtil.null2Long(good_map.get("id")));
										GoodsCart cart = new GoodsCart();
										cart.setAddTime(new Date());
										cart.setGoods(suit_goods);
										String spec_info = "";
										String temp_gsp_ids[] = CommUtil.null2String(this.generic_default_gsp(goods))
												.split(",");
										for (String gsp_id : temp_gsp_ids) {
											GoodsSpecProperty spec_property = this.goodsSpecPropertyService
													.getObjById(CommUtil.null2Long(gsp_id));
											if (spec_property != null) {
												cart.getGsps().add(spec_property);
												spec_info = spec_property.getSpec().getName() + "："
														+ spec_property.getValue() + "<br>" + spec_info;
											}
										}
										cart.setSpec_info(spec_info);
										cart.setCombin_mark(combin_mark);
										cart.setCart_type("combin");
										cart.setPrice(BigDecimal
												.valueOf(CommUtil.null2Double(suit_goods.getGoods_current_price())));
										cart.setCount(1);
										if (user == null) {
											cart.setCart_mobile_id(cart_mobile_id);// 设置手机端未登录用户会话Id
										} else {
											cart.setUser(user);
										}
										this.goodsCartService.save(cart);
										suit_ids = suit_ids + "," + CommUtil.null2String(cart.getId());
									}
									GoodsCart obj = new GoodsCart();// 套装主购物车
									String combin_main_default_gsp = this.generic_default_gsp(goods);
									obj.setCart_gsp(combin_main_default_gsp);
									obj.setAddTime(new Date());
									obj.setCount(CommUtil.null2Int(count));
									if (price == null || price.equals("")) {
										price = this.generGspgoodsPrice(temp_gsp, goods_id, user_id);
									}
									obj.setPrice(BigDecimal.valueOf(CommUtil.null2Double(price)));
									obj.setGoods(goods);
									if (user == null) {
										obj.setCart_mobile_id(cart_mobile_id);
									} else {
										obj.setUser(user);
									}
									obj.setCombin_suit_ids(suit_ids);// 更新套装主购物车id信息(包括自己的id)
									obj.setCombin_main(1);
									obj.setCombin_version("【套装" + combin_version + "】");
									obj.setCount(CommUtil.null2Int(count));
									obj.setPrice(
											BigDecimal.valueOf(CommUtil.null2Double(suit_map.get("plan_goods_price"))));
									obj.setCombin_mark(combin_mark);
									obj.setCart_type("combin");
									String spec_info = "";
									String temp_gsp_ids[] = CommUtil.null2String(this.generic_default_gsp(goods))
											.split(",");
									for (String gsp_id : temp_gsp_ids) {
										GoodsSpecProperty spec_property = this.goodsSpecPropertyService
												.getObjById(CommUtil.null2Long(gsp_id));
										if (spec_property != null) {
											obj.getGsps().add(spec_property);
											spec_info = spec_property.getSpec().getName() + "："
													+ spec_property.getValue() + "<br>" + spec_info;
										}
									}
									obj.setCart_gsp(this.generic_default_gsp(goods));
									obj.setSpec_info(spec_info);
									suit_map.put("suit_count", CommUtil.null2Int(count));
									String suit_all_price = CommUtil.formatMoney(CommUtil.mul(CommUtil.null2Int(count),
											CommUtil.null2Double(suit_map.get("plan_goods_price"))));
									suit_map.put("suit_all_price", suit_all_price);// 套装整体价格=套装单价*数量
									obj.setCombin_suit_info(Json.toJson(suit_map, JsonFormat.compact()));
									this.goodsCartService.save(obj);
								}
							} else {
								GoodsCart update_cart = suit_carts.get(0);
								Map temp_map = Json.fromJson(Map.class, update_cart.getCombin_suit_info());
								temp_map.put("suit_count", update_cart.getCount() + 1);
								update_cart.setCombin_suit_info(Json.toJson(temp_map, JsonFormat.compact()));
								update_cart.setCount(update_cart.getCount() + 1);
								this.goodsCartService.update(update_cart);
							}
						}
						if (cart_mobile_id != null) {
							json_map.put("cart_mobile_id", cart_mobile_id);
						}
						result = new Result(0, "token success", json_map);
					} else {
						result = new Result(3, "库存为零");
					}
				} else {
					result = new Result(2, "The shop is closed");
				}
			} else {
				result = new Result(1, "sold out");
			}
		} else {
			result = new Result(code, "token Invalidation");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 根据商品信息，计算该商品默认的规格信息，以各个规格值的第一个为默认值
	 * 
	 * @param goods
	 *            商品
	 * @return 默认规格id组合，如1,2
	 */
	private String generic_default_gsp(Goods goods) {
		String gsp = "";
		if (goods != null) {
			List<GoodsSpecification> specs = this.goodsViewTools.generic_spec(CommUtil.null2String(goods.getId()));
			for (GoodsSpecification spec : specs) {
				for (GoodsSpecProperty prop : goods.getGoods_specs()) {
					if (prop.getSpec().getId().equals(spec.getId())) {
						gsp = prop.getId() + "," + gsp;
						break;
					}
				}
			}
		}
		return gsp;
	}

	/**
	 * 用户登陆后清除用户购物车中自己店铺的商品，将cookie购物车与用户user购物车合并，去重复商品（相同商品不同规格不去掉） 未使用
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private List<GoodsCart> cart_calc(HttpServletRequest request) {
		List<GoodsCart> carts_list = new ArrayList<GoodsCart>();// 用户整体购物车
		// List<GoodsCart> carts_cookie = new ArrayList<GoodsCart>();//
		// 未提交的用户cookie购物车
		List<GoodsCart> carts_user = new ArrayList<GoodsCart>();// 未提交的用户user购物车
		User user = SecurityUserHolder.getCurrentUser();
		Map cart_map = new HashMap();
		if (user != null) {
			user = userService.getObjById(user.getId());

			cart_map.clear();
			cart_map.put("user_id", user.getId());
			cart_map.put("cart_status", 0);
			carts_user = this.goodsCartService.query(
					"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status ",
					cart_map, -1, -1);

		}
		// 将cookie购物车与用户user购物车合并，去重
		if (user != null) {
			for (GoodsCart ugc : carts_user) {
				carts_list.add(ugc);
			}

		}
		// 组合套装处理，只显示套装主购物车,套装内其他购物车不显示
		List<GoodsCart> combin_carts_list = new ArrayList<GoodsCart>();
		for (GoodsCart gc : carts_list) {
			if (gc.getCart_type() != null && gc.getCart_type().equals("combin")) {
				if (gc.getCombin_main() != 1) {// 组合购物车中非主购物车
					combin_carts_list.add(gc);
				}
			}
		}
		if (combin_carts_list.size() > 0) {
			carts_list.removeAll(combin_carts_list);
		}
		return carts_list;
	}

	/**
	 * 根据商品及传递的规格信息，计算该规格商品的价格、库存量
	 * 
	 * @param goods
	 * @param gsp
	 * @return 价格、库存组成的Map
	 */
	private Map generic_default_info(Goods goods, String gsp, User user) {
		double price = 0;
		Map map = new HashMap();
		int count = 0;
		if (goods.getGroup() != null && goods.getGroup_buy() == 2) {// 团购商品统一按照团购价格处理
			for (GroupGoods gg : goods.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(goods.getGroup().getId())) {
					count = gg.getGg_count();
					price = CommUtil.null2Double(gg.getGg_price());
				}
			}
		} else {
			Long id = goods.getId();
			count = goods.getGoods_inventory();
			price = CommUtil.null2Double(goods.getStore_price());
			if ("spec".equals(goods.getInventory_type())) {
				if (gsp != null && !gsp.equals("")) {
					List<HashMap> list = Json.fromJson(ArrayList.class, goods.getGoods_inventory_detail());
					String[] gsp_ids = gsp.split(",");
					for (Map temp : list) {
						String[] temp_ids = CommUtil.null2String(temp.get("id")).split("_");
						Arrays.sort(gsp_ids);
						Arrays.sort(temp_ids);
						if (Arrays.equals(gsp_ids, temp_ids)) {
							count = CommUtil.null2Int(temp.get("count"));
							price = CommUtil.null2Double(temp.get("price"));
						}
					}
				}
			}
		}
		BigDecimal ac_rebate = null;
		if (goods.getActivity_status() == 2 && user != null) {// 如果是促销商品，并且用户已登录，根据规格配置价格计算相应配置的促销价格
			ActivityGoods actGoods = this.actgoodsService.getObjById(goods.getActivity_goods_id());
			// 0—铜牌会员1—银牌会员2—金牌会员3—超级会员
			BigDecimal rebate = BigDecimal.valueOf(0.00);
			int level = this.integralViewTools.query_user_level(CommUtil.null2String(user.getId()));
			if (level == 0) {
				rebate = actGoods.getAct().getAc_rebate();
			} else if (level == 1) {
				rebate = actGoods.getAct().getAc_rebate1();
			} else if (level == 2) {
				rebate = actGoods.getAct().getAc_rebate2();
			} else if (level == 3) {
				rebate = actGoods.getAct().getAc_rebate3();
			}
			price = CommUtil.mul(rebate, price);
		}
		map.put("price", price);
		map.put("count", count);
		return map;
	}

	/**
	 * @description 用户购物车列表
	 * @param request
	 * @param response
	 * @param load_class
	 * @param token
	 *            用户身份令牌
	 * @param cart_session
	 *            游客cartSessionId
	 * @param language
	 */
	@RequestMapping("v1/cartList.json")
	public void goodsCartLoad(HttpServletRequest request, HttpServletResponse response, String load_class,
			@RequestParam(value = "token") String token, String visitor_id, String language) {
		Result result = null;
		Map<String, Object> goods_cart_map = new HashMap<String, Object>();
		Date date = new Date();
		User user = null;
		if (!"".equals(token)) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		// List<GoodsCart> carts = this.cart_calc(user); 限制登陆用户 获取已登录用户购物车
		String cart_session_id = "";
		if (visitor_id != null && !"".equals(visitor_id)) {
			cart_session_id = visitor_id;
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("cart_session_id")) {
						cart_session_id = CommUtil.null2String(cookie.getValue());
					}
				}
			}
		}
		if (cart_session_id.equals("")) {
			cart_session_id = UUID.randomUUID().toString();
			Cookie cookie = new Cookie("cart_session_id", cart_session_id);
			cookie.setDomain(CommUtil.generic_domain(request));
			response.addCookie(cookie);
		}
		List<GoodsCart> cartsList = this.mCartViewTools.cartListCalc(request, user, cart_session_id);// 获取登录与未登录用户购物车
		if (cartsList.size() > 0) {
			Set<Long> buyGiftSet = new HashSet<Long>();
			Set<Long> storeIds = new HashSet<Long>();
			List<GoodsCart> native_goods = new ArrayList<GoodsCart>();// 无活动购物车
			Map<Long, List<GoodsCart>> enoughReduceMap = new HashMap<Long, List<GoodsCart>>();// 满就减购物车enough_reduce
			List<GoodsCart> point_goods = new ArrayList<GoodsCart>();
			Map<Long, String> erString = new HashMap<Long, String>();
			for (GoodsCart cart : cartsList) {
				if (cart.getGoods().getOrder_enough_give_status() == 1 && cart.getGoods().getBuyGift_id() != null) {
					BuyGift bg = this.buyGiftService.getObjById(cart.getGoods().getBuyGift_id());
					if (bg.getBeginTime().before(date)) {
						buyGiftSet.add(cart.getGoods().getBuyGift_id());
					} else {
						native_goods.add(cart);
					}
				} else if (cart.getGoods().getEnough_reduce() == 1) {// 满就减
					String er_id = cart.getGoods().getOrder_enough_reduce_id();
					EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
					if (er.getErstatus() == 10 && er.getErbegin_time().before(date)) {
						if (enoughReduceMap.containsKey(er.getId())) {
							enoughReduceMap.get(er.getId()).add(cart);
						} else {
							List<GoodsCart> list = new ArrayList<GoodsCart>();
							list.add(cart);
							enoughReduceMap.put(er.getId(), list);
							// Map map = (Map)
							// Json.fromJson(er.getEr_json());
							/*
							 * double k = 0; String str = ""; for (Object key :
							 * map.keySet()) { if (k == 0) { k =
							 * Double.parseDouble(key.toString()); str =
							 * "The activity products buy "+k+
							 * " AED, you can enjoy the discount"; } if
							 * (Double.parseDouble(key.toString()) < k) { k =
							 * Double.parseDouble(key.toString()); str =
							 * "The activity product buy "+k+
							 * " AED, you can enjoy the discount"; } }
							 */
							erString.put(er.getId(), er.getEr_json());
						}
					} else {
						native_goods.add(cart);
					}
				} else {
					native_goods.add(cart);
				}
				if (!storeIds.contains(cart.getGoods().getGoods_store().getId())) {
					storeIds.add(cart.getGoods().getGoods_store().getId());
				}
			}
			List<GoodsCart> erCarts = new ArrayList<GoodsCart>();// 将有活动的商品分组(满就减)
			List<List<Map<String, Object>>> enoughReduceGoodsList = new ArrayList<List<Map<String, Object>>>();
			for (Long key : enoughReduceMap.keySet()) {
				erCarts = enoughReduceMap.get(key);
				enoughReduceGoodsList.add(this.mCartViewTools.cartGoods(erCarts, storeIds, erString, key, language));
			}
			goods_cart_map.put("ermaps", enoughReduceGoodsList);

			// 暂时没有满就送
			/*
			 * if (buyGiftSet.size() > 0) {// 将有活动的商品分组(满就送) Map<Long,
			 * List<GoodsCart>> buyGiftMap = new HashMap<Long,
			 * List<GoodsCart>>(); for (Long id : buyGiftSet) {
			 * buyGiftMap.put(id, new ArrayList<GoodsCart>()); } for (GoodsCart
			 * cart : cartsList) { if
			 * (cart.getGoods().getOrder_enough_give_status() == 1 &&
			 * cart.getGoods().getBuyGift_id() != null) { if
			 * (buyGiftMap.containsKey(cart.getGoods().getBuyGift_id())) {
			 * buyGiftMap.get(cart.getGoods().getBuyGift_id()).add(cart); } } }
			 * List<GoodsCart> buyGiftLis = new ArrayList<GoodsCart>(); for
			 * (Long buyGiftKey : buyGiftMap.keySet()) { buyGiftLis =
			 * buyGiftMap.get(buyGiftKey); List<Map<String, Object>>
			 * buyGiftGoodsList = new ArrayList<Map<String, Object>>(); for
			 * (GoodsCart obj : buyGiftLis) { Map<String, Object> map = new
			 * HashMap<String, Object>(); map.put("goods_cart_id", obj.getId());
			 * map.put("goods_main_photo", obj.getGoods().getGoods_main_photo()
			 * == null ? "" :
			 * this.configService.getSysConfig().getImageWebServer() + "/" +
			 * obj.getGoods().getGoods_main_photo().getPath() + "/" +
			 * obj.getGoods().getGoods_main_photo().getName());
			 * map.put("goods_id", obj.getGoods().getId());
			 * map.put("goods_name", obj.getGoods().getGoods_name());
			 * map.put("goods_type", obj.getGoods().getGoods_type());
			 * map.put("goods_inventory", obj.getGoods().getGoods_inventory());
			 * map.put("goods_curren_price", obj.getPrice());
			 * map.put("goods_store_price", obj.getGoods().getStore_price());
			 * map.put("goods_spec", obj.getSpec_info() == null ? "" :
			 * obj.getSpec_info()); map.put("goods_status",
			 * obj.getGoods().getGoods_status()); map.put("store_status",
			 * obj.getGoods().getGoods_store().getStore_status());
			 * map.put("goods_collect", obj.getGoods().getGoods_collect()); if
			 * (obj.getGoods().getGoods_store() != null) { map.put("store_name",
			 * obj.getGoods().getGoods_store().getStore_name());
			 * map.put("store_id", obj.getGoods().getGoods_store().getId());
			 * map.put("store_logo",
			 * obj.getGoods().getGoods_store().getStore_logo() == null ? "" :
			 * this.configService.getSysConfig().getImageWebServer() + "/" +
			 * obj.getGoods().getGoods_store().getStore_logo().getPath() + "/" +
			 * obj.getGoods().getGoods_store().getStore_logo().getName()); }
			 * buyGiftGoodsList.add(map); }
			 * goods_cart_map.put("buyGiftGoodsList", buyGiftGoodsList);
			 * 
			 * BuyGift buyGift =
			 * goodsViewTools.query_buyGift(CommUtil.null2String(buyGiftKey));
			 * List<Map> bg_goodslist = new ArrayList<Map>(); for (Map bgt :
			 * CommUtil.Json2List(buyGift.getGift_info())) { Map bgtmap = new
			 * HashMap(); bgtmap.put("storegoods_count",
			 * bgt.get("storegoods_count"));// [赠送数量100当前商品库存变为100 //
			 * storegoods_count为1时使用商品当前库存为赠送数量 // 如库存为200则赠送个数为200 //
			 * 当正常出售1个赠送商品后库存为199赠送数量也为199,赠送数与库存数同步此时没有goods_count]
			 * bgtmap.put("goods_id", bgt.get("goods_id"));
			 * bgtmap.put("goods_name", bgt.get("goods_name"));
			 * bgtmap.put("goods_price", bgt.get("goods_price"));
			 * bgtmap.put("goods_main_photo",
			 * this.configService.getSysConfig().getImageWebServer() + "/" +
			 * bgt.get("goods_main_photo")); bg_goodslist.add(bgtmap); }
			 * goods_cart_map.put("bg_goods_info", bg_goodslist); } }
			 */
			Map<String, List<GoodsCart>> separate_carts = this.mCartViewTools.separateCombin(native_goods);// 传入没有分离组合活动商品的购物车
			List<GoodsCart> normalGoodsCart = (List<GoodsCart>) separate_carts.get("normal");// 无活动的商品购物车
			List<Map<String, Object>> normalList = this.mCartViewTools.cartGoods(normalGoodsCart, storeIds, null, null,
					language);
			goods_cart_map.put("normalmap", normalList);
			/*
			 * List<Map> pointlist = this.mCartViewTools.queryGoods(point_goods,
			 * storeIds, null, null); goods_cart_map.put("pointlist",
			 * pointlist);
			 */
			/*
			 * List<GoodsCart> combin = (List<GoodsCart>)
			 * separate_carts.get("combin");// 组合套装商品购物车
			 * //[默认为空，组合销售时候为"combin"] List<Map> combinlist =
			 * this.mCartViewTools.queryGoods(combin, storeIds);
			 * goods_cart_map.put("combinlist", combinlist);
			 * goods_cart_map.put("cart_num", combin.size());
			 */
		}
		goods_cart_map.put("cart_session_id", cart_session_id);
		goods_cart_map.put("cart_num", cartsList.size());
		result = new Result(0, "success", goods_cart_map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 购物车列表-确认购物车商品
	 * @param request
	 * @param response
	 * @param load_class
	 * @param token
	 *            用户唯一标识
	 */
	// @RequestMapping("/goods_cart1_load.json")
	@RequestMapping("v1/goods_cart1_load.json")
	public void goods_cart1_load(HttpServletRequest request, HttpServletResponse response, String load_class,
			@RequestParam(value = "token") String token, String language) {
		Result result = null;
		Map goods_cart_map = new HashMap();
		Date date = new Date();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				List<GoodsCart> carts = this.mCartViewTools.cart_calc(user);
				if (carts.size() > 0) {
					Set<Long> set = new HashSet<Long>();
					Set<Long> storeIds = new HashSet<Long>();
					List<GoodsCart> native_goods = new ArrayList<GoodsCart>();
					Map<Long, List<GoodsCart>> ermap = new HashMap<Long, List<GoodsCart>>();
					List<GoodsCart> point_goods = new ArrayList<GoodsCart>();
					Map<Long, String> erString = new HashMap<Long, String>();
					for (GoodsCart cart : carts) {
						if (cart.getGoods().getOrder_enough_give_status() == 1
								&& cart.getGoods().getBuyGift_id() != null) {
							BuyGift bg = this.buyGiftService.getObjById(cart.getGoods().getBuyGift_id());
							if (bg.getBeginTime().before(date)) {
								set.add(cart.getGoods().getBuyGift_id());
							} else {
								native_goods.add(cart);
							}
						} else if (cart.getGoods().getEnough_reduce() == 1) {// 满就减
							String er_id = cart.getGoods().getOrder_enough_reduce_id();
							EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
							if (er.getErstatus() == 10 && er.getErbegin_time().before(date)) {
								if (ermap.containsKey(er.getId())) {
									ermap.get(er.getId()).add(cart);
								} else {
									List<GoodsCart> list = new ArrayList<GoodsCart>();
									list.add(cart);
									ermap.put(er.getId(), list);
									// Map map = (Map)
									// Json.fromJson(er.getEr_json());
									/*
									 * double k = 0; String str = ""; for
									 * (Object key : map.keySet()) { if (k == 0)
									 * { k = Double.parseDouble(key.toString());
									 * str = "The activity products buy "+k+
									 * " AED, you can enjoy the discount"; } if
									 * (Double.parseDouble(key.toString()) < k)
									 * { k = Double.parseDouble(key.toString());
									 * str = "The activity product buy "+k+
									 * " AED, you can enjoy the discount"; } }
									 */
									erString.put(er.getId(), er.getEr_json());
								}
							} else {
								native_goods.add(cart);
							}
						} else {
							native_goods.add(cart);
						}
						if (!storeIds.contains(cart.getGoods().getGoods_store().getId())) {
							storeIds.add(cart.getGoods().getGoods_store().getId());
						}
					}

					List<GoodsCart> erCarts = new ArrayList<GoodsCart>();// 将有活动的商品分组(满就减)
					List goodsList = new ArrayList();
					List<List<Map<String, Object>>> ermaps = new ArrayList<List<Map<String, Object>>>();

					for (Long key : ermap.keySet()) {
						erCarts = ermap.get(key);
						// goodsList = this.mCartViewTools.queryGoods(erCarts,
						// storeIds, erString, key);
						ermaps.add(this.mCartViewTools.cartGoods(erCarts, storeIds, erString, key, language));
					}
					goods_cart_map.put("ermaps", ermaps);

					if (set.size() > 0) {// 将有活动的商品分组(满就送)
						Map<Long, List<GoodsCart>> map = new HashMap<Long, List<GoodsCart>>();
						for (Long id : set) {
							map.put(id, new ArrayList<GoodsCart>());
						}
						for (GoodsCart cart : carts) {
							if (cart.getGoods().getOrder_enough_give_status() == 1
									&& cart.getGoods().getBuyGift_id() != null) {
								if (map.containsKey(cart.getGoods().getBuyGift_id())) {
									map.get(cart.getGoods().getBuyGift_id()).add(cart);
								}
							}
						}
						List<GoodsCart> ac_list = new ArrayList<GoodsCart>();
						for (Long ac_key : map.keySet()) {
							ac_list = map.get(ac_key);
							List<Map> ac_goods_list = new ArrayList<Map>();
							for (GoodsCart obj : ac_list) {
								Map acmap = new HashMap();
								acmap.put("goods_cart_id", obj.getId());
								acmap.put("goods_main_photo",
										obj.getGoods().getGoods_main_photo() == null ? ""
												: this.configService.getSysConfig().getImageWebServer() + "/"
														+ obj.getGoods().getGoods_main_photo().getPath() + "/"
														+ obj.getGoods().getGoods_main_photo().getName());
								acmap.put("goods_id", obj.getGoods().getId());
								acmap.put("goods_name", obj.getGoods().getGoods_name());
								acmap.put("goods_type", obj.getGoods().getGoods_type());
								acmap.put("goods_inventory", obj.getGoods().getGoods_inventory());
								acmap.put("goods_curren_price", obj.getPrice());
								acmap.put("goods_store_price", obj.getGoods().getStore_price());
								acmap.put("goods_spec", obj.getSpec_info() == null ? "" : obj.getSpec_info());
								acmap.put("goods_status", obj.getGoods().getGoods_status());
								acmap.put("store_status", obj.getGoods().getGoods_store().getStore_status());
								acmap.put("goods_collect", obj.getGoods().getGoods_collect());
								if (obj.getGoods().getGoods_store() != null) {
									acmap.put("store_name", obj.getGoods().getGoods_store().getStore_name());
									acmap.put("store_id", obj.getGoods().getGoods_store().getId());
									acmap.put("store_logo", obj.getGoods().getGoods_store().getStore_logo() == null ? ""
											: this.configService.getSysConfig().getImageWebServer() + "/"
													+ obj.getGoods().getGoods_store().getStore_logo().getPath() + "/"
													+ obj.getGoods().getGoods_store().getStore_logo().getName());
								}
								ac_goods_list.add(acmap);
							}
							goods_cart_map.put("ac_goods_list", ac_goods_list);

							BuyGift bg = goodsViewTools.query_buyGift(CommUtil.null2String(ac_key));
							List<Map> bg_goodslist = new ArrayList<Map>();
							for (Map bgt : CommUtil.Json2List(bg.getGift_info())) {
								Map bgtmap = new HashMap();
								bgtmap.put("storegoods_count", bgt.get("storegoods_count"));// [赠送数量100
																							// 当前商品库存变为100
																							// storegoods_count为1时
																							// 使用商品当前库存为赠送数量
																							// 如库存为200
																							// 则赠送个数为200
																							// 当正常出售1个赠送商品
																							// 后库存为199
																							// 赠送数量也为199
																							// 赠送数与库存数同步
																							// 此时没有goods_count]
								bgtmap.put("goods_id", bgt.get("goods_id"));
								bgtmap.put("goods_name", bgt.get("goods_name"));
								bgtmap.put("goods_price", bgt.get("goods_price"));
								bgtmap.put("goods_main_photo", this.configService.getSysConfig().getImageWebServer()
										+ "/" + bgt.get("goods_main_photo"));
								bg_goodslist.add(bgtmap);
							}
							goods_cart_map.put("bg_goods_info", bg_goodslist);
						}
					}

					Map<String, List<GoodsCart>> separate_carts = this.mCartViewTools.separateCombin(native_goods);// 传入没有分离组合活动商品的购物车
					List<GoodsCart> normalGoodsCart = (List<GoodsCart>) separate_carts.get("normal");// 无活动的商品购物车
					List<Map<String, Object>> normalList = this.mCartViewTools.cartGoods(normalGoodsCart, storeIds,
							null, null, language);
					goods_cart_map.put("normalmap", normalList);
					/*
					 * List<Map> pointlist =
					 * this.mCartViewTools.queryGoods(point_goods, storeIds,
					 * null, null); goods_cart_map.put("pointlist", pointlist);
					 */
					/*
					 * List<GoodsCart> combin = (List<GoodsCart>)
					 * separate_carts.get("combin");// 组合套装商品购物车
					 * //[默认为空，组合销售时候为"combin"] List<Map> combinlist =
					 * this.mCartViewTools.queryGoods(combin, storeIds);
					 * goods_cart_map.put("combinlist", combinlist);
					 * goods_cart_map.put("cart_num", combin.size());
					 */
				}
				goods_cart_map.put("cart_num", carts.size());
				result = new Result(0, "success", goods_cart_map);
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 计算所有购物车商品的店铺id
	 * 
	 * @param carts
	 * @return
	 */
	public List<Object> store(List<GoodsCart> carts) {
		List<Object> store_list = new ArrayList<Object>();
		if (!carts.isEmpty()) {
			for (GoodsCart gc : carts) {
				if (gc.getGoods().getGoods_type() == 1) {
					store_list.add(gc.getGoods().getGoods_store().getId());
				} else {
					store_list.add("self");
				}
			}
			HashSet hs = new HashSet(store_list);
			store_list.removeAll(store_list);
			store_list.addAll(hs);
		}
		return store_list;

	}

	/**
	 * @param request
	 * @param response
	 * @param cart_ids
	 * @param token
	 * @param cart_session
	 * @descript 移除购物车，修改购物车状态为-1
	 */
	@RequestMapping("v1/removeCart.json")
	public void removeCart(HttpServletRequest request, HttpServletResponse response, String ids, String visitor_id,
			String token) {
		User user = null;
		int code = -1;
		String msg = "";
		double total_price = 0.00;// 购物车商品总价
		// 用户整体购物车
		List<GoodsCart> cartsList = new ArrayList<GoodsCart>();
		if (!"".equals(token)) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		String cart_session_id = "";
		if (null != visitor_id && !"".equals(visitor_id)) {
			cart_session_id = visitor_id;
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("cart_session_id")) {
						cart_session_id = CommUtil.null2String(cookie.getValue());
					}
				}
			}
		}
		if (null != cart_session_id && cart_session_id.equals("")) {
			cart_session_id = UUID.randomUUID().toString();
			Cookie cookie = new Cookie("cart_session_id", cart_session_id);
			cookie.setDomain(CommUtil.generic_domain(request));
			response.addCookie(cookie);
		}
		if (null == user && !"".equals(visitor_id)) {
			cart_session_id = visitor_id;
		}
		cartsList = this.mCartViewTools.cartListCalc(request, user, cart_session_id);
		if (null != ids && !"".equals(ids) && cartsList.size() > 0) {
			String cart_ids[] = ids.split(",");
			for (String id : cart_ids) {
				GoodsCart obj = this.goodsCartService.getObjById(CommUtil.null2Long(id));
				if (null != obj) {
					if (null != obj.getCart_type() && obj.getCart_type().equals("0")
							|| "1".equals(obj.getCart_type())) {
						// 判断该购物车是否为当前用户
						for (GoodsCart goodsCart : cartsList) {
							if (goodsCart.getId().equals(obj.getId())) {
								obj.getGsps().clear();
								obj.setDeleteStatus(-1);
								this.goodsCartService.update(obj);
							}
						}
						code = 4200;
						msg = "Successfully";
					}
				} else {
					code = 4400;
					msg = "Parameter Error";
				}
			}
		} else {
			code = 4400;
			msg = "Parameter Error";
		}
		cartsList = this.mCartViewTools.cartListCalc(request, user, cart_session_id);
		total_price = this.mCartViewTools.calCartPrice(cartsList, "");
		Map map = new HashMap();
		map.put("total_price", total_price);
		map.put("cart_size", cartsList.size());
		Result result = new Result(code, msg, map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 移除购物车 弃用
	 * @param request
	 * @param response
	 * @param id
	 * @param count
	 * @param price
	 * @param spec_info
	 */
	// @RequestMapping("/remove_goods_cart.json")
	@RequestMapping("v1/remove_goods_cart.json")
	public void remove_goods_cart(HttpServletRequest request, HttpServletResponse response, String ids, String token) {
		Map params = new HashMap();
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			params.put("app_login_token", token);
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				List<String> list_ids = new ArrayList<String>();
				Double total_price = 0.00;
				int code = 0;// 100表示删除成功，1表示删除失败
				List<GoodsCart> carts = new ArrayList<GoodsCart>();
				if (ids != null && !ids.equals("")) {
					String cart_ids[] = ids.split(",");
					for (String id : cart_ids) {
						if (id != null && !id.equals("")) {
							list_ids.add(id);
							if (id.indexOf("combin") < 0) {
								GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(id));
								if (gc != null) {
									// [默认为空，组合销售时候为"combin"]
									if (gc.getCart_type() != null && gc.getCart_type().equals("combin")) {// 如果商城的购物车为组合套装，则删除最后一个组合套装购物车时，组合套装主购物车更随删除
										params.clear();
										params.put("combin_mark", gc.getCombin_mark());
										params.put("combin_main", 1);
										List<GoodsCart> suit_main_carts = this.goodsCartService.query(
												"select obj from GoodsCart obj where obj.combin_mark=:combin_mark and obj.combin_main=:combin_main",
												params, -1, -1);
										if (suit_main_carts.size() > 0) {
											String suit_cart_ids[] = suit_main_carts.get(0).getCombin_suit_ids()
													.split(",");
											// 取消其他购物车组合状态
											for (String suit_cart_id : suit_cart_ids) {
												if (!suit_cart_id.equals("")) {
													GoodsCart suit_cart = this.goodsCartService
															.getObjById(CommUtil.null2Long(suit_cart_id));
													if (suit_cart != null) {
														suit_cart.setCart_type(null);
														suit_cart.setCombin_mark(null);
														suit_cart.setCombin_main(0);
														suit_cart.setCombin_suit_ids(null);
														// 设置默认规格及价钱2Long(id));
														String default_gsp = this
																.generic_default_gsp(suit_cart.getGoods());
														double default_price = CommUtil.null2Double(
																this.generic_default_info(suit_cart.getGoods(),
																		default_gsp, user).get("price"));
														suit_cart.setPrice(BigDecimal.valueOf(default_price));
														suit_cart.setCart_gsp(default_gsp);
														String[] gsp_ids = CommUtil.null2String(default_gsp).split(",");
														String spec_info = "";
														for (String gsp_id : gsp_ids) {
															GoodsSpecProperty spec_property = this.goodsSpecPropertyService
																	.getObjById(CommUtil.null2Long(gsp_id));
															if (spec_property != null) {
																suit_cart.getGsps().add(spec_property);
																spec_info = spec_property.getSpec().getName() + "："
																		+ spec_property.getValue() + "<br>" + spec_info;
															}
														}
														suit_cart.setSpec_info(spec_info);
														this.goodsCartService.update(suit_cart);
													}
												}
											}
											// 取消组合套装主购物车状态
											for (GoodsCart main_suit_gc : suit_main_carts) {
												main_suit_gc.setCart_type(null);
												main_suit_gc.setCombin_mark(null);
												main_suit_gc.setCombin_main(0);
												main_suit_gc.setCombin_suit_ids(null);
												// 设置默认规格及价钱;
												String default_gsp = this.generic_default_gsp(main_suit_gc.getGoods());
												double default_price = CommUtil
														.null2Double(this.generic_default_info(main_suit_gc.getGoods(),
																default_gsp, user).get("price"));
												main_suit_gc.setPrice(BigDecimal.valueOf(default_price));
												main_suit_gc.setCart_gsp(default_gsp);
												String[] gsp_ids = CommUtil.null2String(default_gsp).split(",");
												String spec_info = "";
												for (String gsp_id : gsp_ids) {
													GoodsSpecProperty spec_property = this.goodsSpecPropertyService
															.getObjById(CommUtil.null2Long(gsp_id));
													if (spec_property != null) {
														main_suit_gc.getGsps().add(spec_property);
														spec_info = spec_property.getSpec().getName() + "："
																+ spec_property.getValue() + "<br>" + spec_info;
													}
												}
												main_suit_gc.setSpec_info(spec_info);
												this.goodsCartService.update(main_suit_gc);
											}
										}
									}
									gc.getGsps().clear();
									this.goodsCartService.delete(CommUtil.null2Long(id));
								}
							} else {
								params.clear();
								params.put("combin_mark", id);
								List<GoodsCart> suit_carts = this.goodsCartService.query(
										"select obj from GoodsCart obj where obj.combin_mark=:combin_mark", params, -1,
										-1);
								for (GoodsCart suit_gc : suit_carts) {
									this.goodsCartService.delete(suit_gc.getId());
								}
							}
						}
					}
				} else {
					code = 1;
				}

				carts = this.mCartViewTools.cart_calc(user);
				total_price = this.mCartViewTools.calCartPrice(carts, "");
				Map map = new HashMap();
				map.put("total_price", BigDecimal.valueOf(total_price));
				map.put("count", carts.size());
				map.put("ids", list_ids);
				result = new Result(code, "success", map);
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 购物车商品数量修改
	 * @param request
	 * @param response
	 * @param gc_id
	 *            当前修改购物车id
	 * @param count
	 *            购买数量
	 * @param gcs
	 *            用户当前选中的所有购物车
	 * @param gift_id
	 */
	@RequestMapping("v1/goodsCountAdjust.json")
	public void goodsCountAdjust(HttpServletRequest request, HttpServletResponse response, String gc_id, String count,
			String gcs, String gift_id, String visitor_id, String token) {
		Result result = null;
		Map params = new HashMap();
		User user = null;
		if (!"".equals(token)) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		String cart_session_id = "";
		if (null != visitor_id && !"".equals(visitor_id)) {
			cart_session_id = visitor_id;
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("cart_session_id")) {
						cart_session_id = CommUtil.null2String(cookie.getValue());
					}
				}
			}
		}
		if (null != cart_session_id && cart_session_id.equals("")) {
			cart_session_id = UUID.randomUUID().toString();
			Cookie cookie = new Cookie("cart_session_id", cart_session_id);
			cookie.setDomain(CommUtil.generic_domain(request));
			response.addCookie(cookie);
		}
		List<GoodsCart> carts = this.mCartViewTools.cartListCalc(request, user, cart_session_id);
		Map map = new HashMap();
		int code = 4200;// 0表示修改成功，2表示库存不足,3表示团购库存不足
		String msg = "";
		double gc_price = 0.00;// 单位GoodsCart总价钱
		double priceDifference = 0.00;// 商品满包邮价格与商品当前价格差价
		double total_price = 0.00;// 购物车总价钱
		String cart_type = "";// 判断是否为组合销售
		String order_enough_reduce_id = "";
		Goods goods = null;
		int temp_count = 1;
		if (CommUtil.null2Int(count) > 1) {
			temp_count = CommUtil.null2Int(count);
		}
		GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(gc_id));
		if (gc != null) {
			if (CommUtil.null2String(temp_count).length() <= 9) {
				if (gc.getId().toString().equals(gc_id)) {
					cart_type = CommUtil.null2String(gc.getCart_type());
					goods = gc.getGoods();
					if (cart_type.equals("0") || cart_type.equals("1") || cart_type.equals("gg")) {
						// 普通商品的处理
						/*
						 * if (cart_type.equals("app") ||
						 * cart_type.equals("web") || cart_type.equals("gg"))
						 * {// 普通商品的处理
						 */ if (goods.getGroup_buy() == 2) {// 团购商品处理
							GroupGoods gg = new GroupGoods();
							for (GroupGoods gg1 : goods.getGroup_goods_list()) {
								if (gg1.getGg_goods().getId().equals(goods.getId())) {
									gg = gg1;
									break;
								}
							}
							if (gg.getGg_count() >= CommUtil.null2Int(temp_count)) {
								gc.setPrice(BigDecimal.valueOf(CommUtil.null2Double(gg.getGg_price())));
								gc_price = CommUtil.mul(gg.getGg_price(), temp_count);
								gc.setCount(CommUtil.null2Int(temp_count));
								this.goodsCartService.update(gc);
							} else {
								if (gg.getGg_count() == 0) {
									gc.setCount(0);
									this.goodsCartService.update(gc);
								}
								code = 3;
							}
						} else if (goods.getActivity_status() == 2) {// 活动商品处理
							if (user != null) {
								gc_price = CommUtil.mul(gc.getPrice(), temp_count);
							}
						} else {
							String gsp = "";
							for (GoodsSpecProperty gs : gc.getGsps()) {
								gsp = gs.getId() + "," + gsp;
							}
							int inventory = goods.getGoods_inventory();
							if (("spec").equals(goods.getInventory_type())) {
								inventory = (int) this.mCartViewTools
										.generic_default_info_color(goods, gsp, gc.getColor()).get("count");
							}
							if (inventory >= CommUtil.null2Int(temp_count)
									&& CommUtil.null2String(temp_count).length() <= 9
									&& gc.getGoods().getGroup_buy() != 2) {
								if (gc.getId().toString().equals(gc_id)) {
									gc.setCount(CommUtil.null2Int(temp_count));
									this.goodsCartService.update(gc);
									gc_price = CommUtil.mul(gc.getPrice(), temp_count);
								}
							} else {
								if (inventory == 0) {
									gc.setCount(0);
									this.goodsCartService.update(gc);
								}
								code = 4206;
								msg = "Goods in short stock";
							}
						}
						/*
						 * if (cart_type.equals("combin") && gc.getCombin_main()
						 * == 1) {// 组合销售的处理 暂时没有该活动 if
						 * (goods.getGoods_inventory() >=
						 * CommUtil.null2Int(count)) {
						 * gc.setCount(CommUtil.null2Int(count));
						 * this.goodsCartService.update(gc); String
						 * suit_all_price = "0.00"; GoodsCart suit = gc; Map
						 * suit_map = (Map)
						 * Json.fromJson(suit.getCombin_suit_info());
						 * suit_map.put("suit_count", CommUtil.null2Int(count));
						 * suit_all_price =
						 * CommUtil.formatMoney(CommUtil.mul(CommUtil.null2Int(
						 * count),
						 * CommUtil.null2Double(suit_map.get("plan_goods_price")
						 * ))); suit_map.put("suit_all_price",
						 * suit_all_price);// 套装整体价格=套装单价*数量 String new_json =
						 * Json.toJson(suit_map, JsonFormat.compact());
						 * suit.setCombin_suit_info(new_json);
						 * suit.setCount(CommUtil.null2Int(count));
						 * this.goodsCartService.update(suit); gc_price =
						 * CommUtil.null2Double(suit_all_price); } else { if
						 * (goods.getGoods_inventory() == 0) { gc.setCount(0);
						 * this.goodsCartService.update(gc); } code = 2; } }
						 */
					}
					// 判断出是否满足满就送条件 暂时没有该活动
					/*
					 * if (gift_id != null) { BuyGift bg =
					 * this.buyGiftService.getObjById(CommUtil.null2Long(gift_id
					 * )); Set<Long> bg_ids = new HashSet<Long>(); if (bg !=
					 * null) { bg_ids.add(bg.getId()); } List<GoodsCart> g_carts
					 * = new ArrayList<GoodsCart>(); if
					 * (CommUtil.null2String(gcs).equals("")) { for (GoodsCart
					 * gCart : carts) { if
					 * (gCart.getGoods().getOrder_enough_give_status() == 1 &&
					 * gCart.getGoods().getBuyGift_id() != null) {
					 * bg_ids.add(gCart.getGoods().getBuyGift_id()); } } g_carts
					 * = carts; } else { String[] gc_ids = gcs.split(","); for
					 * (String g_id : gc_ids) { GoodsCart goodsCart =
					 * this.goodsCartService
					 * .getObjById(CommUtil.null2Long(g_id)); if (goodsCart !=
					 * null &&
					 * goodsCart.getGoods().getOrder_enough_give_status() == 1
					 * && goodsCart.getGoods().getBuyGift_id() != null) {
					 * bg_ids.add(goodsCart.getGoods().getBuyGift_id());
					 * g_carts.add(goodsCart); } } } Map<Long, List<GoodsCart>>
					 * gc_map = new HashMap<Long, List<GoodsCart>>(); for (Long
					 * id : bg_ids) { gc_map.put(id, new
					 * ArrayList<GoodsCart>()); } for (GoodsCart cart : g_carts)
					 * { if (cart.getGoods().getOrder_enough_give_status() == 1
					 * && cart.getGoods().getBuyGift_id() != null) { for
					 * (Map.Entry<Long, List<GoodsCart>> entry :
					 * gc_map.entrySet()) { if
					 * (cart.getGoods().getBuyGift_id().equals(entry.getKey()))
					 * { entry.getValue().add(cart); } } } } List<String>
					 * enough_bg_ids = new ArrayList<String>(); for
					 * (Map.Entry<Long, List<GoodsCart>> entry :
					 * gc_map.entrySet()) { BuyGift buyGift =
					 * this.buyGiftService.getObjById(entry.getKey()); //
					 * 计算出购物车价钱是否满足对应满就送 List<GoodsCart> arrs =
					 * entry.getValue(); BigDecimal bd = new BigDecimal("0.00");
					 * for (GoodsCart arr : arrs) { bd =
					 * bd.add(BigDecimal.valueOf(CommUtil.mul(arr.getPrice(),
					 * arr.getCount()))); } if
					 * (bd.compareTo(buyGift.getCondition_amount()) >= 0) {
					 * enough_bg_ids.add(buyGift.getId().toString()); } }
					 * map.put("bg_ids", enough_bg_ids); }
					 */
				}
			} else {
				code = 4206;
				msg = "Goods in short stock";
			}
			map.put("count", gc.getCount());
		}
		total_price = this.mCartViewTools.calCartPrice(carts, gcs);
		Map price_map = this.mCartViewTools.calEnoughReducePrice(carts, gcs);
		List enoughReduce = this.mCartViewTools.calcActivityPricedifference(gcs);
		map.put("enoughReduce", enoughReduce);
		Map<Long, String> erMap = (Map<Long, String>) price_map.get("erString");
		map.put("gc_price", CommUtil.formatMoney(gc_price));
		map.put("total_price", CommUtil.formatMoney(total_price));
		map.put("enough_reduce_price", CommUtil.formatMoney(price_map.get("reduce")));
		map.put("before", CommUtil.formatMoney(price_map.get("all")));
		for (long k : erMap.keySet()) {
			map.put("erString" + k, erMap.get(k));
		}
		result = new Result(code, msg, map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 购物车商品数量修改
	 * @param request
	 * @param response
	 * @param gc_id
	 * @param count
	 * @param gcs
	 * @param gift_id
	 */
	// @RequestMapping("/goods_count_adjust.json")
	@RequestMapping("v1/goods_count_adjust.json")
	public void goods_count_adjust(HttpServletRequest request, HttpServletResponse response, String gc_id, String count,
			String gcs, String gift_id, String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				List<GoodsCart> carts = this.mCartViewTools.cart_calc(user);
				Map map = new HashMap();
				int code = 0;// 0表示修改成功，2表示库存不足,3表示团购库存不足
				double gc_price = 0.00;// 单位GoodsCart总价钱
				double total_price = 0.00;// 购物车总价钱
				String cart_type = "";// 判断是否为组合销售
				Goods goods = null;
				int temp_count = CommUtil.null2Int(count);
				GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(gc_id));
				if (gc != null) {
					if (CommUtil.null2String(count).length() <= 9) {
						if (gc.getId().toString().equals(gc_id)) {
							cart_type = CommUtil.null2String(gc.getCart_type());
							goods = gc.getGoods();
							if (cart_type.equals("0") || cart_type.equals("1") || cart_type.equals("gg")) {// 普通商品的处理
								// if (cart_type.equals("app") ||
								// cart_type.equals("web") ||
								// cart_type.equals("gg")) {// 普通商品的处理
								if (goods.getGroup_buy() == 2) {// 团购商品处理
									GroupGoods gg = new GroupGoods();
									for (GroupGoods gg1 : goods.getGroup_goods_list()) {
										if (gg1.getGg_goods().getId().equals(goods.getId())) {
											gg = gg1;
											break;
										}
									}
									if (gg.getGg_count() >= CommUtil.null2Int(count)) {
										gc.setPrice(BigDecimal.valueOf(CommUtil.null2Double(gg.getGg_price())));
										gc_price = CommUtil.mul(gg.getGg_price(), count);
										gc.setCount(CommUtil.null2Int(count));
										this.goodsCartService.update(gc);
									} else {
										if (gg.getGg_count() == 0) {
											gc.setCount(0);
											this.goodsCartService.update(gc);
										}
										code = 3;
									}
								} else if (goods.getActivity_status() == 2) {// 活动商品处理
									if (user != null) {
										gc_price = CommUtil.mul(gc.getPrice(), count);
									}
								} else {
									String gsp = "";
									for (GoodsSpecProperty gs : gc.getGsps()) {
										gsp = gs.getId() + "," + gsp;
									}
									int inventory = goods.getGoods_inventory();
									if (("spec").equals(goods.getInventory_type())) {
										Map spec = this.mCartViewTools.generic_default_info_color(goods, gsp,
												gc.getColor());
										inventory = CommUtil.null2Int(spec.get("count"));
									}
									if (inventory >= CommUtil.null2Int(count)
											&& CommUtil.null2String(count).length() <= 9
											&& gc.getGoods().getGroup_buy() != 2) {
										if (gc.getId().toString().equals(gc_id)) {
											gc.setCount(CommUtil.null2Int(count));
											this.goodsCartService.update(gc);
											gc_price = CommUtil.mul(gc.getPrice(), count);
										}
									} else {
										if (inventory == 0) {
											gc.setCount(0);
											this.goodsCartService.update(gc);
										}
										code = 2;
									}
								}
								if (cart_type.equals("combin") && gc.getCombin_main() == 1) {// 组合销售的处理
									if (goods.getGoods_inventory() >= CommUtil.null2Int(count)) {
										gc.setCount(CommUtil.null2Int(count));
										this.goodsCartService.update(gc);
										String suit_all_price = "0.00";
										GoodsCart suit = gc;
										Map suit_map = (Map) Json.fromJson(suit.getCombin_suit_info());
										suit_map.put("suit_count", CommUtil.null2Int(count));
										suit_all_price = CommUtil.formatMoney(CommUtil.mul(CommUtil.null2Int(count),
												CommUtil.null2Double(suit_map.get("plan_goods_price"))));
										suit_map.put("suit_all_price", suit_all_price);// 套装整体价格=套装单价*数量
										String new_json = Json.toJson(suit_map, JsonFormat.compact());
										suit.setCombin_suit_info(new_json);
										suit.setCount(CommUtil.null2Int(count));
										this.goodsCartService.update(suit);
										gc_price = CommUtil.null2Double(suit_all_price);
									} else {
										if (goods.getGoods_inventory() == 0) {
											gc.setCount(0);
											this.goodsCartService.update(gc);
										}
										code = 2;
									}
								}
							}
							// 判断出是否满足满就送条件 暂时没有该活动
							/*
							 * if (gift_id != null) { BuyGift bg =
							 * this.buyGiftService.getObjById(CommUtil.null2Long
							 * (gift_id)); Set<Long> bg_ids = new
							 * HashSet<Long>(); if (bg != null) {
							 * bg_ids.add(bg.getId()); } List<GoodsCart> g_carts
							 * = new ArrayList<GoodsCart>(); if
							 * (CommUtil.null2String(gcs).equals("")) { for
							 * (GoodsCart gCart : carts) { if
							 * (gCart.getGoods().getOrder_enough_give_status()
							 * == 1 && gCart.getGoods().getBuyGift_id() != null)
							 * { bg_ids.add(gCart.getGoods().getBuyGift_id()); }
							 * } g_carts = carts; } else { String[] gc_ids =
							 * gcs.split(","); for (String g_id : gc_ids) {
							 * GoodsCart goodsCart = this.goodsCartService
							 * .getObjById(CommUtil.null2Long(g_id)); if
							 * (goodsCart != null &&
							 * goodsCart.getGoods().getOrder_enough_give_status(
							 * ) == 1 && goodsCart.getGoods().getBuyGift_id() !=
							 * null) {
							 * bg_ids.add(goodsCart.getGoods().getBuyGift_id());
							 * g_carts.add(goodsCart); } } } Map<Long,
							 * List<GoodsCart>> gc_map = new HashMap<Long,
							 * List<GoodsCart>>(); for (Long id : bg_ids) {
							 * gc_map.put(id, new ArrayList<GoodsCart>()); } for
							 * (GoodsCart cart : g_carts) { if
							 * (cart.getGoods().getOrder_enough_give_status() ==
							 * 1 && cart.getGoods().getBuyGift_id() != null) {
							 * for (Map.Entry<Long, List<GoodsCart>> entry :
							 * gc_map.entrySet()) { if
							 * (cart.getGoods().getBuyGift_id().equals(entry.
							 * getKey())) { entry.getValue().add(cart); } } } }
							 * List<String> enough_bg_ids = new
							 * ArrayList<String>(); for (Map.Entry<Long,
							 * List<GoodsCart>> entry : gc_map.entrySet()) {
							 * BuyGift buyGift =
							 * this.buyGiftService.getObjById(entry.getKey());
							 * // 计算出购物车价钱是否满足对应满就送 List<GoodsCart> arrs =
							 * entry.getValue(); BigDecimal bd = new
							 * BigDecimal("0.00"); for (GoodsCart arr : arrs) {
							 * bd = bd.add(BigDecimal.valueOf(CommUtil.mul(arr.
							 * getPrice(), arr.getCount()))); } if
							 * (bd.compareTo(buyGift.getCondition_amount()) >=
							 * 0) {
							 * enough_bg_ids.add(buyGift.getId().toString()); }
							 * } map.put("bg_ids", enough_bg_ids); }
							 */
						}

					} else {
						code = 2;
					}
					map.put("count", gc.getCount());
				}
				total_price = this.mCartViewTools.calCartPrice(carts, gcs);
				Map price_map = this.mCartViewTools.calEnoughReducePrice(carts, gcs);
				Map<Long, String> erMap = (Map<Long, String>) price_map.get("erString");
				map.put("gc_price", CommUtil.formatMoney(gc_price));
				map.put("total_price", CommUtil.formatMoney(total_price));
				map.put("enough_reduce_price", CommUtil.formatMoney(price_map.get("reduce")));
				map.put("before", CommUtil.formatMoney(price_map.get("all")));
				for (long k : erMap.keySet()) {
					map.put("erString" + k, erMap.get(k));
				}
				result = new Result(code, "success", map);
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 购物确认,填写用户地址，配送方式，支付方式等
	 * 
	 * @param request
	 * @param response
	 * @param gcs
	 * @param giftids
	 * @return
	 */
	@RequestMapping("v1/goods_cart2.json")
	public void goods_cart2(HttpServletRequest request, HttpServletResponse response, String gcs, String giftids,
			String token, String language) {
		Result result = null;
		Map cartMap = new HashMap();
		ModelAndView mv = new JModelAndView("goods_cart2.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				if (CommUtil.null2String(gcs).equals("")) {
					result = new Result(1, "请选择商品");
				} else {
					// 去重复商品（相同商品不同规格不去掉）
					List<GoodsCart> carts = this.mCartViewTools.cart_calc(user);
					// 判断当前用户
					boolean flag = true;
					if (carts.size() > 0) {
						for (GoodsCart gc : carts) {
							if (!gc.getUser().getId().equals(user.getId())) {
								flag = false;
								break;
							}
						}
					}
					boolean goods_cod = true;// 默认支持货到付款，这样有一款产品不支持货到付款，这个订单就不支持货到付款
					int tax_invoice = 1;// 默认可以开具增值税发票，只要存在一款产品不支持增值税发票，整体订单不可以开具增值税发票
					if (flag && carts.size() > 0) {
						params.clear();
						params.put("user_id", user.getId());
						params.put("defaulr_val", 1);
						List<Address> addressList = this.addressService.query(
								"select obj from Address obj where obj.user.id=:user_id and obj.default_val=:defaulr_val",
								params, -1, -1);
						if (addressList.size() > 0) {
							List<Map> addresses = this.mCartViewTools.queryAddress(addressList);
							String area_abbr = "";
							String area_id = "";
							for (Map map : addresses) {
								area_abbr = (String) map.get("area_abbr");
								area_id = CommUtil.null2String(map.get("area_id"));
							}
							cartMap.put("address", addresses);
							String cart_session = CommUtil.randomString(32);
							request.getSession(false).setAttribute("cart_session", cart_session);
							Date date = new Date();
							Map erpMap = this.mCartViewTools.calEnoughReducePrice(carts, gcs);// 获取满减商品总价格与满减优惠价格
							cartMap.put("cart_session", cart_session);
							cartMap.put("order_goods_price", erpMap.get("all"));
							cartMap.put("order_er_price", erpMap.get("reduce"));

							List map_list = new ArrayList();
							List<Object> store_list = new ArrayList<Object>();
							for (GoodsCart gc : carts) {
								if (gc.getGoods().getGoods_type() == 1) {
									if (gc.getGoods().getGoods_store().getStore_status() == 15) {
										store_list.add(gc.getGoods().getGoods_store().getId());
									}
								} else {
									store_list.add("self");
								}
							}
							HashSet hs = new HashSet(store_list);
							store_list.removeAll(store_list);
							store_list.addAll(hs);
							String[] gc_ids = CommUtil.null2String(gcs).split(",");
							List<Goods> ac_goodses = new ArrayList<Goods>();
							if (giftids != null && !giftids.equals("")) {
								String[] gift_ids = giftids.split(",");
								for (String gift_id : gift_ids) {
									Goods goods = this.goodsService.getObjById(CommUtil.null2Long(gift_id));
									if (goods != null) {
										ac_goodses.add(goods);
									}
								}
							}
							boolean ret = false;
							if (ac_goodses.size() > 0) {
								ret = true;
							}

							for (Object sl : store_list) {
								if (sl != "self" && !sl.equals("self")) {// 商家商品
									List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
									List<GoodsCart> amount_gc_list = new ArrayList<GoodsCart>();
									Map<Goods, List<GoodsCart>> gift_map = new HashMap<Goods, List<GoodsCart>>();
									Map<Long, List<GoodsCart>> ermap = new HashMap<Long, List<GoodsCart>>();
									Map erString = new HashMap();
									for (Goods g : ac_goodses) {
										if (g.getGoods_type() == 1
												&& g.getGoods_store().getId().toString().equals(sl.toString())) {
											gift_map.put(g, new ArrayList<GoodsCart>());
										}
									}
									for (GoodsCart gc : carts) {
										for (String gc_id : gc_ids) {
											if (!CommUtil.null2String(gc_id).equals("")
													&& CommUtil.null2Long(gc_id).equals(gc.getId())) {
												if (gc.getGoods().getGoods_store() != null) {
													if (gc.getGoods().getGoods_store().getId().equals(sl)) {
														if (ret && gift_map.size() > 0
																&& gc.getGoods().getOrder_enough_give_status() == 1
																&& gc.getGoods().getBuyGift_id() != null) {
															BuyGift bg = this.buyGiftService
																	.getObjById(gc.getGoods().getBuyGift_id());
															if (bg.getBeginTime().before(date)) {
																for (Map.Entry<Goods, List<GoodsCart>> entry : gift_map
																		.entrySet()) {
																	if (entry.getKey().getBuyGift_id()
																			.equals(gc.getGoods().getBuyGift_id())) {
																		entry.getValue().add(gc);
																	} else {
																		gc_list.add(gc);
																	}
																}
															} else {
																gc_list.add(gc);
															}
														} else if (gc.getGoods().getEnough_reduce() == 1) {

															String er_id = gc.getGoods().getOrder_enough_reduce_id();
															EnoughReduce er = this.enoughReduceService
																	.getObjById(CommUtil.null2Long(er_id));
															if (er.getErstatus() == 10
																	&& er.getErbegin_time().before(date)) {
																if (ermap.containsKey(er.getId())) {
																	ermap.get(er.getId()).add(gc);
																} else {
																	List<GoodsCart> list = new ArrayList<GoodsCart>();
																	list.add(gc);
																	ermap.put(er.getId(), list);
																	Map map = (Map) Json.fromJson(er.getEr_json());
																	double k = 0;
																	String str = "";
																	for (Object key : map.keySet()) {
																		if (k == 0) {
																			k = Double.parseDouble(key.toString());
																			str = "The activity product buy " + k
																					+ " AED, you can enjoy the discount";
																		}
																		if (Double.parseDouble(key.toString()) < k) {
																			k = Double.parseDouble(key.toString());
																			str = "The activity product buy " + k
																					+ " AED, you can enjoy the discount";
																		}
																	}

																	erString.put(er.getId(), str);
																	erString.put("er_json", map);
																}
															} else {
																gc_list.add(gc);
															}

														} else {
															gc_list.add(gc);
														}
														amount_gc_list.add(gc);
													}
												}
											}
										}
									}
									if ((gc_list != null && gc_list.size() > 0)
											|| (gift_map != null && gift_map.size() > 0)
											|| (ermap != null && ermap.size() > 0)) {
										Map map = new HashMap();
										Map ergcMap = this.mCartViewTools.calEnoughReducePrice(amount_gc_list, gcs);// 满减相关信息
										if (gift_map.size() > 0) {
											map.put("ac_goods", gift_map);
										}
										if (ermap.size() > 0) {
											map.put("er_goods", ermap);
											map.put("erString", ergcMap.get("erString"));
											map.put("er_json", ergcMap.get("er_info"));
										}
										map.put("store_id", sl);
										map.put("store", this.storeService.getObjById(CommUtil.null2Long(sl)));
										map.put("store_goods_price",
												this.mCartViewTools.calCartPrice(amount_gc_list, gcs));
										map.put("store_enough_reduce", ergcMap.get("reduce"));
										map.put("gc_list", gc_list);
										map_list.add(map);
									}
									for (GoodsCart gc : gc_list) {
										if (gc.getGoods().getGoods_cod() == -1
												|| gc.getGoods().getGoods_choice_type() == 1) {// 只要存在一件不允许使用货到付款购买的商品整个订单就不允许使用货到付款
											goods_cod = false;
										}
										if (gc.getGoods().getTax_invoice() == 0) {// 只要存在一件不支持开具增值税发票的商品，整个订单就不允许开具增值税发票
											tax_invoice = 0;
										}
									}
								} else {// 保留自营商品计算
									List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
									List<GoodsCart> amount_gc_list = new ArrayList<GoodsCart>();
									Map<Goods, List<GoodsCart>> gift_map = new HashMap<Goods, List<GoodsCart>>();
									Map<Long, List<GoodsCart>> ermap = new HashMap<Long, List<GoodsCart>>();
									Map<Long, String> erString = new HashMap<Long, String>();
									for (Goods g : ac_goodses) {
										if (g.getGoods_type() == 0) {
											gift_map.put(g, new ArrayList<GoodsCart>());
										}
									}
									for (GoodsCart gc : carts) {
										for (String gc_id : gc_ids) {
											if (!CommUtil.null2String(gc_id).equals("")
													&& CommUtil.null2Long(gc_id).equals(gc.getId())) {
												if (gc.getGoods().getGoods_store() == null) {
													if (ret && gift_map.size() > 0
															&& gc.getGoods().getOrder_enough_give_status() == 1
															&& gc.getGoods().getBuyGift_id() != null) {
														BuyGift bg = this.buyGiftService
																.getObjById(gc.getGoods().getBuyGift_id());
														if (bg.getBeginTime().before(date)) {
															for (Map.Entry<Goods, List<GoodsCart>> entry : gift_map
																	.entrySet()) {
																if (entry.getKey().getBuyGift_id()
																		.equals(gc.getGoods().getBuyGift_id())) {
																	entry.getValue().add(gc);
																} else {
																	gc_list.add(gc);
																}
															}
														} else {
															gc_list.add(gc);
														}
													} else if (gc.getGoods().getEnough_reduce() == 1) {

														String er_id = gc.getGoods().getOrder_enough_reduce_id();
														EnoughReduce er = this.enoughReduceService
																.getObjById(CommUtil.null2Long(er_id));
														if (er.getErbegin_time().before(date)) {
															if (ermap.containsKey(er.getId())) {
																ermap.get(er.getId()).add(gc);
															} else {
																List<GoodsCart> list = new ArrayList<GoodsCart>();
																list.add(gc);
																ermap.put(er.getId(), list);
																Map map = (Map) Json.fromJson(er.getEr_json());
																double k = 0;
																String str = "";
																for (Object key : map.keySet()) {
																	if (k == 0) {
																		k = Double.parseDouble(key.toString());
																		str = "The activity product buy " + k
																				+ " AED, you can enjoy the discount";
																	}
																	if (Double.parseDouble(key.toString()) < k) {
																		k = Double.parseDouble(key.toString());
																		str = "The activity product buy " + k
																				+ " AED, you can enjoy the discount";
																	}
																}

																erString.put(er.getId(), str);
															}
														} else {
															gc_list.add(gc);
														}

													} else {
														gc_list.add(gc);
													}
													amount_gc_list.add(gc);
												}
											}
										}
									}
									if ((gc_list != null && gc_list.size() > 0)
											|| (gift_map != null && gift_map.size() > 0)
											|| (ermap != null && ermap.size() > 0)) {
										Map map = new HashMap();
										Map ergcMap = this.mCartViewTools.calEnoughReducePrice(amount_gc_list, gcs);// 满减相关信息
										if (gift_map.size() > 0) {
											map.put("ac_goods", gift_map);
										}
										if (ermap.size() > 0) {
											map.put("er_goods", ermap);
											map.put("erString", ergcMap.get("erString"));
											map.put("er_json", ergcMap.get("er_info"));
										}
										map.put("store_id", sl);
										map.put("store_goods_price",
												this.mCartViewTools.calCartPrice(amount_gc_list, gcs));
										map.put("store_enough_reduce", ergcMap.get("reduce"));
										map.put("gc_list", gc_list);
										map_list.add(map);
									}
									for (GoodsCart gc : gc_list) {
										if (gc.getGoods().getGoods_cod() == -1
												|| gc.getGoods().getGoods_choice_type() == 1) {// 只要存在一件不允许使用货到付款购买的商品整个订单就不允许使用货到付款
											goods_cod = false;
										}
										if (gc.getGoods().getTax_invoice() == 0) {// 只要存在一件不支持开具增值税发票的商品，整个订单就不允许开具增值税发票
											tax_invoice = 0;
										}
									}
								}
							}
							// [用户信息 -- 订单结算页展示商品对应的店铺信息]
							this.mCartViewTools.userInfo(user.getId(), cartMap);
							// 店铺商品根据店铺展示所有购物车
							this.mCartViewTools.goods_cart2(map_list, area_abbr, area_id,
									CommUtil.null2String(user.getId()), cartMap, language);
							cartMap.put("gcs", gcs);
							result = new Result(0, "success", cartMap);
						} else {
							result = new Result(2, "添加地址");
						}
					} else {
						result = new Result(3, "购物车信息为空");
					}
				}
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	private String generic_day(int day) {
		String[] list = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
		return list[day - 1];
	}

	// flag = del
	@RequestMapping("v1/order_address.json")
	public void order_address(HttpServletRequest request, HttpServletResponse response, String gcs, String addr_id,
			String store_ids) {
		Map handovermap = new HashMap();
		Result result = null;
		Store store = null;
		double store_total_price = 0;
		String[] gcs_id = gcs.split(",");
		List<GoodsCart> carts = new ArrayList<GoodsCart>();
		List<Map> sms_list = new ArrayList<Map>();
		Object order_goods_price = 0;
		for (String id : gcs_id) {
			GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(id));
			if (gc != null) {
				carts.add(gc);
			}
		}
		String[] ids = store_ids.split(",");
		List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
		List<GoodsCart> amount_gc_list = new ArrayList<GoodsCart>();
		for (GoodsCart gc : carts) {
			gc_list = new ArrayList<GoodsCart>();
			amount_gc_list = new ArrayList<GoodsCart>();
			Map sms_map = new HashMap();
			String store_id = null;
			for (String id : ids) {
				if (id != "self" && !id.equals("self")) {
					if (gc.getGoods().getGoods_type() == 1
							&& gc.getGoods().getGoods_store().getId().equals(CommUtil.null2Long(id))) {

						gc_list.add(gc);
						sms_map.put("store_id", id);
						store_id = id;
						amount_gc_list.add(gc);
					}
				} else {
					if (gc.getGoods().getGoods_type() == 0) {
						gc_list.add(gc);
					}
				}
			}
			if (store_id != null) {
				store = this.storeService.getObjById(CommUtil.null2Long(store_id));
			}

			Address addr = this.addressService.getObjById(CommUtil.null2Long(addr_id));
			double store_goods_price = this.mCartViewTools.calCartPrice(amount_gc_list, gcs);
			if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(), store_goods_price) < 0) {
				store_total_price = store_goods_price;
				order_goods_price = CommUtil.add(order_goods_price, store_total_price);
			} else {
				List<SysMap> sms = this.transportTools.query_cart_trans_goods_cart3(gc_list,
						CommUtil.null2String(addr.getArea().getId()));
				Object price = 0;
				Object ship_price = 0;
				for (SysMap obj : sms) {
					if (obj.getKey().equals("Express")) {
						price = obj.getValue();
					} else {
						price = 0;
					}
					ship_price = CommUtil.add(ship_price, price);
				}
				store_total_price = CommUtil.add(store_goods_price, ship_price);
				order_goods_price = CommUtil.add(order_goods_price, store_total_price);
				sms_map.put("ship_price", ship_price);
			}
			sms_map.put("store_total_price", store_total_price);
			if (store.getTransport() != null && store.getTransport().getExpress_company() != null) {
				if (store.getTransport().getExpress_company().getEnabled() == 1) {
					sms_map.put("flag", mCartViewTools.matching(store, addr.getArea().getParent().getAbbr()));
				}
			}
			sms_list.add(sms_map);
			handovermap.put("store", sms_list);
		}
		handovermap.put("order_goods_price", order_goods_price);
		result = new Result(0, "success", handovermap);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param token
	 * @param gcs
	 * @param gc_id
	 * @param count
	 * @param address_id
	 * @param storeCouponId
	 * @param generalCouponId
	 * @param cart_session
	 * @param language
	 * @descript 购物车数量修改
	 */
	@RequestMapping("v1/adjust.json")
	public void orderCountAdjust(HttpServletRequest request, HttpServletResponse response, String token, String gcs,
			String gc_id, String count, String address_id, String storeCouponId, String generalCouponId,
			String cart_session, String language) {
		Result result = null;
		String msg = "";
		Map cartMap = new HashMap();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				int code = 0;// 0表示修改成功，2表示库存不足,3表示团购库存不足
				double gc_price = 0;// 购物车商品价格
				double store_total_price;// 店铺总价
				double order_total_price;// 订单总价
				int goods_inventory = 0;
				int oversea_inventory = 0;
				int temp_count = CommUtil.null2Int(count);
				GoodsCart goodsCart = this.goodsCartService.getObjById(CommUtil.null2Long(gc_id));
				Goods obj = goodsCart.getGoods();
				Map goodsMap = new HashMap();// 记录当前修改商品信息
				Map erMap = new HashMap();// 记录满减信息
				if (goodsCart != null) {
					if (CommUtil.null2String(count).length() <= 9) {
						double all_price = 0;// 商品总价
						String gsp = "";
						// int inventory = obj.getGoods_inventory();
						if (obj.getInventory_type().equals("spec")) {
							for (GoodsSpecProperty gs : goodsCart.getGsps()) {
								gsp = gs.getId() + ",";
							}
						}
						// 判断优先使用海外仓
						if (obj.getInventory_type().equals("all")) {
							goods_inventory = obj.getGoods_inventory();
							oversea_inventory = obj.getOversea_inventory();
						} else {
							Map<String, Object> goods = this.mCartViewTools.generic_default_info_color(obj, gsp,
									goodsCart.getColor());
							goods_inventory = CommUtil.null2Int(goods.get("count"));
							oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory"));
						}
						if (CommUtil.subtract(goods_inventory, count) > 0
								|| CommUtil.subtract(oversea_inventory, count) > 0
										&& CommUtil.null2String(count).length() <= 9) {
							Date date = new Date();
							gc_price = CommUtil.mul(count, goodsCart.getPrice());
							goodsCart.setCount(CommUtil.null2Int(count));
							this.goodsCartService.update(goodsCart);
							if (obj.getEnough_reduce() == 1) {// 满减商品
								String er_id = obj.getOrder_enough_reduce_id();
								EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
								if (er.getErstatus() == 10 && er.getErbegin_time().before(date)) {
									String er_ids = er.getErgoods_ids_json();
									String er_json = er.getEr_json();
									double er_price = 0;// 满减价格
									String er_str = "";
									Map json_map = (Map) Json.fromJson(er_json);
									for (Object key : json_map.keySet()) {
										if (gc_price > CommUtil.null2Double(key)) {
											er_price = CommUtil.null2Double(json_map.get(key));
											er_str = "Full AED " + key + " minus " + er_price;
											gc_price = CommUtil.subtract(gc_price, er_price);
											erMap.put("er_price", er_price);
											cartMap.put("enoughReduce", erMap);
										}
									}
								}
							} else {
								if (goods_inventory == 0) {
									goodsCart.setCount(0);
									this.goodsCartService.update(goodsCart);
								}
							}
							cartMap.put("gc_price", gc_price);

							Address address = this.addressService.getObjById(CommUtil.null2Long(address_id));
							if (address != null) {
								Area area = address.getArea();
								String area_id = "";
								String area_abbr = "";
								if (area != null) {
									area_id = CommUtil.null2String(area.getId());
									area_abbr = area.getParent().getAbbr();
								}
								boolean goods_cod = true;// 默认支持货到付款，这样有一款产品不支持货到付款，这个订单就不支持货到付款
								if (gcs != null && !gcs.equals("")) {
									String cart_session1 = (String) request.getSession(false)
											.getAttribute("cart_session");
									if (cart_session.equals(cart_session1)) {
										List<GoodsCart> carts = this.goodsCartService.query(
												"select obj from GoodsCart obj where id in(" + gcs + ")", null, -1, -1);
										Map erpMap = this.mCartViewTools.calEnoughReducePrice(carts, gcs);// 获取满减商品总价格与满减优惠价格
										cartMap.put("cart_session", cart_session);
										cartMap.put("order_goods_price", erpMap.get("all"));
										cartMap.put("order_er_price", erpMap.get("reduce"));
										List<Object> store_list = new ArrayList<Object>();
										List map_list = new ArrayList();
										for (GoodsCart goodsCart1 : carts) {
											if (goodsCart1.getGoods().getGoods_type() == 1) {
												store_list.add(goodsCart1.getGoods().getGoods_store().getId());
											}
										}
										HashSet hs = new HashSet(store_list);
										store_list.removeAll(store_list);
										store_list.addAll(hs);
										String[] gc_ids = CommUtil.null2String(gcs).split(",");
										List<Goods> ac_goodses = new ArrayList<Goods>();
										boolean ret = false;
										if (ac_goodses.size() > 0) {
											ret = true;
										}
										for (Object sl : store_list) {
											if (sl != "self" && !sl.equals("self")) {// 商家商品
												List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
												List<GoodsCart> amount_gc_list = new ArrayList<GoodsCart>();
												Map<Goods, List<GoodsCart>> gift_map = new HashMap<Goods, List<GoodsCart>>();
												Map<Long, List<GoodsCart>> ermap = new HashMap<Long, List<GoodsCart>>();
												Map<Long, String> erString = new HashMap<Long, String>();
												for (Goods g : ac_goodses) {
													if (g.getGoods_type() == 1 && g.getGoods_store().getId().toString()
															.equals(sl.toString())) {
														gift_map.put(g, new ArrayList<GoodsCart>());
													}
												}
												for (GoodsCart gc : carts) {
													for (String gcid : gc_ids) {
														if (!CommUtil.null2String(gcid).equals("")
																&& CommUtil.null2Long(gcid).equals(gc.getId())) {
															if (gc.getGoods().getGoods_store() != null) {
																if (gc.getGoods().getGoods_store().getId().equals(sl)) {
																	if (ret && gift_map.size() > 0
																			&& gc.getGoods()
																					.getOrder_enough_give_status() == 1
																			&& gc.getGoods().getBuyGift_id() != null) {
																		BuyGift bg = this.buyGiftService.getObjById(
																				gc.getGoods().getBuyGift_id());
																		if (bg.getBeginTime().before(date)) {
																			for (Map.Entry<Goods, List<GoodsCart>> entry : gift_map
																					.entrySet()) {
																				if (entry.getKey().getBuyGift_id()
																						.equals(gc.getGoods()
																								.getBuyGift_id())) {
																					entry.getValue().add(gc);
																				} else {
																					gc_list.add(gc);
																				}
																			}
																		} else {
																			gc_list.add(gc);
																		}
																	} else if (gc.getGoods().getEnough_reduce() == 1) {

																		String er_id = gc.getGoods()
																				.getOrder_enough_reduce_id();
																		EnoughReduce er = this.enoughReduceService
																				.getObjById(CommUtil.null2Long(er_id));
																		if (er.getErbegin_time().before(date)) {
																			if (ermap.containsKey(er.getId())) {
																				ermap.get(er.getId()).add(gc);
																			} else {
																				List<GoodsCart> list = new ArrayList<GoodsCart>();
																				list.add(gc);
																				ermap.put(er.getId(), list);
																				Map map = (Map) Json
																						.fromJson(er.getEr_json());
																				double k = 0;
																				String str = "";
																				for (Object key : map.keySet()) {
																					if (k == 0) {
																						k = Double.parseDouble(
																								key.toString());
																						str = "The activity product buy "
																								+ k
																								+ " AED, you can enjoy the discount";
																					}
																					if (Double.parseDouble(
																							key.toString()) < k) {
																						k = Double.parseDouble(
																								key.toString());
																						str = "The activity product buy "
																								+ k
																								+ " AED, you can enjoy the discount";
																					}
																				}

																				erString.put(er.getId(), str);
																			}
																		} else {
																			gc_list.add(gc);
																		}

																	} else {
																		gc_list.add(gc);
																	}
																	amount_gc_list.add(gc);
																}
															}
														}
													}
												}
												if ((gc_list != null && gc_list.size() > 0)
														|| (gift_map != null && gift_map.size() > 0)
														|| (ermap != null && ermap.size() > 0)) {
													Map map = new HashMap();
													Map ergcMap = this.mCartViewTools
															.calEnoughReducePrice(amount_gc_list, gcs);// 满减相关信息
													if (gift_map.size() > 0) {
														map.put("ac_goods", gift_map);
													}
													if (ermap.size() > 0) {
														map.put("er_goods", ermap);
														map.put("erString", ergcMap.get("erString"));
														map.put("er_json", ergcMap.get("er_info"));

													}
													// 调整参数格式
													// String coupon_id = "";
													String coupon_id = this.mCartViewTools.coupon(storeCouponId,
															CommUtil.null2String(sl));// 获取店铺对应优惠券（弃用）
													/*
													 * if(storeCouponId != null
													 * &&
													 * !storeCouponId.equals("")
													 * ){ Map storeCoupon =
													 * Json.fromJson(Map.class,
													 * storeCouponId); coupon_id
													 * = storeCoupon.get(sl.
													 * toString()).toString(); }
													 */
													map.put("coupon_id", coupon_id);
													map.put("store_id", sl);
													map.put("store",
															this.storeService.getObjById(CommUtil.null2Long(sl)));
													map.put("store_goods_price",
															this.mCartViewTools.calCartPrice(amount_gc_list, gcs));
													map.put("store_enough_reduce", ergcMap.get("reduce"));
													map.put("gc_list", gc_list);
													map_list.add(map);
												}
												for (GoodsCart gc : gc_list) {
													if (gc.getGoods().getGoods_cod() == -1
															|| gc.getGoods().getGoods_choice_type() == 1) {// 只要存在一件不允许使用货到付款购买的商品整个订单就不允许使用货到付款
														goods_cod = false;
													}
												}
											} else {// 自营商品
												List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
												List<GoodsCart> amount_gc_list = new ArrayList<GoodsCart>();
												Map<Goods, List<GoodsCart>> gift_map = new HashMap<Goods, List<GoodsCart>>();
												Map<Long, List<GoodsCart>> ermap = new HashMap<Long, List<GoodsCart>>();
												Map<Long, String> erString = new HashMap<Long, String>();
												for (Goods g : ac_goodses) {
													if (g.getGoods_type() == 0) {
														gift_map.put(g, new ArrayList<GoodsCart>());
													}
												}
												for (GoodsCart gc : carts) {
													for (String gcid : gc_ids) {
														if (!CommUtil.null2String(gcid).equals("")
																&& CommUtil.null2Long(gcid).equals(gc.getId())) {
															if (gc.getGoods().getGoods_store() == null) {
																if (ret && gift_map.size() > 0
																		&& gc.getGoods()
																				.getOrder_enough_give_status() == 1
																		&& gc.getGoods().getBuyGift_id() != null) {
																	BuyGift bg = this.buyGiftService
																			.getObjById(gc.getGoods().getBuyGift_id());
																	if (bg.getBeginTime().before(date)) {
																		for (Map.Entry<Goods, List<GoodsCart>> entry : gift_map
																				.entrySet()) {
																			if (entry.getKey().getBuyGift_id().equals(
																					gc.getGoods().getBuyGift_id())) {
																				entry.getValue().add(gc);
																			} else {
																				gc_list.add(gc);
																			}
																		}
																	} else {
																		gc_list.add(gc);
																	}
																} else if (gc.getGoods().getEnough_reduce() == 1) {

																	String er_id = gc.getGoods()
																			.getOrder_enough_reduce_id();
																	EnoughReduce er = this.enoughReduceService
																			.getObjById(CommUtil.null2Long(er_id));
																	if (er.getErbegin_time().before(date)) {
																		if (ermap.containsKey(er.getId())) {
																			ermap.get(er.getId()).add(gc);
																		} else {
																			List<GoodsCart> list = new ArrayList<GoodsCart>();
																			list.add(gc);
																			ermap.put(er.getId(), list);
																			Map map = (Map) Json
																					.fromJson(er.getEr_json());
																			double k = 0;
																			String str = "";
																			for (Object key : map.keySet()) {
																				if (k == 0) {
																					k = Double.parseDouble(
																							key.toString());
																					str = "The activity product buy "
																							+ k
																							+ " AED, you can enjoy the discount";
																				}
																				if (Double.parseDouble(
																						key.toString()) < k) {
																					k = Double.parseDouble(
																							key.toString());
																					str = "The activity product buy "
																							+ k
																							+ " AED, you can enjoy the discount";
																				}
																			}

																			erString.put(er.getId(), str);
																		}
																	} else {
																		gc_list.add(gc);
																	}

																} else {
																	gc_list.add(gc);
																}
																amount_gc_list.add(gc);
															}
														}
													}
												}
												if ((gc_list != null && gc_list.size() > 0)
														|| (gift_map != null && gift_map.size() > 0)
														|| (ermap != null && ermap.size() > 0)) {
													Map map = new HashMap();
													Map ergcMap = this.mCartViewTools
															.calEnoughReducePrice(amount_gc_list, gcs);// 满减相关信息
													if (gift_map.size() > 0) {
														map.put("ac_goods", gift_map);
													}
													if (ermap.size() > 0) {
														map.put("er_goods", ermap);
														map.put("erString", ergcMap.get("erString"));
														map.put("er_json", ergcMap.get("er_info"));
													}
													map.put("store_id", sl);
													map.put("store_goods_price",
															this.mCartViewTools.calCartPrice(amount_gc_list, gcs));
													map.put("store_enough_reduce", ergcMap.get("reduce"));
													map.put("gc_list", gc_list);
													map_list.add(map);
												}
												for (GoodsCart gc : gc_list) {
													if (gc.getGoods().getGoods_cod() == -1
															|| gc.getGoods().getGoods_choice_type() == 1) {// 只要存在一件不允许使用货到付款购买的商品整个订单就不允许使用货到付款
														goods_cod = false;
													}
												}
											}
										}
										// 计算订单价格
										this.mCartViewTools.adjust(map_list, area_abbr, area_id,
												CommUtil.null2String(user.getId()), cartMap, generalCouponId, language);
									} else {
										msg = "Order payment timeout";
									}
								} else {
									msg = "Please select the product";
								}
							} else {
								msg = "The shipping address is empty";
							}
						} else {
							msg = "Insufficient stock";
						}
					} else {
						msg = "Insufficient stock";
					}
				} else {
					msg = "The commodity does not exist";
				}
				result = new Result(0, msg, cartMap);
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	@SecurityMapping(title = "完成订单提交进入支付", value = "/goods_cart3.htm*", rtype = "buyer", rname = "购物流程3", rcode = "goods_cart", rgroup = "在线购物")
	@RequestMapping(value = "v1/goods_cart3.json", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String cartPayNow(HttpServletRequest request, HttpServletResponse response, String cart_session,
			String store_id, String addr_id, String gcs, String delivery_time, String delivery_type, String delivery_id,
			String payType, String gifts, String mobile, String mobile_verify_code, String generalCouponId,
			String storeCouponId, String order_type, String token) {
		Result result = null;
		Map goods_cart3_map = new HashMap();
		ModelAndView mv = new JModelAndView("goods_cart3.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if (CommUtil.null2String(token).equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				// 暂时取消下单短信验证
				// Map mobilemap = new HashMap();
				// String userMobile = mobile;
				// String areaCode = userMobile.substring(0, 3);
				// boolean flagMobile = this.mobileTools.verify(mobile);
				// if(flagMobile){
				// mobilemap = this.mobileTools.mobile(mobile);
				// userMobile = (String) mobilemap.get("userMobile");
				// }

				// if("971".equals(areaCode)){
				// mobilemap = this.mobileTools.mobile(mobile);
				// userMobile = (String) mobilemap.get("userMobile");
				// }
				// 根据手机号获取验证码
				// VerifyCode mvc =
				// this.mobileverifycodeService.getObjByProperty(null,
				// "mobile", userMobile);
				// if (mvc != null &&
				// mvc.getCode().equalsIgnoreCase(mobile_verify_code)) {
				// this.mobileverifycodeService.delete(mvc.getId());

				String cart_session1 = (String) request.getSession(false).getAttribute("cart_session");
				if (CommUtil.null2String(cart_session1).equals(cart_session)
						&& !CommUtil.null2String(store_id).equals("")) {
					List<GoodsCart> order_carts = new ArrayList<GoodsCart>();
					Address address = this.addressService.getObjById(CommUtil.null2Long(addr_id));
					Date date = new Date();
					String[] gc_ids = gcs.split(",");
					String[] gift_ids = gifts.split(",");
					List<Goods> gift_goods = new ArrayList<Goods>();
					for (String gid : gift_ids) {
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(gid));
						if (goods != null) {
							BuyGift bg = this.buyGiftService.getObjById(CommUtil.null2Long(goods.getBuyGift_id()));
							if (bg != null && bg.getBeginTime().before(date)) {
								gift_goods.add(goods);
							}
						}
					}
					for (String gc_id : gc_ids) {
						if (!gc_id.equals("")) {
							GoodsCart car = this.goodsCartService.getObjById(CommUtil.null2Long(gc_id));
							if (car != null) {
								order_carts.add(car);
							}
						}
					}
					for (String gc_id : gc_ids) {
						GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(gc_id));
						if (gc != null && gc.getGoods().getGoods_cod() == -1) {// 只要存在一件不允许使用货到付款购买的商品整个订单就不允许使用货到付款
							if (!payType.equals("online")) {// 订单不支持货到付款，用户从页面前端恶意篡改支付方式为货到付款
								result = new Result(1, "您恶意修改付款方式，订单已过期");
								return Json.toJson(result, JsonFormat.compact());
							}
						}
					}
					if (order_carts.size() > 0 && address != null) {
						// 验证购物车中是否存在库存为0的商品
						boolean inventory_very = true;
						for (GoodsCart gc : order_carts) {
							if (gc.getCount() == 0) {
								inventory_very = false;
							}
							// int goods_inventory =
							// CommUtil.null2Int(this.generic_default_info(gc.getGoods(),
							// gc.getCart_gsp(), user).get("count"));// 计算商品库存信息
							int goods_inventory = CommUtil.null2Int(this.mCartViewTools
									.generic_default_info_color(gc.getGoods(), gc.getCart_gsp(), gc.getColor())
									.get("count"));// 计算商品库存信息
							if (goods_inventory == 0 || goods_inventory < gc.getCount()) {
								inventory_very = false;
							}
						}
						if (inventory_very) {
							User buyer = this.userService.getObjById(CommUtil.null2Long(user.getId()));
							OrderForm main_order = null;
							if (payType.equals("payafter")) {// 使用货到付款
								Map payafter_payTypemap = new HashMap();
								String pay_session = CommUtil.randomString(32);
								request.getSession(false).setAttribute("pay_session", pay_session);
								payafter_payTypemap.put("pay_session", pay_session);
								goods_cart3_map.put("payafter_payTypemap", payafter_payTypemap);
								result = new Result(0, "货到付款提交成功", goods_cart3_map);
							}
							double all_of_price = 0;
							request.getSession(false).removeAttribute("cart_session");// 删除订单提交唯一标示，用户不能进行第二次订单提交
							String store_ids[] = store_id.split(",");
							List<Map> child_order_maps = new ArrayList<Map>();
							int whether_gift_in = 0;// 判断是否有满就送 子订单中包含赠品
													// 则主订单whether_gift变为1
							String order_suffix = CommUtil.formatTime("yyyyMMddHHmmss", new Date());
							Long order_id = null;
							for (int i = 0; i < store_ids.length; i++) {// 根据店铺id，保存多个子订单
								String sid = store_ids[i];
								Store store = null;
								List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
								List<Map> map_list = new ArrayList<Map>();
								if (sid != "self" && !sid.equals("self")) {
									store = this.storeService.getObjById(CommUtil.null2Long(sid));
								}
								String tempString = "";
								boolean oversea_very = true;
								for (GoodsCart gc : order_carts) { // 海外仓:
																	// 优先使用海外仓，多个商品有一件海外仓库存不足整单使用国际直邮
									if (gc.getGoods().getGoods_type() == 1) {// 商家商品
										boolean add = false;
										for (String gc_id : gc_ids) {
											if (!CommUtil.null2String(gc_id).equals("")
													&& gc.getId().equals(CommUtil.null2Long(gc_id))) {// 判断是否是用户勾选要购买的商品
												add = true;
												break;
											}
										}
										if (add) {
											if (gc.getGoods().getGoods_store().getId()
													.equals(CommUtil.null2Long(sid))) {
												String goods_type = "";
												if ("combin" == gc.getCart_type()
														|| "combin".equals(gc.getCart_type())) {
													if (gc.getCombin_main() == 1) { // [1为套装主购物车，0为其他套装购物车]
														goods_type = "combin";
													}
												}
												if ("group" == gc.getCart_type() || "group".equals(gc.getCart_type())) {
													goods_type = "group";
												}
												int goods_inventory = 0;
												int oversea_inventory = 0;
												Goods obj = gc.getGoods();
												if (gc.getGoods().getInventory_type().equals("all")) {
													goods_inventory = obj.getGoods_inventory();
													oversea_inventory = obj.getOversea_inventory();
												} else {
													Map<String, Object> spec = this.mCartViewTools
															.generic_default_info_color(obj, gc.getCart_gsp(),
																	gc.getColor());
													goods_inventory = CommUtil.null2Int(spec.get("count"));
													oversea_inventory = CommUtil
															.null2Int(spec.get("oversea_inventory"));
												}
												if (oversea_inventory < gc.getCount()) {
													oversea_very = false;
												}
												/*
												 * 商品快照 final String genId =
												 * user.getId() +
												 * UUID.randomUUID().toString()
												 * + ".html"; final String
												 * goodsId =
												 * gc.getGoods().getId().
												 * toString(); final String url
												 * =
												 * CommUtil.getHttpURL(request);
												 * HttpClient client = new
												 * HttpClient(); HttpMethod
												 * method = new GetMethod(url +
												 * "/goods.json?id=" + goodsId);
												 * try {
												 * client.executeMethod(method);
												 * } catch (HttpException e2) {
												 * // TODO Auto-generated catch
												 * // block
												 * e2.printStackTrace(); } catch
												 * (IOException e2) { // TODO
												 * Auto-generated catch // block
												 * e2.printStackTrace(); }
												 * 
												 * try { String resposneBody =
												 * method.
												 * getResponseBodyAsString();
												 * JSONObject jsonObject =
												 * JSON.parseObject(resposneBody
												 * ); String code =
												 * jsonObject.get("code").
												 * toString(); if(code != null
												 * && !"".equals(code)){
												 * tempString =
												 * jsonObject.get("data").
												 * toString(); }
												 * 
												 * } catch (IOException e2) { //
												 * TODO Auto-generated catch //
												 * block e2.printStackTrace(); }
												 */
												Map json_map = new HashMap();
												json_map.put("goods_id", gc.getGoods().getId());
												json_map.put("goods_name", gc.getGoods().getGoods_name());
												json_map.put("ksa_goods_name", gc.getGoods().getKsa_goods_name());
												json_map.put("goods_choice_type", gc.getGoods().getGoods_choice_type());
												json_map.put("goods_type", goods_type);
												json_map.put("goods_color", gc.getColor());
												json_map.put("goods_sku",
														gc.getGoods().getInventory_type().equals("all")
																? gc.getGoods().getGoods_serial()
																: this.mCartViewTools
																		.generic_default_info_color(gc.getGoods(),
																				gc.getCart_gsp(), gc.getColor())
																		.get("sku"));
												json_map.put("goods_weight", gc.getGoods().getGoods_weight());
												json_map.put("goods_length", gc.getGoods().getGoods_length());
												json_map.put("goods_width", gc.getGoods().getGoods_width());
												json_map.put("goods_high", gc.getGoods().getGoods_high());
												json_map.put("goods_count", gc.getCount());
												json_map.put("goods_price", gc.getPrice());
												json_map.put("goods_current_price", gc.getPrice());
												json_map.put("goods_all_price",
														CommUtil.mul(gc.getPrice(), gc.getCount()));// 商品总价
												json_map.put("goods_commission_price",
														this.mCartViewTools.getGoodsCommission(gc));// 设置该商品总佣金
												json_map.put("goods_commission_rate",
														gc.getGoods().getGc().getCommission_rate());// 设置该商品的佣金比例
												json_map.put("goods_payoff_price",
														CommUtil.subtract(CommUtil.mul(gc.getPrice(), gc.getCount()),
																this.mCartViewTools.getGoodsCommission(gc)));// 该商品结账价格=该商品总价格-商品总佣金
												json_map.put("goods_gsp_val", gc.getSpec_info());
												json_map.put("goods_gsp_ids", gc.getCart_gsp());
												json_map.put("evaluate", 1);
												// json_map.put("goods_snapshoot",
												// tempString);
												if (gc.getGoods().getGoods_main_photo() != null) {
													json_map.put("goods_mainphoto_path",
															gc.getGoods().getGoods_main_photo().getPath() + "/"
																	+ gc.getGoods().getGoods_main_photo().getName()
																	+ "_small."
																	+ gc.getGoods().getGoods_main_photo().getExt());
												} else {
													json_map.put("goods_mainphoto_path",
															this.configService.getSysConfig().getGoodsImage().getPath()
																	+ "/" + this.configService.getSysConfig()
																			.getGoodsImage().getName());
												}
												String goods_domainPath = CommUtil.getURL(request) + "/goods_"
														+ gc.getGoods().getId() + ".htm";
												String store_domainPath = CommUtil.getURL(request) + "/store_"
														+ gc.getGoods().getGoods_store().getId() + ".htm";
												if (this.configService.getSysConfig().isSecond_domain_open()
														&& gc.getGoods().getGoods_store().getStore_second_domain() != ""
														&& gc.getGoods().getGoods_type() == 1) {
													String store_second_domain = "http://"
															+ gc.getGoods().getGoods_store().getStore_second_domain()
															+ "." + CommUtil.generic_domain(request);
													goods_domainPath = store_second_domain + "/goods_"
															+ gc.getGoods().getId() + ".htm";
													store_domainPath = store_second_domain;
												}
												json_map.put("goods_domainPath", goods_domainPath);// 商品二级域名路径
												json_map.put("store_domainPath", store_domainPath);// 店铺二级域名路径
												// 设置商品组合套装信息
												if (goods_type.equals("combin")) {
													json_map.put("combin_suit_info", gc.getCombin_suit_info());
												}
												map_list.add(json_map);
												gc_list.add(gc);

											}
										}
									} else {// 自营商品
										boolean add = false;
										for (String gc_id : gc_ids) {
											if (!CommUtil.null2String(gc_id).equals("")
													&& gc.getId().equals(CommUtil.null2Long(gc_id))) {// 判断是否是用户勾选要购买的商品
												add = true;
												break;
											}
										}
										if (add) {
											if (sid == "self" || sid.equals("self")) {
												String goods_type = "";
												if ("combin" == gc.getCart_type()
														|| "combin".equals(gc.getCart_type())) {
													if (gc.getCombin_main() == 1) {
														goods_type = "combin";
													}

												}
												if ("group" == gc.getCart_type() || "group".equals(gc.getCart_type())) {
													goods_type = "group";
												}
												final String genId = user.getId() + UUID.randomUUID().toString()
														+ ".html";
												final String goodsId = gc.getGoods().getId().toString();
												String uploadFilePath = this.configService.getSysConfig()
														.getUploadFilePath();
												final String saveFilePathName = request.getSession().getServletContext()
														.getRealPath("/") + uploadFilePath + File.separator
														+ "snapshoot" + File.separator + genId;
												File file = new File(
														request.getSession().getServletContext().getRealPath("/")
																+ uploadFilePath + File.separator + "snapshoot");
												if (!file.exists()) {
													file.mkdir();
												}
												final String url = CommUtil.getURL(request);
												Thread t = new Thread(new Runnable() {
													public void run() {
														HttpClient client = new HttpClient();
														HttpMethod method = new GetMethod(
																url + "/goods_" + goodsId + ".htm");
														try {
															client.executeMethod(method);
														} catch (HttpException e2) {
															// TODO
															// Auto-generated
															// catch
															// block
															e2.printStackTrace();
														} catch (IOException e2) {
															// TODO
															// Auto-generated
															// catch
															// block
															e2.printStackTrace();
														}
														String tempString = "";
														try {
															tempString = method.getResponseBodyAsString();
														} catch (IOException e2) {
															// TODO
															// Auto-generated
															// catch
															// block
															e2.printStackTrace();
														}
														method.releaseConnection();
														BufferedWriter writer = null;
														try {
															writer = new BufferedWriter(
																	new FileWriter(saveFilePathName));
														} catch (IOException e1) {
															e1.printStackTrace();
														}
														try {
															writer.append(tempString);
															writer.flush();// 需要及时清掉流的缓冲区，万一文件过大就有可能无法写入了
															writer.close();
														} catch (IOException e) {
															e.printStackTrace();
														}
													}
												});
												t.start();
												Map json_map = new HashMap();
												json_map.put("goods_id", gc.getGoods().getId());
												json_map.put("goods_name", gc.getGoods().getGoods_name());
												json_map.put("ksa_goods_name", gc.getGoods().getKsa_goods_name());
												json_map.put("goods_choice_type", gc.getGoods().getGoods_choice_type());
												json_map.put("goods_weight", gc.getGoods().getGoods_weight());
												json_map.put("goods_length", gc.getGoods().getGoods_length());
												json_map.put("goods_width", gc.getGoods().getGoods_width());
												json_map.put("goods_high", gc.getGoods().getGoods_high());
												json_map.put("goods_spu", gc.getGoods().getGoods_serial());
												json_map.put("goods_sku",
														this.mCartViewTools.generic_default_info_color(gc.getGoods(),
																gc.getCart_gsp(), gc.getColor()).get("sku"));
												json_map.put("goods_count", gc.getCount());
												json_map.put("goods_price", gc.getPrice());// 商品单价
												json_map.put("goods_all_price",
														CommUtil.mul(gc.getPrice(), gc.getCount()));// 商品总价
												json_map.put("goods_gsp_val", gc.getSpec_info());
												json_map.put("goods_color", gc.getColor());
												json_map.put("goods_gsp_ids",
														gc.getCart_gsp() == null ? "" : gc.getCart_gsp());
												json_map.put("goods_snapshoot", CommUtil.getURL(request) + "/"
														+ uploadFilePath + "/snapshoot/" + genId);
												json_map.put("evaluate", 1);
												if (gc.getGoods().getGoods_main_photo() != null) {
													json_map.put("goods_mainphoto_path",
															gc.getGoods().getGoods_main_photo().getPath() + "/"
																	+ gc.getGoods().getGoods_main_photo().getName()
																	+ "_small."
																	+ gc.getGoods().getGoods_main_photo().getExt());
												} else {
													json_map.put("goods_mainphoto_path",
															this.configService.getSysConfig().getGoodsImage().getPath()
																	+ "/" + this.configService.getSysConfig()
																			.getGoodsImage().getName());
												}
												json_map.put("goods_domainPath", CommUtil.getURL(request) + "/goods_"
														+ gc.getGoods().getId() + ".htm");// 商品二级域名路径
												// 设置商品组合套装信息
												if (goods_type.equals("combin")) {
													json_map.put("combin_suit_info", gc.getCombin_suit_info());
												}
												// 增加商品复购次数

												map_list.add(json_map);
												gc_list.add(gc);
											}
										}
									}
								}
								// 赠品信息
								/*
								 * List<Map> gift_map = new ArrayList<Map>();
								 * for (int j = 0; gift_goods.size() > 0 && j <
								 * gift_goods.size(); j++) { if
								 * (gift_goods.get(j).getGoods_type() == 1) { if
								 * (gift_goods.get(j).getGoods_store() != null
								 * &&
								 * gift_goods.get(j).getGoods_store().getId().
								 * toString().equals(sid)) { Map map = new
								 * HashMap(); map.put("goods_id",
								 * gift_goods.get(j).getId());
								 * map.put("goods_name",
								 * gift_goods.get(j).getGoods_name());
								 * map.put("goods_main_photo",
								 * gift_goods.get(j).getGoods_main_photo().
								 * getPath() + "/" +
								 * gift_goods.get(j).getGoods_main_photo().
								 * getName() + "_small." +
								 * gift_goods.get(j).getGoods_main_photo().
								 * getExt()); map.put("goods_price",
								 * gift_goods.get(j).getGoods_current_price());
								 * String goods_domainPath =
								 * CommUtil.getURL(request) + "/goods_" +
								 * gift_goods.get(j).getId() + ".htm"; if
								 * (this.configService.getSysConfig().
								 * isSecond_domain_open() &&
								 * gift_goods.get(j).getGoods_store().
								 * getStore_second_domain() != "" &&
								 * gift_goods.get(j).getGoods_type() == 1) {
								 * String store_second_domain = "http://" +
								 * gift_goods.get(j).getGoods_store().
								 * getStore_second_domain() + "." +
								 * CommUtil.generic_domain(request);
								 * goods_domainPath = store_second_domain +
								 * "/goods_" + gift_goods.get(j).getId() +
								 * ".htm"; } map.put("goods_domainPath",
								 * goods_domainPath);// 商品二级域名路径
								 * map.put("buyGify_id",
								 * gift_goods.get(j).getBuyGift_id());
								 * gift_map.add(map); } } else { if
								 * (sid.equals("self") || sid == "self") { Map
								 * map = new HashMap(); map.put("goods_id",
								 * gift_goods.get(j).getId());
								 * map.put("goods_name",
								 * gift_goods.get(j).getGoods_name());
								 * map.put("goods_main_photo",
								 * gift_goods.get(j).getGoods_main_photo().
								 * getPath() + "/" +
								 * gift_goods.get(j).getGoods_main_photo().
								 * getName() + "_small." +
								 * gift_goods.get(j).getGoods_main_photo().
								 * getExt()); map.put("goods_price",
								 * gift_goods.get(j).getGoods_current_price());
								 * String goods_domainPath =
								 * CommUtil.getURL(request) + "/goods_" +
								 * gift_goods.get(j).getId() + ".htm"; if
								 * (this.configService.getSysConfig().
								 * isSecond_domain_open() &&
								 * gift_goods.get(j).getGoods_store().
								 * getStore_second_domain() != "" &&
								 * gift_goods.get(j).getGoods_type() == 1) {
								 * String store_second_domain = "http://" +
								 * gift_goods.get(j).getGoods_store().
								 * getStore_second_domain() + "." +
								 * CommUtil.generic_domain(request);
								 * goods_domainPath = store_second_domain +
								 * "/goods_" + gift_goods.get(j).getId() +
								 * ".htm"; } map.put("goods_domainPath",
								 * goods_domainPath);// 商品二级域名路径
								 * map.put("buyGify_id",
								 * gift_goods.get(j).getBuyGift_id());
								 * gift_map.add(map); } } }
								 */
								double goods_amount = this.mCartViewTools.calCartPrice(gc_list, gcs);// 订单中商品价格
								String transfee = "-1"; // [多个商品有一件商家承担运费，结算页面就展示商家承担的运费，
														// 0:买家承担 1：卖家承担]
								double ship_price = 0.00;
								double store_ship_price = 0.00;
								double totalPrice = 0;
								// String transport =
								// request.getParameter("transport_" + sid);
								String transport = "Express";
								if (store.getEnough_free() == 1
										&& CommUtil.subtract(store.getEnough_free_price(), goods_amount) <= 0) {
									transfee = "1";
									transport = "Express";
									totalPrice = goods_amount;
								} else {
									// 订单页面根据用户选择地址计算运费
									List<SysMap> sms = this.transportTools.query_cart_trans_goods_cart3(gc_list,
											CommUtil.null2String(address.getArea().getId()));

									/*
									 * if
									 * (CommUtil.null2String(transport).indexOf(
									 * "平邮") < 0 &&
									 * CommUtil.null2String(transport).indexOf(
									 * "Express") < 0 &&
									 * CommUtil.null2String(transport).indexOf(
									 * "EMS") < 0) { transport = "Express"; }
									 */
									for (SysMap sm : sms) {
										if (CommUtil.null2String(sm.getKey()).indexOf(transport) >= 0) {
											ship_price = ship_price + CommUtil.null2Double(sm.getValue());// 订单物流运费
										} else {
											store_ship_price = CommUtil.null2Double(sm.getValue());
										}
									}

									if (store_ship_price == 0.0) {
										transfee = "0";
									} else {
										transfee = "1";
									}
									totalPrice = CommUtil.add(goods_amount, ship_price);// 订单总价
								}
								double commission_amount = this.mCartViewTools.getOrderCommission(gc_list);// 订单总体佣金
								double goods_vat = CommUtil.mul(goods_amount, 0.05);// [服务VAT]
								double commission_vat = CommUtil.mul(commission_amount, 0.05);// [佣金VAT]
								Map ermap = this.mCartViewTools.calEnoughReducePrice(gc_list, gcs);
								String er_json = (String) ermap.get("er_json");
								double all_goods = Double.parseDouble(ermap.get("all").toString());
								double reduce = Double.parseDouble(ermap.get("reduce").toString());
								OrderForm of = new OrderForm();
								of.setAddTime(new Date());

								String order_store_id = "0";
								if (sid != "self" && !sid.equals("self")) {
									order_store_id = CommUtil.null2String(store.getId());
								}

								String SM = "SM" + CommUtil.randomString(5) + user.getId();
								of.setOrder_id(SM);
								// 设置收货地址信息
								if (address.getArea().getLevel() == 2) {
									of.setReceiver_area(address.getArea().getParent().getParent().getAreaName() + " "
											+ address.getArea().getParent().getAreaName()
											+ address.getArea().getAreaName());
									of.setReceiver_state(address.getArea().getParent().getParent().getAreaName());
									of.setReceiver_city(address.getArea().getParent().getAreaName());
									of.setReceiver_street(address.getArea().getAreaName());
								} else if (address.getArea().getLevel() == 1) {
									of.setReceiver_area(address.getArea().getParent().getAreaName()
											+ address.getArea().getAreaName());
									of.setReceiver_state(address.getArea().getParent().getAreaName());
									of.setReceiver_city(address.getArea().getAreaName());
									of.setReceiver_street("");
								}
								of.setReceiver_Name(address.getTrueName());
								of.setReceiver_area_info(address.getArea_info());
								of.setReceiver_mobile(address.getMobile());
								of.setReceiver_telephone(address.getTelephone());
								of.setReceiver_zip(address.getZip());
								of.setReceiver_email(address.getEmail());
								of.setEnough_reduce_amount(BigDecimal.valueOf(reduce));
								of.setEnough_reduce_info(er_json);
								of.setTransport_type(transfee);
								of.setTransport(transport);
								of.setOrder_status(10);
								of.setUser_id(buyer.getId().toString());
								of.setUser_name(buyer.getUserName());
								of.setGoods_info(Json.toJson(map_list, JsonFormat.compact()));// 设置商品信息json数据
								of.setMsg(request.getParameter("msg_" + sid));
								of.setInvoiceType(CommUtil.null2Int(request.getParameter("invoiceType")));
								of.setInvoice(request.getParameter("invoice"));
								of.setShip_price(BigDecimal.valueOf(ship_price));
								of.setStore_ship_price(BigDecimal.valueOf(store_ship_price));
								of.setGoods_amount(BigDecimal.valueOf(all_goods));
								of.setTotalPrice(BigDecimal.valueOf(totalPrice));
								of.setOrder_cat(0);
								// of.setSnapshooot(tempString);
								// [商家优惠券信息]
								String coupon_id = this.mCartViewTools.coupon(storeCouponId,
										CommUtil.null2String(store.getId()));// 弃用
								// String coupon_id =
								// request.getParameter("couponId");
								/*
								 * Map storeCoupon = Json.fromJson(Map.class,
								 * storeCouponId); String coupon_id =
								 * storeCoupon.get(CommUtil.null2String(store.
								 * getId())).toString();
								 */
								if (coupon_id != null && !coupon_id.equals("")) {
									CouponInfo couponInfo = this.couponInfoService
											.getObjById(CommUtil.null2Long(coupon_id));
									if (couponInfo != null) {
										if (user.getId().equals(couponInfo.getUser().getId())) {
											couponInfo.setStatus(1);
											this.couponInfoService.update(couponInfo);
											Map couponMap = new HashMap();
											couponMap.put("couponinfo_id", couponInfo.getId());
											couponMap.put("couponinfo_sn", couponInfo.getCoupon_sn());
											couponMap.put("coupon_amount", couponInfo.getCoupon().getCoupon_amount());
											if (store_ids.length > 1) {
												double rate = CommUtil.div(couponInfo.getCoupon().getCoupon_amount(),
														goods_amount);
												couponMap.put("coupon_goods_rate", rate);
											} else {
												couponMap.put("coupon_goods_rate", 1);
											}
											of.setCoupon_info(Json.toJson(couponMap, JsonFormat.compact()));
											of.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(of.getTotalPrice(),
													couponInfo.getCoupon().getCoupon_amount())));
										}
									}
								}

								all_of_price = all_of_price + of.getTotalPrice().doubleValue();// 总订单价格
								if (sid.equals("self") || sid == "self") {
									of.setOrder_form(1);// 平台自营商品订单
								} else {
									of.setCommission_amount(BigDecimal.valueOf(commission_amount));// 该订单总体佣金费用
									of.setGoods_vat(BigDecimal.valueOf(goods_vat));
									of.setCommission_vat(BigDecimal.valueOf(commission_vat));
									of.setOrder_form(0);// 商家商品订单
									of.setStore_id(store.getId().toString());
									of.setStore_name(store.getStore_name());
								}
								of.setOrder_type(order_type);// App下单
								of.setDelivery_time(delivery_time);
								/*
								 * if (gift_map.size() > 0) {
								 * of.setGift_infos(Json.toJson(gift_map,
								 * JsonFormat.compact()));
								 * of.setWhether_gift(1); whether_gift_in++; }
								 */
								of.setDelivery_type(0);
								/*
								 * if (CommUtil.null2Int(delivery_type) == 1 &&
								 * delivery_id != null &&
								 * !delivery_id.equals("")) {// 自提点信息，使用json管理
								 * of.setDelivery_type(1); DeliveryAddress
								 * deliveryAddr = this.deliveryaddrService
								 * .getObjById(CommUtil.null2Long(delivery_id));
								 * String service_time = "全天"; if
								 * (deliveryAddr.getDa_service_type() == 1) {
								 * service_time =
								 * deliveryAddr.getDa_begin_time() + "点至" +
								 * deliveryAddr.getDa_end_time() + "点"; }
								 * params.clear(); params.put("id",
								 * deliveryAddr.getId()); params.put("da_name",
								 * deliveryAddr.getDa_name());
								 * params.put("da_content",
								 * deliveryAddr.getDa_content());
								 * params.put("da_contact_user",
								 * deliveryAddr.getDa_contact_user());
								 * params.put("da_tel",
								 * deliveryAddr.getDa_tel());
								 * params.put("da_address",
								 * deliveryAddr.getDa_area().getParent().
								 * getParent().getAreaName() +
								 * deliveryAddr.getDa_area().getParent().
								 * getAreaName() +
								 * deliveryAddr.getDa_area().getAreaName() +
								 * deliveryAddr.getDa_address());
								 * params.put("da_service_day",
								 * this.DeliveryAddressTools
								 * .query_service_day(deliveryAddr.
								 * getDa_service_day()));
								 * params.put("da_service_time", service_time);
								 * of.setDelivery_address_id(deliveryAddr.getId(
								 * )); of.setDelivery_info(Json.toJson(params,
								 * JsonFormat.compact())); }
								 */
								// 优先使用海外仓
								if (oversea_very) {
									params.clear();
									params.put("repository", "1");
									List<ShipAddress> sa = this.shipAddressService.query(
											"select obj from ShipAddress obj where obj.repository=:repository", params,
											-1, -1);
									if (sa.size() > 0)
										of.setShip_addr_id(sa.get(0).getId());
								}
								if (i == store_ids.length - 1) {
									of.setOrder_main(1);// 同时购买多个商家商品，最后一个订单为主订单，其他的作为子订单，以json信息保存，用在买家中心统一显示大订单，统一付款
									if (whether_gift_in > 0) {
										of.setWhether_gift(1);
									}
									if (child_order_maps.size() > 0) {
										of.setChild_order_detail(Json.toJson(child_order_maps, JsonFormat.compact()));
									}
								}
								boolean flag = this.orderFormService.save(of);
								main_order = of;
								if (i == store_ids.length - 1) {
									order_id = of.getId();
									goods_cart3_map.put("order_id", of.getId());
									goods_cart3_map.put("order_num", of.getOrder_id());
								}
								if (flag) {
									// 如果是多个店铺的订单同时提交，则记录子订单信息到主订单中，用在买家中心统一显示及统一付款
									if (store_ids.length > 1) {
										Map order_map = new HashMap();
										order_map.put("order_id", of.getId());
										order_map.put("store_id", store.getId());
										order_map.put("store_name", store.getStore_name());
										order_map.put("store_logo",
												store.getStore_logo() != null ? store.getStore_logo().getPath() + "/"
														+ store.getStore_logo().getName() : "");
										order_map.put("order_goods_info", of.getGoods_info());
										child_order_maps.add(order_map);
									}
									for (GoodsCart gc : gc_list) {// 删除已经提交订单的购物车信息
										if (gc.getCart_type() != null && gc.getCart_type().equals("combin")
												&& gc.getCombin_main() == 1) {// 购物车提交订单时如果为组合套装购物车，只提交组合套装主购物车，删除主购物车同时删除该套装中其他购物车
											Map combin_map = new HashMap();
											combin_map.put("combin_mark", gc.getCombin_mark());
											combin_map.put("combin_main", 1);
											List<GoodsCart> suits = this.goodsCartService.query(
													"select obj from GoodsCart obj where obj.combin_mark=:combin_mark and obj.combin_main!=:combin_main",
													combin_map, -1, -1);
											for (GoodsCart suit : suits) {
												gc.getGsps().clear();
												this.goodsCartService.delete(suit.getId());
											}
										}
										for (String gc_id : gc_ids) {
											if (!CommUtil.null2String(gc_id).equals("")
													&& CommUtil.null2Long(gc_id).equals(gc.getId())) {
												gc.getGsps().clear();
												this.goodsCartService.delete(gc.getId());
											}
										}
									}

									OrderFormLog ofl = new OrderFormLog();
									ofl.setAddTime(new Date());
									ofl.setOf(of);
									ofl.setLog_info("提交订单");
									ofl.setLog_user(user);
									this.orderFormLogService.save(ofl);
								}
							}
							// 全网通用优惠券
							this.mCartViewTools.coupon_price(generalCouponId, order_id);
							// 在循环外，给买家只发送一次短信邮件
							/*
							 * if (main_order.getOrder_form() == 0) {
							 * this.msgTools.sendEmailCharge(CommUtil.getURL(
							 * request), "email_tobuyer_order_submit_ok_notify",
							 * buyer.getEmail(), null,
							 * CommUtil.null2String(main_order.getId()),
							 * main_order.getStore_id());
							 * this.msgTools.sendSmsCharge(CommUtil.getURL(
							 * request), "sms_tobuyer_order_submit_ok_notify",
							 * buyer.getMobile(), null,
							 * CommUtil.null2String(main_order.getId()),
							 * main_order.getStore_id()); } else {
							 * this.msgTools.sendEmailFree(CommUtil.getURL(
							 * request), "email_tobuyer_order_submit_ok_notify",
							 * buyer.getEmail(), null,
							 * CommUtil.null2String(main_order.getId()));
							 * this.msgTools.sendSmsFree(CommUtil.getURL(request
							 * ), "sms_tobuyer_order_submit_ok_notify",
							 * buyer.getMobile(), null,
							 * CommUtil.null2String(main_order.getId())); }
							 */
							goods_cart3_map.put("all_of_price", CommUtil.formatMoney(all_of_price));

						} else {// 验证库存不成功，返回购物车，并给出提示！
							result = new Result(2, "订单中库存为零");
						}
					} else {
						result = new Result(3, "订单信息错误");
					}
				} else {
					result = new Result(4, "订单已经失效");
				}
				/*
				 * }else{ result = new Result(5,"验证码错误"); }
				 */
			}
		}
		return Json.toJson(result, JsonFormat.compact());
		// this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 货到付款
	 * 
	 * @param request
	 * @param response
	 * @param payType
	 * @param order_id
	 * @param pay_msg
	 * @param pay_session
	 * @return
	 * @throws Exception
	 */

	@SecurityMapping(title = "订单货到付款", value = "/order_pay_payafter.htm*", rtype = "buyer", rname = "购物流程3", rcode = "goods_cart", rgroup = "在线购物")
	// @RequestMapping("/order_pay_payafter.json")
	@EmailMapping(title = "订单货到付款", value = "order_pay_payafter")
	@RequestMapping("v1/order_pay_payafter.json")
	@ResponseBody
	public String order_pay_payafter(HttpServletRequest request, HttpServletResponse response, String payType,
			String order_id, String pay_msg, String pay_session, String token) throws Exception {
		Result result = null;
		Map order_pay_map = new HashMap();
		Map order_pay_payafter = new HashMap();
		ModelAndView mv = new JModelAndView("success.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if (users.isEmpty()) {
				result = new Result(-100, "token Invalidation");
			} else {
				User user = users.get(0);
				String pay_session1 = CommUtil.null2String(request.getSession(false).getAttribute("pay_session"));
				if (pay_session1.equals(pay_session)) {
					OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
					order.setPay_msg(pay_msg);
					order.setPayTime(new Date());
					order.setPayType("COD");
					order.setOrder_status(16);
					this.orderFormService.update(order);
					if (order.getPayType() != null && order.getPayType().equals("COD")) {
						this.mCartViewTools.updateGoodsInventory(order);// 更新商品库存
					}
					StoreLog storeLog = this.storeLogTools.getTodayStoreLog(CommUtil.null2Long(order.getStore_id()));
					storeLog.setPlaceorder(storeLog.getPlaceorder() + 1);
					if (this.orderFormTools.queryOrder(order.getStore_name())) {
						storeLog.setRepetition(storeLog.getPlaceorder() == 1 ? 0 : storeLog.getRepetition() + 1);
					}
					this.storeLogService.update(storeLog);
					List<Long> cart_order_ids = new ArrayList<Long>();
					cart_order_ids.add(order.getId());
					if (order.getOrder_main() == 1 && !CommUtil.null2String(order.getChild_order_detail()).equals("")) {
						List<Map> maps = this.orderFormTools.queryGoodsInfo(order.getChild_order_detail());
						for (Map child_map : maps) {
							OrderForm child = this.orderFormService
									.getObjById(CommUtil.null2Long(child_map.get("order_id")));
							cart_order_ids.add(child.getId());
							child.setOrder_status(16);
							child.setPayType("COD");
							child.setPayTime(new Date());
							child.setPay_msg(pay_msg);
							this.orderFormService.update(child);
							if (order.getPayType() != null && order.getPayType().equals("COD")) {// 如果买家支付方式为货到付款，买家确认收货时更新商品库存
								this.mCartViewTools.updateGoodsInventory(child);// 更新商品库存
							}
							// 向加盟商家发送付款成功短信提示，自营商品无需发送短信提示
							Store store = this.storeService.getObjById(CommUtil.null2Long(child.getStore_id()));
							StoreLog storeLogc = this.storeLogTools
									.getTodayStoreLog(CommUtil.null2Long(order.getStore_id()));
							storeLogc.setPlaceorder(storeLogc.getPlaceorder() + 1);
							if (this.orderFormTools.queryOrder(order.getStore_name())) {
								storeLogc.setRepetition(storeLogc.getRepetition() + 1);
							}
							this.storeLogService.update(storeLogc);
							/*
							 * if (child_order.getOrder_form() == 0) {
							 * this.msgTools.sendSmsCharge(CommUtil.getURL(
							 * request), "sms_toseller_payafter_pay_ok_notify",
							 * store.getUser().getMobile(), null,
							 * CommUtil.null2String(child_order.getId()),
							 * child_order.getStore_id());
							 * this.msgTools.sendEmailCharge(CommUtil.getURL(
							 * request),
							 * "email_toseller_payafter_pay_ok_notify",
							 * store.getUser().getEmail(), null,
							 * CommUtil.null2String(child_order.getId()),
							 * child_order.getStore_id()); }
							 */
						}
					}
					// 记录支付日志
					OrderFormLog ofl = new OrderFormLog();
					ofl.setAddTime(new Date());
					ofl.setLog_info("提交货到付款申请");
					ofl.setLog_user(user);
					ofl.setOf(order);
					this.orderFormLogService.save(ofl);
					String query = "select * from metoo_lucky_draw where switchs = 1";
					ResultSet res = this.databaseTools.selectIn(query);
					int lucky = 0;
					while (res.next()) {
						lucky = res.getInt("order");
					}
					user.setRaffle(user.getRaffle() + lucky);
					this.userService.update(user);
					request.getSession(false).removeAttribute("pay_session");
					order_pay_map.put("msg", "货到付款提交成功，等待发货");
					order_pay_map.put("raffle", lucky);
					order_pay_map.put("cart_order_ids", cart_order_ids);
				} else {
					order_pay_map.put("msg", "订单已经支付，禁止重复支付");
					result = new Result(1, order_pay_map);
				}
				result = new Result(0, order_pay_map);
			}
		}
		return Json.toJson(result, JsonFormat.compact());
		// this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	@SecurityMapping(title = "订单短信发送", value = "/order_account_mobile.json*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/Order_verify.json")
	// @RequestMapping("v1/Order_verify.json")
	public void order_mobile(HttpServletRequest request, HttpServletResponse response, String type, String mobile,
			String token) throws UnsupportedEncodingException {
		Result result = null;
		String ret = "0";
		String msg = "SMS sent successfully";
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				if (type.equals("mobile_verify_code")) {
					boolean flag = this.mobileTools.verify(mobile);
					if (flag) {
						Map map = this.mobileTools.mobile(mobile);
						String code = CommUtil.randomIntApp(4).toUpperCase();
						String content = "Your Soarmall verification code is " + code + ".The code is valid in 5 mins.";
						if (this.configService.getSysConfig().isSmsEnbale()) {
							boolean ret1 = this.msgTools.sendSMS(CommUtil.null2String(map.get("mobile")), content);
							if (ret1) {
								VerifyCode mvc = this.mobileverifycodeService.getObjByProperty(null, "mobile",
										CommUtil.null2String(map.get("areaMobile")));
								if (mvc == null) {
									mvc = new VerifyCode();
								}
								mvc.setAddTime(new Date());
								mvc.setCode(code);
								mvc.setMobile(CommUtil.null2String(map.get("areaMobile")));
								this.mobileverifycodeService.update(mvc);
							} else {
								ret = "4210";
								msg = "短信发送失败";
							}
						} else {
							ret = "4230";
							msg = "短信平台开启";
						}
					} else {
						ret = "4240";
						msg = "手机号码格式错误";
					}

				}
			}
			this.send_json(Json.toJson(new Result(CommUtil.null2Int(ret), msg), JsonFormat.compact()), response);
		}
	}

	/**
	 * @description 购物车商品规格调整
	 * @param request
	 * @param response
	 * @param id
	 */
	// @RequestMapping("cartSpecQuery.json")
	@RequestMapping("v1/cartSpecQuery.json")
	public void cartSpecQuery(HttpServletRequest request, HttpServletResponse response, String id) {
		int code = -1;
		String msg = "";
		GoodsCart obj = this.goodsCartService.getObjById(CommUtil.null2Long(id));
		List<Map> list = new ArrayList<Map>();
		if (obj != null) {
			Map map = this.mCartViewTools.cartGenericSpec(CommUtil.null2String(obj.getGoods().getId()),
					obj.getCart_gsp());
			map.put("color",
					this.mCartViewTools.cartGoodsColor(CommUtil.null2String(obj.getGoods().getId()), obj.getColor()));
			list.add(map);
			code = 4200;
			msg = "Successfully";
		} else {
			code = 4205;
			msg = "The product has been taken off the shelves";
		}
		this.send_json(Json.toJson(new Result(code, msg, list), JsonFormat.compact()), response);
	}

	/**
	 * @dscription 购物车商品规格调整
	 * @param request
	 * @param response
	 */
	// @RequestMapping("cartSpecSave.json")
	@RequestMapping("v1/cartSpecSave.json")
	public void cartSpecSave(HttpServletRequest request, HttpServletResponse response, String id, String color,
			String gsp, String visitor_id, String token) {
		int code = -1;
		String msg = "";
		User user = null;
		if (!"".equals(token)) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		String cart_session_id = this.mCartViewTools.generateCartSession(request, response, visitor_id);
		if (!"".equals(color) || !"".equals(gsp)) {
			Map params = new HashMap();
			params.put("id", CommUtil.null2Long(id));
			params.put("cart_status", 0);
			params.put("user", user == null ? null : user.getId());
			params.put("cart_session", user == null ? visitor_id : "");
			List<GoodsCart> list = this.goodsCartService.query(
					"select obj from GoodsCart obj where obj.id=:id and obj.cart_status=:cart_status and obj.cart_session_id=:cart_session or obj.user.id=:user",
					params, -1, -1);
			if (list.size() > 0) {
				GoodsCart obj = list.get(0);
				if (null != obj.getGoods() && obj.getGoods().getGoods_status() == 0) {
					// 查询商品库存
					Map spec = this.mCartViewTools.generic_default_info_color(obj.getGoods(), gsp, color);
					int goods_inventory = CommUtil.null2Int(spec.get("count"));
					double price = CommUtil.null2Double(spec.get("goods_current_price"));
					if (goods_inventory >= 1) {
						if (goods_inventory < obj.getCount()) { // 当前选中规格小于购物车数量自动修改购物车数量为当前规格剩余库存
							obj.setCount(goods_inventory);
						}
						String spec_info = "";
						String[] gsp_ids = CommUtil.null2String(gsp).split(",");
						// List gsp_ids = Json.fromJson(List.class, gsp);
						if (obj.getGsps().size() > 0) {
							obj.getGsps().removeAll(obj.getGsps());
							for (Object gsp_id : gsp_ids) {
								GoodsSpecProperty spec_property = this.goodsSpecPropertyService
										.getObjById(CommUtil.null2Long(gsp_id));
								if (null != spec_property) {
									obj.getGsps().add(spec_property);
									spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + ","
											+ spec_info;
								}
							}
						}
						obj.setColor(color);
						obj.setSpec_info(spec_info);
						obj.setCart_gsp(gsp);
						obj.setPrice(BigDecimal.valueOf(price));
						this.goodsCartService.update(obj);
						code = 4200;
						msg = "Successfully";
					} else {
						code = 4206;
						msg = "Goods in short stock";
					}
				} else {
					code = 4205;
					msg = "The product has been taken off the shelves";
				}
			} else {
				code = 4205;
				msg = "The product has been taken off the shelves";
			}
		} else {
			code = 4402;
			msg = "Product specifications not selected";
		}
		this.send_json(Json.toJson(new Result(code, msg), JsonFormat.compact()), response);
	}

	/**
	 * @description 根据购物车查询购物车规格
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	// @RequestMapping("/goods_cart1_spec.json")
	@RequestMapping("v1/goods_cart1_spec.json")
	public void goods_cart1_spec(HttpServletRequest request, HttpServletResponse response, String cart_id) {
		ModelAndView mv = new JModelAndView("goods_cart1_spec.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Result result = null;
		GoodsCart obj = this.goodsCartService.getObjById(CommUtil.null2Long(cart_id));
		if (obj == null) {
			result = new Result(4205, "The product has been taken off the shelves");
		} else {
			List<Map> spec_list = new ArrayList<Map>();
			Map spec_map = this.mCartViewTools.cartGenericSpec(CommUtil.null2String(obj.getGoods().getId()), "");
			spec_map.put("color", this.mCartViewTools.cartGenericSpec(CommUtil.null2String(obj.getGoods().getId()),
					obj.getCart_gsp()));
			spec_list.add(spec_map);
			result = new Result(0, "success", spec_list);
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 购物车修改规格保存
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	// @RequestMapping("/goods_cart1_spec_save.json")
	@RequestMapping("v1/goods_cart1_spec_save.json")
	public void goods_cart1_spec_save(HttpServletRequest request, HttpServletResponse response, String gsp, String id,
			String color, String token) {
		Result result = null;
		int code = 100;// 100修改成功，90库存不足
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				GoodsCart obj = this.goodsCartService.getObjById(CommUtil.null2Long(id));
				params.clear();
				params.put("user_id", user.getId());
				params.put("cart_status", 0);
				params.put("cart_id", obj.getId());
				List<GoodsCart> goodsCarts = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status and obj.id != :cart_id",
						params, -1, -1);
				String[] gsp_id1 = gsp.split(",");
				boolean flag = true;
				for (GoodsCart gc : goodsCarts) {
					String[] gsps_id = new String[gc.getGsps().size()];
					if (gc.getGsps() != null && gc.getGsps().size() > 0
							|| !CommUtil.null2String(gc.getColor()).equals("")) {
						for (int i = 0; i < gc.getGsps().size(); i++) {
							gsps_id[i] = gc.getGsps().get(i) != null ? gc.getGsps().get(i).getId().toString() : "";
						}
						if (gsps_id.length == 0 && "".equals(gsp)) {
							if (!gc.getColor().equals("")) {
								if (gc.getColor().equals(color)
										&& obj.getGoods().getId().toString().equals(gc.getGoods().getId().toString())) {
									flag = false;
									gc.setCount(gc.getCount() + obj.getCount());
									this.goodsCartService.update(gc);
									this.goodsCartService.delete(obj.getId());
									break;
								} else {
									flag = true;
									break;
								}
							}
						} else {
							Arrays.sort(gsps_id);
							Arrays.sort(gsp_id1);
							if (color.equals(gc.getColor())
									&& obj.getGoods().getId().toString().equals(gc.getGoods().getId().toString())
									&& Arrays.equals(gsp_id1, gsps_id)) {
								gc.setCount(gc.getCount() + obj.getCount());
								flag = false;
								this.goodsCartService.update(gc);
								this.goodsCartService.delete(obj.getId());
								break;
							}
						}

					}
				}
				if (flag) {
					// int goods_inventory =
					// CommUtil.null2Int(this.generic_default_info(obj.getGoods(),
					// gsp, user).get("count"));// 计算商品库存信息
					// double price =
					// CommUtil.null2Double(this.generic_default_info(obj.getGoods(),
					// gsp, user).get("price"));// 计算商品库存信息
					Map spec = this.mCartViewTools.generic_default_info_color(obj.getGoods(), gsp, color);
					int goods_inventory = CommUtil.null2Int(spec.get("count"));
					double price = CommUtil.null2Double(spec.get("goods_current_price"));
					if (goods_inventory == 0) {
						result = new Result(-2, "商品库存为零");
					} else {
						String[] gsp_ids = CommUtil.null2String(gsp).split(",");
						String spec_info = "";
						obj.getGsps().removeAll(obj.getGsps());
						for (String gsp_id : gsp_ids) {
							GoodsSpecProperty spec_property = this.goodsSpecPropertyService
									.getObjById(CommUtil.null2Long(gsp_id));
							if (spec_property != null) {
								obj.getGsps().add(spec_property);
								spec_info = spec_property.getSpec().getName() + "：" + spec_property.getValue() + "<br>"
										+ spec_info;
							}
						}
						obj.setColor(color);
						obj.setSpec_info(spec_info);
						obj.setPrice(BigDecimal.valueOf(price));
						this.goodsCartService.update(obj);

					}
				}
				result = new Result(0, "success");
			}
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @description 商品提交订单
	 * @param request
	 * @param response
	 * @param token
	 * @param id
	 *            商品id
	 * @param gsp_id
	 *            规格id
	 * @param color
	 *            颜色
	 * @author hkk
	 * @version 2020/4/2
	 */
	@RequestMapping("v1/buyNow.json")
	public void buyNow(HttpServletRequest request, HttpServletResponse response, String token, String id, String gsp,
			String color, String count, String type, String language) {
		int code = -1;// -100 为未登录
		String msg = "";
		Result result = null;
		Map map = new HashMap();
		if (!token.equals("")) {
			Map params = new HashMap();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user != null) {
				Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
				Store goodsStore = obj.getGoods_store();
				User storeUser = goodsStore.getUser();
				if (obj != null && obj.getGoods_store().getStore_status() == 15) {
					if (!user.getId().equals(storeUser.getId())) {
						boolean inventory_very = true;
						int oversea_inventory = 0;
						int goods_inventory = 0;
						// 判断优先使用海外仓
						if (obj.getInventory_type().equals("all")) {
							goods_inventory = obj.getGoods_inventory();
							oversea_inventory = obj.getOversea_inventory();
						} else {
							Map<String, Object> goods = this.mCartViewTools.generic_default_info_color(obj, gsp, color);
							goods_inventory = CommUtil.null2Int(goods.get("count"));
							oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory"));
						}
						if (CommUtil.subtract(goods_inventory, count) > 0
								|| CommUtil.subtract(oversea_inventory, count) > 0) {
							if (obj.getInventory_type().equals("spec") && "".equals(color) && "".equals(gsp)) {
								code = 4400;
								msg = "Please select specifications";
							} else {
								params.clear();
								params.put("user_id", user.getId());
								params.put("defaulr_val", 1);
								List<Address> addressList = this.addressService.query(
										"select obj from Address obj where obj.user.id=:user_id and obj.default_val=:defaulr_val",
										params, -1, -1);
								if (addressList.size() > 0) {
									String area_abbr = "";
									String area_id = "";
									for (Address address : addressList) {
										Map addressMap = this.mCartViewTools.orderAddress(address);
										map.put("address", addressMap);
										area_abbr = (String) addressMap.get("area_abbr");
										area_id = (String) addressMap.get("area_id");
									}
									String cart_session = CommUtil.randomString(32);
									request.getSession(false).setAttribute("cart_session", cart_session);
									map.put("cart_session", request.getSession(false).getAttribute("cart_session"));
									this.mCartViewTools.userInfo(user.getId(), map);// 用户属性
									this.mCartViewTools.buyNow(request, response, map, obj, area_abbr, area_id,
											CommUtil.null2String(user.getId()), gsp, color, CommUtil.null2Int(count),
											type, language); // 店铺信息
									code = 4200;
									msg = "Successfully";
								} else {
									code = 4207;
									msg = "The default address is empty";
									map.put("id", id);
									map.put("gsp", gsp);
									map.put("color", color);
									map.put("count", count);
									map.put("type", type);
									map.put("language", language);
								}
							}
						} else {
							code = 4206;
							msg = "Goods in short stock";
						}
					} else {
						code = 4203;
						msg = "You cannot buy goods from your own shop";
					}
				} else {
					code = 4208;
					msg = "The merchandise is off the shelves";
				}
			} else {
				code = -100;
			}
		} else {
			code = -100;
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	/**
	 * @description buyNow 订单修改
	 * @param request
	 * @param response
	 * @param id
	 * @param count
	 * @param goods_id
	 * @param integral
	 *            是否使用用户积分
	 * @descript 增加积分兑换金额
	 */
	@RequestMapping("v1/orderCountAdjust.json")
	public void orderCountAdjustV1(HttpServletRequest request, HttpServletResponse response, String token, String id,
			String count, String goods_id, String color, String gsp,
			@RequestParam(value = "cart_session", required = true) String cart_session, String storeCouponId,
			String generalCouponId, String type, String language) {
		Result result = null;
		Map map = new HashMap();
		int code = -1;
		String msg = "";
		double goods_price = 0; // 商品单价
		double store_goods_amount = 0;// 店铺优惠前商品总价
		double goods_amount = 0; // 商品总价--优惠后价格
		double store_total_price = 0; // 店铺总价--优惠后总金额
		double order_goods_amount = 0;// 订单商品总价 -- 店铺优惠计算结束价格(不包含运费，以便计算平台优惠活动)
		double order_total_price = 0; // 订单总价
		double order_discounts_amount = 0;// 订单优惠价格
		double ship_price = 0; // 运费

		User user = this.userService.getObjByProperty(null, "app_login_token", token);
		String cart_session1 = (String) request.getSession(false).getAttribute("cart_session");
		if (cart_session1 != null && !cart_session.equals("")) {
			if (cart_session1.equals(cart_session)) {
				Goods obj = this.goodsService.getObjById(Long.parseLong(goods_id));
				if (obj != null && obj.getGoods_store().getStore_status() == 15) {
					if (count == null && Integer.parseInt(count) < 1) {
						code = 4406;
						msg = "Purchase quantity must not be less than 1";
					} else {
						// 优先使用海外仓
						boolean inventory_very = true;
						int goods_inventory = 0;
						int oversea_inventory = 0;
						if (obj.getInventory_type().equals("all")) {
							goods_inventory = obj.getGoods_inventory();
							oversea_inventory = obj.getOversea_inventory();
						} else {
							Map<String, Object> goods = this.mCartViewTools.generic_default_info_color(obj, gsp, color);
							goods_inventory = CommUtil.null2Int(goods.get("count"));
							oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory"));
						}
						if (CommUtil.subtract(goods_inventory, count) > 0
								|| CommUtil.subtract(oversea_inventory, count) > 0) {
							if (obj.getInventory_type().equals("spec") && "".equals(color) && "".equals(gsp)) {
								code = 4402;
								msg = "Please select the product specifications";
							} else {
								Store store = obj.getGoods_store();
								map.put("store_enough_free", store.getEnough_free());
								map.put("store_enough_free_price", store.getEnough_free_price());
								Address address = this.addressService.getObjById(Long.parseLong(id));
								if (address != null) {
									Map priceMap = this.mCartViewTools.buyNowEnoughReducePrice(obj, gsp, color,
											CommUtil.null2Int(count), type);
									goods_price = CommUtil.null2Double(priceMap.get("goods_price"));
									store_goods_amount = CommUtil.null2Double(priceMap.get("goods_amount"));
									goods_amount = CommUtil.null2Double(priceMap.get("after_goods_amount"));
									store_total_price = goods_amount;
									// 满减
									if (obj.getEnough_reduce() == 1) {
										String er_id = obj.getOrder_enough_reduce_id();
										EnoughReduce enoughReduce = this.enoughReduceService
												.getObjById(CommUtil.null2Long(er_id));
										if (enoughReduce.getErbegin_time().before(new Date())) {
											Map erMap = new HashMap();
											erMap.put("er_tag", enoughReduce.getErtag());
											LinkedHashMap json = JSONObject.parseObject(enoughReduce.getEr_json(),
													LinkedHashMap.class, Feature.OrderedField);
											// JSONObject json =
											// JSONObject.parseObject(enoughReduce.getEr_json());
											erMap.put("er_json", json);
											erMap.put("er_price", priceMap.get("reduce"));
											erMap.put("after", priceMap.get("after"));
											order_discounts_amount += CommUtil.null2Double(priceMap.get("reduce"));
											map.put("enoughReduce", erMap);
										}
									}
									boolean point = true; // 商品为活动兑换商品时不在计算运费
									if (type != null && type.equals("get") && obj.getPoint() == 1
											|| obj.getGoods_status() == 4
													&& CommUtil.subtract(user.getPointNum(), obj.getPointNum()) > 0) { // 邀请兑换商品
										store_total_price = 0;
										order_total_price = 0;
										point = false;
									}
									// 匹配地址库
									boolean flag = true;// 改值为false时，用户地址无法送达--目前不匹配地址库
									/*
									 * if (this.configService.getSysConfig().
									 * getServer_version() == 2) { if
									 * (store.getTransport() != null &&
									 * store.getTransport().getExpress_company()
									 * != null) { if
									 * (store.getTransport().getExpress_company(
									 * ).getEnabled() == 1) { flag =
									 * this.mCartViewTools.matching(store,
									 * address.getArea().getParent().getAbbr());
									 * } } else { flag = false; } }
									 */
									map.put("flag", flag);
									// 商家优惠券信息
									boolean store_coupon = false;
									double store_coupon_price = 0;
									if (storeCouponId != null && !storeCouponId.equals("") && point) {
										CouponInfo couponInfo = this.couponInfoService
												.getObjById(CommUtil.null2Long(storeCouponId));
										if (couponInfo != null) {
											if (user.getId().equals(couponInfo.getUser().getId())) {
												if (CommUtil.subtract(store_total_price,
														couponInfo.getCoupon().getCoupon_order_amount()) >= 0) {
													store_coupon_price = CommUtil
															.null2Double(couponInfo.getCoupon().getCoupon_amount());
													order_discounts_amount += CommUtil.subtract(store_total_price,
															store_coupon_price) <= 0 ? store_total_price
																	: store_coupon_price;
													store_total_price = CommUtil.subtract(store_total_price,
															store_coupon_price) <= 0 ? 0
																	: CommUtil.subtract(store_total_price,
																			store_coupon_price);
												}
											}
										}
									}
									// 店铺优惠券列表
									List<Map> couponinfo = this.mCartViewTools.userCoupon(store.getId(),
											CommUtil.null2Double(store_total_price), user.getId());
									map.put("store_couponinfo", couponinfo);
									order_total_price = store_total_price;

									Map transMap = new HashMap();// 计算运费
									if (flag) {
										Area area = this.areaService.getObjById(address.getArea().getId());
										String volume = "";
										ship_price = this.transportTools.buyNowTransFree(request, response,
												CommUtil.null2String(obj.getId()), "express", volume,
												this.mCartViewTools.getCity(area), CommUtil.null2Int(count), gsp,
												color);
										transMap.put("ship_price", ship_price);
										transMap.put("trans", "express");
										map.put("transMap", transMap);
									}
									// 卖家承担运费这里用户支付页面不展示运费价格
									if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(),
											store_total_price) <= 0) {
										order_discounts_amount += ship_price;
										transMap.put("flag", 1);
									} else {
										store_total_price = CommUtil.add(store_total_price, ship_price);
										transMap.put("flag", 0);
									}

									List<Map> generalCouponinfo = this.mCartViewTools.userCoupon(null,
											order_total_price, user.getId());// 平台优惠券列表
									// 平台优惠券
									boolean self_coupon = false;
									double self_coupon_price = 0;
									if (generalCouponId != null && !generalCouponId.equals("")) {
										CouponInfo couponInfo = this.couponInfoService
												.getObjById(CommUtil.null2Long(generalCouponId));
										if (user.getId().equals(couponInfo.getUser().getId())) {
											if (CommUtil.subtract(store_total_price,
													couponInfo.getCoupon().getCoupon_order_amount()) >= 0) {

												self_coupon_price = CommUtil
														.null2Double(couponInfo.getCoupon().getCoupon_amount());

												order_discounts_amount += CommUtil.subtract(order_total_price,
														self_coupon_price) <= 0 ? order_total_price : self_coupon_price;

												order_total_price = CommUtil.subtract(order_total_price,
														self_coupon_price) <= 0 ? 0
																: CommUtil.subtract(order_total_price,
																		self_coupon_price);
											}
										}
									}
									map.put("store_goods_amount", store_goods_amount);
									map.put("order_goods_price", goods_amount);
									map.put("store_total_price", store_total_price);
									map.put("order_total_price", order_total_price);
									map.put("order_discounts_amount", order_discounts_amount);
									if (transMap.get("flag").toString().equals("1")) {
										map.put("pay_total_price", order_total_price);
									} else {
										if (transMap.get("flag").toString().equals("0")) {
											map.put("pay_total_price", CommUtil.add(order_total_price, ship_price));
										}
									}

									map.put("general_couponinfo", generalCouponinfo);
									code = 4200;
									msg = "Successfully";

								} else {
									code = 4207;
									msg = "Please select the shipping address";
								}
							}
						} else {
							code = 4206;
							msg = "Goods in short stock";
						}
					}
				} else {
					code = 4205;
					msg = "The merchandise is off the shelves";
				}
			} else {
				code = 4204;
				msg = "The order has expired";
			}
		} else {
			code = 4400;
			msg = "session为空";
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	/**
	 * @descriprion 立即购买-支付 订单生成和COD合并
	 * @param request
	 * @param response
	 * @param token
	 * @param goods_id
	 *            商品id
	 * @param color
	 *            商品颜色规格
	 * @param gsp
	 *            商品其他规格
	 * @param count
	 *            购买数量
	 * @param address_id
	 *            用户收货地址
	 * @param payType
	 *            支付类型 COD
	 * @param cart_session
	 *            支付会话
	 * @param storeCouponId
	 *            店铺优惠券
	 * @param generalCouponId
	 *            平台优惠券
	 * @param mobile
	 *            电话
	 * @param mobile_verify_code
	 *            验证码
	 * @param store_id
	 *            店铺id
	 * @param order_type
	 *            订单类型 app or web
	 */
	@EmailMapping(value = "payOnDelivery")
	@RequestMapping("v1/payOnDelivery.json")
	@ResponseBody
	public String payOnDelivery(HttpServletRequest request, HttpServletResponse response, String token, String goods_id,
			String color, String gsp, String count, String address_id, String payType, String cart_session,
			String storeCouponId, String generalCouponId, String mobile, String mobile_verify_code, String store_id,
			String order_type) {
		Result result = null;
		Map map = new HashMap();
		int code = -1;
		String msg = "";
		User user = null;
		if (!CommUtil.null2String(token).equals("")) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		if (user != null) {
			Store store = this.storeService.getObjById(Long.parseLong(store_id));
			String cart_session1 = (String) request.getSession(false).getAttribute("cart_session");
			if (CommUtil.null2String(cart_session1).equals(cart_session) && store.getStore_status() == 15) {
				Address address = this.addressService.getObjById(Long.parseLong(address_id));
				Goods obj = this.goodsService.getObjById(Long.parseLong(goods_id));
				if (address != null) {
					if (obj != null && obj.getGoods_status() == 0) {
						boolean inventory_very = true;
						boolean oversea_very = false;
						int goods_inventory = 0;
						int oversea_inventory = 0;
						// 判断优先使用海外仓
						if (obj.getInventory_type().equals("all")) {
							goods_inventory = obj.getGoods_inventory();
							oversea_inventory = obj.getOversea_inventory();
						} else {
							Map<String, Object> goods = this.mCartViewTools.generic_default_info_color(obj, gsp, color);
							goods_inventory = CommUtil.null2Int(goods.get("count"));
							oversea_inventory = CommUtil.null2Int(goods.get("oversea_inventory"));
						}
						if (CommUtil.subtract(oversea_inventory, count) > 0) {
							oversea_very = true;
						}
						if (CommUtil.subtract(goods_inventory, count) > 0
								|| CommUtil.subtract(oversea_inventory, count) > 0) {
							OrderForm main_order = null;
							/*
							 * if (payType.equals("payafter")) {// 使用货到付款 Map
							 * payafter_payTypemap = new HashMap(); String
							 * pay_session = CommUtil.randomString(32);
							 * request.getSession(false).setAttribute(
							 * "pay_session", pay_session);
							 * payafter_payTypemap.put("pay_session",
							 * pay_session); map.put("payafter_payTypemap",
							 * payafter_payTypemap); }
							 */
							double all_of_price = 0;
							request.getSession(false).removeAttribute("cart_session");// 删除订单提交唯一标示，避免重复提交
							// 封装商品信息
							List<Map> orderGoodsMap = new ArrayList<Map>();
							Map goodsMap = this.mCartViewTools.orderGoods(obj, count, gsp, color);
							orderGoodsMap.add(goodsMap);
							double goods_price = 0; // 商品单价
							double goods_amount = 0; // 商品总价
							double ship_price = 0; // 商品运费
							double store_total_price = 0;// 店铺总价
							double order_goods_amount = 0;// 订单商品总价 --
															// 店铺优惠计算结束价格(不包含运费，以便计算平台优惠活动)
															// 支付金额 = 订单商品总金额 +
															// 运费
							double order_total_price = 0;// 订单总价
							double payment_price = 0;// 订单支付金额
							double user_ship_price = 0; // 用户运费
							double store_ship_price = 0; // 店铺运费
							String transport_type = "-1";
							Map priceMap = this.mCartViewTools.buyNowEnoughReducePrice(obj, gsp, color,
									CommUtil.null2Int(count), null);// 满减
							goods_amount = CommUtil.null2Double(priceMap.get("after_goods_amount"));
							store_total_price = goods_amount;

							// 计算运费
							boolean flag = true;// 改值为false时，用户地址无法送达--目前不匹配地址库
							/*
							 * if(this.configService.getSysConfig().
							 * getServer_version() == 2){
							 * if(store.getTransport() != null &&
							 * store.getTransport().getExpress_company() !=
							 * null){
							 * if(store.getTransport().getExpress_company().
							 * getEnabled() == 1){ flag =
							 * this.mCartViewTools.matching(store,
							 * address.getArea().getParent().getAbbr()); }
							 * }else{ flag = false; } }
							 */
							map.put("flag", flag);

							/*
							 * boolean point = false; // 商品为活动兑换商品时不在计算运费 if
							 * (obj.getPoint() == 1 || obj.getGoods_status() ==
							 * 4 && CommUtil.subtract(user.getPointNum(),
							 * obj.getPointNum()) > 0) { // 邀请兑换商品
							 * store_total_price = 0; order_total_price = 0;
							 * point = true; } if (point) { double db =
							 * CommUtil.subtract(user.getPointNum(),
							 * obj.getPointNum()); user.setPointNum(new
							 * Double(db).intValue());
							 * this.userService.update(user); } && !point
							 */

							String er_string = null;
							// 满就减
							double reduce = 0;
							double after = 0;
							String er_json = "";
							if (obj.getEnough_reduce() == 1) {
								String er_id = obj.getOrder_enough_reduce_id();
								EnoughReduce enoughReduce = this.enoughReduceService
										.getObjById(CommUtil.null2Long(er_id));
								if (enoughReduce.getErbegin_time().before(new Date())) {
									Map erMap = new HashMap();// 满减信息
									er_json = enoughReduce.getEr_json();
									reduce = CommUtil.null2Double(priceMap.get("reduce"));// 商品满减价格
									after = CommUtil.null2Double(priceMap.get("after"));// 减后的商品价格
								}
							}

							// 商家优惠券信息
							boolean store_coupon = false;
							double store_coupon_price = 0;

							String store_coupon_info = "";
							if (storeCouponId != null && !storeCouponId.equals("")) {
								CouponInfo couponInfo = this.couponInfoService
										.getObjById(CommUtil.null2Long(storeCouponId));
								if (couponInfo != null) {
									if (user.getId().equals(couponInfo.getUser().getId())) {
										couponInfo.setStatus(1);
										this.couponInfoService.update(couponInfo);
										Map couponMap = new HashMap();
										couponMap.put("couponinfo_id", couponInfo.getId());
										couponMap.put("couponinfo_sn", couponInfo.getCoupon_sn());
										couponMap.put("coupon_amount", couponInfo.getCoupon().getCoupon_amount());
										double rate = CommUtil.div(couponInfo.getCoupon().getCoupon_amount(),
												goods_amount);
										couponMap.put("coupon_goods_rate", rate);
										store_coupon_info = Json.toJson(couponMap, JsonFormat.compact());
										store_coupon_price = CommUtil
												.null2Double(couponInfo.getCoupon().getCoupon_amount());
										if (CommUtil.subtract(store_total_price, store_coupon_price) > 0) {
											store_coupon = true;
										} else {
											store_total_price = CommUtil.subtract(store_total_price, goods_amount);
										}
									}
								}
							}
							order_goods_amount = store_total_price;
							// 计算运费
							String transPort = "";
							if (flag) {
								Map transMap = new HashMap();
								Area area = this.areaService.getObjById(address.getArea().getId());
								String volume = "";
								ship_price = this.transportTools.buyNowTransFree(request, response,
										CommUtil.null2String(obj.getId()), "express", volume,
										this.mCartViewTools.getCity(area), CommUtil.null2Int(count), gsp, color);
								transMap.put("ship_price", ship_price);
								transMap.put("trans", "express");
								transPort = "Express";
								map.put("transMap", transMap);
							}
							if (store.getEnough_free() == 1
									&& CommUtil.subtract(store.getEnough_free_price(), store_total_price) <= 0) {// 包邮
								store_total_price = goods_amount;
								user_ship_price = 0;
								store_ship_price = ship_price;
								transport_type = "1";
							} else {
								store_total_price = CommUtil.add(goods_amount, ship_price);
								user_ship_price = ship_price;
								store_ship_price = 0;
								transport_type = "0";
							}

							// 自营优惠券
							boolean self_coupon = false;
							double self_coupon_price = 0;
							String general_coupon_info = "";
							if (generalCouponId != null && !generalCouponId.equals("")) {
								CouponInfo couponInfo = this.couponInfoService
										.getObjById(CommUtil.null2Long(generalCouponId));
								if (user.getId().equals(couponInfo.getUser().getId())) {
									couponInfo.setStatus(1);
									this.couponInfoService.update(couponInfo);
									Map couponMap = new HashMap();
									couponMap.put("couponinfo_id", couponInfo.getId());
									couponMap.put("couponinfo_sn", couponInfo.getCoupon_sn());
									couponMap.put("coupon_amount", couponInfo.getCoupon().getCoupon_amount());
									couponMap.put("rate_price", couponInfo.getCoupon().getCoupon_amount());
									/*
									 * if(store_ids.length > 1){ double rate =
									 * CommUtil.div(couponInfo.getCoupon().
									 * getCoupon_amount(), goods_amount);
									 * couponMap.put("coupon_goods_rate", rate);
									 * }else{ couponMap.put("coupon_goods_rate",
									 * 1); } // double rate =
									 * CommUtil.div(couponInfo.getCoupon().
									 * getCoupon_amount(), goods_amount);
									 */
									couponMap.put("coupon_goods_rate", 1);
									general_coupon_info = Json.toJson(couponMap, JsonFormat.compact());
									self_coupon_price = CommUtil.null2Double(couponInfo.getCoupon().getCoupon_amount());
									if (CommUtil.subtract(order_goods_amount, self_coupon_price) > 0) {
										self_coupon = true;
									} else {
										order_goods_amount = CommUtil.subtract(order_goods_amount, goods_amount);
									}
								}
							}

							if (store_coupon) {
								order_goods_amount = CommUtil.subtract(order_goods_amount, store_coupon_price);
								store_total_price = CommUtil.subtract(store_total_price, store_coupon_price);
							}
							if (self_coupon) {
								order_goods_amount = CommUtil.subtract(order_goods_amount, self_coupon_price);
							}
							order_goods_amount = order_goods_amount >= 0 ? order_goods_amount : 0;
							order_total_price = CommUtil.add(order_goods_amount, ship_price);

							double commission_amount = this.mCartViewTools.getGoodsOrderCommission(obj,
									Integer.parseInt(count));// 订单佣金
							double goods_vat = CommUtil.mul(goods_amount, 0.05);// 服务VAT
							double commission_vat = CommUtil.mul(commission_amount, 0.05);// 佣金VAT
							// 创建订单
							OrderForm of = new OrderForm();

							of.setOrder_main(1);
							of.setAddTime(new Date());
							of.setStore_id(store_id);
							String SM = "SM" + CommUtil.randomString(5) + user.getId();
							of.setOrder_id(SM);
							// 设置收货地址信息
							of.setReceiver_Name(address.getTrueName());
							if (address.getArea().getLevel() == 2) {
								of.setReceiver_area(address.getArea().getParent().getParent().getAreaName() + " "
										+ address.getArea().getParent().getAreaName()
										+ address.getArea().getAreaName());
								of.setReceiver_state(address.getArea().getParent().getParent().getAreaName());
								of.setReceiver_city(address.getArea().getParent().getAreaName());
								of.setReceiver_street(address.getArea().getAreaName());
							} else if (address.getArea().getLevel() == 1) {
								of.setReceiver_area(
										address.getArea().getParent().getAreaName() + address.getArea().getAreaName());
								of.setReceiver_state(address.getArea().getParent().getAreaName());
								of.setReceiver_city(address.getArea().getAreaName());
								of.setReceiver_street("");
							}
							of.setReceiver_area_info(address.getArea_info());
							of.setReceiver_mobile(address.getMobile());
							of.setReceiver_telephone(address.getTelephone());
							of.setReceiver_zip(address.getZip());
							of.setReceiver_email(address.getEmail());
							of.setEnough_reduce_amount(BigDecimal.valueOf(reduce));
							of.setEnough_reduce_info(er_json);
							of.setTransport_type(transport_type);
							of.setTransport(transPort);
							of.setUser_id(user.getId().toString());
							of.setUser_name(user.getUserName());
							of.setGoods_info(Json.toJson(orderGoodsMap, JsonFormat.compact()));// 设置商品信息json数据
							of.setShip_price(BigDecimal.valueOf(user_ship_price));
							of.setStore_ship_price(BigDecimal.valueOf(store_ship_price));
							of.setGoods_amount(BigDecimal.valueOf(goods_amount));
							of.setTotalPrice(BigDecimal.valueOf(order_total_price));
							of.setCoupon_info(store_coupon_info);
							of.setGeneral_coupon(general_coupon_info);

							if (oversea_very) {
								Map params = new HashMap();
								params.put("repository", "1");
								List<ShipAddress> sa = this.shipAddressService.query(
										"select obj from ShipAddress obj where obj.repository=:repository", params, -1,
										-1);
								if (sa.size() > 0)
									of.setShip_addr_id(sa.get(0).getId());
							}

							/*
							 * // 商家优惠券信息 boolean store_coupon = false; double
							 * store_coupon_price = 0; if (storeCouponId != null
							 * && !storeCouponId.equals("")) { CouponInfo
							 * couponInfo = this.couponInfoService
							 * .getObjById(CommUtil.null2Long(storeCouponId));
							 * if (couponInfo != null) { if
							 * (user.getId().equals(couponInfo.getUser().getId()
							 * )) { couponInfo.setStatus(1);
							 * this.couponInfoService.update(couponInfo); Map
							 * couponMap = new HashMap();
							 * couponMap.put("couponinfo_id",
							 * couponInfo.getId());
							 * couponMap.put("couponinfo_sn",
							 * couponInfo.getCoupon_sn());
							 * couponMap.put("coupon_amount",
							 * couponInfo.getCoupon().getCoupon_amount());
							 * double rate =
							 * CommUtil.div(couponInfo.getCoupon().
							 * getCoupon_amount(), goods_amount);
							 * couponMap.put("coupon_goods_rate", rate);
							 * of.setCoupon_info(Json.toJson(couponMap,
							 * JsonFormat.compact()));
							 * 
							 * store_coupon_price = CommUtil
							 * .null2Double(couponInfo.getCoupon().
							 * getCoupon_amount()); if
							 * (CommUtil.subtract(order_total_price,
							 * store_coupon_price) > 0) { store_coupon = true; }
							 * else { order_total_price =
							 * CommUtil.subtract(order_total_price,
							 * goods_amount); } } } }
							 * 
							 * // 自营优惠券 boolean self_coupon = false; double
							 * self_coupon_price = 0; if (generalCouponId !=
							 * null && !generalCouponId.equals("")) { CouponInfo
							 * couponInfo = this.couponInfoService
							 * .getObjById(CommUtil.null2Long(generalCouponId));
							 * if
							 * (user.getId().equals(couponInfo.getUser().getId()
							 * )) { couponInfo.setStatus(1);
							 * this.couponInfoService.update(couponInfo); Map
							 * couponMap = new HashMap();
							 * couponMap.put("couponinfo_id",
							 * couponInfo.getId());
							 * couponMap.put("couponinfo_sn",
							 * couponInfo.getCoupon_sn());
							 * couponMap.put("coupon_amount",
							 * couponInfo.getCoupon().getCoupon_amount());
							 * couponMap.put("rate_price",
							 * couponInfo.getCoupon().getCoupon_amount());
							 * 
							 * if(store_ids.length > 1){ double rate =
							 * CommUtil.div(couponInfo.getCoupon().
							 * getCoupon_amount(), goods_amount);
							 * couponMap.put("coupon_goods_rate", rate); }else{
							 * couponMap.put("coupon_goods_rate", 1); } //
							 * double rate =
							 * CommUtil.div(couponInfo.getCoupon().
							 * getCoupon_amount(), goods_amount);
							 * 
							 * couponMap.put("coupon_goods_rate", 1);
							 * of.setGeneral_coupon(Json.toJson(couponMap,
							 * JsonFormat.compact())); self_coupon_price =
							 * CommUtil.null2Double(couponInfo.getCoupon().
							 * getCoupon_amount()); if
							 * (CommUtil.subtract(order_total_price,
							 * self_coupon_price) > 0) { self_coupon = true; }
							 * else { order_total_price =
							 * CommUtil.subtract(order_total_price,
							 * goods_amount); } } }
							 * 
							 * if (store_coupon) { order_total_price =
							 * CommUtil.subtract(order_total_price,
							 * store_coupon_price); store_total_price =
							 * CommUtil.subtract(store_total_price,
							 * store_coupon_price); } if (self_coupon) {
							 * order_total_price =
							 * CommUtil.subtract(order_total_price,
							 * self_coupon_price); } order_total_price =
							 * order_total_price >= 0 ? order_total_price : 0;
							 */

							of.setOrder_form(0);//
							of.setCommission_amount(BigDecimal.valueOf(commission_amount));// 该订单总体佣金费用
							of.setGoods_vat(BigDecimal.valueOf(goods_vat));
							of.setCommission_vat(BigDecimal.valueOf(commission_vat));
							of.setOrder_form(0);// 商家商品订单
							of.setOrder_cat(5);
							of.setStore_id(store.getId().toString());
							of.setStore_name(store.getStore_name());
							of.setOrder_type(order_type);
							/*
							 * if(Order_type != null){
							 * if(Order_type.indexOf("app") > 0){
							 * of.setOrder_type(Order_type); }else{
							 * of.setOrder_type("app-web");
							 * Order_type.indexOf("app-web"); } }
							 */
							/*
							 * String userAgent =
							 * request.getHeader("user-agent"); if (userAgent !=
							 * null && userAgent.indexOf("Android") > 0) {
							 * of.setOrder_type("Android");// 设置为APP订单 }else{
							 * if(userAgent != null &&
							 * userAgent.indexOf("iPhone") > 0){
							 * of.setOrder_type("iPhone");// 设置为APP订单 }else{
							 * of.setOrder_type("其他");// 设置为APP订单 } }
							 */

							of.setDelivery_type(0);
							boolean orderFlag = this.orderFormService.save(of);
							List<Long> cart_order_ids = new ArrayList<Long>();
							cart_order_ids.add(of.getId());
							map.put("cart_order_ids", cart_order_ids);
							// 封装订单日志
							OrderFormLog ofl = new OrderFormLog();
							ofl.setAddTime(new Date());
							of.setPay_msg("COD");
							of.setPayTime(new Date());
							of.setPayType("COD");
							of.setOrder_status(16);

							if (of.getPayType() != null && of.getPayType().equals("COD")) {
								// 如果买家支付方式为货到付款，买家确认收货时更新商品库存
								this.mCartViewTools.updateGoodsInventory(of);
								// 更新商品库存
							}

							StoreLog storeLog = this.storeLogTools
									.getTodayStoreLog(CommUtil.null2Long(of.getStore_id()));
							storeLog.setPlaceorder(storeLog.getPlaceorder() + 1);
							if (this.orderFormTools.queryOrder(of.getStore_name())) {
								storeLog.setRepetition(
										storeLog.getPlaceorder() == 1 ? 0 : storeLog.getRepetition() + 1);
							}
							this.storeLogService.update(storeLog);
							// 记录支付日志
							ofl.setAddTime(new Date());
							ofl.setLog_info("提交货到付款申请");
							ofl.setLog_user(user);
							ofl.setOf(of);
							this.orderFormLogService.save(ofl);
							String query = "select * from metoo_lucky_draw where switchs = 1";
							ResultSet res = this.databaseTools.selectIn(query);
							int lucky = 0;
							try {
								while (res.next()) {
									lucky = res.getInt("order");
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							user.setRaffle(user.getRaffle() + lucky);
							this.userService.update(user);
							request.getSession(false).removeAttribute("pay_session");
							map.put("msg", "Cash on delivery submitted successfully, waiting for delivery");
							map.put("raffle", lucky);
							final String id = CommUtil.null2String(of.getId());
							Thread t = new Thread(new Runnable() {
								private String order_id = id;

								public void run() {
									String email = "1223414075@qq.com,11943732@qq.com,460751446@qq.com";
									try {
										msgTools.sendEmail("", "email_tobuyer_order_submit_ok_notify", email, null,
												order_id, null);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
							t.start();
							code = 4200;
							msg = "Successfully";
						} else {
							code = 4206;
							msg = "Goods in short stock";
						}
					} else {
						code = 4205;
						msg = "The product has been taken off the shelves";
					}
				} else {
					code = 4207;
					msg = "Please select the shipping address";
				}
			} else {
				code = 4204;
				msg = "The order has expired";
			}
			// }else{
			// code = 4430;
			// msg = "验证码错误";
			// }
		} else {
			code = -100;
			msg = "User is not login";
		}
		return Json.toJson(new Result(code, msg, map));
	}

	// 弃用
	@RequestMapping("/payNow.json")
	// @RequestMapping("v1/payNow.json")
	public void buyNow2(HttpServletRequest request, HttpServletResponse response, String token, String goods_id,
			String color, String gsp, String count, String address_id, String payType, String cart_session,
			String storeCouponId, String generalCouponId, String mobile, String mobile_verify_code, String store_id,
			String Order_type) {
		Result result = null;
		Map map = new HashMap();
		int code = -1;
		String msg = "";
		User user = this.userService.getObjByProperty(null, "app_login_token", token);
		if (user != null) {
			/*
			 * String userMobile = mobile; String areaCode =
			 * userMobile.substring(0, 3);// 获取手机区号 Map mobileMap = new
			 * HashMap(); boolean mobileFlag =
			 * this.mobileTools.verify(userMobile); if(mobileFlag){ mobileMap =
			 * this.mobileTools.mobile(mobile); userMobile = (String)
			 * mobileMap.get("userMobile"); } //根据手机号获取验证码 VerifyCode mvc =
			 * this.mobileverifycodeService.getObjByProperty(null, "mobile",
			 * userMobile);
			 */
			/*
			 * if (mvc != null &&
			 * mvc.getCode().equalsIgnoreCase(mobile_verify_code)) {
			 * this.mobileverifycodeService.delete(mvc.getId());
			 */
			Store store = this.storeService.getObjById(Long.parseLong(store_id));
			String cart_session1 = (String) request.getSession(false).getAttribute("cart_session");
			if (cart_session1.equals(cart_session) && store.getStore_status() == 15) {
				Address address = this.addressService.getObjById(Long.parseLong(address_id));
				Goods obj = this.goodsService.getObjById(Long.parseLong(goods_id));
				if (obj != null) {
					if (address != null) {
						boolean inventory_very = true;
						int goods_inventory = CommUtil
								.null2Int(this.mCartViewTools.generic_default_info_color(obj, gsp, color).get("count"));// 计算商品库存信息
						if (goods_inventory == 0 && CommUtil.subtract(goods_inventory, count) < 0) {
							inventory_very = false;
						}
						if (inventory_very && Integer.parseInt(count) > 0) {
							OrderForm main_order = null;
							if (payType.equals("payafter")) {// 使用货到付款
								Map payafter_payTypemap = new HashMap();
								String pay_session = CommUtil.randomString(32);
								request.getSession(false).setAttribute("pay_session", pay_session);
								payafter_payTypemap.put("pay_session", pay_session);
								map.put("payafter_payTypemap", payafter_payTypemap);
							}
							double all_of_price = 0;
							request.getSession(false).removeAttribute("cart_session");// 删除订单提交唯一标示，避免重复提交
							// 封装商品信息
							List<Map> orderGoodsMap = new ArrayList<Map>();
							Map goodsMap = this.mCartViewTools.orderGoods(obj, count, gsp, color);
							orderGoodsMap.add(goodsMap);
							double goods_price = 0; // 商品单价
							double goods_amount = 0; // 商品总价
							double ship_price = 0; // 商品运费
							double store_total_price = 0;// 店铺总价
							double order_total_price = 0;// 订单总价
							double user_ship_price = 0; // 用户运费
							double store_ship_price = 0; // 店铺运费
							String transport_type = "-1";
							Map priceMap = this.mCartViewTools.buyNowEnoughReducePrice(obj, gsp, color,
									CommUtil.null2Int(count), null);// 满减
							goods_amount = CommUtil.null2Double(priceMap.get("after_goods_amount"));
							store_total_price = goods_amount;

							// 计算运费
							boolean flag = true;// 改值为false时，用户地址无法送达--目前不匹配地址库
							/*
							 * if(this.configService.getSysConfig().
							 * getServer_version() == 2){
							 * if(store.getTransport() != null &&
							 * store.getTransport().getExpress_company() !=
							 * null){
							 * if(store.getTransport().getExpress_company().
							 * getEnabled() == 1){ flag =
							 * this.mCartViewTools.matching(store,
							 * address.getArea().getParent().getAbbr()); }
							 * }else{ flag = false; } }
							 */
							map.put("flag", flag);

							boolean point = true; // 商品为活动兑换商品时不在计算运费
							if (obj.getPoint() == 1 || obj.getGoods_status() == 4) { // 邀请兑换商品
								store_total_price = 0;
								order_total_price = 0;
								point = false;
							}
							if (!false) {
								double db = CommUtil.subtract(user.getPointNum(), obj.getPointNum());
								user.setPointNum(new Double(db).intValue());
								this.userService.update(user);
							}

							String transPort = "";
							if (flag && point) {
								Map transMap = new HashMap();
								Area area = this.areaService.getObjById(address.getArea().getId());
								String volume = "";
								ship_price = this.transportTools.buyNowTransFree(request, response,
										CommUtil.null2String(obj.getId()), "express", volume, area.getAreaName(),
										CommUtil.null2Int(count), gsp, color);
								transMap.put("ship_price", ship_price);
								transMap.put("trans", "express");
								transPort = "Express";
								map.put("transMap", transMap);
							}

							if (store.getEnough_free() == 1
									&& CommUtil.subtract(store.getEnough_free_price(), goods_amount) <= 0) {// 包邮
								store_total_price = goods_amount;
								order_total_price = store_total_price;
								user_ship_price = 0;
								store_ship_price = ship_price;
								transport_type = "1";
							} else {
								store_total_price = CommUtil.add(goods_amount, ship_price);
								order_total_price = store_total_price;
								user_ship_price = ship_price;
								store_ship_price = 0;
								transport_type = "0";
							}

							String er_string = null;
							// 满就减
							double reduce = 0;
							double after = 0;
							String er_json = "";
							if (obj.getEnough_reduce() == 1) {
								String er_id = obj.getOrder_enough_reduce_id();
								EnoughReduce enoughReduce = this.enoughReduceService
										.getObjById(CommUtil.null2Long(er_id));
								if (enoughReduce.getErbegin_time().before(new Date())) {
									Map erMap = new HashMap();// 满减信息
									er_json = enoughReduce.getEr_json();
									reduce = CommUtil.null2Double(priceMap.get("reduce"));// 商品满减价格
									after = CommUtil.null2Double(priceMap.get("after"));// 减后的商品价格
								}
							}
							double commission_amount = this.mCartViewTools.getGoodsOrderCommission(obj,
									Integer.parseInt(count));// 订单佣金
							double goods_vat = CommUtil.mul(goods_amount, 0.05);// 服务VAT
							double commission_vat = CommUtil.mul(commission_amount, 0.05);// 佣金VAT
							// 创建订单
							OrderForm of = new OrderForm();
							of.setAddTime(new Date());
							of.setStore_id(store_id);
							String SM = "SM" + CommUtil.randomString(5) + user.getId();
							of.setOrder_id(SM);
							// 设置收货地址信息
							of.setReceiver_Name(address.getTrueName());
							of.setReceiver_area(address.getArea().getParent().getParent().getAreaName() + " "
									+ address.getArea().getParent().getAreaName() + " "
									+ address.getArea().getAreaName());
							of.setReceiver_area_info(address.getArea_info());
							of.setReceiver_state(address.getArea().getParent().getParent().getAreaName());
							of.setReceiver_city(address.getArea().getParent().getAreaName());
							of.setReceiver_street(address.getArea().getAreaName());
							of.setReceiver_mobile(address.getMobile());
							of.setReceiver_telephone(address.getTelephone());
							of.setReceiver_zip(address.getZip());
							of.setReceiver_email(address.getEmail());
							of.setEnough_reduce_amount(BigDecimal.valueOf(reduce));
							of.setEnough_reduce_info(er_json);
							of.setTransport_type(transport_type);
							of.setTransport(transPort);

							of.setOrder_status(10);
							of.setUser_id(user.getId().toString());
							of.setUser_name(user.getUserName());
							of.setGoods_info(Json.toJson(orderGoodsMap, JsonFormat.compact()));// 设置商品信息json数据
							of.setShip_price(BigDecimal.valueOf(user_ship_price));
							of.setStore_ship_price(BigDecimal.valueOf(store_ship_price));
							of.setGoods_amount(BigDecimal.valueOf(goods_amount));
							of.setTotalPrice(BigDecimal.valueOf(order_total_price));

							// 商家优惠券信息
							boolean store_coupon = false;
							double store_coupon_price = 0;
							if (storeCouponId != null && !storeCouponId.equals("")) {
								CouponInfo couponInfo = this.couponInfoService
										.getObjById(CommUtil.null2Long(storeCouponId));
								if (couponInfo != null) {
									if (user.getId().equals(couponInfo.getUser().getId())) {
										couponInfo.setStatus(1);
										this.couponInfoService.update(couponInfo);
										Map couponMap = new HashMap();
										couponMap.put("couponinfo_id", couponInfo.getId());
										couponMap.put("couponinfo_sn", couponInfo.getCoupon_sn());
										couponMap.put("coupon_amount", couponInfo.getCoupon().getCoupon_amount());
										double rate = CommUtil.div(couponInfo.getCoupon().getCoupon_amount(),
												goods_amount);
										couponMap.put("coupon_goods_rate", rate);
										of.setCoupon_info(Json.toJson(couponMap, JsonFormat.compact()));

										store_coupon_price = CommUtil
												.null2Double(couponInfo.getCoupon().getCoupon_amount());
										if (CommUtil.subtract(order_total_price, store_coupon_price) > 0) {
											store_coupon = true;
										} else {
											order_total_price = CommUtil.subtract(order_total_price, goods_amount);
										}
									}
								}
							}
							// 自营优惠券
							boolean self_coupon = false;
							double self_coupon_price = 0;
							if (generalCouponId != null && !generalCouponId.equals("")) {
								CouponInfo couponInfo = this.couponInfoService
										.getObjById(CommUtil.null2Long(generalCouponId));
								if (user.getId().equals(couponInfo.getUser().getId())) {
									couponInfo.setStatus(1);
									this.couponInfoService.update(couponInfo);
									Map couponMap = new HashMap();
									couponMap.put("couponinfo_id", couponInfo.getId());
									couponMap.put("couponinfo_sn", couponInfo.getCoupon_sn());
									couponMap.put("coupon_amount", couponInfo.getCoupon().getCoupon_amount());
									couponMap.put("rate_price", couponInfo.getCoupon().getCoupon_amount());
									/*
									 * if(store_ids.length > 1){ double rate =
									 * CommUtil.div(couponInfo.getCoupon().
									 * getCoupon_amount(), goods_amount);
									 * couponMap.put("coupon_goods_rate", rate);
									 * }else{ couponMap.put("coupon_goods_rate",
									 * 1); } // double rate =
									 * CommUtil.div(couponInfo.getCoupon().
									 * getCoupon_amount(), goods_amount);
									 */ couponMap.put("coupon_goods_rate", 1);
									of.setGeneral_coupon(Json.toJson(couponMap, JsonFormat.compact()));

									self_coupon_price = CommUtil.null2Double(couponInfo.getCoupon().getCoupon_amount());
									if (CommUtil.subtract(order_total_price, self_coupon_price) > 0) {
										self_coupon = true;
									} else {
										order_total_price = CommUtil.subtract(order_total_price, goods_amount);
									}
								}
							}

							if (store_coupon) {
								order_total_price = CommUtil.subtract(order_total_price, store_coupon_price);
								store_total_price = CommUtil.subtract(store_total_price, store_coupon_price);
							}
							if (self_coupon) {
								order_total_price = CommUtil.subtract(order_total_price, self_coupon_price);
							}
							order_total_price = order_total_price >= 0 ? order_total_price : 0;
							of.setTotalPrice(BigDecimal.valueOf(order_total_price));
							of.setOrder_form(0);//
							of.setCommission_amount(BigDecimal.valueOf(commission_amount));// 该订单总体佣金费用
							of.setGoods_vat(BigDecimal.valueOf(goods_vat));
							of.setCommission_vat(BigDecimal.valueOf(commission_vat));
							of.setOrder_form(0);// 商家商品订单
							of.setStore_id(store.getId().toString());
							of.setStore_name(store.getStore_name());
							of.setOrder_cat(5);
							if (Order_type != null) {
								if (Order_type.indexOf("app") > 0) {
									of.setOrder_type("app");
								} else {
									of.setOrder_type("app-web");
									Order_type.indexOf("app-web");
								}
							}
							/*
							 * String userAgent =
							 * request.getHeader("user-agent"); if (userAgent !=
							 * null && userAgent.indexOf("Android") > 0) {
							 * of.setOrder_type("Android");// 设置为APP订单 }else{
							 * if(userAgent != null &&
							 * userAgent.indexOf("iPhone") > 0){
							 * of.setOrder_type("iPhone");// 设置为APP订单 }else{
							 * of.setOrder_type("其他");// 设置为APP订单 } }
							 */

							// of.setOrder_type(userAgent);// 设置为APP订单
							of.setDelivery_type(0);
							boolean orderFlag = this.orderFormService.save(of);
							List<Long> cart_order_ids = new ArrayList<Long>();
							cart_order_ids.add(of.getId());
							map.put("cart_order_ids", cart_order_ids);
							map.put("order_num", of.getOrder_id());
							// 封装订单日志
							OrderFormLog ofl = new OrderFormLog();
							ofl.setAddTime(new Date());
							ofl.setOf(of);
							ofl.setLog_info("提交订单");
							ofl.setLog_user(user);
							this.orderFormLogService.save(ofl);
							code = 4200;
							msg = "Successfully";
						} else {
							code = 4206;
							msg = "Goods in short stock";
						}
					} else {
						code = 4207;
						msg = "Please select the shipping address";
					}
				} else {
					code = 4205;
					msg = "The merchandise is off the shelves";
				}
			} else {
				code = 4204;
				msg = "The order has expired";
			}
			// }else{
			// code = 4430;
			// msg = "验证码错误";
			// }
		} else {
			code = -100;
			msg = "User is not login";
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
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
