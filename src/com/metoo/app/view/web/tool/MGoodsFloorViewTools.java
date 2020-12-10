package com.metoo.app.view.web.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IGoodsService;
@Component
public class MGoodsFloorViewTools {
	@Autowired
	private IGoodsService goodsService;
	
	public void generic_goods(String json,Map goodsfloorchildsName) {
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
					List<Map> goodslist = new ArrayList<Map>(); 
					for(Goods goods:goods_list){
						goodsfloorchildsName.put("goods_main_photo", goods.getGoods_main_photo());
						goodsfloorchildsName.put("path", goods.getGoods_main_photo().getPath());
						goodsfloorchildsName.put("name", goods.getGoods_main_photo().getName());
						goodsfloorchildsName.put("ext", goods.getGoods_main_photo().getExt());
						goodsfloorchildsName.put("id", goods.getGoods_main_photo().getId());
						
						try {
							goodsfloorchildsName.put("store_second_domain", goods.getGoods_store().getStore_second_domain());
						} catch (Exception NullPointerException) {
							// TODO Auto-generated catch block
							goodsfloorchildsName.put("store_second_domain", "");						}
					
						goodsfloorchildsName.put("goods_type", goods.getGoods_name());
						goodsfloorchildsName.put("well_evaluate", goods.getWell_evaluate());
						goodsfloorchildsName.put("eva_count", goods.getEvaluate_count());
						goodsfloorchildsName.put("goods_price", goods.getGoods_price());
						goodsfloorchildsName.put("goods_current_price", goods.getGoods_current_price());
					}
					
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
