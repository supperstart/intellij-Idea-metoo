package com.metoo.manage.buyer.action;

import java.util.Date;

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
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.WebForm;
import com.metoo.foundation.domain.FreeApplyLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.FreeApplyLogQueryObject;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IFreeApplyLogService;
import com.metoo.foundation.service.IFreeClassService;
import com.metoo.foundation.service.IFreeGoodsService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.manage.admin.tools.FreeTools;
import com.metoo.manage.buyer.tools.ShipTools;

/**
 * 
 * <p>
 * Title: FreeBuyerAction.java
 * </p>
 * 
 * <p>
 * Description: 用户中心0元试用中心
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
 * @date 2014-11-18
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Controller
public class FreeBuyerAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IFreeGoodsService freegoodsService;
	@Autowired
	private IFreeClassService freeClassService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsClassService goodsClassService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private FreeTools freeTools;
	@Autowired
	private IFreeApplyLogService freeapplylogService;
	@Autowired
	private IExpressCompanyService expressCompayService;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private ShipTools shipTools;

	@SecurityMapping(title = "买家中心", value = "/buyer/freeapply_logs.htm*", rtype = "buyer", rname = "买家中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/freeapply_logs.htm")
	public ModelAndView freeapply_logs(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String status) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/freeapplylog_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		FreeApplyLogQueryObject qo = new FreeApplyLogQueryObject(currentPage,
				mv, orderBy, orderType);
		qo.addQuery("obj.user_id",
				new SysMap("user_id", CommUtil.null2Long(SecurityUserHolder
						.getCurrentUser().getId())), "=");
		//[0为待审核 5为申请通过 等待收货 -5申请失败 -10为过期作废]
		if (status != null && status.equals("yes")) {
			qo.addQuery("obj.apply_status", new SysMap("apply_status", 5), "=");
			mv.addObject("status", status);
		}
		if (status != null && status.equals("waiting")) {
			qo.addQuery("obj.apply_status", new SysMap("apply_status", 0), "=");
			mv.addObject("status", status);
		}
		if (status != null && status.equals("no")) {
			qo.addQuery("obj.apply_status", new SysMap("apply_status", -5), "=");
			mv.addObject("status", status);
		}
		WebForm wf = new WebForm();
		wf.toQueryPo(request, qo, FreeApplyLog.class, mv);
		IPageList pList = this.freeapplylogService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		return mv;
	}

	@SecurityMapping(title = "买家中心", value = "/buyer/freeapply_log_info.htm*", rtype = "buyer", rname = "买家中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/freeapply_log_info.htm")
	public ModelAndView freeapply_log_info(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/default/usercenter/freeapplylog_info.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		User user = SecurityUserHolder.getCurrentUser();
		FreeApplyLog fal = this.freeapplylogService.getObjById(CommUtil
				.null2Long(id));
		if (fal != null && fal.getUser_id().equals(user.getId())) {
			mv.addObject("obj", fal);
			mv.addObject("shipTools", shipTools);
		} else {
			mv = new JModelAndView("error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "此0元试用申请无效");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/buyer/freeapply_logs.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "买家中心", value = "/buyer/freeapply_log_info.htm*", rtype = "buyer", rname = "买家中心", rcode = "user_center", rgroup = "用户中心")
	@RequestMapping("/buyer/freeapplylog_save.htm")
	public void freeapplylog_save(HttpServletRequest request,
			HttpServletResponse response, String id, String use_experience) {
		User user = SecurityUserHolder.getCurrentUser();
		FreeApplyLog fal = this.freeapplylogService.getObjById(CommUtil
				.null2Long(id));
		if (fal.getUser_id().equals(user.getId())) {
			fal.setUse_experience(use_experience);
			fal.setEvaluate_time(new Date());
			fal.setEvaluate_status(1);
			this.freeapplylogService.save(fal);
		}
	}
}
