package com.metoo.app.foundation.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MIFavoriteService;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.FavoriteQueryObject;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.UserTools;
import com.metoo.view.web.tools.GoodsViewTools;
@Service
@Transactional
public class MFavoriteServiceImpl implements MIFavoriteService{
	
	@Resource(name = "favoriteMetooDAO")
	private IGenericDAO<Favorite> favoriteMetooDao;

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IMessageService messageService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private UserTools userTools;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IFavoriteService favoriteService;
	
	public Favorite getObjById(Long id) {
		Favorite favorite = this.favoriteMetooDao.get(id);
		if(favorite != null){
			return favorite;
		}
		return null;
	}
	
	@Override
	public boolean delete(Long id) {
		try {
			this.favoriteMetooDao.remove(id);
			return true;
		} catch (Exception e) {	
			e.printStackTrace();
			return false;
		}
	}	
	
	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();//默认查询语句1=1
		String construct = properties.getConstruct();// 查询构造器，为空时查询obj所有字段
		Map params = properties.getParameters();//取出封装的参数列表
		//[该类用来进行数据查询并分页返回数据信息  构造分页信息]
		GenericPageList pList = new GenericPageList(Favorite.class, construct,
				query, params, this.favoriteMetooDao);
		if (properties != null) {
			//[获取分页信息 默认 currentPage=-1 pageSize=-1]
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(
						pageObj.getCurrentPage() == null ? 0 : pageObj
								.getCurrentPage(),
						pageObj.getPageSize() == null ? 0 : pageObj
								.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}
	
	//收藏商品信息
	public String favorite_goods(HttpServletRequest request, HttpServletResponse response,
			String currentPage, String orderBy, String orderType, String token, String language) {
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/favorite_goods.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map<String, Object> params = new HashMap<String, Object>();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String url = this.configService.getSysConfig().getAddress();
				if (url == null || url.equals("")) {
					url = CommUtil.getURL(request);
				}
				params.clear();
				FavoriteQueryObject qo = new FavoriteQueryObject(currentPage, mv,
						orderBy, orderType);
				qo.addQuery("obj.type", new SysMap("type", 0), "=");//收藏类型，0为商品收藏、1为店铺收藏
				qo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
				IPageList pList = this.list(qo);
				List<Favorite> favoriteList =  pList.getResult();
				int facoriteNum = favoriteList.size();
				map.put("facoriteNum", facoriteNum);
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(Favorite favorites : favoriteList){
					Map<String, Object> map1 = new HashMap<String, Object>();
					map1.put("favorites_id", favorites.getId());
					map1.put("Goods_id", favorites.getGoods_id());
					Goods obj = this.goodsService.getObjById(CommUtil.null2Long(favorites.getGoods_id()));
					map1.put("Goods_status", obj == null ? "" : obj.getGoods_status());
					map1.put("Store_status", obj == null ? "" : obj.getGoods_store().getStore_status());
					map1.put("Goods_collect", obj == null ? "" : obj.getGoods_collect());
					map1.put("Goods_price", obj == null ? "" : obj.getGoods_current_price() == null ? obj.getGoods_price() : obj.getGoods_price());
					map1.put("Goods_name", favorites.getGoods_name());
					if("1".equals(language)){
						map1.put("Goods_name", favorites.getKsa_goods_name() != null 
																	&& !"".equals(favorites.getKsa_goods_name()) 
																	? "^"
																	+ favorites.getKsa_goods_name() 
																	: favorites.getGoods_name());
					}
					map1.put("Goods_current_price", favorites.getGoods_current_price());//[收藏时候的商品价格,若发送降价通知则更新为降价后的价格]
					map1.put("Goods_photo", this.configService.getSysConfig().getImageWebServer() + "/" + favorites.getGoods_photo());
					map1.put("Goods_type", favorites.getGoods_type());
					map1.put("Goods_store_id", favorites.getGoods_store_id());
					map1.put("AddTime", favorites.getAddTime());
					list.add(map1);
				}
				map.put("favoriteList", list);
				if(!map.isEmpty()){
					result = new Result(0,"success",map);
				}else{
					result = new Result(1,"null");
				}
			}
		}
		return Json.toJson(result, JsonFormat.compact());
	}
	
	//收藏店铺品信息
	public String favorite_store(HttpServletRequest request,
			HttpServletResponse response,String currentPage,String orderBy,String orderType, String token){
		Map<String, Object> map = new HashMap<String, Object>();
		Result result = null;
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/favorite_store.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if(token.equals("")){
			result = new Result(-100, "token Invalidation");
		}else{
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if(user == null){
				result = new Result(-100,"token Invalidation");
			}else{
				String url = this.configService.getSysConfig().getAddress();
				if (url == null || url.equals("")) {
					url = CommUtil.getURL(request);
				}
				FavoriteQueryObject qo = new FavoriteQueryObject(currentPage,mv,
						orderBy,orderType);
				qo.addQuery("obj.type", new SysMap("type",1), "=");
				qo.addQuery("obj.user_id", new SysMap("user_id",user.getId()),"=");
				IPageList pList = this.list(qo);
				List<Favorite> favoriteList = pList.getResult();
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for(Favorite favorite : favoriteList){
					Map<String, Object> favoriteMap = new HashMap<String, Object>();
					favoriteMap.put("favorite",favorite.getId());
					favoriteMap.put("Store_name",favorite.getStore_name());
					favoriteMap.put("Store_id",favorite.getStore_id());
					Store obj = this.storeService.getObjById(CommUtil.null2Long(favorite.getStore_id()));
					favoriteMap.put("store_status",obj == null ? "" : obj.getStore_status());
					favoriteMap.put("Store_photo", obj.getStore_logo() == null ? "" 
										: this.configService.getSysConfig()
											.getImageWebServer()
											+ "/"
											+ obj.getStore_logo()
												.getPath()
											+ "/"
											+ obj.getStore_logo()
												.getName());
					favoriteMap.put("AddTime", favorite.getAddTime());
					favoriteMap.put("store_addr", favorite.getStore_addr().equals("") ? "" : favorite.getStore_addr());
					favoriteMap.put("store_second_domain", favorite.getStore_second_domain() == null ? "" : favorite.getStore_second_domain());
					list.add(favoriteMap);
				}
				map.put("favoriteMap", list); 
				result = new Result(0, "Successfully", map);
			}
		}
		return Json.toJson(result, JsonFormat.compact());
		
	}
	
	//收藏删除
	public String favorite_del(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage) {
		Result result = null;
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Favorite favorite = this.favoriteService.getObjById(Long
						.parseLong(id));
				if (favorite != null && favorite.getGoods_id() != null) {
					// 更新lucene索引
					String goods_lucene_path = System.getProperty("metoob2b2c.root")
							+ File.separator + "luence" + File.separator
							+ "goods";
					File file = new File(goods_lucene_path);
					if (!file.exists()) {
						CommUtil.createFolder(goods_lucene_path);
					}
					LuceneUtil lucene = LuceneUtil.instance();
					lucene.setIndex_path(goods_lucene_path);
					Goods goods = this.goodsService.getObjById(favorite
							.getGoods_id());
					lucene.update(CommUtil.null2String(favorite.getGoods_id()),
							luceneVoTools.updateGoodsIndex(goods));
				}
				if (favorite.getType() == 0) {
					Goods goods = this.goodsService.getObjById(favorite
							.getGoods_id());
					goods.setGoods_collect(goods.getGoods_collect() - 1);
					this.goodsService.update(goods);
					
				}
				if (favorite.getType() == 1) {
					Store store = this.storeService.getObjById(favorite
							.getStore_id());
					store.setFavorite_count(store.getFavorite_count() - 1);
					this.storeService.update(store);
				}
				if(this.delete(Long.parseLong(id))){
					result = new Result(0, "Successfully");
				}else{
					result = new Result(1, "Error");
				}
			}
		}
			return Json.toJson(result, JsonFormat.compact());
	}
}
