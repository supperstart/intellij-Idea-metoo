package com.metoo.app.manage.push.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.Notification;

@Controller
public class JPushAction {
	
	@RequestMapping("/jpush.json")
	public void push(HttpServletRequest request, HttpServletResponse response){
		//这里同学们就可以自定义推送参数了
		Map<String, String> parm = new HashMap<String, String>();
		//设置提示信息,内容是文章标题
		parm.put("msg","测试已通过");
		parm.put("title", "metoo");
		Map map = jpushAll(parm);
		try {
			response.getWriter().print(Json.toJson(map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//极光推送>>All所有平台
	public  Map jpushAll(Map<String, String> parm) {
		Map map = new HashMap();
		String master_secret = "6637c558d2ee52fc024b75b9";//743edccb3dc3ab5053318a6c//6155875638a414b9585c6748
		String app_key = "8cf0a3bb37d600189400fee8";//8cf0a3bb37d600189400fee8//01b20662870e76da5972c56c 81ef3666df98711db2161462
		//创建JPushClient
		JPushClient jpushClient = new JPushClient(master_secret, app_key);
		//创建option
		PushPayload payload = PushPayload.newBuilder()
				.setPlatform(Platform.all())  //所有平台的用户
				//.setAudience(Audience.registrationId("registrationId前端给"))//registrationId指定用户
				.setAudience(Audience.all())
				.setNotification(Notification.newBuilder()
						/*.addPlatformNotification(IosNotification.newBuilder() //发送ios
								.setAlert(parm.get("msg")) //消息体
								.setBadge(+1)
								.setSound("happy") //ios提示音
								.addExtras(parm) //附加参数
								.build())*/
						.addPlatformNotification(AndroidNotification.newBuilder() //发送android
								.setTitle(parm.get("title"))
								.addExtras(parm) //附加参数
								.setAlert(parm.get("msg")) //消息体
								.build())
						.build())
				.setOptions(Options.newBuilder().setApnsProduction(true).build())//指定开发环境 true为生产模式 false 为测试模式 (android不区分模式,ios区分模式)
				.setMessage(Message.newBuilder().setMsgContent(parm.get("msg")).addExtras(parm).build())//自定义信息
				.build();
				
		try {
			PushResult result = jpushClient.sendPush(payload);
			JSONObject json = JSONObject.parseObject(result.toString());
			map.put("code", json.get("statusCode"));
		} catch (APIConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return map;
		}
}
