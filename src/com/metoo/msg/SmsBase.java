package com.metoo.msg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.app.buyer.domain.Http;
import com.metoo.foundation.domain.SysConfig;
/**
 * 
 * <p>
 * Title: SmsBase.java
 * </p>
 * 
 * <p>
 * Description: 系统手机短信发送类，结合第三方短信平台进行管理使用
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
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版
 */
public class SmsBase {
	private String url;
	private String id;
	private String pwd;

	public SmsBase(String url, String id, String pwd) {
		this.url = url;
		this.id = id;
		this.pwd = pwd;
	}

	public String SendSms(String mobile, String content)
			throws UnsupportedEncodingException {
		String code = "1"; //1:代表失败 0：代表成功
		Integer x_ac = 10;// 发送信息
		HttpURLConnection httpconn = null;
		content = Jsoup.clean(content, Whitelist.none()).replace("&nbsp;", "")
				.trim();
		String senderId = "";
		//组装请求参数
			JSONObject map=new JSONObject();
			map.put("account", id);
			map.put("password", pwd);
			map.put("msg", content);
			map.put("mobile", mobile);
			map.put("senderId", senderId);
			String params=map.toString();
		try {
			String HttpSendSms=Http.post("http://intapi.253.com/send/json", params);
			JSONObject jsonObject =  JSON.parseObject(HttpSendSms);
			code = jsonObject.get("code").toString();
			String msgid = jsonObject.get("msgid").toString();
			String error = jsonObject.get("error").toString();
			System.out.println("状态码:" + code + ",状态码说明:" + error + ",消息id:" + msgid);
			//{"code": "108", "error":"手机号码格式错误", "msgid":""}
			//logger.info("状态码:" + code + ",状态码说明:" + error + ",消息id:" + msgid);
		} catch (Exception e) {
			// TODO: handle exception
			//logger.error("请求异常：" + e);
		}
		return code;
	}
	
	public String SendSmsh(String mobile, String content)
			throws UnsupportedEncodingException {
		Integer x_ac = 10;// 发送信息
		HttpURLConnection httpconn = null;
		String result = "-20";
		content = Jsoup.clean(content, Whitelist.none()).replace("&nbsp;", "");// 过滤所有html代码
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("?id=").append(id);
		sb.append("&pwd=").append(pwd);
		sb.append("&to=").append(mobile);
		sb.append("&content=").append(URLEncoder.encode(content, "gb2312")); // 注意乱码的话换成gb2312编码
		try {
			URL url = new URL(sb.toString());
			httpconn = (HttpURLConnection) url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					httpconn.getInputStream()));
			result = rd.readLine();
			rd.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (httpconn != null) {
				httpconn.disconnect();
				httpconn = null;
			}

		}
		return result;
	}

	
}
