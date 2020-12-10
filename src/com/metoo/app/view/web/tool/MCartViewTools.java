package com.metoo.app.view.web.tool;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.Address;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.CheckCity;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.ShipAddress;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.IntegralViewTools;
import com.metoo.view.web.tools.StoreViewTools;

@Component
public class MCartViewTools {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private StoreViewTools storeTools;
	@Autowired
	private IActivityGoodsService actgoodsService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private UserTools userTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private MGoodsViewTools mgoodsViewTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IOrderFormService orderService;
	@Autowired
	private OrderFormTools orderTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICGoodsService cgoodsService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private StoreLogTools storeLogTools;

	/*
	 * if (erString != null && !erString.isEmpty()) { JSONObject json =
	 * JSONObject.parseObject((String) erString.get(key));
	 * map.put("store_enoughreduce", json); }
	 */

	public Map storeProperties(Store store) {
		if (null != store) {
			Map map = new HashMap();
			map.put("store_id", store.getId());
			map.put("store_name", store.getStore_name());
			map.put("store_status", store.getStore_status());
			map.put("store_logo",
					store.getStore_logo() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getStore_logo().getPath() + "/" + store.getStore_logo().getName());
			map.put("store_user_id", store.getUser().getId());
			map.put("store_enough_free", store.getEnough_free());
			map.put("store_enough_price", store.getEnough_free_price());
			return map;
		}
		return null;
	}

	/**
	 * @description 查询购物车商品
	 * @param carts
	 *            用户购物车列表
	 * @param ids
	 *            购物车所有店铺id
	 * @param erString
	 *            满减信息
	 * @param key·满减
	 * @param language
	 *            语言
	 * @return 1
	 */
	public List<Map<String, Object>> cartGoods(List<GoodsCart> carts, Set<Long> ids, Map erString, Long key,
			String language) {
		if (carts.size() > 0) {
			List<Map<String, Object>> normals = new ArrayList<Map<String, Object>>();
			for (Long id : ids) {
				List<Map<String, Object>> goodsList = this.cartsGoods(carts, id, language); // 购物车商品属性
				if (goodsList.size() > 0) {
					Store store = this.storeService.getObjById(CommUtil.null2Long(id));
					Map<String, Object> map = new HashMap<String, Object>();
					EnoughReduce enoughReduce = this.enoughReduceService.getObjById(key);
					if (enoughReduce != null) {
						if (null != store && enoughReduce.getStore_id().equals(store.getId().toString())) {
							map = this.storeProperties(store);
							if (erString != null && !erString.isEmpty()) {
								JSONObject json = JSONObject.parseObject((String) erString.get(key));
								map.put("store_enoughreduce", json);
							}
							map.put("goods", goodsList);
							normals.add(map);
						}
					} else {
						map = this.storeProperties(store);
						if (erString != null && !erString.isEmpty()) {
							JSONObject json = JSONObject.parseObject((String) erString.get(key));
							map.put("store_enoughreduce", json);
						}
						map.put("goods", goodsList);
						normals.add(map);
					}
				}
			}
			return normals;
		}
		return null;
	}

	/**
	 * @description 购物车商品属性
	 * @param carts
	 * @return
	 */
	public List<Map<String, Object>> cartsGoods(List<GoodsCart> carts, Long id, String language) {
		if (carts.size() > 0) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (GoodsCart obj : carts) {
				Goods goods = obj.getGoods();
				if (goods.getGoods_store().getId().equals(id)) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("goods_cart_id", obj.getId());
					map.put("goods_main_photo",
							obj.getGoods().getGoods_main_photo() == null ? ""
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ obj.getGoods().getGoods_main_photo().getPath() + "/"
											+ obj.getGoods().getGoods_main_photo().getName());
					map.put("goods_id", obj.getGoods().getId());
					map.put("goods_name", obj.getGoods().getGoods_name());
					map.put("goods_type", obj.getGoods().getGoods_type());
					if ("1".equals(language)) {
						map.put("goods_name",
								obj.getGoods().getKsa_goods_name() != null
										&& !"".equals(obj.getGoods().getKsa_goods_name())
												? "^" + obj.getGoods().getKsa_goods_name()
												: obj.getGoods().getGoods_name());
					}
					map.put("goods_goods_inventory", obj.getGoods().getGoods_inventory());
					map.put("total_price", CommUtil.mul(obj.getPrice(), obj.getCount()));
					map.put("goods_current_price", obj.getPrice());
					map.put("goods_price", obj.getGoods().getGoods_price());
					map.put("cart_count", obj.getCount());
					map.put("cart_type", obj.getCart_type());
					map.put("cart_status", obj.getCart_status());
					map.put("goods_spec", obj.getSpec_info() == null ? "" : obj.getSpec_info());
					map.put("goods_spec_color", obj.getColor() == null ? "" : obj.getColor());
					map.put("goods_status", obj.getGoods().getGoods_status());
					map.put("goods_collect", obj.getGoods().getGoods_collect());
					map.put("point_time", 0);
					if (obj.getGoods().getPoint() == 1 && obj.getGoods().getPoint_status() == 10) {
						int expiration = this.configService.getSysConfig().getExpiration_point();
						/*
						 * Calendar cal = Calendar.getInstance();
						 * cal.add(Calendar.DAY_OF_YEAR, -expiration);
						 * if(CommUtil.subtract(obj.getAddTime().getTime(),
						 * cal.getTime().getTime()) <= 0){ //超过设置时间将该购物车标记为已过期
						 * obj.setCart_status(2);
						 * this.goodsCartService.update(obj); }else{ Calendar
						 * calendar = Calendar.getInstance();
						 * calendar.setTime(obj.getAddTime());
						 * calendar.add(Calendar.DAY_OF_YEAR, expiration);
						 * Calendar calendar1 = Calendar.getInstance();
						 * calendar1.setTime(new Date()); Long millisecond =
						 * calendar.getTime().getTime() -
						 * calendar1.getTime().getTime();//毫秒
						 * map.put("point_time", millisecond); }
						 */
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(obj.getAddTime());
						calendar.add(Calendar.DAY_OF_YEAR, expiration);
						Calendar current_time = Calendar.getInstance();
						current_time.setTime(new Date());
						Long millisecond = calendar.getTime().getTime() - current_time.getTime().getTime();// 毫秒
						map.put("point_time", millisecond);
						map.put("goods_point", 1);
					} else {
						map.put("goods_point", 0);
					}
					map.put("goods_point",
							obj.getGoods().getPoint() == 1 && obj.getGoods().getPoint_status() == 10 ? 1 : 0);
					if (obj.getGoods().getInventory_type().equals("spec")) {
						int goods_inventory = CommUtil.null2Int(
								this.generic_default_info_color(obj.getGoods(), obj.getCart_gsp(), obj.getColor())
										.get("count"));// 计算商品库存信息
						map.put("goods_inventory", goods_inventory);
						double price = CommUtil.null2Double(
								this.generic_default_info_color(obj.getGoods(), obj.getCart_gsp(), obj.getColor())
										.get("goods_current_price"));// 计算商品库存信息
						double d2 = Math.round(price * 100) / 100.0;
						BigDecimal bd = new BigDecimal(d2);
						BigDecimal bd2 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
						map.put("goods_price", bd2);
						map.put("total_price", CommUtil.mul(bd2, obj.getCount()));
					}
					map.put("free_gifts", obj.getCart_type());
					list.add(map);
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * 根据商品及传递的规格信息，计算该规格商品的价格、库存量
	 * 
	 * @param goods
	 * @param gsp
	 * @return 价格、库存组成的Map
	 */
	public Map generic_default_info_color2(Goods goods, String gsp, String color) {
		double price = 0;
		Map map = new HashMap();
		int count = 0;
		String sku = "";
		int length = 0;
		int goods_width = 0;
		int goods_high = 0;
		int goods_weight = 0;
		if (goods.getGroup() != null && goods.getGroup_buy() == 2) {// 团购商品统一按照团购价格处理
			for (GroupGoods gg : goods.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(goods.getGroup().getId())) {
					count = gg.getGg_count();
					price = CommUtil.null2Double(gg.getGg_price());
				}
			}
		} else {
			count = goods.getGoods_inventory();
			price = CommUtil.null2Double(goods.getStore_price());
			if ("spec".equals(goods.getInventory_type())) {
				if (color != null && !color.equals("")) {
					List<CGoods> cgoodsList = goods.getCgoods();
					for (CGoods obj : cgoodsList) {
						String spec_id = obj.getCombination_id();
						String[] s_id = spec_id.split("_");
						String[] gsp_ids = gsp.split(",");
						Arrays.sort(gsp_ids);
						Arrays.sort(s_id);
						if (Arrays.equals(gsp_ids, s_id) && obj.getGoods_disabled().equals("0")) {
							price = CommUtil.null2Double(obj.getDiscount_price());
							count = obj.getGoods_inventory();
							sku = obj.getGoods_serial();
							length = obj.getGoods_length();
							goods_width = obj.getGoods_width();
							goods_high = obj.getGoods_high();
							goods_weight = obj.getGoods_weight();
						}
					}
				} else {
					if (gsp != null && !gsp.equals("")) {
						List<CGoods> cgoodsList = goods.getCgoods();
						for (CGoods obj : cgoodsList) {
							String spec_id = obj.getCombination_id();
							String[] s_id = spec_id.split("_");
							String[] gsp_ids = gsp.split(",");
							Arrays.sort(gsp_ids);
							Arrays.sort(s_id);
							if (Arrays.equals(gsp_ids, s_id) && obj.getGoods_disabled().equals("0")) {
								price = CommUtil.null2Double(obj.getDiscount_price());
								count = obj.getGoods_inventory();
								sku = obj.getGoods_serial();
								length = obj.getGoods_length();
								goods_width = obj.getGoods_width();
								goods_high = obj.getGoods_high();
								goods_weight = obj.getGoods_weight();
							}
						}
					}
				}
			}
		}
		BigDecimal ac_rebate = null;
		if (goods.getActivity_status() == 2 && SecurityUserHolder.getCurrentUser() != null) {// 如果是促销商品，并且用户已登录，根据规格配置价格计算相应配置的促销价格
			ActivityGoods actGoods = this.actgoodsService.getObjById(goods.getActivity_goods_id());
			// 0—铜牌会员1—银牌会员2—金牌会员3—超级会员
			BigDecimal rebate = BigDecimal.valueOf(0.00);
			int level = this.integralViewTools
					.query_user_level(CommUtil.null2String(SecurityUserHolder.getCurrentUser().getId()));
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
		map.put("sku", sku);
		map.put("length", length);
		map.put("goods_width", goods_width);
		map.put("goods_high", goods_high);
		map.put("goods_weight", goods_weight);
		return map;
	}

	/**
	 * 根据商品及传递的规格信息，计算该规格商品的价格、库存量
	 * 
	 * @param goods
	 * @param gsp
	 * @return 价格、库存组成的Map
	 */
	public Map<String, Object> generic_default_info_color(Goods goods, String gsp, String color) {
		Map<String, Object> map = new HashMap<String, Object>();
		int count = 0;
		String sku = "";
		int length = 0;
		int goods_width = 0;
		int goods_high = 0;
		int goods_weight = 0;
		double current_price = 0.0;
		double goods_price = 0.0;
		int oversea_inventory = 0;
		if (goods.getGroup() != null && goods.getGroup_buy() == 2) {// 团购商品统一按照团购价格处理
			for (GroupGoods gg : goods.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(goods.getGroup().getId())) {
					count = gg.getGg_count();
					current_price = CommUtil.null2Double(gg.getGg_price());
				}
			}
		} else {
			count = goods.getGoods_inventory();
			current_price = CommUtil.null2Double(goods.getGoods_current_price());
			if ("spec".equals(goods.getInventory_type())) {
				// if (color != null && !color.equals("")) {
				List<CGoods> cgoodsList = goods.getCgoods();
				for (CGoods obj : cgoodsList) {
					String spec_id = obj.getCombination_id();
					String[] s_id = spec_id.split("_");
					String[] gsp_ids = gsp.split(",");
					Arrays.sort(gsp_ids);
					Arrays.sort(s_id);
					if (Arrays.equals(gsp_ids, s_id) && obj.getGoods_disabled().equals("0")
							&& obj.getSpec_color().equals(color)) {
						goods_price = CommUtil.null2Double(obj.getGoods_price());
						current_price = CommUtil.null2Double(obj.getDiscount_price());
						count = obj.getGoods_inventory();
						sku = obj.getGoods_serial();
						length = obj.getGoods_length();
						goods_width = obj.getGoods_width();
						goods_high = obj.getGoods_high();
						goods_weight = obj.getGoods_weight();
						oversea_inventory = obj.getOversea_inventory();
						break;
					}
				}
				/*
				 * } else { if (gsp != null && !gsp.equals("")) { List<CGoods>
				 * cgoodsList = goods.getCgoods(); for (CGoods obj : cgoodsList)
				 * { String spec_id = obj.getCombination_id(); String[] s_id =
				 * spec_id.split("_"); String[] gsp_ids = gsp.split(",");
				 * Arrays.sort(gsp_ids); Arrays.sort(s_id); if
				 * (Arrays.equals(gsp_ids, s_id) &&
				 * obj.getGoods_disabled().equals("0")) { price =
				 * CommUtil.null2Double(obj.getDiscount_price()); count =
				 * obj.getGoods_inventory(); sku = obj.getGoods_serial(); length
				 * = obj.getGoods_length(); goods_width = obj.getGoods_width();
				 * goods_high = obj.getGoods_high(); goods_weight =
				 * obj.getGoods_weight(); break; } } }else{ count = -1; } }
				 */
			}
		}
		BigDecimal ac_rebate = null;
		if (goods.getActivity_status() == 2 && SecurityUserHolder.getCurrentUser() != null) {// 如果是促销商品，并且用户已登录，根据规格配置价格计算相应配置的促销价格
			ActivityGoods actGoods = this.actgoodsService.getObjById(goods.getActivity_goods_id());
			// 0—铜牌会员1—银牌会员2—金牌会员3—超级会员
			BigDecimal rebate = BigDecimal.valueOf(0.00);
			int level = this.integralViewTools
					.query_user_level(CommUtil.null2String(SecurityUserHolder.getCurrentUser().getId()));
			if (level == 0) {
				rebate = actGoods.getAct().getAc_rebate();
			} else if (level == 1) {
				rebate = actGoods.getAct().getAc_rebate1();
			} else if (level == 2) {
				rebate = actGoods.getAct().getAc_rebate2();
			} else if (level == 3) {
				rebate = actGoods.getAct().getAc_rebate3();
			}
			current_price = CommUtil.mul(rebate, current_price);
		}
		map.put("goods_price", goods_price);
		map.put("goods_current_price", current_price);
		map.put("count", count);
		map.put("sku", sku);
		map.put("length", length);
		map.put("goods_width", goods_width);
		map.put("goods_high", goods_high);
		map.put("goods_weight", goods_weight);
		map.put("oversea_inventory", oversea_inventory);
		return map;
	}

	/**
	 * 提交购物车获取用户地址
	 * 
	 * @param address
	 * @return
	 */
	public List<Map> queryAddress(List<Address> address) {
		List<Map> addrlist = new ArrayList<Map>();
		Map map = null;
		for (Address obj : address) {
			map = new HashMap();
			map.put("id", obj.getId());
			map.put("userName", obj.getTrueName());
			map.put("telephone", obj.getTelephone());
			map.put("zip", obj.getZip());
			map.put("mobile", obj.getMobile());
			map.put("defaultVal", obj.getDefault_val());
			if (obj.getArea() != null) {
				Area area = this.areaService.getObjById(obj.getArea().getId());
				map.put("area_id", CommUtil.null2String(area.getId()));
				map.put("areaInfo", obj.getArea_info());
				map.put("area_abbr", area.getParent().getAbbr());
				if (area.getLevel() == 2) {
					map.put("country", obj.getArea().getParent().getParent().getAreaName());
					map.put("city", obj.getArea().getParent().getAreaName());
					map.put("area", obj.getArea().getAreaName());
				} else if (area.getLevel() == 1) {
					map.put("country", obj.getArea().getParent().getAreaName());
					map.put("city", obj.getArea().getAreaName());
					map.put("area", "");
				}
			}
			addrlist.add(map);
		}
		return addrlist;
	}

	/**
	 * @description BuyNow用户默认地址
	 * @param obj
	 * @return
	 */
	public Map orderAddress(Address obj) {
		if (obj != null) {
			Map map = new HashMap();
			map.put("id", obj.getId());
			map.put("userName", obj.getTrueName());
			map.put("telephone", obj.getTelephone());
			map.put("zip", obj.getZip());
			map.put("mobile", obj.getMobile());
			map.put("defaultVal", obj.getDefault_val());
			if (obj.getArea() != null) {
				Area area = this.areaService.getObjById(obj.getArea().getId());
				map.put("area_id", CommUtil.null2String(area.getId()));
				map.put("areaInfo", obj.getArea_info());
				map.put("area_abbr", area.getParent().getAbbr());
				if (area.getLevel() == 2) {
					map.put("country", obj.getArea().getParent().getParent().getAreaName());
					map.put("city", obj.getArea().getParent().getAreaName());
					map.put("area", obj.getArea().getAreaName());
				} else if (area.getLevel() == 1) {
					map.put("country", obj.getArea().getParent().getAreaName());
					map.put("city", obj.getArea().getAreaName());
					map.put("area", "");
				}
			}
			return map;
		}
		return null;
	}

	/**
	 * @description 立即购买--商品信息
	 * @param request
	 * @param response
	 * @param map
	 * @param obj
	 *            商品对象
	 * @param area_abbr
	 *            城市代码
	 * @param area_id
	 *            区域ID
	 * @param user_id
	 *            用户ID
	 * @param color
	 *            商品规格-颜色
	 * @param gsp
	 *            商品规格-规格值ID
	 */
	public void buyNow(HttpServletRequest request, HttpServletResponse response, Map map, Goods obj, String area_abbr,
			String area_id, String user_id, String gsp, String color, int count, String type, String language) {
		double order_total_price = 0; // 订单总价格
		double store_total_price = 0;// 店铺总价格
		double goods_price = 0; // 商品单价
		double goods_amount = 0; // 商品总价
		double ship_price = 0; // 商品运费
		Date date = new Date();
		Store store = obj.getGoods_store();
		Map<String, Object> storeMap = this.storeTools.queryStore(store);
		// 匹配地址库
		boolean flag = true;// 改值为false时，用户地址无法送达--目前不匹配地址库
		/*
		 * if(this.configService.getSysConfig().getServer_version() == 2){
		 * if(store.getTransport() != null &&
		 * store.getTransport().getExpress_company() != null){
		 * if(store.getTransport().getExpress_company().getEnabled() == 1){ flag
		 * = this.matching(store, area_abbr); } }else{ flag = false; } }
		 */
		boolean point = true; // 兑换活动商品 obj.getGoods_status() == 4
		/*
		 * if ("get".equals(type)) { if (obj.getPoint() == 1 &&
		 * obj.getPoint_status() == 10) { Ponint
		 * this.pointService.getObjById(CommUtil.null2Long(obj.getPoint_id()0));
		 * } }
		 */
		if (type != null && type.equals("get") && obj.getPoint() == 1 || obj.getGoods_status() == 4) { // 邀请兑换商品
			store_total_price = 0;
			order_total_price = 0;
			point = false;
		}
		storeMap.put("flag", flag);
		// 商品信息
		Map goodsMap = this.mgoodsViewTools.orderGoods(obj, count, gsp, color, point, language);
		storeMap.put("goods_amount", goods_amount);
		storeMap.put("goodsMap", goodsMap);
		Map priceMap = this.buyNowEnoughReducePrice(obj, gsp, color, count, type);
		goods_amount = CommUtil.null2Double(priceMap.get("after_goods_amount"));
		goods_price = CommUtil.null2Double(priceMap.get("goods_price"));
		// 满就减
		if (obj.getEnough_reduce() == 1) {
			String er_id = obj.getOrder_enough_reduce_id();
			EnoughReduce enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
			if (enoughReduce.getErbegin_time().before(date)) {
				Map<String, Object> erMap = new HashMap<String, Object>();// 满减信息
				erMap.put("er_tag", enoughReduce.getErtag());
				JSONObject json = JSONObject.parseObject(enoughReduce.getEr_json());
				erMap.put("er_json", json);
				erMap.put("er_price", priceMap.get("reduce"));
				erMap.put("after", priceMap.get("after"));
				storeMap.put("enoughReduce", erMap);
			}
		}
		// 计算运费
		Map<String, Object> transMap = new HashMap<String, Object>();
		if (flag && point) {
			Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
			String volume = "";
			ship_price = this.transportTools.buyNowTransFree(request, response, CommUtil.null2String(obj.getId()),
					"express", volume, this.getCity(area), count, gsp, color);
			transMap.put("ship_price", ship_price);
			transMap.put("trans", "express");
			storeMap.put("transMap", transMap);
		}
		// 计算满包邮
		if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(), goods_amount) < 0) {
			store_total_price = goods_amount;
			order_total_price = store_total_price;
			transMap.put("flag", 1);
		} else {
			store_total_price = CommUtil.add(goods_amount, ship_price);
			order_total_price = store_total_price;
			transMap.put("flag", 0);
		}
		if (point) {
			// 查询该订单对应的商家优惠券
			List<Map> couponinfo = this.userCoupon(store.getId(), CommUtil.null2Double(goods_amount),
					CommUtil.null2Long(user_id));
			storeMap.put("Couponinfo", couponinfo);
			double price = 0.0;
			if (couponinfo.size() > 0) {
				for (Object key : couponinfo.get(0).keySet()) {
					if (CommUtil.null2String(key).equals("goods_price")) {
						price = CommUtil.null2Double(couponinfo.get(0).get(key));
					}

				}
			}
			// 查询全网通用优惠券 -- 优先使用店铺优惠券，查询可使用优惠券，并过滤使用店铺优惠券后的平台优惠券
			List<Map> generalCouponinfo = this.couponList(null, goods_amount, CommUtil.null2Long(user_id), price);
			map.put("Couponinfo", generalCouponinfo);
			map.put("store_name", "Soarmall");
		}
		storeMap.put("store_total_price", store_total_price);
		storeMap.put("order_total_price", order_total_price);
		storeMap.put("order_goods_price", goods_price);
		map.put("storeMap", storeMap);
	}

	// 当前版本不在匹配地址库 -- 平台满包邮
	public void buyNowV2(HttpServletRequest request, HttpServletResponse response, Map map, Goods obj, String area_abbr,
			String area_id, String user_id, String gsp, String color, int count, String type, String language) {
		double goods_price = 0; // 商品单价
		double store_goods_amount = 0; // 商品总额
		double store_total_price = 0;// 店铺总额 不包含运费额
		double ship_price = 0; // 店铺运费额
		double store_shipping_included = 0; // 活动后含运费店铺总价
		double store_exclude_freight = 0; // 不含运费价格
		double order_ship_price = 0;// 平台满包邮运费总额
		double ship_subsidy_price = 0;// 平台满包邮与店铺总价差价
		double order_total_price = 0; // 订单总额
		double order_free_express_price = 0;// 平台满包可使用订单金额

		Date date = new Date();
		Store store = obj.getGoods_store();
		Map<String, Object> storeMap = this.storeTools.queryStoreV2(store);
		boolean point = true; // 兑换活动商品 obj.getGoods_status() == 4
		// 商品信息
		Map goodsMap = this.mgoodsViewTools.orderGoodsV2(obj, count, gsp, color, language);
		Map priceMap = this.buyNowEnoughReducePrice(obj, gsp, color, count, type);
		goods_price = CommUtil.null2Double(priceMap.get("goods_price"));
		store_goods_amount = CommUtil.null2Double(priceMap.get("goods_amount"));
		store_total_price = CommUtil.null2Double(priceMap.get("after_goods_amount"));
		storeMap.put("goodsMap", goodsMap);
		// 满减
		if (obj.getEnough_reduce() == 1) {
			String er_id = obj.getOrder_enough_reduce_id();
			EnoughReduce enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
			if (enoughReduce.getErbegin_time().before(date)) {
				Map<String, Object> erMap = new HashMap<String, Object>();// 满减信息
				erMap.put("er_tag", enoughReduce.getErtag());
				JSONObject json = JSONObject.parseObject(enoughReduce.getEr_json());
				erMap.put("er_json", json);
				erMap.put("er_price", priceMap.get("reduce"));
				erMap.put("after", priceMap.get("after"));
				storeMap.put("enoughReduce", erMap);
			}
		}
		store_exclude_freight = store_total_price;
		// 运费
		Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
		String volume = "";
		ship_price = this.transportTools.buyNowTransFree(request, response, CommUtil.null2String(obj.getId()),
				"express", volume, this.getCity(area), count, gsp, color);
		store_shipping_included = CommUtil.add(store_exclude_freight, ship_price);// 含运费店铺总价
		// 商家优惠券
		List<Map> couponinfo = this.userCoupon(store.getId(), store_total_price, CommUtil.null2Long(user_id));
		storeMap.put("Couponinfo", couponinfo);
		// 平台优惠券
		List<Map> generalCouponinfo = this.userCoupon(null, store_total_price, CommUtil.null2Long(user_id));
		map.put("Couponinfo", generalCouponinfo);

		// 满包邮 （ 平台）
		if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
			// order_ship_price = CommUtil.subtract(ship_price, ship_price * 2);
			if (CommUtil.subtract(store_exclude_freight,
					this.configService.getSysConfig().getEnoughfree_price()) >= 0) {
				order_total_price = store_total_price;
				order_ship_price = 0;
			} else {
				order_total_price = CommUtil.add(store_total_price, ship_price);
				ship_subsidy_price = CommUtil.subtract(this.configService.getSysConfig().getEnoughfree_price(),
						store_exclude_freight);
				order_ship_price = ship_price;
			}
		} else if (store.getEnough_free() == 1
				&& CommUtil.subtract(store.getEnough_free_price(), store_total_price) <= 0) {
			order_total_price = store_total_price;
			order_ship_price = 0;
		} else {
			store_total_price = CommUtil.add(store_total_price, ship_price);
			order_total_price = store_total_price;
			order_ship_price = ship_price;
			ship_subsidy_price = CommUtil.subtract(this.configService.getSysConfig().getEnoughfree_price(),
					store_exclude_freight);

		}

		storeMap.put("store_goods_amount", store_goods_amount);
		storeMap.put("store_total_price", store_total_price);
		storeMap.put("ship_price", ship_price);
		storeMap.put("order_ship_price", order_ship_price);
		storeMap.put("ship_subsidy_price", ship_subsidy_price);
		storeMap.put("order_total_price", order_total_price);
		map.put("storeMap", storeMap);
	}

	/**
	 * 根据不同国家地址库获取用户收货地址城市
	 * 
	 * @param area
	 * @return
	 */
	public String getCity(Area area) {
		String city = "";
		if (null != area) {
			if (area.getLevel() == 2) {
				city = area.getParent().getAreaName();
			} else if (area.getLevel() == 1) {
				city = area.getAreaName();
			}
			return city;
		}
		return null;
	}

	/**
	 * 
	 * @param map_list
	 *            店铺信息集合
	 * @param area_abbr
	 *            城市代码
	 * @param area_id
	 *            收货地址区域
	 * @param user_id
	 *            用户
	 * @param cartMap
	 *            购物车集合
	 * @param language
	 * 
	 * @decript 购物车商品
	 */
	public void goods_cart2(List map_list, String area_abbr, String area_id, String user_id, Map cartMap,
			String language) {
		// Map map = new HashMap();
		List<Map> orderlist = new ArrayList<Map>();
		double order_tota_price = 0;// 订单总价
		double store_total_price = 0;// 店铺总价
		double store_order_tota_price = 0;
		double goods_amount = 0; // 商品总价
		double general_goods_amount = 0;
		boolean enable = true;
		for (int i = 0; i < map_list.size(); i++) {
			Map storeMap = new HashMap();
			Map map_info = (Map) map_list.get(i);
			Store store = storeService.getObjById(CommUtil.null2Long(map_info.get("store_id")));
			try {
				if (map_info.get("store_id") == "self") {
					storeMap.put("store_id", "self");
					storeMap.put("OnLine", userTools.adminOnLine());
				} else {
					storeMap.put("store_id", store.getId());
					storeMap.put("store_name", store.getStore_name());
					storeMap.put("store_enough_free", store.getEnough_free());
					storeMap.put("store_enough_free_price", store.getEnough_free_price());
					storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
					// 匹配地址库--目前不匹配地址库
					/*
					 * if(this.configService.getSysConfig().getServer_version()
					 * == 2){ if(store.getTransport() != null &&
					 * store.getTransport().getExpress_company() != null){
					 * if(store.getTransport().getExpress_company().getEnabled()
					 * == 1){ enable = matching(store , area_abbr);
					 * storeMap.put("flag", enable); }else{ storeMap.put("flag",
					 * enable); } }else{ storeMap.put("flag", false); } }else{
					 * storeMap.put("flag", enable); }
					 */
					storeMap.put("flag", enable);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			List<GoodsCart> carts = (List<GoodsCart>) map_info.get("gc_list");
			boolean delivery = false;
			List goodsList = this.cartGoods(carts, language);
			storeMap.put("gc_list", goodsList);
			Map<Long, List<GoodsCart>> er_map = null;// 满减
			List er_goodsList = new ArrayList();
			if (map_info.get("er_goods") != null) {
				List<GoodsCart> goodscart_er = new ArrayList<GoodsCart>();
				er_map = (Map<Long, List<GoodsCart>>) map_info.get("er_goods");
				List er_map_list = new ArrayList();
				for (Long key : er_map.keySet()) {
					goodscart_er = er_map.get(key);
					Map reduce_map = new HashMap();
					Map map = (Map) map_info.get("erString");
					reduce_map.put("er_str", map.get(key));
					reduce_map.put("er_price", map_info.get("store_enough_reduce"));
					er_goodsList = this.cartGoods(goodscart_er, language);
					reduce_map.put("er_goods", er_goodsList);
					Map ermap = (Map) map_info.get("er_json");
					reduce_map.put("er_json", ermap.get(key));
					er_map_list.add(reduce_map);
				}
				storeMap.put("enoughReduce", er_map_list);
			}
			Map<Goods, List<GoodsCart>> goods_ac_map = null;// 满就送
			if (map_info.get("ac_goods") != null) {
				List ac_info_list = new ArrayList();
				List<GoodsCart> goodscart_ac = new ArrayList<GoodsCart>();
				goods_ac_map = (Map<Goods, List<GoodsCart>>) map_info.get("ac_goods");
				for (Goods key : goods_ac_map.keySet()) {
					goodscart_ac = goods_ac_map.get(key);
					List ac_goodsList = this.cartGoods(goodscart_ac, language);
					storeMap.put("ac_goodsList", ac_goodsList);
				}
			}
			Object ship_price = 0.00;
			goods_amount = (double) map_info.get("store_goods_price");// CommUtil.add(goods_amount,
																		// map_info.get("store_goods_price"));
			// delivery == true 计算运费
			if (enable) {
				List<SysMap> list = new ArrayList<SysMap>();
				list = transportTools.transportation_cart2(carts, er_map, goods_ac_map, area_id);
				List sys_info = new ArrayList();
				Map sysmap = new HashMap();
				for (SysMap obj : list) {
					if (obj.getKey().equals("Express")) {
						ship_price = obj.getValue();
					}
				}
				sysmap.put("ship_price", ship_price);
				sys_info.add(sysmap);
				storeMap.put("sys_info", sys_info);
			}
			// 计算满包邮
			if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(), goods_amount) <= 0) {
				store_total_price = CommUtil.null2Double(map_info.get("store_goods_price"));// 店铺总价
				order_tota_price = CommUtil.add(order_tota_price,
						CommUtil.null2Double(map_info.get("store_goods_price")));// 订单总价
			} else {
				store_total_price = CommUtil.add(ship_price, map_info.get("store_goods_price"));
				order_tota_price = CommUtil.add(order_tota_price, ship_price)
						+ CommUtil.null2Double(map_info.get("store_goods_price"));
			}
			general_goods_amount = CommUtil.add(general_goods_amount, goods_amount);
			storeMap.put("goods_amount", goods_amount);
			storeMap.put("store_total_price", store_total_price);
			// 查询用户对应的商家优惠券【调整】
			List<Map> couponinfo = this.userCoupon(store.getId(),
					CommUtil.null2Double(map_info.get("store_goods_price")), CommUtil.null2Long(user_id));
			storeMap.put("Couponinfo", couponinfo);
			orderlist.add(storeMap);
			cartMap.put("orderlist", orderlist);
			cartMap.put("order_tota_price", order_tota_price);
		}
		/*
		 * cart_map.put("order_tota_price",
		 * CommUtil.null2BigDecimal(map.get("order_tota_price")));
		 * cart_map.put("orderlist", map);
		 */
		// 全网通用优惠券
		List<Map> generalCouponinfo = this.userCoupon(null, general_goods_amount, CommUtil.null2Long(user_id));
		cartMap.put("Couponinfo", generalCouponinfo);
		cartMap.put("store_name", "Soarmall");
	}

	/**
	 * 
	 * @param map_list
	 *            店铺信息集合
	 * @param area_abbr
	 *            城市代码
	 * @param area_id
	 *            收货地址区域
	 * @param user_id
	 *            用户
	 * @param cartMap
	 *            购物车集合
	 * @param language
	 * 
	 * @decript 购物车商品
	 */
	public void cart_order(List map_list, String area_id, String user_id, Map cartMap, String language) {
		double store_goods_amount = 0;// 店铺商品总价
		double goods_amount = 0; // 商品总价
		double store_total_price = 0;// 店铺总价--不包含店铺运费
		double store_exclude_freight = 0;// 优惠后不含运费价格
		double ship_price = 0; // 店铺运费
		double store_shipping_included = 0;// 优惠后含运费店铺总价
		double order_goods_amount = 0;// 优惠后订单商品总价
		double order_total_price = 0;// 订单总价--不包含店铺运费
		double ship_subsidy_price = 0;// 平台满包邮与店铺总价差价
		double order_ship_price = 0;// 平台满包邮价格
		double order_free_express_price = 0;// 平台满包可使用订单金额
		List<Map> orderlist = new ArrayList<Map>();
		for (int i = 0; i < map_list.size(); i++) {
			Map storeMap = new HashMap();
			Map map_info = (Map) map_list.get(i);
			Store store = storeService.getObjById(CommUtil.null2Long(map_info.get("store_id")));
			try {
				if (store != null) {
					storeMap.put("store_id", store.getId());
					storeMap.put("store_name", store.getStore_name());
					storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
				} else {
					storeMap.put("store_id", "self");
					storeMap.put("OnLine", userTools.adminOnLine());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			List<GoodsCart> carts = (List<GoodsCart>) map_info.get("gc_list");// 无活动商品
			boolean delivery = false;
			List goodsList = this.cartGoodsV2(carts, language);
			storeMap.put("gc_list", goodsList);
			Map<Long, List<GoodsCart>> er_map = null;// 满减
			List er_goodsList = new ArrayList();
			if (map_info.get("er_goods") != null) {
				List<GoodsCart> goodscart_er = new ArrayList<GoodsCart>();
				er_map = (Map<Long, List<GoodsCart>>) map_info.get("er_goods");
				List er_map_list = new ArrayList();
				for (Long key : er_map.keySet()) {
					goodscart_er = er_map.get(key);
					Map reduce_map = new HashMap();
					Map map = (Map) map_info.get("erString");
					reduce_map.put("er_str", map.get(key));
					reduce_map.put("er_price", map_info.get("store_enough_reduce"));
					er_goodsList = this.cartGoodsV2(goodscart_er, language);
					reduce_map.put("er_goods", er_goodsList);
					Map ermap = (Map) map_info.get("er_json");
					reduce_map.put("er_json", ermap.get(key));
					er_map_list.add(reduce_map);
				}
				storeMap.put("enoughReduce", er_map_list);
			}
			store_goods_amount = (double) map_info.get("store_goods_amount");// 店铺商品总价
			store_total_price = (double) map_info.get("store_total_price");// 店铺总价--不含运费
			order_goods_amount = CommUtil.add(order_goods_amount, store_total_price);// 订单商品总价--满减优惠后
			Map<Goods, List<GoodsCart>> goods_ac_map = null;// 满就送
			if (map_info.get("ac_goods") != null) {
				List ac_info_list = new ArrayList();
				List<GoodsCart> goodscart_ac = new ArrayList<GoodsCart>();
				goods_ac_map = (Map<Goods, List<GoodsCart>>) map_info.get("ac_goods");
				for (Goods key : goods_ac_map.keySet()) {
					goodscart_ac = goods_ac_map.get(key);
					List ac_goodsList = this.cartGoodsV2(goodscart_ac, language);
					storeMap.put("ac_goodsList", ac_goodsList);
				}
			}
			goods_amount = (double) map_info.get("store_goods_price");// CommUtil.add(goods_amount,
																		// 待查询
			// 查询用户对应的商家优惠券【调整】
			List<Map> couponinfo = this.userCoupon(store.getId(), store_total_price, CommUtil.null2Long(user_id));
			storeMap.put("Couponinfo", couponinfo);
			store_exclude_freight = store_total_price;

			// 运费
			List<SysMap> list = new ArrayList<SysMap>();
			list = transportTools.transportation_cart2(carts, er_map, goods_ac_map, area_id);
			Map sysmap = new HashMap();
			for (SysMap obj : list) {
				if (obj.getKey().equals("Express")) {
					ship_price = CommUtil.null2Double(obj.getValue());
				}
			}
			store_shipping_included = CommUtil.add(store_exclude_freight, ship_price);// 含运费店铺总价
			order_ship_price = CommUtil.add(order_ship_price, ship_price);// 订单运费总额
			storeMap.put("goods_amount", goods_amount);// 待查询
			storeMap.put("store_goods_amount", store_goods_amount);
			storeMap.put("store_total_price", store_total_price);
			storeMap.put("store_shipping_included", store_shipping_included);
			storeMap.put("ship_price", ship_price);
			orderlist.add(storeMap);
		}
		// 平台优惠券
		List<Map> generalCouponinfo = this.userCoupon(null, order_goods_amount, CommUtil.null2Long(user_id));
		cartMap.put("Couponinfo", generalCouponinfo);
		// 平台满包邮
		if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
			// order_ship_price = CommUtil.subtract(ship_price, ship_price * 2);
			order_free_express_price = this.configService.getSysConfig().getEnoughfree_price();
			if (CommUtil.subtract(order_goods_amount, order_free_express_price) >= 0) {
				order_total_price = order_goods_amount;
				order_ship_price = 0;
			} else {
				order_total_price = CommUtil.add(order_goods_amount, order_ship_price);
				ship_subsidy_price = CommUtil.subtract(order_free_express_price, order_goods_amount);
			}
		}
		cartMap.put("order_goods_amount", order_goods_amount);
		cartMap.put("order_free_express_price", order_free_express_price);
		cartMap.put("ship_subsidy_price", ship_subsidy_price);
		cartMap.put("order_ship_price", order_ship_price);
		cartMap.put("order_total_price", CommUtil.add(order_total_price, order_ship_price));
		cartMap.put("orderlist", orderlist);
	}

	/**
	 * 
	 * @param map_list
	 * @param area_abbr
	 * @param area_id
	 * @param user_id
	 * @param cartMap
	 * @param generalCouponId
	 * @param language
	 * @param integral
	 * @descript cart_order 订单修改
	 */
	public void cart_order_adjust(List map_list, String area_abbr, String area_id, String user_id, Map cartMap,
			String generalCouponId, String language, String integral) {

		// double store_goods_amount = 0;// 店铺商品总价
		// double goods_amount = 0; // 商品总价
		// double store_total_price = 0;// 店铺总价--不包含店铺运费
		// double ship_price = 0; // 店铺运费
		// double store_shipping_included = 0;// 优惠后含运费店铺总价
		// double store_exclude_freight = 0;// 优惠后不含运费价格
		// double order_goods_amount = 0;// 优惠后订单商品总价
		// double order_total_price = 0;// 订单总价--不包含店铺运费
		// double store_order_tota_price = 0;// 店铺总价 --不含运费
		// double ship_subsidy_price = 0;// 平台满包邮与店铺总价差价
		// double order_ship_price = 0;// 平台满包邮价格
		// double order_free_express_price = 0;//平台满包可使用订单金额
		// double order_discounts_amount = 0;// 订单优惠价格
		//
		User user = this.userService.getObjById(CommUtil.null2Long(user_id));
		List<Map> orderlist = new ArrayList<Map>();
		double store_goods_amount = 0;// 店铺商品总额--优惠前
		double store_total_price = 0;// 店铺总额--不包含店铺运费
		double ship_price = 0; // 店铺运费
		double store_exclude_freight = 0;// 优惠后不含运费价格
		double store_shipping_included = 0;// 优惠后含运费店铺总额
		double order_goods_amount = 0;// 优惠后订单商品总额
		double order_ship_price = 0;// 订单运费总额
		double ship_subsidy_price = 0;// 平台满包邮与店铺总额差价
		double order_free_express_price = 0;// 平台满包可使用订单金额
		double order_discounts_amount = 0;// 订单优惠价格
		double order_total_price = 0;// 订单总价

		for (int i = 0; i < map_list.size(); i++) {
			Map storeMap = new HashMap();
			Map map_info = (Map) map_list.get(i);
			Store store = storeService.getObjById(CommUtil.null2Long(map_info.get("store_id")));
			if (store != null) {
				storeMap.put("store_id", store.getId());
				storeMap.put("store_name", store.getStore_name());
				storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
			} else {
				storeMap.put("store_id", "self");
				storeMap.put("OnLine", userTools.adminOnLine());
			}
			List<GoodsCart> carts = (List<GoodsCart>) map_info.get("gc_list");
			// 商品信息
			List goodsList = this.cartGoodsV2(carts, language);
			storeMap.put("gc_list", goodsList);
			Map<Long, List<GoodsCart>> er_map = null;// 满减
			List er_goodsList = new ArrayList();
			if (map_info.get("er_goods") != null) {
				List<GoodsCart> goodscart_er = new ArrayList<GoodsCart>();
				er_map = (Map<Long, List<GoodsCart>>) map_info.get("er_goods");
				List er_map_list = new ArrayList();
				for (Long key : er_map.keySet()) {
					Map reduce_map = new HashMap();
					goodscart_er = er_map.get(key);
					Map map = (Map) map_info.get("erString");
					reduce_map.put("er_str", map.get(key));
					Map ermap = (Map) map_info.get("er_json");
					reduce_map.put("er_json", ermap.get(key));
					reduce_map.put("er_price", map_info.get("store_enough_reduce"));
					er_goodsList = this.cartGoodsV2(goodscart_er, language);
					reduce_map.put("er_goods", er_goodsList);
					er_map_list.add(reduce_map);
				}
				storeMap.put("enoughReduce", er_map_list);
				order_discounts_amount += CommUtil.null2Double(map_info.get("store_enough_reduce"));// 满减优惠金额
			}
			store_goods_amount = (double) map_info.get("store_goods_amount");
			store_total_price = (double) map_info.get("store_total_price");// 店铺总价--满减

			Map<Goods, List<GoodsCart>> goods_ac_map = null;// 满就送
			if (map_info.get("ac_goods") != null) {
				List ac_info_list = new ArrayList();
				List<GoodsCart> goodscart_ac = new ArrayList<GoodsCart>();
				goods_ac_map = (Map<Goods, List<GoodsCart>>) map_info.get("ac_goods");
				for (Goods key : goods_ac_map.keySet()) {
					goodscart_ac = goods_ac_map.get(key);
					List ac_goodsList = this.cartGoods(goodscart_ac, language);
					storeMap.put("ac_goodsList", ac_goodsList);
				}
			}
			// 店铺优惠券列表 满减后金额
			List<Map> couponinfo = this.userCoupon(store.getId(), store_total_price, CommUtil.null2Long(user_id));
			// 店铺优惠券
			String coupon_id = CommUtil.null2String(map_info.get("coupon_id"));
			CouponInfo userCoupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
			if (userCoupon != null) {
				Coupon storeCoupon = userCoupon.getCoupon();
				if (storeCoupon.getCoupon_begin_time().before(new Date())
						&& CommUtil.subtract(store_total_price, storeCoupon.getCoupon_order_amount()) >= 0) {
					order_discounts_amount += CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount()) <= 0
							? store_total_price : storeCoupon.getCoupon_amount().doubleValue();
					store_total_price = CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount()) <= 0 ? 0
							: CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount());
				}
			}
			store_exclude_freight = store_total_price;
			order_goods_amount = CommUtil.add(order_goods_amount, store_exclude_freight);

			// 运费
			List<SysMap> list = new ArrayList<SysMap>();
			list = transportTools.transportation_cart2(carts, er_map, goods_ac_map, area_id);
			for (SysMap obj : list) {
				if (obj.getKey().equals("Express")) {
					ship_price = CommUtil.null2Double(obj.getValue());
				}
			}
			order_ship_price += ship_price;// 订单运费总额
			store_shipping_included = CommUtil.add(store_exclude_freight, ship_price);// 含运费店铺总价
			storeMap.put("store_goods_amount", store_goods_amount);
			storeMap.put("store_total_price", store_total_price);
			storeMap.put("ship_price", ship_price);
			storeMap.put("store_shipping_included", store_shipping_included);
			storeMap.put("Couponinfo", couponinfo);
			orderlist.add(storeMap);
		}
		order_total_price = order_goods_amount;
		// 平台优惠券
		CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(generalCouponId));
		if (coupon != null) {
			Coupon platformCoupon = coupon.getCoupon();
			if (platformCoupon.getCoupon_begin_time().before(new Date())) {
				if (CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_order_amount()) >= 0) {
					order_discounts_amount += CommUtil.subtract(order_goods_amount,
							platformCoupon.getCoupon_amount()) <= 0 ? order_goods_amount
									: platformCoupon.getCoupon_amount().doubleValue();
					order_total_price = CommUtil.subtract(order_total_price, platformCoupon.getCoupon_amount()) <= 0 ? 0
							: CommUtil.subtract(order_total_price, platformCoupon.getCoupon_amount());
				}
			}
		}
		List generalCouponinfo = this.userCoupon(null, order_goods_amount, CommUtil.null2Long(user_id));// 平台优惠券列表

		// 积分
		if (this.configService.getSysConfig().isIntegral()) {
			if (CommUtil.null2Int(integral) == 1) {
				int use_integral = user.getIntegral();
				double integral_price = CommUtil.mul(use_integral,
						this.configService.getSysConfig().getIntegralExchangeRate());
				if (integral_price > 0) {
					order_total_price = CommUtil.subtract(order_total_price, integral_price) >= 0
							? CommUtil.subtract(order_total_price, integral_price) : 0;
					order_discounts_amount += integral_price;
				}
			}
		}
		// 平台满包邮
		if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
			order_free_express_price = this.configService.getSysConfig().getEnoughfree_price();
			if (CommUtil.subtract(order_goods_amount, order_free_express_price) >= 0) {
				order_discounts_amount += order_ship_price;
				order_ship_price = 0;
			} else if (order_ship_price > 0) {
				order_total_price = CommUtil.add(order_total_price, order_ship_price);
				ship_subsidy_price = CommUtil.subtract(order_free_express_price, order_goods_amount);
			}
		}
		cartMap.put("order_goods_amount", order_goods_amount);
		cartMap.put("order_free_express_price", order_free_express_price);
		cartMap.put("ship_subsidy_price", ship_subsidy_price);
		cartMap.put("order_ship_price", order_ship_price);
		cartMap.put("order_discounts_amount", order_discounts_amount);
		cartMap.put("order_total_price", order_total_price);
		cartMap.put("Couponinfo", generalCouponinfo);
		cartMap.put("orderlist", orderlist);
	}

	/**
	 * 店铺商品
	 * 
	 * @param map_list
	 * @param area_abbr
	 * @param area_id
	 * @param request
	 * @return
	 */
	public void adjust(List map_list, String area_abbr, String area_id, String user_id, Map cartMap,
			String generalCouponId, String language) {
		List<Map> orderlist = new ArrayList<Map>();
		double store_total_price = 0;
		double store_order_tota_price = 0;
		double goods_amount = 0; // 店铺商品总价
		double order_goods_amount = 0;// 订单商品总价
		double order_ship_price = 0;// 订单运费总价
		double order_total_price = 0;// 订单总价
		double pay_total_price = 0;// 订单支付价格
		double store_general_goods_amount = 0;// 店铺优惠价格
		double general_goods_amount = 0;// 平台优惠价格
		double order_discounts_amount = 0;// 订单优惠价格
		double reduce = 0;// 满减价格
		double price = 0;

		boolean enable = true;
		for (int i = 0; i < map_list.size(); i++) {
			Map storeMap = new HashMap();
			Map map_info = (Map) map_list.get(i);
			Store store = storeService.getObjById(CommUtil.null2Long(map_info.get("store_id")));
			try {
				if (map_info.get("store_id") == "self") {
					storeMap.put("store_id", "self");
					storeMap.put("OnLine", userTools.adminOnLine());
				} else {
					storeMap.put("store_id", store.getId());
					storeMap.put("store_name", store.getStore_name());
					storeMap.put("store_enough_free", store.getEnough_free());
					storeMap.put("store_enough_free_price", store.getEnough_free_price());
					storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
					// 匹配地址库--目前不匹配地址库
					/*
					 * if(this.configService.getSysConfig().getServer_version()
					 * == 2){ if(store.getTransport() != null &&
					 * store.getTransport().getExpress_company() != null){
					 * if(store.getTransport().getExpress_company().getEnabled()
					 * == 1){ enable = matching(store , area_abbr);
					 * storeMap.put("flag", enable); }else{ storeMap.put("flag",
					 * enable); } }else{ storeMap.put("flag", false); } }else{
					 * storeMap.put("flag", enable); }
					 */
					storeMap.put("flag", enable);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			List<GoodsCart> carts = (List<GoodsCart>) map_info.get("gc_list");
			boolean delivery = false;
			List goodsList = this.cartGoods(carts, language);
			storeMap.put("gc_list", goodsList);
			Map<Long, List<GoodsCart>> er_map = null;// 满减
			List er_goodsList = new ArrayList();
			if (map_info.get("er_goods") != null) {
				List<GoodsCart> goodscart_er = new ArrayList<GoodsCart>();
				er_map = (Map<Long, List<GoodsCart>>) map_info.get("er_goods");
				List er_map_list = new ArrayList();
				for (Long key : er_map.keySet()) {
					Map reduce_map = new HashMap();
					goodscart_er = er_map.get(key);
					Map map = (Map) map_info.get("erString");
					reduce_map.put("er_str", map.get(key));
					Map ermap = (Map) map_info.get("er_json");
					reduce_map.put("er_json", ermap.get(key));
					reduce_map.put("er_price", map_info.get("store_enough_reduce"));
					er_goodsList = this.cartGoods(goodscart_er, language);
					reduce_map.put("er_goods", er_goodsList);
					er_map_list.add(reduce_map);
				}
				reduce = CommUtil.null2Double(map_info.get("store_enough_reduce"));
				order_discounts_amount += CommUtil.null2Double(map_info.get("store_enough_reduce"));
				storeMap.put("enoughReduce", er_map_list);
			}

			Map<Goods, List<GoodsCart>> goods_ac_map = null;// 满就送
			if (map_info.get("ac_goods") != null) {
				List ac_info_list = new ArrayList();
				List<GoodsCart> goodscart_ac = new ArrayList<GoodsCart>();
				goods_ac_map = (Map<Goods, List<GoodsCart>>) map_info.get("ac_goods");
				for (Goods key : goods_ac_map.keySet()) {
					goodscart_ac = goods_ac_map.get(key);
					List ac_goodsList = this.cartGoods(goodscart_ac, language);
					storeMap.put("ac_goodsList", ac_goodsList);
				}
			}
			double ship_price = 0.00;
			goods_amount = (double) map_info.get("store_goods_price");// CommUtil.add(goods_amount,
																		// map_info.get("store_goods_price"));
			store_total_price = CommUtil.null2Double(map_info.get("store_goods_price"));// 店铺总价
			// delivery == true
			Map sysMap = new HashMap();
			if (enable) {
				List<SysMap> list = new ArrayList<SysMap>();
				list = transportTools.transportation_cart2(carts, er_map, goods_ac_map, area_id);
				List sysInfo = new ArrayList();
				for (SysMap obj : list) {
					if (obj.getKey().equals("Express")) {
						ship_price = CommUtil.null2Double(obj.getValue());
					}
				}
				sysMap.put("ship_price", ship_price);
				sysInfo.add(sysMap);
				storeMap.put("sys_info", sysInfo);
			}

			// 计算店铺优惠券
			String coupon_id = CommUtil.null2String(map_info.get("coupon_id"));
			if (coupon_id != null && !coupon_id.equals("")) {
				CouponInfo userCoupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
				if (userCoupon != null) {
					Coupon storeCoupon = userCoupon.getCoupon();
					if (storeCoupon.getCoupon_begin_time().before(new Date())
							&& CommUtil.subtract(store_total_price, storeCoupon.getCoupon_order_amount()) >= 0) {
						order_discounts_amount += (double) (CommUtil.subtract(store_total_price,
								storeCoupon.getCoupon_amount()) <= 0 ? store_total_price
										: CommUtil.null2Double(storeCoupon.getCoupon_amount()));
						store_total_price = CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount()) <= 0
								? 0 : CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount());
					}
				}
			}
			// 店铺优惠券列表
			List<Map> couponinfo = this.userCoupon(store.getId(), goods_amount, CommUtil.null2Long(user_id));// 查询用户对应的商家优惠券【调整】

			if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(), goods_amount) <= 0) {// 计算满包邮--包邮
				order_goods_amount += store_total_price;
				order_discounts_amount += ship_price;
				storeMap.put("flag", 1);
			} else {
				order_goods_amount += store_total_price;
				store_total_price = CommUtil.add(ship_price, store_total_price);
				order_ship_price = order_ship_price + ship_price;
				storeMap.put("flag", 0);
			}
			storeMap.put("Couponinfo", couponinfo);
			storeMap.put("goods_amount", CommUtil.add(reduce, goods_amount));
			storeMap.put("store_goods_price", goods_amount);
			storeMap.put("store_total_price", store_total_price);
			orderlist.add(storeMap);
		}
		order_total_price = CommUtil.add(order_goods_amount, order_ship_price);// 订单总价--包含运费
		cartMap.put("order_tota_price", order_total_price);
		List generalCouponinfo = this.userCoupon(null, order_goods_amount, CommUtil.null2Long(user_id));// 平台优惠券列表
		// 计算平台优惠券 商品总价
		if (generalCouponId != null && !generalCouponId.equals("")) {
			CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(generalCouponId));
			Coupon platformCoupon = coupon.getCoupon();
			if (platformCoupon.getCoupon_begin_time().before(new Date())) {
				if (CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_order_amount()) >= 0) {
					order_discounts_amount += CommUtil.subtract(order_goods_amount,
							platformCoupon.getCoupon_amount()) <= 0 ? order_goods_amount
									: CommUtil.null2Double(platformCoupon.getCoupon_amount());
					order_goods_amount = CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_amount()) <= 0
							? 0 : CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_amount());
				}
			}
		}
		pay_total_price = CommUtil.add(order_goods_amount, order_ship_price);
		cartMap.put("pay_total_price", pay_total_price);
		cartMap.put("order_discounts_amount", order_discounts_amount);
		cartMap.put("orderlist", orderlist);
		cartMap.put("Couponinfo", generalCouponinfo);
		cartMap.put("store_name", "Soarmall");
	}

	/**
	 * @descript 多店铺下单-平台设置满额包邮
	 * @param map_list
	 * @param area_abbr
	 * @param area_id
	 * @param user_id
	 * @param cartMap
	 * @param generalCouponId
	 * @param language
	 */
	public void adjustV2(List map_list, String area_abbr, String area_id, String user_id, Map cartMap,
			String generalCouponId, String language) {
		List<Map> orderlist = new ArrayList<Map>();
		double store_total_price = 0;
		double goods_amount = 0; // 店铺商品总价
		double order_goods_amount = 0;// 订单商品总价
		double order_ship_price = 0;// 订单运费总价
		double order_total_price = 0;// 订单总价
		double pay_total_price = 0;// 订单支付价格
		double store_general_goods_amount = 0;// 店铺优惠价格
		double general_goods_amount = 0;// 平台优惠价格
		double order_discounts_amount = 0;// 订单优惠价格
		double reduce = 0;// 满减价格
		double price = 0;
		boolean enable = true;
		for (int i = 0; i < map_list.size(); i++) {
			Map storeMap = new HashMap();
			Map map_info = (Map) map_list.get(i);
			Store store = storeService.getObjById(CommUtil.null2Long(map_info.get("store_id")));
			try {
				if (map_info.get("store_id") == "self") {
					storeMap.put("store_id", "self");
					storeMap.put("OnLine", userTools.adminOnLine());
				} else {
					storeMap.put("store_id", store.getId());
					storeMap.put("store_name", store.getStore_name());
					storeMap.put("store_enough_free", store.getEnough_free());
					storeMap.put("store_enough_free_price", store.getEnough_free_price());
					storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
					// 匹配地址库--目前不匹配地址库
					/*
					 * if(this.configService.getSysConfig().getServer_version()
					 * == 2){ if(store.getTransport() != null &&
					 * store.getTransport().getExpress_company() != null){
					 * if(store.getTransport().getExpress_company().getEnabled()
					 * == 1){ enable = matching(store , area_abbr);
					 * storeMap.put("flag", enable); }else{ storeMap.put("flag",
					 * enable); } }else{ storeMap.put("flag", false); } }else{
					 * storeMap.put("flag", enable); }
					 */
					storeMap.put("flag", enable);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			List<GoodsCart> carts = (List<GoodsCart>) map_info.get("gc_list");
			boolean delivery = false;
			List goodsList = this.cartGoods(carts, language);
			storeMap.put("gc_list", goodsList);
			Map<Long, List<GoodsCart>> er_map = null;// 满减
			List er_goodsList = new ArrayList();
			if (map_info.get("er_goods") != null) {
				List<GoodsCart> goodscart_er = new ArrayList<GoodsCart>();
				er_map = (Map<Long, List<GoodsCart>>) map_info.get("er_goods");
				List er_map_list = new ArrayList();
				for (Long key : er_map.keySet()) {
					Map reduce_map = new HashMap();
					goodscart_er = er_map.get(key);
					Map map = (Map) map_info.get("erString");
					reduce_map.put("er_str", map.get(key));
					Map ermap = (Map) map_info.get("er_json");
					reduce_map.put("er_json", ermap.get(key));
					reduce_map.put("er_price", map_info.get("store_enough_reduce"));
					er_goodsList = this.cartGoods(goodscart_er, language);
					reduce_map.put("er_goods", er_goodsList);
					er_map_list.add(reduce_map);
				}
				reduce = CommUtil.null2Double(map_info.get("store_enough_reduce"));
				order_discounts_amount += CommUtil.null2Double(map_info.get("store_enough_reduce"));
				storeMap.put("enoughReduce", er_map_list);
			}

			Map<Goods, List<GoodsCart>> goods_ac_map = null;// 满就送
			if (map_info.get("ac_goods") != null) {
				List ac_info_list = new ArrayList();
				List<GoodsCart> goodscart_ac = new ArrayList<GoodsCart>();
				goods_ac_map = (Map<Goods, List<GoodsCart>>) map_info.get("ac_goods");
				for (Goods key : goods_ac_map.keySet()) {
					goodscart_ac = goods_ac_map.get(key);
					List ac_goodsList = this.cartGoods(goodscart_ac, language);
					storeMap.put("ac_goodsList", ac_goodsList);
				}
			}
			double ship_price = 0.00;
			goods_amount = (double) map_info.get("store_goods_price");// CommUtil.add(goods_amount,
																		// map_info.get("store_goods_price"));
			store_total_price = CommUtil.null2Double(map_info.get("store_goods_price"));// 店铺总价
			// delivery == true
			// v2版本运费使用平台满包邮
			Map sysMap = new HashMap();
			if (enable) {
				List<SysMap> list = new ArrayList<SysMap>();
				list = transportTools.transportation_cart2(carts, er_map, goods_ac_map, area_id);
				List sysInfo = new ArrayList();
				for (SysMap obj : list) {
					if (obj.getKey().equals("Express")) {
						ship_price = CommUtil.null2Double(obj.getValue());
					}
				}
				sysMap.put("ship_price", ship_price);
				sysInfo.add(sysMap);
				storeMap.put("sys_info", sysInfo);
			}

			// 计算店铺优惠券
			String coupon_id = CommUtil.null2String(map_info.get("coupon_id"));
			if (coupon_id != null && !coupon_id.equals("")) {
				CouponInfo userCoupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
				if (userCoupon != null) {
					Coupon storeCoupon = userCoupon.getCoupon();
					if (storeCoupon.getCoupon_begin_time().before(new Date())
							&& CommUtil.subtract(store_total_price, storeCoupon.getCoupon_order_amount()) >= 0) {
						order_discounts_amount += (double) (CommUtil.subtract(store_total_price,
								storeCoupon.getCoupon_amount()) <= 0 ? store_total_price
										: CommUtil.null2Double(storeCoupon.getCoupon_amount()));
						store_total_price = CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount()) <= 0
								? 0 : CommUtil.subtract(store_total_price, storeCoupon.getCoupon_amount());
					}
				}
			}
			// 店铺优惠券列表
			List<Map> couponinfo = this.userCoupon(store.getId(), goods_amount, CommUtil.null2Long(user_id));// 查询用户对应的商家优惠券【调整】

			if (store.getEnough_free() == 1 && CommUtil.subtract(store.getEnough_free_price(), goods_amount) <= 0) {// 计算满包邮--包邮
				order_goods_amount += store_total_price;
				order_discounts_amount += ship_price;
				storeMap.put("flag", 1);
			} else {
				order_goods_amount += store_total_price;
				store_total_price = CommUtil.add(ship_price, store_total_price);
				order_ship_price = order_ship_price + ship_price;
				storeMap.put("flag", 0);
			}
			storeMap.put("Couponinfo", couponinfo);
			storeMap.put("goods_amount", CommUtil.add(reduce, goods_amount));
			storeMap.put("store_goods_price", goods_amount);
			storeMap.put("store_total_price", store_total_price);
			orderlist.add(storeMap);
		}
		order_total_price = CommUtil.add(order_goods_amount, order_ship_price);// 订单总价--包含运费
		cartMap.put("order_tota_price", order_total_price);
		List generalCouponinfo = this.userCoupon(null, order_goods_amount, CommUtil.null2Long(user_id));// 平台优惠券列表
		// 计算平台优惠券 商品总价
		if (generalCouponId != null && !generalCouponId.equals("")) {
			CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(generalCouponId));
			Coupon platformCoupon = coupon.getCoupon();
			if (platformCoupon.getCoupon_begin_time().before(new Date())) {
				if (CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_order_amount()) >= 0) {
					order_discounts_amount += CommUtil.subtract(order_goods_amount,
							platformCoupon.getCoupon_amount()) <= 0 ? order_goods_amount
									: CommUtil.null2Double(platformCoupon.getCoupon_amount());
					order_goods_amount = CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_amount()) <= 0
							? 0 : CommUtil.subtract(order_goods_amount, platformCoupon.getCoupon_amount());
				}
			}
		}
		pay_total_price = CommUtil.add(order_goods_amount, order_ship_price);
		cartMap.put("pay_total_price", pay_total_price);
		cartMap.put("order_discounts_amount", order_discounts_amount);
		cartMap.put("orderlist", orderlist);
		cartMap.put("Couponinfo", generalCouponinfo);
		cartMap.put("store_name", "Soarmall");
	}

	/**
	 * 
	 * @param id
	 * @param area_abr
	 * @param area_id
	 * @param user_id
	 * @param cart_map
	 */
	public void queryPointGoods(HttpServletRequest request, HttpServletResponse response, Goods obj, String area_abbr,
			String area_id, String user_id, Map map, String color, String gsp) {
		String spec_info = "";
		double order_tota_price = 0;
		double store_total_price = 0;
		String[] gsp_ids = gsp.split(",");
		for (String gsp_id : gsp_ids) {
			GoodsSpecProperty spec_property = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gsp_id));
			if (spec_property != null) {
				spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + "<br> " + spec_info;
			}
		}
		List<Map> orderlist = new ArrayList<Map>();
		boolean enable = true;
		Map storeMap = new HashMap();
		Store store = obj.getGoods_store();
		storeMap.put("store_id", store.getId());
		storeMap.put("store_name", store.getStore_name());
		storeMap.put("OnLine", userTools.userOnLine(store.getUser().getTrueName()));
		// 匹配地址库
		if (this.configService.getSysConfig().getServer_version() == 2) {
			if (store.getTransport() != null && store.getTransport().getExpress_company() != null) {
				if (store.getTransport().getExpress_company().getEnabled() == 1) {
					enable = matching(store, area_abbr);
					storeMap.put("flag", enable);
				} else {
					storeMap.put("flag", enable);
				}
			} else {
				storeMap.put("flag", enable);
			}
		} else {
			storeMap.put("flag", enable);
		}
		boolean delivery = false;
		if (obj.getGoods_choice_type() == 0) {
			delivery = true;
		}
		Map goodsMap = new HashMap();
		goodsMap.put("gcg_choice_type", obj.getGoods_choice_type());
		goodsMap.put("id", obj.getId());
		goodsMap.put("all_price", 0);
		goodsMap.put("gcg_name", obj.getGoods_name());
		goodsMap.put("gcg_price", 0);
		goodsMap.put("gcg_main_photo", this.configService.getSysConfig().getImageWebServer() + "/"
				+ obj.getGoods_main_photo().getPath() + "/" + obj.getGoods_main_photo().getName());
		goodsMap.put("gc_count", 1);
		goodsMap.put("gc_sepc_info", spec_info);
		goodsMap.put("goods_spec_color", color);
		goodsMap.put("goods_spec_gsp", gsp);
		storeMap.put("goods", goodsMap);

		if (delivery == true && enable) {
			Area area = this.areaService.getObjById(CommUtil.null2Long(area_id)).getParent();
			String city_name = area.getAreaName();
			// [根据区域查询运费 商品兑换不需要运费]
			/*
			 * float price = this.transportTools.point_order_trans(request,
			 * response, CommUtil.null2String(obj.getId()), "express", "",
			 * city_name, "1", gsp, color);
			 */
			List sys_info = new ArrayList();
			Map sysmap = new HashMap();
			sysmap.put("ship_price", 0);
			sys_info.add(sysmap);
			storeMap.put("sys_info", sys_info);
			/*
			 * store_total_price = price; order_tota_price = price;
			 */
			orderlist.add(storeMap);
			map.put("orderlist", orderlist);
			map.put("order_tota_price", 0);
		}
	}

	/**
	 * @description 用户优惠券(查询)
	 * @param storeId
	 * @param goods_cmount
	 * @param user_id
	 * @return
	 */
	public List<Map> userCoupon(Long storeId, double goods_cmount, Long user_id) {
		Result result = null;
		Map params = new HashMap();
		Store store = this.storeService.getObjById(storeId);
		User user = this.userService.getObjById(user_id);
		List<CouponInfo> couponinfos = new ArrayList<CouponInfo>();
		List<CouponInfo> generalCouponInfos = new ArrayList<CouponInfo>();
		params.clear();
		// params.put("coupon_order_amount",
		// BigDecimal.valueOf(CommUtil.null2Double(goods_cmount)));
		params.put("user_id", user.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("status", 0);
		// 全网通用优惠券
		if (storeId == null || storeId == 0) {
			params.put("coupon_type", 0);
			couponinfos = this.couponInfoService.query("select obj from CouponInfo obj where "
					+ "obj.status=:status and obj.user.id=:user_id and obj.coupon.coupon_type=:coupon_type "
					+ "and obj.coupon.coupon_begin_time<=:coupon_begin_time and obj.coupon.coupon_end_time>=:coupon_end_time "
					+ "order by obj.coupon.coupon_amount desc", params, -1, -1);

		} else {
			params.put("store_id", store.getId());
			couponinfos = this.couponInfoService.query("select obj from CouponInfo obj where "
					+ "obj.status=:status and obj.user.id=:user_id and obj.coupon.store.id=:store_id "
					+ "and obj.coupon.coupon_begin_time<=:coupon_begin_time and obj.coupon.coupon_end_time>=:coupon_end_time "
					+ "order by obj.coupon.coupon_amount desc", params, -1, -1);
		}
		LinkedList list = new LinkedList();
		for (CouponInfo couponinfo : couponinfos) {
			Map map = new HashMap();
			map.put("id", couponinfo.getId());
			// map.put("Coupon_name", couponinfo.getCoupon().getCoupon_name());
			map.put("Coupon_name", store == null ? "Soarmall" : store.getStore_name());
			map.put("Coupon_amount", couponinfo.getCoupon().getCoupon_amount());
			map.put("Coupon_order_amount", couponinfo.getCoupon().getCoupon_order_amount());
			map.put("goods_price", CommUtil.subtract(goods_cmount, couponinfo.getCoupon().getCoupon_amount()) >= 0
					? CommUtil.subtract(goods_cmount, couponinfo.getCoupon().getCoupon_amount()) : 0);
			if (CommUtil.null2Int(couponinfo.getCoupon().getCoupon_order_amount()) == 0) {
				if (goods_cmount == 0) {
					map.put("flag", false);
				} else {
					map.put("flag", true);
				}
			} else {
				map.put("flag", CommUtil.subtract(couponinfo.getCoupon().getCoupon_order_amount(), goods_cmount) > 0
						? false : true);
			}
			map.put("Coupon_begin_time",
					CommUtil.formatTime("yyyy.MM.dd", couponinfo.getCoupon().getCoupon_begin_time()));
			map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", couponinfo.getCoupon().getCoupon_end_time()));
			if ("false".equals(map.get("flag").toString())) {
				list.add(map);
			} else {
				list.addFirst(map);
			}
		}
		return list;
	}

	/**
	 * @description 用户优惠券(查询)
	 * @param storeId
	 * @param goods_cmount
	 * @param user_id
	 * @return
	 */
	public List<Map> couponList(Long storeId, double goods_cmount, Long user_id, double goods_price) {
		Result result = null;
		Map params = new HashMap();
		Store store = this.storeService.getObjById(storeId);
		User user = this.userService.getObjById(user_id);
		List<CouponInfo> couponinfos = new ArrayList<CouponInfo>();
		List<CouponInfo> generalCouponInfos = new ArrayList<CouponInfo>();
		params.clear();
		// params.put("coupon_order_amount",
		// BigDecimal.valueOf(CommUtil.null2Double(goods_cmount)));
		params.put("user_id", user.getId());
		params.put("coupon_begin_time", new Date());
		params.put("coupon_end_time", new Date());
		params.put("status", 0);
		// 全网通用优惠券
		if (storeId == null || storeId == 0) {
			params.put("coupon_type", 0);
			couponinfos = this.couponInfoService.query("select obj from CouponInfo obj where "
					+ "obj.status=:status and obj.user.id=:user_id and obj.coupon.coupon_type=:coupon_type "
					+ "and obj.coupon.coupon_begin_time<=:coupon_begin_time and obj.coupon.coupon_end_time>=:coupon_end_time "
					+ "order by obj.coupon.coupon_amount desc", params, -1, -1);

		} else {
			params.put("store_id", store.getId());
			couponinfos = this.couponInfoService.query("select obj from CouponInfo obj where "
					+ "obj.status=:status and obj.user.id=:user_id and obj.coupon.store.id=:store_id "
					+ "and obj.coupon.coupon_begin_time<=:coupon_begin_time and obj.coupon.coupon_end_time>=:coupon_end_time "
					+ "order by obj.coupon.coupon_amount desc", params, -1, -1);
		}
		LinkedList list = new LinkedList();
		for (CouponInfo couponinfo : couponinfos) {
			Map map = new HashMap();
			map.put("id", couponinfo.getId());
			// map.put("Coupon_name", couponinfo.getCoupon().getCoupon_name());
			map.put("Coupon_name", store == null ? "Soarmall" : store.getStore_name());
			map.put("Coupon_amount", couponinfo.getCoupon().getCoupon_amount());
			map.put("Coupon_order_amount", couponinfo.getCoupon().getCoupon_order_amount());
			map.put("goods_price", CommUtil.subtract(couponinfo.getCoupon().getCoupon_amount(), goods_cmount) >= 0
					? CommUtil.subtract(couponinfo.getCoupon().getCoupon_amount(), goods_cmount) : 0);
			map.put("flag", true);
			// if
			// (CommUtil.subtract(couponinfo.getCoupon().getCoupon_order_amount(),
			// goods_price) <= 0) {
			// map.put("flag", false);
			// }
			if (CommUtil.null2Int(couponinfo.getCoupon().getCoupon_order_amount()) == 0) {
				if (goods_cmount == 0) {
					map.put("flag", false);
				} else {
					map.put("flag", true);
				}
			} else {
				if (CommUtil.subtract(couponinfo.getCoupon().getCoupon_order_amount(), goods_price) > 0
						|| CommUtil.subtract(couponinfo.getCoupon().getCoupon_order_amount(), goods_cmount) > 0) {
					map.put("flag", false);
				} else {
					map.put("flag", true);
				}
			}
			map.put("Coupon_begin_time",
					CommUtil.formatTime("yyyy.MM.dd", couponinfo.getCoupon().getCoupon_begin_time()));
			map.put("Coupon_end_time", CommUtil.formatTime("yyyy.MM.dd", couponinfo.getCoupon().getCoupon_end_time()));
			if ("false".equals(map.get("flag").toString())) {
				list.add(map);
			} else {
				list.addFirst(map);
			}
		}
		return list;
	}

	/**
	 * @description goods_cart2商品信息
	 * @param carts
	 * @param storemap
	 * @return
	 */
	public List cartGoods(List<GoodsCart> carts, String language) {
		boolean delivery = false;
		List<Map> gc_list = new ArrayList<Map>();
		for (GoodsCart gc : carts) {
			if (gc.getGoods().getGoods_choice_type() == 0) {
				delivery = true;
			}
			Map map = new HashMap();
			map.put("gc_id", gc.getId());
			map.put("gcg_choice_type", gc.getGoods().getGoods_choice_type());
			map.put("gcg_id", gc.getGoods().getId());
			map.put("all_price", CommUtil.mul(gc.getCount(), gc.getPrice()));
			map.put("gcg_name", gc.getGoods().getGoods_name());
			if ("1".equals(language)) {
				map.put("gcg_name",
						gc.getGoods().getKsa_goods_name() != null && !"".equals(gc.getGoods().getKsa_goods_name())
								? "^" + gc.getGoods().getKsa_goods_name() : gc.getGoods().getGoods_name());
			}
			map.put("gcg_price", gc.getPrice());
			map.put("gcg_type", gc.getGoods().getGoods_type());
			map.put("gcg_activity_status", gc.getGoods().getActivity_status());
			map.put("gcg_group_buy", gc.getGoods().getGroup_buy());
			map.put("gcg_main_photo",
					this.configService.getSysConfig().getImageWebServer() + "/"
							+ gc.getGoods().getGoods_main_photo().getPath() + "/"
							+ gc.getGoods().getGoods_main_photo().getName());
			map.put("gc_count", gc.getCount());
			int goods_inventory = 0;
			if (gc.getGoods().getInventory_type().equals("spec")) {
				goods_inventory = CommUtil.null2Int(
						this.generic_default_info_color(gc.getGoods(), gc.getCart_gsp(), gc.getColor()).get("count"));// 计算商品库存信息
			} else {
				goods_inventory = gc.getGoods().getGoods_inventory();
			}
			map.put("goods_inventory", goods_inventory);
			map.put("gc_sepc_info", gc.getSpec_info());
			map.put("goods_spec_color", gc.getColor() == null ? "" : gc.getColor());
			map.put("free_gifts", gc.getCart_type());
			/*
			 * List<Map> combinList = new ArrayList<Map>(); // 组合销售
			 * if(gc.getCart_type() == "combin"){ gcmap.put("suit_name",
			 * goodsViewTools.getsuitName(gc.getCombin_suit_info())); String
			 * weburl=
			 * request.getSession().getServletContext().getContextPath();
			 * List<Map> suit_goods_list =
			 * goodsViewTools.getsuitGoods(weburl,CommUtil.null2String(gc.getId(
			 * ))); List<Map> suit_goods_listmap = new ArrayList<Map>(); for(Map
			 * suit_goods:suit_goods_list){ Map combinmap = new HashMap();
			 * combinmap.put("url", suit_goods.get("url"));
			 * combinmap.put("name", suit_goods.get("name"));
			 * combinmap.put("img", suit_goods.get("img"));
			 * suit_goods_listmap.add(combinmap); } gcmap.put("suit_goods",
			 * suit_goods_listmap); } Map suit_map =
			 * goodsViewTools.getSuitInfo(CommUtil.null2String(gc.getId()));
			 * if(gc.getCart_type() != null ){ if(gc.getCart_type() ==
			 * "combin"){ gcmap.put("gc_price",
			 * suit_map.get("plan_goods_price")); gcmap.put("all_price",
			 * suit_map.get("suit_all_price")); }else{ gcmap.put("gc_price",
			 * gc.getPrice()); gcmap.put("all_price",
			 * CommUtil.mul(gc.getCount(), gc.getPrice())); }
			 * gcmap.put("gc_count", gc.getCount()); gcmap.put("spec_info",
			 * gc.getSpec_info()); }
			 */
			gc_list.add(map);
		}
		return gc_list;
	}

	public List cartGoodsV2(List<GoodsCart> carts, String language) {
		boolean delivery = false;
		List<Map> gc_list = new ArrayList<Map>();
		for (GoodsCart gc : carts) {
			if (gc.getGoods().getGoods_choice_type() == 0) {
				delivery = true;
			}
			Map map = new HashMap();
			map.put("gc_id", gc.getId());
			map.put("gcg_choice_type", gc.getGoods().getGoods_choice_type());
			map.put("goods_id", gc.getGoods().getId());
			map.put("all_price", CommUtil.mul(gc.getCount(), gc.getPrice()));
			map.put("goods_name", gc.getGoods().getGoods_name());
			if ("1".equals(language)) {
				map.put("goods_name",
						gc.getGoods().getKsa_goods_name() != null && !"".equals(gc.getGoods().getKsa_goods_name())
								? "^" + gc.getGoods().getKsa_goods_name() : gc.getGoods().getGoods_name());
			}
			map.put("goods_price", gc.getPrice());
			map.put("goods_type", gc.getGoods().getGoods_type());
			map.put("goods_img",
					this.configService.getSysConfig().getImageWebServer() + "/"
							+ gc.getGoods().getGoods_main_photo().getPath() + "/"
							+ gc.getGoods().getGoods_main_photo().getName());
			map.put("goods_count", gc.getCount());
			int goods_inventory = 0;
			if (gc.getGoods().getInventory_type().equals("spec")) {
				goods_inventory = CommUtil.null2Int(
						this.generic_default_info_color(gc.getGoods(), gc.getCart_gsp(), gc.getColor()).get("count"));// 计算商品库存信息
			} else {
				goods_inventory = gc.getGoods().getGoods_inventory();
			}
			map.put("goods_inventory", goods_inventory);
			map.put("goods_spec", gc.getSpec_info());
			map.put("goods_color", gc.getColor() == null ? "" : gc.getColor());
			/*
			 * List<Map> combinList = new ArrayList<Map>(); // 组合销售
			 * if(gc.getCart_type() == "combin"){ gcmap.put("suit_name",
			 * goodsViewTools.getsuitName(gc.getCombin_suit_info())); String
			 * weburl=
			 * request.getSession().getServletContext().getContextPath();
			 * List<Map> suit_goods_list =
			 * goodsViewTools.getsuitGoods(weburl,CommUtil.null2String(gc.getId(
			 * ))); List<Map> suit_goods_listmap = new ArrayList<Map>(); for(Map
			 * suit_goods:suit_goods_list){ Map combinmap = new HashMap();
			 * combinmap.put("url", suit_goods.get("url"));
			 * combinmap.put("name", suit_goods.get("name"));
			 * combinmap.put("img", suit_goods.get("img"));
			 * suit_goods_listmap.add(combinmap); } gcmap.put("suit_goods",
			 * suit_goods_listmap); } Map suit_map =
			 * goodsViewTools.getSuitInfo(CommUtil.null2String(gc.getId()));
			 * if(gc.getCart_type() != null ){ if(gc.getCart_type() ==
			 * "combin"){ gcmap.put("gc_price",
			 * suit_map.get("plan_goods_price")); gcmap.put("all_price",
			 * suit_map.get("suit_all_price")); }else{ gcmap.put("gc_price",
			 * gc.getPrice()); gcmap.put("all_price",
			 * CommUtil.mul(gc.getCount(), gc.getPrice())); }
			 * gcmap.put("gc_count", gc.getCount()); gcmap.put("spec_info",
			 * gc.getSpec_info()); }
			 */
			gc_list.add(map);
		}
		return gc_list;
	}

	/**
	 * @description 匹配店铺地址库 目前所有地区都能送达;该方法保留
	 * @param store_id
	 * @param address_id
	 * @return
	 */
	public boolean matching(Store store, String area_abbr) {
		if (store != null && area_abbr != null && !area_abbr.equals("")) {
			List<CheckCity> checkCitys = store.getTransport().getExpress_company().getCheck_city();
			for (CheckCity cc_name : checkCitys) {
				if (cc_name.getAbbr() == null) {
					return false;
				} else {
					if (cc_name.getAbbr().equals(area_abbr)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 查询用户信息
	public void userInfo(Long user_id, Map map) {
		User user = this.userService.getObjById(user_id);
		Map userMap = new HashMap();
		try {
			userMap = new HashMap();
			if (this.configService.getSysConfig().isIntegral()) {
				userMap.put("userIntegral", user.getIntegral());
				double integral_price = CommUtil.mul(user.getIntegral(),
						this.configService.getSysConfig().getIntegralExchangeRate());
				userMap.put("integral_price", integral_price);
			} else {
				userMap.put("userIntegral", "");
				userMap.put("integral_price", "");
			}
			userMap.put("username", user.getUsername());
			userMap.put("user_id", user.getId());
			map.put("user", userMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @description 获得购物车中用户勾选需要购买的商品总价格
	 * 
	 * @param request
	 * @param response
	 */
	public double calCartPrice(List<GoodsCart> carts, String gcs) {
		double all_price = 0.0;
		Map<String, Double> ermap = new HashMap<String, Double>();
		if (CommUtil.null2String(gcs).equals("")) {
			for (GoodsCart gc : carts) {
				if (null != gc.getCart_type() & "0".equals(gc.getCart_type()) || "1".equals(gc.getCart_type())) {// 普通商品处理
					// if (gc.getCart_type().equals("app") ||
					// gc.getCart_type().equals("web")) {// 普通商品处理
					all_price = CommUtil.add(all_price, CommUtil.mul(gc.getCount(), gc.getPrice()));
				} else if ("combin".equals(gc.getCart_type())) {// 组合套装商品处理
					if (gc.getCombin_main() == 1) {
						Map map = (Map) Json.fromJson(gc.getCombin_suit_info());
						all_price = CommUtil.add(all_price, map.get("suit_all_price"));
					}
				}
				if (gc.getGoods().getEnough_reduce() == 1) {// 是满就减商品，记录金额
					String er_id = gc.getGoods().getOrder_enough_reduce_id();
					if (ermap.containsKey(er_id)) {
						double last_price = (double) ermap.get(er_id);
						ermap.put(er_id, CommUtil.add(last_price, CommUtil.mul(gc.getCount(), gc.getPrice())));
					} else {
						ermap.put(er_id, CommUtil.mul(gc.getCount(), gc.getPrice()));
					}
				}
			}
		} else {
			String[] gc_ids = gcs.split(",");
			for (GoodsCart gc : carts) {
				for (String gc_id : gc_ids) {
					if (gc.getId().equals(CommUtil.null2Long(gc_id))) {
						if (gc.getCart_type() != null && gc.getCart_type().equals("combin")
								&& gc.getCombin_main() == 1) {
							Map map = (Map) Json.fromJson(gc.getCombin_suit_info());
							all_price = CommUtil.add(all_price, map.get("suit_all_price"));
						} else {
							all_price = CommUtil.add(all_price, CommUtil.mul(gc.getCount(), gc.getPrice()));
						}
						if (gc.getGoods().getEnough_reduce() == 1) {// 是满就减商品，记录金额
							String er_id = gc.getGoods().getOrder_enough_reduce_id();
							if (ermap.containsKey(er_id)) {
								double last_price = (double) ermap.get(er_id);
								ermap.put(er_id, CommUtil.add(last_price, CommUtil.mul(gc.getCount(), gc.getPrice())));
							} else {
								ermap.put(er_id, CommUtil.mul(gc.getCount(), gc.getPrice()));
							}
						}
					}
				}
			}
		}

		double all_enough_reduce = 0;
		for (String er_id : ermap.keySet()) {
			EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
			if (er.getErstatus() == 10 && er.getErbegin_time().before(new Date())) {// 活动通过审核且正在进行
				String erjson = er.getEr_json();
				double er_money = ermap.get(er_id);// 购物车中的此类满减的金额
				Map fromJson = (Map) Json.fromJson(erjson);
				double reduce = 0;
				for (Object enough : fromJson.keySet()) {
					if (er_money >= CommUtil.null2Double(enough)) {
						reduce = CommUtil.null2Double(fromJson.get(enough));
					}
				}
				all_enough_reduce = CommUtil.add(all_enough_reduce, reduce);
			}
		}
		double d2 = Math.round((all_price - all_enough_reduce) * 100) / 100.0;
		return CommUtil.null2Double(CommUtil.formatMoney(d2));
	}

	/**
	 * @description //满减活动
	 * 
	 * @param carts
	 *            用户整体购物车
	 * @param gcs
	 *            用户当前选中购物车
	 * @return
	 */
	public Map calEnoughReducePrice(List<GoodsCart> carts, String gcs) {
		Map<Long, String> erString = new HashMap<Long, String>();
		Map er_json = new HashMap();
		double all_price = 0.0;
		double goods_current_price = 0.0; // 购物车商品实时价格
		Map<String, Double> ermap = new HashMap<String, Double>();
		Map erid_goodsids = new HashMap();
		Date date = new Date();
		if (CommUtil.null2String(gcs).equals("")) {
			for (GoodsCart gc : carts) {
				all_price = CommUtil.add(all_price, CommUtil.mul(gc.getCount(), gc.getPrice()));
				if (gc.getGoods().getEnough_reduce() == 1) {// 满就减商品，记录金额
					String er_id = gc.getGoods().getOrder_enough_reduce_id();
					EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
					if (er.getErstatus() == 10 // [审核状态 默认为0待审核 10为 审核通过
												// -10为审核未通过
												// 20为已结束。5为提交审核，此时商家不能再修改]
							&& er.getErbegin_time().before(date)) {
						if (ermap.containsKey(er_id)) {
							double last_price = (double) ermap.get(er_id);
							ermap.put(er_id, CommUtil.add(last_price, CommUtil.mul(gc.getCount(), gc.getPrice())));
							((List) erid_goodsids.get(er_id)).add(gc.getGoods().getId());
						} else {
							ermap.put(er_id, CommUtil.mul(gc.getCount(), gc.getPrice()));
							List list = new ArrayList();
							list.add(gc.getGoods().getId());
							erid_goodsids.put(er_id, list);
						}
					}
				}
			}
		} else {
			String[] gc_ids = gcs.split(",");
			for (GoodsCart gc : carts) {
				for (String gc_id : gc_ids) {
					if (gc.getId().equals(CommUtil.null2Long(gc_id))) {
						Goods goods = gc.getGoods();
						all_price = CommUtil.add(all_price, CommUtil.mul(gc.getCount(), gc.getPrice()));
						// 获取商品实时价格goods_current_price
						/*
						 * all_price = CommUtil.add(all_price,
						 * CommUtil.mul(gc.getCount(),
						 * goods.getGoods_current_price()));
						 */
						/*
						 * if (goods.getInventory_type().equals("spec")) {
						 * goods_current_price = CommUtil.null2Int(this
						 * .generic_default_info_color(goods, gc.getCart_gsp(),
						 * gc.getColor()).get("price")); all_price =
						 * CommUtil.add(all_price, CommUtil.mul(gc.getCount(),
						 * goods.getGoods_current_price())); }
						 */
						if (gc.getGoods().getEnough_reduce() == 1) {// 是满就减商品，记录金额
							String er_id = gc.getGoods().getOrder_enough_reduce_id();
							EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
							if (er.getErstatus() == 10 && er.getErbegin_time().before(date)) {
								if (ermap.containsKey(er_id)) {
									double last_price = (double) ermap.get(er_id);
									ermap.put(er_id,
											CommUtil.add(last_price, CommUtil.mul(gc.getCount(), gc.getPrice())));
									((List) erid_goodsids.get(er_id)).add(gc.getGoods().getId());
								} else {
									ermap.put(er_id, CommUtil.mul(gc.getCount(), gc.getPrice()));
									List list = new ArrayList();
									list.add(gc.getGoods().getId());
									erid_goodsids.put(er_id, list);
								}
							}
						}
					}
				}
			}
		}
		double all_enough_reduce = 0;
		for (String er_id : ermap.keySet()) {
			EnoughReduce er = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
			String erjson = er.getEr_json();
			double er_money = ermap.get(er_id);// 购物车中的此类满减的金额
			Map fromJson = (Map) Json.fromJson(erjson);
			double reduce = 0;
			String erstr = "";
			for (Object enough : fromJson.keySet()) {
				if (er_money >= CommUtil.null2Double(enough)) {
					reduce = CommUtil.null2Double(fromJson.get(enough));
					// erstr = "The activity products already bought full"+
					// enough +" AED. Has been reduced "+ reduce +" AED";
					erstr = "Full AED " + enough + " minus " + reduce;
					erid_goodsids.put("enouhg_" + er_id, enough);
				}
			}
			erString.put(er.getId(), erstr);
			er_json.put(er.getId(), fromJson);
			erid_goodsids.put("all_" + er_id, er_money);
			erid_goodsids.put("reduce_" + er_id, reduce);
			all_enough_reduce = CommUtil.add(all_enough_reduce, reduce);

		}
		Map prices = new HashMap();
		prices.put("er_json", Json.toJson(erid_goodsids, JsonFormat.compact()));
		prices.put("erString", erString);
		prices.put("er_info", er_json);
		double d2 = Math.round(all_price * 100) / 100.0;
		BigDecimal bd = new BigDecimal(d2);
		BigDecimal bd2 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		prices.put("all", CommUtil.null2Double(bd2));// 商品总价

		double er = Math.round(all_enough_reduce * 100) / 100.0;
		BigDecimal erbd = new BigDecimal(er);
		BigDecimal erbd2 = erbd.setScale(2, BigDecimal.ROUND_HALF_UP);
		prices.put("reduce", CommUtil.null2Double(erbd2));// 满减价格

		double af = Math.round((all_price - all_enough_reduce) * 100) / 100.0;
		BigDecimal afbd = new BigDecimal(af);
		BigDecimal afbd2 = afbd.setScale(2, BigDecimal.ROUND_HALF_UP);
		prices.put("after", CommUtil.null2Double(afbd2));// 减后价格
		return prices;
	}

	/**
	 * @description 获取用户勾选购物车对应店铺满减活动差价
	 * @param gsc
	 */
	public List calcActivityPricedifference(String gsc) {
		Map<String, Object> map = new HashMap<String, Object>();
		Set<Long> storeIds = new HashSet<Long>();
		Map<Long, String> erString = new HashMap<Long, String>();
		List<GoodsCart> normalList = new ArrayList<GoodsCart>();// 店铺整体购物车
		Map<Long, List<GoodsCart>> enoughReduceMap = new HashMap<Long, List<GoodsCart>>();// 满就减购物车enough_reduce
		if (gsc != null && !gsc.equals("")) {
			String ids[] = gsc.split(",");
			for (String id : ids) {
				GoodsCart obj = this.goodsCartService.getObjById(CommUtil.null2Long(id));
				normalList.add(obj);
				storeIds.add(obj.getGoods().getGoods_store().getId());
				if (obj.getGoods().getGoods_status() == 0 && obj.getGoods().getGoods_store().getStore_status() == 15
						&& obj.getGoods().getEnough_reduce() == 1) {
					String reduceId = obj.getGoods().getOrder_enough_reduce_id();
					EnoughReduce enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(reduceId));
					if (enoughReduce.getErstatus() == 10 && enoughReduce.getErbegin_time().before(new Date())
							&& enoughReduce.getErend_time().after(new Date())) {
						if (enoughReduceMap.containsKey(enoughReduce.getId())) {
							enoughReduceMap.get(enoughReduce.getId()).add(obj);
						} else {
							List<GoodsCart> list = new ArrayList<GoodsCart>();
							list.add(obj);
							enoughReduceMap.put(enoughReduce.getId(), list);
							erString.put(enoughReduce.getId(), enoughReduce.getEr_json());
						}
					}
				}
			}
		}
		if (!enoughReduceMap.isEmpty() || normalList.size() > 0) {
			List<GoodsCart> ermapList = new ArrayList<GoodsCart>();// 满减购物车列表
			List<GoodsCart> goodsCartList = new ArrayList<GoodsCart>();
			List<List<Map<String, Object>>> enoughReduceGoodsList = new ArrayList<List<Map<String, Object>>>();
			List<Map<String, Object>> storeActivityList = new ArrayList<Map<String, Object>>(); // 整体店铺活动信息
			// 多店铺活动
			for (Long id : storeIds) {
				double goodsAmount = 0.0;// 满减商品商品总金额
				double all_price = 0.0;// 店铺商品总金额
				double storePrice = 0.0;// 店铺总金额
				double reduceAmount = 0;// 满减总金额
				double reducePrice = 0.0;// 满减差价
				double postagePrice = 0.0;// 满包邮差价
				// double activityGoodsAllPrice = 0.0;// 商品总金额 根据购物车获取商品实时金额
				Store store = this.storeService.getObjById(CommUtil.null2Long(id));
				for (GoodsCart gc : normalList) {
					if (gc.getGoods().getGoods_store().getId().equals(store.getId())) {
						all_price = CommUtil.add(all_price, CommUtil.mul(gc.getCount(), gc.getPrice()));
					}
				}
				List<Map<String, Object>> activityList = new ArrayList<Map<String, Object>>(); // 单个店铺活动信息
				Map<String, Object> storeActivityMap = new HashMap<String, Object>();
				for (Long key : enoughReduceMap.keySet()) {
					Map<String, Object> activityMap = new HashMap<String, Object>();
					EnoughReduce enoughReduce = this.enoughReduceService.getObjById(key);
					if (enoughReduce != null && enoughReduce.getStore_id().equals(CommUtil.null2String(id))) {
						ermapList = enoughReduceMap.get(key);
						if (ermapList.size() > 0) {
							List<Map<String, Object>> goodsList = new ArrayList<Map<String, Object>>();
							for (GoodsCart cart : ermapList) {
								Map<String, Object> goodsMap = new HashMap<String, Object>();
								// Goods goods = cart.getGoods();
								goodsAmount = CommUtil.add(goodsAmount, CommUtil.mul(cart.getCount(), cart.getPrice()));
								// 获取商品实时价格
								/*
								 * if (goods.getInventory_type().equals("spec"))
								 * { goods_current_price = CommUtil.null2Double(
								 * this.generic_default_info_color(goods,
								 * cart.getCart_gsp(), cart.getColor())
								 * .get("price")); activityAllPrice =
								 * CommUtil.add(activityAllPrice,
								 * CommUtil.mul(cart.getCount(),
								 * goods_current_price)); }else{
								 * activityAllPrice =
								 * CommUtil.add(activityAllPrice,
								 * CommUtil.mul(cart.getCount(),
								 * goods.getGoods_current_price())); }
								 */
								goodsMap.put("goods_id", cart.getGoods().getId());
								goodsMap.put("goods_name", cart.getGoods().getGoods_name());
								goodsMap.put("goods_img",
										cart.getGoods().getGoods_main_photo() != null
												? this.configService.getSysConfig().getImageWebServer() + "/"
														+ cart.getGoods().getGoods_main_photo().getPath() + "/"
														+ cart.getGoods().getGoods_main_photo().getName()
												: "");

								goodsList.add(goodsMap);
							}
							activityMap.put("goodsList", goodsList);
							storeActivityMap.put("goodsAmount", goodsAmount);
							LinkedHashMap er_json = JSONObject.parseObject(enoughReduce.getEr_json(),
									LinkedHashMap.class, Feature.OrderedField);
							activityMap.put("er_json", er_json);
							if (!er_json.isEmpty()) {
								Iterator iterator = er_json.keySet().iterator();
								double price = 0.0;// 当前活动已优惠金额
								boolean flag = true;
								while (iterator.hasNext()) {
									Object key1 = iterator.next();
									Object value = er_json.get(key1);
									if (CommUtil.subtract(goodsAmount, key1) < 0) {// 有未满足活动计算差价
										flag = false;
										activityMap.put("key", key1);
										activityMap.put("value", value);
										reducePrice = CommUtil.subtract(key1, goodsAmount);
										activityMap.put("reducePrice", reducePrice);
										break;
									}
									if (CommUtil.subtract(goodsAmount, key1) >= 0) {
										price = CommUtil.null2Double(value);
										activityMap.put("key", key1);
										activityMap.put("value", value);
										activityMap.put("reducePrice", reducePrice);
									}
								}
								reduceAmount = CommUtil.add(reduceAmount, price);
								if (flag) {
									goodsAmount = CommUtil.subtract(goodsAmount, price);
								}
							}
							storePrice += goodsAmount;
						}

						activityMap.put("er_id", enoughReduce.getId());
						activityList.add(activityMap);
					}
				}
				// 计算店铺满包邮
				if (store.getEnough_free() == 1) {
					postagePrice = CommUtil.subtract(store.getEnough_free_price(),
							CommUtil.subtract(all_price, reduceAmount));
				}
				storeActivityMap.put("activityList", activityList);
				storeActivityMap.put("reduceAmount", reduceAmount);
				storeActivityMap.put("storePrice", CommUtil.subtract(all_price, reduceAmount));
				storeActivityMap.put("postagePrice", postagePrice > 0 ? postagePrice : 0);
				storeActivityMap.put("store", this.storeTools.queryStore(store));
				storeActivityList.add(storeActivityMap);
			}
			return storeActivityList;
		}
		return null;

	}

	/**
	 * @description 计算订单页面价格
	 * @param mulitid
	 * @param area_abbr
	 * @param area_id
	 * @param store_id
	 * @return
	 */
	public Map orderCartPrice(String mulitid, String area_abbr, String area_id, String store_id) {
		String[] gc_ids = mulitid.split(",");
		Set<Long> list = new HashSet<Long>();
		String gcs = "";
		Map map = new HashMap();
		List<Map> orderlist = new ArrayList<Map>();
		Map ordermap = new HashMap();
		double goods_amount = 0;
		double store_goods_total_price = 0.00;
		double store_total_price = 0.00;
		double store_order_total_price = 0;
		double order_tota_price = 0.00;
		Object ship_price = 0.00;
		String[] store_ids = store_id.split(",");
		Map smap = new HashMap();
		for (String id : store_ids) {
			List<GoodsCart> gc_list = new ArrayList<GoodsCart>();
			for (String cid : gc_ids) {
				GoodsCart gc = this.goodsCartService.getObjById(CommUtil.null2Long(cid));
				if (CommUtil.null2String(gc.getGoods().getGoods_store().getId()).equals(id)) {
					gc_list.add(gc);
				}
			}
			smap.put(id, gc_list);
		}

		for (String id : store_ids) {
			Map storemap = new HashMap();
			Store store = this.storeService.getObjById(CommUtil.null2Long(id));
			storemap.put("store_id", store.getId());
			storemap.put("store_name", store.getStore_name());
			List<GoodsCart> sgc_list = (List<GoodsCart>) smap.get(id);
			for (GoodsCart gc : sgc_list) {
				if (gc.getGoods() != null && CommUtil.null2String(gc.getGoods().getGoods_store().getId())
						.equals(CommUtil.null2String(id))) {
					if (gc.getGoods().getGoods_store().getStore_status() == 15) {
						if (sgc_list != null && sgc_list.size() > 0) {
							store_goods_total_price = this.calCartPrice(sgc_list, "");
							/*
							 * goods_amount = CommUtil.mul(gc.getCount(),
							 * gc.getPrice());
							 * 
							 * store_goods_total_price =
							 * CommUtil.add(goods_amount,
							 * store_goods_total_price);
							 */
						}
					}
				}
			}

			// 匹配地址库
			boolean enable = true;
			if (this.configService.getSysConfig().getServer_version() == 2) {
				if (store.getTransport() != null && store.getTransport().getExpress_company() != null) {
					if (store.getTransport().getExpress_company().getEnabled() == 1) {
						enable = this.matching(store, area_abbr);
					}
				}
			}
			if (store.getEnough_free() == 1
					&& CommUtil.subtract(store.getEnough_free_price(), store_goods_total_price) < 0) {
				store_total_price = store_goods_total_price;
			} else {
				if (enable) {
					List<SysMap> sysList = new ArrayList<SysMap>();
					sysList = transportTools.transportation_cart2(sgc_list, null, null, area_id);
					List sys_info = new ArrayList();
					Map sysmap = new HashMap();
					for (SysMap obj : sysList) {
						if (obj.getKey().equals("Express")) {
							ship_price = obj.getValue();
						}
					}
					sysmap.put("ship_price", ship_price);
					sys_info.add(sysmap);
					storemap.put("sys_info", sys_info);
				}
				store_total_price = CommUtil.add(ship_price, store_goods_total_price);
			}
			storemap.put("store_goods_total_price", store_goods_total_price);
			storemap.put("store_total_price", store_total_price);
			orderlist.add(storemap);
			order_tota_price = CommUtil.add(order_tota_price, store_total_price);
		}
		ordermap.put("store", orderlist);
		ordermap.put("order_tota_price", order_tota_price);
		return ordermap;
	}

	// 计算全网通用价格
	public void Platform_activity(String coupon_id, Long order_id) {
		CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
		if (coupon != null) {
			coupon.setStatus(1);
			this.couponInfoService.update(coupon);
			OrderForm order = this.orderService.getObjById(order_id);
			double order_goods_amount = 0.0;
			if (order != null) {
				if (order.getChild_order_detail() != null && !order.getChild_order_detail().isEmpty()) {
					order_goods_amount = order.getGoods_amount().doubleValue();
					List<Map> list = orderTools.queryGoodsInfo(order.getChild_order_detail());
					for (Map map : list) {
						OrderForm ChildOrderForm = this.orderService
								.getObjById(CommUtil.null2Long(map.get("order_id")));
						order_goods_amount = CommUtil.add(ChildOrderForm.getGoods_amount(), order_goods_amount);
					}
					// if
					// (order_goods_amount.compareTo(coupon.getCoupon().getCoupon_order_amount().doubleValue())
					// == 1) {
					if (CommUtil.subtract(order_goods_amount, coupon.getCoupon().getCoupon_order_amount()) > 0) {
						double rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order_goods_amount);
						this.general_coupon(order, coupon, rate);
					}
				} else {
					Map coupon_map = new HashMap();
					coupon_map.put("couponinfo_id", coupon.getId());
					coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
					coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
					double coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
					double rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order.getGoods_amount());
					coupon_map.put("coupon_goods_rate", rate);
					coupon_map.put("rate_price", coupon_price);
					order.setGeneral_coupon(Json.toJson(coupon_map, JsonFormat.compact()));
					order.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(order.getTotalPrice(), coupon_price)));
					this.orderService.update(order);
				}
			}
		}

	}

	public void coupon_price(String coupon_id, Long order_id) {
		CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
		if (coupon != null) {
			coupon.setStatus(1);
			this.couponInfoService.update(coupon);
			OrderForm order = this.orderService.getObjById(order_id);
			double order_goods_amount = 0.0;
			double order_free_express_price = 0.0;// 订单可使用金额
			if (order != null) {
				if (order.getChild_order_detail() != null && !order.getChild_order_detail().isEmpty()) {
					order_goods_amount = order.getGoods_amount().doubleValue();
					List<Map> list = orderTools.queryGoodsInfo(order.getChild_order_detail());
					for (Map map : list) {
						OrderForm ChildOrderForm = this.orderService
								.getObjById(CommUtil.null2Long(map.get("order_id")));
						order_goods_amount = CommUtil.add(ChildOrderForm.getGoods_amount(), order_goods_amount);
					}
					// if
					// (order_goods_amount.compareTo(coupon.getCoupon().getCoupon_order_amount().doubleValue())
					// == 1) {
					if (CommUtil.subtract(order_goods_amount, coupon.getCoupon().getCoupon_order_amount()) > 0) {
						double rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order_goods_amount);
						this.general_coupon(order, coupon, rate);
					}
				} else {
					Map coupon_map = new HashMap();
					coupon_map.put("couponinfo_id", coupon.getId());
					coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
					coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
					double coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
					double rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order.getGoods_amount());
					coupon_map.put("coupon_goods_rate", rate);
					coupon_map.put("rate_price", coupon_price);
					order.setGeneral_coupon(Json.toJson(coupon_map, JsonFormat.compact()));
					order.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(order.getTotalPrice(), coupon_price)));
					this.orderService.update(order);
					order_goods_amount = order.getGoods_amount().doubleValue();
				}

				// 平台满包邮
				boolean ship_flag = false;
				if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
					if (CommUtil.subtract(order_goods_amount,
							this.configService.getSysConfig().getEnoughfree_price()) >= 0) {
						// 清空订单运费价格-并记录平台运费金额
					} else {
						ship_flag = true;
					}
				}
			}
		}

	}

	// 计算优惠价格
	public void general_coupon(OrderForm order, CouponInfo coupon, double rate) {
		if (order != null) {
			double coupon_price = 0;
			if (order.getChild_order_detail() != null && !order.getChild_order_detail().equals("")) {
				List<Map> orders = orderTools.queryGoodsInfo(order.getChild_order_detail());
				double price = 0.0;
				for (Map map : orders) {
					OrderForm childOrder = this.orderService.getObjById(CommUtil.null2Long(map.get("order_id")));
					double rate_price = CommUtil.mul(childOrder.getGoods_amount(), rate);
					price = CommUtil.add(rate_price, price);
					Map couponMap = new HashMap();
					couponMap.put("couponinfo_id", coupon.getId());
					couponMap.put("couponinfo_sn", coupon.getCoupon_sn());
					couponMap.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
					couponMap.put("coupon_goods_rate", rate);
					couponMap.put("rate_price", rate_price);
					childOrder.setGeneral_coupon(Json.toJson(couponMap, JsonFormat.compact()));
					childOrder.setTotalPrice(
							BigDecimal.valueOf(CommUtil.subtract(childOrder.getTotalPrice(), rate_price)));
					this.orderService.update(childOrder);
				}
				Map coupon_map = new HashMap();
				coupon_map.put("couponinfo_id", coupon.getId());
				coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
				coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
				coupon_map.put("coupon_goods_rate", rate);
				coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
				double rate_price = CommUtil.subtract(coupon_price, price);// 优惠总金额减子订单优惠总金额=剩余优惠金额（主订单优惠金额）
				coupon_map.put("rate_price", rate_price);
				order.setGeneral_coupon(Json.toJson(coupon_map, JsonFormat.compact()));
				order.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(order.getTotalPrice(), rate_price)));
				this.orderService.update(order);
			} else {
				Map coupon_map = new HashMap();
				coupon_map.put("couponinfo_id", coupon.getId());
				coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
				coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
				coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
				coupon_map.put("coupon_goods_rate", rate);
				coupon_map.put("rate_price", coupon_price);
				order.setGeneral_coupon(Json.toJson(coupon_map, JsonFormat.compact()));
				order.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(order.getTotalPrice(), coupon_price)));
				this.orderService.update(order);
			}
		}
	}

	public void Platform_activity(String coupon_id, Long order_id, String integral) {
		CouponInfo coupon = this.couponInfoService.getObjById(CommUtil.null2Long(coupon_id));
		// if (coupon != null) {
		OrderForm order = this.orderService.getObjById(order_id);
		double order_goods_amount = 0.0;
		double order_free_express_price = 0.0;// 订单可使用金额
		if (order != null) {
			User user = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
			if (order.getChild_order_detail() != null && !order.getChild_order_detail().isEmpty()) {
				order_goods_amount = order.getGoods_amount().doubleValue();
				List<Map> list = orderTools.queryGoodsInfo(order.getChild_order_detail());
				for (Map map : list) {
					OrderForm ChildOrderForm = this.orderService.getObjById(CommUtil.null2Long(map.get("order_id")));
					order_goods_amount = CommUtil.add(ChildOrderForm.getGoods_amount(), order_goods_amount);
				}
				// if(order_goods_amount.compareTo(coupon.getCoupon().getCoupon_order_amount().doubleValue())==
				// 1) {
				// 优惠券
				double rate = 0.0;
				boolean coupon_flag = false;
				if (coupon != null && coupon.getStatus() == 0
						&& coupon.getCoupon().getCoupon_end_time().after(new Date())) {
					if (CommUtil.subtract(order_goods_amount, coupon.getCoupon().getCoupon_order_amount()) > 0) {
						coupon.setStatus(1);
						this.couponInfoService.update(coupon);
						rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order_goods_amount);
						coupon_flag = true;
					}
				}

				// 积分
				double integrla_rate = 0.0;
				boolean integral_flag = false;
				double integral_price = 0.0;
				if (this.configService.getSysConfig().isIntegral()) {
					if (CommUtil.null2Int(integral) == 1) {
						int use_integral = user.getIntegral();
						integral_price = CommUtil.mul(use_integral,
								this.configService.getSysConfig().getIntegralExchangeRate());
						if (integral_price > 0) {
							integrla_rate = CommUtil.div(integral_price, order_goods_amount);
							integral_flag = true;

							if (CommUtil.subtract(integral_price, order_goods_amount) >= 0) {
								user.setIntegral(0);
							} else {
								user.setIntegral(new Double(
										CommUtil.mul(this.configService.getSysConfig().getIntegralExchangeRate(),
												CommUtil.subtract(order_goods_amount, integral_price))).intValue());
							}
							this.userService.update(user);
						}
					}
				}

				// 平台满包邮
				boolean ship_flag = false;
				if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
					if (CommUtil.subtract(order_goods_amount,
							this.configService.getSysConfig().getEnoughfree_price()) >= 0) {
						// 清空订单运费价格-并记录平台运费金额
						ship_flag = true;
					}
				}
				this.calculate_platform_activity(order, coupon, rate, ship_flag, coupon_flag, integral_flag,
						integrla_rate, integral_price);
			} else {
				double order_total_price = order.getTotalPrice().doubleValue();
				order_goods_amount = order.getGoods_amount().doubleValue();// 订单商品总价--店铺优惠后
				if (coupon != null && coupon.getStatus() == 0
						&& coupon.getCoupon().getCoupon_end_time().after(new Date())) {
					if (CommUtil.subtract(order_goods_amount, coupon.getCoupon().getCoupon_order_amount()) > 0) {
						double coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
						if (CommUtil.subtract(order_goods_amount, coupon.getCoupon().getCoupon_order_amount()) > 0) {
							double rate = CommUtil.div(coupon.getCoupon().getCoupon_amount(), order.getGoods_amount());
							Map coupon_map = new HashMap();
							coupon_map.put("couponinfo_id", coupon.getId());
							coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
							coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
							coupon_map.put("coupon_goods_rate", rate);
							coupon_map.put("rate_price", coupon_price);
							order.setGeneral_coupon(Json.toJson(coupon_map, JsonFormat.compact()));
							order_total_price = CommUtil.subtract(order_total_price, coupon_price);
						}
					}
				}

				// 积分 - 记录订单积分优惠金额
				if (this.configService.getSysConfig().isIntegral()) {
					if (CommUtil.null2Int(integral) == 1) {
						int use_integral = user.getIntegral();
						double integral_price = CommUtil.mul(use_integral,
								this.configService.getSysConfig().getIntegralExchangeRate());
						if (integral_price > 0) {
							order_total_price = CommUtil.subtract(order_total_price, integral_price) >= 0
									? CommUtil.subtract(order_total_price, integral_price) : 0;
							// 扣除用户积分--订单商品总价--不含运费
							if (CommUtil.subtract(integral_price, order_goods_amount) >= 0) {
								user.setIntegral(0);
							} else {
								user.setIntegral(new Double(
										CommUtil.mul(this.configService.getSysConfig().getIntegralExchangeRate(),
												CommUtil.subtract(order_goods_amount, integral_price))).intValue());
							}
							this.userService.update(user);
						}
					}
				}
				// 平台满包邮-记录订单满包邮优惠金额
				boolean ship_flag = false;
				if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
					if (CommUtil.subtract(order_goods_amount,
							this.configService.getSysConfig().getEnoughfree_price()) >= 0) {
						// 清空订单运费价格-并记录平台运费金额
						order_total_price = CommUtil.subtract(order_total_price, order.getShip_price());
						order.setPlatform_ship_price(order.getShip_price());
						order.setShip_price(BigDecimal.valueOf(0));

					}
				}
				order.setTotalPrice(BigDecimal.valueOf(order_total_price));
				this.orderService.update(order);
			}
		}
		// }

	}

	public void calculate_platform_activity(OrderForm order, CouponInfo coupon, double rate, boolean ship_flag,
			boolean coupon_flag, boolean integral_flag, double integrla_rate, double integral_price) {
		if (order != null) {
			if (order.getChild_order_detail() != null && !order.getChild_order_detail().equals("")) {
				List<Map> orders = orderTools.queryGoodsInfo(order.getChild_order_detail());
				double price = 0.0;
				double integral_amount = 0.0;
				// 子订单
				for (Map map : orders) {
					OrderForm childOrder = this.orderService.getObjById(CommUtil.null2Long(map.get("order_id")));
					double order_total_price = childOrder.getTotalPrice().doubleValue();
					double order_goods_amount = childOrder.getGoods_amount().doubleValue();
					if (coupon_flag) {
						double rate_price = CommUtil.mul(order_goods_amount, rate);
						price = CommUtil.add(rate_price, price);
						Map couponMap = new HashMap();
						couponMap.put("couponinfo_id", coupon.getId());
						couponMap.put("couponinfo_sn", coupon.getCoupon_sn());
						couponMap.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
						couponMap.put("coupon_goods_rate", rate);
						couponMap.put("rate_price", rate_price);
						childOrder.setGeneral_coupon(Json.toJson(couponMap, JsonFormat.compact()));
						order_total_price = CommUtil.subtract(order_total_price, rate_price);
					}
					// 积分
					if (integral_flag) {
						double integral_rate_price = CommUtil.mul(order_goods_amount, integrla_rate);
						order_total_price = CommUtil.subtract(order_total_price, integral_rate_price);
						integral_amount = CommUtil.add(integral_amount, integral_rate_price);
					}
					if (ship_flag) {// 如果平台包邮 && 用户承担运费 清空用户运费，并记录为平台包邮
						// 清空订单运费价格-并记录平台运费金额
						order_total_price = CommUtil.subtract(order_total_price, childOrder.getShip_price());
						childOrder.setPlatform_ship_price(childOrder.getShip_price());
						childOrder.setShip_price(BigDecimal.valueOf(0));
					}
					childOrder.setTotalPrice(BigDecimal.valueOf(order_total_price));
					this.orderService.update(childOrder);
				}
				// 主订单
				// begin
				/*
				 * Map coupon_map = new HashMap();
				 * coupon_map.put("couponinfo_id", coupon.getId());
				 * coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
				 * coupon_map.put("coupon_amount",
				 * coupon.getCoupon().getCoupon_amount());
				 * coupon_map.put("coupon_goods_rate", rate); double
				 * coupon_price =
				 * coupon.getCoupon().getCoupon_amount().doubleValue(); double
				 * rate_price = CommUtil.subtract(coupon_price, price);//
				 * 优惠总金额减子订单优惠总金额=剩余优惠金额（主订单优惠金额） coupon_map.put("rate_price",
				 * rate_price); order.setGeneral_coupon(Json.toJson(coupon_map,
				 * JsonFormat.compact()));
				 * order.setTotalPrice(BigDecimal.valueOf(CommUtil.subtract(
				 * order.getTotalPrice(), rate_price))); if (flag) {// 如果平台包邮 &&
				 * 用户承担运费 清空用户运费，并记录为平台包邮 // 清空订单运费价格-并记录平台运费金额
				 * order.setPlatform_ship_price(order.getShip_price());
				 * order.setShip_price(BigDecimal.valueOf(0)); }
				 * this.orderService.update(order);
				 */
				double order_total_price = order.getTotalPrice().doubleValue();
				double order_goods_amount = order.getGoods_amount().doubleValue();// 订单商品总价--店铺优惠后
				if (coupon_flag) {
					double coupon_price = coupon.getCoupon().getCoupon_amount().doubleValue();
					double mian_rate_price = CommUtil.subtract(coupon_price, price);// 优惠总金额减子订单优惠总金额=剩余优惠金额（主订单优惠金额）
					Map main_coupon_map = new HashMap();
					main_coupon_map.put("couponinfo_id", coupon.getId());
					main_coupon_map.put("couponinfo_sn", coupon.getCoupon_sn());
					main_coupon_map.put("coupon_amount", coupon.getCoupon().getCoupon_amount());
					main_coupon_map.put("coupon_goods_rate", rate);
					main_coupon_map.put("rate_price", mian_rate_price);
					order.setGeneral_coupon(Json.toJson(main_coupon_map, JsonFormat.compact()));
					order_total_price = CommUtil.subtract(order_total_price, mian_rate_price);
				}

				// 积分
				if (this.configService.getSysConfig().isIntegral()) {
					if (integral_flag) {
						double main_integral_price = CommUtil.subtract(integral_price, integral_amount);
						order_total_price = CommUtil.subtract(order_total_price, main_integral_price);
					}
				}
				// 平台满包邮
				if (ship_flag) {
					// 清空订单运费价格-并记录平台运费金额
					order_total_price = CommUtil.subtract(order_total_price, order.getShip_price());
					order.setPlatform_ship_price(order.getShip_price());
					order.setShip_price(BigDecimal.valueOf(0));
				}
				order.setTotalPrice(BigDecimal.valueOf(order_total_price));
				this.orderService.update(order);
				// end
			}
		}
	}

	public double getUserIntegral(String integral, User user) {
		double integral_price = 0;
		if (this.configService.getSysConfig().isIntegral()) {
			if (CommUtil.null2Int(integral) == 1) {
				int use_integral = user.getIntegral();
				integral_price = CommUtil.mul(use_integral,
						this.configService.getSysConfig().getIntegralExchangeRate());
			}
		}
		return integral_price;
	}

	/**
	 * 根据店铺ID获取优惠全ID(弃用)
	 */
	public static String coupon(String couponId, String storeId) {
		if (couponId != null && !couponId.equals("")) {
			String[] store_coupon = couponId.split(",");
			Map map = new HashMap();
			for (String id : store_coupon) {
				String store_id = id.substring(0, id.indexOf("_"));
				String coupon_id = id.substring(id.indexOf("_") + 1, id.length());
				map.put(store_id, coupon_id);
			}
			return (String) map.get(storeId);
		}
		return null;
	}

	/**
	 * @description buyNow 获取商品佣金
	 * @param obj
	 * @param count
	 * @return
	 */
	public double getGoodsOrderCommission(Goods obj, int count) {
		double commission_price = CommUtil.mul(obj.getGc().getCommission_rate(),
				CommUtil.mul(obj.getGoods_price(), count));
		return commission_price;
	}

	public String generateCartSession(HttpServletRequest request, HttpServletResponse response, String cart_session) {
		String cart_session_id = "";
		if (null != cart_session && !"".equals(cart_session)) {
			cart_session_id = cart_session;
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
		return cart_session_id;
	}

	/**
	 * @description 根据商品id查询颜色规格
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> cartGoodsColor(String id, String color) {
		Map<String, Object> map = new HashMap<String, Object>();
		Goods goods = null;
		if (id != null && !id.equals("")) {
			goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			List<CGoods> childGoods = goods.getCgoods();
			Set<Map<String, Object>> set = new HashSet<Map<String, Object>>();
			Map<String, Object> valueMap = new HashMap<String, Object>();
			for (CGoods obj : childGoods) {
				if (obj.getSpec_color() != null && !"".equals(obj.getSpec_color())) {
					Map<String, Object> colorMap = new HashMap<String, Object>();
					colorMap.put("value", obj.getSpec_color());
					colorMap.put("photo",
							obj.getGoods_photos().size() > 0
									? this.configService.getSysConfig().getImageWebServer() + "/"
											+ obj.getGoods_photos().get(0).getPath() + "/"
											+ obj.getGoods_photos().get(0).getName()
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
											+ this.configService.getSysConfig().getGoodsImage().getName());
					set.add(colorMap);
					if (obj.getSpec_color().equals(color)) {
						valueMap.put("color", color);
					}
				}
			}
			map.put("key", valueMap);
			map.put("color", set);
			return map;
		}
		return null;
	}

	/**
	 * @description Collections app商品规格信息 将商品属性归类便于前台展示
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> cartGenericSpec(String id, String spec_id) {
		Result result = null;
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
		String spec_ids[] = spec_id.split(",");
		List<String> list = Arrays.asList(spec_ids);
		List<Map<String, Object>> spec_list = new ArrayList<Map<String, Object>>();
		for (GoodsSpecification goodsSpecification : specs) {
			Map<String, Object> goodsSpecificationMap = new HashMap<String, Object>();
			goodsSpecificationMap.put("id", goodsSpecification.getId());
			goodsSpecificationMap.put("spec_name", goodsSpecification.getName());
			String gsp_id = "";
			List<GoodsSpecProperty> goodsSpecPropertyList = goods.getGoods_specs();
			List<Map<String, Object>> gsp_list = new ArrayList<Map<String, Object>>();
			for (GoodsSpecProperty goodsSpecProperty : goodsSpecPropertyList) {
				Map<String, Object> gsp_map = new HashMap<String, Object>();
				if (goodsSpecProperty.getSpec().getId() == goodsSpecification.getId()) {
					/*
					 * for(Object obj : list){
					 * if(CommUtil.null2Long(obj).equals(goodsSpecProperty.getId
					 * ())){ gsp_id = goodsSpecProperty.getId().toString(); } }
					 */
					Long ids = goodsSpecProperty.getId();
					if (list.contains(ids.toString())) {
						gsp_id = goodsSpecProperty.getId().toString();
					}
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
			goodsSpecificationMap.put("gsp_id", gsp_id);
			goodsSpecificationMap.put("gsp_list", gsp_list);
			spec_list.add(goodsSpecificationMap);
		}
		map.put("spec_info", spec_list);
		return map;
	}

	/**
	 * @description 合并用户购物车和游客身份购物车
	 * @param request
	 * @param user
	 * @param cart_session_id
	 * @return
	 */
	public List<GoodsCart> cartListCalc(HttpServletRequest request, User user, String cart_session_id) {
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
				params.put("cart_status1", 2);
				params.put("deleteStatus", 0);
				cookieCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.deleteStatus=:deleteStatus and obj.cart_status=:cart_status or obj.cart_status=:cart_status1 ",
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
				params.put("cart_status1", 2);
				params.put("deleteStatus", 0);
				userCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.deleteStatus=:deleteStatus and obj.cart_status=:cart_status or obj.cart_status=:cart_status1 "
						+ "and obj.deleteStatus=:deleteStatus",
						params, -1, -1);
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("cart_status", 0);
				params.put("cart_status1", 2);
				params.put("deleteStatus", 0);
				userCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.user.id=:user_id and obj.deleteStatus=:deleteStatus and obj.cart_status=:cart_status or obj.cart_status=:cart_status1 "
						+ "and obj.deleteStatus=:deleteStatus",
						params, -1, -1);
			}
		} else {
			if (!"".equals(cart_session_id)) {
				params.clear();
				params.put("cart_session_id", cart_session_id);
				params.put("cart_status", 0);
				params.put("cart_status1", 2);
				params.put("deleteStatus", 0);
				cookieCartList = this.goodsCartService.query(
						"select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.deleteStatus=:deleteStatus and obj.cart_status=:cart_status or obj.cart_status=:cart_status1 "
						+ "and obj.deleteStatus=:deleteStatus",
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
					List<GoodsCart> repetitionList = new ArrayList<GoodsCart>();
					for (GoodsCart userCart : userCartList) {
						if (userCart.getGoods().getId().equals(cookieCart.getGoods().getId())) {
							yes = true;
							repetitionList.add(userCart);
						} else {
							no = true;
						}
					}
					if (yes) {
						boolean flag = false;
						for (GoodsCart repetition : repetitionList) {
							boolean colorFlag = false;
							boolean gspFlag = false;
							if (null != cookieCart.getColor() && !"".equals(cookieCart.getColor())) {
								if (cookieCart.getColor().equals(CommUtil.null2String(repetition.getColor()))) {
									colorFlag = true;
								}
							} else {
								colorFlag = true;
							}
							if (cookieCart.getSpec_info().equals(repetition.getSpec_info())) {
								gspFlag = true;
							}
							/*
							 * if (colorFlag && gspFlag) {
							 * this.goodsCartService.delete(cookieCart.getId());
							 * }
							 */
							if (colorFlag && gspFlag) {
								flag = true;
							}
						}
						if (!flag) {
							// 将cookieCart转换为userCart
							cookieCart.setCart_session_id(null);
							cookieCart.setUser(user);
							this.goodsCartService.update(cookieCart);
							cartsList.add(cookieCart);
						} else {
							this.goodsCartService.delete(cookieCart.getId());
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
	 * 
	 * @param obj
	 * @param color
	 * @param gsp
	 * @return
	 */
	/**
	 * 
	 * @param obj
	 *            订单商品
	 * @param gsp
	 *            购买规格
	 * @param color
	 *            购买颜色规格
	 * @param count
	 *            购买数量
	 * @param type
	 *            是否为兑换商品
	 * @return 商品价格Map集合
	 * @description buyNow 计算店铺满减商品价格
	 */
	public Map buyNowEnoughReducePrice(Goods obj, String gsp, String color, int count, String type) {
		Map map = new HashMap();
		if (obj != null) {
			double goods_price = 0; // 商品单价
			double reduce = 0; // 满减价格
			double goods_amount = 0; // 优惠前商品总价
			double after_goods_amount = 0; // 商品总价
			// 计算商品价格
			if (obj.getInventory_type().equals("spec")) {
				goods_price = CommUtil
						.null2Double(this.generic_default_info_color(obj, gsp, color).get("goods_current_price"));// 计算商品价格
				goods_amount = CommUtil.mul(goods_price, count);
			} else {
				goods_price = CommUtil.null2Double(obj.getGoods_current_price());
				goods_amount = CommUtil.mul(goods_price, count);
			}

			after_goods_amount = goods_amount;
			if (obj.getEnough_reduce() == 1) {// 满减
				String er_id = obj.getOrder_enough_reduce_id();
				EnoughReduce enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
				if (enoughReduce != null) {
					if (enoughReduce.getErstatus() == 10 && enoughReduce.getErbegin_time().before(new Date())) {
						Map er_json = (Map) Json.fromJson(enoughReduce.getEr_json());
						String er_string = "";
						double after = 0;
						for (Object enough : er_json.keySet()) {
							if (goods_amount >= CommUtil.null2Double(enough)) {
								reduce = CommUtil.null2Double(er_json.get(enough));
								double af = Math.round((goods_amount - reduce) * 100) / 100.0;
								BigDecimal afbd = new BigDecimal(af);
								BigDecimal afbd2 = afbd.setScale(2, BigDecimal.ROUND_HALF_UP);
								after = CommUtil.null2Double(afbd2);
								after_goods_amount = after;
								map.put("reduce", reduce);
								map.put("after", after);
							}
						}
					}
				}
			}
			if (type != null && type.equals("get") && obj.getPoint() == 1 || obj.getGoods_status() == 4) {
				after_goods_amount = 0;
				goods_price = 0;
			}
			map.put("goods_price", goods_price);
			map.put("after_goods_amount", after_goods_amount);
			map.put("goods_amount", goods_amount);
			return map;
		}
		return null;
	}

	public Map buyNowEnoughReducePriceV2(Goods obj, String gsp, String color, int count) {
		Map map = new HashMap();
		if (obj != null) {
			double goods_price = 0; // 商品单价
			double reduce = 0; // 满减价格
			double goods_amount = 0; // 优惠前商品总价
			double after_goods_amount = 0; // 商品总价
			// 计算商品价格
			if (obj.getInventory_type().equals("spec")) {
				goods_price = CommUtil
						.null2Double(this.generic_default_info_color(obj, gsp, color).get("goods_current_price"));// 计算商品价格
				goods_amount = CommUtil.mul(goods_price, count);
			} else {
				goods_price = CommUtil.null2Double(obj.getGoods_current_price());
				goods_amount = CommUtil.mul(goods_price, count);
			}

			after_goods_amount = goods_amount;
			if (obj.getEnough_reduce() == 1) {// 满减
				String er_id = obj.getOrder_enough_reduce_id();
				EnoughReduce enoughReduce = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
				if (enoughReduce != null) {
					if (enoughReduce.getErstatus() == 10 && enoughReduce.getErbegin_time().before(new Date())) {
						Map er_json = (Map) Json.fromJson(enoughReduce.getEr_json());
						String er_string = "";
						double after = 0;
						for (Object enough : er_json.keySet()) {
							if (goods_amount >= CommUtil.null2Double(enough)) {
								reduce = CommUtil.null2Double(er_json.get(enough));
								double af = Math.round((goods_amount - reduce) * 100) / 100.0;
								BigDecimal afbd = new BigDecimal(af);
								BigDecimal afbd2 = afbd.setScale(2, BigDecimal.ROUND_HALF_UP);
								after = CommUtil.null2Double(afbd2);
								after_goods_amount = after;
								map.put("reduce", reduce);
								map.put("after", after);
							}
						}
					}
				}
			}
			map.put("goods_price", goods_price);
			map.put("after_goods_amount", after_goods_amount);
			map.put("goods_amount", goods_amount);
			return map;
		}
		return null;
	}

	/**
	 * 
	 * @param obj
	 *            订单商品
	 * @param count
	 *            购买数量
	 * @param gsp
	 *            购买商品规格
	 * @param color
	 *            购买颜色规格
	 * @return
	 * @description order 封装商品信息
	 */
	public Map orderGoods(Goods obj, String count, String gsp, String color) {
		if (obj != null) {
			Map map = new HashMap();
			String imageWebServer = this.configService.getSysConfig().getImageWebServer();
			String spec_info = "";
			String[] gsp_ids = gsp.split(",");
			for (String gsp_id : gsp_ids) {
				GoodsSpecProperty spec_property = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gsp_id));
				if (spec_property != null) {
					spec_info = spec_property.getSpec().getName() + ":" + spec_property.getValue() + "<br> "
							+ spec_info;
				}
			}
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			map.put("ksa_goods_name", obj.getKsa_goods_name());
			map.put("goods_price", obj.getGoods_price());
			double goods_current_price = 0;
			int goods_inventory = 0;
			if (obj.getInventory_type().equals("spec")) {
				goods_current_price = CommUtil
						.null2Double(this.generic_default_info_color(obj, gsp, color).get("goods_current_price"));// 计算商品价格
				goods_inventory = CommUtil.null2Int(this.generic_default_info_color(obj, gsp, color).get("count"));// 计算商品库存信息
			} else {
				goods_current_price = CommUtil.null2Double(obj.getGoods_current_price());
				goods_inventory = obj.getGoods_inventory();
			}
			map.put("goods_current_price", goods_current_price);
			map.put("goods_count", count);
			map.put("goods_inventory", goods_inventory);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_sku", obj.getInventory_type().equals("all") ? obj.getGoods_serial()
					: this.generic_default_info_color(obj, gsp, color).get("sku"));
			map.put("goods_color", color);
			map.put("goods_gsp_ids", gsp);
			map.put("goods_gsp_val", spec_info);
			map.put("goods_weight", obj.getGoods_weight());
			map.put("goods_length", obj.getGoods_length());
			map.put("goods_width", obj.getGoods_width());
			map.put("goods_high", obj.getGoods_high());
			map.put("goods_mainphoto_path",
					obj.getGoods_main_photo() != null ? obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_all_price", CommUtil.mul(goods_current_price, count));
			map.put("goods_commission_price", this.getGoodsOrderCommission(obj, Integer.parseInt(count)));// 商品总佣金
			map.put("goods_commission_rate", obj.getGc().getCommission_rate());// 商品的佣金比例
			map.put("evaluate", 1);
			return map;
		}
		return null;
	}

	public List updateChildOrder(List<Map> order) {
		List<Map> list = new ArrayList<Map>();
		if (order.size() > 0) {
			for (Map map : order) {
				map.put("enough_free", 1);
				map.put("totalPrice", CommUtil.subtract(map.get("totalPrice"), map.get("ship_price")));
				map.put("ship_price", 0);
				list.add(map);
				OrderForm obj = this.orderService.getObjById(CommUtil.null2Long(map.get("order_id")));
				obj.setEnough_free(1);
				this.orderService.update(obj);
			}
			return list;
		}
		return null;

	}

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

	// 分离组合销售活动购物车,只显示主体套装商品购物车
	public Map<String, List<GoodsCart>> separateCombin(List<GoodsCart> carts) {
		Map<String, List<GoodsCart>> map = new HashMap<String, List<GoodsCart>>();
		List<GoodsCart> normal_carts = new ArrayList<GoodsCart>();
		List<GoodsCart> combin_carts = new ArrayList<GoodsCart>();
		for (GoodsCart cart : carts) {
			if (cart.getCart_type() != null && cart.getCart_type().equals("combin")) {
				if (cart.getCombin_main() == 1) {
					combin_carts.add(cart);
				}
			} else {
				normal_carts.add(cart);
			}
		}
		map.put("combin", combin_carts);
		map.put("normal", normal_carts);
		return map;
	}

	/**
	 * 商品佣金
	 * 
	 * @param request
	 * @param response
	 */
	public double getOrderCommission(List<GoodsCart> gcs) {
		double commission_price = 0.00;
		for (GoodsCart gc : gcs) {
			commission_price = commission_price + this.getGoodsCommission(gc);
		}
		return commission_price;
	}

	public Map<String, Double> getCommission(List<GoodsCart> gcs) {
		double commission = 0.00;
		double vat = 0.0;
		Map<String, Double> map = new HashMap<String, Double>();
		for (GoodsCart obj : gcs) {
			commission = CommUtil.add(commission, this.getGoodsCommission(obj));
			if (commission != 0) {
				vat = CommUtil.add(vat,
						this.getGoodsCommissionVat(commission, obj.getGoods().getGc().getCommission_vat()));
			}

		}
		map.put("commission", commission);
		map.put("vat", vat);
		return map;

	}

	/**
	 * 商品佣金
	 * 
	 * @param request
	 * @param response
	 */
	public double getGoodsCommission(GoodsCart gc) {
		double commission_price = CommUtil.mul(gc.getGoods().getGc().getCommission_rate(),
				CommUtil.mul(gc.getPrice(), gc.getCount()));
		return commission_price;
	}

	public double getGoodsCommissionVat(double commission, BigDecimal vat) {
		if (commission != 0) {
			return CommUtil.mul(commission, vat);
		}
		return 0;
	};

	/**
	 * 兑换商品下单 获得商品佣金
	 * 
	 * @param request
	 * @param response
	 */
	public double getGoodscartCommissionPoint(Goods obj) {
		double commission_price = CommUtil.mul(obj.getGc().getCommission_rate(), CommUtil
				.mul(obj.getGoods_current_price() == null ? obj.getGoods_price() : obj.getGoods_current_price(), 1));
		return commission_price;
	}

	/**
	 * 
	 * @description 限制必须登录才可进入购物车 去重复商品（相同商品不同规格不去掉）
	 * @param user
	 * @return
	 */
	public List<GoodsCart> cart_calc(User user) {
		List<GoodsCart> carts_list = new ArrayList<GoodsCart>();// 用户整体购物车
		// List<GoodsCart> carts_cookie = new ArrayList<GoodsCart>();//
		// 未提交的用户cookie购物车
		List<GoodsCart> carts_user = new ArrayList<GoodsCart>();// 未提交的用户user购物车
		Map cart_map = new HashMap();
		if (user != null) {
			cart_map.clear();
			cart_map.put("user_id", user.getId());
			cart_map.put("cart_status", 0);
			carts_user = this.goodsCartService.query(
					"select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status order by obj.addTime desc",
					cart_map, -1, -1);

			// 将cookie购物车与用户user购物车合并，去重
			for (GoodsCart ugc : carts_user) {
				carts_list.add(ugc);
			}
		}
		/*
		 * // 组合套装处理，只显示套装主购物车,套装内其他购物车不显示 List<GoodsCart> combin_carts_list =
		 * new ArrayList<GoodsCart>(); for (GoodsCart gc : carts_list) { if
		 * (gc.getCart_type() != null && gc.getCart_type().equals("combin")) {
		 * if (gc.getCombin_main() != 1) {// 组合购物车中非主购物车
		 * combin_carts_list.add(gc); } } } if (combin_carts_list.size() > 0) {
		 * carts_list.removeAll(combin_carts_list); }
		 */
		return carts_list;
	}

}
