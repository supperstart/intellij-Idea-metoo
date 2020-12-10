package com.metoo.app.test;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.ISysConfigService;

@Controller
public class GoodsClassTest {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsClassService goodsClassService;
	
	@RequestMapping("/cla.json")
	public void etst() {
		GoodsClass class0 = this.goodsClassService.getObjById(CommUtil.null2Long(971));
		Long id0 = class0.getId();
		System.out.println(this.configService.getSysConfig().getImageWebServer() + "/" + class0.getIcon_acc().getPath() + "/" + class0.getIcon_acc().getName());
		if(class0.getChilds().size() > 0){
			Set<GoodsClass> classes = class0.getChilds();
			for(GoodsClass class1 : classes){
				Long id1 = class1.getId();
				System.out.println(this.configService.getSysConfig().getImageWebServer() + "/" + class1.getIcon_acc().getPath() + "/" + class1.getIcon_acc().getName());
				if(class1.getChilds().size() > 0){
					Set<GoodsClass> classes1 = class1.getChilds();
					for(GoodsClass class2 : classes1){
						Long id2 = class2.getId();
						System.out.println(this.configService.getSysConfig().getImageWebServer() + "/" + class2.getIcon_acc().getPath() + "/" + class2.getIcon_acc().getName());
					}
				}
			
			}
		}
	}

}
