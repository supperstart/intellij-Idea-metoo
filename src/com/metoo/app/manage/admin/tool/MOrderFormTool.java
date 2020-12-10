package com.metoo.app.manage.admin.tool;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.foundation.domain.OrderForm;


@Component
public class MOrderFormTool {
	


	/**
	 * @descript 查询订单列表种是否包含商品名称
	 * @param list
	 * @param name
	 * @return
	 */
	public boolean indexof(List<Map> list, String name) {
		for (Map map : list) {
			String goodsName = (String) map.get("goods_name");
			if (goodsName.toLowerCase().contains(name.toLowerCase())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}


}
