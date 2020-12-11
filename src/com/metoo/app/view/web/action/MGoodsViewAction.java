package com.metoo.app.view.web.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.thread.GoodsThread;
import com.metoo.app.view.web.thread.GoodsThreadPool;
import com.metoo.app.view.web.tool.GoodsBatcUpdateUtil;
import com.metoo.app.view.web.tool.MCartViewTools;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.constant.Globals;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.CombinPlan;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.FootPoint;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsSpecification;
import com.metoo.foundation.domain.GoodsTypeProperty;
import com.metoo.foundation.domain.Group;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.Transport;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserRecommend;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.domain.query.PointQueryObject;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IBuyGiftService;
import com.metoo.foundation.service.ICGoodsService;
import com.metoo.foundation.service.ICombinPlanService;
import com.metoo.foundation.service.IConsultSatisService;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IFootPointService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPointService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreNavigationService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITransportService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserGoodsClassService;
import com.metoo.foundation.service.IUserRecommendService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.action.PayoffLogManageAction;
import com.metoo.manage.admin.tools.ImageTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.ActivityViewTools;
import com.metoo.view.web.tools.AreaViewTools;
import com.metoo.view.web.tools.ConsultViewTools;
import com.metoo.view.web.tools.EvaluateViewTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.IntegralViewTools;
import com.metoo.view.web.tools.StoreViewTools;

/**
 * 
 * <p>
 * point/goods/query.json Title: GoodsViewAction.java
 * </p>
 * 
 * <p>
 * Description: 商品前台控制器,用来显示商品列表、商品详情、商品其他信息
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2014
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.metoo.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-28
 * 
 * @version metoo_b2b2c v2.0 2015版
 */
@Controller
@RequestMapping("/app/v1")
public class MGoodsViewAction {
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IConsultService consultService;
	@Autowired
	private MGoodsViewTools mGoodsViewTools;
	@Autowired
	private ICGoodsService cGoodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private AreaViewTools areaViewTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private StoreViewTools storeViewTools;
	@Autowired
	private UserTools userTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private ConsultViewTools consultViewTools;
	@Autowired
	private EvaluateViewTools evaluateViewTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreNavigationService storenavigationService;
	@Autowired
	private IConsultSatisService consultsatisService;
	@Autowired
	private IntegralViewTools integralViewTools;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IFootPointService footPointService;
	@Autowired
	private IActivityGoodsService actgoodsService;
	@Autowired
	private ActivityViewTools activityViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private ICombinPlanService combinplanService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IBuyGiftService buyGiftService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ImageTools imageTools;
	@Autowired
	private IUserGoodsClassService userGoodsClassService;
	@Autowired
	private ITransportService transportService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private MCartViewTools metooCartViewTools;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IStoreLogService storeLogService;
	@Autowired
	private ICouponService couponService;
	@Autowired
	private ICouponInfoService couponInfoService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private IUserRecommendService userRecommendService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private GoodsBatcUpdateUtil goodsBatcUpdateUtil;

	private static org.slf4j.Logger log = LoggerFactory.getLogger(MGoodsViewAction.class);
	private final static Log logger = LogFactory.getLog(MGoodsViewAction.class);

	/**
	 * @description 商品详情信息
	 * 
	 * @param request
	 * @param response
	 * @param id
	 */
	@RequestMapping("goods.json")
	@ResponseBody
	public void goodsdetail(HttpServletRequest request, HttpServletResponse response, String id, String token,
			String language) {
		Result result = null;
		User user = null;
		if (!token.equals("")) {
			user = this.userService.getObjByProperty(null, "app_login_token", token);
		}
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 0,
				request, response);
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		Map<String, Object> goodsmap = new HashMap<String, Object>();
		Map<String, Object> objmap = new HashMap<String, Object>();
		if (obj != null) {
			if (obj.getGoods_status() == 0 || obj.getGoods_status() == 4 && obj.getPoint_status() == 10) { // 兑换商品审核完成
				if (obj.getGoods_store().getStore_status() == 15) {
					if (this.configService.getSysConfig().isSecond_domain_open()) {
						String serverName = request.getServerName().toLowerCase();
						String secondDomain = CommUtil.null2String(serverName.substring(0, serverName.indexOf(".")));
						if (serverName.indexOf(".") == serverName.lastIndexOf(".")) {
							secondDomain = "www";
						}
						// System.out.println("已经开启二级域名，二级域名为：" + secondDomain);
						if (!secondDomain.equals("")) {
							// [0为自营商品，1为第三方经销商]
							if (obj.getGoods_type() == 0) { // [自营商品禁止使用二级域名访问]
								if (!secondDomain.equals("www")) {
									result = new Result(1, "自营商品禁止使用二级域名访问");
								}
							} else {
								if (!obj.getGoods_store().getStore_second_domain().equals(secondDomain)) {
									result = new Result(1, "商品对应店铺二级与当前二级域名不符");
								}
							}

						} else {
							result = new Result(1, "域名为空");
						}
					}

					// 2[利用cookie添加查看过的商品]
					String tourists = "";
					Cookie[] cookies = request.getCookies();
					if(cookies != null){
						for (Cookie cookie : cookies) {
							if (cookie.getName().equals("tourists")) {
								tourists = CommUtil.null2String(cookie.getValue());
							}
						}
					}
					Cookie goodscookie = null;
					int k = 0;
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							if (cookie.getName().equals("goodscookie")) {
								String goods_ids = cookie.getValue();
								int m = 6;
								int n = goods_ids.split(",").length;
								if (m > n) {
									m = n + 1;
								}
								String[] new_goods_ids = goods_ids.split(",", m);
								for (int i = 0; i < new_goods_ids.length; i++) {
									if ("".equals(new_goods_ids[i])) {
										for (int j = i + 1; j < new_goods_ids.length; j++) {
											new_goods_ids[i] = new_goods_ids[j];
										}
									}
								}
								String[] new_ids = new String[6];
								for (int i = 0; i < m - 1; i++) {
									if (id.equals(new_goods_ids[i])) {
										k++;
									}
								}
								if (k == 0) {
									new_ids[0] = id;
									for (int j = 1; j < m; j++) {
										new_ids[j] = new_goods_ids[j - 1];
									}
									goods_ids = id + ",";
									if (m == 2) {
										for (int i = 1; i <= m - 1; i++) {
											goods_ids = goods_ids + new_ids[i] + ",";
										}
									} else {
										for (int i = 1; i < m; i++) {
											goods_ids = goods_ids + new_ids[i] + ",";
										}
									}
									goodscookie = new Cookie("goodscookie", goods_ids);
								} else {
									new_ids = new_goods_ids;
									goods_ids = "";
									for (int i = 0; i < m - 1; i++) {
										goods_ids += new_ids[i] + ",";
									}
									goodscookie = new Cookie("goodscookie", goods_ids);
								}
								goodscookie.setMaxAge(60 * 60 * 24 * 30);
								//goodscookie.setDomain(CommUtil.generic_domain(request));
								response.addCookie(goodscookie);
								break;
							} else {
								goodscookie = new Cookie("goodscookie", id + ",");
								goodscookie.setMaxAge(60 * 60 * 24 * 30);
								//goodscookie.setDomain(CommUtil.generic_domain(request));
								response.addCookie(goodscookie);
							}
						}
					} else {
						goodscookie = new Cookie("goodscookie", id + ",");
						goodscookie.setMaxAge(60 * 60 * 24 * 30);
						//goodscookie.setDomain(CommUtil.generic_domain(request));
						response.addCookie(goodscookie);
					}

					// 3[记录登陆用户浏览足迹]
					User current_user = user;
					boolean admin_view = false;// 超级管理员可以查看未审核得到商品信息
					if (current_user != null) {
						// 登录用户记录浏览足迹信息
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("fp_date", CommUtil.formatDate(CommUtil.formatShortDate(new Date())));
						params.put("fp_user_id", current_user.getId());
						List<FootPoint> fps = this.footPointService.query(
								"select obj from FootPoint obj where obj.fp_date=:fp_date and obj.fp_user_id=:fp_user_id",
								params, -1, -1);
						if (fps.size() == 0) {
							FootPoint fp = new FootPoint();
							fp.setAddTime(new Date());
							fp.setFp_date(new Date());
							fp.setFp_user_id(current_user.getId());
							fp.setFp_user_name(current_user.getUsername());
							fp.setFp_goods_count(1);
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("goods_id", obj.getId());
							map.put("goods_name", obj.getGoods_name());
							map.put("goods_sale", obj.getGoods_salenum());
							map.put("goods_time", CommUtil.formatLongDate(new Date()));
							map.put("goods_img_path", obj.getGoods_main_photo() != null
									? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
											+ obj.getGoods_main_photo().getName() + "_middle."
											+ obj.getGoods_main_photo().getExt()
									: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath()
											+ "/" + this.configService.getSysConfig().getGoodsImage().getName()
											+ "_middle." + this.configService.getSysConfig().getGoodsImage().getExt());
							map.put("goods_price", obj.getGoods_price());
							map.put("goods_current_price", obj.getGoods_current_price());
							map.put("goods_discount_rate", obj.getGoods_discount_rate());
							map.put("goods_status", obj.getGoods_status());
							map.put("store_status", obj.getGoods_store().getStore_status());
							map.put("goods_class_id", CommUtil.null2Long(obj.getGc().getId()));
							map.put("goods_class_name", CommUtil.null2String(obj.getGc().getClassName()));
							map.put("ksa_goods_name", obj.getKsa_goods_name());
							List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
							list.add(map);
							fp.setFp_goods_content(Json.toJson(list, JsonFormat.compact()));
							this.footPointService.save(fp);
						} else {
							FootPoint fp = fps.get(0);
							List<Map> list = Json.fromJson(List.class, fp.getFp_goods_content());
							boolean add = true;
							for (Map map : list) {// 排除重复的商品足迹
								if (CommUtil.null2Long(map.get("goods_id")).equals(obj.getId())) {
									add = false;
								}
							}
							if (add) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("goods_id", obj.getId());
								map.put("goods_name", obj.getGoods_name());
								if ("1".equals(language)) {
									map.put("goods_name",
											obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
													? obj.getKsa_goods_name() : obj.getGoods_name());
								}
								map.put("goods_sale", obj.getGoods_salenum());
								map.put("goods_time", CommUtil.formatLongDate(new Date()));
								map.put("goods_img_path", obj.getGoods_main_photo() != null
										? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
												+ obj.getGoods_main_photo().getName() + "_middle."
												+ obj.getGoods_main_photo().getExt()
										: imageWebServer + "/"
												+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
												+ this.configService.getSysConfig().getGoodsImage().getName()
												+ "_middle."
												+ this.configService.getSysConfig().getGoodsImage().getExt());
								map.put("goods_price", obj.getGoods_price());
								map.put("goods_current_price", obj.getGoods_current_price());
								map.put("goods_discount_rate", obj.getGoods_discount_rate());
								map.put("goods_status", obj.getGoods_status());
								map.put("status_status", obj.getGoods_store().getStore_status());
								map.put("goods_class_id", CommUtil.null2Long(obj.getGc().getId()));
								map.put("goods_class_name", CommUtil.null2String(obj.getGc().getClassName()));
								list.add(0, map);// 后浏览的总是插入最前面
								fp.setFp_goods_count(list.size());
								fp.setFp_goods_content(Json.toJson(list, JsonFormat.compact()));
								this.footPointService.update(fp);
							}
						}

						if (current_user.getUserRole().equals("ADMIN")) {
							admin_view = true;
						}
					}

					// 记录商品日志
					GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(obj.getId());
					todayGoodsLog.setGoods_click(todayGoodsLog.getGoods_click() + 1);
					todayGoodsLog.setUser_id(user == null ? 0 : user.getId());
					String click_from_str = todayGoodsLog.getGoods_click_from();
					Map<String, Integer> clickmap = (click_from_str != null && !click_from_str.equals(""))
							? (Map<String, Integer>) Json.fromJson(click_from_str) : new HashMap<String, Integer>();
					String from = clickfrom_to_chinese(CommUtil.null2String(request.getParameter("from")));
					if (from != null && !from.equals("")) {
						if (clickmap.containsKey(from)) {
							clickmap.put(from, clickmap.get(from) + 1);
						} else {
							clickmap.put(from, 1);
						}
					} else {
						if (clickmap.containsKey("unknow")) {
							clickmap.put("unknow", clickmap.get("unknow") + 1);
						} else {
							clickmap.put("unknow", 1);
						}
					}
					todayGoodsLog.setGoods_click_from(Json.toJson(clickmap, JsonFormat.compact()));
					this.goodsLogService.update(todayGoodsLog);
					// 记录用户浏览商品记录（推荐）
					if (user != null) {
						UserRecommend userRecommend = this.goodsViewTools.getTodayUserRcommend(obj.getId(),
								user.getId());
						userRecommend.setGoods_click(userRecommend.getGoods_click() + 1);
						this.userRecommendService.update(userRecommend);
					}
					if (obj.getGoods_status() == 0 || admin_view || obj.getGoods_status() == 4) {

						obj.setGoods_click(obj.getGoods_click() + 1);
						if (this.configService.getSysConfig().isZtc_status() && obj.getZtc_status() == 2) {
							obj.setZtc_click_num(obj.getZtc_click_num() + 1);
						}

						if (obj.getActivity_status() == 1 || obj.getActivity_status() == 2) {
							if (!CommUtil.null2Boolean(obj.getActivity_goods_id().equals(""))) {
								ActivityGoods ag = this.actgoodsService.getObjById(obj.getActivity_goods_id());
								if (ag.getAct().getAc_end_time().before(new Date())) {
									ag.setAg_status(-2);
									this.actgoodsService.update(ag);
									obj.setActivity_status(0);
									obj.setActivity_goods_id(null);
								}
							}
						}

						if (obj.getGroup() != null && obj.getGroup_buy() == 2) {
							Group group = obj.getGroup();
							if (group.getEndTime().before(new Date())) {
								obj.setGroup(null);
								obj.setGroup_buy(0);
								obj.setGoods_current_price(obj.getStore_price());
							}
						}

						if (obj.getCombin_status() == 1) {
							Map params = new HashMap();
							params.put("endTime", new Date());
							params.put("main_goods_id", obj.getId());
							List<CombinPlan> combins = this.combinplanService.query(
									"select obj from CombinPlan obj where obj.endTime<=:endTime and obj.main_goods_id=:main_goods_id",
									params, -1, -1);
							if (combins.size() > 0) {
								for (CombinPlan com : combins) {
									if (com.getCombin_type() == 0) {
										if (obj.getCombin_suit_id().equals(com.getId())) {
											obj.setCombin_suit_id(null);
										}
									} else {
										if (obj.getCombin_parts_id().equals(com.getId())) {
											obj.setCombin_parts_id(null);
										}
									}
									obj.setCombin_status(0);
								}
							}
						}
						if (obj.getOrder_enough_give_status() == 1) {
							BuyGift bg = this.buyGiftService.getObjById(obj.getBuyGift_id());
							if (bg != null && bg.getEndTime().before(new Date())) {
								bg.setGift_status(20);
								List<Map> maps = Json.fromJson(List.class, bg.getGift_info());
								maps.addAll(Json.fromJson(List.class, bg.getGoods_info()));
								for (Map map : maps) {
									Goods goods = this.goodsService.getObjById(CommUtil.null2Long(map.get("goods_id")));
									if (goods != null) {
										goods.setOrder_enough_give_status(0);
										goods.setOrder_enough_if_give(0);
										goods.setBuyGift_id(null);
									}
								}
								this.buyGiftService.update(bg);
							}
							if (bg != null && bg.getGift_status() == 10) {
								objmap.put("isGift", true);
							}
						}
						if (obj.getOrder_enough_if_give() == 1) {
							BuyGift bg = this.buyGiftService.getObjById(obj.getBuyGift_id());
							if (bg != null && bg.getGift_status() == 10) {
								objmap.put("isGift", true);
							}
						}
						// 满就减
						Map enoughReduceMap = new HashMap();
						double reducePrice = 0.00;
						double total_price = 0.0; // 商品总价
						total_price = CommUtil.null2Double(obj.getGoods_current_price());
						if (obj.getEnough_reduce() == 1) {
							EnoughReduce er = this.enoughReduceService
									.getObjById(CommUtil.null2Long(obj.getOrder_enough_reduce_id()));
							if (er.getErstatus() == 10 && er.getErbegin_time().before(new Date())
									&& er.getErend_time().after(new Date())) {// 正在进行
								// ermap.put("ertag", er.getErtag());
								// JSONObject解析数据后保持数据顺序不变
								/*
								 * LinkedHashMap er_json =
								 * JSONObject.parseObject(er.getEr_json(),
								 * LinkedHashMap.class, Feature.OrderedField);
								 */
								Map er_json = Json.fromJson(Map.class, er.getEr_json());
								if (!er_json.isEmpty()) {
									double price = 0.0;
									Iterator iterator = er_json.keySet().iterator();
									while (iterator.hasNext()) {
										Object key = iterator.next();
										Object value = er_json.get(key);
										if (CommUtil.subtract(total_price, key) < 0) {
											enoughReduceMap.put("key", key);
											enoughReduceMap.put("value", value);
											reducePrice = CommUtil.subtract(key, total_price);
											break;
										}
										if (CommUtil.subtract(total_price, key) >= 0) {
											price = CommUtil.null2Double(value);
											enoughReduceMap.put("key", key);
											enoughReduceMap.put("value", value);
										}
										total_price = CommUtil.subtract(total_price, price);
									}
									enoughReduceMap.put("reducePrice", reducePrice);
									enoughReduceMap.put("reduceAmount", price);
								}
								enoughReduceMap.put("er_id", er.getId());
								enoughReduceMap.put("total_price", total_price);
								enoughReduceMap.put("er_json", er_json);
							} else if (er.getErend_time().before(new Date())) {// 已过期
								er.setErstatus(20);
								this.enoughReduceService.update(er);
								String goods_json = er.getErgoods_ids_json();
								List<String> goods_id_list = (List) Json.fromJson(goods_json);
								for (String goods_id : goods_id_list) {
									Goods ergood = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
									ergood.setEnough_reduce(0);
									ergood.setOrder_enough_reduce_id("");
									this.goodsService.update(ergood);
								}
							}
						}
						goodsmap.put("enoughReduceMap", enoughReduceMap);
						// 兑换活动
						int goods_pointNum = 0;
						int goods_percentage = 0;
						int goods_point = 0;
						if (obj.getPoint() == 1 && obj.getPoint_status() == 10) {
							Point point = this.pointService.getObjById(CommUtil.null2Long(obj.getPoint_id()));
							goods_pointNum = obj.getPointNum();
							goods_percentage = (int) (user == null ? 0
									: CommUtil.div(user.getPointNum(), obj.getPointNum()));
							goods_point = obj.getPoint() == 1 && obj.getPoint_status() == 10 ? 1 : 0;
						}
						objmap.put("goods_pointNum", goods_pointNum);
						objmap.put("goods_percentage", goods_percentage);
						objmap.put("goods_point", goods_point);
						this.goodsService.update(obj);
						System.out.println(
								obj.getGoods_main_photo().getPath() + "/" + obj.getGoods_main_photo().getName());
						List<Accessory> accessorys = obj.getGoods_photos();
						List<Accessory> photos = new ArrayList<Accessory>();
						photos.addAll(accessorys);
						List<Map<String, Object>> accessoryList = new ArrayList<Map<String, Object>>();
						photos.add(0, obj.getGoods_main_photo());
						for (Accessory accessory : photos) {
							Map<String, Object> accmap = new HashMap<String, Object>();
							accmap.put("photos", configService.getSysConfig().getImageWebServer() + "/"
									+ accessory.getPath() + "/" + accessory.getName());
							accessoryList.add(accmap);
						}
						objmap.put("photos", accessoryList);
						objmap.put("goods_id", obj.getId());
						objmap.put("goods_name", obj.getGoods_name());
						objmap.put("goods_img", obj.getGoods_main_photo() != null
								? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
										+ obj.getGoods_main_photo().getName()
								: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath()
										+ "/" + this.configService.getSysConfig().getGoodsImage().getName());
						objmap.put("goods_detail", obj.getGoods_details() == null && "".equals(obj.getGoods_details())
								? "" : obj.getGoods_details());
						objmap.put("features_one", obj.getFeatures_one());
						objmap.put("features_two", obj.getFeatures_two());
						objmap.put("features_three", obj.getFeatures_three());
						objmap.put("features_four", obj.getFeatures_four());
						objmap.put("features_five", obj.getFeatures_five());
						if ("1".equals(language)) {
							objmap.put("goods_name",
									obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
											? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
							objmap.put("goods_detail",
									obj.getKsa_goods_detail() != null && !"".equals(obj.getKsa_goods_detail())
											? "^" + obj.getKsa_goods_detail() : obj.getGoods_details());

							if (null != obj.getKsa_features() && !"".equals(obj.getKsa_features())) {
								Map features_map = Json.fromJson(Map.class, obj.getKsa_features());
								objmap.put("features_one",
										features_map.get("1") != null && !"".equals(features_map.get("1"))
												? "^" + features_map.get("1") : obj.getFeatures_one());
								objmap.put("features_two",
										features_map.get("2") != null && !"".equals(features_map.get("2"))
												? "^" + features_map.get("2") : obj.getFeatures_two());
								objmap.put("features_three",
										features_map.get("3") != null && !"".equals(features_map.get("3"))
												? "^" + features_map.get("3") : obj.getFeatures_three());
								objmap.put("features_four",
										features_map.get("4") != null && !"".equals(features_map.get("4"))
												? "^" + features_map.get("4") : obj.getFeatures_four());
								objmap.put("features_five",
										features_map.get("5") != null && !"".equals(features_map.get("5"))
												? "^" + features_map.get("5") : obj.getFeatures_five());
							}
						}
						Map countWords = this.mGoodsViewTools
								.pictureCharacterSplit(objmap.get("goods_detail").toString());
						objmap.put("goods_character_detail", countWords.get("character"));
						objmap.put("goods_photo_detail", countWords.get("picture"));
						objmap.put("goods_current_price", obj.getGoods_current_price());
						objmap.put("goods_tiered_price", JSONObject.parseObject(obj.getGoods_tiered_price()));
						objmap.put("goods_inventory", obj.getGoods_inventory());
						objmap.put("goods_evaluate_count", obj.getEvaluate_count());
						objmap.put("goods_price", obj.getGoods_price());
						objmap.put("well_evaluate", obj.getWell_evaluate());
						objmap.put("good_middle_evaluate", obj.getMiddle_evaluate());
						objmap.put("goods_bad_evaluate", obj.getBad_evaluate());
						int all_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(obj.getId()), "all");
						int well_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(obj.getId()), "well");
						int middle_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(obj.getId()), "middle");
						int bad_evaluate = evaluateViewTools.appQueryByEva(CommUtil.null2String(obj.getId()), "bad");
						objmap.put("eva_all_count", all_eval);
						objmap.put("eva_well_count", well_eval);
						objmap.put("eva_middle_count", middle_eval);
						objmap.put("eva_bad_count", bad_evaluate);
						objmap.put("goods_salenum", obj.getGoods_salenum());
						objmap.put("goods_cod", obj.getGoods_cod());
						objmap.put("goods_gc_id", obj.getGc().getParent().getParent().getId());
						objmap.put("act_status", obj.getActivity_status());
						objmap.put("group_buy", obj.getGroup_buy());
						objmap.put("give_status", obj.getOrder_enough_give_status());
						objmap.put("BuyGift_id", obj.getBuyGift_id());
						objmap.put("order_enough_give_status", obj.getOrder_enough_give_status());
						objmap.put("buyGift_amount", obj.getBuyGift_amount());
						objmap.put("order_enough_if_give", obj.getOrder_enough_if_give());
						objmap.put("goods_collect", obj.getGoods_collect());
						objmap.put("goods_status", obj.getGoods_status());
						objmap.put("price_history", obj.getPrice_history());
						if (obj.getGoods_global() == 1) {
							objmap.put("goods_arrival_time", "It is expected to be delivered in 10-15 working days");
						} else if (obj.getGoods_global() == 2) {
							objmap.put("goods_arrival_time", "It is expected to be delivered in 1-3 working days");
						}
						objmap.put("user_pointNum", user == null ? 0 : user.getPointNum());
						objmap.put("goods_discount_rate", obj.getGoods_discount_rate());
						objmap.put("advance_sale_type", obj.getAdvance_sale_type());
						if (user != null) {
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("user_id", user.getId());
							params.put("goods_id", CommUtil.null2Long(id));
							List<Favorite> list1 = this.favoriteService.query(
									"select obj from Favorite obj where obj.user_id=:user_id and obj.goods_id=:goods_id order by obj.goods_id ",
									params, -1, -1);
							if (list1.size() > 0) {
								objmap.put("goods_favorite_goods", list1.size());
							} else {
								objmap.put("goods_favorite_goods", "0");
							}
						}
						if (obj.getGoods_brand() == null) {
							objmap.put("goods_brand_id", "");
							objmap.put("goods_brand_name", "");
						} else {
							objmap.put("goods_brand_id", obj.getGoods_brand().getId());
							objmap.put("goods_brand_name", obj.getGoods_brand().getName());
						}
						if (obj.getGoods_property() != null) {
							List<Map> goods_property = this.orderFormTools.queryGoodsInfo(obj.getGoods_property());
							objmap.put("goods_property", goods_property);
						} else {
							objmap.put("goods_property", "");
						}
						if (obj.getGoods_store() != null) {
							objmap.put("store_logo",
									obj.getGoods_store().getStore_logo() == null ? ""
											: this.configService.getSysConfig().getImageWebServer() + "/"
													+ obj.getGoods_store().getStore_logo().getPath() + "/"
													+ obj.getGoods_store().getStore_logo().getName());
							objmap.put("store_status", obj.getGoods_store().getStore_status());
						}
						goodsmap.put("goods", objmap);
						// [计算档期访问用户IP地址，并计算对应的运费信息]
						String current_city = "";
						String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
						if (CommUtil.isIp(current_ip)) {
							IPSeeker ip = new IPSeeker(null, null);
							current_city = ip.getIPLocation(current_ip).getCountry();
							goodsmap.put("current_city", current_city);
						} else {
							goodsmap.put("current_city", "The unknown region");
						}
						// 查询运费地区
						List<Area> areas = this.areaService.query(
								"select obj from Area obj where obj.parent.id is null order by obj.sequence asc", null,
								-1, -1);
						List<Map<String, Object>> areaList = new ArrayList<Map<String, Object>>();
						for (Area area : areas) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("areaid", area.getId());
							map.put("areaname", area.getAreaName());
							areaList.add(map);
						}
						goodsmap.put("area_info", areaList);
						// 优惠券
						//goodsmap.put("coupon", this.mGoodsViewTools.queryCoupon(obj.getGoods_store(), user));
						goodsmap.put("coupon", this.mGoodsViewTools.queryCoupon(obj.getGoods_store(), user, tourists));
						Map<String, Object> storeMap = new HashMap<String, Object>();
						// [查询商家评分信息 generic_evaluate ]
						if (obj.getGoods_type() == 0) {// 平台自营商品
							storeMap.put("store_name", "self");
						} else {// 商家商品
							// this.generic_evaluate(obj.getGoods_store(),
							// mv);加载店铺评分信息
							storeMap.put("store_id", obj.getGoods_store().getId());
							storeMap.put("store_name", obj.getGoods_store().getStore_name());
							storeMap.put("store_enough_free", obj.getGoods_store().getEnough_free());
							storeMap.put("store_enough_price", obj.getGoods_store().getEnough_free_price());
						}
						goodsmap.put("storemap", storeMap);
						// 商品规格
						Map<String, Object> specMap = this.mGoodsViewTools
								.goodsGenericSpec(CommUtil.null2String(obj.getId()));
						specMap.put("color", this.mGoodsViewTools.goodsColor(CommUtil.null2String(obj.getId())));
						goodsmap.put("spec1", specMap);
						goodsmap.put("spec", this.mGoodsViewTools.spec(obj));

						// 根据区域查询运费
						double postagePrice = 0;
						Map<String, Object> goods_transfee_map = new HashMap<String, Object>();
						if (obj.getGoods_transfee() == 1) {
							goods_transfee_map.put("goods_transfee", "0");
						} else {
							float freight_price = 0;
							if (obj.getInventory_type().equals("all")) {
								if (obj.getGoods_store().getTransport() != null
										&& obj.getGoods_store().getTransport().isTrans_express()) {
									freight_price = transportTools.goods_trans_fee(
											CommUtil.null2String(obj.getGoods_store().getTransport().getId()),
											"express", obj.getId(), current_city);
								}
								goods_transfee_map.put("goods_transfee", freight_price);
								if (obj.getGoods_store().getEnough_free() == 1) {
									BigDecimal enoughFreePrice = obj.getGoods_store().getEnough_free_price();
									postagePrice = CommUtil.subtract(enoughFreePrice, obj.getGoods_current_price());
								}
							} else {
								goods_transfee_map.put("goods_transfee", freight_price);
							}
						}
						goods_transfee_map.put("postagePrice", postagePrice > 0 ? postagePrice : 0);
						goodsmap.put("goods_transfee_map", goods_transfee_map);
						// 评论
						EvaluateQueryObject qo = new EvaluateQueryObject("1", mv, "addTime", "desc");
						qo.addQuery("obj.evaluate_goods.id", new SysMap("goods_id", CommUtil.null2Long(id)), "=");
						List<String> evaluate_type = new ArrayList<String>();
						evaluate_type.add("goods");
						evaluate_type.add("Brush");
						qo.addQuery("obj.evaluate_type", new SysMap("evaluate_type", evaluate_type), "in");
						qo.addQuery("obj.evaluate_status", new SysMap("evaluate_status", 0), "=");
						qo.setPageSize(10);
						IPageList eva_pList = this.evaluateService.list(qo);
						objmap.put("currentpage", eva_pList.getCurrentPage());
						objmap.put("pages", eva_pList.getPages());
						List<Evaluate> evas = eva_pList.getResult();
						List<Map<String, Object>> eva_list = new ArrayList<Map<String, Object>>();
						for (Evaluate evaluate : evas) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("evaid", evaluate.getId());
							if (evaluate.getEvaluate_user() == null) {
								map.put("eva_userName",
										evaluate.getUser_name() == null || evaluate.getUser_name().equals("") ? ""
												: evaluate.getUser_name().charAt(0) + "***" + evaluate.getUser_name()
														.charAt(evaluate.getUser_name().length() - 1));
								map.put("user_photo",
										this.configService.getSysConfig().getImageWebServer() + "/" + "resources" + "/"
												+ "style" + "/" + "common" + "/" + "images" + "/" + "member.png");
							} else {
								User eval_user = evaluate.getEvaluate_user();
								map.put("eva_userName", eval_user.getUserName().charAt(0) + "***"
										+ eval_user.getUserName().charAt(eval_user.getUserName().length() - 1));
								if (eval_user.getSex() == -1) {
									map.put("user_photo",
											this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
													+ "/" + "style" + "/" + "common" + "/" + "images" + "/"
													+ "member.png");
								}
								if (eval_user.getSex() == 0) {
									map.put("user_photo",
											this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
													+ "/" + "style" + "/" + "common" + "/" + "images" + "/"
													+ "member0.png");
								}
								if (eval_user.getSex() == 1) {
									map.put("user_photo",
											this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
													+ "/" + "style" + "/" + "common" + "/" + "images" + "/"
													+ "member1.png");
								}
							}

							map.put("eva_addTime", evaluate.getAddTime());
							map.put("eva_description", evaluate.getDescription_evaluate());
							map.put("eva_service", evaluate.getService_evaluate());
							map.put("eva_ship", evaluate.getShip_evaluate());
							map.put("eva_add_info", evaluate.getAddeva_info());
							map.put("eva_reply_status", evaluate.getReply_status());
							map.put("eva_info", evaluate.getEvaluate_info() == null ? "" : evaluate.getEvaluate_info());
							map.put("eva_reply", evaluate.getReply());
							map.put("eva_add_status", evaluate.getAddeva_status());
							map.put("eva_buyer_val", evaluate.getEvaluate_buyer_val()); // 买家评价，评价类型，1为好评，0为中评，-1为差评[1,2,3,4,5,
																						// 1-5星]
							Goods goods = evaluate.getEvaluate_goods();
							map.put("eva_well", goods.getWell_evaluate());// 商品好评率,例如：该值为0.96，好评率即为96%
							List<Accessory> eval_accessorys = this.evaluateViewTools
									.queryEvaImgSrc(evaluate.getEvaluate_photos());
							List<Map> eva_map_list = new ArrayList<Map>();
							if (!eval_accessorys.isEmpty()) {
								for (Accessory accessory : eval_accessorys) {
									Map photoMap = new HashMap();
									photoMap.put("Evaluate_photos",
											this.configService.getSysConfig().getImageWebServer() + "/"
													+ accessory.getPath() + "/" + accessory.getName());
									eva_map_list.add(photoMap);
								}
								map.put("eva_photo", eva_map_list);
							} else {
								map.put("eva_photo", "");
							}
							List<Accessory> addAcc = imageTools.queryImgs(evaluate.getAddeva_photos());
							List<Map<String, Object>> add_eva_map_list = new ArrayList<Map<String, Object>>();
							if (CommUtil.isNotNull(add_eva_map_list)) {
								for (Accessory accessory : addAcc) {
									Map<String, Object> Addeva_photoMap = new HashMap<String, Object>();
									Addeva_photoMap.put("Evaluate_photos",
											this.configService.getSysConfig().getImageWebServer() + "/"
													+ accessory.getPath() + "/" + accessory.getName());
									add_eva_map_list.add(Addeva_photoMap);
								}
								map.put("eva_add_photo", add_eva_map_list);
							}
							eva_list.add(map);
						}
						goodsmap.put("evalist", eva_list);
						objmap.put("eval_count", eva_list.size());
					} else {
						result = new Result(1, "Goods not on shelves");
					}
					result = new Result(0, "Successfully", goodsmap);
				} else {
					result = new Result(-4, "The shop has been closed", goodsmap);
				}
			} else {
				result = new Result(-5, "Commodity audit", goodsmap);
			}
		} else {
			result = new Result(-1, "No item found");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * @description 根据商品规格查询商品价格库存
	 * @param request
	 * @param response
	 * @param id
	 *            商品id
	 * @param gsp
	 *            商品规格
	 * @param color
	 *            商品颜色规格
	 */
	@RequestMapping("loadGoodsGsps.json")
	public void loadGoodsGsps(HttpServletRequest request, HttpServletResponse response, String id, String gsp,
			String color, String count) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> fullReductionActivity = new HashMap<String, Object>();
		Map<String, Object> exemptPostageActivity = new HashMap<String, Object>();
		Map<String, Object> goodsMap = new HashMap<String, Object>();
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		int number = 1;// 购买数量
		int inventory = 0;// 库存数量
		double goods_current_price = 0.0;
		double total_price = 0.0;// 商品总价
		double act_price = 0.0;
		double postagePrice = 0.0;// 满包邮差价
		double enough_free_price = 0.0;// 满包邮邮费
		double reducePrice = 0.0;// 满减差价
		if (obj.getGroup() != null && obj.getGroup_buy() == 2) {// 团购商品统一按照团购价格处理
			for (GroupGoods gg : obj.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(obj.getGroup().getId())) {
					inventory = gg.getGg_count();
					goods_current_price = CommUtil.null2Double(gg.getGg_price());
				}
			}
		} else {
			if (number < CommUtil.null2Int(count)) {
				number = CommUtil.null2Int(count);
			}
			inventory = obj.getGoods_inventory();
			goods_current_price = CommUtil.null2Double(obj.getGoods_current_price());
			if (obj.getInventory_type().equals("spec")) {
				List<CGoods> childGoodsList = obj.getCgoods();
				for (CGoods childGoods : childGoodsList) {
					if (childGoods.getSpec_color().equals(color)) {
						String spec_ids = childGoods.getCombination_id();
						String[] spec_id = spec_ids.split("_");
						String[] gsp_ids = gsp.split(",");
						Arrays.sort(gsp_ids);
						Arrays.sort(spec_id);
						if (Arrays.equals(gsp_ids, spec_id) && childGoods.getGoods_disabled().equals("0")) {
							total_price = CommUtil.mul(number, CommUtil.null2Double(childGoods.getDiscount_price()));
							goodsMap.put("goods_current_price", childGoods.getDiscount_price());
							goodsMap.put("goods_price", childGoods.getGoods_price());
							goodsMap.put("goods_off", "");
							if (obj.getGoods_store().getEnough_free() == 1) {
								enough_free_price = CommUtil.null2Double(obj.getGoods_store().getEnough_free_price());
								postagePrice = CommUtil.subtract(obj.getGoods_store().getEnough_free_price(),
										childGoods.getDiscount_price());
								/*
								 * 计算运费 ship_price =
								 * this.transportTools.buyNowTransFree(request,
								 * response, CommUtil.null2String(obj.getId()),
								 * "express", volume, area.getAreaName(),
								 * CommUtil.null2String(count), gsp, color);
								 */
							}
							if (!childGoods.getDiscount_price().equals("")) {
								double prices = CommUtil
										.div(CommUtil.subtract(childGoods.getGoods_price(),
												childGoods.getDiscount_price().equals("")), childGoods.getGoods_price())
										* 100;
								goodsMap.put("goods_off", Math.round(prices));
							}
							goodsMap.put("goods_inventory", childGoods.getGoods_inventory());
							List<Map<String, Object>> photos = new ArrayList<Map<String, Object>>();
							List<Accessory> accessoryList = childGoods.getC_goods_photos();
							if (accessoryList.size() > 0) {
								for (Accessory accessory : accessoryList) {
									Map<String, Object> photo_map = new HashMap<String, Object>();
									photo_map.put("photo", configService.getSysConfig().getImageWebServer() + "/"
											+ accessory.getPath() + "/" + accessory.getName());
									photos.add(photo_map);
								}
								goodsMap.put("goods_photo", photos);
								goodsMap.put("goods_mian_photo", "");
							} else {
								goodsMap.put("goods_photo", obj.getGoods_main_photo() != null
										? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
												+ obj.getGoods_main_photo().getName()
										: imageWebServer + "/"
												+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
												+ this.configService.getSysConfig().getGoodsImage().getName());
								goodsMap.put("goods_photo", "");
							}
							map.put("spec_map", goodsMap);
							break;
						}
					}
				}
			}
			exemptPostageActivity.put("enough_free_price", enough_free_price);
			exemptPostageActivity.put("postagePrice", postagePrice > 0 ? postagePrice : 0);
		}
		if (obj.getEnough_reduce() == 1) {
			EnoughReduce enoughReduce = this.enoughReduceService
					.getObjById(CommUtil.null2Long(obj.getOrder_enough_reduce_id()));
			if (enoughReduce.getErstatus() == 10 && enoughReduce.getErbegin_time().before(new Date())
					&& enoughReduce.getErend_time().after(new Date())) {
				fullReductionActivity.put("er_id", enoughReduce.getId());
				LinkedHashMap er_json = JSONObject.parseObject(enoughReduce.getEr_json(), LinkedHashMap.class,
						Feature.OrderedField);
				if (!er_json.isEmpty()) {
					double price = 0.0;
					boolean flag = true;
					Iterator iterator = er_json.keySet().iterator();
					while (iterator.hasNext()) {
						Object key = iterator.next();
						Object value = er_json.get(key);
						if (CommUtil.subtract(total_price, key) < 0) {
							fullReductionActivity.put("key", key);
							fullReductionActivity.put("value", value);
							reducePrice = CommUtil.subtract(key, total_price);
							break;
						}
						if (CommUtil.subtract(total_price, key) >= 0) {
							price = CommUtil.null2Double(value);
							fullReductionActivity.put("key", key);
							fullReductionActivity.put("value", value);
						}
					}
					fullReductionActivity.put("reduceAmount", price);
					total_price = CommUtil.subtract(total_price, price);
				}
			} else if (enoughReduce.getErend_time().after(new Date()) && enoughReduce.getErstatus() != 30) {
				enoughReduce.setErstatus(20);
				this.enoughReduceService.update(enoughReduce);
				Set<Long> set = genericEnoughReduce(enoughReduce.getEr_json());
				for (Long lg : set) {
					Goods goods = this.goodsService.getObjById(lg);
					goods.setEnough_reduce(0);
					goods.setOrder_enough_reduce_id("");
					this.goodsService.update(goods);
				}
			}
			fullReductionActivity.put("reducePrice", reducePrice);
		}
		goodsMap.put("total_price", total_price);
		BigDecimal ac_rebate = null;
		if (obj.getActivity_status() == 2) {// 如果是促销商品，并且用户已登录，根据规格配置价格计算相应配置的促销价格
			ActivityGoods actGoods = this.actgoodsService.getObjById(obj.getActivity_goods_id());
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
			act_price = CommUtil.mul(rebate, goods_current_price);
		}
		if (act_price != 0) {
			map.put("act_price", CommUtil.formatMoney(act_price));
		}
		map.put("exemptPostageActivity", exemptPostageActivity);
		map.put("fullReductionActivity", fullReductionActivity);
		this.send_json(Json.toJson(new Result(0, "Successfully", map), JsonFormat.compact()), response);
	}

	// 解析满减活动商品id
	public Set<Long> genericEnoughReduce(String ids) {
		Set<Long> set = new HashSet<Long>();
		List list = (List) Json.fromJson(ids);
		for (Object object : list) {
			set.add(CommUtil.null2Long(object));
		}
		return set;

	}

	/**
	 * @description 根据商品规格查询商品信息 弃用
	 * @param request
	 * @param response
	 * @param id
	 * @param gsp
	 * @param color
	 *            弃用
	 */
	@RequestMapping("load_goods_gsps.json")
	public void load_goods_gsp_colors(HttpServletRequest request, HttpServletResponse response, String id, String gsp,
			String color) {
		Map<String, Object> map = new HashMap<String, Object>();
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(id));
		int count = 0;
		double price = 0;
		double act_price = 0;
		double priceDifference = 0;
		if (obj.getGroup() != null && obj.getGroup_buy() == 2) {// 团购商品统一按照团购价格处理
			for (GroupGoods gg : obj.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(obj.getGroup().getId())) {
					count = gg.getGg_count();
					price = CommUtil.null2Double(gg.getGg_price());
				}
			}
		} else {
			count = obj.getGoods_inventory();
			price = CommUtil.null2Double(obj.getGoods_current_price());
			if (obj.getInventory_type().equals("spec")) {
				List<CGoods> childGoodsList = obj.getCgoods();
				for (CGoods childGoods : childGoodsList) {
					if (childGoods.getSpec_color().equals(color)) {
						Map<String, Object> goodsMap = new HashMap<String, Object>();
						String spec_ids = childGoods.getCombination_id();
						String[] spec_id = spec_ids.split("_");
						String[] gsp_ids = gsp.split(",");
						Arrays.sort(gsp_ids);
						Arrays.sort(spec_id);
						if (Arrays.equals(gsp_ids, spec_id) && childGoods.getGoods_disabled().equals("0")) {
							goodsMap.put("goods_price", CommUtil.formatMoney(childGoods.getGoods_price()));
							goodsMap.put("goods_current_price",
									childGoods.getDiscount_price().equals("") ? "" : childGoods.getDiscount_price());
							goodsMap.put("goods_off", "");
							if (obj.getGoods_store().getEnough_free() == 1) {
								priceDifference = CommUtil.subtract(obj.getGoods_store().getEnough_free_price(),
										childGoods.getDiscount_price());
							}
							if (!childGoods.getDiscount_price().equals("")) {
								double prices = CommUtil
										.div(CommUtil.subtract(childGoods.getGoods_price(),
												childGoods.getDiscount_price().equals("")), childGoods.getGoods_price())
										* 100;
								goodsMap.put("goods_off", Math.round(prices));
							}
							goodsMap.put("goods_inventory", childGoods.getGoods_inventory());
							List<Map<String, Object>> photos = new ArrayList<Map<String, Object>>();
							List<Accessory> accessorys = childGoods.getC_goods_photos();
							if (accessorys.size() > 0) {
								for (Accessory acc : accessorys) {
									Map<String, Object> photo_map = new HashMap<String, Object>();
									photo_map.put("photo", configService.getSysConfig().getImageWebServer() + "/"
											+ acc.getPath() + "/" + acc.getName() + "_middle." + acc.getExt());
									photos.add(photo_map);
								}
								goodsMap.put("goods_photo", photos);
								goodsMap.put("goods_mian_photo", "");
							} else {
								goodsMap.put("goods_mian_photo", obj.getGoods_main_photo() != null
										? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
												+ obj.getGoods_main_photo().getName()
										: imageWebServer + "/"
												+ this.configService.getSysConfig().getGoodsImage().getPath() + "/"
												+ this.configService.getSysConfig().getGoodsImage().getName());
							}
							map.put("spec_map", goodsMap);
							break;
						}
					}
				}
			}
			map.put("priceDifference", priceDifference);
		}
		BigDecimal ac_rebate = null;
		if (obj.getActivity_status() == 2) {// 如果是促销商品，并且用户已登录，根据规格配置价格计算相应配置的促销价格
			ActivityGoods actGoods = this.actgoodsService.getObjById(obj.getActivity_goods_id());
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
			act_price = CommUtil.mul(rebate, price);
		}
		if (act_price != 0) {
			map.put("act_price", CommUtil.formatMoney(act_price));
		}
		this.send_json(Json.toJson(new Result(0, "Successfully", map), JsonFormat.compact()), response);
	}

	/**
	 * 修改来源提示语言
	 * 
	 * @param key
	 * @return
	 */
	public String clickfrom_to_chinese(String key) {
		String str = "其它";
		if (key.equals("search")) {
			str = "搜索";
		}
		if (key.equals("floor")) {
			str = "首页楼层";
		}
		if (key.equals("gcase")) {
			str = "橱窗";
		}

		return str;
	}

	/**
	 * 移动端商品规格信息 将商品属性归类便于前台展示
	 * 
	 * @param id
	 * @return
	 */
	// @RequestMapping("/load_generic_spec.json")
	@RequestMapping("loadGenericSpec.json")
	public void load_generic_spec(HttpServletRequest request, HttpServletResponse response, String id) {
		Result result = null;
		Map map = new HashMap();
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
		List<Map> spec_list = new ArrayList<Map>();
		for (GoodsSpecification g_spec : specs) {
			Map spec_map = new HashMap();
			spec_map.put("id", g_spec.getId());
			spec_map.put("name", g_spec.getName());
			List<GoodsSpecProperty> val = goods.getGoods_specs();
			List<Map> gsp_list = new ArrayList<Map>();
			for (GoodsSpecProperty gsp : val) {
				Map gsp_map = new HashMap();
				if (gsp.getSpec().getId() == g_spec.getId()) {
					gsp_map.put("gsp_id", gsp.getId());
					if (g_spec.getType() != null && !g_spec.getType().equals("")) {
						String tyoe = g_spec.getType();
						if (g_spec.getType().equals("img")) {
							gsp_map.put("gsp_value", this.configService.getSysConfig().getImageWebServer() + "/"
									+ gsp.getSpecImage().getPath() + "/" + gsp.getSpecImage().getName());
						}
						if (g_spec.getType().equals("text")) {
							gsp_map.put("gsp_value", gsp.getValue());
						}
					}
					gsp_list.add(gsp_map);
				}

			}
			spec_map.put("gsp_list", gsp_list);
			spec_list.add(spec_map);
		}
		map.put("spec_info", spec_list);
		this.send_json(Json.toJson(new Result(0, "Successfully", map), JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param pid
	 */
	@RequestMapping("area.json")
	public void queryArea(HttpServletRequest request, HttpServletResponse response, String pid) {
		int code = -1;
		String msg = "";
		List<Area> areas = new ArrayList<Area>();
		Map<String, Object> childmap = new HashMap<String, Object>();
		List<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
		if (pid != null && !pid.equals("")) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("pid", CommUtil.null2Long(pid));
			areas = this.areaService.query(
					"select obj from Area obj where obj.parent.id=:pid order by obj.sequence asc", params, -1, -1);
			for (Area area : areas) {
				Map<String, Object> areaMap = new HashMap<String, Object>();
				areaMap.put("areaid", area.getId());
				areaMap.put("areaname", area.getAreaName());

				List<Area> areachilds = area.getChilds();
				List<Map<String, Object>> areachildsList = new ArrayList<Map<String, Object>>();
				for (Area childs : areachilds) {
					Map<String, Object> areaChildsMap = new HashMap<String, Object>();
					areaChildsMap.put("areaid", childs.getId());
					areaChildsMap.put("areaname", childs.getAreaName());
					areachildsList.add(areaChildsMap);
				}
				areaMap.put("areachildmaps", areachildsList);
				map.add(areaMap);
			}
			code = 0;
			msg = "Successfully";
		} else {
			code = 1;
			msg = "parameter error";
		}
		this.send_json(Json.toJson(new Result(code, msg, map), JsonFormat.compact()), response);
	}

	/**
	 * 加载店铺评分信息
	 * 
	 * @param store
	 * @param mv
	 */
	private void generic_evaluate(Store store, ModelAndView mv) {
		double description_result = 0;
		double service_result = 0;
		double ship_result = 0;
		GoodsClass gc = this.goodsClassService.getObjById(store.getGc_main_id());
		if (store != null && gc != null && store.getPoint() != null) {
			float description_evaluate = CommUtil.null2Float(gc.getDescription_evaluate());// [商品分类描述相符评分，同行业均分]
			float service_evaluate = CommUtil.null2Float(gc.getService_evaluate());// [商品分类服务态度评价，同行业均分]
			float ship_evaluate = CommUtil.null2Float(gc.getShip_evaluate());// [商品分类发货速度评价，同行业均分]

			float store_description_evaluate = CommUtil.null2Float(store.getPoint().getDescription_evaluate());// [描述相符评价]
			float store_service_evaluate = CommUtil.null2Float(store.getPoint().getService_evaluate());// [服务态度评价]
			float store_ship_evaluate = CommUtil.null2Float(store.getPoint().getShip_evaluate());// [发货速度评价]
			// 计算和同行比较结果
			description_result = CommUtil.div(store_description_evaluate - description_evaluate, description_evaluate);
			service_result = CommUtil.div(store_service_evaluate - service_evaluate, service_evaluate);
			ship_result = CommUtil.div(store_ship_evaluate - ship_evaluate, ship_evaluate);
		}
		if (description_result > 0) {
			mv.addObject("description_css", "value_strong");
			mv.addObject("description_result",
					CommUtil.null2String(
							CommUtil.mul(description_result, 100) > 100 ? 100 : CommUtil.mul(description_result, 100))
							+ "%");
		}
		if (description_result == 0) {
			mv.addObject("description_css", "value_normal");
			mv.addObject("description_result", "-----");
		}
		if (description_result < 0) {
			mv.addObject("description_css", "value_light");
			mv.addObject("description_result", CommUtil.null2String(CommUtil.mul(-description_result, 100)) + "%");
		}
		if (service_result > 0) {
			mv.addObject("service_css", "value_strong");
			mv.addObject("service_result", CommUtil.null2String(
					CommUtil.mul(service_result, 100) > 100 ? 100 : CommUtil.mul(service_result, 100)) + "%");
		}
		if (service_result == 0) {
			mv.addObject("service_css", "value_normal");
			mv.addObject("service_result", "-----");
		}
		if (service_result < 0) {
			mv.addObject("service_css", "value_light");
			mv.addObject("service_result", CommUtil.null2String(CommUtil.mul(-service_result, 100)) + "%");
		}
		if (ship_result > 0) {
			mv.addObject("ship_css", "value_strong");
			mv.addObject("ship_result",
					CommUtil.null2String(CommUtil.mul(ship_result, 100) > 100 ? 100 : CommUtil.mul(ship_result, 100))
							+ "%");
		}
		if (ship_result == 0) {
			mv.addObject("ship_css", "value_normal");
			mv.addObject("ship_result", "-----");
		}
		if (ship_result < 0) {
			mv.addObject("ship_css", "value_light");
			mv.addObject("ship_result", CommUtil.null2String(CommUtil.mul(-ship_result, 100)) + "%");
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param goods_id
	 * @param currentPage
	 * @param goods_eva
	 * @description 商品评论查询
	 */
	@RequestMapping("goodsEvaluate.json")
	public void goods_evaluation(HttpServletRequest request, HttpServletResponse response, String goods_id,
			String currentPage, String goods_eva) {
		Map<String, Object> evaluateMap = new HashMap();
		Result result = null;
		ModelAndView mv = new JModelAndView("default/goods_evaluation.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		EvaluateQueryObject qo = new EvaluateQueryObject(currentPage, mv, "addTime", "desc");
		qo.addQuery("obj.evaluate_goods.id", new SysMap("goods_id", CommUtil.null2Long(goods_id)), "=");
		List<String> type = new ArrayList<String>();
		type.add("goods");
		type.add("Brush");
		qo.addQuery("obj.evaluate_type", new SysMap("evaluate_type", type), "in");
		qo.addQuery("obj.evaluate_status", new SysMap("evaluate_status", 0), "=");
		qo.setPageSize(10);
		qo.setCurrentPage(CommUtil.null2Int(currentPage));
		if (goods_eva != null && !CommUtil.null2String(goods_eva).equals("")) {
			if (goods_eva.equals("100")) {
				qo.addQuery("obj.evaluate_photos", new SysMap("evaluate_photos", ""), "!=");
			} else {
				qo.addQuery("obj.evaluate_buyer_val", new SysMap("evaluate_buyer_val", CommUtil.null2Int(goods_eva)),
						"=");
			}
		}
		IPageList pList = this.evaluateService.list(qo);
		List<Evaluate> evaluates = pList.getResult();
		List<Map> evaluateMaps = new ArrayList<Map>();
		for (Evaluate evaluate : evaluates) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("evaid", evaluate.getId());
			if (evaluate.getEvaluate_user() == null) {
				map.put("eva_userName", evaluate.getUser_name() == null || evaluate.getUser_name().equals("") ? ""
						: evaluate.getUser_name().charAt(0) + "***"
								+ evaluate.getUser_name().charAt(evaluate.getUser_name().length() - 1));
				map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources" + "/"
						+ "style" + "/" + "common" + "/" + "images" + "/" + "member.png");
			} else {
				User user = evaluate.getEvaluate_user();
				log.info("用户ID" + user.getId());
				map.put("eva_userName", user.getUserName().charAt(0) + "***"
						+ user.getUserName().charAt(user.getUserName().length() - 1));
				map.put("eva_userName", user.getUserName());
				if (user.getSex() == -1) {
					map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
							+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member.png");
				}
				if (user.getSex() == 0) {
					map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
							+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member0.png");
				}
				if (user.getSex() == 1) {
					map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
							+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member1.png");
				}
			}
			map.put("eva_addTime", evaluate.getAddTime());
			map.put("eva_description", evaluate.getDescription_evaluate());
			map.put("eva_service", evaluate.getDescription_evaluate());
			map.put("eva_service", evaluate.getService_evaluate());
			map.put("eva_ship", evaluate.getShip_evaluate());
			map.put("eva_add_info", evaluate.getAddeva_info());
			map.put("eva_reply_status", evaluate.getReply_status());
			map.put("eva_reply", evaluate.getReply());
			map.put("eva_info", evaluate.getEvaluate_info() == null ? "" : evaluate.getEvaluate_info());
			map.put("eva_add_status", evaluate.getAddeva_status());
			map.put("eva_buyer_val", evaluate.getEvaluate_buyer_val()); // 买家评价，评价类型，1为好评，0为中评，-1为差评[1,2,3,4,5,
																		// 1-5星]

			List<Accessory> Accessoryes = imageTools.queryImgs(evaluate.getEvaluate_photos());
			List<Map> photos = new ArrayList<Map>();
			if (Accessoryes.size() > 0) {
				for (Accessory accessory : Accessoryes) {
					Map<String, Object> accessmap = new HashMap<String, Object>();
					accessmap.put("Evaluate_photos", this.configService.getSysConfig().getImageWebServer() + "/"
							+ accessory.getPath() + "/" + accessory.getName());
					photos.add(accessmap);
				}
				map.put("eva_photo", photos);
			}
			List<Accessory> addAccessoryes = imageTools.queryImgs(evaluate.getAddeva_photos());
			List<Map<String, Object>> addeva_photos = new ArrayList<Map<String, Object>>();
			if (CommUtil.isNotNull(addeva_photos)) {
				for (Accessory accessory : addAccessoryes) {
					Map<String, Object> accessmap = new HashMap<String, Object>();
					accessmap.put("Evaluate_photos", this.configService.getSysConfig().getImageWebServer() + "/"
							+ accessory.getPath() + "/" + accessory.getName());
					addeva_photos.add(accessmap);
				}
				map.put("eva_add_photo", addeva_photos);
			}
			evaluateMaps.add(map);
		}

		Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		evaluateMap.put("eva_well",
				goods.getWell_evaluate() == null ? "" : CommUtil.bigDeCimal(goods.getWell_evaluate()));// 商品好评率,例如：该值为0.96，好评率即为96%
		evaluateMap.put("eva_middle", goods.getMiddle_evaluate());
		int all_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(goods.getId()), "all");
		int well_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(goods.getId()), "5");
		int middle_eval = evaluateViewTools.appQueryByEva(CommUtil.null2String(goods.getId()), "3");
		evaluateMap.put("eva_all_count", all_eval);
		evaluateMap.put("eva_well_count", well_eval);
		evaluateMap.put("eva_middle_count", middle_eval);
		evaluateMap.put("evaluate", evaluateMaps);
		evaluateMap.put("currentpage", pList.getCurrentPage());
		evaluateMap.put("pages", pList.getPages());
		this.send_json(Json.toJson(new Result(0, "Suucessfully", evaluateMap), JsonFormat.compact()), response);
	}

	/**
	 * 根据商城分类查看商品列表
	 * 
	 * @param request
	 * @param response
	 * @param gc_id
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @return
	 */
	@RequestMapping("category_goods.json")
	public void store_goods_list(HttpServletRequest request, HttpServletResponse response, String gc_id,
			String currentPage, String orderBy, String orderType, String brand_ids, String gs_ids, String properties,
			String all_property_status, String detail_property_status, String goods_type, String goods_inventory,
			String goods_transfee, String goods_cod, String gc_ids, String language) {
		Result result = null;
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Map wap_map = new HashMap();
		ModelAndView mv = new JModelAndView("store_goods_list.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		GoodsClass gc = this.goodsClassService.getObjById(CommUtil.null2Long(gc_id));
		Set gc_list = new TreeSet();
		if (gc != null) {
			if (gc.getLevel() == 0) {
				gc_list = gc.getChilds();
			} else if (gc.getLevel() == 1) {
				gc_list = gc.getParent().getChilds();
			} else if (gc.getLevel() == 2) {
				gc_list = gc.getParent().getParent().getChilds();
			}

			List<Map> gc_list_map = new ArrayList<Map>();
			for (Iterator<GoodsClass> iterator = gc_list.iterator(); iterator.hasNext();) {
				GoodsClass goodsclass = iterator.next();
				Map gc_map = new HashMap();
				gc_map.put("gc_id", goodsclass.getId());
				gc_map.put("gc_name", goodsclass.getClassName());
				if (goodsclass.getChilds() != null && !goodsclass.getChilds().equals("")) {
					List<Map> gcc_list_map = new ArrayList<Map>();
					for (Iterator<GoodsClass> iteratorc = goodsclass.getChilds().iterator(); iterator.hasNext();) {
						GoodsClass goodsclassc = iterator.next();
						Map gcc_map = new HashMap();
						gcc_map.put("gcc_name", goodsclassc.getClassName());
						gcc_map.put("gcc_id", goodsclassc.getId());
						gcc_list_map.add(gcc_map);
					}
					gc_map.put("gcc_list_map", gcc_list_map);
					gc_list_map.add(gc_map);
				}
			}
		}

		if (orderBy == null || orderBy.equals("")) {
			orderBy = "weightiness";
		}
		if (orderType == null || orderType.equals("")) {
			orderType = "desc";
		}
		GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv, orderBy, orderType);
		Set<Long> ids = null;
		if (gc != null) {
			ids = this.genericIds(gc.getId());
		}
		if (ids != null && ids.size() > 0) {
			Map paras = new HashMap();
			paras.put("ids", ids);
			gqo.addQuery("obj.gc.id in (:ids)", paras);
		} else {
			Set<Long> goodsclass_id = null;
			goodsclass_id = this.genericGcIds(gc_ids);
			if (goodsclass_id != null && !goodsclass_id.equals("")) {
				Map paras = new HashMap();
				paras.put("ids", goodsclass_id);
				gqo.addQuery("obj.gc.id in (:ids)", paras);
			}
		}

		if (goods_cod != null && !goods_cod.equals("")) {
			gqo.addQuery("obj.goods_cod", new SysMap("goods_cod", 0), "=");
			mv.addObject("goods_cod", goods_cod);
		}
		if (goods_transfee != null && !goods_transfee.equals("")) {
			gqo.addQuery("obj.goods_transfee", new SysMap("goods_transfee", 1), "=");
			mv.addObject("goods_transfee", goods_transfee);
		}
		gqo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
		gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");

		List<Map> goods_property = new ArrayList<Map>();

		if (!CommUtil.null2String(brand_ids).equals("")) {
			if (brand_ids.indexOf(",") < 0) {
				brand_ids = brand_ids + ",";
			}
			String[] brand_id_list = CommUtil.null2String(brand_ids).split(",");
			if (brand_id_list.length == 1) {
				String brand_id = brand_id_list[0];
				gqo.addQuery("obj.goods_brand.id", new SysMap("brand_id", CommUtil.null2Long(brand_id)), "=", "and");
				Map map = new HashMap();
				GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
				if (brand != null) {
					map.put("name", "品牌");
					map.put("value", brand.getName());
					map.put("type", "brand");
					map.put("id", brand.getId());
					goods_property.add(map);
				}
			} else {
				for (int i = 0; i < brand_id_list.length; i++) {
					String brand_id = brand_id_list[i];
					if (i == 0) {
						gqo.addQuery("and (obj.goods_brand.id=" + CommUtil.null2Long(brand_id), null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else if (i == brand_id_list.length - 1) {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id) + ")", null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id), null);
						Map map = new HashMap();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					}
				}
			}
			if (brand_ids != null && !brand_ids.equals("")) {
				wap_map.put("brand_ids", brand_ids);
			}
		}

		if (!CommUtil.null2String(gs_ids).equals("")) {
			List<List<GoodsSpecProperty>> gsp_lists = this.generic_gsp(gs_ids);
			for (int j = 0; j < gsp_lists.size(); j++) {
				List<GoodsSpecProperty> gsp_list = gsp_lists.get(j);
				if (gsp_list.size() == 1) {
					GoodsSpecProperty gsp = gsp_list.get(0);
					gqo.addQuery("gsp" + j, gsp, "obj.goods_specs", "member of", "and");
					Map map = new HashMap();
					map.put("spec_name", gsp.getSpec().getName());
					map.put("value", gsp.getValue());
					map.put("type", "gs");
					map.put("id", gsp.getId());
					goods_property.add(map);
				} else {
					for (int i = 0; i < gsp_list.size(); i++) {
						if (i == 0) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "and(");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else if (i == gsp_list.size() - 1) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs)", "member of", "or");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "or");
							Map map = new HashMap();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						}
					}
				}
			}

			wap_map.put("gs_ids", gs_ids);
		}

		List<Map> propertylist = null;
		if (!CommUtil.null2String(properties).equals("")) {
			String[] properties_list = properties.substring(1).split("\\|");
			for (int i = 0; i < properties_list.length; i++) {
				String property_info = CommUtil.null2String(properties_list[i]);
				String[] property_info_list = property_info.split(",");
				GoodsTypeProperty gtp = this.goodsTypePropertyService
						.getObjById(CommUtil.null2Long(property_info_list[0]));
				Map p_map = new HashMap();
				p_map.put("gtp_name" + i, "%" + gtp.getName().trim() + "%");
				p_map.put("gtp_value" + i, "%" + property_info_list[1].trim() + "%");
				gqo.addQuery("and (obj.goods_property like :gtp_name" + i + " and obj.goods_property like :gtp_value"
						+ i + ")", p_map);
				Map map = new HashMap();
				map.put("name", gtp.getName());
				map.put("value", property_info_list[1]);
				map.put("type", "properties");
				map.put("id", gtp.getId());
				goods_property.add(map);
			}
			wap_map.put("properties", properties);

			// 处理筛选类型互斥,|1,超短裙（小于75cm）|2,纯色
			List<GoodsTypeProperty> filter_properties = new ArrayList<GoodsTypeProperty>();
			List<String> hc_property_list = new ArrayList<String>();// 已经互斥处理过的属性值，在循环中不再处理
			if (gc.getGoodsType() != null) {
				for (GoodsTypeProperty gtp : gc.getGoodsType().getProperties()) {
					boolean flag = true;
					GoodsTypeProperty gtp1 = new GoodsTypeProperty();
					gtp1.setDisplay(gtp.isDisplay());
					gtp1.setGoodsType(gtp.getGoodsType());
					gtp1.setHc_value(gtp.getHc_value());
					gtp1.setId(gtp.getId());
					gtp1.setName(gtp.getName());
					gtp1.setSequence(gtp.getSequence());
					gtp1.setValue(gtp.getValue());
					for (String hc_property : hc_property_list) {
						String[] hc_list = hc_property.split(":");
						if (hc_list[0].equals(gtp.getName())) {
							String[] hc_temp_list = hc_list[1].split(",");
							String[] defalut_list_value = gtp1.getValue().split(",");
							ArrayList<String> defalut_list = new ArrayList<String>(Arrays.asList(defalut_list_value));
							for (String hc_temp : hc_temp_list) {
								defalut_list.remove(hc_temp);
							}
							String value = "";
							for (int i = defalut_list.size() - 1; i >= 0; i--) {
								value = defalut_list.get(i) + "," + value;
							}
							gtp1.setValue(value.substring(0, value.length() - 1));
							flag = false;
							break;
						}

					}
					if (flag) {
						if (!CommUtil.null2String(gtp.getHc_value()).equals("")) {// 取消互斥类型
							String[] list1 = gtp.getHc_value().split("#");
							for (int i = 0; i < properties_list.length; i++) {
								String property_info = CommUtil.null2String(properties_list[i]);
								String[] property_info_list = property_info.split(",");
								if (property_info_list[1].equals(list1[0])) {// 存在该互斥，则需要进行处理
									hc_property_list.add(list1[1]);
								}
							}

						}
						filter_properties.add(gtp);
					} else {
						filter_properties.add(gtp1);
					}
				}
				Map goodsTypemap = new HashMap();
				propertylist = new ArrayList<Map>();
				for (GoodsTypeProperty goodsType : filter_properties) {
					goodsTypemap.put("id", goodsType.getId());
					goodsTypemap.put("name", goodsType.getName());
					// [属性可选值]
					goodsTypemap.put("value", CommUtil.splitByChar(goodsType.getValue(), ","));
					propertylist.add(goodsTypemap);
				}
				wap_map.put("propertylist", propertylist);
			}
		} else {
			// 处理筛选类型互斥
			try {
				if (gc.getGoodsType() != null) {
					List<GoodsTypeProperty> goodstype_property = gc.getGoodsType().getProperties();

					List<Map> goodsTypeProperty_list = new ArrayList<Map>();

					for (GoodsTypeProperty goodsTypeProperty : goodstype_property) {
						Map goodsTypePropertyMap = new HashMap();
						goodsTypePropertyMap.put("Property_id", goodsTypeProperty.getId());
						goodsTypePropertyMap.put("Property_name", goodsTypeProperty.getName());

						List<Map> goodsTypeProperty_v_list = new ArrayList<Map>();
						for (String s : CommUtil.splitByChar(goodsTypeProperty.getValue(), ",")) {
							Map goodsTypeProperty_v_Map = new HashMap();
							goodsTypeProperty_v_Map.put("v_info", s);
							goodsTypeProperty_v_list.add(goodsTypeProperty_v_Map);
						}
						goodsTypePropertyMap.put("v_info_value", goodsTypeProperty_v_list);
						goodsTypeProperty_list.add(goodsTypePropertyMap);
					}
					wap_map.put("propertylist", goodsTypeProperty_list);
				} else {
					wap_map.put("propertylist", "");
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				wap_map.put("propertylist", "");
			}
		}

		try {
			if (gc.getGoodsType().getGbs().size() > 0) {
				List<Map> gb_list = new ArrayList<Map>();
				for (GoodsBrand gb : gc.getGoodsType().getGbs()) {
					Map gbmap = new HashMap();
					gbmap.put("gb_id", gb.getId());
					gbmap.put("gb_name", gb.getName());
					gbmap.put("gb_photo", this.configService.getSysConfig().getImageWebServer() + "/"
							+ gb.getBrandLogo().getPath() + "/" + gb.getBrandLogo().getName());
					gb_list.add(gbmap);
				}
				wap_map.put("gb_list", gb_list);
			}
		} catch (NullPointerException e1) {
			// TODO Auto-generated catch block
			wap_map.put("gb_list", "");
		}

		/*
		 * if (CommUtil.null2Int(goods_inventory) == 0) {// 查询库存大于0
		 * gqo.addQuery("obj.goods_inventory", new SysMap("goods_inventory", 0),
		 * ">"); }
		 */ if (!CommUtil.null2String(goods_type).equals("") && CommUtil.null2Int(goods_type) != -1) {// 查询自营或者第三方经销商商品
			gqo.addQuery("obj.goods_type", new SysMap("goods_type", CommUtil.null2Int(goods_type)), "=");
		}
		IPageList pList = this.goodsService.list(gqo);
		wap_map.put("goods_Pages", pList.getPages());
		// [商品信息]
		List<Goods> goods = pList.getResult();
		List<Map> goodslist = new ArrayList<Map>();
		Set<Map> goodsset = new HashSet<Map>();
		for (Goods obj : goods) {
			Map goodsmap = new HashMap();
			goodsmap.put("goods_id", obj.getId());
			goodsmap.put("goods_name", obj.getGoods_name());
			if ("1".equals(language)) {
				goodsmap.put("goods_name", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
						? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
			}
			goodsmap.put("goods_price", obj.getGoods_price());
			goodsmap.put("goods_current_price", obj.getGoods_current_price());
			goodsmap.put("well_evaluate", obj.getWell_evaluate() == null ? 0 : obj.getWell_evaluate());
			goodsmap.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
									+ this.configService.getSysConfig().getGoodsImage().getName() + "_middle."
									+ this.configService.getSysConfig().getGoodsImage().getExt());
			goodsmap.put("goodsext", obj.getAccessory() != null ? obj.getAccessory().getExt() : null);
			List<Accessory> acc = obj.getGoods_photos();

			List<Map> acclist = new ArrayList<Map>();
			for (Accessory accessory : acc) {
				Map accmap = new HashMap();
				accmap.put("photos", configService.getSysConfig().getImageWebServer() + "/" + accessory.getPath() + "/"
						+ accessory.getName() + "_middle." + accessory.getExt());
				acclist.add(accmap);
			}

			goodsmap.put("goods_photo", acclist);
			goodsmap.put("goods_status", obj.getGoods_status());
			goodsmap.put("store_status", obj.getGoods_store().getStore_status());
			goodsmap.put("goods_collect", obj.getGoods_collect());
			goodsmap.put("goods_discount_rate", obj.getGoods_discount_rate());
			if (obj.getGoods_store() != null && !obj.getGoods_store().equals("")) {
				try {
					goodsmap.put("store_logo",
							this.configService.getSysConfig().getImageWebServer() + "/"
									+ obj.getGoods_store().getStore_logo().getPath() + "/"
									+ obj.getGoods_store().getStore_logo().getName());
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					goodsmap.put("store_logo", "");
				}
			}
			goodslist.add(goodsmap);
		}
		wap_map.put("goodslist", goodslist);
		// [已选择商品属性]
		Map goods_property_map = new HashMap();
		List<Map> goods_property_list = new ArrayList<Map>();
		for (Map map : goods_property) {
			goods_property_map.put("name", map.get("name"));
			goods_property_map.put("value", map.get("value"));
			goods_property_map.put("id", map.get("id"));
			goods_property_map.put("type", map.get("type"));
			goods_property_list.add(goods_property_map);
		}
		wap_map.put("goods_property_list", goods_property_list);
		wap_map.put("orderBy", orderBy);
		wap_map.put("goods_property", goods_property);
		wap_map.put("allCount", pList.getRowCount());
		if (detail_property_status != null && !detail_property_status.equals("")) {
			mv.addObject("detail_property_status", detail_property_status);
			String temp_str[] = detail_property_status.split(",");
			Map pro_map = new HashMap();
			List pro_list = new ArrayList();
			for (String property_status : temp_str) {
				if (property_status != null && !property_status.equals("")) {
					String mark[] = property_status.split("_");
					pro_map.put(mark[0], mark[1]);

					pro_list.add(mark[0]);
				}
			}
			/*
			 * mv.addObject("pro_list", pro_list); mv.addObject("pro_map",
			 * pro_map);
			 */
		}
		wap_map.put("all_property_status", all_property_status);

		// 计算当期访问用户的IP地址，并计算对应的运费信息
		String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
		if (CommUtil.isIp(current_ip)) {
			IPSeeker ip = new IPSeeker(null, null);
			String current_city = ip.getIPLocation(current_ip).getCountry();
			wap_map.put("current_city", current_city);
			// mv.addObject("current_city", current_city);
		} else {
			wap_map.put("current_city", "未知地区");
		}
		wap_map.put("goods_inventory", CommUtil.null2Int(goods_inventory));
		wap_map.put("goods_type", CommUtil.null2String(goods_type).equals("") ? -1 : CommUtil.null2Int(goods_type));
		this.send_json(Json.toJson(new Result(0, "Successfully", wap_map), JsonFormat.compact()), response);
	}

	private Set<Long> genericIds(Long id) {
		Set<Long> ids = new HashSet<Long>();
		if (id != null) {
			ids.add(id);
			Map params = new HashMap();
			params.put("pid", id);
			List id_list = this.goodsClassService.query("select obj.id from GoodsClass obj where obj.parent.id=:pid",
					params, -1, -1);
			ids.addAll(id_list);
			for (int i = 0; i < id_list.size(); i++) {
				Long cid = CommUtil.null2Long(id_list.get(i));
				Set<Long> cids = genericIds(cid);
				ids.add(cid);
				ids.addAll(cids);
			}
		}
		return ids;
	}

	/**
	 * 查询多个一级类目的子类目
	 * 
	 * @param gclass_ids
	 * @return
	 */
	private Set<Long> genericGcIds(String gclass_ids) {
		String[] gc_ids = gclass_ids.split(",");
		Set<Long> ids = new HashSet<Long>();
		for (String id : gc_ids) {
			if (id != null) {
				Long lid = Long.parseLong(id);
				ids.add(lid);
				Map params = new HashMap();
				params.put("pid", lid);
				List id_list = this.goodsClassService
						.query("select obj.id from GoodsClass obj where obj.parent.id=:pid", params, -1, -1);
				ids.addAll(id_list);
				for (int i = 0; i < id_list.size(); i++) {
					Long cid = CommUtil.null2Long(id_list.get(i));
					Set<Long> cids = genericIds(cid);
					ids.add(cid);
					ids.addAll(cids);
				}
			}
		}
		return ids;
	}

	private List<List<GoodsSpecProperty>> generic_gsp(String gs_ids) {
		List<List<GoodsSpecProperty>> list = new ArrayList<List<GoodsSpecProperty>>();
		String[] gs_id_list = gs_ids.substring(1).split("\\|");
		for (String gd_id_info : gs_id_list) {
			String[] gs_info_list = gd_id_info.split(",");
			GoodsSpecProperty gsp = this.goodsSpecPropertyService.getObjById(CommUtil.null2Long(gs_info_list[0]));
			boolean create = true;
			for (List<GoodsSpecProperty> gsp_list : list) {
				for (GoodsSpecProperty gsp_temp : gsp_list) {
					if (gsp_temp.getSpec().getId().equals(gsp.getSpec().getId())) {
						gsp_list.add(gsp);
						create = false;
						break;
					}
				}
			}
			if (create) {
				List<GoodsSpecProperty> gsps = new ArrayList<GoodsSpecProperty>();
				gsps.add(gsp);
				list.add(gsps);
			}
		}
		return list;
	}

	/**
	 * @Description 根据运费模板信息、商品重量及配送城市计算商品运费，配送城市根据IP自动获取
	 * @param request
	 * @param response
	 * @param goods_id
	 * @param type
	 * @param volume
	 * @param city_name
	 * @param number
	 * @param gsp
	 * @param color
	 * @return 商品运费
	 */
	@RequestMapping("getFreight.json")
	public void getFreight(HttpServletRequest request, HttpServletResponse response, String goods_id, String type,
			String volume, String city_name, String number, String gsp, String color) {
		int code = -1;
		String message = "";
		Result result = null;
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		if (obj != null) {
			int count = 1;
			if (Integer.parseInt(number) >= 1) {
				count = CommUtil.null2Int(number);
			}
			Map gspmap = this.metooCartViewTools.generic_default_info_color(obj, gsp, color);
			int goods_inventory = CommUtil.null2Int(gspmap.get("count"));
			if (count < goods_inventory) {
				double goods_weight = 0.0;
				double length = 0.0;
				double goods_width = 0.0;
				double goods_high = 0.0;
				double value = 0.0;
				float fee = 0;
				if (obj.getInventory_type().equals("all")) {
					length = CommUtil.null2Double(obj.getGoods_length());
					goods_width = CommUtil.null2Double(obj.getGoods_width());
					goods_high = CommUtil.null2Double(obj.getGoods_high());
					goods_weight = CommUtil.mul(count, obj.getGoods_weight());
				} else {
					length = CommUtil.null2Double(gspmap.get("length"));
					goods_width = CommUtil.null2Double(gspmap.get("goods_width"));
					goods_high = CommUtil.null2Double(gspmap.get("goods_high"));
					goods_weight = CommUtil.null2Double(gspmap.get("goods_weight"));
				}
				Object trans_id = obj.getGoods_store().getTransport() == null ? ""
						: obj.getGoods_store().getTransport().getId();
				Transport trans = this.transportService.getObjById(CommUtil.null2Long(trans_id));
				if (obj.getGoods_transfee() == 0 && trans != null) {
					String json = "";
					if (type.equals("mail")) {
						json = trans.getTrans_mail_info();
					}
					if (type.equals("express")) {
						json = trans.getTrans_express_info();
					}
					if (type.equals("ems")) {
						json = trans.getTrans_ems_info();
					}
					boolean cal_flag = false;// 是否已经计算过运费，用在没有特殊配置的区域，没有特殊配置的区域使用默认价格计算
					List<Map> list = Json.fromJson(ArrayList.class, CommUtil.null2String(json));
					if (list != null && list.size() > 0) {
						for (Map map : list) {
							String[] city_list = CommUtil.null2String(map.get("city_name")).split("、");
							for (String city : city_list) {
								if (city_name.indexOf(city) >= 0 || city.equals(city_name)) {
									cal_flag = true;
									float trans_weight = CommUtil.null2Float(map.get("trans_weight"));
									float trans_fee = CommUtil.null2Float(map.get("trans_fee"));
									float trans_add_weight = CommUtil.null2Float(map.get("trans_add_weight"));
									float trans_add_fee = CommUtil.null2Float(map.get("trans_add_fee"));
									if (trans.getTrans_type() == 0) {// 按照件数计算运费
										fee = trans_fee;
									}
									if (trans.getTrans_type() == 1) {// 按照重量计算运费用
										double goods_volume = CommUtil.mul(CommUtil.mul(length, goods_width),
												goods_high);
										double volume_wight = CommUtil.mul(CommUtil.div(goods_volume, 6000), 1000);
										if (count > 1) {
											goods_weight = CommUtil.mul(count, goods_weight);
											volume_wight = CommUtil.mul(count, volume_wight);
										}
										if (CommUtil.subtract(volume_wight, goods_weight) > 0) {
											value = volume_wight;
										} else {
											value = goods_weight;
										}
										if (CommUtil.subtract(value, trans_weight) > 0) {
											fee = trans_fee;// 首重运费
											float other_price = 0;
											if (trans_add_weight > 0) {
												other_price = (float) (trans_add_fee
														* Math.ceil(CommUtil.subtract(goods_weight, trans_weight)
																/ trans_add_weight));
											}
											fee = fee + other_price;
										} else {
											fee = trans_fee;
										}
									}
									if (trans.getTrans_type() == 2) {// 按照体积计算运费用
										float goods_volume = CommUtil.null2Float(volume);
										if (goods_volume > 0) {
											fee = trans_fee;
											float other_price = 0;
											if (trans_add_weight > 0) {
												other_price = (float) (trans_add_fee
														* (Math.ceil(CommUtil.subtract(goods_volume, trans_weight)
																/ trans_add_weight)));
											}
											fee = fee + other_price;
										}
									}
									break;
								}
							}
						}
						if (!cal_flag) {// 如果没有找到配置所在的区域运费信息，则使用全国价格进行计算
							for (Map map : list) {
								String[] city_list = CommUtil.null2String(map.get("city_name")).split("、");
								for (String city : city_list) {
									if (city.equals("全国")) {
										float trans_weight = CommUtil.null2Float(map.get("trans_weight"));
										float trans_fee = CommUtil.null2Float(map.get("trans_fee"));
										float trans_add_weight = CommUtil.null2Float(map.get("trans_add_weight"));
										float trans_add_fee = CommUtil.null2Float(map.get("trans_add_fee"));
										if (trans.getTrans_type() == 0) {// 按照件数计算运费
											fee = trans_fee;
										}
										if (trans.getTrans_type() == 1) {// 按照重量计算运费用
											double goods_volume = CommUtil.mul(CommUtil.mul(length, goods_width),
													goods_high);
											double volume_wight = CommUtil.mul(CommUtil.div(goods_volume, 6000), 1000);
											if (count > 1) {
												goods_weight = CommUtil.mul(count, goods_weight);
												volume_wight = CommUtil.mul(count, volume_wight);
											}
											if (CommUtil.subtract(volume_wight, goods_weight) > 0) {
												value = volume_wight;
											} else {
												value = goods_weight;
											}
											if (CommUtil.subtract(value, trans_weight) > 0) {
												fee = trans_fee;// 首重运费
												float other_price = 0;
												if (trans_add_weight > 0) {
													other_price = (float) (trans_add_fee
															* Math.ceil(CommUtil.subtract(goods_weight, trans_weight)
																	/ trans_add_weight));
												}
												fee = fee + other_price;
											} else {
												fee = trans_fee;
											}
										}
										if (trans.getTrans_type() == 2) {// 按照体积计算运费用
											float goods_volume = CommUtil.null2Float(volume);
											if (goods_volume > 0) {
												fee = trans_fee;
												float other_price = 0;
												if (trans_add_weight > 0) {
													other_price = (float) (trans_add_fee
															* (Math.ceil(CommUtil.subtract(goods_volume, trans_weight)
																	/ trans_add_weight)));
												}
												fee = fee + other_price;
											}
										}
										break;
									}
								}
							}
						}
					}
				}
				result = new Result(4200, "Successfully", fee);
			} else {
				result = new Result(4206, "Exceed the maximum stock of goods");
			}
		} else {
			result = new Result(4205, "Goods don't exist");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 生成随机时间
	 *
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static Date randomDate(String beginDate, String endDate) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date start = format.parse(beginDate);// 构造开始日期
			Date end = format.parse(endDate);// 构造结束日期
			// getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
			System.out.println(start.getTime());
			if (start.getTime() >= end.getTime()) {
				return null;
			}
			long date = random(start.getTime(), end.getTime());
			return new Date(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static long random(long begin, long end) {
		long rtn = begin + (long) (Math.random() * (end - begin));
		// 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
		if (rtn == begin || rtn == end) {
			return random(begin, end);
		}
		return rtn;
	}

	public static void main(String[] args) {
		Date randomDate = randomDate("2010-09-20", "2010-09-22");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String result = format.format(randomDate);
		System.out.println(result);
	}

	/**
	 * @description 商品刷评
	 * @param request
	 * @param response
	 * @param file
	 * @param goods_ids
	 */
	@RequestMapping("brushComments.json")
	public void brushComments(HttpServletRequest request, HttpServletResponse response, MultipartFile file,
			String goods_ids, String beginDate, String endDate) {
		String msg = "";
		int code = -1;
		log.debug("刷评");
		InputStream input = null;
		HSSFWorkbook wb = null;
		Result result = null;
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (null != goods_ids && !"".equals(goods_ids)) {
			try {
				input = file.getInputStream();
				wb = new HSSFWorkbook(input);
				HSSFSheet sheet1 = wb.getSheet("Sheet1");
				// FileInputStream fileInputStream = new FileInputStream(file);
				// 获取系统文档
				// POIFSFileSystem fspoi = new POIFSFileSystem();
				// 创建工作簿对象
				// HSSFWorkbook wb = new HSSFWorkbook(fspoi);
				// 创建工作表
				// HSSFSheet sheet1 = wb.getSheet("Sheet1");
				// 得到Excel表格
				// HSSFRow row = sheet1.getRow(2);
				// 得到Excel工作表指定行的单元格
				// HSSFCell cell = row.getCell(2);
				List list = new ArrayList();
				for (int i = 1; i < sheet1.getPhysicalNumberOfRows(); i++) {
					List list1 = new ArrayList();
					HSSFRow row = sheet1.getRow(i);
					for (int j = 0; j < row.getLastCellNum(); j++) {
						// map.put(sheet1.getRow(1).getCell(j), row.getCell(j));
						list1.add(row.getCell(j));
					}
					list.add(list1);
				}
				String[] ids = goods_ids.split(",");
				int[] number = new int[] { 3, 4, 5 };
				int[] dateNumber = new int[] { 1, 2, 3, 4, };
				for (int i = 0; i < list.size(); i++) {
					List list2 = (List) list.get(i);
					for (int j = 0; j < list2.size(); j++) {
						Goods goods = this.goodsService
								.getObjById(CommUtil.null2Long(ids[new Random().nextInt(ids.length)]));
						Evaluate eva = new Evaluate();
						goods.setEvaluate_count(goods.getEvaluate_count() + 1);
						eva.setAddTime(this.randomDate(beginDate, endDate));
						eva.setEvaluate_goods(goods);
						eva.setEvaluate_info(list2.size() == 1 ? "" : CommUtil.null2String(list2.get(j + 1)));
						// eva.setEvaluate_photos(request.getParameter("evaluate_photos"));
						eva.setEvaluate_buyer_val(number[new Random().nextInt(number.length)]);
						eva.setDescription_evaluate(
								CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
						eva.setService_evaluate(CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
						eva.setShip_evaluate(CommUtil.null2BigDecimal(number[new Random().nextInt(number.length)]));
						eva.setEvaluate_type("Brush");
						eva.setUser_name(CommUtil.null2String(list2.get(j)) == "" ? "Khalidi"
								: CommUtil.null2String(list2.get(j)));
						eva.setReply_status(0);
						this.goodsService.update(goods);
						this.evaluateService.save(eva);
						break;
					}
				}

				// 计算商品好评率
				Map params = new HashMap();
				List<Goods> goods_list = new ArrayList<Goods>();
				for (String str : ids) {
					Goods obj = this.goodsService.getObjById(CommUtil.null2Long(str));
					goods_list.add(obj);
				}

				for (Goods goods : goods_list) {
					// 统计所有商品的描述相符评分
					double description_evaluate = 0;
					params.clear();
					params.put("evaluate_goods_id", goods.getId());
					List<Evaluate> eva_list = this.evaluateService.query(
							"select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id", params, -1,
							-1);
					// [add 浮点数据加法]
					for (Evaluate eva : eva_list) {
						description_evaluate = CommUtil.add(eva.getDescription_evaluate(), description_evaluate);
					}
					// [div 浮点数除法运算]
					description_evaluate = CommUtil.div(description_evaluate, eva_list.size());

					goods.setDescription_evaluate(BigDecimal.valueOf(description_evaluate));
					if (eva_list.size() > 0) {// 商品有评价情况下
						// 统计所有商品的好评率
						double well_evaluate = 0;
						double well_evaluate_num = 0;
						params.clear();
						params.put("evaluate_goods_id", goods.getId());
						// params.put("evaluate_buyer_val", 5);
						String id = CommUtil.null2String(goods.getId());
						// 星级率
						int num = this.databaseTools.queryNum("select SUM(evaluate_buyer_val) from "
								+ Globals.DEFAULT_TABLE_SUFFIX + "evaluate where evaluate_goods_id=" + id
								+ " and evaluate_buyer_val BETWEEN 1 AND 5 ");

						well_evaluate_num = CommUtil.mul(5, eva_list.size());
						well_evaluate = CommUtil.div(num, well_evaluate_num);
						goods.setWell_evaluate(BigDecimal.valueOf(well_evaluate));
						// 统计所有商品的中评率
						double middle_evaluate = 0;
						params.clear();
						params.put("evaluate_goods_id", goods.getId());
						// params.put("evaluate_buyer_val", 3);
						List<Evaluate> middle_list = this.evaluateService.query(
								"select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id and obj.evaluate_buyer_val BETWEEN 2 AND 3",
								params, -1, -1);
						middle_evaluate = CommUtil.div(middle_list.size(), eva_list.size());
						goods.setMiddle_evaluate(BigDecimal.valueOf(middle_evaluate));
						// 统计所有商品的差评率
						double bad_evaluate = 0;
						params.clear();
						params.put("evaluate_goods_id", goods.getId());
						params.put("evaluate_buyer_val", 1);
						List<Evaluate> bad_list = this.evaluateService.query(
								"select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id and obj.evaluate_buyer_val=:evaluate_buyer_val",
								params, -1, -1);
						bad_evaluate = CommUtil.div(bad_list.size(), eva_list.size());
						goods.setBad_evaluate(BigDecimal.valueOf(bad_evaluate));
					}
					this.goodsService.update(goods);
				}
				code = 4200;
				msg = "Successfully";
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				code = 4501;
				msg = "FileNotFoundException";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				code = 4502;
				msg = "IOException";
			} catch (NullPointerException e) {
				code = 4503;
				msg = "NullPointerException";
			}
		} else {
			code = 4403;
			msg = "The product ID is empty";
		}
		result = new Result(code, msg);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	@RequestMapping("pointGoods.json")
	public void query(HttpServletRequest request, HttpServletResponse response, String currentPage, Integer count,
			String token) {
		Result result = null;
		Map map = new HashMap();
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		User user = null;
		if (token != null && !token.equals("")) {
			Map params = new HashMap();
			params.put("app_login_token", token);
			user = this.userService.getObjByProperty(null, "app_login_token", token);

		}
		/*
		 * ModelAndView mv = new JModelAndView("", configService.getSysConfig(),
		 * this.userConfigService.getUserConfig(), 0, request, response);
		 * PointQueryObject qo = new PointQueryObject(currentPage, mv, "", "");
		 * // 兑换活动不做时间限制
		 * 
		 * qo.addQuery("DATE_FORMAT(obj.ptbegin_time,'%Y-%m-%d')", new
		 * SysMap("ptbegin_time", CommUtil.formatTime("yyyy-MM-dd", new
		 * Date())), "<=");
		 * qo.addQuery("DATE_FORMAT(obj.ptend_time,'%Y-%m-%d')", new
		 * SysMap("ptend_time", CommUtil.formatTime("yyyy-MM-dd", new Date())),
		 * ">=");
		 * 
		 * // qo.addQuery("obj.goods_store.store_status", new //
		 * SysMap("store_status", 15), "="); qo.setOrderBy("addTime");
		 * qo.setOrderType("desc"); qo.addQuery("obj.point_status", new
		 * SysMap("point_status", 0), "="); // this.pointService.query(
		 * "select obj from Point obj where ", params, // begin, max)
		 * 
		 * IPageList pList = this.pointService.list(qo); List<Point> points =
		 * pList.getResult();
		 */
		Map params = new HashMap();
		params.put("type", 0);
		List<Point> points = this.pointService.query("select obj from Point obj where obj.type=:type", params, -1, -1);
		if (points.size() > 0) {
			params.clear();
			String hql = "1=1";
			List<Goods> list = new ArrayList<Goods>();
			List rules = new ArrayList();
			List link = new ArrayList();
			Map rulesMap = new HashMap();
			Map linkMap = new HashMap();
			for (Point point : points) {
				String goods_json = point.getGoods_ids_json();
				if (null != goods_json) {
					Set<Long> ids = this.genericIds(goods_json);
					if (!CommUtil.null2String(point.getRules()).equals("")) {
						Map rulesJson = Json.fromJson(Map.class, point.getRules());
						rulesMap.put("Arab", rulesJson.get("Arab"));
						rulesMap.put("USA", rulesJson.get("USA"));
						rules.add(rulesMap);
					}
					if (!CommUtil.null2String(point.getLink()).equals("")) {
						Map linkJson = Json.fromJson(Map.class, point.getLink());
						linkMap.put("Android", linkJson.get("Android"));
						linkMap.put("IOS", linkJson.get("IOS"));
						link.add(linkMap);
					}
					List<Goods> goodslist = new ArrayList<Goods>();
					if (!ids.isEmpty()) {
						if (count != null || count.intValue() == -1) {
							params.put("ids", ids);
							params.put("goods_status", 4);
							params.put("store_status", 15);
							params.put("point_status", 10);
							goodslist = this.goodsService.query(
									"select obj from Goods obj where obj.id in (:ids) and obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status and obj.point_status=:point_status order by obj.sequence desc",
									params, -1, -1);
						} else {
							params.put("ids", ids);
							params.put("goods_status", 4);
							params.put("store_status", 15);
							params.put("point_status", 10);
							goodslist = this.goodsService.query(
									"select obj from Goods obj where obj.id in (:ids) and obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status and obj.point_status=:point_status order by obj.sequence desc",
									params, 0, count);
						}
						list.addAll(goodslist);
					}
				}
			}
			map.put("rules", rules);
			map.put("link", link);

			List<Map> goodsLists = new ArrayList<Map>();
			for (Goods obj : list) {
				if (obj.getGoods_status() == 4 && obj.getGoods_store().getStore_status() == 15) {
					Map objmap = new HashMap();
					objmap.put("goods_img",
							obj.getGoods_main_photo() != null
									? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
											+ obj.getGoods_main_photo().getName()
									: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath()
											+ "/" + this.configService.getSysConfig().getGoodsImage().getName());
					objmap.put("goods_id", obj.getId());
					objmap.put("goods_name", obj.getGoods_name());
					objmap.put("goods_current_price", obj.getGoods_current_price());
					objmap.put("goods_inventory", obj.getGoods_inventory());
					objmap.put("goods_evaluate_count", obj.getEvaluate_count());
					objmap.put("goods_price", obj.getGoods_price());
					objmap.put("well_evaluate", obj.getWell_evaluate());
					objmap.put("good_middle_evaluate", obj.getMiddle_evaluate());
					int all_eval = evaluateViewTools.queryByEva(CommUtil.null2String(obj.getId()), "all").size();
					int well_eval = evaluateViewTools.queryByEva(CommUtil.null2String(obj.getId()), "5").size();
					int middle_eval = evaluateViewTools.queryByEva(CommUtil.null2String(obj.getId()), "3").size();
					objmap.put("eva_all_count", all_eval);
					objmap.put("eva_well_count", well_eval);
					objmap.put("goods_point", obj.getPoint());
					objmap.put("goods_salenum", obj.getGoods_salenum());
					objmap.put("goods_gc_id", obj.getGc().getParent().getParent().getId());
					objmap.put("goods_detail", obj.getGoods_details() == null ? "" : obj.getGoods_details());
					objmap.put("goods_status", obj.getGoods_status());
					objmap.put("store_status", obj.getGoods_store().getStore_status());
					objmap.put("goods_sequence", obj.getSequence() == null ? 0 : obj.getSequence());
					if (obj.getGoods_global() == 1) {
						objmap.put("goods_arrival_time", "It is expected to be delivered in 10-15 working days");
					} else {
						if (obj.getGoods_global() == 2) {
							objmap.put("goods_arrival_time", "It is expected to be delivered in 1-3 working days");
						}
					}
					if (obj.getPoint_id() != null && !obj.getPoint_id().equals("")) {
						objmap.put("goods_pointNum", obj.getPointNum());
						objmap.put("goods_percentage",
								user == null ? 0 : CommUtil.div(user.getPointNum(), obj.getPointNum()));
					}
					objmap.put("user_pointNum", user == null ? 0 : user.getPointNum());
					objmap.put("goods_discount_rate", obj.getGoods_discount_rate());
					goodsLists.add(objmap);
				}
			}
			map.put("goods", goodsLists);
			result = new Result(0, "success", map);
		} else {
			result = new Result(1009, "没有商品");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @descript 查询所有店铺满减
	 */
	@RequestMapping("enoughreduceGoods.json")
	@ResponseBody
	public String enoughreduceGoods(HttpServletRequest request, HttpServletResponse response) {
		int code = -1;
		String msg = "";
		Map params = new HashMap();
		params.put("status", 15);
		List<Store> stores = this.storeService.query("select obj from Store obj where obj.store_status=:status", params,
				-1, -1);
		List list = new ArrayList();
		if (stores.size() > 0) {
			// List<EnoughReduce> enoughReduces = new ArrayList<EnoughReduce>();
			for (Store store : stores) {
				List storeList = new ArrayList();
				Map map = new HashMap();
				params.clear();
				params.put("store_id", store.getId().toString());
				params.put("begin_time", new Date());
				params.put("end_time", new Date());
				params.put("erstatus", 10);
				List<EnoughReduce> enoughReduces = this.enoughReduceService.query(
						"select obj from EnoughReduce obj where obj.store_id=:store_id and obj.erbegin_time<=:begin_time and obj.erend_time>=:end_time and obj.erstatus=:erstatus",
						params, -1, -1);
				for (EnoughReduce enoughReduce : enoughReduces) {
					Map enoughReduceMap = new HashMap();
					String json = enoughReduce.getEr_json();
					Map fromJson = (Map) Json.fromJson(json);
					double price = 0.0;
					double reduce = 0.0;
					Object enough = "";
					for (Object key : fromJson.keySet()) {
						reduce = Double.parseDouble((fromJson.get(key).toString().trim()));
						if (CommUtil.subtract(reduce, price) > 0) {
							price = reduce;
							enough = key;
						}
					}
					enoughReduceMap.put("enough_reduce_id", enoughReduce.getId());
					enoughReduceMap.put("key", enough);
					enoughReduceMap.put("value", price);
					List<Goods> goodsList = new ArrayList<Goods>();
					List<String> goods_id_list = new ArrayList<String>();
					String goods_json = enoughReduce.getErbanner_goods_ids();
					if (goods_json != null && !goods_json.equals("")) {
						if (goods_json != null && !goods_json.equals("") && goods_json.length() > 2) {
							goods_id_list = (List<String>) Json.fromJson(goods_json);
						}
						if (goods_id_list.size() > 0 && !goods_id_list.isEmpty()) {
							Set<Long> set = new HashSet<Long>();
							for (String id : goods_id_list) {
								set.add(CommUtil.null2Long(id));
							}
							params.clear();
							params.put("goods_ids", set);
							goodsList = this.goodsService.query(
									"select new Goods(id,goods_name,goods_current_price,goods_collect,goods_salenum,goods_main_photo) from Goods obj where obj.id in(:goods_ids)",
									params, -1, -1);
						}
					}
					List enoughReduceGoodsList = this.mGoodsViewTools.pointGoods(goodsList);
					if (this.mGoodsViewTools.pointGoods(goodsList).size() > 0) {
						enoughReduceMap.put("goodsList", this.mGoodsViewTools.pointGoods(goodsList));
						storeList.add(enoughReduceMap);
					}
				}
				if (!storeList.isEmpty()) {
					map.put("store_id", store.getId());
					map.put("store_name", store.getStore_name());
					map.put("store_enoughreduce", storeList);
					list.add(map);
				}
			}
			code = 4200;
			msg = "Successfully";
		}
		return Json.toJson(new Result(code, msg, list), JsonFormat.compact());
	}

	/**
	 * @description 批量更新商品属性
	 */
	// @RequestMapping("updateGoodsProperties.json")
	@RequestMapping("updateGoodsProperties.json")
	public void updateGoodsProperties(HttpServletRequest request, HttpServletResponse response) {
		Map params = new HashMap();
		params.put("id", Long.parseLong("22"));
		final List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.goods_store.id=:id",
				params, -1, -1);
		GoodsBatcUpdateUtil util = this.goodsBatcUpdateUtil.getInstance();
		this.goodsBatcUpdateUtil.updateGoodsProperties(goodsList);
		this.send_json(Json.toJson("Successfully", JsonFormat.compact()), response);
	}

	private Set<Long> genericIds(String str) {
		Set<Long> ids = new HashSet<Long>();
		List list = (List) Json.fromJson(str);
		for (Object object : list) {
			ids.add(CommUtil.null2Long(object));
		}
		return ids;
	}

	@RequestMapping("/state.json")
	public void state(HttpServletRequest request, HttpServletResponse response) {
		String current_ip = CommUtil.getIpAddr(request);
		System.out.println("当前IP：" + current_ip);
		if (CommUtil.isIp(current_ip)) {
			IPSeeker ip = new IPSeeker(null, null);
			String current_city = ip.getIPLocation(current_ip).getCountry();
			this.send_json(current_city, response);
		}
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Access-Control-Allow-Credentials", "true");// 允许携带cookie
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