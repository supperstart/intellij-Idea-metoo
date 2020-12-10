package com.metoo.app.v1.manage.buyer.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestMethod;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MIFavoriteService;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.annotation.SecurityMapping;
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
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.manage.seller.tools.StoreLogTools;
import com.metoo.view.web.tools.GoodsViewTools;

@Controller
@RequestMapping("/app/")
public class MFavoriteBuyerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private MIFavoriteService favoriteMetooService;
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
	@Autowired
	private IUserService userService;

	/**
	 * 用户商品收藏
	 * 
	 * @param response
	 * @param id
	 * @param token
	 */
//	@RequestMapping("/add_goods_favorite.json")
	@EmailMapping(title = "商品收藏", value = "goods_favorite")
	@RequestMapping(value = "v1/add_goods_favorite.json", method = RequestMethod.POST)
	public void add_goods_favorite(HttpServletResponse response, String id, String token) {
		Result result = null;
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("goods_id", CommUtil.null2Long(id));
				List<Favorite> list = this.favoriteService.query(
						"select obj from Favorite obj where " + "obj.user_id=:user_id and obj.goods_id=:goods_id",
						params, -1, -1);
				if (list.size() == 0) {
					Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
					Favorite obj = new Favorite();
					obj.setAddTime(new Date());
					obj.setType(0);
					obj.setUser_name(user.getUserName());
					obj.setUser_id(user.getId());
					obj.setGoods_id(goods.getId());
					obj.setGoods_name(goods.getGoods_name());
					obj.setKsa_goods_name(goods.getKsa_goods_name());
					obj.setGoods_photo(
							goods.getGoods_main_photo().getPath() + "/" + goods.getGoods_main_photo().getName());
					obj.setGoods_photo_ext(goods.getGoods_main_photo().getExt());
					obj.setGoods_store_id(goods.getGoods_store() == null ? null : goods.getGoods_store().getId());
					obj.setGoods_type(goods.getGoods_type());
					obj.setGoods_current_price(goods.getGoods_current_price() == null ? goods.getGoods_price()
							: goods.getGoods_current_price());
					if (this.configService.getSysConfig().isSecond_domain_open()) {
						Store store = this.storeService.getObjById(obj.getStore_id());
						obj.setGoods_store_second_domain(store.getStore_second_domain());
					}
					this.favoriteService.save(obj);
					goods.setGoods_collect(goods.getGoods_collect() + 1);
					this.goodsService.update(goods);
					GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(Long.parseLong(id));
					todayGoodsLog.setGoods_collect(todayGoodsLog.getGoods_collect() + 1);
					this.goodsLogService.update(todayGoodsLog);
					// 更新lucene索引[D:\HK\tomcat\apache-tomcat-8.0.53\webapps\metoo_store\\luence\goods]
					String goods_lucene_path = System.getProperty("metoob2b2c.root") + File.separator + "luence"
							+ File.separator + "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					lucene.update(CommUtil.null2String(goods.getId()), luceneVoTools.updateGoodsIndex(goods));
					result = new Result(0, "Successfully");
				} else {
					result = new Result(1, "Repeat collection");
				}
			}
		}
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Favorite列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "用户商品收藏", value = "/buyer/favorite_goods.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
//	@RequestMapping("/buyer_favorite_goods.json")
	@RequestMapping(value = "v1/favorite_goods.json", method = RequestMethod.POST)
	public void favorite_goods(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String orderBy, String orderType, String token, String language) {
		String favorite_goods = this.favoriteMetooService.favorite_goods(request, response, currentPage, orderBy,
				orderType, token, language);
		try {
			response.getWriter().println(favorite_goods);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SecurityMapping(title = "用户店铺收藏", value = "/buyer/favorite_goods.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
//	@RequestMapping("/buyer_favorite_store.json")
	@RequestMapping(value = "v1/favorite_store.json", method = RequestMethod.POST)
	public void favorite_store(HttpServletRequest request, HttpServletResponse response, String currentPage,
			String orderBy, String orderType, String token) {
		String favoriteStore = this.favoriteMetooService.favorite_store(request, response, currentPage, orderBy,
				orderType, token);
		try {
			response.getWriter().println(favoriteStore);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 收藏店铺-商品取消
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param token
	 */
//	@RequestMapping("/delete_favorite_sotre.json")
	@RequestMapping(value = "v1/delete_favorite_sotre.json", method = RequestMethod.POST)
	public void favorite_store(HttpServletRequest request, HttpServletResponse response, String store_id,
			String goods_id, String token) {
		Result result = null;
		if (token.equals("") && token == null) {
			result = new Result(-100, "token Invalidation");
		} else {
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if (users.isEmpty()) {
				result = new Result(-100, "token Invalidation");
			} else {

				Map<String, Object> map = new HashMap<String, Object>();
				User user = users.get(0);
				if (!store_id.equals("") && store_id != null) {

					params.clear();
					params.put("user_id", user.getId());
					params.put("store_id", CommUtil.null2Long(store_id));
					List<Favorite> storelist = this.favoriteService.query(
							"select obj from Favorite obj where obj.user_id=:user_id and obj.store_id=:store_id",
							params, -1, -1);
					if (!storelist.isEmpty() && storelist.get(0).getType() == 1) {
						Store store = this.storeService.getObjById(CommUtil.null2Long(store_id));
						store.setFavorite_count(store.getFavorite_count() - 1);
						this.storeService.update(store);

						if (this.favoriteService.delete(CommUtil.null2Long(storelist.get(0).getId()))) {
							result = new Result(0, "success");
						} else {
							result = new Result(1, "error");
						}
					}
				}
				if (!goods_id.equals("") && goods_id != null) {
					params.clear();
					params.put("user_id", user.getId());
					params.put("goods_id", CommUtil.null2Long(goods_id));
					List<Favorite> goodslist = this.favoriteService.query(
							"select obj from Favorite obj where obj.user_id=:user_id and obj.goods_id=:goods_id order by obj.goods_id ",
							params, -1, -1);
					if (goodslist.size() > 0 && goodslist.get(0).getType() == 0) {// 有数据则删除第一条数据

						// 商品收藏数 - 1
						Goods goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));
						goods.setGoods_collect(goods.getGoods_collect() - 1);
						this.goodsService.update(goods);

						// 商品日志 收藏数 -1
						GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(Long.parseLong(goods_id));
						todayGoodsLog.setGoods_collect(todayGoodsLog.getGoods_collect() - 1);
						this.goodsLogService.update(todayGoodsLog);
						Favorite obj = goodslist.get(0);
						if (this.favoriteService.delete(obj.getId())) {
							result = new Result(0, "success");
						} else {
							result = new Result(0, "error");
						}
					}
				}
			}

		}
		String favorite_store = Json.toJson(result, JsonFormat.compact());
		try {
			response.getWriter().print(favorite_store);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SecurityMapping(title = "用户收藏删除", value = "/buyer/favorite_del.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
//	@RequestMapping("/buyer_favorite_del.json")
	@RequestMapping(value = "v1/buyer_favorite_del.json", method = RequestMethod.DELETE)
	public void favoriteDel(HttpServletRequest request, HttpServletResponse response, String mulitId,
			String currentPage) {
		String favoritedel = this.favoriteMetooService.favorite_del(request, response, mulitId, currentPage);
		try {
			response.getWriter().print(favoritedel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
