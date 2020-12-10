package com.metoo.manage.admin.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.foundation.domain.Area;
import com.metoo.foundation.service.IAreaService;

/**
 * 
 * <p>
 * Title: AreaManageTools.java
 * </p>
 * 
 * <p>
 * Description: 区域工具类,前端生成区域信息，使用ModelAndView封装到前端，velocity可以调用该类中的public方法
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
 * @date 2015-3-16
 * 
 * @version koala_b2b2c 2015
 */
@Component
public class AreaManageTools {
	@Autowired
	private IAreaService areaService;

	/**
	 * 根据区域生成区域信息字符串
	 * 
	 * @param area
	 * @return
	 */
	public String generic_area_info(Area area) {
		String area_info = "";
		if (area != null) {
			area_info = area.getAreaName() + " ";
			if (area.getParent() != null) {
				area_info = area.getParent().getAreaName() + area_info;
				if (area.getParent().getParent() != null) {
					area_info = area.getParent().getParent().getAreaName()
							+ area_info;
				}
			}
		}
		return area_info;
	}
}
