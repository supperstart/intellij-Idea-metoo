package com.metoo.module.weixin.view.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.service.IQueryService;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.CombinPlan;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.FootPoint;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.Group;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IBuyGiftService;
import com.metoo.foundation.service.ICombinPlanService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IFootPointService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.ActivityViewTools;
import com.metoo.view.web.tools.ConsultViewTools;
import com.metoo.view.web.tools.EvaluateViewTools;
import com.metoo.view.web.tools.GoodsViewTools;

/**
 * 
 * zhuzhi
 * 
 */
@Controller
public class WeixinOutdoorsGoodsViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private GoodsViewTools goodsViewTools;
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
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IFootPointService footPointService;
	@Autowired
	private ActivityViewTools activityViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private ICombinPlanService combinplanService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IBuyGiftService buyGiftService;
	@Autowired
	private IQueryService queryService;
	@Autowired
	private IIntegralLogService integralLogService;

	/**
	 * 手机客户端商城首页商品详情请求
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 * @return
	 */
	@SuppressWarnings("unused")
	@RequestMapping("/wap/outdoors_goods_details.htm")
	public ModelAndView goods(HttpServletRequest request,
			HttpServletResponse response, String id, String puid) {
		ModelAndView mv = null;
		
		System.out.println("...OutDoors...");
		
		try {

			User user = SecurityUserHolder.getCurrentUser();// 当前用户
			if (StringUtils.isEmpty(puid)) {
				puid = request.getParameter("puid");
			}
			User parent_user = null;

			Goods obj = null;
			// 打开分享内容，建立父子级关系
			if (!StringUtils.isEmpty(puid) && !puid.equals(user.getId())) {
				parent_user = this.userService.getObjByProperty(null, "openId",
						puid);// 父级用户

				if (user.getParent() == null) {
					user = this.userService.getObjById(user.getId());
					if (parent_user != null) {
						List<User> ulists = new ArrayList<User>();
						ulists.add(user);
						parent_user.setChilds(ulists);
						this.userService.update(parent_user);

						user.setParent(parent_user);
						this.userService.update(user);

					}

					// 分享者增加10积分
					parent_user.setIntegral(parent_user.getIntegral() + 10);
					this.userService.save(parent_user);

					IntegralLog log = new IntegralLog();
					log.setAddTime(new Date());
					log.setContent("分享商品增加"
							+ this.configService.getSysConfig()
									.getMemberDayLogin() + "分");
					log.setIntegral(this.configService.getSysConfig()
							.getMemberDayLogin());
					log.setIntegral_user(user);
					log.setType("share");
					this.integralLogService.save(log);
				}

			}

			obj = this.goodsService.getObjById(CommUtil.null2Long(id));

			// System.out.println("未开启二级域名");
			// 利用cookie添加浏览过的商品
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
						goodscookie.setMaxAge(60 * 60 * 24 * 7);
						goodscookie.setDomain(CommUtil.generic_domain(request));
						response.addCookie(goodscookie);
						break;
					} else {
						goodscookie = new Cookie("goodscookie", id + ",");
						goodscookie.setMaxAge(60 * 60 * 24 * 7);
						goodscookie.setDomain(CommUtil.generic_domain(request));
						response.addCookie(goodscookie);
					}
				}
			} else {
				goodscookie = new Cookie("goodscookie", id + ",");
				goodscookie.setMaxAge(60 * 60 * 24 * 7);
				goodscookie.setDomain(CommUtil.generic_domain(request));
				response.addCookie(goodscookie);
			}
			boolean admin_view = false;// 超级管理员可以查看未审核得到商品信息
			if (user != null) {
				// 登录用户记录浏览足迹信息
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("fp_date", CommUtil.formatDate(CommUtil
						.formatShortDate(new Date())));
				params.put("fp_user_id", user.getId());
				List<FootPoint> fps = this.footPointService
						.query("select obj from FootPoint obj where obj.fp_date=:fp_date and obj.fp_user_id=:fp_user_id",
								params, -1, -1);
				if (fps.size() == 0) {
					FootPoint fp = new FootPoint();
					fp.setAddTime(new Date());
					fp.setFp_date(new Date());
					fp.setFp_user_id(user.getId());
					fp.setFp_user_name(user.getUsername());
					fp.setFp_goods_count(1);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("goods_id", obj.getId());
					map.put("goods_name", obj.getGoods_name());
					map.put("goods_sale", obj.getGoods_salenum());
					map.put("goods_time", CommUtil.formatLongDate(new Date()));
					map.put("goods_img_path",
							obj.getGoods_main_photo() != null ? CommUtil
									.getURL(request)
									+ "/"
									+ obj.getGoods_main_photo().getPath()
									+ "/"
									+ obj.getGoods_main_photo().getName()
									: CommUtil.getURL(request)
											+ "/"
											+ this.configService.getSysConfig()
													.getGoodsImage().getPath()
											+ "/"
											+ this.configService.getSysConfig()
													.getGoodsImage().getName());
					map.put("goods_price", obj.getGoods_current_price());
					map.put("goods_class_id",
							CommUtil.null2Long(obj.getGc().getId()));
					map.put("goods_class_name",
							CommUtil.null2String(obj.getGc().getClassName()));
					List<Map> list = new ArrayList<Map>();
					list.add(map);
					fp.setFp_goods_content(Json.toJson(list,
							JsonFormat.compact()));
					this.footPointService.save(fp);
				} else {
					FootPoint fp = fps.get(0);
					List<Map> list = Json.fromJson(List.class,
							fp.getFp_goods_content());
					boolean add = true;
					for (Map map : list) {// 排除重复的商品足迹
						if (CommUtil.null2Long(map.get("goods_id")).equals(
								obj.getId())) {
							add = false;
						}
					}
					if (add) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("goods_id", obj.getId());
						map.put("goods_name", obj.getGoods_name());
						map.put("goods_sale", obj.getGoods_salenum());
						map.put("goods_time",
								CommUtil.formatLongDate(new Date()));
						map.put("goods_img_path",
								obj.getGoods_main_photo() != null ? CommUtil
										.getURL(request)
										+ "/"
										+ obj.getGoods_main_photo().getPath()
										+ "/"
										+ obj.getGoods_main_photo().getName()
										: CommUtil.getURL(request)
												+ "/"
												+ this.configService
														.getSysConfig()
														.getGoodsImage()
														.getPath()
												+ "/"
												+ this.configService
														.getSysConfig()
														.getGoodsImage()
														.getName());
						map.put("goods_price", obj.getGoods_current_price());
						map.put("goods_class_id",
								CommUtil.null2Long(obj.getGc().getId()));
						map.put("goods_class_name", CommUtil.null2String(obj
								.getGc().getClassName()));
						list.add(0, map);// 后浏览的总是插入最前面
						fp.setFp_goods_count(list.size());
						fp.setFp_goods_content(Json.toJson(list,
								JsonFormat.compact()));
						this.footPointService.update(fp);
					}
				}
				if (("ADMIN").equals(user.getUserRole())) {
					admin_view = true;
				}
			}

			// 记录商品点击日志
			if (obj != null) {
				GoodsLog todayGoodsLog = this.goodsViewTools
						.getTodayGoodsLog(obj.getId());
				todayGoodsLog
						.setGoods_click(todayGoodsLog.getGoods_click() + 1);
				String click_from_str = todayGoodsLog.getGoods_click_from();
				Map<String, Integer> clickmap = (click_from_str != null && !click_from_str
						.equals("")) ? (Map<String, Integer>) Json
						.fromJson(click_from_str)
						: new HashMap<String, Integer>();
				String from = clickfrom_to_chinese(CommUtil.null2String(request
						.getParameter("from")));
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
				todayGoodsLog.setGoods_click_from(Json.toJson(clickmap,
						JsonFormat.compact()));
				this.goodsLogService.update(todayGoodsLog);
			}
			if (obj != null && obj.getGoods_status() == 0 || admin_view) {
				if (obj.getGoods_type() == 0) {// 平台自营商品
					mv = new JModelAndView("wap/outdoors_goods_details.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					obj.setGoods_click(obj.getGoods_click() + 1);
					if (this.configService.getSysConfig().isZtc_status()
							&& obj.getZtc_status() == 2) {
						obj.setZtc_click_num(obj.getZtc_click_num() + 1);
					}

					boolean groupFlag = false;
					GroupGoods gg = null;
					List<GroupGoods> ggList = obj.getGroup_goods_list();

					if (null != ggList && 0 < ggList.size()) {

						for (GroupGoods ggs : ggList) {

							if (ggs.getGg_status() == 1) {

								gg = ggs;
								groupFlag = true;
							}
						}
					}

					/* if (obj.getGroup() != null && obj.getGroup_buy() == 2) { */// 如果是团购商品，检查团购是否过期
					if (groupFlag) {
						mv = new JModelAndView("wap/goods_group_details.html",
								configService.getSysConfig(),
								this.userConfigService.getUserConfig(), 1,
								request, response);

						mv.addObject("groupObj", gg);

						// 判断当前用户是否已经参加团购（开团或参团）
						Long userId = user.getId();
						Long gid = gg.getId();
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("gid", gid);
						params.put("userId", userId);
						String qSql = "SELECT count(1) from metoo_group_joiner a, metoo_group_goods b "
								+ "where "
								+ "a.rela_group_goods_id=b.id "
								+ "and a.status=1 "
								+ "and b.gg_status=1 "
								+ "and b.id=:gid "
								+ "and a.create_time > b.beginTime "
								+ "and a.create_time < b.endTime "
								+ " and now() < date_sub(a.create_time,interval - b.gg_max_count HOUR) "
								+ " and now() > a.create_time "
								+ "and a.user_id=:userId";
						List tList = this.queryService.nativeQuery(qSql,
								params, -1, -1);
						String jGFlag = "0";
						if (null != tList && 0 < tList.size()) {

							BigInteger oa = (BigInteger) tList.get(0);
							long count = oa.longValue();
							if (count > 0) {

								jGFlag = "1";
							}
						}
						mv.addObject("jGFlag", jGFlag);
						mv.addObject("group_gid", gid);

						List list = null;
						if (jGFlag.equals("1")) { // 已经参加团购，查询当前用户参加团的信息：参加人昵称、图像、团剩余时间、剩余人数

							String nql = "SELECT t.rela_group_goods_id, t.child_group_id, t1.trueName, t1.headImgUrl, (t2.gg_group_count-t.joiner_count), t.is_group_creator, "
									+ " (UNIX_TIMESTAMP(date_sub(t.create_time,interval - t2.gg_max_count HOUR)) - UNIX_TIMESTAMP(now())), DATE_ADD(t.create_time, INTERVAL t2.gg_max_count HOUR) from metoo_group_joiner t, "
									+ " metoo_user t1, metoo_group_goods t2 where t.user_id=t1.id and t.rela_group_goods_id=t2.id "
									+ " and t.status=1 and t2.gg_status=1"
									+ " and now() < date_sub(t.create_time,interval - t2.gg_max_count HOUR) "
									+ " and now() > t.create_time "
									+ " and t.child_group_id in ( "
									+ " SELECT a.child_group_id from metoo_group_joiner a where a.user_id=:userId and a.rela_group_goods_id=:gid) "
									+ " order by t.is_group_creator desc";
							list = this.queryService.nativeQuery(nql, params,
									-1, -1);

							if (null != list && list.size() > 0) {

								mv.addObject("group_my_flag", "yes");
							}

						} else { // 未参加团购，查询可参团列表信息：参加人昵称、图像、团剩余时间、剩余人数

							String sql = "SELECT t.rela_group_goods_id, t.child_group_id, t1.trueName, t1.headImgUrl, (t2.gg_group_count-t.joiner_count), t.is_group_creator, "
									+ " (UNIX_TIMESTAMP(date_sub(t.create_time,interval - t2.gg_max_count HOUR)) - UNIX_TIMESTAMP(now())), DATE_ADD(t.create_time, INTERVAL t2.gg_max_count HOUR) from metoo_group_joiner t, metoo_user t1, metoo_group_goods t2 where"
									+ " t.user_id=t1.id and t.rela_group_goods_id=t2.id and t2.gg_status=1"
									+ " and t.user_id!=:userId"
									+ " and t.rela_group_goods_id=:gid and t.status='1'"
									+ " and is_group_creator = '1'"
									+ " and now() < date_sub(t.create_time,interval - t2.gg_max_count HOUR)"
									+ " and (t2.gg_group_count-t.joiner_count) > 0"
									+ " and now() > t.create_time order by (t2.gg_group_count-t.joiner_count), t.create_time asc";

							if (null != parent_user) {

								Map<String, Object> tmp = new HashMap<String, Object>();
								tmp.put("gid", gid);
								tmp.put("userId", parent_user.getId());
								List ssList = this.queryService.nativeQuery(
										qSql, tmp, -1, -1);
								if (null != ssList && 0 < ssList.size()) {

									BigInteger oa = (BigInteger) ssList.get(0);
									long count = oa.longValue();

									if (count > 0) {
										String nql = "SELECT t.rela_group_goods_id, t.child_group_id, t1.trueName, t1.headImgUrl, (t2.gg_group_count-t.joiner_count), t.is_group_creator, "
												+ " (UNIX_TIMESTAMP(date_sub(t.create_time,interval - t2.gg_max_count HOUR)) - UNIX_TIMESTAMP(now())), DATE_ADD(t.create_time, INTERVAL t2.gg_max_count HOUR) from metoo_group_joiner t, "
												+ " metoo_user t1, metoo_group_goods t2 where t.user_id=t1.id and t.rela_group_goods_id=t2.id "
												+ " and t.status=1 and t2.gg_status=1"
												+ " and now() < date_sub(t.create_time,interval - t2.gg_max_count HOUR) "
												+ " and now() > t.create_time "
												+ " and t.child_group_id in ( "
												+ " SELECT a.child_group_id from metoo_group_joiner a where a.user_id=:userId and a.rela_group_goods_id=:gid) "
												+ " and t.is_group_creator='1'"
												+ " order by t.is_group_creator desc";
										list = this.queryService.nativeQuery(
												nql, tmp, -1, -1);

									} else {

										list = this.queryService.nativeQuery(
												sql, params, -1, -1);
									}

								}

							} else {

								list = this.queryService.nativeQuery(sql,
										params, -1, -1);
							}

							if (null != list && list.size() > 0) {

								mv.addObject("group_child_flag", "yes");
							}

						}
						List<Map<String, Object>> nList = new ArrayList<Map<String, Object>>();
						Map<String, Object> map;
						if (null != list && list.size() > 0) {

							if (jGFlag.equals("0")) {
								int index = 0;

								for (Object o : list) {

									if (index <= 1) {

										Object[] oa = (Object[]) o;
										map = new HashMap<String, Object>();
										map.put("ggid", oa[0]);
										map.put("cgid", oa[1]);
										map.put("userName", oa[2]);
										map.put("imgUrl", oa[3]);
										map.put("joinerCount", oa[4]);
										map.put("isCreator", oa[5]);
										BigInteger rt = (BigInteger) oa[6];
										String remainTime = secToTime(rt
												.intValue());
										map.put("remainTime", remainTime);
										Date endTime = (Date) oa[7];
										Calendar endC = Calendar.getInstance();
										endC.setTime(endTime);

										int endYear = endC.get(Calendar.YEAR);
										int endMonth = endC.get(Calendar.MONTH);
										int endDay = endC
												.get(Calendar.DAY_OF_MONTH);
										int endHour = endC
												.get(Calendar.HOUR_OF_DAY);
										int endMin = endC.get(Calendar.MINUTE);
										int endSec = endC.get(Calendar.SECOND);
										map.put("endTime", (Date) oa[7]);
										map.put("endYear", endYear);
										map.put("endMonth", endMonth);
										map.put("endDay", endDay);
										map.put("endHour", endHour);
										map.put("endMin", endMin);
										map.put("endSec", endSec);
										nList.add(map);
										index++;
									}

								}
							} else {

								for (Object o : list) {

									Object[] oa = (Object[]) o;
									map = new HashMap<String, Object>();
									map.put("ggid", oa[0]);
									map.put("cgid", oa[1]);
									map.put("userName", oa[2]);
									map.put("imgUrl", oa[3]);
									map.put("joinerCount", oa[4]);
									map.put("isCreator", oa[5]);
									BigInteger rt = (BigInteger) oa[6];
									String remainTime = secToTime(rt.intValue());
									map.put("remainTime", remainTime);
									Date endTime = (Date) oa[7];
									Calendar endC = Calendar.getInstance();
									endC.setTime(endTime);

									int endYear = endC.get(Calendar.YEAR);
									int endMonth = endC.get(Calendar.MONTH);
									int endDay = endC
											.get(Calendar.DAY_OF_MONTH);
									int endHour = endC
											.get(Calendar.HOUR_OF_DAY);
									int endMin = endC.get(Calendar.MINUTE);
									int endSec = endC.get(Calendar.SECOND);
									map.put("endTime", (Date) oa[7]);
									map.put("endYear", endYear);
									map.put("endMonth", endMonth);
									map.put("endDay", endDay);
									map.put("endHour", endHour);
									map.put("endMin", endMin);
									map.put("endSec", endSec);
									nList.add(map);

								}

							}

						}

						mv.addObject("groupJoinList", nList);

						obj.setGroup_buy(2);
						obj.setGoods_current_price(gg.getGg_price());

					}
					if (obj.getCombin_status() == 1) {// 如果是组合商品，检查组合是否过期
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("endTime", new Date());
						params.put("main_goods_id", obj.getId());
						List<CombinPlan> combins = this.combinplanService
								.query("select obj from CombinPlan obj where obj.endTime<=:endTime and obj.main_goods_id=:main_goods_id",
										params, -1, -1);
						if (combins.size() > 0) {
							for (CombinPlan com : combins) {
								if (com.getCombin_type() == 0) {
									if (obj.getCombin_suit_id().equals(
											com.getId())) {
										obj.setCombin_suit_id(null);
									}
								} else {
									if (obj.getCombin_parts_id().equals(
											com.getId())) {
										obj.setCombin_parts_id(null);
									}
								}
								obj.setCombin_status(0);
							}
						}
					}
					if (obj.getOrder_enough_give_status() == 1) {
						BuyGift bg = this.buyGiftService.getObjById(obj
								.getBuyGift_id());
						if (bg != null && bg.getEndTime().before(new Date())) {
							bg.setGift_status(20);
							List<Map> maps = Json.fromJson(List.class,
									bg.getGift_info());
							maps.addAll(Json.fromJson(List.class,
									bg.getGoods_info()));
							for (Map map : maps) {
								Goods goods = this.goodsService
										.getObjById(CommUtil.null2Long(map
												.get("goods_id")));
								if (goods != null) {
									goods.setOrder_enough_give_status(0);
									goods.setOrder_enough_if_give(0);
									goods.setBuyGift_id(null);
									this.goodsService.update(goods);
								}
							}
							this.buyGiftService.update(bg);
						}
						if (bg != null && bg.getGift_status() == 10) {
							mv.addObject("isGift", true);
						}
					}
					if (obj.getOrder_enough_if_give() == 1) {
						BuyGift bg = this.buyGiftService.getObjById(obj
								.getBuyGift_id());
						if (bg != null && bg.getGift_status() == 10) {
							mv.addObject("isGive", true);
						}
					}
					this.goodsService.update(obj);

					if (obj.getEnough_reduce() == 1) {// 如果是满就减商品，未到活动时间不作处理，活动时间显示满减信息，已过期则删除满减信息
						EnoughReduce er = this.enoughReduceService
								.getObjById(CommUtil.null2Long(obj
										.getOrder_enough_reduce_id()));
						if (er.getErstatus() == 10
								&& er.getErbegin_time().before(new Date())
								&& er.getErend_time().after(new Date())) {// 正在进行
							mv.addObject("enoughreduce", er);
						}
					}

					mv.addObject("obj", obj);
					mv.addObject("goodsViewTools", goodsViewTools);
					mv.addObject("transportTools", transportTools);

					mv.addObject("parent_user", parent_user);
					// 计算当期访问用户的IP地址，并计算对应的运费信息
					String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
					if (CommUtil.isIp(current_ip)) {
						IPSeeker ip = new IPSeeker(null, null);
						String current_city = ip.getIPLocation(current_ip)
								.getCountry();
						mv.addObject("current_city", current_city);
					} else {
						mv.addObject("current_city", "未知地区");
					}
					// 查询运费地区
					List<Area> areas = this.areaService
							.query("select obj from Area obj where obj.parent.id is null order by obj.sequence asc",
									null, -1, -1);
					mv.addObject("areas", areas);
					mv.addObject("userTools", userTools);
					mv.addObject("goodsViewTools", goodsViewTools);
					mv.addObject("activityViewTools", activityViewTools);
					if (SecurityUserHolder.getCurrentUser() != null) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("gid", obj.getId());
						map.put("uid", SecurityUserHolder.getCurrentUser()
								.getId());
						List<Favorite> favorites = this.favoriteService
								.query("select obj from Favorite obj where obj.goods_id=:gid and obj.user_id=:uid",
										map, -1, -1);
						if (favorites.size() > 0) {
							mv.addObject("mark", 1);
						}
					}
					String type = CommUtil.null2String(request
							.getAttribute("type"));
					String cart_session_id = "";
					Cookie[] cookies1 = request.getCookies();
					if (cookies1 != null) {
						for (Cookie cookie : cookies1) {
							if (cookie.getName().equals("cart_session_id")) {
								cart_session_id = CommUtil.null2String(cookie
										.getValue());
							}
						}
					}
					if (cart_session_id.equals("")) {
						cart_session_id = UUID.randomUUID().toString();
						Cookie cookie = new Cookie("cart_session_id",
								cart_session_id);
						cookie.setDomain(CommUtil.generic_domain(request));
					}
					List<GoodsCart> carts_list = new ArrayList<GoodsCart>();// 用户整体购物车
					List<GoodsCart> carts_cookie = new ArrayList<GoodsCart>();// 未提交的用户cookie购物车
					List<GoodsCart> carts_user = new ArrayList<GoodsCart>();// 未提交的用户user购物车
					Map<String, Object> cart_map = new HashMap<String, Object>();
					if (user != null) {
						// user = userService.getObjById(user.getId());
						if (!cart_session_id.equals("")) {
							cart_map.clear();
							cart_map.put("cart_session_id", cart_session_id);
							cart_map.put("cart_status", 0);
							carts_cookie = this.goodsCartService
									.query("select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status ",
											cart_map, -1, -1);
							// 如果用户拥有自己的店铺，删除carts_cookie购物车中自己店铺中的商品信息

							cart_map.clear();
							cart_map.put("user_id", user.getId());
							cart_map.put("cart_status", 0);
							carts_user = this.goodsCartService
									.query("select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status ",
											cart_map, -1, -1);
						} else {
							cart_map.clear();
							cart_map.put("user_id", user.getId());
							cart_map.put("cart_status", 0);
							carts_user = this.goodsCartService
									.query("select obj from GoodsCart obj where obj.user.id=:user_id and obj.cart_status=:cart_status ",
											cart_map, -1, -1);
						}
					} else {
						if (!cart_session_id.equals("")) {
							cart_map.clear();
							cart_map.put("cart_session_id", cart_session_id);
							cart_map.put("cart_status", 0);
							carts_cookie = this.goodsCartService
									.query("select obj from GoodsCart obj where obj.cart_session_id=:cart_session_id and obj.cart_status=:cart_status ",
											cart_map, -1, -1);
						}
					}
					// 将cookie购物车与user购物车合并，并且去重
					if (user != null) {
						for (GoodsCart cookie : carts_cookie) {
							boolean add = true;
							for (GoodsCart gc2 : carts_user) {
								if (cookie.getGoods().getId()
										.equals(gc2.getGoods().getId())) {
									if (cookie.getSpec_info().equals(
											gc2.getSpec_info())) {
										add = false;
										this.goodsCartService.delete(cookie
												.getId());
									}
								}
							}
							if (add) {// 将cookie去重并添加到cart_list中
								cookie.setCart_session_id(null);
								cookie.setUser(user);
								this.goodsCartService.update(cookie);
								carts_list.add(cookie);
							}
						}
					} else {
						for (GoodsCart gc : carts_cookie) {// 将carts_cookie添加到cart_list中
							carts_list.add(gc);
						}
					}
					for (GoodsCart gc : carts_user) {// 将carts_user添加到cart_list中
						carts_list.add(gc);
					}
					// 组合套装处理，只显示套装主购物车,套装内其他购物车不显示
					List<GoodsCart> combin_carts_list = new ArrayList<GoodsCart>();
					for (GoodsCart gc : carts_list) {
						if (gc.getCart_type() != null
								&& gc.getCart_type().equals("combin")) {
							if (gc.getCombin_main() != 1) {
								combin_carts_list.add(gc);
							}
						}
					}
					if (combin_carts_list.size() > 0) {
						carts_list.removeAll(combin_carts_list);
					}
					mv.addObject("carts", carts_list);
				} else {
					mv = new JModelAndView("wap/outdoors_goods_details.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					obj.setGoods_click(obj.getGoods_click() + 1);
					if (this.configService.getSysConfig().isZtc_status()
							&& obj.getZtc_status() == 2) {
						obj.setZtc_click_num(obj.getZtc_click_num() + 1);
					}
					if (obj.getGroup() != null && obj.getGroup_buy() == 2) {// 如果是团购商品，检查团购是否过期
						Group group = obj.getGroup();
						if (group.getEndTime().before(new Date())) {
							obj.setGroup(null);
							obj.setGroup_buy(0);
							obj.setGoods_current_price(obj.getStore_price());
						}
					}
					if (obj.getCombin_status() == 1) {// 如果是组合商品，检查组合是否过期
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("endTime", new Date());
						params.put("main_goods_id", obj.getId());
						List<CombinPlan> combins = this.combinplanService
								.query("select obj from CombinPlan obj where obj.endTime<=:endTime and obj.main_goods_id=:main_goods_id",
										params, -1, -1);
						if (combins.size() > 0) {
							for (CombinPlan com : combins) {
								if (com.getCombin_type() == 0) {
									if (obj.getCombin_suit_id().equals(
											com.getId())) {
										obj.setCombin_suit_id(null);
									}
								} else {
									if (obj.getCombin_parts_id().equals(
											com.getId())) {
										obj.setCombin_parts_id(null);
									}
								}
								obj.setCombin_status(0);
							}
						}
					}
					if (obj.getOrder_enough_give_status() == 1) {
						BuyGift bg = this.buyGiftService.getObjById(obj
								.getBuyGift_id());
						if (bg != null && bg.getEndTime().before(new Date())) {
							bg.setGift_status(20);
							List<Map> maps = Json.fromJson(List.class,
									bg.getGift_info());
							maps.addAll(Json.fromJson(List.class,
									bg.getGoods_info()));
							for (Map map : maps) {
								Goods goods = this.goodsService
										.getObjById(CommUtil.null2Long(map
												.get("goods_id")));
								if (goods != null) {
									goods.setOrder_enough_give_status(0);
									goods.setOrder_enough_if_give(0);
									goods.setBuyGift_id(null);
									this.goodsService.update(goods);
								}
							}
							this.buyGiftService.update(bg);
						}
						if (bg != null && bg.getGift_status() == 10) {
							mv.addObject("isGift", true);
						}
					}
					if (obj.getOrder_enough_if_give() == 1) {
						BuyGift bg = this.buyGiftService.getObjById(obj
								.getBuyGift_id());
						if (bg != null && bg.getGift_status() == 10) {
							mv.addObject("isGive", true);
						}
					}
					this.goodsService.update(obj);

					if (obj.getEnough_reduce() == 1) {// 如果是满就减商品，未到活动时间不作处理，活动时间显示满减信息
						EnoughReduce er = this.enoughReduceService
								.getObjById(CommUtil.null2Long(obj
										.getOrder_enough_reduce_id()));
						if (er.getErstatus() == 10
								&& er.getErbegin_time().before(new Date())
								&& er.getErend_time().after(new Date())) {// 正在进行
							mv.addObject("enoughreduce", er);
						}
					}
					// if (obj.getGoods_store().getStore_status() == 15) {//
					// 店铺为开通状态
					mv.addObject("obj", obj);
					mv.addObject("store", obj.getGoods_store());
					mv.addObject("goodsViewTools", goodsViewTools);
					mv.addObject("transportTools", transportTools);

					// 计算当期访问用户的IP地址，并计算对应的运费信息
					String current_ip = CommUtil.getIpAddr(request);// 获得本机IP
					if (CommUtil.isIp(current_ip)) {
						IPSeeker ip = new IPSeeker(null, null);
						String current_city = ip.getIPLocation(current_ip)
								.getCountry();
						mv.addObject("current_city", current_city);
					} else {
						mv.addObject("current_city", "未知地区");
					}
					// 查询运费地区
					List<Area> areas = this.areaService
							.query("select obj from Area obj where obj.parent.id is null order by obj.sequence asc",
									null, -1, -1);
					mv.addObject("areas", areas);
					this.generic_evaluate(obj.getGoods_store(), mv);

					mv.addObject("userTools", userTools);
					mv.addObject("goodsViewTools", goodsViewTools);
					mv.addObject("activityViewTools", activityViewTools);

				}
				// 查询评论次数
				int evaluates_count = this.evaluateViewTools.queryByEva(
						obj.getId().toString(), "all").size();
				mv.addObject("evaluates_count", evaluates_count);
				int consul_count = this.consultViewTools.queryByType(null,
						obj.getId().toString()).size();
				mv.addObject("consul_count", consul_count);

			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "参数错误，商品查看失败");
				mv.addObject("url", CommUtil.getURL(request) + "/index.htm");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return mv;
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
		GoodsClass gc = this.goodsClassService
				.getObjById(store.getGc_main_id());
		if (store != null && gc != null && store.getPoint() != null) {
			float description_evaluate = CommUtil.null2Float(gc
					.getDescription_evaluate());
			float service_evaluate = CommUtil.null2Float(gc
					.getService_evaluate());
			float ship_evaluate = CommUtil.null2Float(gc.getShip_evaluate());

			float store_description_evaluate = CommUtil.null2Float(store
					.getPoint().getDescription_evaluate());
			float store_service_evaluate = CommUtil.null2Float(store.getPoint()
					.getService_evaluate());
			float store_ship_evaluate = CommUtil.null2Float(store.getPoint()
					.getShip_evaluate());
			// 计算和同行比较结果
			description_result = CommUtil.div(store_description_evaluate
					- description_evaluate, description_evaluate);
			service_result = CommUtil.div(store_service_evaluate
					- service_evaluate, service_evaluate);
			ship_result = CommUtil.div(store_ship_evaluate - ship_evaluate,
					ship_evaluate);
		}
		if (description_result > 0) {
			mv.addObject("description_css", "value_strong");
			mv.addObject(
					"description_result",
					CommUtil.null2String(CommUtil.mul(description_result, 100) > 100 ? 100
							: CommUtil.mul(description_result, 100))
							+ "%");
		}
		if (description_result == 0) {
			mv.addObject("description_css", "value_normal");
			mv.addObject("description_result", "-----");
		}
		if (description_result < 0) {
			mv.addObject("description_css", "value_light");
			mv.addObject(
					"description_result",
					CommUtil.null2String(CommUtil.mul(-description_result, 100))
							+ "%");
		}
		if (service_result > 0) {
			mv.addObject("service_css", "value_strong");
			mv.addObject("service_result",
					CommUtil.null2String(CommUtil.mul(service_result, 100))
							+ "%");
		}
		if (service_result == 0) {
			mv.addObject("service_css", "value_normal");
			mv.addObject("service_result", "-----");
		}
		if (service_result < 0) {
			mv.addObject("service_css", "value_light");
			mv.addObject("service_result",
					CommUtil.null2String(CommUtil.mul(-service_result, 100))
							+ "%");
		}
		if (ship_result > 0) {
			mv.addObject("ship_css", "value_strong");
			mv.addObject("ship_result",
					CommUtil.null2String(CommUtil.mul(ship_result, 100)) + "%");
		}
		if (ship_result == 0) {
			mv.addObject("ship_css", "value_normal");
			mv.addObject("ship_result", "-----");
		}
		if (ship_result < 0) {
			mv.addObject("ship_css", "value_light");
			mv.addObject("ship_result",
					CommUtil.null2String(CommUtil.mul(-ship_result, 100)) + "%");
		}
	}

	private String secToTime(int time) {
		String timeStr = "";
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99:59:59";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
						+ unitFormat(second);
			}
		}
		return timeStr;
	}

	private String unitFormat(int i) {
		String retStr = "";
		if (i >= 0 && i < 10)
			retStr = "0" + i;
		else
			retStr = "" + i;
		return retStr;
	}

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
}
