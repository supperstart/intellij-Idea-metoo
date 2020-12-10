package com.metoo.module.weixin.view.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.view.web.tools.BuyGiftViewTools;
import com.metoo.view.web.tools.GoodsViewTools;
@Component
public class OrderPayTools {

	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private BuyGiftViewTools buyGiftViewTools;

	public void update_goods_inventory(OrderForm order) {
		try {
			// 付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
			List<Goods> goods_list = this.orderFormTools.queryOfGoods(CommUtil
					.null2String(order.getId()));
			for (Goods goods : goods_list) {
				int goods_count = this.orderFormTools.queryOfGoodsCount(
						CommUtil.null2String(order.getId()),
						CommUtil.null2String(goods.getId()));
				if (goods.getGroup() != null && goods.getGroup_buy() == 2) {
					for (GroupGoods gg : goods.getGroup_goods_list()) {
						if (gg.getGroup().getId().equals(goods.getGroup().getId())) {
							gg.setGg_count(gg.getGg_count() - goods_count);
							gg.setGg_selled_count(gg.getGg_selled_count()
									+ goods_count);
							this.groupGoodsService.update(gg);
							// 更新lucene索引
							String goods_lucene_path = System
									.getProperty("user.dir")
									+ File.separator
									+ "luence" + File.separator + "groupgoods";
							File file = new File(goods_lucene_path);
							if (!file.exists()) {
								CommUtil.createFolder(goods_lucene_path);
							}
							LuceneUtil lucene = LuceneUtil.instance();
							lucene.setIndex_path(goods_lucene_path);
							lucene.update(CommUtil.null2String(goods.getId()),
									luceneVoTools.updateGroupGoodsIndex(gg));
						}
					}
				}
				List<String> gsps = new ArrayList<String>();
				List<GoodsSpecProperty> temp_gsp_list = this.orderFormTools
						.queryOfGoodsGsps(CommUtil.null2String(order.getId()),
								CommUtil.null2String(goods.getId()));
				String spectype = "";
				if (null != temp_gsp_list && 0 < temp_gsp_list.size()) {
					
					for (GoodsSpecProperty gsp : temp_gsp_list) {
						
						if (null != gsp) {
							gsps.add(gsp.getId().toString());
							spectype += gsp.getSpec().getName() + ":" + gsp.getValue()
									+ " ";
						}
						
					}
				}
				
				String[] gsp_list = new String[gsps.size()];
				gsps.toArray(gsp_list);
				goods.setGoods_salenum(goods.getGoods_salenum() + goods_count);
				GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(goods
						.getId());
				todayGoodsLog.setGoods_salenum(todayGoodsLog.getGoods_salenum()
						+ goods_count);

				Map<String, Integer> logordermap = (Map<String, Integer>) Json
						.fromJson(todayGoodsLog.getGoods_order_type());
				String ordertype = order.getOrder_type();
				if (logordermap.containsKey(ordertype)) {
					logordermap.put(ordertype, logordermap.get(ordertype)
							+ goods_count);
				} else {
					logordermap.put(ordertype, goods_count);
				}
				todayGoodsLog.setGoods_order_type(Json.toJson(logordermap,
						JsonFormat.compact()));

				Map<String, Integer> logspecmap = (Map<String, Integer>) Json
						.fromJson(todayGoodsLog.getGoods_sale_info());

				if (logspecmap.containsKey(spectype)) {
					logspecmap
							.put(spectype, logspecmap.get(spectype) + goods_count);
				} else {
					logspecmap.put(spectype, goods_count);
				}
				todayGoodsLog.setGoods_sale_info(Json.toJson(logspecmap,
						JsonFormat.compact()));

				this.goodsLogService.update(todayGoodsLog);
				String inventory_type = goods.getInventory_type() == null ? "all"
						: goods.getInventory_type();
				boolean inventory_warn = false;
				if (inventory_type.equals("all")) {
					goods.setGoods_inventory(goods.getGoods_inventory()
							- goods_count);
					if (goods.getGoods_inventory() <= goods
							.getGoods_warn_inventory()) {
						inventory_warn = true;
					}
				} else {
					List<HashMap> list = Json
							.fromJson(ArrayList.class, CommUtil.null2String(goods
									.getGoods_inventory_detail()));
					for (Map temp : list) {
						String[] temp_ids = CommUtil.null2String(temp.get("id"))
								.split("_");
						Arrays.sort(temp_ids);
						Arrays.sort(gsp_list);
						if (Arrays.equals(temp_ids, gsp_list)) {
							temp.put("count", CommUtil.null2Int(temp.get("count"))
									- goods_count);
							if (CommUtil.null2Int(temp.get("count")) <= CommUtil
									.null2Int(temp.get("supp"))) {
								inventory_warn = true;
							}
						}
					}
					goods.setGoods_inventory_detail(Json.toJson(list,
							JsonFormat.compact()));
				}
				for (GroupGoods gg : goods.getGroup_goods_list()) {
					if (gg.getGroup().getId().equals(goods.getGroup().getId())
							&& gg.getGg_count() == 0) {
						goods.setGroup_buy(3);// 标识商品的状态为团购数量已经结束
					}
				}
				if (inventory_warn) {
					goods.setWarn_inventory_status(-1);// 该商品库存预警状态
				}
				this.goodsService.update(goods);
				// 更新lucene索引
				String goods_lucene_path = System.getProperty("metoob2b2c.root")
						+ File.separator + "luence" + File.separator + "goods";
				File file = new File(goods_lucene_path);
				if (!file.exists()) {
					CommUtil.createFolder(goods_lucene_path);
				}
				LuceneUtil lucene = LuceneUtil.instance();
				lucene.setIndex_path(goods_lucene_path);
				lucene.update(CommUtil.null2String(goods.getId()),
						luceneVoTools.updateGoodsIndex(goods));
			}
			// 判断是否有满就送如果有则进行库存操作
			if (order.getWhether_gift() == 1) {
				this.buyGiftViewTools.update_gift_invoke(order);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
}
