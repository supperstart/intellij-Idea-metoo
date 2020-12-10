package com.metoo.manage.admin.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.EmailModel;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.EmailModelQueryObject;
import com.metoo.foundation.service.IEmailModelService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserConfigService;
import com.sun.glass.ui.Pixels.Format;

import net.sf.json.JSONArray;

@Controller
public class EmailModelManageAction {

	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IEmailModelService emailModelService;
	@Autowired
	private ITemplateService templateService;

	@SecurityMapping(title = "emailModel设置", value = "/admin/set_emailModel.htm*", rtype = "admin", rname = "emailModel设置", rcode = "admin_set_emailModel", rgroup = "设置")
	@RequestMapping("/admin/set_email_model.htm")
	public ModelAndView set_emailModel(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/set_email_model.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		EmailModelQueryObject emq = new EmailModelQueryObject();
		emq.addQuery("type", new SysMap("type", "BUYER"), "=");
		IPageList pList = this.emailModelService.list(emq);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", "api");
		List<Template> templates = this.templateService.query("select obj from Template obj where obj.type=:type order By obj.addTime desc",
				params, -1, -1);
		mv.addObject("templates", templates);
		return mv;
	}

	//未记录该api权限
	@RequestMapping("/admin/email_model_save.htm")
	public ModelAndView emailModel_save(HttpServletRequest request, HttpServletResponse response, String model_ids,
			String add_url, String list_url) {
		model_ids = "{" + model_ids +"}";
		Map<Object, Object> map = JSON.parseObject(model_ids, Map.class);
		List<Long> emailModelIds = new ArrayList<Long>();
		for(Object key : map.keySet()){
			EmailModel emailModel = this.emailModelService.getObjById(CommUtil.null2Long(key));
			emailModel.setDisplay(true);
			emailModel.setTemplate_id(CommUtil.null2Long(map.get(key)));
			this.emailModelService.update(emailModel);
			emailModelIds.add(CommUtil.null2Long(key));
		}
		Map params = new HashMap();
		List<EmailModel> emailModels = null;
		if(emailModelIds.size() > 0){
			params.put("ids", emailModelIds);
			emailModels = this.emailModelService.query("select obj from EmailModel obj where obj.id not in(:ids)", params, -1, -1);
		}else{
			params.clear();
			emailModels = this.emailModelService.query("select obj from EmailModel obj", params, -1, -1);
		}
		for(EmailModel emailModel : emailModels){
			emailModel.setDisplay(false);
			emailModel.setTemplate_id(null);
			this.emailModelService.update(emailModel);
		}
		ModelAndView mv = new JModelAndView("admin/blue/success.html", configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url);
		mv.addObject("op_title", "设置邮件模板成功");
		if (add_url != null) {
			mv.addObject("add_url", add_url);
		}
		return mv;
	}
}
