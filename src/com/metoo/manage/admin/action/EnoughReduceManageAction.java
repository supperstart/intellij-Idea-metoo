package com.metoo.manage.admin.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.EnoughReduce;
import com.metoo.foundation.domain.query.EnoughReduceQueryObject;
import com.metoo.foundation.domain.query.GoodsQueryObject;
import com.metoo.foundation.service.IEnoughReduceService;
import com.metoo.foundation.service.IGoodsBrandService;
import com.metoo.foundation.service.IGoodsClassService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;

/**
 * 
 * <p>
 * Title: EnoughReduceManageAction.java
 * </p>
 * 
 * <p>
 * Description: 满就减控制器，对整个平台的满就减活动进行管理
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
 * @author lixiaoyang
 * 
 * @date 2014-9-22
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class EnoughReduceManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IEnoughReduceService enoughreduceService;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * EnoughReduce列表页
	 * 
	 * @param currentPage
	 * @param orderBy
	 * @param orderType
	 * @param request
	 * @return
	 */
	@SecurityMapping(title = "满就减活动列表", value = "/admin/enoughreduce_list.htm*", rtype = "admin", rname = "满就减管理", rcode = "enoughreduce_admin", rgroup = "运营")
	@RequestMapping("/admin/enoughreduce_list.htm")
	public ModelAndView enoughreduce_list(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String orderBy,
			String orderType, String ertitle, String erstatus,
			String erbegin_time, String erend_time) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/enoughreduce_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		EnoughReduceQueryObject qo = new EnoughReduceQueryObject(currentPage,
				mv, "addTime", "desc");
		if (ertitle != null && !"".equals(ertitle)) {
			qo.addQuery("obj.ertitle", new SysMap("ertitle", ertitle), "=");
			mv.addObject("ertitle", ertitle);
		}
		if (erstatus != null && !"".equals(erstatus)) {
			qo.addQuery("obj.erstatus",
					new SysMap("erstatus", CommUtil.null2Int(erstatus)), "=");
			mv.addObject("erstatus", erstatus);
		}

		if (erbegin_time != null && !erbegin_time.equals("")) {
			qo.addQuery("DATE_FORMAT(obj.erbegin_time,'%Y-%m-%d')", new SysMap(
					"erbegin_time", erbegin_time), ">=");
			mv.addObject("erbegin_time", erbegin_time);
		}
		if (erend_time != null && !erend_time.equals("")) {
			qo.addQuery("DATE_FORMAT(obj.erend_time,'%Y-%m-%d')", new SysMap(
					"erend_time", erend_time), "<=");
			mv.addObject("erend_time", erend_time);
		}

		IPageList pList = this.enoughreduceService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", null, pList, mv);
		return mv;
	}

	@SecurityMapping(title = "满就减活动商品列表", value = "/admin/enoughreduce_goods_list.htm*", rtype = "admin", rname = "满就减管理", rcode = "enoughreduce_admin", rgroup = "运营")
	@RequestMapping("/admin/enoughreduce_goods_list.htm")
	public ModelAndView enoughreduce_goods_list(HttpServletRequest request,
			HttpServletResponse response, String er_id, String currentPage,
			String orderBy, String orderType) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/enoughreduce_goods.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		EnoughReduce er = this.enoughreduceService.getObjById(CommUtil
				.null2Long(er_id));
		GoodsQueryObject qo = new GoodsQueryObject(currentPage, mv, orderBy,
				orderType);
		if (er_id != null && !"".equals(er_id)) {
			Map para = new HashMap();
			para.put("ids", genericIds(er.getErgoods_ids_json()));
			qo.addQuery("obj.id in (:ids)", para);// id被记录下来的
		}
		IPageList pList = this.goodsService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);

		mv.addObject("er", er);
		return mv;
	}

	@SecurityMapping(title = "满就减活动保存", value = "/admin/enoughreduce_verify.htm*", rtype = "admin", rname = "满就减管理", rcode = "enoughreduce_admin", rgroup = "运营")
	@RequestMapping("/admin/enoughreduce_verify.htm")
	public ModelAndView enoughreduce_verify(HttpServletRequest request,
			HttpServletResponse response, String er_id, String erstatus,
			String list_url, String failed_reason) {
		EnoughReduce enoughreduce = null;
		if (er_id != null && !er_id.equals("")) {
			enoughreduce = this.enoughreduceService.getObjById(CommUtil
					.null2Long(er_id));
			if (CommUtil.null2Int(erstatus) == -10) {
				enoughreduce.setErstatus(CommUtil.null2Int(erstatus));
				enoughreduce.setFailed_reason(failed_reason);
			}
			if (CommUtil.null2Int(erstatus) == 10) {
				enoughreduce.setErstatus(CommUtil.null2Int(erstatus));
				enoughreduce.setFailed_reason("");
			}
			this.enoughreduceService.update(enoughreduce);
		}
		ModelAndView mv = new JModelAndView("admin/blue/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		mv.addObject("list_url", list_url);
		mv.addObject("op_title", "审核完成");
		return mv;
	}

	private Set<Long> genericIds(String str) {
		Set<Long> ids = new HashSet<Long>();
		List list = (List) Json.fromJson(str);
		for (Object object : list) {
			ids.add(CommUtil.null2Long(object));
		}
		return ids;
	}
}