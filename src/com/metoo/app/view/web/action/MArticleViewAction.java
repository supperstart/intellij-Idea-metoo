package com.metoo.app.view.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.ip.IPSeeker;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Article;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.GoodsFloor;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GoodsTypeProperty;
import com.metoo.foundation.domain.query.ArticleQueryObject;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IArticleService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IGoodsTypePropertyService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.view.web.tools.GoodsFloorViewTools;

@Controller
@RequestMapping("/app/")
public class MArticleViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IArticleService articleService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsTypePropertyService goodsTypePropertyService;
	@Autowired
	private IGoodsSpecPropertyService goodsSpecPropertyService;
	@Autowired
	private GoodsFloorViewTools gf_tools;

	/**
	 * wap端Discovery 文章列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("v1/article_info.json")
	public void footer_api(HttpServletRequest request, HttpServletResponse response, String currentPage) {
		Result result;
		Map<String, Object> map = new HashMap<String, Object>();
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		/*
		 * 查询指定数量的文章 Map params = new HashMap(); Set marks = new TreeSet();//
		 * marks.add("Discovery"); params.put("marks", marks); List<Article>
		 * articles = this.articleService .query(
		 * "select obj from Article obj where obj.mark=:marks order by obj.addTime desc"
		 * , params, -1, -1);
		 */
		ArticleQueryObject aqo = new ArticleQueryObject(currentPage, mv, "addTime", "desc");
		aqo.addQuery("obj.mark", new SysMap("mark", "Discovery"), "=");
		aqo.setPageSize(10);
		aqo.setCurrentPage(CommUtil.null2Int(currentPage));
		IPageList pList = this.articleService.list(aqo);
		List<Article> articles = pList.getResult();
		List<Map<String, Object>> articleList = new ArrayList<Map<String, Object>>();
		for (Article article : articles) {
			Map<String, Object> articleMap = new HashMap<String, Object>();
			articleMap.put("url", article.getUrl());
			articleMap.put("id", article.getId());
			articleMap.put("title", article.getTitle());
			articleMap.put("articleid", article.getArticle_id());
			articleMap
					.put("article_acc",
							article.getArticle_acc() == null ? ""
									: this.configService.getSysConfig().getImageWebServer() + "/"
											+ article.getArticle_acc().getPath() + "/"
											+ article.getArticle_acc().getName());
			articleMap.put("articlegcinfo", wap_article(article.getArticle_id()));
			articleList.add(articleMap);
		}
		map.put("articlelist", articleList);
		map.put("article_Pages", pList.getPages());
		result = new Result(0, "Successfully", map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 文章关联的商品分类
	 * 
	 * @param id
	 * @return
	 */
//	@RequestMapping("/wap_article.json")
	@RequestMapping("v1/wap_article.json")
	public Map wap_article(Long id) {
		Map map = new HashMap();
		if (id != null && !id.equals("")) {
			GoodsClass gc = goodsClassService.getObjById(id);
			map.put("gcid", gc.getId());
			map.put("gcname", gc.getClassName());
		}
		return map;
	}

	/**
	 * APP-Discovery 文章详情
	 * 
	 * @param request
	 * @param response
	 * @param param
	 */
//	@RequestMapping("/discovery_info.json")
	@RequestMapping("v1/discovery_info.json")
	public void article(HttpServletRequest request, HttpServletResponse response, String param, String language) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		Article obj = null;
		Long id = CommUtil.null2Long(param);
		String mark = "";
		if (id == -1l) {
			mark = param;
		}
		if (id != -1l) {
			obj = this.articleService.getObjById(id);
			if (obj != null) {
				map.put("articlecontent", obj.getContent());
				map.put("id", obj.getId());
				map.put("title", obj.getTitle());
				map.put("articleid", obj.getArticle_id());
				map.put("article_acc",
						obj.getArticle_acc() == null ? ""
								: this.configService.getSysConfig().getImageWebServer() + "/"
										+ obj.getArticle_acc().getPath() + "/" + obj.getArticle_acc().getName());
				GoodsFloor goodsFloor = obj.getGoodsFloor();
				List<List> list = new ArrayList<List>();
				if (goodsFloor != null && goodsFloor.getChilds().size() > 0) {
					for (GoodsFloor gf : goodsFloor.getChilds()) {
						if (gf.isGf_display()) {
							List goods_list = this.gf_tools.generic_goods2(gf.getGf_gc_goods(), language);
							list.add(goods_list);
						}
					}
				}
				map.put("article_goods", list);
				result = new Result(0, "Successfully", map);
			} else {
				result = new Result(1, "The article does not exist");
			}
		}
		if (!mark.equals("")) {
			obj = this.articleService.getObjByProperty(null, "mark", mark);
			if (obj != null) {
				obj = this.articleService.getObjById(id);
				map.put("articlecontent", obj.getContent());
				map.put("articleid", obj.getId());
				map.put("class_id", obj.getArticle_id());
				GoodsFloor goodsFloor = obj.getGoodsFloor();
				List<List> list = new ArrayList<List>();
				if (goodsFloor != null && goodsFloor.getChilds().size() > 0) {
					for (GoodsFloor gf : goodsFloor.getChilds()) {
						if (gf.isGf_display()) {
							List goods_list = this.gf_tools.generic_goods2(gf.getGf_gc_goods(), language);
							list.add(goods_list);
						}
					}
				}
				map.put("article_goods", list);
				result = new Result(0, "Successfully", map);
			} else {
				result = new Result(1, "The article does not exist");
			}
		}

		/*
		 * * User user = SecurityUserHolder.getCurrentUser(); if (user != null)
		 * { if (user.getUserRole().equals("BUYER")) { if (obj != null &&
		 * obj.getType().equals("user")) { Map params = new HashMap();
		 * //排除不展示的页面
		 * 
		 * params.clear(); params.put("type", "user"); List<Article> articles =
		 * this.articleService .query(
		 * "select obj from Article obj where obj.type=:type order by obj.addTime desc"
		 * , params, 0, 6); List<Map> articlelist = new ArrayList<Map>();
		 * for(Article article:articles){ Map articlesmap = new HashMap();
		 * articlesmap.put("", value)
		 * 
		 * } } else { mv = new JModelAndView("error.html",
		 * configService.getSysConfig(), this.userConfigService.getUserConfig(),
		 * 1, request, response); mv.addObject("op_title",
		 * "Seller announcement, you can't view it！"); } } if
		 * (user.getUserRole().equals("SELLER") ||
		 * user.getUserRole().equals("ADMIN")) { if (obj != null) {
		 * 
		 * Map params = new HashMap(); params.put("type", "user"); List<Article>
		 * articles = this.articleService .query(
		 * "select obj from Article obj where obj.type=:type order by obj.addTime desc"
		 * , params, 0, 6);
		 * 
		 * } else { mv = new JModelAndView("error.html",
		 * configService.getSysConfig(), this.userConfigService.getUserConfig(),
		 * 1, request, response); mv.addObject("op_title",
		 * "Seller announcement, you can't view it！"); } } } else { if (obj !=
		 * null && obj.getType().equals("user")) {
		 * 
		 * Map params = new HashMap(); params.put("type", "user"); List<Article>
		 * articles = this.articleService .query(
		 * "select obj from Article obj where obj.type=:type order by obj.addTime desc"
		 * , params, 0, 6);
		 * 
		 * } else { mv = new JModelAndView("error.html",
		 * configService.getSysConfig(), this.userConfigService.getUserConfig(),
		 * 1, request, response); mv.addObject("op_title",
		 * "Seller announcement, you can't view it！"); } }
		 */
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("v1/article_goods.json")
	public void article_goods(HttpServletRequest request, HttpServletResponse response, String gc_id,
			String currentPage, String orderBy, String orderType, String brand_ids, String gs_ids, String properties,
			String all_property_status, String detail_property_status, String goods_type, String goods_inventory,
			String goods_transfee, String goods_cod, String gc_ids) {
		Result result = null;
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Map<String, Object> wap_map = new HashMap<String, Object>();
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

			List<Map<String, Object>> gc_list_map = new ArrayList<Map<String, Object>>();
			for (Iterator<GoodsClass> iterator = gc_list.iterator(); iterator.hasNext();) {
				GoodsClass goodsclass = iterator.next();
				Map<String, Object> gc_map = new HashMap<String, Object>();
				gc_map.put("gc_id", goodsclass.getId());
				gc_map.put("gc_name", goodsclass.getClassName());
				if (goodsclass.getChilds() != null && !goodsclass.getChilds().equals("")) {
					List<Map<String, Object>> gcc_list_map = new ArrayList<Map<String, Object>>();
					for (Iterator<GoodsClass> iteratorc = goodsclass.getChilds().iterator(); iterator.hasNext();) {
						GoodsClass goodsclassc = iterator.next();
						Map<String, Object> gcc_map = new HashMap<String, Object>();
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
			orderBy = "addTime";
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
			Map<String, Object> paras = new HashMap<String, Object>();
			paras.put("ids", ids);
			gqo.addQuery("obj.gc.id in (:ids)", paras);
		}

		if (goods_cod != null && !goods_cod.equals("")) {
			gqo.addQuery("obj.goods_cod", new SysMap("goods_cod", 0), "=");
			mv.addObject("goods_cod", goods_cod);
		}
		if (goods_transfee != null && !goods_transfee.equals("")) {
			gqo.addQuery("obj.goods_transfee", new SysMap("goods_transfee", 1), "=");
			mv.addObject("goods_transfee", goods_transfee);
		}

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
				Map<String, Object> map = new HashMap<String, Object>();
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
						Map<String, Object> map = new HashMap<String, Object>();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else if (i == brand_id_list.length - 1) {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id) + ")", null);
						Map<String, Object> map = new HashMap<String, Object>();
						GoodsBrand brand = this.brandService.getObjById(CommUtil.null2Long(brand_id));
						map.put("name", "品牌");
						map.put("value", brand.getName());
						map.put("type", "brand");
						map.put("id", brand.getId());
						goods_property.add(map);
					} else {
						gqo.addQuery("or obj.goods_brand.id=" + CommUtil.null2Long(brand_id), null);
						Map<String, Object> map = new HashMap<String, Object>();
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
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", gsp.getSpec().getName());
					map.put("value", gsp.getValue());
					map.put("type", "gs");
					map.put("id", gsp.getId());
					goods_property.add(map);
				} else {
					for (int i = 0; i < gsp_list.size(); i++) {
						if (i == 0) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "and(");
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else if (i == gsp_list.size() - 1) {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs)", "member of", "or");
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("name", gsp.getSpec().getName());
							map.put("value", gsp.getValue());
							map.put("type", "gs");
							map.put("id", gsp.getId());
							goods_property.add(map);
						} else {
							GoodsSpecProperty gsp = gsp_list.get(i);
							gqo.addQuery("gsp" + j + i, gsp, "obj.goods_specs", "member of", "or");
							Map<String, Object> map = new HashMap<String, Object>();
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

		List<Map<String, Object>> propertylist = null;
		if (!CommUtil.null2String(properties).equals("")) {
			String[] properties_list = properties.substring(1).split("\\|");
			for (int i = 0; i < properties_list.length; i++) {
				String property_info = CommUtil.null2String(properties_list[i]);
				String[] property_info_list = property_info.split(",");
				GoodsTypeProperty gtp = this.goodsTypePropertyService
						.getObjById(CommUtil.null2Long(property_info_list[0]));
				Map<String, Object> p_map = new HashMap<String, Object>();
				p_map.put("gtp_name" + i, "%" + gtp.getName().trim() + "%");
				p_map.put("gtp_value" + i, "%" + property_info_list[1].trim() + "%");
				gqo.addQuery("and (obj.goods_property like :gtp_name" + i + " and obj.goods_property like :gtp_value"
						+ i + ")", p_map);
				Map<String, Object> map = new HashMap<String, Object>();
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
				Map<String, Object> goodsTypemap = new HashMap<String, Object>();
				propertylist = new ArrayList<Map<String, Object>>();
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
					List<Map<String, Object>> goodsTypeProperty_list = new ArrayList<Map<String, Object>>();
					for (GoodsTypeProperty goodsTypeProperty : goodstype_property) {
						Map<String, Object> goodsTypePropertyMap = new HashMap<String, Object>();
						goodsTypePropertyMap.put("Property_id", goodsTypeProperty.getId());
						goodsTypePropertyMap.put("Property_name", goodsTypeProperty.getName());

						List<Map<String, Object>> goodsTypeProperty_v_list = new ArrayList<Map<String, Object>>();
						for (String s : CommUtil.splitByChar(goodsTypeProperty.getValue(), ",")) {
							Map<String, Object> goodsTypeProperty_v_Map = new HashMap<String, Object>();
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
				List<Map<String, Object>> gb_list = new ArrayList<Map<String, Object>>();
				for (GoodsBrand gb : gc.getGoodsType().getGbs()) {
					Map<String, Object> gbmap = new HashMap<String, Object>();
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
		List<Goods> obj = pList.getResult();
		List<Map<String, Object>> goodslist = new ArrayList<Map<String, Object>>();
		Set<Map<String, Object>> goodsset = new HashSet<Map<String, Object>>();
		for (Goods goods : obj) {
			Map goodsmap = new HashMap();
			goodsmap.put("goodsid", goods.getId());
			goodsmap.put("goods_name", goods.getGoods_name());
			goodsmap.put("goods_price", goods.getGoods_price());
			goodsmap.put("goods_current_price", goods.getGoods_current_price());
			goodsmap.put("well_evaluate", goods.getWell_evaluate() == null ? 0 : goods.getWell_evaluate());
			goodsmap.put("goods_main_photo", goods.getGoods_main_photo() != null
					? imageWebServer + "/" + goods.getGoods_main_photo().getPath() + "/"
							+ goods.getGoods_main_photo().getName() + "_small." + goods.getGoods_main_photo().getExt()
					: imageWebServer + "/" + this.configService.getSysConfig().getGoodsImage().getPath() + "/"
							+ this.configService.getSysConfig().getGoodsImage().getName() + "_small."
							+ this.configService.getSysConfig().getGoodsImage().getExt());
			goodsmap.put("goodsext", goods.getAccessory() != null ? goods.getAccessory().getExt() : null);
			List<Accessory> acc = goods.getGoods_photos();
			List<Map> acclist = new ArrayList<Map>();
			for (Accessory accessory : acc) {
				Map accmap = new HashMap();
				accmap.put("photos", configService.getSysConfig().getImageWebServer() + "/" + accessory.getPath() + "/"
						+ accessory.getName());
				acclist.add(accmap);
			}

			goodsmap.put("goods_photo", acclist);
			if (goods.getGoods_store() != null && !goods.getGoods_store().equals("")) {
				try {
					goodsmap.put("store_logo",
							this.configService.getSysConfig().getImageWebServer() + "/"
									+ goods.getGoods_store().getStore_logo().getPath() + "/"
									+ goods.getGoods_store().getStore_logo().getName());
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					goodsmap.put("store_logo", "");
				}
			}
			goodslist.add(goodsmap);
		}
		wap_map.put("goodslist", goodslist);
		// [已选择商品属性]
		Map<String, Object> goods_property_map = new HashMap<String, Object>();
		List<Map<String, Object>> goods_property_list = new ArrayList<Map<String, Object>>();
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
			Map<String, Object> pro_map = new HashMap<String, Object>();
			List pro_list = new ArrayList();
			for (String property_status : temp_str) {
				if (property_status != null && !property_status.equals("")) {
					String mark[] = property_status.split("_");
					pro_map.put(mark[0], mark[1]);
					pro_list.add(mark[0]);
				}
			}
			mv.addObject("pro_list", pro_list);
			mv.addObject("pro_map", pro_map);
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
		result = new Result(0, "Successfully", wap_map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	 * 解析集合
	 * 
	 * @param list
	 * @return
	 */
	/*
	 * public static <T> String ToJsonByTObject(List<T> list){ //json格式
	 * [{"":"","":""},{}] StringBuffer sb = new StringBuffer("[");
	 * if(list!=null){ try { //获取集合类型 int j = list.size(); for (int i = 0; i <
	 * j; i++) { Class c = Class.forName(list.get(i).getClass().getName());
	 * Field[] fs = c.getDeclaredFields(); sb.append("{"); for (int k = 0; k <
	 * fs.length; k++) { Object objs = list.get(i); StringBuffer suf = new
	 * StringBuffer("get"); suf.append(captureName(fs[k].getName())); Method me
	 * = c.getDeclaredMethod(suf.toString()); Object o = me.invoke(objs);
	 * //判断属性值是否为空 if(o!=null){ String str = fs[k].getType().getName();
	 * //注解判断属性是否是本地类 Entity entity = fs[k].getAnnotation(Entity.class);
	 * if(entity!=null){ //判断是否是对象 //if(isMyObject(str)){
	 * analysisObject(str,sb,fs,k,o); }else{ if(k!=0){ sb.append(","); }
	 * sb.append("\""+fs[k].getName()+"\":\""+o+"\""); } } } sb.append("}");
	 * if(i!=j-1){ sb.append(","); } } } catch (Exception e) {
	 * e.printStackTrace(); } sb.append("]"); } return sb.toString(); }
	 * 
	 *//**
		 * 解析属性为对象的本地类，p_name
		 * 
		 * @param str
		 */
	/*
	 * public static void analysisObject(String str,StringBuffer sb,Field[]
	 * fs,int k,Object o){ try { String content = getFirstA(fs[k].getName());
	 * Class c1 = Class.forName(str);
	 * 
	 * Field[] f1 = c1.getDeclaredFields(); for (int l = 0; l < f1.length; l++)
	 * { StringBuffer suf = new StringBuffer("get");
	 * suf.append(captureName(f1[l].getName())); Method me =
	 * c1.getDeclaredMethod(suf.toString()); Object ob = me.invoke(o);
	 * if(ob!=null){ String str1 = f1[l].getType().getName(); Entity entity =
	 * f1[l].getAnnotation(Entity.class); if(entity!=null){ //判断是否是对象
	 * //if(isMyObject(str)){ analysisObject(str1,sb,f1,l,ob); }else{
	 * sb.append(","); sb.append("\""+content+f1[l].getName()+"\":\""+ob+"\"");
	 * } } } } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 *//**
		 * 字符串第一个字符大写
		 * 
		 * @param name
		 * @return
		 *//*
		 * public static String captureName(String name) { char[]
		 * cs=name.toCharArray(); //字符a对应数字97 cs[0]-=32; return
		 * String.valueOf(cs); }
		 * 
		 * public static String getFirstA(String str){ String name =
		 * str.toLowerCase(); char[] cs=name.substring(0, 1).toCharArray();
		 * return String.valueOf(cs[0])+"_"; }
		 */
}
