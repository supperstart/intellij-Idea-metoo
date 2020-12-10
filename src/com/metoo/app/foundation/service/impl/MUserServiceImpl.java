package com.metoo.app.foundation.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MIAreaService;
import com.metoo.app.foundation.service.MIUserService;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.foundation.service.impl.IntegralLogServiceImpl;
import com.metoo.msg.MsgTools;
@Service
@Transactional
public class MUserServiceImpl implements MIUserService{

	@Resource(name = "userMetooDAO")
	private IGenericDAO<User> userMetooDao;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private MIAreaService areaMetooService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IUserService userService;
	@Autowired
	private IIntegralLogService intergralLogService;
	
	@Override
	public User getObjById(Long id) {
		// TODO Auto-generated method stub
		
		User user = this.userMetooDao.get(id);
		if(user != null){
			return user;
		}
		return null;
	}

	@Override
	public boolean update(User user) {
		// TODO Auto-generated method stub
		try {
			this.userMetooDao.update(user);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	   //个人信息
		public String metoo_account(HttpServletRequest request,
				HttpServletResponse response, String token) {
			Result result = new Result();
			Map<String, Object> map = new HashMap<String, Object>();
			if(token.equals("")){
				result = new Result(-100,"token Invalidation");
			}else{
				User user = this.userService.getObjByProperty(null, "app_login_token", token);
				if(user == null){
					result = new Result(-100,"token Invalidation");
				} else{
					Map<String, Object> userMap = new HashMap<String, Object>();
					userMap.put("TrueName", user.getTrueName());
					userMap.put("Sex", user.getSex());
					userMap.put("Email", user.getEmail());
					userMap.put("Telephone", user.getTelephone());
					userMap.put("Mobile", user.getMobile());
					userMap.put("Birthday", user.getBirthday());
					List<Area> areas = this.areaMetooService.query(
							"select obj from Area obj where obj.parent.id is null", null, -1, -1);
					List<Map<String, Object>> areaList = new ArrayList<Map<String, Object>>();
					for (Area area : areas) {
						Map<String, Object> areaMap = new HashMap<String, Object>();
						areaMap.put("id", area.getId());
						areaMap.put("areaName", area.getAreaName());
						areaList.add(areaMap);
						map.put("areaMap", areaList);
					}
					map.put("userMap", userMap);
				    result = new Result(0, "Successfully" ,map);
			}
		}
			return Json.toJson(result, JsonFormat.compact());
	}
		
		//信息保存
		public String account_metoo_save(HttpServletRequest request,
				HttpServletResponse response, String area_id, String birthday, String token) {
			Result result = null;
			WebForm wf = new WebForm();
			if(token.equals("")){
				result = new Result(-100, "token Invalidation");
			}else{
				User user = this.userService.getObjByProperty(null, "app_login_token", token);
				if(user == null){
					result = new Result(-100, "token Invalidation");
				}else{
					   user = (User) wf.toPo(request, this.userService
							.getObjById(user.getId()));
					if (area_id != null && !area_id.equals("")) {
						Area area = this.areaMetooService
								.getObjById(CommUtil.null2Long(area_id));
					}
					if (birthday != null && !birthday.equals("")) {
						String y[] = birthday.split("-");
						Calendar calendar = new GregorianCalendar();
						int years = calendar.get(Calendar.YEAR) - CommUtil.null2Int(y[0]);
						user.setYears(years);
					}
					if(this.update(user)){
						result = new Result(0, "Successfully");
					}else{
						result = new Result(1, "Error");
					}
				}
			}
			return Json.toJson(result, JsonFormat.compact());
		}
		
		//密码保存
		public String account_metoo_password_save(HttpServletRequest request, HttpServletResponse response,
				String old_password, String new_password, String token){
			Result result = new Result();
			if(token.equals("")){
				result = new Result(-100,"token Invalidation");
			}else{
				User user = this.userService.getObjByProperty(null, "app_login_token", token);
				if(user == null){
					result = new Result(-100,"token Invalidation");	
				}else{
					//获取当前登录用户数据库密码，比较用户输入密码加密 (md5)if-true 对新的密码加密
					if(user.getPassword().equals(
							Md5Encrypt.md5(old_password).toLowerCase())){
						user.setPassword(Md5Encrypt.md5(new_password).toLowerCase());
						boolean pwd = this.update(user);
						if(pwd){
/*							String content1 = "尊敬的"
									+ user.getUserName()
									+ "您好，您于" + CommUtil.formatLongDate(new Date())
									+ "修改密码成功，新密码为：" + new_password + ",请妥善保管。["
									+ this.configService.getSysConfig().getTitle() + "]";
							
*/							String content = "Hi,dear customer "
									+ user.getUserName()
									+ ". you have successfully changed your password on : "
									+ CommUtil.formatLongDate(new Date())
									+ ". Your new password is: " 
									+ new_password
									+ ",Please keep it safe. [Soarmall.com]";
							try {
								this.msgTools.sendSMS(user.getMobile(), content);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							result = new Result(0, content, pwd);
						}
					}else{
						result = new Result(1, "PassWord Error");
					}
				}
			}
			return Json.toJson(result, JsonFormat.compact());
		}
		
		//修改邮箱
		public String  account_email_save(HttpServletRequest request, HttpServletResponse response,
				String password, String email, String token) {
			Result result = null;
			if(CommUtil.null2String(token).equals("")){
				result = new Result(-100,"token Invalidation");
			}else{
				User user = this.userService.getObjByProperty(null, "app_login_token", token);
				if(user == null){
					result = new Result(-100, "token Invalidation");	
				}else{
			      if(user.getPassword().equals(Md5Encrypt.md5(password).toLowerCase())){
			    	  user.setEmail(email);
			    	  if(this.configService.getSysConfig().isIntegral()){
			    		  user.setIntegral(this.configService.getSysConfig().getMemberEmail());
			    		  IntegralLog integralLog = new IntegralLog();
			    		  integralLog.setAddTime(new Date());
			    		  integralLog.setContent("用户"+new Date()+"完善邮箱增加" + this.configService.getSysConfig().getMemberEmail() + "分");
			    		  integralLog.setIntegral(this.configService.getSysConfig().getMemberEmail());
			    		  integralLog.setIntegral_user(user);
			    		  integralLog.setType("Add");
			    		  integralLog.setIntegral_from("登录");
			    		  this.intergralLogService.save(integralLog);
			    	  }
			    	 if(this.update(user)){
			    		 result = new Result(0, "Successfully");
			    	 }else{
			    		 result = new Result(1, "Error");
			    	 }
			      }else{
			    	  result = new Result(1, "PassWord Error");
			      }
				}
			}
			return Json.toJson(result, JsonFormat.compact());
		}
	}
