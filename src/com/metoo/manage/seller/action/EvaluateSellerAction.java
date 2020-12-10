package com.metoo.manage.seller.action;

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
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.EvaluateQueryObject;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.ImageTools;

/**
 * 
 * <p>
 * Title: EvaluateSellerAction.java
 * </p>
 * 
 * <p>
 * Description: 卖家评价管理控制器，
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
public class EvaluateSellerAction {
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
	
	@SecurityMapping(title = "商品评价列表", value = "/seller/evaluate_list.htm*", rtype = "seller", rname = "评价管理", rcode = "evaluate_seller", rgroup = "客户服务")
	@RequestMapping("/seller/evaluate_list.htm")
	public ModelAndView evaluate_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType,String status) {
		ModelAndView mv = new JModelAndView("user/default/sellercenter/evaluate_list.html",
				configService.getSysConfig(), this.userConfigService
						.getUserConfig(), 0, request, response);
		EvaluateQueryObject qo = new EvaluateQueryObject(currentPage, mv,
				orderBy, orderType);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Store store = user.getStore();
		qo.addQuery("obj.evaluate_goods.goods_store.id", new SysMap("store_id", store.getId()), "=");
		qo.addQuery("obj.evaluate_status", new SysMap("evaluate_status", 0), "=");
		qo.addQuery("obj.evaluate_type", new SysMap("evaluate_type", "goods"), "=");
		if("yes".equals(status)){
			qo.addQuery("obj.reply_status", new SysMap("reply_status", 1), "=");
			mv.addObject("status", status);
		}
		if("no".equals(status)){
			qo.addQuery("obj.reply_status", new SysMap("reply_status", 0), "=");
			mv.addObject("status", status);
		}
		IPageList pList = this.evaluateService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		return mv;
	}
	
	@SecurityMapping(title = "商品评价内容", value = "/seller/evaluate_info.htm*", rtype = "seller", rname = "评价管理", rcode = "evaluate_seller", rgroup = "客户服务")
	@RequestMapping("/seller/evaluate_info.htm")
	public ModelAndView evaluate_info(HttpServletRequest request,
			HttpServletResponse response,String id) {
		ModelAndView mv = new JModelAndView("user/default/sellercenter/evaluate_info.html",
				configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		if(evl!=null&&evl.getOf().getStore_id().equals(user.getStore().getId().toString())){
			mv.addObject("evl", evl);
			mv.addObject("imageTools", imageTools);
		}else{
			mv.addObject("ret", 0);
		}
		return mv;
	}
	
	@SecurityMapping(title = "商品评价内容", value = "/seller/evaluate_reply.htm*", rtype = "seller", rname = "评价管理", rcode = "evaluate_seller", rgroup = "客户服务")
	@RequestMapping("/seller/evaluate_reply.htm")
	public ModelAndView evaluate_reply(HttpServletRequest request,
			HttpServletResponse response,String id) {
		ModelAndView mv = new JModelAndView("user/default/sellercenter/evaluate_reply.html",
				configService.getSysConfig(), this.userConfigService.getUserConfig(), 0, request, response);
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		if(evl!=null&&evl.getOf().getStore_id().equals(user.getStore().getId().toString())){
			mv.addObject("evl", evl);
		}else{
			mv.addObject("ret", 0);
		}
		mv.addObject("id", id);
		return mv;
	}
	
	@SecurityMapping(title = "商品评价内容", value = "/seller/evaluate_reply_save.htm*", rtype = "seller", rname = "评价管理", rcode = "evaluate_seller", rgroup = "客户服务")
	@RequestMapping("/seller/evaluate_reply_save.htm")
	public void evaluate_reply_save(HttpServletRequest request,
			HttpServletResponse response,String id,String reply) {
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		user = user.getParent() == null ? user : user.getParent();
		Evaluate evl = this.evaluateService.getObjById(CommUtil.null2Long(id));
		if(evl!=null&&evl.getOf().getStore_id().equals(user.getStore().getId().toString())){
			evl.setReply(CommUtil.filterHTML(reply));
			evl.setReply_status(1);
			this.evaluateService.update(evl);
		}
	}
}
