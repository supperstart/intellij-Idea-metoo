package com.metoo.core.loader;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.support.WebApplicationContextUtils;

import com.metoo.core.security.SecurityManager;
import com.metoo.core.tools.SpringUtil;

/**
 * 
* <p>Title: ServletContextLoaderListener.java</p>

* <p>Description:系统基础信息加载监听器，目前用来加载系统权限数据，也可以将系统语言包在这里加载进来，该监听器会在系统系统的时候进行一次数据加载 
* 		1）WEB容器监听器ServletContextListener主要用来监听容器启动和销毁的时候需要做一些操作，就可以使用这个监听器来做。
* 		ServletContextListener在Spring启动前启动。

		2）ServletContextListener 接口有两个方法:contextInitialized,contextDestroyed。

        a. 在服务器加载web应用的时候，这个Listener将被调用。

        b. spring在contextInitialized中进行spring容器的初始化。
* </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: </p>

* @author 

* @date 2014-4-24

* @version koala_b2b2c v2.0 2015版 
 */
public class ServletContextLoaderListener implements ServletContextListener {

	/**
	 * 
	 * 当Servlet 容器启动Web应用时调用该方法。在调用完该方法之后，容器再对Filter 初始化，
	 * 并且对那些在Web 应用启动时就需要被初始化的Servlet 进行初始化。
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
		ServletContext servletContext = servletContextEvent.getServletContext();
		//获取系统权限
		SecurityManager securityManager = this
				.getSecurityManager(servletContext);
		Map<String, String> urlAuthorities = securityManager
				.loadUrlAuthorities();
		servletContext.setAttribute("urlAuthorities", urlAuthorities);
		
		
		SpringUtil.setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(servletContext));
		
	}
	
	/**
	 * 当Servlet 容器终止Web应用时调用该方法。在调用该方法之前，容器会先销毁所有的Servlet 和Filter 过滤器。
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		servletContextEvent.getServletContext().removeAttribute(
				"urlAuthorities");
	}

	/**
	 * @description 获取加载权限的bean
	 * @param servletContext
	 * @return
	 */
	protected SecurityManager getSecurityManager(ServletContext servletContext) {
		return (SecurityManager) WebApplicationContextUtils
				.getWebApplicationContext(servletContext).getBean(
						"securityManager");
	}
}
