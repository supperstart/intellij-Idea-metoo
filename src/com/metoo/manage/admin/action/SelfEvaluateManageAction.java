package com.metoo.manage.admin.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.ImageTools;

/**
 * 
 * <p>
 * Title: SelfEvaluateManageAction.java
 * </p>
 * 
 * <p>
 * Description: 平台评价回复控制器。
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
 * @author jinxinzhe
 * 
 * @date 2014-12-3
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class SelfEvaluateManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ImageTools imageTools;

	@SecurityMapping(title = "商品评价列表", value = "/admin/self_evaluate_list.htm*", rtype = "admin", rname = "评价管理", rcode = "self_evaluate_admin", rgroup = "自营")
	@RequestMapping("/admin/self_evaluate_list.htm")
	public ModelAndView self_evaluate_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String status) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/self_evaluate_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		EvaluateQueryObject qo = new EvaluateQueryObject(currentPage, mv,
				orderBy, orderType);
		qo.addQuery("obj.evaluate_goods.goods_type",
				new SysMap("goods_type", 0), "=");
		qo.addQuery("obj.evaluate_status", new SysMap("evaluate_status", 0),
				"=");
		qo.addQuery("obj.evaluate_type", new SysMap("evaluate_type", "goods"),
				"=");
		if ("yes".equals(status)) {
			qo.addQuery("obj.reply_status", new SysMap("reply_status", 1), "=");
			mv.addObject("status", status);
		}
		if ("no".equals(status)) {
			qo.addQuery("obj.reply_status", new SysMap("reply_status", 0), "=");
			mv.addObject("status", status);
		}
		IPageList pList = this.evaluateService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		return mv;
	}

	@SecurityMapping(title = "商品评价内容", value = "/admin/self_evaluate_info.htm*", rtype = "admin", rname = "评价管理", rcode = "self_evaluate_admin", rgroup = "自营")
	@RequestMapping("/admin/self_evaluate_info.htm")
	public ModelAndView self_evaluate_info(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/self_evaluate_info.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		mv.addObject("evl", evl);
		mv.addObject("imageTools", imageTools);
		return mv;
	}

	@SecurityMapping(title = "商品评价内容", value = "/admin/self_evaluate_reply.htm*", rtype = "admin", rname = "评价管理", rcode = "self_evaluate_admin", rgroup = "自营")
	@RequestMapping("/admin/self_evaluate_reply.htm")
	public ModelAndView evaluate_reply(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/self_evaluate_reply.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		mv.addObject("evl", evl);
		mv.addObject("id", id);
		return mv;
	}

	@SecurityMapping(title = "商品评价内容", value = "/admin/self_evaluate_reply_save.htm*", rtype = "admin", rname = "评价管理", rcode = "self_evaluate_admin", rgroup = "自营")
	@RequestMapping("/admin/self_evaluate_reply_save.htm")
	public void evaluate_reply_save(HttpServletRequest request,
			HttpServletResponse response, String id, String reply) {
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		if (evl != null) {
			evl.setReply(CommUtil.filterHTML(reply));
			evl.setReply_status(1);
			this.evaluateService.update(evl);
		}
	}
}
