package com.metoo.lucene.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Accessory;
import com.metoo.foundation.domain.Activity;
import com.metoo.foundation.domain.BuyGift;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.GroupLifeGoods;
import com.metoo.foundation.service.IActivityService;
import com.metoo.foundation.service.IBuyGiftService;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.lucene.LuceneVo;
import com.metoo.view.web.tools.GroupViewTools;

/**
 * 
 * <p>
 * Title: LuceneUpdateTools.java
 * </p>
 * 
 * <p>
 * Description: lucene更新时的调用方法
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 湖南创发科技有限公司 www.koala.com
 * </p>
 * 
 * @author jinxinzhe,jy
 * 
 * @date 2014-6-5
 * 
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Component
public class LuceneVoTools {
	@Autowired
	private GroupViewTools groupViewTools;
	@Autowired
	private IActivityService activityService;
	@Autowired
	private IEnoughReduceService enoughReduceService;
	@Autowired
	private IBuyGiftService buyGiftService;
	@Autowired
	private MGoodsViewTools mgoodsViewTools;

	/**
	 * 封装商品信息到lucene索引
	 * 
	 * @param goods
	 * @return lucene索引
	 */
	public LuceneVo updateGoodsIndex(Goods goods) {
		LuceneVo vo = new LuceneVo();
		vo.setVo_id(goods.getId());
		vo.setVo_title(goods.getGoods_name());
		vo.setVo_content(goods.getGoods_details());
		vo.setVo_arabic_title(goods.getKsa_goods_name());
		vo.setVo_arabic_content(goods.getKsa_goods_detail());
		vo.setVo_type("goods");
		vo.setVo_store_price(CommUtil.null2Double(goods.getStore_price()));
		vo.setVo_cost_price(CommUtil.null2Double(goods.getGoods_price()));
		vo.setVo_curr_price(CommUtil.null2Double(goods.getGoods_current_price()));
		vo.setVo_add_time(goods.getAddTime().getTime());
		vo.setVo_goods_salenum(goods.getGoods_salenum());
		vo.setVo_goods_collect(goods.getGoods_collect());
		vo.setVo_well_evaluate(CommUtil.null2Double(goods.getWell_evaluate()));
		vo.setVo_goods_inventory(goods.getGoods_inventory());
		vo.setVo_goods_type(goods.getGoods_type());

		if (goods.getGoods_brand() != null) {
			vo.setVo_goods_brandname(goods.getGoods_brand().getName());
		}
		if (goods.getGoods_main_photo() != null) {
			vo.setVo_main_photo_url(
					goods.getGoods_main_photo().getPath() + "/" + goods.getGoods_main_photo().getName());
		}
		if (goods.getGoods_store() != null && goods.getGoods_store().getUser() != null) {
			vo.setVo_store_username(goods.getGoods_store().getUser().getUserName());
			vo.setVo_store_id(goods.getGoods_store().getId());
		}
		List<String> list = new ArrayList<String>();
		for (Accessory obj : goods.getGoods_photos()) {
			list.add(obj.getPath() + obj.getName());
		}
		String str = Json.toJson(list, JsonFormat.compact());
		vo.setVo_photos_url(str);
		vo.setVo_goods_evas(goods.getEvaluates().size());
		if (goods.getGc().getLevel() == 2) {
			vo.setVo_goods_class(CommUtil.null2String(goods.getGc().getParent().getId()) + "_"
					+ CommUtil.null2String(goods.getGc().getId()));
		} else {
			vo.setVo_goods_class(CommUtil.null2String(goods.getGc().getId()) + "_");
		}
		vo.setVo_goods_transfee(String.valueOf(goods.getGoods_transfee()));
		vo.setVo_goods_cod(goods.getGoods_cod());
		if (goods.getOrder_enough_give_status() == 1) {
			vo.setVo_whether_active(1);
		}
		if (goods.getOrder_enough_give_status() == 0) {
			vo.setVo_whether_active(0);
		}
		// 查询商品正在进行的优惠活动
		// 0为无活动 1为团购,2为活动，3为满送，4为满减，5为组合，6为F码，7为预售。
		int active = 0;
		Date nowDate = new Date();
		if (goods.getGroup_buy() == 2) {
			if (goods.getGroup().getBeginTime().before(nowDate)) {
				active = 1;
			}
		}
		if (goods.getActivity_status() == 2) {
			Activity ac = this.activityService.getObjById(CommUtil.null2Long(goods.getActivity_goods_id()));
			if (ac != null && ac.getAc_begin_time().before(nowDate)) {
				active = 2;
			}
		}
		if (goods.getOrder_enough_give_status() == 1) {
			BuyGift bg = this.buyGiftService.getObjById(goods.getBuyGift_id());
			if (bg != null && bg.getBeginTime().before(nowDate)) {
				active = 3;
			}
		}
		if (goods.getEnough_reduce() == 1) {
			EnoughReduce er = this.enoughReduceService
					.getObjById(CommUtil.null2Long(goods.getOrder_enough_reduce_id()));
			if (er.getErbegin_time().before(nowDate)) {
				active = 4;
			}
		}
		if (goods.getCombin_status() == 1) {
			active = 5;
		}
		if (goods.getF_sale_type() == 1) {
			active = 6;
		}
		if (goods.getAdvance_sale_type() == 1) {
			active = 7;
		}
		vo.setVo_whether_active(active);
		vo.setVo_f_sale_type(goods.getF_sale_type());
		vo.setVo_goods_serial(goods.getGoods_serial());
		vo.setVo_rate(goods.getGoods_discount_rate().toString());
		return vo;
	}

	/**
	 * 设置生活类团购商品的索引
	 * 
	 * @param goods
	 * @return LuceneVo
	 */
	public LuceneVo updateLifeGoodsIndex(GroupLifeGoods goods) {
		LuceneVo vo = new LuceneVo();
		vo.setVo_id(goods.getId());
		vo.setVo_title(goods.getGg_name());
		vo.setVo_content(goods.getGroup_details());
		vo.setVo_type("lifegoods");
		vo.setVo_store_price(CommUtil.null2Double(goods.getGroup_price()));
		vo.setVo_add_time(goods.getAddTime().getTime());
		vo.setVo_goods_salenum(goods.getGroupInfos().size());
		if (goods.getGroup_acc() != null) {
			vo.setVo_main_photo_url(goods.getGroup_acc().getPath() + "/" + goods.getGroup_acc().getName());
		}
		vo.setVo_cost_price(CommUtil.null2Double(goods.getCost_price()));
		vo.setVo_cat(goods.getGg_gc().getId().toString());
		String rate = this.groupViewTools
				.getRate(CommUtil.null2Double(goods.getGroup_price()), CommUtil.null2Double(goods.getCost_price()))
				.toString();
		vo.setVo_rate(rate);
		if (goods.getGg_ga() != null) {
			vo.setVo_goods_area(goods.getGg_ga().getId().toString());
		}

		return vo;
	}

	/**
	 * 设置团购商品的索引
	 * 
	 * @param goods
	 * @return LuceneVo
	 */
	public LuceneVo updateGroupGoodsIndex(GroupGoods goods) {
		LuceneVo vo = new LuceneVo();
		vo.setVo_id(goods.getId());
		vo.setVo_title(goods.getGg_name());
		vo.setVo_content(goods.getGg_content());
		vo.setVo_type("lifegoods");
		vo.setVo_store_price(CommUtil.null2Double(goods.getGg_price()));
		vo.setVo_curr_price(CommUtil.null2Double(goods.getGg_price()));
		vo.setVo_add_time(goods.getAddTime().getTime());
		vo.setVo_goods_salenum(goods.getGg_selled_count());
		vo.setVo_cost_price(CommUtil.null2Double(goods.getGg_goods().getGoods_price()));
		if (goods.getGg_img() != null) {
			vo.setVo_main_photo_url(goods.getGg_img().getPath() + "/" + goods.getGg_img().getName());
		}
		vo.setVo_cat(goods.getGg_gc().getId().toString());
		vo.setVo_rate(CommUtil.null2String(goods.getGg_rebate()));
		if (goods.getGg_ga() != null) {
			vo.setVo_goods_area(goods.getGg_ga().getId().toString());
		}
		return vo;
	}
}
