package com.metoo.app.view.web.tool;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.database.DatabaseTools;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;

@Component
public class MGoodsSimilarTools {

	private Integer pageSize = 20;// 默认分页数据，表示每页20条记录

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private MGoodsViewTools mgoodsViewTools;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private DatabaseTools databaseTools;

	public Set<Map> recommendGoods(ModelAndView mv, String currentPage, int remaining, Map similar_map,
			String language) {
		GoodsQueryObject gqo = new GoodsQueryObject(null, currentPage, mv, "weightiness", ",rand()");
		if (remaining != 0) {
			this.pageSize = remaining;
		}
		gqo.addQuery("obj.goods_store.store_status", new SysMap("store_status", 15), "=");
		gqo.addQuery("obj.goods_status", new SysMap("goods_status", 0), "=");
		gqo.addQuery("obj.store_recommend", new SysMap("goods_store_recommend", CommUtil.null2Boolean(true)), "=");
		// gqo.addQuery("obj.store_recommend", new
		// SysMap("goods_store_recommend",
		// CommUtil.null2Boolean(store_recommend)), "=");
		gqo.setPageSize(CommUtil.null2Int(this.pageSize));
		List<Goods> objList = new ArrayList<Goods>();
		Set<Long> similar_id = new HashSet();
		IPageList pList = this.goodsService.list(gqo);
		objList = pList.getResult();
		Set<Map> set = this.similar_query(objList, language);
		similar_map.put("result", set);
		similar_map.put("currentPage", pList.getCurrentPage());
		similar_map.put("Pages", pList.getPages());
		return set;
	}

	/**
	 * @description 商品属性查询
	 * @param objList
	 * @param resultMap
	 */
	public Set<Map> similar_query(List<Goods> goods_list, String language) {
		String imageWebServer = this.configService.getSysConfig().getImageWebServer();
		Set set = new HashSet();
		for (Goods obj : goods_list) {
			Map map = new HashMap();
			map.put("goods_id", obj.getId());
			map.put("goods_name", obj.getGoods_name());
			if ("1".equals(language)) {
				map.put("goods_name", obj.getKsa_goods_name() != null && !"".equals(obj.getKsa_goods_name())
						? "^" + obj.getKsa_goods_name() : obj.getGoods_name());
			}
			map.put("goods_img",
					obj.getGoods_main_photo() != null ? imageWebServer + "/" + obj.getGoods_main_photo().getPath() + "/"
							+ obj.getGoods_main_photo().getName() + "_middle." + obj.getGoods_main_photo().getExt()
							: imageWebServer);
			map.put("goods_type", obj.getGoods_type());
			map.put("goods_well_evaluate", obj.getWell_evaluate());
			map.put("goods_price", obj.getGoods_price());
			map.put("goods_current_price", obj.getGoods_current_price());
			map.put("goods_discount_rate", obj.getGoods_discount_rate());
			map.put("goods_collect", obj.getGoods_collect());
			map.put("goods_status", obj.getGoods_status());
			map.put("store_status", obj.getGoods_store().getStore_status());
			set.add(map);
		}
		return set;
	}

	/**
	 * @description 拼接sql 推荐商品
	 * @param currentPage
	 * @param keys
	 * @param values
	 * @param countMap
	 * @param appendMulitId
	 * @return
	 * @throws SQLException
	 */
	public Set<Map> key_value(String currentPage, List<String> keys, List<String> values, Map<String, Integer> countMap, String appendMulitId) throws SQLException{
		String sql = "";
		int num = 0;
		String[] appendIds = appendMulitId.split(",");
		List list = new ArrayList();//补充的商品id
		for(String id : appendIds){
			list.add(id);
		}
		for(String  vid : values){
			for(int i = 0; i < keys.size(); i ++){
				double amount = CommUtil.mul(currentPage, vid);
				if(CommUtil.subtract(countMap.get(keys.get(i)), amount) > 0){
					sql += this.union_sql(Long.parseLong(keys.get(i)), Integer.parseInt(vid)) + " union ";
					keys.remove(i);
					break;
				}else{
					for(int j = 0; j < list.size(); j ++ ){
						keys.remove(i);
						keys.add((String) list.get(j));
						list.remove(j);
						break;
					}
				}
			}
		}
		Set<Map> goodsSet = new HashSet<Map>();
		if(!sql.equals("")){
			String union_sql = StringUtils.substringBeforeLast(sql, "union");
			String query = getQuery(union_sql);
			String imageWebServer = this.configService.getSysConfig().getImageWebServer();
			ResultSet res = this.databaseTools.selectIn(query);
			Long goods_id;
			String name = "";	
			String ksa_name = "";
			String img;
			BigDecimal price;
			BigDecimal current_price;
			BigDecimal goods_discount_rate;
			int goods_status;
			int goods_collect;
			while(res.next()){
				Map goodsMap = new HashMap();
				goods_id = res.getLong("goods_vice_id");
				name = res.getString("goods_name");
				ksa_name = res.getString("ksa_goods_name");
				price = res.getBigDecimal("goods_price");
				current_price = res.getBigDecimal("goods_current_price");
				goods_status = res.getInt("goods_status");
				goods_discount_rate = res.getBigDecimal("goods_discount_rate");
				goodsMap.put("goods_id", goods_id);
				goodsMap.put("goods_name", ksa_name != null? ksa_name : name);
				goodsMap.put("goods_price", price);
				goodsMap.put("goods_current_price", current_price);
				goodsMap.put("goods_discount_rate", goods_discount_rate);
				goodsMap.put("goods_status", goods_status);
				Accessory acc = this.accessoryService.getObjById(res.getLong("goods_main_photo_id"));
				if(acc != null){
					goodsMap.put("goods_img", imageWebServer + "/" + acc.getPath() +"/"+ acc.getName());
				}
				goodsSet.add(goodsMap);
			}
		}
		return goodsSet;
	}

	/**
	 * @description
	 * @param sql
	 * @return
	 */
	public String getQuery(String sql) {
		String query = "select goods_vice_id,goods_name,ksa_goods_name,goods_price,goods_current_price,be_similar,goods_status,goods_main_photo_id,goods_discount_rate from( "
				+ sql + " ) tablen order by rand()";
		return query;
	}

	/**
	 * @description 拼接子查询
	 * @param id
	 * @param number
	 * @return
	 */
	public String union_sql(Long id, int number) {
		// rand() limit + number
		String union_sql = "select goods_vice_id,goods_name, ksa_goods_name,goods_price,goods_current_price,be_similar,goods_status,goods_main_photo_id,goods_discount_rate from ("
				+ "select goods_vice_id,goods_name, ksa_goods_name,goods_price,goods_current_price,be_similar,goods_status,goods_main_photo_id,goods_discount_rate from metoo_goods_similar "
				+ "inner join " + "metoo_goods on metoo_goods.id=metoo_goods_similar.goods_vice_id "
				+ "inner join metoo_store on metoo_store.id=metoo_goods.goods_store_id " + "where goods_status=0 "
				+ "and store_status=15 " + "and metoo_goods.store_recommend=1 " + "and be_similar > 0.9 and "
				+ "goods_main_id=" + id + " order by metoo_goods.addTime desc" + ") table1";
		return union_sql;
	}

}
