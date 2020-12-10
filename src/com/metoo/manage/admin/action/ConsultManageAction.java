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
import com.metoo.foundation.domain.Consult;
import com.metoo.foundation.domain.query.ConsultQueryObject;
import com.metoo.foundation.service.IConsultService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;

/**
 * 
 * <p>
 * Title: ConsultManageAction.java
 * </p>
 * 
 * <p>
 * Description: 系统咨询管理类，
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
 * @date 2014年4月24日
 *
 * @version koala_b2b2c v2.0 2015版 
 */
@Controller
public class ConsultManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IConsultService consultService;

	/**
	 * Consult列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "咨询列表", value = "/admin/consult_list.htm*", rtype = "admin", rname = "咨询管理", rcode = "consult_admin", rgroup = "交易")
	@RequestMapping("/admin/consult_list.htm")
	public ModelAndView list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String consult_user_userName,
			String consult_content) {
		ModelAndView mv = new JModelAndView("admin/blue/consult_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		ConsultQueryObject qo = new ConsultQueryObject(currentPage, mv,
				orderBy, orderType);
		qo.setPageSize(3);
		if (consult_user_userName != null && !consult_user_userName.equals("")) {
			qo.addQuery(
					"obj.consult_user_name",
					new SysMap("consult_user_name", CommUtil.null2String(
							consult_user_userName).trim()), "=");
		}
		IPageList pList = this.consultService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("consult_user_userName", consult_user_userName);
		mv.addObject("consult_content", consult_content);
		return mv;
	}

	@SecurityMapping(title = "咨询删除", value = "/admin/consult_del.htm*", rtype = "admin", rname = "咨询管理", rcode = "consult_admin", rgroup = "交易")
	@RequestMapping("/admin/consult_del.htm")
	public String delete(HttpServletRequest request,
			HttpServletResponse response, String mulitId, String currentPage) {
		String[] ids = mulitId.split(",");
		for (String id : ids) {
			if (!id.equals("")) {
				Consult consult = this.consultService.getObjById(Long
						.parseLong(id));
				this.consultService.delete(Long.parseLong(id));
			}
		}
		return "redirect:consult_list.htm?currentPage=" + currentPage;
	}
}