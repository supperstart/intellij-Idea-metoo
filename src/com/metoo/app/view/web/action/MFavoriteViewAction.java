package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Favorite;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IFavoriteService;
import com.metoo.foundation.service.IStoreLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.seller.tools.StoreLogTools;

@Controller
@RequestMapping("/app/")
public class MFavoriteViewAction {
	@Autowired
	private IFavoriteService favoriteService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private StoreLogTools storeLogTools;
	@Autowired
	private IStoreLogService storeLogService;

//		@RequestMapping("/add_store_favorite.json")
	@RequestMapping("v1/addStoreFavorite.json")
	public void add_store_favorite(HttpServletResponse response, String id, String token) {
		Result result = null;
		Map<String, Object> params = new HashMap<String, Object>();
		if (token.equals("")) {
			result = new Result(-100, "token Invalidation");
		} else {
			params.clear();
			User user = this.userService.getObjByProperty(null, "app_login_token", token);
			if (user == null) {
				result = new Result(-100, "token Invalidation");
			} else {
				params.clear();
				params.put("user_id", user.getId());
				params.put("store_id", CommUtil.null2Long(id));
				List<Favorite> list = this.favoriteService.query(
						"select obj from Favorite obj where obj.user_id=:user_id and obj.store_id=:store_id", params,
						-1, -1);
				if (list.size() == 0) {
					Favorite obj = new Favorite();
					obj.setAddTime(new Date());
					obj.setType(1);
					Store store = this.storeService.getObjById(CommUtil.null2Long(id));
					obj.setUser_id(user.getId());
					obj.setStore_id(store.getId());
					obj.setStore_name(store.getStore_name());
					obj.setStore_photo(store.getStore_logo() != null
							? store.getStore_logo().getPath() + "/" + store.getStore_logo().getName() : null);
					if (this.configService.getSysConfig().isSecond_domain_open()) {
						obj.setStore_second_domain(store.getStore_second_domain());
					}
					String store_addr = "";
					if (store.getArea() != null) {
						store_addr = store.getArea().getAreaName() + store.getStore_address();
						if (store.getArea().getParent() != null) {
							store_addr = store.getArea().getParent().getAreaName() + store_addr;
							if (store.getArea().getParent().getParent() != null) {
								store_addr = store.getArea().getParent().getParent().getAreaName() + store_addr;
							}
						}
					}
					obj.setStore_ower(store.getUser().getUserName());
					obj.setStore_addr(store_addr);
					this.favoriteService.save(obj);
					store.setFavorite_count(store.getFavorite_count() + 1);
					this.storeService.update(store);
					// 添加收藏次数
					StoreLog storeLog = this.storeLogTools.getTodayStoreLog(CommUtil.null2Long(id));
					storeLog.setStore_collect(storeLog.getStore_collect() + 1);
					this.storeLogService.update(storeLog);
					result = new Result(0, "Successfully");
				} else {
					result = new Result(0, "Successfully");
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
}
