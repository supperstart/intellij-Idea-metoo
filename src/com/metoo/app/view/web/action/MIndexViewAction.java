package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;

@Controller
@RequestMapping("/app/v1/")
public class MIndexViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired 
	private IGoodsClassService gcService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IUserConfigService userConfigService;
	

	@RequestMapping("indexDiscount.json")
	public void index_discount(HttpServletRequest request,
			HttpServletResponse response,String orderBy, 
			String orderType,String currentPage,String type,
			String price, String language){
		ModelAndView mv = new JModelAndView("",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Result result = null;
		Map map = new HashMap();
		//obj.weightiness
		if (orderBy == null || orderBy.equals("")) {
			orderBy = "addTime";
		}
		if (orderType == null || orderType.equals("")) {
			orderType = "desc";
		}
		if(type != null && type.equals("goods_discount_rate")){
			orderBy = "goods_discount_rate";
		}
		GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv,
				orderBy, orderType);
		if(type != null && type.equals("store_recommend")){
			gqo.addQuery(
					"obj.store_recommend",
					new SysMap("goods_store_recommend", CommUtil
							.null2Boolean(true)), "=");
			gqo.setOrderBy("weightiness,RAND()");
		}
		if(type != null && type.equals("store_creativity")){
			gqo.addQuery(
					"obj.store_creativity",
					new SysMap("store_creativity", CommUtil
							.null2Boolean(true)), "=");
			gqo.setOrderBy("weightiness,RAND()");
		}
		if(type != null && type.equals("store_deals")){
			gqo.addQuery(
					"obj.store_deals",
					new SysMap("store_deals", CommUtil
							.null2Boolean(true)), "=");
		}
		if(type != null && type.equals("china")){
			gqo.addQuery(
					"obj.store_china",
					new SysMap("store_china", CommUtil
							.null2Boolean(true)), "=");
		}
		if(type != null && type.equals("goods_global")){
			gqo.addQuery("obj.goods_global", 
					new SysMap("goods_global", CommUtil.null2Int(1)), "=");
		}
		if(type != null && type.equals("point")){
			gqo.addQuery("obj.point", 
					new SysMap("point", CommUtil.null2Int(1)), "=");
		}
		
		if(price != null && !price.equals("")){
			String[] prices = price.split(",");
			String first = prices[0];
			if(first != null && !first.equals("") && prices.length >= 2){
				String second = prices[1];
				if(second != null && !second.equals("")){
					if(Integer.parseInt(first) < Integer.parseInt(second)){
						gqo.addQuery("obj.goods_current_price", first, second,"BETWEEN");
					}
				}
			}
		}
		gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
		gqo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
		IPageList pList = this.goodsService.list(gqo);
		List<Goods> goods = pList.getResult();
		List objList = new ArrayList();
		for(Goods obj : goods){
			Map<String, Object> objMap = new HashMap<String, Object>();
			if(CommUtil.isNotNull(obj.getGoods_main_photo())){
				objMap.put("goods_img", this.configService.getSysConfig().getImageWebServer() 
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
				objMap.put("goods_id", obj.getId());
				objMap.put("goods_type", obj.getGoods_type());
				objMap.put("goods_name", obj.getGoods_name());
				if("1".equals(language)){
					objMap.put("goods_name", obj.getKsa_goods_name() != null 
												&& !"".equals(
														obj.getKsa_goods_name()) 
												   ? "^" 
												   + obj.getKsa_goods_name() 
												   : obj.getGoods_name());
				}
				objMap.put("goods_discount_rate", obj.getGoods_discount_rate());
				objMap.put("goods_price", obj.getGoods_price());
				objMap.put("goods_current_price", obj.getGoods_current_price() == null ? 0 : obj.getGoods_current_price());
				objMap.put("well_evaluate", obj.getWell_evaluate() == null ? 0 : obj.getWell_evaluate());
				objMap.put("goods_collect", obj.getGoods_collect());
				//objMap.put("goods_inventory", obj.getGoods_inventory());
				if(obj.isStore_deals()){
					objMap.put("goods_inventory", obj.isStore_deals() == true ? obj.getStore_seckill_inventory() : 0);//商品秒杀库存
					objMap.put("store_deals_inventory", obj.isStore_deals() == true ? obj.getStore_deals_inventory() : 0);
				}
				objMap.put("goods_status", obj.getGoods_status());
				objMap.put("goods_salenum", obj.getGoods_salenum());
				objMap.put("store_statue", obj.getGoods_store().getStore_status());
				objMap.put("store_logo", obj.getGoods_store().getStore_logo() == null ? "" : 
								this.configService.getSysConfig()
									.getImageWebServer() 
									+"/"
									+ obj.getGoods_store()
										.getStore_logo()
										.getPath() 
									+"/"
									+ obj.getGoods_store()
									.getStore_logo()
									.getName());
			for(Evaluate eva:obj.getEvaluates()){
				objMap.put("evaluate_status", eva.getEvaluate_status());
			}
			objList.add(objMap);
		}
		map.put("goods_info", objList);
		map.put("goods_Pages", pList.getPages());
		result = new Result(0, "Successfully", map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}
	
	/**
	 *APP-商品类目列表
	 */
//	@RequestMapping("/nav_querygc_pnull.json")	
	@RequestMapping("goodsClass.json")	
	public void nav(HttpServletRequest request,HttpServletResponse response){
		List<GoodsClass> goodsClassList = new ArrayList<GoodsClass>();
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("display", true);
		goodsClassList = this.gcService
				.query("select obj from GoodsClass obj where obj.parent.id is null and obj.display=:display order by obj.sequence asc",
						params, -1, -1);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if(goodsClassList.size() > 0){
			for(GoodsClass obj : goodsClassList){
				Map<String, Object> goodsClassMap = new HashMap<String, Object>();
				goodsClassMap.put("gcid", obj.getId());
				goodsClassMap.put("gcname", obj.getClassName());
				goodsClassMap.put("icon_acc", obj.getIcon_acc() == null ? "" : 
							this.configService.getSysConfig()
							.getImageWebServer()
							+ "/"  
							+ obj.getIcon_acc()
								.getPath()
							+ "/"
							+ obj.getIcon_acc()
								.getName());
			
				list.add(goodsClassMap);
			}
		}
		map.put("gcslist", list);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		result = new Result(0,"Successfully", map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}
	

	/**
	 * APP-查询子类目以及类目下最热销商品
	 * APP-Find subcategories and the most popular goods under each category
	 * 可优化【使用递归】
	 * @param request
	 * @param response
	 * @param pid category ID
	 * 
	 */
	/**
	 * 
	 * @param request
	 * @param response
	 * @param pid
	 * @description 二级类目查询 and 一个热销产品
	 */
	@RequestMapping("categoryGoods.json")
	public void categoryGoods(HttpServletRequest request,
				HttpServletResponse response, @RequestParam(value = "pid", required = true) String pid){
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		if(!CommUtil.null2String(pid).equals("")){
			GoodsClass goodsClass = this.goodsClassService.getObjById(CommUtil
					.null2Long(pid));
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> goodsClassMap = new HashMap<String, Object>();
			goodsClassMap.put("gc_id", goodsClass.getId());
			goodsClassMap.put("gc_name", goodsClass.getClassName());
			goodsClassMap.put("gc_level", goodsClass.getLevel());
			list.add(goodsClassMap);
			map.put("topgc", list);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("display", true);
			params.put("pid",CommUtil.null2Long(pid));
			List<GoodsClass> childGoodsClassList = this.gcService.query("select obj from GoodsClass obj where obj.parent.id=:pid and obj.display=:display order by obj.sequence asc", params, -1, -1);
			List<Map<String, Object>> gcslist = new ArrayList<Map<String, Object>>();
			for(GoodsClass childGoodsClass : childGoodsClassList){
				Map<String, Object> gcsmap = new HashMap<String, Object>() ;
				Set<GoodsClass> GoodsClassSet = childGoodsClass.getChilds();
				List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
				if(GoodsClassSet.size() > 0){
					for(GoodsClass grandsonGoodsClass : GoodsClassSet){
						Map<String, Object> dmap = new HashMap<String, Object>();
						Accessory accessory = gc_hot_goods(request, response, CommUtil.null2String(grandsonGoodsClass.getId()));
						if(accessory == null){
						}else{
							dmap.put("gc_img", grandsonGoodsClass.getIcon_acc() == null 
														? imageWebServer + "/"  
															+ accessory
																.getPath() 
															+ "/"
															+ accessory
																.getName() 
															+ "_small." 
															+ accessory
																	.getExt()
														: imageWebServer 
															+ "/" 
															+ grandsonGoodsClass.getIcon_acc()
																.getPath() 
															+ "/"
															+ grandsonGoodsClass.getIcon_acc()
																.getName() 
															+ "_small." 
															+ grandsonGoodsClass.getIcon_acc()
																.getExt());
							
							dmap.put("gc_name", grandsonGoodsClass.getClassName());
							dmap.put("gc_id", grandsonGoodsClass.getId());
							if(grandsonGoodsClass.getIcon_acc() == null ){
								grandsonGoodsClass.setIcon_acc(accessory);
								this.goodsClassService.update(grandsonGoodsClass);
							}
						}
						if(!dmap.isEmpty()){
							dList.add(dmap);
						}
					}
					if(dList.size() > 0){
						gcsmap.put("childList", dList); 
						gcsmap.put("gc_id", childGoodsClass.getId());
						gcsmap.put("gc_name", childGoodsClass.getClassName());
						gcslist.add(gcsmap);
					}
				}
			}
			map.put("gcslist", gcslist);
			result = new Result(0, "Suucessfully", map);
		}else{
			result = new Result(1, "parameter error");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}
	
	
	/**
	 * APP-查询二级类目下最热销商品
	 * @param request
	 * @param response
	 * @param gc_id
	 * @return
	 */
	public Accessory gc_hot_goods(HttpServletRequest request,
			HttpServletResponse response, String gc_id){
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Accessory accessory = null;
		if(gc_id == null && gc_id.equals("")){
			return null;
		}else{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("goods_status", 0);
			params.put("gc_id", CommUtil.null2Long(gc_id));
			List<Goods> goods = this.goodsService.query("select new Goods(id, goods_main_photo) from Goods "
					+ ""
					+ "obj where obj.goods_status=:goods_status "
					+ "and obj.goods_store.store_status=15 "
					+ "and obj.gc.id=:gc_id", params, 0, 1);
			Map<String, Object> map = new HashMap<String, Object>();
	 		for(Goods obj : goods){
	 			map.put("goods_id", obj.getId());
	 			map.put("goods_img", obj.getGoods_main_photo() != null
						? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
								+ obj.getGoods_main_photo().getName() + "_small." + obj.getGoods_main_photo().getExt() 
						: imageWebServer
								+ "/"
								+ this.configService.getSysConfig()
										.getGoodsImage().getPath()
								+ "/"
								+ this.configService.getSysConfig()
										.getGoodsImage().getName());
	 			 accessory = obj.getGoods_main_photo();
	 		}
 		return accessory;
		}
	}
	
	
	/**
	 * 二三级 移动端 弃用
	 * @param request
	 * @param response
	 * 弃用
	 */
	//@RequestMapping("/nav_queryGcs.json")				   
	public void nav1(HttpServletRequest request,HttpServletResponse response ){
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("display", true);
		List<GoodsClass> goodsClassList = this.gcService
				.query("select obj from GoodsClass obj where obj.parent.id is null and obj.display=:display order by obj.sequence asc",
						params, -1, -1);
		List<Map<String, Object>> GoodsClassList = new ArrayList<Map<String, Object>>();
		for(GoodsClass goodsclass : goodsClassList){
			Map<String, Object> goodsClassMap = new HashMap<String, Object>() ;
			goodsClassMap.put("gcid", goodsclass.getId());
			goodsClassMap.put("gcname", goodsclass.getClassName());
			goodsClassMap.put("gcIcon_type", goodsclass.getIcon_type());
			goodsClassMap.put("gcimgname", goodsclass.getIcon_acc() == null ? "" : goodsclass.getIcon_acc().getName());
			List<Map<String, Object>> childsList = new ArrayList<Map<String, Object>>();
			for(GoodsClass childs : goodsclass.getChilds()){
				Map<String, Object> childsMap = new HashMap<String, Object>();
				childsMap.put("gcname", childs.getClassName());
				childsMap.put("gcdid", childs.getId());
				List<Map<String, Object>> grandchildList = new ArrayList<Map<String, Object>>();
				for(GoodsClass grandchild : childs.getChilds()){
					Map<String, Object> grandchildMap = new HashMap<String, Object>();
					grandchildMap.put("gcname", grandchild.getClassName());
					grandchildMap.put("gcdid", grandchild.getId());
					grandchildList.add(grandchildMap);
					childsMap.put("childsList2", grandchildList);
				}
				childsList.add(childsMap);
			}
			goodsClassMap.put("childList", childsList); 
			GoodsClassList.add(goodsClassMap);
			map.put("gcslist", GoodsClassList);
		}
		result = new Result(0,"Successfully", map);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().print(
					Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * pc 首页品牌数据 
	 * 默认十个
	 * @param request
	 * @param response
	 */
//	@RequestMapping("/index_brand.json")
	@RequestMapping("indexBrand.json")
	public void indexBrand(HttpServletRequest request,HttpServletResponse response){
		Result result = null;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("show_index",true);
		params.put("audit", 1);
		List<GoodsBrand> goodsbrands = this.goodsBrandService.query("select new GoodsBrand(id,name,brandLogo) from GoodsBrand obj where obj.show_index=:show_index and obj.audit=:audit order by obj.sequence asc ",
				params, 0, 10);
		List<Map<String, Object>> goodsBrandsList = new ArrayList<Map<String, Object>>(); 
		for(GoodsBrand goodsbrand : goodsbrands){
			Map<String, Object> goodsBrandMap = new HashMap<String, Object>();
			goodsBrandMap.put("brandimgpath", configService.getSysConfig().getImageWebServer() + "/" + goodsbrand.getBrandLogo().getPath()+"/"+goodsbrand.getBrandLogo().getName());
			goodsBrandMap.put("brandid", goodsbrand.getId());
			goodsBrandMap.put("brandname",goodsbrand.getName());
			goodsBrandsList.add(goodsBrandMap);
			map.put("goodsbrandlist", goodsBrandsList);
		}
		result = new Result(0, "Successfully", map);
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
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
