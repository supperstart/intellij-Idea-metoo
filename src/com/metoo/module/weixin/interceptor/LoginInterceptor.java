package com.metoo.module.weixin.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.weixin.utils.CommonUtil;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.weixin.view.tools.Base64Tools;

public class LoginInterceptor implements HandlerInterceptor {
	@Autowired
	private IUserService userService;
	@Autowired
	private Base64Tools base64Tools;

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub
		

	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		// TODO Auto-generated method stub
		/*String openid = request.getParameter("openid");
		String url = request.getParameter("url");
		if(openid!=null&&url!=null){
			request.getSession(false).setAttribute("his_url",
					url + "&openid=" + openid);
		}
		boolean ret = true;
			if (openid != null && !openid.equals("")&&SecurityUserHolder.getCurrentUser() == null) {
					Map params = new HashMap();
					params.put("openid", openid);
					List<User> user = this.userService
							.query("select obj from User obj where obj.openId=:openid",
									params, -1, -1);
					if (user.size() == 1) {
						String userName = user.get(0).getUserName();
						String password = user.get(0).getPassword();
						if (userName != null && !userName.equals("")) {
							String userMark = this.base64Tools.decodeStr(user
									.get(0).getUserMark());
							response.sendRedirect("/koala_login.json?username="
									+ userName
									+ "&password="
									+ password
									+ "&encode=true");
						}
					}
			}*/
		
		System.out.println("....login interceptor ...");
		User user = SecurityUserHolder.getCurrentUser();

		System.out.println("request url : " + CommUtil.get_all_url(request));
		System.out.println("puid 2 : "+ request.getParameter("puid"));
		if(user == null){
			System.out.println("ljqljqljqljqljqljqljqljqljq");
			
			request.getSession().setAttribute("returnUrl", CommUtil.get_all_url(request));
			CommonUtil.weixinLogin(request, response);
			return false;
		}

		return true;
	}

}
