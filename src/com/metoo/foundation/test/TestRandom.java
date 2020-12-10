package com.metoo.foundation.test;

import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class TestRandom {

	/**
	 * @param args
	 */
	@Test
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Random random = new Random();
		System.out.println(random.nextInt(3));
	}
	
	@RequestMapping("/random.json")
	public static final void randomString(HttpServletRequest request, HttpServletResponse response) {
		int len = 4;
		char[] numbersAndLetters = ("0123456789"
				+ "0123456789").toCharArray();
		if (len < 1) {
			
		}
		Random randGen = new Random();
		char[] randBuffer = new char[len];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[randGen.nextInt(19)];//nextInt:这里是一个方法的重载，参数的内容是指定范围
		}
		System.out.println(new String(randBuffer));
		try {
			response.getWriter().print(new String(randBuffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*	@RequestMapping("/1zhen.json")
	public Object ZhenziDx(HttpSession session) {//number发送的手机号
		String number = "15838152042";
		try {
			JSONObject json = null;
			//生成6位验证码
			int num1=(int)(Math.random()*10000);
			String SjNum = String.valueOf(num1);
			//发送短信
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("number", number);
			map.put("SjNum", "metoo科技,您的验证码为:" + SjNum + "，该码有效期为1分");
			ZhenziSmsClient client = new ZhenziSmsClient("https://sms_developer.zhenzikj.com","103897", "d2610471-db00-4f1e-bbba-72f23989e17f");
			String result = client.send(number,"metoo科技,您的验证码为:" + SjNum + "，该码有效期为1分");
			json = JSONObject.parseObject(result);
			if(json.getIntValue("code") != 0) {//发送短信失败
				return JSON.toJSONString("fail");	
			}else {
				// 将认证码存入SESSION
				session.setAttribute("SjNum ", json);	
				session.setMaxInactiveInterval(1*60);
				return JSON.toJSONString("success");	 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	*/
}
