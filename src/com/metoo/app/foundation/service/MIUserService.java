package com.metoo.app.foundation.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metoo.foundation.domain.User;

public interface MIUserService {

	/**
	 * 根据用户id得到User对象
	 * @param id
	 * @return
	 */
	User getObjById(Long id);
	
	/**
	 * 用户=更新
	 * @param user
	 * @return
	 */
	boolean update(User user);
	
	String metoo_account(HttpServletRequest request,HttpServletResponse response, String token);
	
	String account_metoo_save(HttpServletRequest request, HttpServletResponse response,
			String area_id, String birthday, String token);
	
	String account_metoo_password_save(HttpServletRequest request, HttpServletResponse response,
			String old_password, String new_password, String token);
	
	String  account_email_save(HttpServletRequest request, HttpServletResponse response,
			String password, String email, String token);
}
