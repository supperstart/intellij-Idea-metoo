package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.GoodsBatcUpdateUtil;
import com.metoo.app.view.web.tool.MCartViewTools;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.ActivityGoods;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.CombinPlan;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.FootPoint;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.Group;
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserRecommend;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
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

@Controller
@RequestMapping("/app/v2/")
public class MGoodsViewActionV2 {

	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private MGoodsViewTools mGoodsViewTools;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private EvaluateViewTools evaluateViewTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IFootPointService footPointService;
	@Autowired
	private IActivityGoodsService actgoodsService;
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
	private IFavoriteService favoriteService;
	@Autowired
	private IPointService pointService;
	@Autowired
	private IUserRecommendService userRecommendService;
	@Autowired
	private GoodsBatcUpdateUtil goodsBatcUpdateUtil;

	@RequestMapping("goods.json")
	@ResponseBody
	public String goods(HttpServletRequest request, HttpServletResponse response, String id, String token,
			String language) {
		Result result = null;
		User user = null;
		if (!CommUtil.null2String(token).equals("")) {
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

					Cookie[] cookies = request.getCookies();
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
							/*
							 * if (point.getPtbegin_time().before(new Date()) &&
							 * point.getPtend_time().after(new Date())) {
							 * goods_pointNum = obj.getPointNum();
							 * goods_percentage = (int) (user == null ? 0 :
							 * CommUtil.div(user.getPointNum(),
							 * obj.getPointNum())); goods_point = obj.getPoint()
							 * == 1 && obj.getPoint_status() == 10 ? 1 : 0; }
							 * else { point.setPoint_status(20);
							 * this.pointService.update(point); }
							 */
						}
						objmap.put("goods_pointNum", goods_pointNum);
						objmap.put("goods_percentage", goods_percentage);
						objmap.put("goods_point", goods_point);
						this.goodsService.update(obj);
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
						if (obj.getGoods_global() == 1) {
							objmap.put("goods_arrival_time", "It is expected to be delivered in 10-15 working days");
						} else {
							if (obj.getGoods_global() == 2) {
								objmap.put("goods_arrival_time", "It is expected to be delivered in 1-3 working days");
							}
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
						//goodsmap.put("coupon", this.mGoodsViewTools.queryCoupon(obj.getGoods_store(), user, tourists));
						Map<String, Object> storeMap = new HashMap<String, Object>();
						// [查询商家评分信息 generic_evaluate ]
						if (obj.getGoods_type() == 0) {
							storeMap.put("store_name", "self");
						} else {
							storeMap.put("store_id", obj.getGoods_store().getId());
							storeMap.put("store_name", obj.getGoods_store().getStore_name());
							int enoughfree_status = 0;
							int enoughfree_price = 0;
							if (this.configService.getSysConfig().getEnoughfree_status() == 1) {
								enoughfree_status = this.configService.getSysConfig().getEnoughfree_status();
								enoughfree_price = this.configService.getSysConfig().getEnoughfree_price();
							}
							storeMap.put("enoughfree_status", enoughfree_status);
							storeMap.put("enoughfree_price", enoughfree_price);
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
		// this.send_json(Json.toJson(result, JsonFormat.compact()), response);
		return Json.toJson(result, JsonFormat.compact());
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
	

	@RequestMapping("pointGoods.json")
	public void queryV2(HttpServletRequest request, HttpServletResponse response, String currentPage,
			@RequestParam(value = "count", required = true) Integer count, String token) {
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
			List<Goods> invitationGifts = new ArrayList<Goods>();
			List<Goods> freeGifts = new ArrayList<Goods>();
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
					if (!ids.isEmpty()) {
						params.put("ids", ids);
						params.put("goods_status", 4);
						params.put("store_status", 15);
						params.put("point_status", 10);
						params.put("pointNum", 0);
						invitationGifts = this.goodsService.query(
								"select obj from Goods obj where obj.id in (:ids) and obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status and obj.point_status=:point_status and obj.pointNum>:pointNum order by obj.sequence desc",
								params, count > 0 ? 0 : -1, count);
						/*
						 * params.remove("pointNum"); params.put("pointNum", 0);
						 */
						freeGifts = this.goodsService.query(
								"select obj from Goods obj where obj.id in (:ids) and obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status and obj.point_status=:point_status and obj.pointNum=:pointNum order by obj.sequence desc",
								params, count > 0 ? 0 : -1, count);
					}
				}
			}
			map.put("rules", rules);
			map.put("link", link);
			map.put("invitationGifts", this.mGoodsViewTools.pointGoods(invitationGifts));
			map.put("freeGifts", this.mGoodsViewTools.pointGoods(freeGifts));
			map.put("user_pointNum", user == null ? 0 : user.getPointNum());

			result = new Result(0, "success", map);
		} else {
			result = new Result(1009, "没有商品");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

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
