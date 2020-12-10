package com.metoo.app.test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.UserLog;
import com.metoo.foundation.service.IUserService;

public class JavaBeanTest {

	public static void main(String[] args) throws IntrospectionException {
		  BeanInfo beanInfo = Introspector.getBeanInfo(UserLog.class);
		  PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
		  for(int i=0;i<pds.length;i++){
	            System.out.println(pds[i].getName());
	        }
	}
	
	@Test
	public void aplicationTest(){
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext-configuration.xml");
		User user = applicationContext.getBean(User.class);
		System.out.println(user);
	}
}
