package com.metoo.core.weixin.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.metoo.core.weixin.utils.MD5Util;

public class payTest {

	private static String key = "XqsTxIsBkoePHCKLPNT225AwtzENMWtK";
	public static void main(String[] args) {
		System.out.println(".....模拟微信支付...");
		//微信api提供的参数
		String appid = "wx230ecfa4c5bdb96d";
		String mch_id = "1463804502";
		String device_info="10000";
		String body = "test";
		String nonce_str = "ibuaiVcKdpRxkhJA";
		
		SortedMap<Object,Object> parameters = new TreeMap<Object, Object>();
		parameters.put("appid",appid);
		parameters.put("mch_id",mch_id);
		parameters.put("device_info",device_info);
		parameters.put("body",body);
		parameters.put("nonce_str", nonce_str);
		
		String characterEncoding = "UTF-8";
		String mySign = createSign(characterEncoding,parameters);
		
		
		System.out.println(mySign);
		
	}
	
	public static String createSign(String characterEncoding,SortedMap<Object, Object> parameters){
		StringBuffer sb = new StringBuffer();
		Set st = parameters.entrySet();
		Iterator it = st.iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			sb.append(k+"="+v+"&");
		}
		sb.append("key="+key);
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		
		return sign;
	}
}
