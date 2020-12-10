package com.metoo.app.view.web.thread;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.springframework.stereotype.Component;


import com.metoo.core.beans.Assert;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Goods;

/**
 * 
 * <p>
 * Title: GoodsThreadPool.java
 * </p>
 * 
 * <p>
 * Description: 商品更新线程池;默认为单线程池。Executors可创建各类线程池 JDK1.5。
 * </p>
 * 
 * <p>
 * Company: 觅通科技
 * </p>
 * 
 * @author 46075
 *
 */
@Component
public class GoodsThreadPool {
	ExecutorService fixedThreadPool = Executors.newSingleThreadExecutor();// 创建单线程池
	// ExecutorService fixedThreadPool = Executors.newCachedThreadPool();//
	private static GoodsThreadPool pool = new GoodsThreadPool();// 创建单例

	/**
	 * @description 获取一个单例
	 * @return
	 */
	public static GoodsThreadPool getInstance() {
		return pool;
	}

	public void addThread(Runnable run) {
		fixedThreadPool.execute(run);
	}

	/**
	 * @description 获取线程池对象
	 * @return
	 */
	public ExecutorService getService() {
		return fixedThreadPool;
	}

	/**
	 * @description 商品更新工具类
	 * @param obj
	 * @return
	 */
	public Goods updateGoods(Goods obj) {
		List<CGoods> cgoodsList = obj.getCgoods();
		if (cgoodsList.size() > 0) {
			List goods_price_list = new ArrayList();
			List goods_current_price_list = new ArrayList();
			for (CGoods cobj : cgoodsList) {
				goods_price_list.add(cobj.getGoods_price());
				goods_current_price_list.add(cobj.getDiscount_price());
			}
			BigDecimal goods_price = CommUtil.null2BigDecimal(Collections.min(goods_price_list));
			BigDecimal store_price = CommUtil.null2BigDecimal(Collections.min(goods_current_price_list));
			double subtract = CommUtil.subtract(goods_price, store_price);
			double div = CommUtil.div(subtract, goods_price);
			double mul = CommUtil.mul(div, 100);
			BigDecimal e = new BigDecimal(mul);
			if (obj.getGoods_price() == null) {
				obj.setGoods_price(goods_price);
			}
			if (obj.getGoods_current_price() == null || obj.getStore_price() == null) {
				obj.setGoods_current_price(store_price);
				obj.setStore_price(store_price);
			}
			obj.setGoods_discount_rate(CommUtil.null2BigDecimal(e));
			// System.out.println(goodsService.update(obj));

		} else {
			BigDecimal goods_price = obj.getGoods_price();
			BigDecimal store_price = obj.getGoods_current_price();
			double subtract = CommUtil.subtract(goods_price, store_price);
			double div = CommUtil.div(subtract, goods_price);
			double mul = CommUtil.mul(div, 100);
			BigDecimal e = new BigDecimal(mul);
			obj.setGoods_discount_rate(CommUtil.null2BigDecimal(e));
			// System.out.println(goodsService.update(obj));
		}
		return obj;
	}

	/**
	 * @description 更新deals
	 * @param obj
	 * @return
	 */
	public Goods updateDealInventory(Goods obj) {
		Assert.notNull(obj);
		Random random = new Random();
		int inventory = random.nextInt(40-18+1)+18;
		obj.setStore_seckill_inventory((CommUtil.null2Int((new Double(inventory)).intValue())));
		obj.setStore_deals_inventory(50);
		return obj;
	}

	/**
	 * @description 更新商品折扣率
	 * @param obj
	 * @return
	 * @descript 获取sku中最低的当前价格and对应的sku原价
	 */
	public Goods updateGoodsDiscountRate(Goods obj) {
		BigDecimal price = new BigDecimal(0.00);
		BigDecimal current_price = new BigDecimal(0.00);
		if (obj.getInventory_type().equals("spec") && obj.getCgoods().size() > 0) {
			List<CGoods> cgoodsList = obj.getCgoods();
			Map<BigDecimal, BigDecimal> minmumPriceMap = new HashMap<BigDecimal, BigDecimal>();
			for (CGoods copyGoods : cgoodsList) {
				BigDecimal goods_price = copyGoods.getGoods_price();
				BigDecimal currnent_price = copyGoods.getDiscount_price();
				BigDecimal value = minmumPriceMap.get(currnent_price);
				if(value == null || CommUtil.subtract(value, goods_price) < 0){
					minmumPriceMap.put(currnent_price,
							goods_price);
				}
			}
			current_price = this.getMinKey(minmumPriceMap);
			price = minmumPriceMap.get(current_price);
		} else {
			price = obj.getGoods_price();
			current_price = obj.getGoods_current_price();
		}
		obj.setGoods_price(price);
		obj.setGoods_current_price(current_price);
		obj.setGoods_discount_rate(this.getGoodsRate(price, current_price));
		return obj;
	}
	
	public BigDecimal getMinKey(Map<BigDecimal, BigDecimal> map) {
		if (map == null)
			return null;
		Set<BigDecimal> set = map.keySet();
		Object[] obj = set.toArray();
		Arrays.sort(obj);
		return CommUtil.null2BigDecimal(obj[0]);
	}
	
	public BigDecimal getGoodsRate(BigDecimal goods_price, BigDecimal goods_current_price) {
		double ret = 0.0;
		if (!"".equals(CommUtil.null2String(goods_price)) && !"".equals(CommUtil.null2String(goods_current_price))) {
			double subtract = CommUtil.subtract(goods_price, goods_current_price);
			if (CommUtil.null2Float(goods_price) > 0)
				ret = CommUtil.null2BigDecimal(subtract).divide(goods_price, 2, BigDecimal.ROUND_DOWN).doubleValue();
		}
		DecimalFormat df = new DecimalFormat("0.00");
		Double re = CommUtil.mul(Double.valueOf(df.format(ret)), 100);
		// BigDecimal rate = new BigDecimal(re); //精准度问题 BigDecimal(String
		// val)构造是靠谱的
		BigDecimal rate = new BigDecimal(re.toString());
		return rate;
	}
}
