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
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.UserQueryObject;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * 
 * 
 * <p>
 * Title: AdminEvaManageAction.java
 * </p>
 * 
 * <p>
 * Description: 买家对自营商品管理员的评价
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com<／p>
 * 
 * @author jinxinzhe
 * 
 * @date 2014年5月6日
 * 
 * @version koala_b2b2c 2.0
 */

@Controller
public class AdminEvaManageAction {
	@Autowired
	private IUserService userService;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	
	@SecurityMapping(title = "管理员服务评价", value = "/admin/admin_eva.htm*", rtype = "admin", rname = "服务评价", rcode = "admin_evas", rgroup = "自营")
	@RequestMapping("/admin/admin_eva.htm")
	public ModelAndView admin_eva(String currentPage, String orderBy,
			String orderType, HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new JModelAndView("admin/blue/admin_eva.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		UserQueryObject uqo = new UserQueryObject(currentPage, mv, orderBy,
				orderType);
		WebForm wf = new WebForm();
		wf.toQueryPo(request, uqo, User.class, mv);
		uqo.addQuery("obj.userRole", new SysMap("userRole", "ADMIN"), "=");
		uqo.addQuery("obj.userRole", new SysMap("userRole1",
				"ADMIN_BUYER_SELLER"), "=", "or");
		uqo.addQuery("obj.admin_sp.id is not null", null);
		IPageList pList = this.userService.list(uqo);
		String url = this.configService.getSysConfig().getAddress();
		if (url == null || url.equals("")) {
			url = CommUtil.getURL(request);
		}
		CommUtil.saveIPageList2ModelAndView(url + "/admin/admin_eva.htm", "",
				"", pList, mv);
		mv.addObject("userRole", "ADMIN");
		return mv;
	}
}
