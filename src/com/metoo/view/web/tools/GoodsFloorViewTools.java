package com.metoo.view.web.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Advert;
import com.metoo.foundation.domain.AdvertPosition;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAdvertPositionService;
import com.metoo.foundation.service.IAdvertService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsFloorService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;

/**
 * 
 * <p>
 * Title: GoodsFloorViewTools.java
 * </p>
 * 
 * <p>
 * Description: 楼层管理json转换工具
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-8-25
 * 
 * @version koala_b2b2c v2.0 2015版 
 */
@Component
public class GoodsFloorViewTools {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsFloorService goodsFloorService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IAdvertPositionService advertPositionService;
	@Autowired
	private IAdvertService advertService;
	@Autowired
	private IGoodsBrandService goodsBrandService;

	public List<GoodsClass> generic_gf_gc(String json) {
		List<GoodsClass> gcs = new ArrayList<GoodsClass>();
		if (json != null && !json.equals("")) {
			try {
				List<Map> list = Json.fromJson(List.class, json);
				Set<Long> ids = new HashSet<Long>();
				Map params = new HashMap();
				for (Map map : list) {
					GoodsClass the_gc = this.goodsClassService
							.getObjById(CommUtil.null2Long(map.get("pid")));
					ids.add(CommUtil.null2Long(map.get("pid")));
					params.put("ids", ids);
					List<GoodsClass> the_gcs = this.goodsClassService
							.query("select new GoodsClass(id,className) from GoodsClass obj where obj.id in(:ids)",
									params, 0, 1);
					if (the_gcs.size() > 0) {
						the_gc = the_gcs.get(0);
						if (the_gc != null) {
							int count = CommUtil.null2Int(map.get("gc_count"));
							ids.clear();
							params.clear();
							for (int i = 1; i <= count; i++) {
								ids.add(CommUtil.null2Long(map.get("gc_id" + i)));
							}
							// 查询子类
							ids.add(CommUtil.null2Long(map.get("pid")));
							params.put("ids", ids);
							List<GoodsClass> childs = this.goodsClassService
									.query("select new GoodsClass(id,className) from GoodsClass obj where obj.id in(:ids)",
											params, -1, -1);
							the_gc.getChilds().addAll(childs);
							gcs.add(the_gc);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return gcs;
	}

	public List<Goods> generic_goods(String json) {
		List<Goods> goods_list = new ArrayList<Goods>();
		Set<Long> ids = new HashSet<Long>();
		Map params = new HashMap();
		if (json != null && !json.equals("")) {
			try {
				Map map = Json.fromJson(Map.class, json);
				for (int i = 1; i <= 10; i++) {
					String key = "goods_id" + i;
					ids.add(CommUtil.null2Long(map.get(key)));
				}
				if (!ids.isEmpty()) {
					params.put("ids", ids);
					goods_list = this.goodsService
							.query("select new Goods(id,goods_name,goods_current_price,goods_price,goods_main_photo) from Goods obj where obj.id in(:ids)",
									params, -1, -1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return goods_list;
	}
	
	public List generic_goods2(String json, String language) {
		List<Goods> list = new ArrayList<Goods>();
		List goods_list = new ArrayList();
		Set<Long> ids = new HashSet<Long>();
		Map params = new HashMap();
		int length = 0;
		if(json != null && !json.equals("")){
			String[] jsons = json.split(",");
			for(String str : jsons){
				length += 1;
			}
		}
		if (json != null && !json.equals("")) {
			try {
				Map map = Json.fromJson(Map.class, json);
				for (int i = 1; i <= length; i++) {
					String key = "goods_id" + i;
					ids.add(CommUtil.null2Long(map.get(key)));
				}
				if (!ids.isEmpty()) {
					params.put("ids", ids);
					list = this.goodsService
							.query("select obj from Goods obj where obj.id in(:ids) and obj.goods_store.store_status=15 ",
									params, -1, -1);
					for(Goods obj : list){
						Map goodsMap = new HashMap();
						if(CommUtil.isNotNull(obj.getGoods_main_photo())){
							goodsMap.put("goodsimg", obj.getGoods_main_photo() == null ? "" : 
											this.configService.getSysConfig()
													.getImageWebServer()
												+ "/"
												+ obj.getGoods_main_photo()
													.getPath()
												+ "/"
												+ obj.getGoods_main_photo()
													.getName() 
												+ "_middle." 
												+ obj.getGoods_main_photo()
													.getExt());
						}
						goodsMap.put("goodsid", obj.getId());
						goodsMap.put("goodsType", obj.getGoods_type());
						goodsMap.put("goodsname", obj.getGoods_name());
						if("1".equals(language)){
							goodsMap.put("goodsname", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name()) ? "^"+obj.getKsa_goods_name() : obj.getGoods_name());
							goodsMap.put("goods_detail", obj.getKsa_goods_detail() != null && !"".equals(obj.getKsa_goods_detail()) ? "^"+obj.getKsa_goods_detail() : obj.getGoods_details());
							if(null != obj.getKsa_features() && !"".equals(obj.getKsa_features())){
								Map features_map = Json.fromJson(Map.class,
										obj.getKsa_features());
								goodsMap.put("features_one", features_map.get("1") != null && !"".equals(features_map.get("1")) ? "^"+features_map.get("1") : obj.getFeatures_one());
								goodsMap.put("features_two", features_map.get("2") != null && !"".equals(features_map.get("1")) ? "^"+features_map.get("2") : obj.getFeatures_two());
								goodsMap.put("features_three", features_map.get("3") != null && !"".equals(features_map.get("1")) ? "^"+features_map.get("3") :  obj.getFeatures_three());
								goodsMap.put("features_four", features_map.get("4") != null && !"".equals(features_map.get("1")) ? "^"+features_map.get("4") : obj.getFeatures_four());
								goodsMap.put("features_five", features_map.get("5") != null && !"".equals(features_map.get("1")) ? "^"+features_map.get("5") : obj.getFeatures_five());
							}
						}
						goodsMap.put("goods_discount_rate", obj.getGoods_discount_rate());
						goodsMap.put("goods_price", obj.getGoods_price());
						goodsMap.put("goods_current_price", obj.getGoods_current_price());
						goodsMap.put("well_evaluate", obj.getWell_evaluate() == null ? 0 : obj.getWell_evaluate());
						goodsMap.put("goods_inventory", obj.getGoods_inventory());
						goodsMap.put("goods_status", obj.getGoods_status());
						goodsMap.put("store_status", obj.getGoods_store() == null ? "" : obj.getGoods_store().getStore_status());
						if(obj.getGoods_store() != null && !obj.getGoods_store().equals("")){
							goodsMap.put("store_logo", obj.getGoods_store().getStore_logo() == null ? "" : 
											this.configService.getSysConfig().getImageWebServer() 
											+ "/"
											+ obj.getGoods_store()
												.getStore_logo()
												.getPath() 
											+ "/"
											+ obj.getGoods_store()
												.getStore_logo()
												.getName());
						}
						for(Evaluate eva:obj.getEvaluates()){
							goodsMap.put("evaluate_status", eva.getEvaluate_status());
								}
						goods_list.add(goodsMap);
						}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return goods_list;
	}
	

	public Map generic_goods_list(String json) {
		Map map = new HashMap();
		map.put("list_title", "商品排行");
		if (json != null && !json.equals("")) {
			try {
				Map list = Json.fromJson(Map.class, json);
				map.put("list_title",
						CommUtil.null2String(list.get("list_title")));
				Map params = new HashMap();
				for (int i = 1; i <= 6; i++) {
					params.clear();
					params.put("id",
							CommUtil.null2Long(list.get("goods_id" + i)));
					List<Goods> temps = this.goodsService
							.query("select new Goods(id,goods_name,goods_current_price,goods_price,goods_main_photo) from Goods obj where obj.id=:id",
									params, -1, -1);
					if (temps.size() > 0) {
						map.put("goods" + i, temps.get(0));
					} else {
						map.put("goods" + i, null);
					}
				}
			} catch (Exception e) {
				map.put("list_title", "");
				map.put("goods1", null);
				map.put("goods2", null);
				map.put("goods3", null);
				map.put("goods4", null);
				map.put("goods5", null);
				map.put("goods6", null);
				e.printStackTrace();
			}

		}
		return map;
	}

	public String generic_adv(String web_url, String json) {
		String template = "<div style='float:left;overflow:hidden;'>";
		if (json != null && !json.equals("")) {
			try {
				Map map = Json.fromJson(Map.class, json);
				if (CommUtil.null2String(map.get("adv_id")).equals("")) {
					Accessory img = this.accessoryService.getObjById(CommUtil
							.null2Long(map.get("acc_id")));
					if (img != null) {
						String url = CommUtil.null2String(map.get("acc_url"));
						template = template + "<a href='" + url
								+ "' target='_blank'><img src='" + web_url
								+ "/" + img.getPath() + "/" + img.getName()
								+ "' /></a>";
					}
				} else {
					AdvertPosition ap = this.advertPositionService
							.getObjById(CommUtil.null2Long(map.get("adv_id")));
					AdvertPosition obj = new AdvertPosition();
					obj.setAp_type(ap.getAp_type());
					obj.setAp_status(ap.getAp_status());
					obj.setAp_show_type(ap.getAp_show_type());
					obj.setAp_width(ap.getAp_width());
					obj.setAp_height(ap.getAp_height());
					List<Advert> advs = new ArrayList<Advert>();
					for (Advert temp_adv : ap.getAdvs()) {
						if (temp_adv.getAd_status() == 1
								&& temp_adv.getAd_begin_time().before(
										new Date())
								&& temp_adv.getAd_end_time().after(new Date())) {
							advs.add(temp_adv);
						}
					}
					if (advs.size() > 0) {
						if (obj.getAp_type().equals("img")) {
							if (obj.getAp_show_type() == 0) {// 固定广告
								obj.setAp_acc(advs.get(0).getAd_acc());
								obj.setAp_acc_url(advs.get(0).getAd_url());
								obj.setAdv_id(CommUtil.null2String(advs.get(0)
										.getId()));
							}
							if (obj.getAp_show_type() == 1) {// 随机广告
								Random random = new Random();
								int i = random.nextInt(advs.size());
								obj.setAp_acc(advs.get(i).getAd_acc());
								obj.setAp_acc_url(advs.get(i).getAd_url());
								obj.setAdv_id(CommUtil.null2String(advs.get(i)
										.getId()));
							}
						}
					} else {
						obj.setAp_acc(ap.getAp_acc());
						obj.setAp_text(ap.getAp_text());
						obj.setAp_acc_url(ap.getAp_acc_url());
						Advert adv = new Advert();
						adv.setAd_url(obj.getAp_acc_url());
						adv.setAd_acc(ap.getAp_acc());
						obj.getAdvs().add(adv);
					}
					//
					template = template + "<a href='" + obj.getAp_acc_url()
							+ "' target='_blank'><img src='" + web_url + "/"
							+ obj.getAp_acc().getPath() + "/"
							+ obj.getAp_acc().getName() + "' /></a>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		template = template + "</div>";
		return template;
	}

	public List<GoodsBrand> generic_brand(String json) {
		List<GoodsBrand> brands = new ArrayList<GoodsBrand>();
		if (json != null && !json.equals("")) {
			try {
				Map map = Json.fromJson(Map.class, json);
				for (int i = 1; i <= 11; i++) {
					String key = "brand_id" + i;
					GoodsBrand brand = this.goodsBrandService
							.getObjById(CommUtil.null2Long(map.get(key)));
					if (brand != null) {
						brands.add(brand);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return brands;
	}

	/**
	 * 生成商城首页style2样式单个模块信息
	 * 
	 * @param json
	 * @param module_id
	 * @return
	 */
	public Map generic_style2_goods(String json, String module_id) {
		try {
			List<Map> maps = Json.fromJson(List.class, json);
			for (Map map : maps) {
				if (map.get("module_id").equals(module_id)) {
					return map;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
}
