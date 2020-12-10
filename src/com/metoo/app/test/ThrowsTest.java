package com.metoo.app.test;



import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.metoo.core.tools.SpringUtil;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IUserService;

@Controller
public class ThrowsTest {

	/**
	 * @description 测试 数组下标越界异常
	 */
	@Test
	public void testArrayIndexOutOfBoundsExceptionThrows() {
		int[] index = new int[] { 1, 2 };
		int number = 0;
		String message = "正常响应";
		for (int i = 0; i < 3; i++) {
			if (i >= index.length) {
				message = "手动抛出异常后不能正常响应客户端";
				throw new ArrayIndexOutOfBoundsException();
			}
			number = index[i];
			System.out.println(number);
		}
		System.out.println(message);
	}

	@Test
	public void testArrayIndexOutOfBoundsExceptionTryCatch() {
		int[] index = new int[] { 1, 2 };
		int number = 0;
		String message = "正常响应";
		for (int i = 0; i < 3; i++) {
			try {
				number = index[i];
			} catch (Exception e) {
				// TODO Auto-generated catch block
				message = "try catch 后仍可正常响应客户端";
				e.printStackTrace();
			}
			System.out.println(number);
		}
		System.out.println(message);
	}

	/**
	 * @description 测试数学运算异常 ArithmeticException
	 */
	@Test
	public void testArithmeticException() {
		// ApplicationContext ctx = new Classpath
		IUserService userService = (IUserService) SpringUtil.getObject(IUserService.class);
		int divisor = 2;
		int dividend = 0;
		boolean flag = false;
		/*
		 * if(dividend == 0){ throw new ArithmeticException(); }
		 */
		User user = new User();
		user.setUserName("ari1");
		userService.save(user);
		int result = divisor / dividend;
	}

	@RequestMapping("/testArithmeticException")
	@ResponseBody
	public void testArithmeticExceptionV2(){
		//ApplicationContext ctx = new Classpath
		IUserService  userService = (IUserService)SpringUtil.getObject(IUserService.class);
		int divisor = 2;
		int dividend = 0;
		boolean flag = false;
		/*if(dividend == 0){
			throw new ArithmeticException();
		}*/
		User user = new User();
		user.setUserName("ari7");
		try {
			userService.save(user);
			//int result = divisor/dividend;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
