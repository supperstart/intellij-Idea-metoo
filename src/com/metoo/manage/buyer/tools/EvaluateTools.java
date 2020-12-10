package com.metoo.manage.buyer.tools;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGoodsSpecPropertyService;
import com.metoo.foundation.service.IIntegralGoodsOrderService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.service.IExpressInfoService;

/**
 * 
 * <p>
 * Title: MsgTools.java
 * </p>
 * 
 * <p>
 * Description: 评价工具类
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
 * @author hezeng
 * 
 * @date 2014-5-4
 * 
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Component
public class EvaluateTools {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsSpecPropertyService gspService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IExpressCompanyService expressCompanyService;
	@Autowired
	private IExpressInfoService expressInfoService;
	@Autowired
	private IIntegralGoodsOrderService integralGoodsOrderService;
	@Autowired
	private IEvaluateService evaluateService;

	/**
	 * 查询订单中某件是否评价
	 * 
	 * @param order_id
	 * @param goods_id
	 * @return
	 */
	public Evaluate query_order_evaluate(Object order_id, Object goods_id) {
		Map para = new HashMap();
		para.put("order_id", CommUtil.null2Long(order_id));
		para.put("goods_id", CommUtil.null2Long(goods_id));
		List<Evaluate> list = this.evaluateService
				.query("select obj from Evaluate obj where obj.evaluate_goods.id=:goods_id and obj.of.id=:order_id",
						para, -1, -1);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * 判断是否可修改评价
	 * 
	 * @param date
	 * @return
	 */
	public int evaluate_able(Date date) {
		if (date != null) {
			long begin = date.getTime();
			long end = new Date().getTime();
			SysConfig config = this.configService.getSysConfig();
			long day = (end - begin) / 86400000;
			if (day <= config.getEvaluate_edit_deadline()) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 判断是否可追加评价
	 * 
	 * @param date
	 * @return
	 */
	public int evaluate_add_able(Date date) {
		if (date != null) {
			long begin = date.getTime();
			long end = new Date().getTime();
			SysConfig config = this.configService.getSysConfig();
			long day = (end - begin) / 86400000;
			if (day <= config.getEvaluate_add_deadline()) {
				return 1;
			}
		}
		return 0;
	}
	
	public int evaluate_add_able1(Date date) {
		if (date != null) {
			long begin = date.getTime();
			long end = new Date().getTime();
			SysConfig config = this.configService.getSysConfig();
			long day = (end - begin) / 86400000;
			if (day <= config.getEvaluate_add_deadline()) {
				return 0;
			}
		}
		return 1;
	}
	

	/**
	 * 计算今天到指定时间天数
	 * 
	 * @param date
	 * @return
	 */
	public int how_soon(Date date) {
		if (date != null) {
			long begin = date.getTime();
			long end = new Date().getTime();
			long day = (end - begin) / 86400000;
			return CommUtil.null2Int(day);
		}
		return 999;
	}

}
