package com.metoo.app.test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class SMSTest {

	
	public static void main(String[] args) {
		String str = "97136547899";
//		String pattern = "^[0-9]+a$";
//		String pattern = "^[a-z0-9_-]{3,15}$";
//		String pattern = "^asd+[a-z0-9_-]{3,15}$";
		String pattern = "^(971)([0|5])[0-9]{8,9}$";
		Pattern r = Pattern.compile(pattern);//将给定的正则表达式编译成模式
		Matcher m = r.matcher(str);
		System.out.println(m.matches());
	}
	
	@Test
	public void phone(){
//		String mobile = "97105033214927";
//		String pattern = "^([971])+([0][5]|[5])[0-9]\\d{8,9}$";
		String chare = "码云云3";
		String pattern = "^[\u4e00-\u9fa5]{1,5}\\d$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(chare);
		System.out.println(m.matches());
	}
}
