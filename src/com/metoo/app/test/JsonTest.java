package com.metoo.app.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.nutz.json.Json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.foundation.domain.EmailModel;
import com.metoo.foundation.domain.User;

public class JsonTest {

	private static final String JSON_ARRAY_STR = "[{16:17},{14:123}]";
	
	@Test
	public void json(){
		JSONArray ja = JSON.parseArray(JSON_ARRAY_STR);
		JSONArray jsonArray1 = JSONArray.parseArray(JSON_ARRAY_STR);//因为JSONArray继承了JSON，所以这样也是可以的
		
		EmailModel email = new EmailModel();
		email.setType("buyer");
		email.setName("hkk");
		
		String json = JSON.toJSONString(email);
		
		System.out.println("json 字符串:" + json);
		
		JSONObject jsonObj = JSONObject.parseObject(json);
		
		System.out.println("Json Object: " + jsonObj);
		System.out.println("Json Object name:" + jsonObj.get("name"));
		
		Map map = JSON.parseObject(json, Map.class);
		System.out.println("map : " + map);
		System.out.println("map name : " + map.get("name"));
		
		Map map1 = Json.fromJson(Map.class, json);
		System.out.println("map1 : " + map1);
		
		System.out.println("JSONObject: " + JSON.parseObject(json).getClass());
		
		List<EmailModel> emailModels = JSON.parseArray("[{'name':'hkk01','type':'buyer'}, {'name':'hkk02','type':'buyer'}]", EmailModel.class);
		System.out.println("emailModels: " + emailModels);
		System.out.println("emailModels name01: " + emailModels.get(0).getName());
		System.out.println("emailModels name02: " + emailModels.get(1).getName());
		
		String model_ids = "1:2,3:4";
		String[] str = model_ids.split(",");
		System.out.println(str);
		System.out.println(str);
	}
	
	@Test
	public void model(){
		//String model_ids = "{'1':'2','3':'4'}";
		String model_ids = "1:2,3:4";
		model_ids = "{" + model_ids +"}";
		Map map = JSON.parseObject(model_ids, Map.class);
		System.out.println(map.get(1));
	}
}
