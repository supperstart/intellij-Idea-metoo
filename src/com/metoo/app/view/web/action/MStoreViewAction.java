package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.seller.tools.StoreLogTools;

@Controller
@RequestMapping("/app/store/")
public class MStoreViewAction {

	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private MGoodsViewTools mgoodsViewTools;
	@Autowired
	private IPointService pointService;
	/**
	 * 
	 * @param request
	 * @param response
	 * @param id
	 *            店铺id
	 */
	// @RequestMapping(value = "store", method = RequestMethod.GET)
	// public void store(HttpServletRequest request, HttpServletResponse
	// response, String id) {
	//
	// }

	/**
	 * @methodsName index_discount
	 * @description 根据store id 查询商品列表
	 * @param request
	 * @param response
	 * @param orderBy
	 *            排序字段
	 * @param orderType
	 *            排序类型
	 * @param currentPage
	 *            当前页数
	 * @param store_recommend
	 *            是否被推荐
	 * @param store_creativity
	 *            商城精选
	 * @param goods_global
	 *            国际直邮 普通商品为2 国际直邮为1
	 * @param goods_type
	 *            商品类型
	 * @param goods_store
	 *            店铺id
	 * @param token
	 *            用户token
	 * 
	 */
	@EmailMapping(value = "store")
	@RequestMapping(value = "v1/index.json", method = RequestMethod.POST)
	public void index(HttpServletRequest request, HttpServletResponse response, String orderBy, String orderType,
			String currentPage, String store_recommend, String store_creativity, String goods_global, String goods_type,
			@RequestParam(value = "goods_store", required = true) String goods_store, String token, String language) {
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		Result result = null;
		Store store = this.storeService.getObjById(CommUtil.null2Long(goods_store));
		User user = null;
		Map<String, Object> map = new HashMap<String, Object>();
		if (!CommUtil.null2String(token).equals("")) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		if (orderBy == null || orderBy.equals("")) {
			orderBy = "addTime";
		}
		if (orderType == null || orderType.equals("")) {
			orderType = "desc";
		}
		if (store.getStore_status() != 15) {
			result = new Result(4205, "店铺已关闭");
		} else {
			GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv, orderBy, orderType);
			if (store_recommend != null && !store_recommend.equals("")) {
				gqo.addQuery("obj.store_recommend",
						new SysMap("goods_store_recommend", CommUtil.null2Boolean(store_recommend)), "=");
			}
			if (store_creativity != null && !store_creativity.equals("")) {
				gqo.addQuery("obj.store_creativity",
						new SysMap("store_creativity", CommUtil.null2Boolean(store_creativity)), "=");
			}
			if (goods_global != null && !goods_global.equals("")) {
				gqo.addQuery("obj.goods_global", new SysMap("goods_global", CommUtil.null2Int(goods_global)), "=");
			}
			if (goods_type != null && !goods_type.equals("")) {
				gqo.addQuery("obj.goods_type", new SysMap("goods_type", CommUtil.null2Int(goods_type)), "=");
			}
			if (goods_store != null && !goods_store.equals("")) {
				gqo.addQuery("obj.goods_store.id", new SysMap("store_id", CommUtil.null2Long(goods_store)), "=");
			}
			gqo.setPageSize(30);
			gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
			IPageList pList = this.goodsService.list(gqo);
			List<Goods> goods = pList.getResult();
			List<Map<String, Object>> goodsList = new ArrayList<Map<String, Object>>();
			for (Goods obj : goods) {
				Map<String, Object> goodsMap = new HashMap<String, Object>();
				if (CommUtil.isNotNull(obj.getGoods_main_photo())) {
					goodsMap.put("goods_img",
							this.configService.getSysConfig().getImageWebServer() + "/"
									+ obj.getGoods_main_photo().getPath() + "/" + obj.getGoods_main_photo().getName()
									+ "_middle." + obj.getGoods_main_photo().getExt());
				}
				if (CommUtil.isNotNull(obj.getGoods_main_photo())) {
					goodsMap.put("goods_img",
							this.configService.getSysConfig().getImageWebServer() + "/"
									+ obj.getGoods_main_photo().getPath() + "/" + obj.getGoods_main_photo().getName()
									+ "_middle." + obj.getGoods_main_photo().getExt());
				}
				goodsMap.put("goods_id", obj.getId());
				goodsMap.put("goods_type", obj.getGoods_type());
				goodsMap.put("goods_name", obj.getGoods_name());
				if ("1".equals(language)) {
					goodsMap.put("goods_name", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
							? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
				}
				goodsMap.put("goods_discount_rate", obj.getGoods_discount_rate());
				goodsMap.put("goods_price", obj.getGoods_price());
				goodsMap.put("goods_current_price", obj.getGoods_current_price());
				goodsMap.put("well_evaluate", obj.getWell_evaluate() == null ? 0 : obj.getWell_evaluate());
				goodsMap.put("goods_inventory", obj.getGoods_inventory());
				goodsMap.put("goods_status", obj.getGoods_status());
				goodsMap.put("store_status", obj.getGoods_store().getStore_status());
				goodsMap.put("goods_collect", obj.getGoods_collect());
				goodsMap.put("goods_pointNum", obj.getPointNum());
				goodsMap.put("user_pointNum", user == null ? 0 : user.getPointNum());
				if (obj.getGoods_store() != null && !obj.getGoods_store().equals("")) {
					goodsMap.put("store_logo",
							obj.getGoods_store().getStore_logo() != null
									? this.configService.getSysConfig().getImageWebServer() + "/"
											+ obj.getGoods_store().getStore_logo().getPath() + "/"
											+ obj.getGoods_store().getStore_logo().getName()
									: "");
				}
				for (Evaluate eva : obj.getEvaluates()) {
					goodsMap.put("evaluate_status", eva.getEvaluate_status());
				}
				goodsList.add(goodsMap);
			}
			map.put("goods_info", goodsList);
			// 增加店铺点击率
			StoreLog storeLog = this.storeLogTools.getTodayStoreLog(CommUtil.null2Long(goods_store));
			storeLog.setStore_click(storeLog.getStore_click() + 1);
			this.storeLogService.update(storeLog);
			Map<String, Object> params = new HashMap<String, Object>();
			if (user != null) {
				params.put("user_id", user.getId());
				params.put("store_id", CommUtil.null2Long(goods_store));
				List<Favorite> list = this.favoriteService.query(
						"select obj from Favorite obj where obj.user_id=:user_id and obj.store_id=:store_id", params,
						-1, -1);
				map.put("favorite_store", list.size() > 0 ? list.size() : 0);
			}
			// 满减信息
			params.clear();
			params.put("store_id", store.getId().toString());
			params.put("er_type", 1);
			params.put("begin_time", new Date());
			params.put("end_time", new Date());
			List<EnoughReduce> enoughReduces = this.enoughReduceService.query(
					"select obj from EnoughReduce obj where obj.store_id=:store_id and obj.er_type=:er_type and obj.erbegin_time<=:begin_time and obj.erend_time>:end_time order by obj.addTime",
					params, -1, -1);
			List<Map<String, Object>> enoughReduceList = new ArrayList<Map<String, Object>>();
			if (enoughReduces.size() > 0) {
				enoughReduceList = new ArrayList<Map<String, Object>>();
				for (EnoughReduce obj : enoughReduces) {
					Map<String, Object> erMap = new HashMap<String, Object>();
					erMap.put("er_id", obj.getId());
					erMap.put("er_title", obj.getErtitle());
					erMap.put("end_time", obj.getErend_time());
					Map express_map = Json.fromJson(Map.class, obj.getEr_json());
					erMap.put("er_json", express_map);
					enoughReduceList.add(erMap);
				}
			}
			// 兑换商品
			params.clear();
			params.put("type", 0);
			List<Point> points = this.pointService.query("SELECT obj FROM Point obj WHERE obj.type=:type", params, -1,
					-1);
			List pointGoods = null;
			if (points.size() > 0) {
				Point point = points.get(0);
				Set<Long> ids = this.genericIds(point.getGoods_ids_json());
				params.clear();
				params.put("store_id", store.getId());
				params.put("goods_status", 4);
				params.put("goods_ids", ids);
				params.put("point", 1);
				params.put("point_status", 10);
				List<Goods> list = this.goodsService
						.query("SELECT new Goods(id, goods_name, goods_current_price, goods_price, goods_main_photo) FROM Goods obj "
								+ "WHERE obj.goods_store.id=:store_id and obj.goods_status=:goods_status "
								+ "and obj.id in (:goods_ids) and obj.point=:point and obj.point_status=:point_status "
								+ "order by obj.sequence desc", params, -1, -1);
				pointGoods = this.mgoodsViewTools.goods(list, user);
			}
			map.put("pointGoods", pointGoods);
			// 优惠券
			map.put("coupons", this.mgoodsViewTools.storeCoupon(store, user));
			map.put("enoughReduces", enoughReduceList);
			map.put("store_id", store.getId());
			map.put("store_name", store.getStore_name());
			map.put("store_logo",
					store.getStore_logo() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ store.getStore_logo().getPath() + "/" + store.getStore_logo().getName());
			map.put("goods_Pages", pList.getPages());
			int store_enough_free = 0;
			BigDecimal store_enough_price = null;
			Map enoughFree = new HashMap();
			//满包邮
			if(store.getEnough_free() == 1){
				store_enough_free = store.getEnough_free();
				store_enough_price = store.getEnough_free_price();
			}
			enoughFree.put("store_enough_free", store_enough_free);
			enoughFree.put("store_enough_price", store_enough_price);
			map.put("enoughFree", enoughFree);
			result = new Result(0, "Successfully", map);
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description
	 * @param request
	 * @param response
	 * @param er_id
	 *            满减活动id
	 */
	@RequestMapping(value = "v1/enoughReduce.json", method = RequestMethod.GET)
	public void enoughReduce(HttpServletRequest request, HttpServletResponse response, String orderBy, String orderType,
			String er_id, String currentPage, String language) {
		int code = -1;
		String msg = "";
		Map<String, Object> map = new HashMap<String, Object>();
		EnoughReduce obj = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
		if (obj != null) {
			if (obj.getErbegin_time().before(new Date()) && obj.getErend_time().after(new Date())) {
				Map er_json = Json.fromJson(Map.class, obj.getEr_json());
				map.put("er_json", er_json);
				String ids_json = obj.getErgoods_ids_json();
				List<Map<String, Object>> goodsList = null;
				if (null != ids_json && !"".equals(ids_json)) {
					List<String> goods_ids = Json.fromJson(List.class, ids_json);
					goodsList = new ArrayList<Map<String, Object>>();
					for (String s : goods_ids) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(s));
						goodsMap.put("goods_id", goods.getId());
						goodsMap.put("goods_name", goods.getGoods_name());
						if ("1".equals(language)) {
							goodsMap.put("goods_name",
									goods.getKsa_goods_name() != null && !"".equals(goods.getKsa_goods_name())
											? "^" + goods.getKsa_goods_name() : goods.getGoods_name());
						}
						goodsMap.put("goods_img",
								goods.getGoods_main_photo() != null
										? this.configService.getSysConfig().getImageWebServer() + "/"
												+ goods.getGoods_main_photo().getPath() + "/"
												+ goods.getGoods_main_photo().getName()
										: "");
						goodsMap.put("goods_price", goods.getGoods_price());
						goodsMap.put("goods_current_price", goods.getGoods_current_price());
						goodsMap.put("add_cart", true);
						goodsList.add(goodsMap);
					}
				}
				map.put("goodsList", goodsList);
				code = 4200;
				msg = "Successfully";
			} else {
				code = 4204;
				msg = "The activity is over";
			}
		} else {
			code = 4205;
			msg = "parameter error";
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	@RequestMapping(value = "v2/enoughReduce.json", method = RequestMethod.GET)
	public void enoughReducev2(HttpServletRequest request, HttpServletResponse response, String orderBy,
			String orderType, String er_id, String currentPage) {
		int code = -1;
		String msg = "";
		if (orderBy == null || orderBy.equals("")) {
			orderBy = "addTime";
		}
		if (orderType == null || orderType.equals("")) {
			orderType = "desc";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		EnoughReduce obj = this.enoughReduceService.getObjById(CommUtil.null2Long(er_id));
		if (obj != null) {
			if (obj.getErbegin_time().before(new Date()) && obj.getErend_time().after(new Date())) {
				Map er_json = Json.fromJson(Map.class, obj.getEr_json());
				map.put("er_json", er_json);
				String ids_json = obj.getErgoods_ids_json();
				List<Goods> goodsList = null;
				if (null != ids_json && !"".equals(ids_json)) {
					List goods_ids = Json.fromJson(List.class, ids_json);
					Set<Long> set = new HashSet<Long>();
					for (Object id : goods_ids) {
						set.add(CommUtil.null2Long(id));
					}
					Map params = new HashMap();
					params.put("goods_ids", set);
					goodsList = this.goodsService.query(
							"select new Goods(id, goods_name, goods_current_price, goods_price, goods_main_photo) from Goods obj where obj.id in (:goods_ids) order by obj."
									+ orderBy + " " + orderType,
							params, new Double(CommUtil.subtract(currentPage, 1)).intValue(), 20);
					this.mgoodsViewTools.goods(goodsList, map);
				}
				code = 4200;
				msg = "Successfully";
			} else {
				code = 4204;
				msg = "The activity is over";
			}
		} else {
			code = 4205;
			msg = "parameter error";
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	/*
	 * @RequestMapping("v1/point/goods.json") public void
	 * pointGoods(HttpServletRequest request, HttpServletResponse response,
	 * 
	 * @RequestParam(value="store_id", required=true) String store_id) { int
	 * code = -1; String msg = ""; Store store =
	 * this.storeService.getObjById(CommUtil.null2Long(store_id)); List<Goods>
	 * list = null; if (null != store && store.getStore_status() == 15) { Map
	 * params = new HashMap(); params.put("type", 0); List<Point> points =
	 * this.pointService.query("SELECT obj FROM Point obj WHERE obj.type=:type",
	 * params, -1, -1); if (points.size() > 0) { Point point = points.get(0);
	 * Set<Long> ids = this.genericIds(point.getGoods_ids_json());
	 * params.clear(); params.put("store_id", store.getId());
	 * params.put("goods_status", 4); params.put("goods_ids", ids);
	 * params.put("point", 1); params.put("point_status", 10); List<Goods>
	 * goodsList = this.goodsService.query(
	 * "SELECT new Goods(id, goods_name, goods_current_price, goods_price, goods_main_photo) FROM Goods obj WHERE obj.goods_store.id=:store_id and obj.goods_status=:goods_status and obj.id in (:goods_ids) and obj.point=:point and obj.point_status=:point_status"
	 * , params, -1, -1); list = this.mgoodsViewTools.goods(goodsList); } code =
	 * 4200; msg = "Successfully"; } else { code = 4205; msg =
	 * "The shop has been closed"; } this.send_json(Json.toJson(new Result(code,
	 * msg, list), JsonFormat.compact()), response); }
	 */
	private Set<Long> genericIds(String str) {
		Set<Long> ids = new HashSet<Long>();
		List list = (List) Json.fromJson(str);
		for (Object object : list) {
			ids.add(CommUtil.null2Long(object));
		}
		return ids;
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
