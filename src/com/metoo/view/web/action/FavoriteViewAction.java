package com.metoo.view.web.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.view.web.tools.GoodsViewTools;

/**
 * 
 * <p>
 * Title: FavoriteViewAction.java
 * </p>
 * 
 * <p>
 * Description: 商城前台收藏控制器，用来添加商品、店铺收藏
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.metoo.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-30
 * 
 * @version metoo_b2b2c v2.0 2015版 
 */
@Controller
public class FavoriteViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;

	@RequestMapping("/add_goods_favorite.htm")
	public void add_goods_favorite(HttpServletResponse response, String id) {
		Result result = null;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		params.put("goods_id", CommUtil.null2Long(id));
		List<Favorite> list = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id=:user_id and obj.goods_id=:goods_id",
						params, -1, -1);
		int ret = 0;
		if (list.size() == 0) {
			Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			Favorite obj = new Favorite();
			obj.setAddTime(new Date());
			obj.setType(0);
			User user = SecurityUserHolder.getCurrentUser();
			obj.setUser_name(user.getUserName());
			obj.setUser_id(user.getId());
			obj.setGoods_id(goods.getId());
			obj.setGoods_name(goods.getGoods_name());
			obj.setGoods_photo(goods.getGoods_main_photo().getPath() + "/"
					+ goods.getGoods_main_photo().getName());
			obj.setGoods_photo_ext(goods.getGoods_main_photo().getExt());
			obj.setGoods_store_id(goods.getGoods_store() == null ? null : goods
					.getGoods_store().getId());
			obj.setGoods_type(goods.getGoods_type());
			obj.setGoods_current_price(goods.getGoods_current_price());
			if (this.configService.getSysConfig().isSecond_domain_open()) {
				Store store = this.storeService.getObjById(obj.getStore_id());
				obj.setGoods_store_second_domain(store.getStore_second_domain());
			}
			this.favoriteService.save(obj);
			goods.setGoods_collect(goods.getGoods_collect() + 1);
			this.goodsService.update(goods);
			GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(Long.parseLong(id));
			todayGoodsLog
					.setGoods_collect(todayGoodsLog.getGoods_collect() + 1);
			this.goodsLogService.update(todayGoodsLog);
			// 更新lucene索引[D:\HK\tomcat\apache-tomcat-8.0.53\webapps\metoo_store\\luence\goods]
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
		} else {
			ret = 1;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/add_store_favorite.htm")
	public void add_store_favorite(HttpServletResponse response, String id) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		params.put("store_id", CommUtil.null2Long(id));
		List<Favorite> list = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id=:user_id and obj.store_id=:store_id",
						params, -1, -1);
		int ret = 0;
		if (list.size() == 0) {
			Favorite obj = new Favorite();
			obj.setAddTime(new Date());
			obj.setType(1);
			User user = SecurityUserHolder.getCurrentUser();
			Store store = this.storeService.getObjById(CommUtil.null2Long(id));
			obj.setUser_id(user.getId());
			obj.setStore_id(store.getId());
			obj.setStore_name(store.getStore_name());
			obj.setStore_photo(store.getStore_logo() != null ? store
					.getStore_logo().getPath()
					+ "/"
					+ store.getStore_logo().getName() : null);
			if (this.configService.getSysConfig().isSecond_domain_open()) {
				obj.setStore_second_domain(store.getStore_second_domain());
			}
			String store_addr = "";
			if (store.getArea() != null) {
				store_addr = store.getArea().getAreaName() + store.getStore_address();
				if (store.getArea().getParent() != null) {
					store_addr = store.getArea().getParent().getAreaName()
							+ store_addr;
					if (store.getArea().getParent().getParent() != null) {
						store_addr = store.getArea().getParent().getParent()
								.getAreaName()
								+ store_addr;
					}
				}
			}
			obj.setStore_ower(store.getUser().getUserName());
			obj.setStore_addr(store_addr);
			this.favoriteService.save(obj);
			store.setFavorite_count(store.getFavorite_count() + 1);
			this.storeService.update(store);
		} else {
			ret = 1;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@RequestMapping("/delete_goods_favorite.htm")
	public void delete_goods_favorite(HttpServletResponse response, String id) {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
		params.put("goods_id", CommUtil.null2Long(id));
		List<Favorite> list = this.favoriteService
				.query("select obj from Favorite obj where obj.user_id=:user_id and obj.goods_id=:goods_id order by obj.goods_id ",
						params, -1, -1);
		int ret = 0;
		if (list.size()  > 0) {//有数据则删除第一条数据 
			Favorite obj = list.get(0);
			this.favoriteService.delete(obj.getId());
			
			//商品收藏数 - 1
			Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			goods.setGoods_collect(goods.getGoods_collect() - 1);
			this.goodsService.update(goods);
			
			//商品日志 收藏数 -1
			GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(Long.parseLong(id));
			todayGoodsLog
					.setGoods_collect(todayGoodsLog.getGoods_collect() - 1);
			this.goodsLogService.update(todayGoodsLog);

		} else {
			ret = 1;
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
