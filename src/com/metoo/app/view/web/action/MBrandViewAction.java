package com.metoo.app.view.web.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.app.buyer.domain.Result;
import com.metoo.core.mv.JModelAndView;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.service.IGoodsBrandCategoryService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.lucene.LuceneResult;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.StoreViewTools;

@Controller
@RequestMapping("/app/")
public class MBrandViewAction {

	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;

	@RequestMapping("v1/wap_brand.json")
	public void wap_brand(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("recommend", true);
		params.put("audit", 1);// 是否通过审核,1为审核通过，0为未审核，-1为审核未通过
		List<GoodsBrand> goodsBrandList = this.goodsBrandService.query(
				"select obj from GoodsBrand obj where obj.recommend=:recommend and obj.audit=:audit order by obj.sequence asc",
				params, -1, -1);
		List<Map<String, Object>> brandsmap = new ArrayList<Map<String, Object>>();
		for (GoodsBrand obj : goodsBrandList) {
			Map<String, Object> goodsBrandMap = new HashMap<String, Object>();
			goodsBrandMap.put("brand_id", obj.getId());
			goodsBrandMap.put("brand_name", obj.getName());
			goodsBrandMap.put("brand_photo", this.configService.getSysConfig().getImageWebServer() + "/"
					+ obj.getBrandLogo().getPath() + "/" + obj.getBrandLogo().getName());
			brandsmap.add(goodsBrandMap);
		}
		map.put("brands", brandsmap);
		List<GoodsBrand> brands = new ArrayList<GoodsBrand>();
		params.clear();
		params.put("audit", 1);
		brands = this.goodsBrandService.query(
				"select obj from GoodsBrand obj where obj.audit=:audit order by obj.sequence asc ", params, -1, -1);
		List<Map<String, Object>> all_list = new ArrayList<Map<String, Object>>();
		String list_word = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
		String words[] = list_word.split(",");
		for (String word : words) {
			Map<String, Object> brand_map = new HashMap<String, Object>();
			List<Map<String, Object>> word_brand_map = new ArrayList<Map<String, Object>>();
			List<GoodsBrand> brandList = new ArrayList<GoodsBrand>();
			for (GoodsBrand brand : brands) {
				// 品牌首字母，后台管理添加
				if (!CommUtil.null2String(brand.getFirst_word()).equals("")
						&& word.equals(brand.getFirst_word().toUpperCase())) {
					brandList.add(brand);
				}
			}
			for (GoodsBrand obj : brandList) {
				Map<String, Object> brandMap = new HashMap<String, Object>();
				brandMap.put("goods_brand_id", obj.getId());
				brandMap.put("recommend", obj.isRecommend());
				brandMap.put("goods_brand_name", obj.getName());
				brandMap.put("goods_brand_remark", obj.getRemark());
				brandMap.put("brand_photo", this.configService.getSysConfig().getImageWebServer() + "/"
						+ obj.getBrandLogo().getPath() + "/" + obj.getBrandLogo().getName());
				word_brand_map.add(brandMap);
			}
			brand_map.put("word_brand_map", word_brand_map);
			brand_map.put("word", word);
			all_list.add(brand_map);
			map.put("all_list", all_list);
		}
		this.send_json(Json.toJson(new Result(0, "Successfully", map), JsonFormat.compact()), response);
	}

	/**
	 * 根据品牌id查看商品
	 */
	// @RequestMapping("/wap_brand_goods.json")
	@RequestMapping("v1/wap_brand_goods.json")
	public void brand_goods(HttpServletRequest request, HttpServletResponse response, String id, String currentPage,
			String orderBy, String orderType, String goods_inventory, String goods_type, String goods_transfee,
			String goods_cod) {
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		ModelAndView mv = new JModelAndView("brand_goods.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		GoodsBrand goodsBrand = this.goodsBrandService.getObjById(CommUtil.null2Long(id));
		if (goodsBrand != null) {
			Map<String, Object> goodsBrandMap = new HashMap<String, Object>();
			goodsBrandMap.put("goods_brand_id", goodsBrand.getId());
			goodsBrandMap.put("recommend", goodsBrand.isRecommend());
			goodsBrandMap.put("goods_brand_name", goodsBrand.getName());
			goodsBrandMap.put("goods_brand_remark", goodsBrand.getRemark());
			goodsBrandMap.put("brand_photo",
					goodsBrand.getBrandLogo() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/"
									+ goodsBrand.getBrandLogo().getPath() + "/" + goodsBrand.getBrandLogo().getName()
									+ "_small." + goodsBrand.getBrandLogo().getExt());
			map.put("gb_info", goodsBrandMap);
			String path = System.getProperty("metoob2b2c.root") + File.separator + "luence" + File.separator + "goods";
			LuceneUtil lucene = LuceneUtil.instance();
			lucene.setIndex_path(path);
			Sort sort = null;
			boolean order_type = true;
			String order_by = "";
			// 处理排序方式
			if (CommUtil.null2String(orderBy).equals("goods_salenum")) {
				order_by = "goods_salenum";
				sort = new Sort(new SortField(order_by, SortField.Type.INT, order_type));
			}
			if (CommUtil.null2String(orderBy).equals("goods_collect")) {
				order_by = "goods_collect";
				sort = new Sort(new SortField(order_by, SortField.Type.INT, order_type));
			}
			if (CommUtil.null2String(orderBy).equals("well_evaluate")) {
				order_by = "well_evaluate";
				sort = new Sort(new SortField(order_by, SortField.Type.DOUBLE, order_type));
			}
			if (CommUtil.null2String(orderType).equals("asc")) {
				order_type = false;
			}
			if (CommUtil.null2String(orderType).equals("")) {
				orderType = "desc";
			}
			if (CommUtil.null2String(orderBy).equals("goods_current_price")) {
				order_by = "store_price";
				sort = new Sort(new SortField(order_by, SortField.Type.DOUBLE, order_type));
			}
			LuceneResult pList = null;
			if (sort != null) {
				pList = lucene.search(null, CommUtil.null2Int(currentPage), goods_inventory, goods_type, null,
						goods_transfee, goods_cod, sort, null, null, goodsBrand.getName());
			} else {
				pList = lucene.search(null, CommUtil.null2Int(currentPage), goods_inventory, goods_type, null,
						goods_transfee, goods_cod, null, null, goodsBrand.getName());
			}
			List<LuceneVo> goodslist = pList.getVo_list();
			List<Map> goodsList = new ArrayList<Map>();
			for (LuceneVo goods : goodslist) {
				Map<String, Object> goodsMap = new HashMap<String, Object>();
				goodsMap.put("goods_id", goods.getVo_id());
				goodsMap.put("goods_name", goods.getVo_title());
				goodsMap.put("goods_type", goods.getVo_goods_type());
				goodsMap.put("goods_main_photo",
						this.configService.getSysConfig().getImageWebServer() + "/" + goods.getVo_main_photo_url());

				/*
				 * List<String> goods_img =
				 * goodsViewTools.query_LuceneVo_photos_url(goods.
				 * getVo_photos_url()); for(String img : goods_img){ if(img !=
				 * null && !img.equals("")){ goodsMap.put("goodsphoto",
				 * this.configService.getSysConfig().getImageWebServer() + "/" +
				 * img); } }
				 */

				goodsMap.put("goodsphoto", "");
				goodsMap.put("goods_store_price", goods.getVo_store_price());
				goodsMap.put("goods_cost_price", goods.getVo_cost_price());
				goodsMap.put("goods_salenum", goods.getVo_goods_salenum());
				goodsMap.put("goods_well_evaluate", goods.getVo_well_evaluate());
				goodsMap.put("goods_vo_goods_evas", goods.getVo_goods_evas());
				goodsMap.put("goods_evas", goods.getVo_goods_evas());
				goodsList.add(goodsMap);
			}
			map.put("goodsinfo", goodsList);
			map.put("allCount", pList.getRows());
		}
		this.send_json(Json.toJson(new Result(0, "Successfully", map), JsonFormat.compact()), response);
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
