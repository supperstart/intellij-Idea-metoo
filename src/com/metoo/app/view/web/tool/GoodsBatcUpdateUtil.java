package com.metoo.app.view.web.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.view.web.thread.GoodsThreadPool;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IGoodsService;

/**
 * 
 * <p>
 * Title: GoodsMoreThreadDemo.java
 * </p>
 * 
 * <p>
 * 
 * Description: 商品更新 使用多线程
 * 批量处理;(问题：修改数量过,nginx访问超时;目前解决办法：增加nginx超时时间proxy_connect_timeout 90;
 * proxy_send_timeout 90; proxy_read_timeout 90;)
 * </p>
 *
 * 
 * @author 46075
 *
 */
@Controller
public class GoodsBatcUpdateUtil {

	private static GoodsBatcUpdateUtil gbu = null;// 商品更新属性单例 饿汉式单例模式

	private GoodsBatcUpdateUtil() {
	} // 构造函数设置为private，防止被new实例化

	public static GoodsBatcUpdateUtil getInstance() {
		if (gbu == null) {
			gbu = new GoodsBatcUpdateUtil();
		}
		return gbu;
	}

	// DCL双重检查锁定单例
	public static GoodsBatcUpdateUtil getInstance2() {
		if (gbu == null) {
			synchronized (GoodsBatcUpdateUtil.class) {
				if (gbu == null) {
					gbu = new GoodsBatcUpdateUtil();
				}
			}
		}
		return gbu;

	}

	/*
	 * private final static Executor executor =
	 * Executors.newCachedThreadPool();//启用多线程 (弃用，已封装)
	 * 
	 */
	@Autowired
	private IGoodsService goodsService;

	/**
	 * @description 多线程更新商品
	 * @param request
	 * @param response
	 * @获取GoodsThreadPool对象封装多线程
	 */
	@RequestMapping("/finalVariable.json")
	public void finalTest(HttpServletRequest request, HttpServletResponse response, String name) {
		Map params = new HashMap();
		/*params.put("goods_status", 0);
		params.put("store_deals", true);
		List<Goods> goodsList = this.goodsService.query(
				"select obj from Goods obj where obj.goods_status=:goods_status and obj.store_deals=:store_deals",
				params, -1, -1);*/
		params.put("goods_status", 0);
		List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.goods_status=:goods_status", params, -1, -1);
	/*	List<Goods> goodsList = new ArrayList<Goods>();
		Goods goods1 = this.goodsService.getObjById(CommUtil.null2Long("3884"));
		goodsList.add(goods1);*/
		for (Goods obj : goodsList) {
			GoodsThreadPool goodsThreadPool = GoodsThreadPool.getInstance();
			final Goods goods = goodsThreadPool.updateGoodsDiscountRate(obj);
			goodsThreadPool.addThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					System.out.println(goodsService.update(goods));
				}
			});
			// executor.execute(new Runnable(){
			//
			// @Override
			// public void run() {
			//
			// System.out.println(goodsService.update(goods));
			// }
			//
			// });
		}
		try {
			response.getWriter().print("Successfully");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @description 商品更新折扣率
	 *//*
		 * @RequestMapping("goods_discount_rate.json") public void
		 * goods_discount_rate(HttpServletRequest request, HttpServletResponse
		 * response){ Map params = new HashMap(); params.put("goods_status", 0);
		 * params.put("store_status", 15); List<Goods> goodsList =
		 * this.goodsService.query(
		 * "select obj from Goods obj where obj.goods_status=:goods_status and obj.goods_store.store_status=:store_status"
		 * , params, -1, -1); for(Goods obj : goodsList){ GoodsThreadPool
		 * goodsThreadPool = GoodsThreadPool.install(); final Goods goods =
		 * goodsThreadPool.updateGoodsDiscountRate(obj); executor.execute(new
		 * Runnable(){
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * goodsService.update(goods); }
		 * 
		 * }); } try { response.getWriter().print("Successfully"); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 */

	/**
	 * @description 批量更新商品属性
	 * @param list
	 */
	public void updateGoodsProperties(List<Goods> goodsList) {
		final List<Goods> list = goodsList;
		GoodsThreadPool tp = GoodsThreadPool.getInstance();
		tp.addThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (Goods obj : list) {
					obj.setZtc_price(6);
					goodsService.update(obj);
				}
			}
		});
	}

}
