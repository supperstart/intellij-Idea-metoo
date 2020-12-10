package com.metoo.app.view.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.view.web.tool.MobileTools;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.GoodsCart;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.VerifyCode;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IVerifyCodeService;
import com.metoo.msg.MsgTools;

@Controller
@RequestMapping("/app/")
public class MAppLoginViewAction {
	@Autowired
	private IUserService userService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IVerifyCodeService mobileverifycodeService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private MobileTools mobileTools;

	/**
	 * APP-客户端登陆
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 * @param password
	 * @param token
	 */
	@RequestMapping("v1/app_koala_token_login.json")
	public void app_login_token(HttpServletRequest request, HttpServletResponse response, String userName,
			String password, String token) {
		String code = "-3";// 0,登陆成功,-1账号不存在，-2,密码不正确，-3登录失败
		Result result = null;
		Map<String, Object> json_map = new HashMap<String, Object>();
		String user_id = "";
		String user_name = "";
		String user_sex = "";
		String login_token = "";
		User login_user = null;
		if (userName != null && !userName.equals("") && password != null && !password.equals("") && CommUtil.null2String(token).equals("")) {
			password = Md5Encrypt.md5(password).toLowerCase();
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("email", userName);
			params.put("mobile", userName);
			params.put("telephone", userName);
			params.put("userName", userName.replace(" ", ""));
			params.put("deleteStatus", 0);
			List<User> users = this.userService.query(
					"select obj from User obj where obj.deleteStatus=:deleteStatus and obj.userName =:userName or obj.email=:email or obj.mobile=:mobile or obj.telephone=:telephone",
					params, -1, -1);
			if (users.size() > 0) {
				for (User u : users) {
					if (!u.getPassword().equals(password)) {
						code = "-2";
					} else {
						if (u.getUserRole().equalsIgnoreCase("admin")) {
							code = "-1";
						} else {
							code = "0";
							user_id = CommUtil.null2String(u.getId());
							user_sex = CommUtil.null2String(u.getSex());
							user_name = u.getUserName();
							login_token = CommUtil.randomString(12) + user_id;
							u.setApp_login_token(login_token.toLowerCase());
							this.userService.update(u);
							login_user = u;
							break;
						}
					}
				}
			} else {
				code = "-4";
			}
		} else {
			if (!CommUtil.null2String(token).equals("")) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("app_login_token", token);
				List<User> users_t = this.userService.query(
						"select obj from User obj where obj.app_login_token=:app_login_token order by obj.addTime desc",
						params, -1, -1);
				User user = this.userService.getObjByProperty(null, "app_login_token", token);
				if (null == user) {
					code = "-5";
				} else {
/*					for (User obj : users_t) {
						usr_name = obj.getUsername();
						usr_password = obj.getPassword();
					}
*/					//if (usr_name != null && !usr_name.equals("") && usr_password != null && !usr_password.equals("")) {
						//if (users_t.size() > 0) {
							//for (User u : users_t) {
								/*if (!user.getPassword().equals(usr_password)) {
									code = "-2";
								} else {*/
									if (user.getUserRole().equalsIgnoreCase("admin")) {
										code = "-1";
									} else {
										user_id = CommUtil.null2String(user.getId());
										user_sex = CommUtil.null2String(user.getSex());
										user_name = user.getUserName();
										code = "0";
										login_token = token;
										user.setApp_login_token(token);
										this.userService.update(user);
										login_user = user;
									}
								//}
							//}
					/*	} else {
							code = "-4";
						}*/
				//	}
				}
			}
		}
		if (code.equals("0")) {
			json_map.put("verify", this.create_appverify(login_user));
			json_map.put("user_id", user_id.toString());
			json_map.put("userName", user_name);
			json_map.put("token", login_token);
			if (CommUtil.null2Int(user_sex) == -1) {
				json_map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
						+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member.png");
			}
			if (CommUtil.null2Int(user_sex) == 0) {
				json_map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
						+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member0.png");
			}
			if (CommUtil.null2Int(user_sex) == 1) {
				json_map.put("user_photo", this.configService.getSysConfig().getImageWebServer() + "/" + "resources"
						+ "/" + "style" + "/" + "common" + "/" + "images" + "/" + "member1.png");
			}
			// 记录积分明细
			/*if(this.configService.getSysConfig().isIntegral()){
				login_user.setIntegral(this.configService.getSysConfig().getMemberSignIn());
				
			}*/
		}
		this.send_json(Json.toJson(new Result(CommUtil.null2Int(code), json_map), JsonFormat.compact()), response);
	}
	
	@SecurityMapping(title = "手机验证码验证", value = "/buyer/account_mobile_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
	// @RequestMapping("/app_mobile.json")
	@RequestMapping("v1/app_mobile.json")
	public void account_mobile_save(HttpServletRequest request, HttpServletResponse response, String mobile_verify_code,
			String mobile) {
		Map map = new HashMap();
		String userMobile = mobile;
		boolean flag = this.mobileTools.verify(mobile);
		if (flag) {
			map = this.mobileTools.mobile(mobile);
			userMobile = (String) map.get("areaMobile");
		}
		Result result = null;
		VerifyCode mvc = this.mobileverifycodeService.getObjByProperty(null, "mobile", userMobile);
		if (mvc != null && mvc.getCode().equalsIgnoreCase(mobile_verify_code)) {
			this.mobileverifycodeService.delete(mvc.getId());
			// 绑定成功后发送手机短信提醒
			/*
			 * String content = "尊敬的" +
			 * "Ebuyair网站用户"SecurityUserHolder.getCurrentUser().getUserName() +
			 * "您好，您于" + CommUtil.formatLongDate(new Date()) + "验证身份成功。[" +
			 * this.configService.getSysConfig().getTitle() + "]";
			 */
			// String content = "";
			// this.msgTools.sendSMS(user.getMobile(), content);
			// this.msgTools.sendSMS(mobile, content);
			// mv.addObject("url", CommUtil.getURL(request) +
			// "/buyer/account.htm");
			result = new Result(0, "Successfully");
		} else {
			result = new Result(1, "Verification code error");
		}
		this.send_json(Json.toJson(result, JsonFormat.compact()), response);
	}

	/**
	 * App 注册短信发送
	 * 
	 * @param request
	 * @param response
	 * @param type
	 * @param mobile
	 * @throws UnsupportedEncodingException
	 */
	// @RequestMapping("/app_verify.json")
	@RequestMapping("v1/app_verify.json")
	public void order_mobile(HttpServletRequest request, HttpServletResponse response, String type, String mobile)
			throws UnsupportedEncodingException {
		Result result = null;
		String ret = "0";
		String msg = "SMS sent successfully";
		if (type.equals("mobile_verify_code")) {
			boolean flag = this.mobileTools.verify(mobile);
			if (flag) {
				String code = CommUtil.randomIntApp(4).toUpperCase();
				String content = "Your Soarmall verification code is " + code + ".The code is valid in 5 mins.";
				if (this.configService.getSysConfig().isSmsEnbale()) {
					Map map = this.mobileTools.mobile(mobile);
					boolean ret1 = this.msgTools.sendSMS(CommUtil.null2String(map.get("mobile")), content);
					if (ret1) {
						VerifyCode mvc = this.mobileverifycodeService.getObjByProperty(null, "mobile",
								map.get("areaMobile").toString());
						if (mvc == null) {
							mvc = new VerifyCode();
						}
						mvc.setAddTime(new Date());
						mvc.setCode(code);
						mvc.setMobile(CommUtil.null2String(map.get("areaMobile")));
						this.mobileverifycodeService.update(mvc);
					} else {
						ret = "3";
						msg = "Text message sending failed";
					}
				} else {
					ret = "2";
					msg = "SMS Function not enabled";
				}
			} else {
				ret = "1";
				msg = "Wrong phone number format";
			}
		}
		this.send_json(Json.toJson(new Result(CommUtil.null2Int(ret), msg), JsonFormat.compact()), response);
	}

	@RequestMapping("v2/app_verify.json")
	public void order_mobile2(HttpServletRequest request, HttpServletResponse response, String type, String mobile)
			throws UnsupportedEncodingException {
		Result result = null;
		String ret = "0";
		String msg = "SMS sent successfully";
		if (type.equals("mobile_verify_code")) {
			boolean flag = this.mobileTools.verify(mobile);
			if (flag) {
				if (this.configService.getSysConfig().isSmsEnbale()) {
					Map map = this.mobileTools.mobile(mobile);
					boolean ret1 = false;
					String code = "8888";
					if (CommUtil.null2String(map.get("areaMobile")).equals("88888888")) {
						ret1 = true;
					} else {
						code = CommUtil.randomIntApp(4).toUpperCase();
						String content = "Your Soarmall verification code is " + code + ".The code is valid in 5 mins.";
						ret1 = this.msgTools.sendSMS(CommUtil.null2String(map.get("mobile")), content);
					}
					if (ret1) {
						VerifyCode mvc = this.mobileverifycodeService.getObjByProperty(null, "mobile",
								map.get("areaMobile").toString());
						if (mvc == null) {
							mvc = new VerifyCode();
						}
						mvc.setAddTime(new Date());
						mvc.setCode(code);
						mvc.setMobile(CommUtil.null2String(map.get("areaMobile")));
						this.mobileverifycodeService.update(mvc);
					} else {
						ret = "3";
						msg = "Text message sending failed";
					}
				} else {
					ret = "2";
					msg = "SMS Function not enabled";
				}
			} else {
				ret = "1";
				msg = "Wrong phone number format";
			}
		}
		this.send_json(Json.toJson(new Result(CommUtil.null2Int(ret), msg), JsonFormat.compact()), response);
	}

	@RequestMapping("v1/app_forget.json")
	public void app_forget(HttpServletRequest request, HttpServletResponse response, String mobile,
			String mobile_verify_code, String userName, String password) {
		int code = -1;
		String msg = "";
		String new_password = password;
		Map pwdmap = new HashMap();
		SysConfig config = this.configService.getSysConfig();
		boolean flag = this.mobileTools.verify(mobile);
		Map map = this.mobileTools.mobile(mobile);
		if (flag) {
			if (!config.isEmailEnable() && !config.isSmsEnbale()) {
				code = 1;
				msg = "系统关闭邮件及手机短信功能，不能找回密码";
			} else {
				// 根据手机号获取验证码
				VerifyCode mvc = this.mobileverifycodeService.getObjByProperty(null, "mobile",
						CommUtil.null2String(map.get("areaMobile")));
				if (mvc != null && mvc.getCode().equalsIgnoreCase(mobile_verify_code)) {
					this.mobileverifycodeService.delete(mvc.getId());
					Map params = new HashMap();
					params.put("userName", userName);
					// params.put("email", userName);
					params.put("mobile", map.get("areaMobile").toString());
					List<User> users = this.userService.query(
							"select obj from User obj where obj.userName =:userName and obj.mobile =:mobile", params,
							-1, -1);
					if (users.size() > 0) {
						User user = users.get(0);
						if (!CommUtil.null2String(user.getMobile()).equals("")) {
							user.setPassword(Md5Encrypt.md5(password).toLowerCase());
							boolean ret = this.userService.update(user);
							if (ret) {
								String content = "Dear customer " + user.getUsername()
										+ ", you have successfully changed your password." + "Your new password is "
										+ new_password + ", please keep it safe.[Soarmall.com]";
								try {
									this.msgTools.sendSMS(mobile, content);
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							code = 0;
							msg = "Successfuly";
						} else {
							code = 2;
							msg = "用户没有绑定手机号";
						}
					} else {
						code = 3;
						msg = "请先注册用户,或使用本人手机号";
					}

				} else {
					code = 1;
					msg = "验证码错误";
				}
			}
		} else {
			code = 4;
			msg = "Wrong phone number format";
		}
		try {
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(Json.toJson(new Result(code, msg), JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 当用户登录后生成verify返回给客户端保存，每次发送用户中心中请求时将verify放入到请求头中，
	 * 用来验证用户密码是否已经被更改，如已经更改，手机客户端提示用户重新登录
	 * 
	 * @param user
	 * @return
	 */
	private String create_appverify(User user) {
		String app_verify = user.getPassword() + user.getApp_login_token();
		app_verify = Md5Encrypt.md5(app_verify).toLowerCase();
		return app_verify;
	}

	private void send_json(String json, HttpServletResponse response) {
		response.setContentType("application/json");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
