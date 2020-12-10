package com.metoo.module.weixin.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.weixin.pojo.SNSUserInfo;
import com.metoo.core.weixin.utils.CommonUtil;
import com.metoo.foundation.domain.Album;
import com.metoo.foundation.domain.Document;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.Role;
import com.metoo.foundation.domain.SysConfig;
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

/**
 * 
 * <p>
 * Title: WapLoginViewAction.java
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
 * @author hezeng
 * 
 * @date 2014-7-22
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class WeixinLoginViewAction {
	private final static Logger logger = Logger.getLogger(WeixinLoginViewAction.class);
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
	private IVerifyCodeService verifyCodeService;
	private static final String REGEX1 = "(.*管理员.*)";
	private static final String REGEX2 = "(.*admin.*)";


	/**
	 * 手机客户端用户登录
	 * 
	 * @param request
	 * @param response
	 * @param store_id
	 * @return
	 */
	@RequestMapping("/wap/koala_user_login.htm")
	public void mobile_login(HttpServletRequest request,
			HttpServletResponse response, String userName, String password) {
		String code = "-300";// 100,登陆成功,-100账号不存在，-200,密码不正确，-300登录失败
		Map<String,Object> json_map = new HashMap<String,Object>();
		boolean verify = CommUtil.null2Boolean(request.getHeader("verify"));
		if (verify) {// 头文件验证成功
			String user_id = "";
			String user_name = "";
			String login_token = "";
			if (userName != null && !userName.equals("") && password != null
					&& !password.equals("")) {
				password = Md5Encrypt.md5(password).toLowerCase();
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("userName", userName);
				List<User> users = this.userService
						.query("select obj from User obj where obj.userName=:userName order by addTime asc",
								map, -1, -1);
				if (users.size() > 0) {
					for (User u : users) {
						if (!u.getPassword().equals(password)) {
							code = "-200";
						} else {
							if (u.getUserRole().equals("admin")
									|| u.getUserRole().equals("ADMIN")) {
								code = "-100";
							} else {
								user_id = CommUtil.null2String(u.getId());
								user_name = u.getUserName();
								code = "100";
								login_token = CommUtil.randomString(12)
										+ user_id;
								u.setApp_login_token(login_token.toLowerCase());
								this.userService.update(u);
							}
						}
					}
				} else {
					code = "-100";
				}
			}
			if (code.equals("100")) {

				json_map.put("user_id", user_id.toString());
				json_map.put("userName", user_name);
				json_map.put("token", login_token);
			}
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
	 * 移动端用户登陆页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/wap/login.htm")
	public ModelAndView login(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("wap/login.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}
	
	/**
	 * 微信用户登陆页面
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/wap/weixinLogin.htm")
	public void weixinLogin(HttpServletRequest request,
			HttpServletResponse response) {
		//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx230ecfa4c5bdb96d&redirect_uri=http%3A%2F%2Fwx.fensekaola.com%2foauthServlet&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect
		CommonUtil.weixinLogin(request, response);
	}
	
	/**
	 * 移动端用户注册页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/wap/register.htm")
	public ModelAndView register(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("wap/register.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		return mv;
	}
	/**
	 * 移动端注册完成
	 * 
	 * @param request
	 * @param userName
	 * @param password
	 * @param email
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 */
	@RequestMapping("/wap/register_finish.htm")
	public String register_finish(HttpServletRequest request,
			HttpServletResponse response, String userName, String password,
			String email, String code, String user_type) throws HttpException,
			IOException, InterruptedException {
		try {
			boolean reg = true;// 防止机器注册，如后台开启验证码则强行验证验证码
			if (code != null && !code.equals("")) {
				code = CommUtil.filterHTML(code);// 过滤验证码
			}
			// System.out.println(this.configService.getSysConfig().isSecurityCodeRegister());
			if (this.configService.getSysConfig().isSecurityCodeRegister()) {
				if (!request.getSession(false).getAttribute("verify_code")
						.equals(code)) {
					reg = false;
				}
			}
			// 禁止用户注册带有 管理员 admin 等字样用户名
			if (userName.matches(REGEX1)
					|| userName.toLowerCase().matches(REGEX2)) {
				reg = false;
			}
			if (reg) {
				User user = new User();
				user.setUserName(userName);
				user.setUserRole("BUYER");
				user.setAddTime(new Date());
				user.setEmail(email);
				user.setAvailableBalance(BigDecimal.valueOf(0));
				user.setFreezeBlance(BigDecimal.valueOf(0));
				if (user_type != null && !user_type.equals("")) {
					user.setUser_type(CommUtil.null2Int(user_type));
					user.setTelephone(request.getParameter("telephone"));
					user.setMobile(request.getParameter("mobile"));
					
				}
				user.setPassword(Md5Encrypt.md5(password).toLowerCase());
				Map<String,Object> params = new HashMap<String,Object>();
				params.put("type", "BUYER");
				List<Role> roles = this.roleService
						.query("select new Role(id) from Role obj where obj.type=:type",
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
				return "redirect:koala_login.json?username="
						+ CommUtil.encode(userName) + "&password=" + password
						+ "&encode=true";
			} else {
				return "redirect:register.htm";
			}
		} catch (Exception e) {
			return "redirect:index.htm";
		}

	}

	/**
	 * 手机客户端查看注册协议
	 * 
	 */
	@RequestMapping("/wap/register_doc.htm")
	public ModelAndView mobile_register_doc(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("wap/register_doc.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		Document doc = this.documentService.getObjByProperty(null,"mark",
				"reg_agree");
		mv.addObject("doc", doc);
		return mv;
	}
	
	/**
	 * 找回密码第一步
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/wap/forget1.htm")
	public ModelAndView forget1(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("wap/forget1.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		SysConfig config = this.configService.getSysConfig();
		if (!config.isEmailEnable() && !config.isSmsEnbale()) {
			mv = new JModelAndView("wap/error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "系统关闭邮件及手机短信功能，不能找回密码");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
		}
		return mv;
	}

	/**
	 * 找回密码第二步
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/wap/forget2.htm")
	public ModelAndView forget2(HttpServletRequest request,
			HttpServletResponse response, String userName) {
		ModelAndView mv = new JModelAndView("wap/forget2.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		SysConfig config = this.configService.getSysConfig();
		if (!config.isEmailEnable() && !config.isSmsEnbale()) {
			mv = new JModelAndView("wap/error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "系统关闭邮件及手机短信功能，不能找回密码");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
		} else {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("userName", userName);
			params.put("email", userName);
			params.put("mobile", userName);
			List<User> users = this.userService
					.query("select obj from User obj where obj.userName =:userName or obj.email=:email or obj.mobile =:mobile",
							params, -1, -1);
			if (users.size() > 0) {
				User user = users.get(0);
				if (!CommUtil.null2String(user.getEmail()).equals("")
						|| !CommUtil.null2String(user.getMobile()).equals("")) {
					mv.addObject("user", user);
				} else {
					mv = new JModelAndView("wap/error.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					mv.addObject("op_title", "用户没有绑定邮箱和手机，无法找回");
					mv.addObject("url", CommUtil.getURL(request)
							+ "/wap/forget1.htm");
				}

			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "不存在该用户");
				mv.addObject("url", CommUtil.getURL(request) + "/wap/forget1.htm");
			}
		}
		return mv;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 * @return
	 */
	@RequestMapping("/wap/forget3.htm")
	public ModelAndView forget3(HttpServletRequest request,
			HttpServletResponse response, String accept_type, String email,
			String mobile, String userName, String verify_code) {
		ModelAndView mv = new JModelAndView("wap/forget3.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if (accept_type.equals("email")) {
			VerifyCode vc = this.verifyCodeService.getObjByProperty(null,"email",
					email);
			if (vc != null) {
				if (!vc.getCode().equals(verify_code)) {
					mv = new JModelAndView("wap/error.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					mv.addObject("op_title", "验证码输入错误");
					mv.addObject("url", CommUtil.getURL(request)
							+ "/wap/forget2.htm?userName=" + userName);
				} else {
					String verify_session = CommUtil.randomString(64)
							.toLowerCase();
					mv.addObject("verify_session", verify_session);
					request.getSession(false).setAttribute("verify_session",
							verify_session);
					mv.addObject("userName", userName);
					this.verifyCodeService.delete(vc.getId());
				}
			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "验证码输入错误");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/wap/forget2.htm?userName=" + userName);
			}
		}
		if (accept_type.equals("mobile")) {
			VerifyCode vc = this.verifyCodeService.getObjByProperty(null,"mobile",
					mobile);
			if (vc != null) {
				if (!vc.getCode().equals(verify_code)) {
					mv = new JModelAndView("wap/error.html",
							configService.getSysConfig(),
							this.userConfigService.getUserConfig(), 1, request,
							response);
					mv.addObject("op_title", "验证码输入错误");
					mv.addObject("url", CommUtil.getURL(request)
							+ "/wap/forget2.htm?userName=" + userName);
				} else {
					String verify_session = CommUtil.randomString(64)
							.toLowerCase();
					mv.addObject("verify_session", verify_session);
					request.getSession(false).setAttribute("verify_session",
							verify_session);
					mv.addObject("userName", userName);
					this.verifyCodeService.delete(vc.getId());
				}
			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "验证码输入错误");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/wap/forget2.htm?userName=" + userName);
			}
		}
		return mv;
	}

	@RequestMapping("/wap/forget4.htm")
	public ModelAndView forget4(HttpServletRequest request,
			HttpServletResponse response, String userName, String password,
			String verify_session) {
		ModelAndView mv = new JModelAndView("wap/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String verify_session1 = CommUtil.null2String(request.getSession(false)
				.getAttribute("verify_session"));
		if (!verify_session1.equals("")
				&& verify_session1.equals(verify_session)) {
			User user = this.userService.getObjByProperty(null,"userName", userName);
			user.setPassword(Md5Encrypt.md5(password).toLowerCase());
			this.userService.update(user);
			request.getSession(false).removeAttribute("verify_session");
			mv.addObject("op_title", "密码修改成功，请使用新密码登录");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/login.htm");
		} else {
			mv = new JModelAndView("wap/error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "会话过期，找回密码失败");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/forget1.htm");
		}
		return mv;
	}

	/**
	 * 微信端登陆
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/wap/weixin_login.htm")
	public String weixin_login(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession();
		SNSUserInfo info = (SNSUserInfo) session.getAttribute("snsUserInfo");

		if(info == null){
			logger.info("未获取到用户信息。");
			
			//下面代码by zhuzhi 2017-10-05
			//重新调用网页获取用户信息
			StringBuffer sb = new StringBuffer();
			sb.append("https://open.weixin.qq.com/connect/oauth2/authorize?");
			sb.append("appid=wx230ecfa4c5bdb96d");
			sb.append("&redirect_uri=http%3A%2F%2Fwx.fensekaola.com%2foauthServlet");
			sb.append("&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect");

			System.out.println("zzzzzzzzzzzzzzzzzzzzzz");
			System.out.println(sb.toString());
			
			return "redirect:"+sb.toString();

			
			//下面代码注释by zhuzhi 2017-10-05
			/*
			return "redirect:/wap/index.htm";
		}else if("0".equals(info.getSubscribe())){
			logger.info("用户未关注公众号，无法获取用户信息。");
			//return "redirect:/wap/index.htm";
			String followUrl = "https://mp.weixin.qq.com/s?__biz=MzIyNzc3NTI4Nw==&tempkey=xhTdh754mohQOtxgsFe7plK6USUVDaSinZPwAR%2BuRY93PcKhm3TViI6mVn6sxUMgrC45skmDc2dUYuTZHV9Yb8g3tF5c1Y2K01IbWtTBj00qRUAQyh9EyMItB30VoUBFteijI%2FemnbBfezWIYSND8A%3D%3D&chksm=685d475a5f2ace4c9a172c42bcf2ecbf63adb93e04d25bac7842c4e15832bae81bcf2e120f0c#rd";
			return "redirect:"+followUrl;
			*/
			
			
		}else{
			
			System.out.println("iiiiiiiiiiiiiiiiiiiii");
			
			logger.info("获取到用户信息。");
			String password = "123456";
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", info.getOpenId());
			List<User> userList = this.userService.query("select obj from User obj where obj.openId = :id ", params, -1, -1);
			User obj = null;
			if(userList.size()>0){
				obj = userList.get(0);
			}

			if(obj == null){
				obj = new User();
				obj.setAddTime(new Date());
				obj.setUserName(info.getNickname());//用户名为微信昵称
				obj.setNickName(info.getRemark());//昵称为公众号设置的备注信息
				obj.setSex(info.getSex());
				obj.setTrueName(info.getNickname());
				obj.setOpenId(info.getOpenId());
				obj.setCity(info.getCity());
				obj.setProvince(info.getProvince());
				obj.setCountry(info.getCountry());
				obj.setHeadImgUrl(info.getHeadImgUrl());
				obj.setUserRole("BUYER");
				obj.setPassword(Md5Encrypt.md5(password).toLowerCase());
				obj.setSubscribe(info.getSubscribe());//是否关注
				obj.setSubscribe_time(info.getSubscribe_time());//关注时间
				if(!"0".equals(info.getSubscribe())){// 是否关注值不为0 则关注了公众号，赠送100积分
					//obj.setIntegral(200);
					obj.setIntegral(this.configService.getSysConfig()
							.getMemberRegister());

				}
				params.clear();
				params.put("type", "BUYER");
				List<Role> roles = this.roleService
						.query("select new Role(id) from Role obj where obj.type=:type",
								params, -1, -1);
				obj.getRoles().addAll(roles);
				this.userService.save(obj);

				//用户首次进入商城 赠送100积分
				IntegralLog log = new IntegralLog();
				log.setAddTime(new Date());
				log.setContent("用户注册增加"
						+ this.configService.getSysConfig()
								.getMemberRegister() + "分");
				log.setIntegral(this.configService.getSysConfig()
						.getMemberRegister());
				log.setIntegral_user(obj);
				log.setType("reg");
				this.integralLogService.save(log);
				
			}else{
				//首次关注公众号送100积分
				if(!"1".equals(obj.getSubscribe()) && "1".equals(info.getSubscribe()))
				{
					obj.setSubscribe(info.getSubscribe());
					//obj.setIntegral(obj.getIntegral()+200);//不是首次来平台，但是首次关注送100积分
					obj.setIntegral(obj.getIntegral()+this.configService.getSysConfig()
							.getMemberRegister());
					
					//用户首次进入商城 赠送100积分
					IntegralLog log = new IntegralLog();
					log.setAddTime(new Date());
					log.setContent("用户注册增加"
							+ this.configService.getSysConfig()
									.getMemberRegister() + "分");
					log.setIntegral(this.configService.getSysConfig()
							.getMemberRegister());
					log.setIntegral_user(obj);
					log.setType("reg");
					this.integralLogService.save(log);
				}
				//更新微信信息
				obj.setUserName(info.getNickname());//用户名为微信昵称
				obj.setNickName(info.getRemark());//昵称为公众号设置的备注信息
				obj.setSex(info.getSex());
				obj.setTrueName(info.getNickname());//微信昵称
				obj.setHeadImgUrl(info.getHeadImgUrl());//微信头像
				obj.setSubscribe_time(info.getSubscribe_time());//关注时间
				this.userService.save(obj);			
				
			}

			session.setAttribute("type", "weixin");
			return "redirect:http://wx.fensekaola.com/koala_login.json?username="
			+ CommUtil.encode(obj.getUserName()) + "&password=" + password
			+ "&encode=true&type=weixin";
		}
	}

}
