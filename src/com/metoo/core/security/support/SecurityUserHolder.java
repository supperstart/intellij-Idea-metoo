package com.metoo.core.security.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.metoo.foundation.domain.User;

/**
 * 
* <p>Title: SecurityUserHolder.java</p>

* <p>Description: SpringSecurity用户获取工具类，该类的静态方法可以直接获取已经登录的用户信息 </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南创发科技有限公司 www.koala.com</p>

* @author erikzhang

* @date 2014-4-24

* @version koala_b2b2c v2.0 2015版 
 */
public class SecurityUserHolder {

	/**
	 * Returns the current user
	 * 获取当前已登录用户.
	 * @return
	 */
	public static User getCurrentUser() {
		if (SecurityContextHolder.getContext().getAuthentication() != null
				&& SecurityContextHolder.getContext().getAuthentication()
						.getPrincipal() instanceof User) {

			return (User) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal();
		} else {
			User user = null;
			if (RequestContextHolder.getRequestAttributes() != null) {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
						.getRequestAttributes()).getRequest();
				user = (request.getSession().getAttribute("user") != null ? (User) request
						.getSession().getAttribute("user") : null);
				// System.out.println(user != null ? user.getUserName() : "空");
			}
			return user;
		}

	}
}
