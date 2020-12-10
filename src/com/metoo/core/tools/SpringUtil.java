package com.metoo.core.tools;

import org.springframework.context.ApplicationContext;

import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.impl.AreaServiceImpl;

public class SpringUtil {

	private static ApplicationContext applicationContext01;

	public static void setApplicationContext(ApplicationContext arg0) {
		
		applicationContext01 = arg0;
	}

	public static Object getObject(String id) {
		Object object = null;
		object = applicationContext01.getBean(id);
		return object;
	}

	public static Object getObject(Class<?> clazz) {
		Object object = null;
		try {
			object = applicationContext01.getBean(clazz);
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		return object;
	}

}
