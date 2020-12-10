package com.metoo.app.view.web.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.stereotype.Component;

import com.metoo.core.tools.CommUtil;

@Component
public class MobileTools {

	@Test
	public boolean verify(String mobile) {
		boolean flag = false;
		if (mobile != null || mobile.equals("")) {
			String pattern = "";
			String areaCode = mobile.substring(0, 3);// 国际电话区号码
			String telephone = mobile.substring(3, mobile.length());
			if(CommUtil.null2String(telephone).equals("88888888")){
				flag = true;
			}else{
				if(areaCode.equals("965")){
					pattern = "^965[0-9]{8}$";
				}else{
					pattern = "^[971|966]+(0[5][0-9]{8}|[5][0-9]{8})$";
				}
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(mobile);
				if (m.matches()) {
					flag = true;
				}
			}
		}
		return flag;
	}

	@Test
	public void testArea(){
		String mobile = "965512345678";
		String pattern = "^965[0-9]{8}$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(mobile);
		if (m.matches()) {
			System.out.println(true);
		}else{
			System.out.println(false);
		}
	} 
	public Map<String, String> mobile(String mobile) {
		Map<String, String> map = new HashMap<String, String>();
		String areaCode = mobile.substring(0, 3);// 国际电话区号码 如: 966 965 971
		String firstNumber = mobile.substring(3, 4);// 区号码 0
		String phoneNumber = mobile.substring(3, mobile.length());
		String userMobile = phoneNumber;
		String areaMobile = "";
		if(areaCode.equals("965")){
			mobile = areaCode + phoneNumber;
			userMobile = phoneNumber;
			areaMobile = phoneNumber;
		}else{
			if ("0".equals(firstNumber)) {
				mobile = areaCode + phoneNumber.substring(1, phoneNumber.length());
				userMobile = phoneNumber;
				areaMobile = phoneNumber.substring(1);
			} else {
				if ("5".equals(firstNumber)) {
					mobile = areaCode + phoneNumber;
					userMobile = 0 + phoneNumber;
					areaMobile = phoneNumber;
				}
			}
		}
		map.put("areaMobile", areaMobile);// 去0和区号
		map.put("phoneNumber", phoneNumber);// 实际电话号码
		map.put("mobile", mobile);//加区号号码
		map.put("userMobile", userMobile);//加0电话
		return map;

	}

	public String verifyUsername(String userName) {
		String name = "";
		if (userName != null && !"".equals(userName)) {
			String prefix = userName.substring(0, 2);
			switch (prefix) {
			case "05":
				name = userName;
				break;
			default:
				name = "0" + userName;
			}
		}
		return name;
	}

	@Test
	public void verifyUsername() {
		String name = "";
		String userName = "5asdfpppp";
		if (userName != null && !"".equals(userName) && 9 <= userName.length() && userName.length() <= 10) {
			String prefix = userName.substring(0, 2);
			switch (prefix) {
			case "05":
				name = userName;
				break;
			default:
				name = "0" + userName;
			}
		} else {
			name = userName;
		}
		System.out.println(name);
	}

	@Test
	public void min() {
		List list = new ArrayList();
		list.add(499.00);
		list.add(998.00);
		list.add(1996.00);
		list.add(499.00);
		list.add(998.00);
		list.add(1996.00);
		System.out.println(Collections.min(list));
	}
	
	
}
