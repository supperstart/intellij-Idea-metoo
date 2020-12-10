package com.metoo.core.security.support;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.util.TextUtils;

import com.iskyshop.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * 
* <p>Title: LoginAuthenticationFilter.java</p>

* <p>Description: 重写SpringSecurity登录验证过滤器,验证器重新封装封装用户登录信息，可以任意控制用户与外部程序的接口，如整合UC论坛等等
* 	补充：认证处理过滤器
* </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南创发科技有限公司 www.iskyshop.com</p>

* @author erikzhang

* @date 2014-4-24

* @version iskyshop_b2b2c v2.0 2015版 
 */
public class LoginAuthenticationFilter extends AuthenticationProcessingFilter {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;

	public Authentication attemptAuthentication(HttpServletRequest request)
			throws AuthenticationException {
		// 状态， admin表示后台，user表示前台,seller表示商家
		String login_role = request.getParameter("login_role");
		String login_name = request.getParameter("username");
		String login_pass = request.getParameter("password");
		String login_token = request.getParameter("token");
		String type = request.getParameter("type");
		String login_type = request.getParameter("login_type");

		if (login_role == null || login_role.equals(""))
			login_role = "user";
		String id = request.getParameter("id");

		HttpSession session = request.getSession();
		session.setAttribute("login_role", login_role);
		session.setAttribute("login_name", login_name);
		session.setAttribute("login_pass", login_pass);
		session.setAttribute("login_token", login_token);
		session.setAttribute("type", type);
		session.setAttribute("id", id);
		session.setAttribute("ajax_login",
				CommUtil.null2Boolean(request.getParameter("ajax_login")));
		session.setAttribute("login_type", login_type);
		boolean flag = true;
		if("weixin".equals(type)){//门户登录 不需要验证码
			flag = true;
		}else{
			if (session.getAttribute("verify_code") != null) {
				String code = request.getParameter("code") != null ? request
						.getParameter("code").toUpperCase() : "";
				if (!session.getAttribute("verify_code").equals(code)) {
					flag = false;
				}
			}
		}
		User user = null;
		String loginPass = "";
		if(login_token != null && !login_token.equals("")){
			Map params = new HashMap();
			/*params.put("email", login_name);
			params.put("mobile", login_name);
			params.put("userName", login_name.replace(" ",""));
			params.put("deleteStatus", 0);
			List<User> users = this.userService
					.query("select obj from User obj where obj.deleteStatus=:deleteStatus and obj.userName =:userName or obj.email=:email or obj.mobile=:mobile",
							params, -1, -1);*/
			params.put("app_login_token", login_token);
			List<User> users =  this.userService.query("select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
					params, -1, -1);
			if(users.size() > 0){
				user = users.get(0);
				loginPass = user.getPassword();
			}
		}else{
			/*==============给填写错误密码的用户添加错误次数 ==================================*/
		    loginPass = Md5Encrypt.md5(login_pass).toLowerCase();
			user = this.userService.getObjByProperty(null, "userName",
					login_name);
		}
		if(user != null&!"weixin".equals(type)){
			String Userpass = user.getPassword();
			if(!Userpass.equals(loginPass)){//判断当前用户密码是否正确
				SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss");
				String Datetime = simpleDate.format(new Date());//获取系统当前时间转化为字符串
				String controldate = "";
				if(user.getControlDate() != null && !"".equals(user.getControlDate())){
					//获取用户输入错误密码时间加十五分钟
					Calendar nowTime = Calendar.getInstance();
					nowTime.setTime(user.getControlDate());
					nowTime.add(Calendar.MINUTE, 15);
					Date date15 = nowTime.getTime();
					
					controldate = simpleDate.format(date15);
					//判断当前时间是否小于用户输入错误密码时间加15分钟
					if(Long.parseLong(Datetime) < Long.parseLong(controldate)){
						//user.setControlDate(new Date());
						if(user.getControlNumber() != null && !"".equals(user.getControlNumber())){
							Long number = user.getControlNumber() + 1;
							user.setControlNumber(number);
						}else{
							Long number = 1L;
							user.setControlNumber(number);
						}
						this.userService.update(user);
					}else{
						user.setControlDate(new Date());
						Long number = 1L;
						user.setControlNumber(number);
						this.userService.update(user);
					}
				}else{
					user.setControlDate(new Date());
					Long number = 1L;
					user.setControlNumber(number);
					this.userService.update(user);
				}
				
			}else{//用户输入正确密码清零当前记录错误密码
				user.setControlDate(new Date());
				Long number = 0L;
				user.setControlNumber(number);
				this.userService.update(user);
			}
		}
		/*==============end========================================*/
		
		
		if (!flag) {
			String username = obtainUsername(request);
			String password = "";// 验证码不正确清空密码禁止登陆
			username = username.trim();
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
					username, password);
			if ((session != null) || (getAllowSessionCreation())) {
				request.getSession().setAttribute(
						"SPRING_SECURITY_LAST_USERNAME",
						TextUtils.escapeEntities(username));
			}
			setDetails(request, authRequest);
			return getAuthenticationManager().authenticate(authRequest);
		} else {
			String username = "";
			if (CommUtil.null2Boolean(request.getParameter("encode"))) {
				username = CommUtil.decode(obtainUsername(request)) + ","
						+ login_role;
			} else
				username = obtainUsername(request) + "," + login_role;
			String password = obtainPassword(request);
			username = username.trim();
//			
			System.out.println("登录信息：用户名 = "+username+"密码="+password);
			UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
					username, password);
			
			if ((session != null) || (getAllowSessionCreation())) {
				request.getSession().setAttribute(
						"SPRING_SECURITY_LAST_USERNAME",
						TextUtils.escapeEntities(username));
				Object s = request.getSession().getAttribute("SPRING_SECURITY_LAST_USERNAME");
				System.out.println();
			}
			setDetails(request, authRequest);
			
			try {
				return getAuthenticationManager().authenticate(authRequest);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
			
		}
	}

	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException {
		// TODO Auto-generated method stub
		request.getSession(false).removeAttribute("verify_code");

		super.onSuccessfulAuthentication(request, response, authResult);
	}

	protected void onUnsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException {
		// TODO Auto-generated method stub
		String uri = request.getRequestURI();
		super.onUnsuccessfulAuthentication(request, response, failed);
	}
}
