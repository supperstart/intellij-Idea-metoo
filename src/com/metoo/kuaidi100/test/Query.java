package com.metoo.kuaidi100.test;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.metoo.core.tools.Md5Encrypt;
import com.metoo.kuaidi100.post.HttpRequest;
import com.metoo.kuaidi100.utils.MD5;

public class Query {
	public static void main(String[] args) throws Exception {

		String param ="{\"com\":\"yunda\",\"num\":\"1202443176364\"}";
		String customer ="EF91A38461385824F6FB14D0C594E54E";
		String key = "CNbeXYJm2595";
		String sign =MD5.encode(param+key+customer);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("param",param);
		params.put("sign",sign);
		params.put("customer",customer);
		String resp;
		try {
			resp = new HttpRequest().postData("http://poll.kuaidi100.com/poll/query.do", params, "utf-8").toString();
			System.out.println(resp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
