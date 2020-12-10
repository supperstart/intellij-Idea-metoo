package com.metoo.pc.lucene.view.action;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.lucene.LuceneResult;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.LuceneVo;

@Controller
@RequestMapping("/pc/")
public class PSearchViewAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IStoreService storeService;
	
	
	/**
	 * 用于PC端搜索
	 * @param request
	 * @param response
	 * @param gc_id
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param goods_type
	 * @param goods_inventory
	 * @param keyword
	 * @param goods_transfee
	 * @param goods_cod
	 * @param searchType
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/search.json")
	public void wapsearch(HttpServletRequest request,
			HttpServletResponse response, String gc_id, String currentPage,
			String orderBy, String orderType, String goods_type,
			String goods_inventory, String keyword, String goods_transfee,
			String goods_cod,String searchType) throws UnsupportedEncodingException {
		if(goods_type.equals("")){
			goods_type = "1";
		}
		Result result = null;
		Map seachmap = new HashMap();
		//response.addCookie(search_history_cookie(request, keyword));
		ModelAndView mv = new JModelAndView("lucene/search_goods_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		
		ModelAndView storeMv = new JModelAndView("supplier_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request,
				response);
		
	/*	if("store".equals(searchType) ){
			orderBy = "sort_place";
			orderType = "asc";
			StoreQueryObject qo = new StoreQueryObject(currentPage, mv, orderBy,
					orderType);
			Map params = new HashMap();
			params.put("store_status", 15);//正常营业的店铺
			
			qo.setPageSize(5);
			qo.addQuery("obj.store_status = :store_status", params);
			if(!"".equals(keyword) && keyword != null){
				params.put("store_name", keyword);
				qo.addQuery("obj.store_name", new SysMap("store_name", "%"
						+ keyword + "%"), "like");
			}
			
			IPageList pList = this.storeService.list(qo);
			CommUtil.saveIPageList2ModelAndView("", "", null, pList, storeMv);
			
			storeMv.addObject("orderBy", orderBy);
			storeMv.addObject("orderType", orderType);
			storeMv.addObject("store_name", keyword);
			storeMv.addObject("keyword", keyword);
			storeMv.addObject("searchType", "store");
			storeMv.addObject("gcTools", gcTools);
			storeMv.addObject("goodsTools", goodsTools);
			return storeMv;
		}*/
		// 将关键字加入用户的搜索历史中
		if (keyword != null && !keyword.equals("")) {

			// 根据店铺SEO关键字，查出关键字命中的店铺
			if (keyword != null && !keyword.equals("") && keyword.length() > 1) {
				mv.addObject("stores", search_stores_seo(keyword));
			}
			String path = System.getProperty("metoob2b2c.root") + File.separator
					+ "luence" + File.separator + "goods";
			LuceneUtil lucene = LuceneUtil.instance();
			lucene.setIndex_path(path);
			List temp_list = this.goodsClassService.query(
					"select obj.id from GoodsClass obj", null, -1, -1);
			lucene.setGc_size(temp_list.size());
			boolean order_type = true;
			String order_by = "";
			Sort sort = null;
			String query_gc = "";
			// 处理排序方式
			if (CommUtil.null2String(orderBy).equals("goods_salenum")) {
				order_by = "goods_salenum";
				sort = new Sort(new SortField(order_by, SortField.Type.INT,
						order_type));
			}
			if (CommUtil.null2String(orderBy).equals("evaluate_count")) {
				order_by = "evaluate_count";
				sort = new Sort(new SortField(order_by, SortField.Type.INT,
						order_type));
			}
			if (CommUtil.null2String(orderBy).equals("goods_collect")) {
				order_by = "goods_collect";
				sort = new Sort(new SortField(order_by, SortField.Type.INT,
						order_type));
			}
			if (CommUtil.null2String(orderBy).equals("well_evaluate")) {
				order_by = "well_evaluate";
				sort = new Sort(new SortField(order_by, SortField.Type.DOUBLE,
						order_type));
			}
			if (CommUtil.null2String(orderType).equals("asc")) {
				order_type = false;
			}
			if (CommUtil.null2String(orderType).equals("")) {
				orderType = "desc";
			}
			if (CommUtil.null2String(orderBy).equals("goods_current_price")) {
				order_by = "store_price";
				sort = new Sort(new SortField(order_by, SortField.Type.DOUBLE,
						order_type));
			}
			if (gc_id != null && !gc_id.equals("")) {
				GoodsClass gc = this.goodsClassService.getObjById(CommUtil
						.null2Long(gc_id));
				query_gc = gc.getLevel() == 1 ? gc_id + "_*" : CommUtil
						.null2String(gc.getParent().getId()) + "_" + gc_id;
				mv.addObject("gc_id", gc_id);
			}
			LuceneResult pList = null;
			if (sort != null) {
				pList = lucene.search(keyword, CommUtil.null2Int(currentPage),
						goods_inventory, goods_type, query_gc, goods_transfee,
						goods_cod, sort, null, null, null);
			} else {
				pList = lucene.search(keyword, CommUtil.null2Int(currentPage),
						goods_inventory, goods_type, query_gc, goods_transfee,
						goods_cod, null, null, null);
			}

			CommUtil.saveLucene2ModelAndView(pList, mv);

			List<LuceneVo> luceneVos = pList.getVo_list();
			List<Map> lunceneMaps = new ArrayList<Map>();
			for(LuceneVo lucenevo:luceneVos){
				Map lunmap = new HashMap();
				lunmap.put("goodsid", lucenevo.getVo_id());
				lunmap.put("goodsname", lucenevo.getVo_title());
				lunmap.put("goodinventory", lucenevo.getVo_goods_inventory());
				lunmap.put("vo_well_evaluate", lucenevo.getVo_well_evaluate() == 0.0 ? 0 : lucenevo.getVo_well_evaluate());
				lunmap.put("goods_price", lucenevo.getVo_cost_price());
				lunmap.put("goodscurrprice", lucenevo.getVo_curr_price());
				lunmap.put("store_price", lucenevo.getVo_store_price());
				lunmap.put("main_photo_url", this.configService.getSysConfig().getImageWebServer()+"/"+lucenevo.getVo_main_photo_url());
				lunmap.put("store_name", lucenevo.getVo_store_username());
				lunmap.put("goods_serial", lucenevo.getVo_goods_serial());
				lunceneMaps.add(lunmap);
			}
			seachmap.put("lucen", lunceneMaps);
			seachmap.put("goods_currentPage", pList.getCurrentPage());
			seachmap.put("goods_Pages", pList.getPages());
			
			// 对关键字命中的商品进行分类提取
	/*		Set<String> list_gcs = lucene.LoadData_goods_class(keyword);
			// 对商品分类数据进行分析加载,只查询id和className
			List<GoodsClass> gcs = this.query_GC_second(list_gcs);

			mv.addObject("list_gc", list_gcs);
			mv.addObject("gcs", gcs);
			mv.addObject("allCount", pList.getRows());*/
		}
	/*	mv.addObject("keyword", keyword);
		mv.addObject("searchType", "goods");
		mv.addObject("orderBy", orderBy);
		mv.addObject("orderType", orderType);
		mv.addObject("goods_type", goods_type);
		mv.addObject("goods_inventory", goods_inventory);
		mv.addObject("goods_transfee", goods_transfee);
		mv.addObject("goods_cod", goods_cod);
		mv.addObject("goodsViewTools", goodsViewTools);
		mv.addObject("userTools", userTools);*/
		// 加载页面上其它的商品信息，最近浏览，猜你喜欢，推广热卖，直通车。
		/*this.search_other_goods(mv);
		// 处理系统商品对比信息
		List<Goods> goods_compare_list = (List<Goods>) request
				.getSession(false).getAttribute("goods_compare_cart");
		// 计算商品对比中第一间商品的分类，只允许对比同一个分类的商品
		if (goods_compare_list == null) {
			goods_compare_list = new ArrayList<Goods>();
		}
		int compare_goods_flag = 0;// 默认允许对比商品，如果商品分类不一致曾不允许对比
		for (Goods compare_goods : goods_compare_list) {
			if (compare_goods != null) {
				compare_goods = this.goodsService.getObjById(compare_goods
						.getId());
				if (!compare_goods.getGc().getParent().getParent().getId()
						.equals(CommUtil.null2Long(gc_id))) {
					compare_goods_flag = 1;
				}
			}
		}
		mv.addObject("compare_goods_flag", compare_goods_flag);
		mv.addObject("goods_compare_list", goods_compare_list);*/
	
		result = new Result(3200,"查询成功",seachmap);
		String seachtemp = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(seachtemp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	/**
	 * 根据店铺SEO关键字，查出关键字命中的店铺
	 * 
	 * @param keyword
	 * @return
	 */
	public List<Store> search_stores_seo(String keyword) {
		Map params = new HashMap();
		params.put("keyword1", keyword);
		params.put("keyword2", keyword + ",%");
		params.put("keyword3", "%," + keyword + ",%");
		params.put("keyword4", "%," + keyword);
		List<Store> stores = this.storeService
				.query("select obj from Store obj where obj.store_seo_keywords =:keyword1 or obj.store_seo_keywords like:keyword2 or obj.store_seo_keywords like:keyword3 or obj.store_seo_keywords like:keyword4",
						params, 0, 3);
		Collections.sort(stores, new Comparator() {
			public int compare(Object o1, Object o2) {
				Store store1 = (Store) o1;
				Store store2 = (Store) o2;
				int l1 = store1.getStore_seo_keywords().split(",").length;
				int l2 = store2.getStore_seo_keywords().split(",").length;
				if (l1 > l2) {
					return 1;
				}
				;
				if (l1 == l2) {
					if (store1.getPoint().getStore_evaluate()
							.compareTo(store2.getPoint().getStore_evaluate()) == 1) {
						return -1;
					}
					;
					if (store1.getPoint().getStore_evaluate().compareTo(store2.getPoint().getStore_evaluate()) == -1) {
						return 1;
					}
					;
					return 0;
				}
				return -1;
			}
		});
		return stores;
	}
}
