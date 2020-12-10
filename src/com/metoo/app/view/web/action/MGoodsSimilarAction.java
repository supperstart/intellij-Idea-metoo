package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsSimilarTools;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserRecommend;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserRecommendService;
import com.metoo.foundation.service.IUserService;

/**
 * <p>
 * 	Title:MGoodsSimilarAction.java
 * </p>
 * 
 * <p>
 * 	Description:智能推荐控制器，用来显示用户曾经浏览过商品的类似商品；商品详情页，首页查询商品关联商品，每页20，不足由metoo_goods_similar 商品补充，该表数据由爬虫分析数据填充
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c)
 * </p>
 * 
 * 
 * @author Administrator
 * 
 * @date 2014-4-28
 *
 */

@Controller
@RequestMapping("/app/v1/")
public class MGoodsSimilarAction {

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private DatabaseTools databaseTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IUserRecommendService userRecommendService;
	@Autowired
	private MGoodsSimilarTools goodsSimilarTools;

	public void user_recommend(HttpServletRequest request, HttpServletResponse response, String token, String orderby,
			String orderType, String currentPage, String language) throws SQLException {
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		Result result = null;
		String mainIds = "";
		String amount = "20";// similar PageSize商品总数
		Map<String, Object> similar_map = new HashMap<String, Object>();
		User user = this.userService.getObjByProperty(null, "app_login_token", token);
		if (token != null && !token.equals("") && user != null) {
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user_id", user.getId());
			List<UserRecommend> urs = this.userRecommendService.query(
					"select obj from UserRecommend obj where obj.user_id=:user_id order by goods_click desc, addTime desc",
					params, 6, 200);// 长期行为-召回候选产品
			String[] cooke_ids = CommUtil.creatArray(5);
			String[] zero = CommUtil.creatArray(4);// 实时行为召回
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("goodscookie")) {
						cooke_ids = cookie.getValue().split(",", 5);
					}
				}
				for (int i = 0; i < cooke_ids.length - 1; i++) {
					zero[i] = cooke_ids[i];
				}
				String[] first = CommUtil.creatArray(3);
				String[] second = CommUtil.creatArray(3);
				String[] third = CommUtil.creatArray(2);
				int secondLength = 0;
				String[] secondAll = null;
				if (!urs.isEmpty() && urs.size() > 6) {
					double secondRate = CommUtil.div(urs.size() - first.length, 3);// 第二段取前1/3
					secondLength = new Double(secondRate).intValue();
					secondAll = new String[secondLength];
				}
				String[] thirdAll = null;
				if (!urs.isEmpty() && urs.size() > 6) {
					thirdAll = new String[urs.size() - first.length - secondLength];
				}

				Random random = new Random();
				if (urs.size() > 0) {
					if (urs.size() > 0 && urs.size() <= 3) {
						for (UserRecommend ur : urs) {
							mainIds += ur.getGoods_id() + ",";
						}
					} else if (3 < urs.size() && urs.size() < 50) {
						for (int i = 0; i < urs.size(); i++) {
							if (i < 3) {
								first[i] = CommUtil.null2String(urs.get(i).getGoods_id());
							} else if (secondAll != null && i < secondLength + 3) {
								secondAll[i - first.length] = CommUtil.null2String(urs.get(i).getGoods_id());
							} else if (thirdAll != null && i < secondLength + 3 && i < urs.size()) {
								thirdAll[i - first.length - secondAll.length] = CommUtil
										.null2String(urs.get(i).getGoods_id());
							}
						}
					} else if (50 < urs.size() && urs.size() < 200) {
						for (int i = 0; i < urs.size(); i++) {
							if (i < 3) {
								first[i] = CommUtil.null2String(urs.get(i).getGoods_id());
							} else if (secondAll != null && i < secondLength + 3) {
								secondAll[i - first.length] = CommUtil.null2String(urs.get(i).getGoods_id());
							} else if (thirdAll != null && i < urs.size()) {
								thirdAll[i - first.length - secondAll.length] = CommUtil
										.null2String(urs.get(i).getGoods_id());
							}
						}
					}
					String[] marge = new String[zero.length + first.length + second.length + third.length];
					if (first.length > 0 && zero.length > 0) {
						marge = (String[]) ArrayUtils.addAll(zero, first);
					}
					if (secondAll != null) {
						for (int i = 0; i < second.length; i++) {
							second[i] = secondAll[random.nextInt(secondAll.length)];
						}
						marge = (String[]) ArrayUtils.addAll(marge, second);
					}
					if (thirdAll != null) {
						for (int i = 0; i < third.length; i++) {
							third[i] = thirdAll[random.nextInt(thirdAll.length)];
						}
						marge = (String[]) ArrayUtils.addAll(marge, third);
					}
					for (String str : marge) {
						if (str != null) {
							mainIds += str + ",";
						}
					}
				} else {
					Cookie[] goods_cookies = request.getCookies();
					// String cookie_goods_id = "";
					for (Cookie cookie : goods_cookies) {
						if (cookie.getName().equals("goodscookie")) {
							mainIds = cookie.getValue();
						}
					}
				}
			} else {
				this.goodsSimilarTools.recommendGoods(mv, currentPage, 0, similar_map, language);
			}
		} else {
			Cookie[] goods_cookies = request.getCookies();
			for (Cookie cookie : goods_cookies) {
				if (cookie.getName().equals("goodscookie")) {
					mainIds = cookie.getValue();
				}
			}
		}
		Set<Map> goodsSet = new HashSet<Map>();
		if (!mainIds.equals("")) {
			List<String> keys = new ArrayList<String>();// 推荐商品个数（10）
			List<String> values = new ArrayList<String>();// 推荐商品主id为十个，value为对应的推荐商品个数
			values.add("2");
			values.add("2");
			values.add("2");
			values.add("2");
			values.add("2");
			values.add("1");
			values.add("2");
			values.add("2");
			values.add("1");
			values.add("2");
			values.add("1");
			values.add("1");
			String[] goods_ids = mainIds.split(",");
			for (String id : goods_ids) {
				keys.add(id);
			}
			Map<String, Integer> countMap = new HashMap<String, Integer>();
			String count_ids = StringUtils.substringBeforeLast(mainIds, ",");
			if (!count_ids.equals("")) {
				String count_sql = "select goods_main_id, count(*) as count from metoo_goods_similar where goods_main_id in ("
						+ count_ids + ") group by goods_main_id"; // 查询每个推荐商品主id对应的子商品总数
				ResultSet res = this.databaseTools.selectIn(count_sql);// 获取结果集
				Long id;
				int number;
				List countList = new ArrayList();
				while (res.next()) {
					id = res.getLong("goods_main_id");
					number = res.getInt("count");
					countMap.put(id.toString(), number);
				}
				String appendId = "";// 随机补充id
				goodsSet = this.goodsSimilarTools.key_value(currentPage, keys, values, countMap, appendId);// 获取推荐商品
				int remaining = CommUtil.null2Int(amount) - goodsSet.size();
				if (remaining > 0) {
					Set<Map> set = this.goodsSimilarTools.recommendGoods(mv, currentPage, remaining, similar_map,
							language);
					goodsSet.addAll(set);
				}
				similar_map.put("result", goodsSet);
			}
		} else {
			int remaining = CommUtil.null2Int(amount) - goodsSet.size();
			if (remaining > 0) {
				Set<Map> set = this.goodsSimilarTools.recommendGoods(mv, currentPage, remaining, similar_map, language);
				goodsSet.addAll(set);
			}
			similar_map.put("result", goodsSet);
		}
		result = new Result(0, "Successfully", similar_map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("youLikeGoods.json")
	public void recommen1(HttpServletRequest request, HttpServletResponse response, String token, String orderby,
			String orderType, String currentPage, String language, String goods_id) throws SQLException {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Result result = null;
		int amount = 20;// similar PageSize商品总数
		Set<Map> goodsSet = new LinkedHashSet<Map>();
		Goods obj = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
		if (currentPage.equals("")) {
			currentPage = "1";
		}
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		if (obj != null && currentPage.equals("1") 
								&& obj.getGoods_correlation() != null) {
			String correlationId = obj.getGoods_correlation();
			String[] goods_ids = correlationId.split(",");
			Set<Long> setId = new HashSet<Long>();
			for (String str : goods_ids) {
				setId.add(Long.parseLong(str));
			}
			Map params = new HashMap();
			params.put("goods_ids", setId);
			params.put("goods_status", 0);
			List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.id in (:goods_ids) and obj.goods_status=:goods_status",
					params, -1, -1);
			if (goodsList.size() > 0) {
				for (Goods goods : goodsList) {
					Map map = new HashMap();
					map.put("goods_id", goods.getId());
					map.put("goods_name", goods.getGoods_name());
					if ("1".equals(language)) {
						map.put("goods_name", goods.getKsa_goods_name() != null && !"".equals(goods.getKsa_goods_name())
								? "^" + goods.getKsa_goods_name() : goods.getGoods_name());
					}
					map.put("goods_img", goods.getGoods_main_photo() != null ? imageWebServer + "/"
							+ goods.getGoods_main_photo().getPath() + "/" + goods.getGoods_main_photo().getName()
							+ "_middle." + goods.getGoods_main_photo().getExt() : imageWebServer);
					map.put("goods_type", goods.getGoods_type());
					map.put("goods_well_evaluate", goods.getWell_evaluate());
					map.put("goods_price", goods.getGoods_price());
					map.put("goods_current_price", goods.getGoods_current_price());
					map.put("goods_collect", goods.getGoods_collect());
					map.put("goods_status", goods.getGoods_status());
					map.put("store_status", goods.getGoods_store().getStore_status());
					map.put("goods_discount_rate", goods.getGoods_discount_rate());
					goodsSet.add(map);
				}
				Double num = CommUtil.subtract(amount, goodsSet.size());
				amount = num.intValue();
			}
		}
		String mainIds = "";
		Map similar_map = new HashMap();
		User user = this.userService.getObjByProperty(null, "app_login_token", token);
		if(amount > 0){
			if (token != null && !token.equals("") && user != null) {
				Map params = new HashMap();
				params.put("user_id", user.getId());
				List<UserRecommend> urs = this.userRecommendService.query(
						"select obj from UserRecommend obj where obj.user_id=:user_id order by goods_click desc, addTime desc",
						params, 6, 200);// 长期行为-召回候选产品
				String[] cooke_ids = CommUtil.creatArray(5);
				String[] zero = CommUtil.creatArray(4);// 实时行为召回
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals("goodscookie")) {
							cooke_ids = cookie.getValue().split(",", 5);
						}
					}
					for (int i = 0; i < cooke_ids.length - 1; i++) {
						zero[i] = cooke_ids[i];
					}
					String[] first = CommUtil.creatArray(3);
					String[] second = CommUtil.creatArray(3);
					String[] third = CommUtil.creatArray(2);
					int secondLength = 0;
					String[] secondAll = null;
					if (!urs.isEmpty() && urs.size() > 6) {
						double secondRate = CommUtil.div(urs.size() - first.length, 3);// 第二段取前1/3
						secondLength = new Double(secondRate).intValue();
						secondAll = new String[secondLength];
					}
					String[] thirdAll = null;
					if (!urs.isEmpty() && urs.size() > 6) {
						thirdAll = new String[urs.size() - first.length - secondLength];
					}

					Random random = new Random();
					if (urs.size() > 0) {
						if (urs.size() > 0 && urs.size() <= 3) {
							for (UserRecommend ur : urs) {
								mainIds += ur.getGoods_id() + ",";
							}
						} else if (3 < urs.size() && urs.size() < 50) {
							for (int i = 0; i < urs.size(); i++) {
								if (i < 3) {
									first[i] = CommUtil.null2String(urs.get(i).getGoods_id());
								} else if (secondAll != null && i < secondLength + 3) {
									secondAll[i - first.length] = CommUtil.null2String(urs.get(i).getGoods_id());
								} else if (thirdAll != null && i < secondLength + 3 && i < urs.size()) {
									thirdAll[i - first.length - secondAll.length] = CommUtil
											.null2String(urs.get(i).getGoods_id());
								}
							}
						} else if (50 < urs.size() && urs.size() < 200) {
							for (int i = 0; i < urs.size(); i++) {
								if (i < 3) {
									first[i] = CommUtil.null2String(urs.get(i).getGoods_id());
								} else if (secondAll != null && i < secondLength + 3) {
									secondAll[i - first.length] = CommUtil.null2String(urs.get(i).getGoods_id());
								} else if (thirdAll != null && i < urs.size()) {
									thirdAll[i - first.length - secondAll.length] = CommUtil
											.null2String(urs.get(i).getGoods_id());
								}
							}
						}
						String[] marge = new String[zero.length + first.length + second.length + third.length];
						if (first.length > 0 && zero.length > 0) {
							marge = (String[]) ArrayUtils.addAll(zero, first);
						}
						if (secondAll != null) {
							for (int i = 0; i < second.length; i++) {
								second[i] = secondAll[random.nextInt(secondAll.length)];
							}
							marge = (String[]) ArrayUtils.addAll(marge, second);
						}
						if (thirdAll != null) {
							for (int i = 0; i < third.length; i++) {
								third[i] = thirdAll[random.nextInt(thirdAll.length)];
							}
							marge = (String[]) ArrayUtils.addAll(marge, third);
						}
						for (String str : marge) {
							if (str != null) {
								mainIds += str + ",";
							}
						}
					} else {
						Cookie[] goods_cookies = request.getCookies();
						// String cookie_goods_id = "";
						for (Cookie cookie : goods_cookies) {
							if (cookie.getName().equals("goodscookie")) {
								mainIds = cookie.getValue();
							}
						}
					}
				} else {
					this.goodsSimilarTools.recommendGoods(mv, currentPage, 0, similar_map, language);
				}
				if (!mainIds.equals("")) {
					List<String> keys = new ArrayList<String>();// 推荐商品个数（10）
					List<String> values = new ArrayList<String>();// 推荐商品主id为十个，value为对应的推荐商品个数
					values.add("2");
					values.add("2");
					values.add("2");
					values.add("2");
					values.add("2");
					values.add("1");
					values.add("2");
					values.add("2");
					values.add("1");
					values.add("2");
					values.add("1");
					values.add("1");
					String[] goods_ids = mainIds.split(",");
					for (String id : goods_ids) {
						keys.add(id);
					}
					Map<String, Integer> countMap = new HashMap<String, Integer>();
					String count_ids = StringUtils.substringBeforeLast(mainIds, ",");
					if (!count_ids.equals("")) {
						String count_sql = "select goods_main_id, count(*) as count from metoo_goods_similar where goods_main_id in ("
								+ count_ids + ") group by goods_main_id"; // 查询每个推荐商品主id对应的子商品总数
						ResultSet res = this.databaseTools.selectIn(count_sql);// 获取结果集
						Long id;
						int number;
						List countList = new ArrayList();
						while (res.next()) {
							id = res.getLong("goods_main_id");
							number = res.getInt("count");
							countMap.put(id.toString(), number);
						}
						String appendId = "";// 随机补充id
						goodsSet = this.goodsSimilarTools.key_value(currentPage, keys, values, countMap, appendId);// 获取推荐商品
						int remaining = amount - goodsSet.size();
						if (remaining > 0) {
							Set<Map> set = this.goodsSimilarTools.recommendGoods(mv, currentPage, remaining, similar_map,
									language);
							goodsSet.addAll(set);
						}
						similar_map.put("result", goodsSet);
					}
				} else {
					int remaining = amount - goodsSet.size();
					if (remaining > 0) {
						Set<Map> set = this.goodsSimilarTools.recommendGoods(mv, currentPage, remaining, similar_map,
								language);
						goodsSet.addAll(set);
					}
					similar_map.put("result", goodsSet);
				}
			} else {
				ModelAndView mv1 = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
						request, response);
				GoodsQueryObject qo = new GoodsQueryObject(currentPage, mv1, "addTime", "desc");
				qo.addQuery("obj.store_recommend", new SysMap("goods_store_recommend", CommUtil.null2Boolean(true)), "=");
				qo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
				qo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
				qo.setPageSize(amount);
				IPageList pList = this.goodsService.list(qo);
				List<Goods> goods_list = pList.getResult();
				goodsSet.addAll(this.goodsSimilarTools.similar_query(goods_list, language));
				similar_map.put("result", goodsSet);
			}
		}else{
			similar_map.put("result", goodsSet);
		}
		result = new Result(0, "Successfully", similar_map);
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

}