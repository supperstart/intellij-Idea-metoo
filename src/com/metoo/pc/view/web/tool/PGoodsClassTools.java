package com.metoo.pc.view.web.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.service.ISysConfigService;

@Component
public class PGoodsClassTools {
	@Autowired
	private ISysConfigService configService;

	public List info(List<GoodsClass> gcs) {
		List<Map> list = new ArrayList<Map>();
		for (GoodsClass obj : gcs) {
			Map map = new HashMap();
			map.put("id", obj.getId());
			map.put("class_name", obj.getClassName());
			map.put("class_Icon_acc",
					obj.getIcon_acc() == null ? ""
							: this.configService.getSysConfig().getImageWebServer() + "/" + obj.getIcon_acc().getPath()
									+ "/" + obj.getIcon_acc().getName());
			list.add(map);
		}
		return list;
	}

	public String[] generic_goods_class_info(GoodsClass gc) {
		if (gc != null) {
			String goods_class_info = this.generic_the_goods_class_info(gc);
			String[] cls = goods_class_info.split(">");
			return cls;
		} else
			return null;
	}

	private String generic_the_goods_class_info(GoodsClass gc) {
		if (gc != null) {
			String goods_class_info = gc.getClassName() + "," + gc.getId() + ">";
			if (gc.getParent() != null) {
				String class_info = generic_the_goods_class_info(gc.getParent());
				goods_class_info = class_info + goods_class_info;
			}

			return goods_class_info;
		} else
			return "";
	}

}
