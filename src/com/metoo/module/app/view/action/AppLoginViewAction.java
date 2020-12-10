package com.metoo.module.app.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpException;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.Document;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.VerifyCode;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IDocumentService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IRoleService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.IVerifyCodeService;
import com.metoo.module.app.domain.QRLogin;
import com.metoo.module.app.service.IQRLoginService;
import com.metoo.msg.MsgTools;

/**
 * 
 * <p>
 * Title: MobileLoginViewAction.java
 * </p>
 * 
 * <p>
 * Description: 手机端登录请求管理类
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author hezeng、erikzhang
 * 
 * @date 2014-7-22
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class AppLoginViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IDocumentService documentService;
	@Autowired
	private IQRLoginService qrLoginService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IVerifyCodeService mobileverifycodeService;

	/**
	 * 手机客户端用户登录
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 * @return
	 */
	@RequestMapping("/app/koala_user_login.htm")
	public void app_login(HttpServletRequest request,
			HttpServletResponse response, String userName, String password) {
		String code = "-300";// 100,登陆成功,-100账号不存在，-200,密码不正确，-300登录失败
		Map json_map = new HashMap();
		String user_id = "";
		String user_name = "";
		String login_token = "";
		User login_user = null;
		if (userName != null && !userName.equals("") && password != null
				&& !password.equals("")) {
			password = Md5Encrypt.md5(password).toLowerCase();
			Map map = new HashMap();
			map.put("userName", userName);
			List<User> users = this.userService
					.query("select obj from User obj where obj.userName=:userName order by addTime asc",
							map, -1, -1);
			if (users.size() > 0) {
				for (User u : users) {
					if (!u.getPassword().equals(password)) {
						code = "-200";
					} else {
						if (u.getUserRole().equalsIgnoreCase("admin")) {
							code = "-100";
						} else {
							user_id = CommUtil.null2String(u.getId());
							user_name = u.getUserName();
							code = "100";
							login_token = CommUtil.randomString(12) + user_id;
							u.setApp_login_token(login_token.toLowerCase());
							this.userService.update(u);
							login_user = u;
							break;
						}
					}
				}
			} else {
				code = "-100";
			}
		}
		if (code.equals("100")) {
			json_map.put("verify", this.create_appverify(login_user));
			json_map.put("user_id", user_id.toString());
			json_map.put("userName", user_name);
			json_map.put("token", login_token);
		}
		json_map.put("code", code);
		String json = Json.toJson(json_map, JsonFormat.compact());
		response.setContentType("text/plain");
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

	
	
	/**
	 * 手机客户端注册完成
	 * 
	 * @param request
	 * @param userName
	 * @param password
	 * @param email
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	@RequestMapping("/app/register_finish.htm")
	public void app_register(HttpServletRequest request,
			HttpServletResponse response, String userName, String password,
			String type, String mobile, String verify_code)
			throws HttpException, IOException {
		boolean verify = true;
		boolean reg = true;// 防止机器注册，如后台开启验证码则强行验证验证码
		int code = 100;// 100注册成功，-100，手机注册时验证码错误，-200用户名已存在
		Map params = new HashMap();
		Map json_map = new HashMap();
		User user = new User();
		String login_token = "";
		if (CommUtil.null2String(type).equals("mobile")) {
			userName = mobile;
			password = "123456";
			code = this.app_verify_mobile_code(verify_code, mobile);
			if (code == -100) {
				reg = false;
			}
		}
		if (verify) {// 头文件验证成功
			// 进一步控制用户名不能重复，防止在未开启注册码的情况下注册机恶意注册
			params.put("userName", userName);
			params.put("mobile", userName);
			List<User> users = this.userService
					.query("select obj from User obj where obj.userName=:userName or obj.mobile=:mobile",
							params, -1, -1);
			if (users != null && users.size() > 0) {
				reg = false;
				code = -200;
			}
			if (reg) {
				user.setUserName(userName);
				user.setUserRole("BUYER");
				user.setAddTime(new Date());
				user.setAvailableBalance(BigDecimal.valueOf(0));
				user.setFreezeBlance(BigDecimal.valueOf(0));
				// 生成token
				login_token = CommUtil.randomString(12)
						+ CommUtil.null2String(user.getId());
				user.setApp_login_token(login_token.toLowerCase());
				user.setPassword(Md5Encrypt.md5(password).toLowerCase());
				if (type != null && type.equals("mobile")) {
					user.setMobile(mobile);
				}
				params.clear();
				params.put("type", "BUYER");
				List<Role> roles = this.roleService.query(
						"select obj from Role obj where obj.type=:type",
						params, -1, -1);
				user.getRoles().addAll(roles);
				if (this.configService.getSysConfig().isIntegral()) {
					user.setIntegral(this.configService.getSysConfig()
							.getMemberRegister());
					this.userService.save(user);
					IntegralLog log = new IntegralLog();
					log.setAddTime(new Date());
					log.setContent("用户注册增加"
							+ this.configService.getSysConfig()
									.getMemberRegister() + "分");
					log.setIntegral(this.configService.getSysConfig()
							.getMemberRegister());
					log.setIntegral_user(user);
					log.setType("reg");
					this.integralLogService.save(log);
				} else {
					this.userService.save(user);
				}
				// 创建用户默认相册
				Album album = new Album();
				album.setAddTime(new Date());
				album.setAlbum_default(true);
				album.setAlbum_name("默认相册");
				album.setAlbum_sequence(-10000);
				album.setUser(user);
				this.albumService.save(album);
				request.getSession(false).removeAttribute("verify_code");
				json_map.put("verify", this.create_appverify(user));
				json_map.put("user_id", user.getId().toString());
				json_map.put("userName", user.getUserName());
				json_map.put("token", login_token);
				//如果手机号码注册，注册成功后清除验证码信息
				if (type != null && type.equals("mobile")) {
					Map mvc_params = new HashMap();
					mvc_params.put("mobile", mobile);
					List<VerifyCode> mvcs = this.mobileverifycodeService.query(
							"select obj from VerifyCode obj where obj.mobile=:mobile ",
							mvc_params, -1, -1);
					for (VerifyCode mvc : mvcs) {
						this.mobileverifycodeService.delete(mvc.getId());
					}
				}
			}
		} else {
			reg = false;
		}
		json_map.put("code", code);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 手机客户端查看注册协议
	 * 
	 */
	@RequestMapping("/app/register_doc.htm")
	public ModelAndView app_register_doc(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("app/doc.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Document doc = this.documentService.getObjByProperty(null, "mark",
				"reg_agree");
		mv.addObject("doc", doc);
		return mv;
	}

	private int app_verify_mobile_code(String verify_code, String mobile) {
		int code = -100;// 100验证成功，-100，验证失败
		Map params = new HashMap();
		params.put("mobile", mobile);
		List<VerifyCode> mvcs = this.mobileverifycodeService.query(
				"select obj from VerifyCode obj where obj.mobile=:mobile ",
				params, -1, -1);
		if (mvcs.size() > 0) {
			VerifyCode mv = mvcs.get(0);
			if (mv.getCode().equals(verify_code)) {
				code = 100;				
			}
		}
		return code;
	}

	/**
	 * 手机发送注册验证码
	 * 
	 * @param request
	 * @param response
	 * @param type
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/app/send_register_code.htm")
	public void send_register_code(HttpServletRequest request,
			HttpServletResponse response, String mobile)
			throws UnsupportedEncodingException {
		Map json_map = new HashMap();
		String ret = "100";// 100发送成功，200，发送失败，300系统未开启短信功能
		String code = CommUtil.randomInt(6);
		String content = "您正在使用手机号注册，您的验证码为：" + code + "。["
				+ this.configService.getSysConfig().getTitle() + "]";
		if (mobile != null && !mobile.equals("")) {
			if (this.configService.getSysConfig().isSmsEnbale()) {
				boolean ret1 = this.msgTools.sendSMS(mobile, content);
				if (ret1) {
					// 删除所有该号码code
					Map params = new HashMap();
					params.put("mobile", mobile);
					List<VerifyCode> codes = this.mobileverifycodeService
							.query("select obj from VerifyCode obj where obj.mobile=:mobile ",
									params, -1, -1);
					for (VerifyCode cd : codes) {
						this.mobileverifycodeService.delete(cd.getId());
					}
					// 每次新建一个mvc
					VerifyCode mvc = new VerifyCode();
					mvc.setAddTime(new Date());
					mvc.setCode(code);
					json_map.put("code", code);// 用于测试
					mvc.setMobile(mobile);
					this.mobileverifycodeService.update(mvc);
				} else {
					ret = "200";
				}
			} else {
				ret = "300";
			}
		} else {
			code = "200";
		}
		json_map.put("ret", ret);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map, JsonFormat.compact()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 手机app扫描二维码登录，要求手机客户端必须为登录状态
	 */
	@RequestMapping("/app/buyer/app_qr_login.htm")
	public void app_qr_login(HttpServletRequest request,
			HttpServletResponse response, String user_id, String qr_id) {
		Map json_map = new HashMap();
		String ret = "100";
		QRLogin qrlogin = new QRLogin();
		qrlogin.setAddTime(new Date());
		qrlogin.setUser_id(user_id);
		qrlogin.setQr_session_id(qr_id);
		this.qrLoginService.save(qrlogin);
		json_map.put("ret", ret);
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.print(Json.toJson(json_map, JsonFormat.compact()));
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
}
