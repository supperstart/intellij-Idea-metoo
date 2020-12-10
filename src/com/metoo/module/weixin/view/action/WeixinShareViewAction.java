package com.metoo.module.weixin.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.weixin.utils.WeixinUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * 
 * 
* <p>WeixinShareViewAction</p>

* <p>Description:微信分享类 </p>

* @version koala_b2b2c_2015
 */
@Controller
public class WeixinShareViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IUserService userService;
	
	@RequestMapping("/wap/share.htm")
	public ModelAndView share(HttpServletRequest request,HttpServletResponse response,String good_id){
		ModelAndView mv = new JModelAndView("wap/share.html", configService
				.getSysConfig(), this.userConfigService.getUserConfig(), 1,
				request, response);
		if(StringUtils.isEmpty(good_id)){
			good_id = (String) request.getAttribute("good_id");
		}
		User user = SecurityUserHolder.getCurrentUser();
		SysConfig config = configService.getSysConfig();
		Map<String,Object> map = new HashMap<String, Object>();
		map = WeixinUtil.getWxConfig(request,config);
		if(map != null){
			mv.addObject("appId", map.get("appId"));
			mv.addObject("timestamp", map.get("timestamp"));
			mv.addObject("nonceStr", map.get("nonceStr"));
			mv.addObject("signature", map.get("signature"));
			mv.addObject("jsapi_ticket", map.get("jsapi_ticket"));
			
			config.setWeixin_jsapi_ticket(map.get("jsapi_ticket").toString());
			configService.update(config);
		}
		Goods obj = this.goodsService
				.getObjById(CommUtil.null2Long(good_id));
		String webPath = CommUtil.getURL(request);
		String title = "",link="",imgUrl = "";
		if(obj != null){
			title = obj.getGoods_name();
			link = webPath+"/wap/goods.htm?id="+obj.getId();
			imgUrl = webPath+ "/"
					+ obj.getGoods_main_photo().getPath()
					+ "/"
					+ obj.getGoods_main_photo().getName();
		}else{
			
			link = CommUtil.get_all_url(request);
			imgUrl = webPath+"/"+config.getWeixin_qr_img().getPath()+"/"+config.getWeixin_qr_img().getName();
		}
		
		mv.addObject("webPath", webPath);
		mv.addObject("title", title);
		//puid 分享者
		if(user != null && !StringUtils.isEmpty(user.getOpenId())){
			link = link+"&puid="+user.getOpenId();
		}
		mv.addObject("link", link);
		mv.addObject("imgUrl", imgUrl);
		mv.addObject("user",user);
		
		return mv;
		
	}
	
	@RequestMapping("/wap/shareSuccess.htm")
	public void goods(HttpServletRequest request,
			HttpServletResponse response) {
		User user = SecurityUserHolder.getCurrentUser();
		Map<String,Object> json_map = new HashMap<String,Object>();
		json_map.put("result", "分享失败！");
		if(user != null){
			System.out.println(" add integral ...");
			//分享成功增加10积分
			user.setIntegral(user.getIntegral()+10);
			this.userService.save(user);
			
			IntegralLog log = new IntegralLog();
			log.setAddTime(new Date());
			log.setContent("分享商品增加"
					+ this.configService.getSysConfig()
							.getMemberRegister() + "分");
			log.setIntegral(this.configService.getSysConfig()
					.getMemberRegister());
			log.setIntegral_user(user);
			log.setType("reg");
			this.integralLogService.save(log);
			

			json_map.put("result", "分享成功，增加10积分！");
		}
		
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			System.out.println(Json.toJson(json_map, JsonFormat.compact()));
			writer.print(Json.toJson(json_map, JsonFormat.compact()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
