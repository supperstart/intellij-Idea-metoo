package com.metoo.app.chatting.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.chatting.domain.Chatting;
import com.metoo.module.chatting.domain.ChattingConfig;
import com.metoo.module.chatting.domain.ChattingLog;
import com.metoo.module.chatting.service.IChattingConfigService;
import com.metoo.module.chatting.service.IChattingLogService;
import com.metoo.module.chatting.service.IChattingService;
import com.metoo.view.web.tools.GoodsViewTools;

/**
 * 
 * <p>
 * Title: ChattingViewAction.java
 * </p>
 * 
 * <p>
 * Description: 系统聊天工具,作为单独聊天系统系统，可以集成其他系统
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
public class MPlatChattingManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IChattingService chattingService;
	@Autowired
	private IChattingLogService chattinglogService;
	@Autowired
	private IChattingConfigService chattingconfigService;
	
	/*
	 * 平台客服受理咨询信息请求，
	 */
	@SecurityMapping(title = "APP聊天对话框", value = "/admin_plat_chatting.json", rtype = "admin", rname = "自营客服", rcode = "self_chatting", rgroup = "自营")
	@RequestMapping("/admin_plat_chatting.json")
	public void plat_chatting(HttpServletRequest request,
			HttpServletResponse response) {
		Map admin_map = new HashMap();
		Result result = null;
		ModelAndView mv = new JModelAndView("chatting/plat_chatting.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		HttpSession session = request.getSession(false);
		session.setAttribute("chatting_session", "chatting_session");
		ChattingConfig config = new ChattingConfig();
		Map params = new HashMap();
		params.put("config_type", 1);// 类型：平台
		params.put("chatting_display", 0);// 显示
		List<Chatting> chattings = this.chattingService
				.query("select obj from Chatting obj where obj.config.config_type=:config_type  and obj.chatting_display=:chatting_display and obj.logs.size>0 order by addTime desc",
						params, -1, -1);
		// 生成聊天组件配置信息
		params.clear();
		params.put("config_type", 1);// 类型：平台
		List<ChattingConfig> config_list = this.chattingconfigService
				.query("select obj from ChattingConfig obj where obj.config_type=:config_type ",
						params, 0, 1);
		if (config_list.size() == 0) {
			config.setAddTime(new Date());
			config.setConfig_type(1);
			config.setKf_name("平台在线客服");
			this.chattingconfigService.save(config);
		} else {
			config = config_list.get(0);
		}
		params.clear();
		params.put("config_type", 1);// 类型：平台
		params.put("plat_read", 0);
		List<ChattingLog> logs = this.chattinglogService
				.query("select obj from ChattingLog obj where obj.chatting.config.config_type=:config_type and obj.plat_read=:plat_read order by addTime asc",
						params, -1, -1);// 所有未读聊天记录
		mv.addObject("logs", logs);
		mv.addObject("chattings", chattings);// 所有联系人列表,
		
		//[联系人列表]
		List<Map> chattinglog_list = new ArrayList<Map>();
		for(Chatting chatting : chattings){
			Map chattinglog_map = new HashMap();
			int person = 0;
			for(ChattingLog chattingLog : logs){
				if(chattingLog.getChatting().getId() == chatting.getId()){
					person = person+1;
				}
			}
			chattinglog_map.put("chatting_id", chatting.getId());
			chattinglog_map.put("person", person);
			chattinglog_map.put("user_name", CommUtil.substring(chatting.getUser_name(), 15));
			chattinglog_list.add(chattinglog_map);
		}
		
		admin_map.put("chattinglog_list", chattinglog_list);
		
		mv.addObject("chattingConfig", config);
		List chatting_config_list = new ArrayList();
		Map chatting_config_map = new HashMap();
		chatting_config_map.put("config_id", config.getId());
		chatting_config_map.put("config_font", config.getFont());
		chatting_config_map.put("config_font_size", config.getFont_size());
		chatting_config_map.put("config_font_colour", config.getFont_colour());
		chatting_config_map.put("config_kfname", config.getKf_name());
		chatting_config_map.put("config_quick_reply_conten", config.getQuick_reply_content());
		chatting_config_map.put("config_quick_reply_open", config.getQuick_reply_open());
		chatting_config_list.add(chatting_config_map);
		
		admin_map.put("chatting_config_list", chatting_config_list);
		
		if(!admin_map.isEmpty()){
			result = new Result(0,"success",admin_map);
		}else{
			result = new Result(1,"error");
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/*
	 * 平台客服受理咨询信息请求，
	 */
	@SecurityMapping(title = "自营客服窗口", value = "/admin/plat_chatting_open.htm*", rtype = "admin", rname = "自营客服", rcode = "self_chatting", rgroup = "自营")
	@RequestMapping("/admin_plat_chatting_open.json")
	public void plat_chatting_open(HttpServletRequest request,
			HttpServletResponse response, String chatting_id) {
		Map plat_map = new HashMap();
		Result result = null;
		ModelAndView mv = new JModelAndView("chatting/plat_chatting_open.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Chatting chatting = this.chattingService.getObjById(CommUtil
				.null2Long(chatting_id));
		Map params = new HashMap();
		params.put("chatting_id", chatting.getId());
		params.put("plat_read", 0);// 平台客服未读信息
		List<ChattingLog> logs = this.chattinglogService
				.query("select obj from ChattingLog obj where obj.chatting.id=:chatting_id and obj.plat_read=:plat_read order by addTime asc",
						params, -1, -1);
		for (ChattingLog cl : logs) {// 查询所有未读信息，并将所有未读信息标记为已读
			cl.setPlat_read(1);// 设置为平台客服已读
			this.chattinglogService.update(cl);
		}
		if (chatting.getGoods_id() != null) {
			Long gid = chatting.getGoods_id();
			Goods goods = this.goodsService.getObjById(gid);
			mv.addObject("goods", goods);
		}
		mv.addObject("chatting", chatting);
		plat_map.put("user_name", chatting.getUser_name());
		
		mv.addObject("objs", logs);
		//[平台所有未读信息]
		List<Map> clog_list = new ArrayList<Map>();
		for(ChattingLog obj : logs){
			Map info_map = new HashMap();
			if(CommUtil.isNotNull(obj.getUser_id())  && CommUtil.isNotNull(obj.getStore_id()) || CommUtil.isNotNull(obj.getStore_id())){
				Map self_info_map = new HashMap();
				self_info_map.put("kf_name", obj.getChatting().getConfig().getKf_name());
				self_info_map.put("time", CommUtil.formatLongDate(obj.getAddTime()));
				self_info_map.put("font", obj.getFont());
				self_info_map.put("font_size", obj.getFont_size());
				self_info_map.put("font_colour", obj.getFont_colour());
				self_info_map.put("content", obj.getContent());
				clog_list.add(self_info_map);
				plat_map.put("clog_list", clog_list);
			}else{
				Map user_info_map = new HashMap();
				user_info_map.put("user_name", obj.getChatting().getUser_name());
				user_info_map.put("time", CommUtil.formatLongDate(obj.getAddTime()));
				user_info_map.put("font", obj.getFont());
				user_info_map.put("font_size", obj.getFont_size());
				user_info_map.put("font_colour", obj.getFont_colour());
				user_info_map.put("content", obj.getContent());
				clog_list.add(user_info_map);
				plat_map.put("clog_list", clog_list);
			}
		}
		
		if(plat_map.isEmpty()){
			result = new Result(1,"没有新的聊天信息");
		}else{
			result = new Result(0,"success",plat_map);
		}
		try {
			response.getWriter().print(Json.toJson(result, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 聊天信息设置
	 */

	@SecurityMapping(title = "自营客服窗口", value = "/admin/plat_chatting_set.htm*", rtype = "admin", rname = "自营客服", rcode = "self_chatting", rgroup = "自营")
	@RequestMapping("/admin_plat_chatting_set.json")
	public void plat_chatting_set(HttpServletRequest request,
			HttpServletResponse response, String chattingConfig_id,
			String kf_name, String content, String reply_open) {
		ChattingConfig config = this.chattingconfigService.getObjById(CommUtil
				.null2Long(chattingConfig_id));
		if (kf_name != null && !kf_name.equals("")) {
			config.setKf_name(kf_name);
		}
		if (content != null && !content.equals("")) {
			config.setQuick_reply_content(content);
		}
		if (reply_open != null && !reply_open.equals("")) {
			config.setQuick_reply_open(CommUtil.null2Int(reply_open));
			if (reply_open.equals("1")
					&& config.getQuick_reply_content() == null) {
				config.setQuick_reply_content("不能及时回复，敬请原谅！");
			}
		}
		this.chattingconfigService.update(config);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(config.getQuick_reply_open());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SecurityMapping(title = "自营客服窗口", value = "/admin/plat_chatting_save.htm*", rtype = "admin", rname = "自营客服", rcode = "self_chatting", rgroup = "自营")
	@RequestMapping("/admin_plat_chatting_save.json")
	public void plat_chatting_save(HttpServletRequest request,
			HttpServletResponse response, String text, String chatting_id,
			String font, String font_size, String font_colour) {
		ModelAndView mv = new JModelAndView("chatting/plat_chatting_log.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Chatting chatting = this.chattingService.getObjById(CommUtil
				.null2Long(chatting_id));
		ChattingLog log = new ChattingLog();
		log.setAddTime(new Date());
		log.setChatting(chatting);
		log.setContent(text);
		// 保存聊天记录字体信息
		log.setFont(font);
		log.setFont_size(font_size);
		log.setFont_colour(font_colour);
		// 保存平台聊天组件字体信息
		if (!font.equals(chatting.getConfig().getFont()) && !font.equals("")) {
			chatting.getConfig().setFont(font);
		}
		if (!font_size.equals(chatting.getConfig().getFont_size())
				&& !font_size.equals("")) {
			chatting.getConfig().setFont_size(font_size);
		}
		if (!font_colour.equals(chatting.getConfig().getFont_colour())
				&& !font_colour.equals("")) {
			chatting.getConfig().setFont_colour(font_colour);
		}
		this.chattingconfigService.update(chatting.getConfig());
		log.setPlat_read(1);// 自己发布的消息设置为自己已读
		this.chattinglogService.save(log);
		List<ChattingLog> logs = new ArrayList<ChattingLog>();
		logs.add(log);
		mv.addObject("objs", logs);// 发送消息即时显示
		// 重新设置sessin
		HttpSession session = request.getSession(false);
		session.removeAttribute("chatting_session");
		session.setAttribute("chatting_session", "chatting_session");
		String chatting_session = CommUtil.null2String(session
				.getAttribute("chatting_session"));
		if (session != null && !session.equals("")) {
			mv.addObject("chatting_session", chatting_session);
		}
		
	}

}
