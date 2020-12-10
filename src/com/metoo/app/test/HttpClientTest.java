package com.metoo.app.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.rules.Timeout.Builder;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

/**
 * <p>
 * Description : StringHttpMessageConverter :使用http
 * </p>
 * 
 * @author 46075
 *
 */
@Controller
public class HttpClientTest {

	/*
	 * org.apache.http.client.config.RequestConfig.Builder customReqConf =
	 * RequestConfig.custom(); customReqConf.setConnectTimeout(10000);
	 * customReqConf.setSocketTimeout(10000);
	 * customReqConf.setConnectionRequestTimeout(10000);
	 * httpPost.setConfig(customReqConf.build());
	 */

	private static HttpClient client;
	String url = "http://local.soarmall.com/getGoods.json";
	private static String CHARSET = "UTF-8";
	
	

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws InterruptedException
	 * @descript 使用HttpClient 请求
	 */
	@RequestMapping(value = "/request.json")
	@ResponseBody
	public String send(HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
		client = new HttpClient();
		// HttpPost httpPost = new
		// HttpPost("http://local.soarmall.com/getGoods.json");
		GetMethod get = new GetMethod("http://local.soarmall.com/getGoods.json");
		get.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		HttpConnectionManagerParams managerParams = client.getHttpConnectionManager().getParams();
		// 设置连接超时时间(单位毫秒)
		managerParams.setConnectionTimeout(5000);
		// 设置读数据超时时间(单位毫秒)
		managerParams.setSoTimeout(5000);
		String res = "";
		int j = 0;
		int status = 0;
		String ress = "";
		for (int i = 1; i <= 3; i++) {
			j++;
			if (j == 3) {
				System.out.println("发从第" + i + "次请求");
				ress = 	ress = "返回回来的数据：" + null + "  状态值：" + 200 + "循环次数" + j;
			} else {
				try {
					System.out.println("发从第" + i + "次请求");
					status = client.executeMethod(get);
					res = get.getResponseBodyAsString().trim();
					ress = "返回回来的数据：" + res + "  状态值：" + status + "循环次数" + j;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return Json.toJson(ress, JsonFormat.compact());
	}

	@RequestMapping(value = "/request2.json")
	@ResponseBody
	public String send2(HttpServletRequest request, HttpServletResponse response) {
		String ress = "返回回来的数据";
		JSON.parseObject("");
		return Json.toJson(ress, JsonFormat.compact());
	}

}
