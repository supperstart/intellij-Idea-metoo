package com.metoo.core.weixin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ServletProxy extends HttpServlet {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	public void init() throws ServletException {
		
		super.init();
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		AutowireCapableBeanFactory factory = wac.getAutowireCapableBeanFactory();
		factory.autowireBean(this);
	}
	
}