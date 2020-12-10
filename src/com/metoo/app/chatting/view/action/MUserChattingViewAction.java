package com.metoo.app.chatting.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MGoodsViewTools;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsClass;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IArticleService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.module.chatting.domain.Chatting;
import com.metoo.module.chatting.domain.ChattingConfig;
import com.metoo.module.chatting.domain.ChattingLog;
import com.metoo.module.chatting.domain.query.ChattingLogQueryObject;
import com.metoo.module.chatting.service.IChattingConfigService;
import com.metoo.module.chatting.service.IChattingLogService;
import com.metoo.module.chatting.service.IChattingService;
import com.metoo.view.web.tools.ActivityViewTools;
import com.metoo.view.web.tools.GoodsViewTools;
/**
 * 
 * <p>
 * Title: ChattingViewAction.java
 * </p>
 * 
 * <p>
 * Description: 系统聊天工具,作为单独聊天系统，可以集成其他系统,用户使用聊天系统超过session时长自动关闭会话窗口
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * 
 * <p>
 * Company: 
 * </p>
 * 
 * @author 
 * 
 * @date 2019年7月25日
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class MUserChattingViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsclassService;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IOrderFormService ofService;
	@Autowired
	private IChattingService chattingService;
	@Autowired
	private IChattingLogService chattinglogService;
	@Autowired
	private IChattingConfigService chattingconfigService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IReturnGoodsLogService returngoodslogService;
	@Autowired
	private OrderFormTools orderformTools;
	@Autowired
	private IArticleService articleService;
	@Autowired
	private ActivityViewTools activityViewTools;
	@Autowired
	private MGoodsViewTools metooGoodsViewTools;
	
	@RequestMapping("/user_chatting.json")
	public void user_chatting(HttpServletRequest request,
			HttpServletResponse response, String gid, String type,
			String store_id, String currentPage, String token) {
		Result result = null;
		Map chatting_map = new HashMap();
		ModelAndView mv = new JModelAndView("chatting/user_chatting.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users =  this.userService.query("select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if(users.isEmpty()){
				result = new Result(-100,"token Invalidation");	
			}else{
				User user = users.get(0);
				List<Chatting> chattings = null;
				Chatting chatting = new Chatting();
				ChattingConfig config = new ChattingConfig();
				if (!users.isEmpty()) {
					HttpSession session = request.getSession(false);
					session.setAttribute("chatting_session", "chatting_session");
					
					//[接受商品id查询商品信息 否则该位置显示推荐商品信息]
					if (gid != null && !gid.equals("")) {
						Goods goods = this.goodsService.getObjById(CommUtil
								.null2Long(gid));
						if (goods.getGoods_type() == 1) {// 和商家客服对话
							Map map = new HashMap();
							// 生成聊天组件配置信息
							map.put("store_id", goods.getGoods_store().getId());
							List<ChattingConfig> config_list = this.chattingconfigService
									.query("select obj from ChattingConfig obj where obj.store_id=:store_id ",
											map, 0, 1);
							if (config_list.size() == 0) {
								config.setAddTime(new Date());
								config.setConfig_type(0);
								config.setStore_id(goods.getGoods_store().getId());
								config.setKf_name(goods.getGoods_store()
										.getStore_name() + "在线客服");
								this.chattingconfigService.save(config);
							} else {
								config = config_list.get(0);
							}
							map.clear(); 
							map.put("uid", user.getId());
							map.put("store_id", goods.getGoods_store().getId());
							chattings = this.chattingService
									.query("select obj from Chatting obj where obj.user_id=:uid and obj.config.store_id=:store_id order by addTime asc",
											map, 0, 1);
							if (chattings.size() == 0) {
								chatting.setAddTime(new Date());
								chatting.setUser_id(user.getId());
								chatting.setConfig(config);
								chatting.setUser_name(user.getUserName());
								chatting.setGoods_id(CommUtil.null2Long(gid));
								this.chattingService.save(chatting);
							} else {
								chatting = chattings.get(0);
								chatting.setGoods_id(CommUtil.null2Long(gid));
								this.chattingService.update(chatting);
							}
							mv.addObject("store", goods.getGoods_store());
							this.generic_evaluate(goods.getGoods_store(), mv);// 店铺信用信息
							
						} else {// 和平台客服对话
							Map map = new HashMap();
							// 生成聊天组件配置信息
							map.put("config_type", 1);// 平台
							List<ChattingConfig> config_list = this.chattingconfigService
									.query("select obj from ChattingConfig obj where obj.config_type=:config_type ",
											map, 0, 1);
							if (config_list.size() == 0) {
								config.setAddTime(new Date());
								config.setConfig_type(1);// 平台聊天
								config.setKf_name("平台在线客服");
								this.chattingconfigService.save(config);
							} else {
								config = config_list.get(0);
							}
							map.clear();
							map.put("uid", user.getId());
							map.put("config_type", 1);
							chattings = this.chattingService
									.query("select obj from Chatting obj where obj.user_id=:uid and obj.config.config_type=:config_type order by addTime asc",
											map, 0, 1);
							if (chattings.size() == 0) {
								chatting.setAddTime(new Date());
								chatting.setUser_id(user.getId());
								chatting.setConfig(config);
								chatting.setUser_name(user.getUserName());
								chatting.setGoods_id(CommUtil.null2Long(gid));
								this.chattingService.save(chatting);
							} else {
								chatting = chattings.get(0);
								chatting.setGoods_id(CommUtil.null2Long(gid));
								this.chattingService.update(chatting);
							}
						}
					} else {// 当不传入商品id时，聊天窗口不显示商品信息，显示推荐商品
						Map map = new HashMap();
						if (type.equals("store")) {// 没有传入商品id时和商家客服对话
							Store store = this.storeService.getObjById(CommUtil
									.null2Long(store_id));
							if (store != null) {
								map.clear();
								map.put("store_id", store.getId());
								map.put("goods_status", 0);
								List<Goods> recommends = this.goodsService
										.query("select obj from Goods obj where obj.goods_status=:goods_status and obj.goods_store.id=:store_id order by goods_salenum desc",
												map, 0, 5);
								mv.addObject("recommends", recommends);
								// 生成聊天组件配置信息
								map.clear();
								map.put("store_id", store.getId());
								List<ChattingConfig> config_list = this.chattingconfigService
										.query("select obj from ChattingConfig obj where obj.store_id=:store_id ",
												map, 0, 1);
								if (config_list.size() == 0) {
									config.setAddTime(new Date());
									config.setConfig_type(0); //[类型 0商家]
									config.setStore_id(store.getId());//[店铺id]
									config.setKf_name(store.getStore_name() + "在线客服");//[客服自定义名称]
									this.chattingconfigService.save(config);
								} else {
									config = config_list.get(0);
								}
								map.clear();
								map.put("uid", user.getId());
								map.put("store_id", store.getId());
								chattings = this.chattingService
										.query("select obj from Chatting obj where obj.user_id=:uid and obj.config.store_id=:store_id order by addTime asc",
												map, 0, 1);
								if (chattings.size() == 0) {
									chatting.setAddTime(new Date());
									chatting.setUser_id(user.getId());
									chatting.setConfig(config);
									chatting.setUser_name(user.getUserName());
									this.chattingService.save(chatting);
								} else {
									chatting = chattings.get(0);
								}
								mv.addObject("store", store);
								this.generic_evaluate(store, mv);// 店铺信用信息
							} else {
								result = new Result(1,"请求参数错误");
							}
		
						} else {// 没有传入商品id时和平台客服对话
							map.clear();
							map.put("goods_type", 0);
							map.put("goods_status", 0);
							List<Goods> recommends = this.goodsService
									.query("select obj from Goods obj where obj.goods_status=:goods_status and obj.goods_type=:goods_type order by goods_salenum desc",
											map, 0, 5);
							mv.addObject("recommends", recommends);
							// 生成聊天组件配置信息
							map.clear();
							map.put("config_type", 1);// 平台
							List<ChattingConfig> config_list = this.chattingconfigService
									.query("select obj from ChattingConfig obj where obj.config_type=:config_type ",
											map, 0, 1);
							if (config_list.size() == 0) {
								config.setAddTime(new Date());
								config.setConfig_type(1);// 平台聊天
								config.setKf_name("平台在线客服");
								this.chattingconfigService.save(config);
							} else {
								config = config_list.get(0);
							}
							map.clear();
							map.put("uid", user.getId());
							map.put("config_type", 1);
							chattings = this.chattingService
									.query("select obj from Chatting obj where obj.user_id=:uid and obj.config.config_type=:config_type order by addTime asc",
											map, 0, 1);
							if (chattings.size() == 0) {
								chatting.setAddTime(new Date());
								chatting.setUser_id(user.getId());
								chatting.setConfig(config);
								chatting.setUser_name(user.getUserName());
								this.chattingService.save(chatting);
							} else {
								chatting = chattings.get(0);
							}
							//读取聊天记录
							List<ChattingLog> chattingLogs = new ArrayList<ChattingLog>();
							ChattingLogQueryObject qo = new ChattingLogQueryObject(currentPage, mv,
									"addTime", "desc");
							qo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
							qo.addQueryOR("obj.chatting.id", new SysMap("chatting_id", chatting.getId()), "=");
							//qo.addQuery("obj.user_read", new SysMap("user_read", 1), "=");
							//qo.addQuery("obj.plat_read", new SysMap("plat_read", 1), "=");
							qo.setPageSize(20);
							IPageList pList = chattinglogService.list(qo);
							chattingLogs = pList.getResult();
							List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
							for(ChattingLog log : chattingLogs){
								Map<String,Object> logmap = new HashMap<String,Object>();
								logmap.put("user_read", log.getUser_read());
								logmap.put("user_id", log.getUser_id());
								logmap.put("addTime", log.getAddTime());
								logmap.put("store_read", log.getStore_read());
								logmap.put("plat_read", log.getPlat_read());
								logmap.put("id", log.getId());
								logmap.put("content", log.getContent());
								list.add(logmap);
							}
							Collections.reverse(list);
							chatting_map.put("chattingLogJson", list);
						}
					}
					
					
					// 我的订单
					params.clear();
					/*params.put("user_id", SecurityUserHolder.getCurrentUser().getId()
							.toString());
					List<OrderForm> orders = this.ofService
							.query("select obj from OrderForm obj where obj.user_id=:user_id order by addTime desc",
									params, 0, 1);*/
					/*params.clear();
					params.put("user_id", SecurityUserHolder.getCurrentUser().getId()
							.toString());
					List<OrderForm> all_orders = this.ofService
							.query("select obj.id from OrderForm obj where obj.user_id=:user_id order by addTime desc",
									params, -1, -1);*/
					// 我的退货
					/*params.clear();
					params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
					List<ReturnGoodsLog> returnlogs = this.returngoodslogService
							.query("select obj from ReturnGoodsLog obj where obj.user_id=:user_id order by addTime desc",
									params, 0, 1);*/
					/*params.clear();
					params.put("user_id", SecurityUserHolder.getCurrentUser().getId());
					List<ReturnGoodsLog> all_returnlogs = this.returngoodslogService
							.query("select obj.id from ReturnGoodsLog obj where obj.user_id=:user_id order by addTime desc",
									params, -1, -1);*/
					// 文章
					/*params.clear();
					params.put("class_mark", "chatting_article");
					params.put("display", true);
					List<Article> article = this.articleService
							.query("select obj from Article obj where obj.articleClass.parent.mark=:class_mark and obj.display=:display order by obj.addTime desc",
									params, 0, 10);*/
					//mv.addObject("chatting", chatting);
					
					chatting_map.put("chatting_user_id", user.getId());
					chatting_map.put("store_id", store_id == null ? "" : store_id);
					chatting_map.put("chatting_userName", user.getUserName());
					chatting_map.put("chatting_id", chatting.getId());
					chatting_map.put("chatting_font", chatting.getFont());
					chatting_map.put("chatting_font_size", chatting.getFont_size());
					chatting_map.put("chatting_font_colour", chatting.getFont_colour());
					chatting_map.put("chatting_config_name", chatting.getConfig().getKf_name());
				
					if(!chatting_map.isEmpty()){
						result = new Result(0,"success",chatting_map);
					}else{
						result = new Result(1,"error");
					}
					//mv.addObject("user", user);
					//mv.addObject("article", article);
					//mv.addObject("returnlogs", returnlogs);
					//mv.addObject("all_returnlogs", all_returnlogs.size());
					//mv.addObject("orders", orders);
					//mv.addObject("all_orders", all_orders.size());
					//mv.addObject("orderformTools", orderformTools);
				} else {
					result = new Result(2,"长时间没有操作，窗口已关闭");
				}
			}
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/user_chatting_save.json")
	public void user_chatting_save(HttpServletRequest request,
			HttpServletResponse response, String text, String chatting_id,
			String font, String font_size, String font_colour, String token) {
		Result result = null;
		Map user_chatting_map = new HashMap(); 
		ModelAndView mv = new JModelAndView("chatting/user_chatting_log.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users =  this.userService.query("select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if(users.isEmpty()){
				result = new Result(-100,"token Invalidation");	
			}else{
				boolean ret = true;
				User user = users.get(0);
				Chatting chatt = this.chattingService.getObjById(CommUtil
						.null2Long(chatting_id));
				if (chatt == null) {
					ret = false;
				}
				if (users.isEmpty()) {
					ret = false;
				}
				if (ret) {
					chatt.setChatting_display(0);// 显示
					this.chattingService.update(chatt);
					ChattingLog log = new ChattingLog();
					log.setAddTime(new Date());
					log.setContent(text);
					log.setFont(font);
					log.setFont_size(font_size);
					log.setFont_colour(font_colour);
					log.setChatting(chatt);
					log.setUser_id(user.getId());
					log.setUser_read(1);// 自己发布的消息设置为自己已读
					this.chattinglogService.save(log);
					// 保存用户聊天组件字体信息
					if (!font.equals(chatt.getFont()) && !font.equals("")) {
						chatt.setFont(font);
					}
					if (!font_size.equals(chatt.getFont_size())
							&& !font_size.equals("")) {
						chatt.setFont_size(font_size);
					}
					if (!font_colour.equals(chatt.getFont_colour())
							&& !font_colour.equals("")) {
						chatt.setFont_colour(font_colour);
					}
					this.chattingService.update(chatt);
					List<ChattingLog> cls = new ArrayList<ChattingLog>();
					cls.add(log);
					// 客服自动回复
					if (chatt.getConfig().getQuick_reply_open() == 1) {
						ChattingLog log2 = new ChattingLog();
						log2.setAddTime(new Date());
						log2.setChatting(chatt);
						log2.setContent(chatt.getConfig().getQuick_reply_content()
								+ "[自动回复]");
						log2.setFont(chatt.getConfig().getFont());
						log2.setFont_size(chatt.getConfig().getFont_size());
						log2.setFont_colour(chatt.getConfig().getFont_colour());
						log2.setStore_id(chatt.getConfig().getStore_id());// 当与平台对话时，该值为null
						this.chattinglogService.save(log2);
					}
					List<Map> cl_list = new ArrayList<Map>();
					for(ChattingLog obj : cls){
						if(obj.getUser_id() != null && !obj.getUser_id().equals("")){
							Map cl_map = new HashMap();
							cl_map.put("user_name", obj.getChatting().getUser_name());
							User usr = this.userService.getObjById(CommUtil.null2Long(obj.getUser_id()));
							if(usr.getSex() == 0){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member0.png");
							}
							if(usr.getSex() == 1){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member1.png");
							}
							if(usr.getSex() == -1){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member.png");
							}
							cl_map.put("chatting_time", CommUtil.formatLongDate(obj.getAddTime()));
							cl_map.put("chatting_font_colour", obj.getFont_colour());
							cl_map.put("chatting_font", obj.getFont());
							cl_map.put("chatting_font_size", obj.getFont_size());
							cl_map.put("chatting_content", obj.getContent());
							cl_map.put("kf_name", obj.getChatting().getConfig().getKf_name());
							cl_list.add(cl_map);
						}
					}
					user_chatting_map.put("cl_list", cl_list);
					//mv.addObject("chatting", chatt);
					
					// 重新设置sessin
					HttpSession session = request.getSession(false);
					session.removeAttribute("chatting_session");
					session.setAttribute("chatting_session", "chatting_session");
					String chatting_session = CommUtil.null2String(session
							.getAttribute("chatting_session"));
					if (session != null && !session.equals("")) {
						user_chatting_map.put("chatting_session", chatting_session);
						//mv.addObject("chatting_session", chatting_session);
					}
					result = new Result(0,"chatting_success",user_chatting_map);
				} else {
					result = new Result(1,"chatting_error",true);
				}
			}
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用户端定时刷新请求， type;// 对话类型，0为用户和商家对话，1为用户和平台对话 user_read:用户没有读过的信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/user_chatting_refresh.json")
	public void user_chatting_refresh(HttpServletRequest request,
			HttpServletResponse response, String chatting_id, String token) {
		Map refresh_map = new HashMap();
		Result result = null;
		ModelAndView mv = new JModelAndView("chatting/user_chatting_log.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if(token.equals("")){
			result = new Result(-100,"token Invalidation");
		}else{
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users =  this.userService.query("select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if(users.isEmpty()){
				result = new Result(-100,"token Invalidation");
			}else{
				Chatting chatting = this.chattingService.getObjById(CommUtil
						.null2Long(chatting_id));
				if (!users.isEmpty() && chatting != null) {
					params.clear();
					params.put("chatting_id", CommUtil.null2Long(chatting_id));
					params.put("user_read", 0);// user_read:用户没有读过的信息
					List<ChattingLog> logs = this.chattinglogService
							.query("select obj from ChattingLog obj where obj.chatting.id=:chatting_id and obj.user_read=:user_read order by addTime asc",
									params, -1, -1);
					for (ChattingLog log : logs) {
						log.setUser_read(1);// 标记为用户已读
						this.chattinglogService.update(log);
					}
					HttpSession session = request.getSession(false);
					String chatting_session = CommUtil.null2String(session
							.getAttribute("chatting_session"));
					if (session != null && !session.equals("")) {
						refresh_map.put("chatting_session", chatting_session);
					}
					List<Map> cl_list = new ArrayList<Map>();
					for(ChattingLog obj : logs){
						if(obj.getUser_id() != null && !obj.getUser_id().equals("")){
							
							Map cl_map = new HashMap();
							cl_map.put("user_name", obj.getChatting().getUser_name());
							cl_map.put("addTime", CommUtil.formatLongDate(obj.getAddTime()));
							cl_map.put("content", obj.getContent());
							User usr = this.userService.getObjById(CommUtil.null2Long(obj.getUser_id()));
							if(usr.getSex() == 0){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member0.png");
							}
							if(usr.getSex() == 1){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member1.png");
							}
							if(usr.getSex() == -1){
								cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"member.png");
							}
							cl_map.put("kf_name", obj.getChatting().getConfig().getKf_name());
							cl_list.add(cl_map);
						}else{
							Map cl_map = new HashMap();
							cl_map.put("profile_photo", this.configService.getSysConfig().getImageWebServer()+"/"+"resources"+"/"+"style"+"/"+"common"+"/"+"images"+"/"+"loader.png");
							cl_map.put("kf_name", obj.getChatting().getConfig().getKf_name());
							cl_map.put("chatting_time", CommUtil.formatLongDate(obj.getAddTime()));
							cl_map.put("chatting_font_colour", obj.getFont_colour());
							cl_map.put("chatting_font", obj.getFont());
							cl_map.put("chatting_font_size", obj.getFont_size());
							cl_map.put("content", obj.getContent());
							cl_list.add(cl_map);
						}
					}
					refresh_map.put("cl_list", cl_list);
				}
				if(refresh_map.isEmpty()){
					result = new Result(1,"暂时还没有聊天记录");
				}else{
					result = new Result(0,"success",refresh_map);
				}
			}
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 加载店铺评分信息
	 * 
	 * @param store
	 * @param mv
	 */
	private void generic_evaluate(Store store, ModelAndView mv) {
		double description_result = 0;
		double service_result = 0;
		double ship_result = 0;
		GoodsClass gc = this.goodsclassService
				.getObjById(store.getGc_main_id());
		if (store != null && gc != null && store.getPoint() != null) {
			float description_evaluate = CommUtil.null2Float(gc
					.getDescription_evaluate());
			float service_evaluate = CommUtil.null2Float(gc
					.getService_evaluate());
			float ship_evaluate = CommUtil.null2Float(gc.getShip_evaluate());

			float store_description_evaluate = CommUtil.null2Float(store
					.getPoint().getDescription_evaluate());
			float store_service_evaluate = CommUtil.null2Float(store.getPoint()
					.getService_evaluate());
			float store_ship_evaluate = CommUtil.null2Float(store.getPoint()
					.getShip_evaluate());
			// 计算和同行比较结果
			description_result = CommUtil.div(store_description_evaluate
					- description_evaluate, description_evaluate);
			service_result = CommUtil.div(store_service_evaluate
					- service_evaluate, service_evaluate);
			ship_result = CommUtil.div(store_ship_evaluate - ship_evaluate,
					ship_evaluate);
		}
		if (description_result > 0) {
			mv.addObject("description_css", "red");
			mv.addObject("description_css1", "bg_red");
			mv.addObject("description_type", "高于");
			mv.addObject(
					"description_result",
					CommUtil.null2String(CommUtil.mul(description_result, 100) > 100 ? 100
							: CommUtil.mul(description_result, 100))
							+ "%");
		}
		if (description_result == 0) {
			mv.addObject("description_css", "orange");
			mv.addObject("description_css1", "bg_orange");
			mv.addObject("description_type", "持平");
			mv.addObject("description_result", "-----");
		}
		if (description_result < 0) {
			mv.addObject("description_css", "green");
			mv.addObject("description_css1", "bg_green");
			mv.addObject("description_type", "低于");
			mv.addObject(
					"description_result",
					CommUtil.null2String(CommUtil.mul(-description_result, 100))
							+ "%");
		}
		if (service_result > 0) {
			mv.addObject("service_css", "red");
			mv.addObject("service_css1", "bg_red");
			mv.addObject("service_type", "高于");
			mv.addObject("service_result",
					CommUtil.null2String(CommUtil.mul(service_result, 100))
							+ "%");
		}
		if (service_result == 0) {
			mv.addObject("service_css", "orange");
			mv.addObject("service_css1", "bg_orange");
			mv.addObject("service_type", "持平");
			mv.addObject("service_result", "-----");
		}
		if (service_result < 0) {
			mv.addObject("service_css", "green");
			mv.addObject("service_css1", "bg_green");
			mv.addObject("service_type", "低于");
			mv.addObject("service_result",
					CommUtil.null2String(CommUtil.mul(-service_result, 100))
							+ "%");
		}
		if (ship_result > 0) {
			mv.addObject("ship_css", "red");
			mv.addObject("ship_css1", "bg_red");
			mv.addObject("ship_type", "高于");
			mv.addObject("ship_result",
					CommUtil.null2String(CommUtil.mul(ship_result, 100)) + "%");
		}
		if (ship_result == 0) {
			mv.addObject("ship_css", "orange");
			mv.addObject("ship_css1", "bg_orange");
			mv.addObject("ship_type", "持平");
			mv.addObject("ship_result", "-----");
		}
		if (ship_result < 0) {
			mv.addObject("ship_css", "green");
			mv.addObject("ship_css1", "bg_green");
			mv.addObject("ship_type", "低于");
			mv.addObject("ship_result",
					CommUtil.null2String(CommUtil.mul(-ship_result, 100)) + "%");
		}
	}
	
	@RequestMapping("/user_chattinglog.json")
	public void chattingLog(HttpServletRequest request,
			HttpServletResponse response, String chatting_id, String store_id, String token, String currentPage){
		Result result = null;
		ModelAndView mv = new JModelAndView("", configService.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		if(token != null && !token.equals("")){
			Map params = new HashMap();
			params.put("app_login_token", token);
			List<User> users =  this.userService.query("select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if(users.isEmpty()){
				result = new Result(-100,"token Invalidation");	
			}else{
				int code = -1;
				User user = users.get(0);
				List<ChattingLog> chattingLogs = new ArrayList<ChattingLog>();
				ChattingLogQueryObject qo = new ChattingLogQueryObject(currentPage, mv,
						"addTime", "desc");
				qo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
				qo.addQueryOR("obj.chatting.id", new SysMap("chatting_id", CommUtil.null2Long(chatting_id)), "=");
				if(store_id != null && !store_id.equals("")){
					//qo.addQuery("obj.user_read", new SysMap("user_read", 1), "=");
					//qo.addQuery("obj.store_read", new SysMap("store_read", 1), "=");
					qo.setPageSize(20);
					qo.setCurrentPage(CommUtil.null2Int(currentPage) + 1);
					IPageList pList = chattinglogService.list(qo);
					int page = pList.getPages();
					chattingLogs = pList.getResult();
					if(page <= 1){
						code = 1;
					}
				}else{
					//qo.addQuery("obj.user_read", new SysMap("user_read", 1), "=");
					//qo.addQuery("obj.plat_read", new SysMap("plat_read", 1), "=");
					qo.setPageSize(20);
					int pages = CommUtil.null2Int(currentPage) + 1;
					qo.setCurrentPage(pages);
					IPageList pList = chattinglogService.list(qo);
					chattingLogs = pList.getResult();
					int page = pList.getPages();
					/*if(page <= 1){
						code = 1;
					}*/
				}
				/*if(code == 1){
					result = new Result(-1, "No history chat");	
				}else{*/
					List<Map> list = new ArrayList<Map>();
					for(ChattingLog log : chattingLogs){
						Map logmap = new HashMap();
						logmap.put("user_read", log.getUser_read());
						logmap.put("user_id", log.getUser_id());
						logmap.put("addTime", log.getAddTime());
						logmap.put("store_read", log.getStore_read());
						logmap.put("plat_read", log.getPlat_read());
						logmap.put("id", log.getId());
						logmap.put("content", log.getContent());
						list.add(logmap);
					//}
					
					//JSONArray jsonArray = JSONArray.parseArray(JSON.toJSONString(chattingLogs));
				}
					Collections.reverse(list);
					result = new Result(0, list);
 			}
		}else{
			result = new Result(-100, "token为空");
		}
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
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
