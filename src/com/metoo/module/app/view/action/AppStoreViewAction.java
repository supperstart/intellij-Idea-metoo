package com.metoo.module.app.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StorePoint;
import com.metoo.foundation.service.IActivityGoodsService;
import com.metoo.foundation.service.IActivityService;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.seller.tools.TransportTools;
import com.metoo.view.web.tools.GoodsViewTools;
import com.metoo.view.web.tools.IntegralViewTools;

/**
 * 
 * <p>
 * Title: AppStoreViewAction.java
 * </p>
 * 
 * <p>
 * Description: 手机app店铺页
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
 * @author lixiaoyang
 * 
 * @date 2015-1-19
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class AppStoreViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsBrandService brandService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IGoodsBrandService goodsBrandService;
	@Autowired
	private IGroupGoodsService groupgoodsService;
	@Autowired
	private IActivityGoodsService activityGoodsService;
	@Autowired
	private IActivityService activityService;
	@Autowired
	private IConsultService consultService;
	@Autowired
	private GoodsViewTools goodsviewTools;
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IUserService userService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private TransportTools transportTools;
	@Autowired
	private IntegralViewTools integralViewTools;

	/**
	 * 手机客户端店铺首页详情请求
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 * @return
	 */
	@RequestMapping("/app/store_index.htm")
	public void store_index(HttpServletRequest request,
			HttpServletResponse response, String store_id) {
		boolean verify = true;
		Map json_map = new HashMap();
		if (verify) {
			if (store_id != null) {
				Store store = this.storeService.getObjById(CommUtil
						.null2Long(store_id));
				if (store != null) {
					json_map.put("store_name", store.getStore_name());
					String url = CommUtil.getURL(request);
					if (!"".equals(CommUtil.null2String(this.configService
							.getSysConfig().getImageWebServer()))) {
						url = this.configService.getSysConfig()
								.getImageWebServer();
					}
					if (store.getStore_logo() != null) {
						json_map.put("store_logo", url + "/"
								+ store.getStore_logo().getPath() + "/"
								+ store.getStore_logo().getName());
					} else {
						json_map.put("store_logo", url
								+ "/"
								+ this.configService.getSysConfig()
										.getStoreImage().getPath()
								+ "/"
								+ this.configService.getSysConfig()
										.getStoreImage().getName());
					}
					Map params = new HashMap();
					params.put("store_id", store.getId());
					List goods_count = this.goodsService
							.query("select count(obj.id) from Goods obj where obj.goods_store.id=:store_id",
									params, -1, -1);
					json_map.put("store_goods_count", goods_count.get(0));

					StorePoint point = store.getPoint();
					json_map.put("description_evaluate",
							point.getDescription_evaluate());
					json_map.put("service_evaluate",
							point.getService_evaluate());
					json_map.put("ship_evaluate", point.getShip_evaluate());

					params.put("goods_recommend", true);
					List<Goods> goods = this.goodsService
							.query("select obj from Goods obj where obj.goods_store.id=:store_id and obj.goods_recommend=:goods_recommend",
									params, 0, 16);
					List map_list = new ArrayList();
					String goods_main_photo = url// 系统默认商品图片
							+ "/"
							+ this.configService.getSysConfig().getGoodsImage()
									.getPath()
							+ "/"
							+ this.configService.getSysConfig().getGoodsImage()
									.getName();
					for (Goods obj : goods) {
						Map goods_map = new HashMap();
						goods_map.put("id", obj.getId());
						goods_map.put("goods_name", obj.getGoods_name());
						goods_map.put("goods_current_price", CommUtil
								.null2String(obj.getGoods_current_price()));// 商品现价
						if (obj.getGoods_main_photo() != null) {// 商品主图片
							goods_main_photo = url + "/"
									+ obj.getGoods_main_photo().getPath() + "/"
									+ obj.getGoods_main_photo().getName()
									+ "_small."
									+ obj.getGoods_main_photo().getExt();
						}
						goods_map.put("goods_main_photo", goods_main_photo);

						map_list.add(goods_map);
					}
					json_map.put("goods_list", map_list);

				} else {
					json_map.put("msg", "store is null,params id is "
							+ store_id);
				}
			} else {
				json_map.put("msg", "no params id");
			}

		}
		this.send_json(Json.toJson(json_map, JsonFormat.compact()), response);
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
