package com.metoo.msg.email;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.metoo.app.view.web.action.MGoodsViewAction;
import com.metoo.core.annotation.EmailMapping;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.EmailModel;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IEmailModelService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserService;
import com.metoo.msg.MsgTools;

/**
 * <p>
 * Title: EmailAspect.class
 * </p>
 * 
 * <p>
 * Description: 指定api发送邮件
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2020
 * </p>
 * 
 * <p>
 * Company: 觅通科技
 * </p>
 * 
 * @autho hkk
 * 
 */

@Aspect
@Component // 将当前类生成代理对象
public class EmailAspect {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IEmailModelService emailService;
	@Autowired
	private IUserService userService;

	private final static Log logger = LogFactory.getLog(EmailAspect.class);

	/*
	 * public void send_email1(JoinPoint joinPoint, EmailMapping annotation) {
	 * MethodSignature methodSignature = (MethodSignature)
	 * joinPoint.getSignature(); String[] parameterNames =
	 * methodSignature.getParameterNames(); User user = null; String registEmail
	 * = ""; for (int i = 0; i < parameterNames.length; i++) { registEmail =
	 * parameterNames[i].equals("email") ? (String) joinPoint.getArgs()[i] : "";
	 * if (parameterNames[i].equals("token")) { String token = (String)
	 * joinPoint.getArgs()[i]; if (!CommUtil.null2String(token).equals("")) {
	 * user = this.userService.getObjByProperty(null, "app_login_token", token);
	 * } } } if (null != user || annotation.value().equals("wap_register")) {
	 * Map params = new HashMap(); params.put("type", "Buyer");
	 * params.put("display", true); params.put("value", annotation.value());
	 * List<EmailModel> models = this.emailService.query(
	 * "select obj from EmailModel obj where obj.type=:type and obj.display=:display and obj.value=:value"
	 * , params, -1, -1); String userEmail = "";
	 * 
	 * if(registEmail.equals("")){ userEmail = user.getEmail() == null ? "" :
	 * user.getEmail(); }else{ user = this.userService.getObjByProperty(null,
	 * "value", registEmail); }
	 * 
	 * if (!registEmail.equals("")) { user =
	 * this.userService.getObjByProperty(null, "email", registEmail); userEmail
	 * = user.getEmail(); } final String emails = userEmail; final User parent =
	 * user; Pattern emailPattern =
	 * Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	 * Matcher matcher = emailPattern.matcher(CommUtil.null2String(userEmail));
	 * if (matcher.matches()) { for (EmailModel model : models) { if
	 * (model.getValue().equals(annotation.value())) { Template template =
	 * this.templateService.getObjByProperty(null, "mark", model.getValue());
	 * final String customName = template.getUser_name(); final EmailMapping
	 * annotation1 = annotation; final String value = model.getValue(); Thread t
	 * = new Thread(new Runnable() { public void run() { // 1223414075@qq.com
	 * gao // 11943732@qq.com wang // 1393813658@qq.com liu // String emails =
	 * "460751446@qq.com"; // 11943732@qq.com,460751446@qq.com,1393813658@qq.com
	 * try { msgTools.sendEmail("", value, "460751446@qq.com", null, null,
	 * parent); } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } } }); t.start(); } } } } }
	 */

	//@AfterReturning(pointcut = "execution(* com.metoo.app.view.web.action.MCartViewActionV2.*(..)) && @annotation(annotation)&&args(request,..)", returning = "result")
	public void emailCart(JoinPoint joinPoint, EmailMapping annotation, HttpServletRequest request, String result) {
		String order_id = "";
		String cart_order_ids = "";
		int code = -1;
		if (!CommUtil.null2String(result).equals("")) {
			JSONObject json = JSONObject.parseObject(result);
			JSONObject data = (JSONObject) json.get("data");
			code = json.getInteger("code");
			if (code == 4200 || code == 0 || null != data) {
				JSONArray array = data.getJSONArray("cart_order_ids");
				List<Long> order_ids = new ArrayList<Long>();
				if (null != array) {
					for (int i = 0; i < array.size(); i++) {
						if (!order_ids.contains(CommUtil.null2Long(array.get(i)))) {
							order_ids.add(CommUtil.null2Long(array.get(i)));
						}
					}
				}
				if (order_ids.size() > 0) {
					MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
					String[] parameterNames = methodSignature.getParameterNames();
					String token = request.getParameter("token");
					User user = this.userService.getObjByProperty(null, "app_login_token", token);
					if (null != user) {
						Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
						Matcher matcher = emailPattern.matcher(CommUtil.null2String(user.getEmail()));
						if (matcher.matches()) { // 给用户发送指定邮件
							Map params = new HashMap();
							params.put("type", "Buyer");
							params.put("display", true);
							params.put("value", annotation.value());
							List<EmailModel> models = this.emailService.query(
									"select obj from EmailModel obj where obj.type=:type and obj.display=:display and obj.value=:value",
									params, -1, -1);
							String url = CommUtil.getURL(request);
							String imageWebServer = this.configService.getSysConfig().getImageWebServer();
							this.send_email(annotation, models, user, order_ids, imageWebServer);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param joinPoint
	 * @param annotation
	 * @param request
	 * @param result
	 * @descript 用户未填写邮箱时，只发送给管理员；取消验证用户邮箱
	 */
	@AfterReturning(pointcut = "execution(* com.metoo.app.view.web.action.MCartViewActionV2.*(..)) && @annotation(annotation)&&args(request,..)", returning = "result")
	public void emailCartV2(JoinPoint joinPoint, EmailMapping annotation, HttpServletRequest request, String result) {
		String server_name = request.getServerName();
		if(!request.getServerName().equals("local.soarmall.com")){//避免测试订单
			String order_id = "";
			String cart_order_ids = "";
			int code = -1;
			if (!CommUtil.null2String(result).equals("")) {
				JSONObject json = JSONObject.parseObject(result);
				JSONObject data = (JSONObject) json.get("data");
				code = json.getInteger("code");
				if (code == 4200 || code == 0 || null != data) {
					JSONArray array = data.getJSONArray("cart_order_ids");
					List<Long> order_ids = new ArrayList<Long>();
					if (null != array) {
						for (int i = 0; i < array.size(); i++) {
							if (!order_ids.contains(CommUtil.null2Long(array.get(i)))) {
								order_ids.add(CommUtil.null2Long(array.get(i)));
							}
						}
					}
					if (order_ids.size() > 0) {
						MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
						String[] parameterNames = methodSignature.getParameterNames();
						String token = request.getParameter("token");
						User user = this.userService.getObjByProperty(null, "app_login_token", token);
						if (null != user) {
							Map params = new HashMap();
							params.put("type", "Buyer");
							params.put("display", true);
							params.put("value", annotation.value());
							List<EmailModel> models = this.emailService.query(
									"select obj from EmailModel obj where obj.type=:type and obj.display=:display and obj.value=:value",
									params, -1, -1);
							String url = CommUtil.getURL(request);
							String imageWebServer = this.configService.getSysConfig().getImageWebServer();
							this.send_email(annotation, models, user, order_ids, imageWebServer);
						}
					}
				}
			}
		}
	}

	/**
	 * @description 注册完成发送邮箱
	 * @param joinPoint
	 * @param annotation
	 * @param request
	 * @param result
	 *            com.metoo.app.view.web.action
	 */
	@After("execution(* com.metoo.app.view.web.action.MAppRegisterViewAction.*(..)) && @annotation(annotation) && args(request,..)")
	public void emailRegist(JoinPoint joinPoint, EmailMapping annotation, HttpServletRequest request) {
		Map<String, Object> params = new HashMap<String, Object>();
		String registEmail = request.getParameter("email");
		String token = request.getParameter("token");
		String password = request.getParameter("password");
		String userEmail = "";
		User user = null;
		if (CommUtil.null2String(password).equals("")) {
			if (!CommUtil.null2String(token).equals("")) {
				user = this.userService.getObjByProperty(null, "app_login_token", CommUtil.null2String(token));
			}
			if (null == user || annotation.value().equals("wap_register")) {
				if (!CommUtil.null2String(registEmail).equals("")) {
					params.clear();
					params.put("email", registEmail);
					List<User> users = this.userService.query("select obj from User obj where obj.email=:email", params,
							-1, -1);
					if (users.size() > 1) {
					} else {
						user = this.userService.getObjByProperty(null, "email", registEmail);
					}
				}
				if (null != user && !CommUtil.null2String(user.getEmail()).equals("")) {
					Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
					Matcher matcher = emailPattern.matcher(user.getEmail());
					if (matcher.matches()) { // 给用户发送指定邮件
						params.clear();
						params.put("type", "Buyer");
						params.put("display", true);
						params.put("value", annotation.value());
						List<EmailModel> models = this.emailService.query(
								"select obj from EmailModel obj where obj.type=:type and obj.display=:display and obj.value=:value",
								params, -1, -1);
						String url = CommUtil.getURL(request);
						String imageWebServer = this.configService.getSysConfig().getImageWebServer();
						this.send_email(annotation, models, user, null, imageWebServer);
					}
				}
			}
		}
	}

	public void send_email(EmailMapping annotation, List<EmailModel> models, User user, List<Long> order_ids,
			String url) {
		for (EmailModel model : models) {
			if (model.getValue().equals(annotation.value())) {
				final String mark = model.getValue();
				final User parent = user;
				final List<Long> ids = order_ids;
				final String imageWebServer = url;
				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							msgTools.sendJMail(imageWebServer, mark, ids, parent);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
			}
		}
	}

	/**
	 * 拦截异常
	 *
	 * @param e
	 *            异常
	 * @param spriteLog
	 *            拦截日志
	 */
	/*
	 * @AfterThrowing(value = "@within(spriteLog)", throwing = "e") public void
	 * AfterThrowingAnnotationMatch(Exception e, Log spriteLog) {
	 * System.out.println(e.getMessage()); }
	 */

}
