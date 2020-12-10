package com.metoo.manage.admin.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.SOAPUtils;
import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.ddu.pojo.DduTaskRequest;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.domain.ExpressCompany;
import com.metoo.foundation.domain.ExpressCompanyCommon;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.ShipAddress;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.OrderFormQueryObject;
import com.metoo.foundation.domain.virtual.TransInfo;
import com.metoo.foundation.service.IAreaService;
import com.metoo.foundation.service.IExpressCompanyCommonService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IShipAddressService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.domain.ExpressInfo;
import com.metoo.kuaidi100.pojo.TaskRequest;
import com.metoo.kuaidi100.pojo.TaskResponse;
import com.metoo.kuaidi100.post.HttpRequest;
import com.metoo.kuaidi100.post.JacksonHelper;
import com.metoo.kuaidi100.service.IExpressInfoService;
import com.metoo.kuaidi100.utils.MD5;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.seller.tools.OrderTools;
import com.metoo.msg.MsgTools;
import com.metoo.pay.alipay.config.AlipayConfig;
import com.metoo.pay.alipay.util.AlipaySubmit;

import net.sf.json.JSONObject;

/**
 * 
 * 
 * <p>
 * Title: OrderSelfManageAction.java
 * </p>
 * 
 * <p>
 * Description: 自营商品订单管理器，显示所有自营商品订单，添加权限的管理员都可进行管理。
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
 * @date 2014年4月24日
 * 
 * @version koala_b2b2c 2.0
 */
@Controller
public class SelfOrderManageAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IExpressCompanyService expressCompanyService;
	@Autowired
	private IShipAddressService shipAddressService;
	@Autowired
	private IExpressCompanyCommonService expressCompanyCommonService;
	@Autowired
	private IAreaService areaService;
	@Autowired
	private IExpressInfoService expressInfoService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private OrderTools orderTools;

	private static final BigDecimal WHETHER_ENOUGH = new BigDecimal(0.00);
	private static final Map<Integer, String> STATUS_MAP = new HashMap<Integer, String>() {
		{
			put(0, "已取消");
			put(10, "待付款");
			put(15, "线下支付待审核");
			put(16, "货到付款待发货");
			put(20, "已付款");
			put(30, "已发货");
			put(40, "已收货");
			put(50, "已完成");
			put(60, "已结束");
		}
	};

	private static final Map<String, String> PAYMENT_MAP = new HashMap<String, String>() {
		{
			put(null, "未支付");
			put("", "未支付");
			put("alipay", "支付宝");
			put("alipay_wap", "手机网页支付宝");
			put("alipay_app", "手机支付宝APP");
			put("tenpay", "财付通");
			put("bill", "快钱");
			put("chinabank", "网银在线");
			put("outline", "线下支付");
			put("balance", "预存款支付");
			put("payafter", "货到付款");
			put("paypal", "paypal");
		}
	};

	private static final Map<String, String> TYPE_MAP = new HashMap<String, String>() {
		{
			put(null, "PC订单");
			put("", "PC订单");
			put("weixin", "微信订单");
			put("android", "Android订单");
			put("ios", "IOS订单");
		}
	};

	@SecurityMapping(title = "自营订单列表", value = "/admin/self_order.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/self_order.htm")
	public ModelAndView self_order(HttpServletRequest request,
			HttpServletResponse response, String currentPage,
			String order_status, String order_id, String beginTime,
			String endTime, String buyer_userName) {
		ModelAndView mv = new JModelAndView("admin/blue/self_order.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv,
				"addTime", "desc");
		ofqo.addQuery("obj.order_form", new SysMap("order_form", 1), "=");
		ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
		if (!CommUtil.null2String(order_status).equals("")) {
			if ("order_submit".equals(order_status)) {// 已经提交
				
				Map map = new HashMap();
				map.put("order_status1", 10);
				map.put("order_status2", 16);
				ofqo.addQuery(
						"(obj.order_status=:order_status1 or obj.order_status=:order_status2)",
						map);
			}
			if ("order_pay".equals(order_status)) {// 已经付款
				Map map = new HashMap();
				map.put("order_status1", 20);
				map.put("order_status2", 16);
				map.put("order_status3", 66);
				ofqo.addQuery(
						"(obj.order_status=:order_status1 or obj.order_status=:order_status2 or obj.order_status=:order_status3)",
						map);
			}
			if ("order_shipping".equals(order_status)) {// 已经发货
				ofqo.addQuery("obj.order_status",
						new SysMap("order_status", 30), "=");
			}
			if ("order_evaluate".equals(order_status)) {// 买家已评价
				ofqo.addQuery("obj.order_status",
						new SysMap("order_status", 40), "=");
			}
			if ("order_finish".equals(order_status)) {// 已经完成
				ofqo.addQuery("obj.order_status",
						new SysMap("order_status", 50), "=");
			}
			if ("order_cancel".equals(order_status)) {// 已经取消
				ofqo.addQuery("obj.order_status",
						new SysMap("order_status", 0), "=");
			}
		}
		if (!CommUtil.null2String(order_id).equals("")) {
			ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id
					+ "%"), "like");
		}
		
		if (!CommUtil.null2String(beginTime).equals("")) {
			ofqo.addQuery("obj.addTime",
					new SysMap("beginTime", CommUtil.formatDate(beginTime)),
					">=");
			mv.addObject("beginTime", beginTime);
		}
		if (!CommUtil.null2String(endTime).equals("")) {
			String ends = endTime + " 23:59:59";
			ofqo.addQuery(
					"obj.addTime",
					new SysMap("endTime", CommUtil.formatDate(ends,
							"yyyy-MM-dd hh:mm:ss")), "<=");
			mv.addObject("endTime", endTime);
		}
		if (!CommUtil.null2String(buyer_userName).equals("")) {
			ofqo.addQuery("obj.user_name", new SysMap("user_name",
					buyer_userName), "=");
		}
		IPageList pList = this.orderFormService.list(ofqo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("order_id", order_id);
		mv.addObject("order_status", order_status == null ? "all"
				: order_status);
		mv.addObject("beginTime", beginTime);
		mv.addObject("endTime", endTime);
		mv.addObject("buyer_userName", buyer_userName);
		mv.addObject("list_url", CommUtil.getURL(request)
				+ "/admin/self_order.htm");
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "自营待发货订单列表", value = "/admin/self_order_ship.htm*", rtype = "admin", rname = "发货管理", rcode = "order_ship", rgroup = "自营")
	@RequestMapping("/admin/self_order_ship.htm")
	public ModelAndView self_order_ship(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String buyer_userName) {
		ModelAndView mv = new JModelAndView("admin/blue/self_order_ship.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv,
				"payTime", "desc");
		ofqo.addQuery("obj.order_form", new SysMap("order_form", 1), "=");
		ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("order_status1", 20);
		map.put("order_status2", 16);
		ofqo.addQuery(
				"(obj.order_status=:order_status1 or obj.order_status=:order_status2)",
				map);
		if (!CommUtil.null2String(order_id).equals("")) {
			ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id
					+ "%"), "like");
		}
		if (!CommUtil.null2String(beginTime).equals("")) {
			ofqo.addQuery("obj.addTime",
					new SysMap("beginTime", CommUtil.formatDate(beginTime)),
					">=");
			mv.addObject("beginTime", beginTime);
		}
		if (!CommUtil.null2String(endTime).equals("")) {
			String ends = endTime + " 23:59:59";
			ofqo.addQuery(
					"obj.addTime",
					new SysMap("endTime", CommUtil.formatDate(ends,
							"yyyy-MM-dd hh:mm:ss")), "<=");
			mv.addObject("endTime", endTime);
		}
		if (!CommUtil.null2String(buyer_userName).equals("")) {
			ofqo.addQuery("obj.user_name", new SysMap("user_name",
					buyer_userName), "=");
		}
		IPageList pList = this.orderFormService.list(ofqo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("order_id", order_id);
		mv.addObject("beginTime", beginTime);
		mv.addObject("endTime", endTime);
		mv.addObject("buyer_userName", buyer_userName);
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "自营待发货订单详情", value = "/admin/ship_order_view.htm*", rtype = "admin", rname = "发货管理", rcode = "order_ship", rgroup = "自营")
	@RequestMapping("/admin/ship_order_view.htm")
	public ModelAndView ship_order_view(HttpServletRequest request,
			HttpServletResponse response, String id, String view_type) {
		ModelAndView mv = new JModelAndView("admin/blue/ship_order_view.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj.getOrder_cat() == 1) {
			mv = new JModelAndView("admin/blue/order_recharge_view.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		} else {
			mv.addObject("obj", obj);
		}
		mv.addObject("express_company_name", this.orderFormTools.queryExInfo(
				obj.getExpress_info(), "express_company_name"));
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("obj", obj);
		mv.addObject("view_type", view_type);
		return mv;
	}

	@SecurityMapping(title = "自营待发货订单列表", value = "/admin/self_order_confirm.htm*", rtype = "admin", rname = "收货管理", rcode = "order_confirm", rgroup = "自营")
	@RequestMapping("/admin/self_order_confirm.htm")
	public ModelAndView self_order_confirm(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String buyer_userName) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/self_order_confirm.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv,
				"shipTime", "desc");
		ofqo.addQuery("obj.order_form", new SysMap("order_form", 1), "=");
		ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
		Map<String,Object> temp = new HashMap<String,Object>();
		temp.put("order_status1", 30);
		temp.put("order_status2", 35);
		ofqo.addQuery(
				"and (obj.order_status =:order_status1 or obj.order_status =:order_status2)",
				temp);
		if (!CommUtil.null2String(order_id).equals("")) {
			ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id
					+ "%"), "like");
		}
		if (!CommUtil.null2String(beginTime).equals("")) {
			ofqo.addQuery("obj.addTime",
					new SysMap("beginTime", CommUtil.formatDate(beginTime)),
					">=");
			mv.addObject("beginTime", beginTime);
		}
		if (!CommUtil.null2String(endTime).equals("")) {
			String ends = endTime + " 23:59:59";
			ofqo.addQuery(
					"obj.addTime",
					new SysMap("endTime", CommUtil.formatDate(ends,
							"yyyy-MM-dd hh:mm:ss")), "<=");
			mv.addObject("endTime", endTime);
		}
		if (!CommUtil.null2String(buyer_userName).equals("")) {
			ofqo.addQuery("obj.user_name", new SysMap("user_name",
					buyer_userName), "=");
		}
		IPageList pList = this.orderFormService.list(ofqo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("order_id", order_id);
		mv.addObject("beginTime", beginTime);
		mv.addObject("endTime", endTime);
		mv.addObject("buyer_userName", buyer_userName);
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("orderTools", this.orderTools);
		return mv;
	}

	@SecurityMapping(title = "自营待收货订单详情", value = "/admin/confirm_order_view.htm*", rtype = "admin", rname = "收货管理", rcode = "order_confirm", rgroup = "自营")
	@RequestMapping("/admin/confirm_order_view.htm")
	public ModelAndView confirm_order_view(HttpServletRequest request,
			HttpServletResponse response, String id, String view_type) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/confirm_order_view.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		//[订单分类，0为购物订单，1为手机充值订单 2为生活类团购订单  3为商品类团购订单 4旅游报名订单]
		if (obj.getOrder_cat() == 1) {
			mv = new JModelAndView("admin/blue/order_recharge_view.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		} else {
			mv.addObject("obj", obj);
		}
		mv.addObject("express_company_name", this.orderFormTools.queryExInfo(
				obj.getExpress_info(), "express_company_name"));
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("obj", obj);
		mv.addObject("view_type", view_type);
		return mv;
	}

	@SecurityMapping(title = "自营订单延长收货时间", value = "/admin/self_order_comfirm_delay.htm*", rtype = "admin", rname = "收货管理", rcode = "order_confirm", rgroup = "自营")
	@RequestMapping("/admin/self_order_comfirm_delay.htm")
	public ModelAndView order_comfirm_delay(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/self_order_comfirm_delay.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj.getOrder_form() == 1) {// 只能处理自营订单
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;

	}

	@SecurityMapping(title = "自营订单延长收货时间保存", value = "/admin/self_order_confirm_delay_save.htm*", rtype = "admin", rname = "收货管理", rcode = "order_confirm", rgroup = "自营")
	@RequestMapping("/admin/self_order_confirm_delay_save.htm")
	public String self_order_confirm_delay_save(HttpServletRequest request,
			HttpServletResponse response, String id, String delay_time,
			String currentPage) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj.getOrder_form() == 1) {// 只能处理自营订单
			obj.setOrder_confirm_delay(obj.getOrder_confirm_delay()
					+ CommUtil.null2Int(delay_time));
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("延长收货时间");
			ofl.setState_info("延长收货时间：" + delay_time + "天");
			ofl.setLog_user(SecurityUserHolder.getCurrentUser());
			ofl.setOf(obj);
			this.orderFormLogService.save(ofl);
		}
		return "redirect:self_order_confirm.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "调整订单费用", value = "/admin/order_fee.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_fee.htm")
	public ModelAndView order_fee(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView("admin/blue/admin_order_fee.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "调整订单费用保存", value = "/admin/order_fee_save.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_fee_save.htm")
	public String order_fee_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String goods_amount, String ship_price, String totalPrice)
			throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			obj.setGoods_amount(BigDecimal.valueOf(CommUtil
					.null2Double(goods_amount)));
			obj.setShip_price(BigDecimal.valueOf(CommUtil
					.null2Double(ship_price)));
			obj.setTotalPrice(BigDecimal.valueOf(CommUtil
					.null2Double(totalPrice)));
			obj.setOperation_price_count(obj.getOperation_price_count() + 1);
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("调整订单费用");
			ofl.setState_info("调整订单总金额为:" + totalPrice + ",调整运费金额为:"
					+ ship_price);
			ofl.setLog_user(SecurityUserHolder.getCurrentUser());
			ofl.setOf(obj);
			this.orderFormLogService.save(ofl);
			User buyer = this.userService.getObjById(CommUtil.null2Long(obj
					.getUser_id()));
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("buyer_id", buyer.getId().toString());
			map.put("self_goods", this.configService.getSysConfig().getTitle());
			map.put("order_id", obj.getId());
			String json = Json.toJson(map);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"email_tobuyer_selforder_update_fee_notify",
					buyer.getEmail(), json, null);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"sms_tobuyer_selforder_fee_notify", buyer.getMobile(),
					json, null);
		}
		return "redirect:self_order.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "买家个人信息", value = "/admin/order_query_userinfor.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_query_userinfor.htm")
	public ModelAndView order_query_userinfor(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/order_query_userinfor.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		mv.addObject("obj", obj);
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "取消订单", value = "/admin/order_cancel.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_cancel.htm")
	public ModelAndView order_cancel(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/admin_order_cancel.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "取消订单保存", value = "/admin/order_cancel_save.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_cancel_save.htm")
	public String order_cancel_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String state_info, String other_state_info) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			obj.setOrder_status(0);
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("取消订单");
			ofl.setLog_user(SecurityUserHolder.getCurrentUser());
			ofl.setOf(obj);
			if (state_info.equals("other")) {
				ofl.setState_info(other_state_info);
			} else {
				ofl.setState_info(state_info);
			}
			this.orderFormLogService.save(ofl);
			User buyer = this.userService.getObjById(CommUtil.null2Long(obj
					.getUser_id()));
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("buyer_id", buyer.getId().toString());
			map.put("self_goods", this.configService.getSysConfig().getTitle());
			map.put("order_id", obj.getId());
			String json = Json.toJson(map);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"email_tobuyer_selforder_cancel_notify", buyer.getEmail(),
					json, null);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"sms_tobuyer_selforder_cancel_notify", buyer.getMobile(),
					json, null);
		}
		return "redirect:self_order.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "确认发货", value = "/admin/order_shipping.htm*", rtype = "admin", rname = "发货管理", rcode = "order_ship", rgroup = "自营")
	@RequestMapping("/admin/order_shipping.htm")
	public ModelAndView order_shipping(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String op) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/admin_order_shipping.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		//[订单种类，0为商家商品订单，1为平台自营商品订单]
		if (1 == obj.getOrder_form()) {
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
			//[根据订单id查询该订单中所有商品,包括子订单中的商品]
			List<Goods> list_goods = this.orderFormTools.queryOfGoods(id);
			List<Goods> deliveryGoods = new ArrayList<Goods>();
			boolean physicalGoods = false;
			for (Goods g : list_goods) {
				//[0实体商品，1为虚拟商品]
				if (g.getGoods_choice_type() == 1) {
					deliveryGoods.add(g);
				} else {
					physicalGoods = true;
				}
			}
			Map<String,Object> params = new HashMap<String,Object>();
			//[常用物流公司类型，0为商家的常用物流公司，1为自营的常用物流公司]
			params.put("ecc_type", 1);
			List<ExpressCompanyCommon> eccs = this.expressCompanyCommonService
					.query("select obj from ExpressCompanyCommon obj where obj.ecc_type=:ecc_type",
							params, -1, -1);
			params.clear();
			params.put("sa_type", 1);
			//[发货地址类型，0为商家发货地址，1为管理员发货地址,默认为0]
			List<ShipAddress> shipAddrs = this.shipAddressService
					.query("select obj from ShipAddress obj where obj.sa_type=:sa_type order by obj.sa_default desc,obj.sa_sequence asc",
							params, -1, -1);// 按照默认地址倒叙、其他地址按照索引升序排序，保证默认地址在第一位
			mv.addObject("shipAddrs", shipAddrs);
			mv.addObject("eccs", eccs);
			mv.addObject("physicalGoods", physicalGoods);
			mv.addObject("deliveryGoods", deliveryGoods);
			mv.addObject("orderFormTools", orderFormTools);
			mv.addObject("op", op);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "确认发货保存", value = "/admin/order_shipping_save.htm*", rtype = "admin", rname = "发货管理", rcode = "order_ship", rgroup = "自营")
	@RequestMapping("/admin/order_shipping_saves.htm")
	public String order_shipping_saves(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String shipCode, String state_info, String order_seller_intro,
			String ecc_id, String sa_id, String op) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		ExpressCompanyCommon ecc = this.expressCompanyCommonService
				.getObjById(CommUtil.null2Long(ecc_id));
		if (1 == obj.getOrder_form()) {
			obj.setEva_user_id(SecurityUserHolder.getCurrentUser().getId());
			//[ 订单状态，0为订单取消，10为已提交待付款，15为线下付款提交申请(已经取消该付款方式)，16为货到付款，20为已付款待发货（团购为已成团），30为已发货待收货，
			// 35,自提点已经收货，40为已收货, 50买家评价完毕 ,65订单不可评价，到达设定时间，系统自动关闭订单相互评价功能    66：未成团 （已付款）  70:退款申请成功   75:退款申请失败  80:退款成功  85：退款失败]
			obj.setOrder_status(30);
			//[物流单号]
			//obj.setShipCode(shipCode);
			//[发货时间]
			obj.setShipTime(new Date());
			//[{express_company_id=64, express_company_type=EXPRESS, express_company_name=DDU Express, express_company_mark=ddu}]
			if (ecc != null) {
				Map<String,Object> json_map = new HashMap<String,Object>();
				json_map.put("express_company_id", ecc.getId());
				json_map.put("express_company_name", ecc.getEcc_name());
				json_map.put("express_company_mark", ecc.getEcc_code());
				json_map.put("express_company_type", ecc.getEcc_ec_type());
				String express_json = Json.toJson(json_map);
				//[物流公司信息]
				obj.setExpress_info(express_json);
			}
			String[] order_seller_intros = request.getParameterValues("order_seller_intro");
			String[] goods_ids = request.getParameterValues("goods_id");
			String[] goods_names = request.getParameterValues("goods_name");
			String[] goods_counts = request.getParameterValues("goods_count");
			if(order_seller_intros!=null&&order_seller_intros.length>0){
				List<Map> list_map = new ArrayList<Map>();
				for (int i = 0; i < goods_ids.length; i++) {
					Map<String,Object> json_map = new HashMap<String,Object>();
					json_map.put("goods_id", goods_ids[i]);
					json_map.put("goods_name", goods_names[i]);
					json_map.put("goods_count", goods_counts[i]);
					json_map.put("order_seller_intro", order_seller_intros[i]);
					json_map.put("order_id", id);
					list_map.add(json_map);
				}
				obj.setOrder_seller_intro(Json.toJson(list_map));
			}
			//[express_company_mark]
			//[发货地址]
			ShipAddress shipAddress = this.shipAddressService.getObjById(CommUtil
					.null2Long(sa_id));
			if (shipAddress!=null) {
				//[ 发货详细地址]
				obj.setShip_addr_id(shipAddress.getId());
				Area area = this.areaService.getObjById(shipAddress.getSa_area_id());
				obj.setShip_addr(area.getParent().getParent().getAreaName()
						+ area.getParent().getAreaName() + area.getAreaName()
						+ shipAddress.getSa_addr());
			}		
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("确认发货");
			ofl.setState_info(state_info);
			ofl.setLog_user(SecurityUserHolder.getCurrentUser());
			ofl.setOf(obj);
			this.orderFormLogService.save(ofl);
			User buyer = this.userService.getObjById(CommUtil.null2Long(obj
					.getUser_id()));
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("buyer_id", buyer.getId().toString());
			map.put("self_goods", this.configService.getSysConfig().getTitle());
			map.put("order_id", obj.getId());
			String json = Json.toJson(map);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"email_tobuyer_selforder_ship_notify", buyer.getEmail(),
					json, null);
			this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"sms_tobuyer_selforder_ship_notify", buyer.getMobile(),
					json, null);
			Map express_map = Json.fromJson(Map.class,
					obj.getExpress_info());
			Long area_id = shipAddress.getSa_area_id();
			List<Map> goods_map = this.orderFormTools.queryGoodsInfo(
					obj.getGoods_info());
			Area area = areaService.getObjById(CommUtil.null2Long(area_id));
			DduTaskRequest ddutaskRequest = new DduTaskRequest();
			 
			    ddutaskRequest.setBatchNumber(obj.getOrder_id());
		        ddutaskRequest.setFromCompany(shipAddress.getSa_company());
		        ddutaskRequest.setFromAddress(area.getAreaName());
		        ddutaskRequest.setFromLocation(shipAddress.getSa_addr());
		        ddutaskRequest.setFromCountry(area.getParent().getParent().getAreaName());
		        ddutaskRequest.setFromCperson(shipAddress.getSa_user_name());
		        ddutaskRequest.setFromContactno(shipAddress.getSa_telephone());
		        //[面单不显示]
		        //ddutaskRequest.setFromMobileno(sa.getSa_telephone());
		        //ddutaskRequest.setToCompany();
		        ddutaskRequest.setToAddress(obj.getReceiver_area());
		        ddutaskRequest.setToLocation(obj.getReceiver_area_info());
		        //ddutaskRequest.setToCountry(sa.getSa_addr());
		        ddutaskRequest.setToCountry(obj.getReceiver_area());
		        ddutaskRequest.setToCperson(obj.getReceiver_Name());
		        ddutaskRequest.setToContactno(obj.getReceiver_telephone());
		        ddutaskRequest.setToMobileno(obj.getReceiver_telephone());
		        ddutaskRequest.setReferenceNumber(obj.getReceiver_mobile());
		        ddutaskRequest.setCompanyCode(CommUtil.null2String(express_map.get("express_company_mark")));
		        int pieces;
		        int weight;
		        int weightnum=0;
		        int piecesnum=0;
		        for(Map goods_maps:goods_map){
		        	pieces = CommUtil.null2Int(goods_maps.get("goods_count"));
		        	piecesnum += pieces;
		        	weight = CommUtil.null2Int(goods_maps.get("weight"));
		        }
		        ddutaskRequest.setWeight(weightnum);
		        ddutaskRequest.setPieces(piecesnum);
		        //ddutaskRequest.setPackageType("Document");
		        ddutaskRequest.setCurrencyCode("AED");
		        ddutaskRequest.setNcndAmount(obj.getTotalPrice());
		        ddutaskRequest.setItemDescription("DESCRIPTION");
		        ddutaskRequest.setSpecialInstruction("SPECIAL");
		        
		        StringBuffer stringBuffer = SOAPUtils.webServiceTow(ddutaskRequest);
		
		        String xmlResult = stringBuffer.toString().replace("<", "<");
		        String AWBNumber = SOAPUtils.getXmlMessageByName(xmlResult, "AWBNumber");
		        //报文返回状态码，0表示正常，3表示错误
		        String responseCode = SOAPUtils.getXmlMessageByName(xmlResult, "responseCode");
		        if("1".equals(responseCode)){
		        	obj.setShipCode(AWBNumber);
		        	this.orderFormService.update(obj);
		        }
		        System.out.println(AWBNumber);
			//确认发货后发送消息模板
           
			/*WeixinTemplate tem=new WeixinTemplate();
			tem.setTemplateId("rHdx9u-mb2_ciVN_7RqpPm9WO3MSGF_t_1RLZTZfjwU");
			tem.setTopColor("#00DD00");
			tem.setToUser(buyer.getOpenId());
			tem.setUrl("http://wx.fensekaola.com/wap/buyer/center.htm?op=center"); 
			 
			List<Map> maps = orderFormTools.queryGoodsInfo(obj.getGoods_info());
			StringBuffer sb = new StringBuffer();
			for (Map<String,Object> mm : maps) {
				sb.append(mm.get("goods_name")+"*"+mm.get("goods_count")+";");
			}
			
			List<WeixinTemplateParam> paras=new ArrayList<WeixinTemplateParam>();
			paras.add(new WeixinTemplateParam("first","您好，您有一个订单已经发货，请及时查看并收货。","#FF3333"));
			paras.add(new WeixinTemplateParam("keyword1",obj.getOrder_id(),"#0044BB"));
			paras.add(new WeixinTemplateParam("keyword2",obj.getReceiver_Name(),"#0044BB"));
			paras.add(new WeixinTemplateParam("keyword3",obj.getReceiver_area_info(),"#0044BB"));
			paras.add(new WeixinTemplateParam("keyword4",obj.getTransport(),"#0044BB"));
			paras.add(new WeixinTemplateParam("keyword5",obj.getShipCode(),"#0044BB"));
			paras.add(new WeixinTemplateParam("Remark","感谢你对我们商城的支持!!!!","#AAAAAA"));
					
			tem.setTemplateParamList(paras);
					
			boolean tempResult=WeixinUtil.sendTemplateMsg(configService.getSysConfig().getWeixin_token(),tem);
			
			System.out.println("........................");
			System.out.println(tempResult);*/

			// 异步通知支付宝,该方法用在支付宝担保支付，目的用来修改支付宝订单状态为“已发货”
			if (obj.getPayment() != null
					&& obj.getPayment().getMark().equals("alipay")
					&& obj.getPayment().getInterfaceType() == 1) {
				// 把请求参数打包成数组
				boolean synch = false;
				String safe_key = "";
				String partner = "";
				if (!CommUtil.null2String(obj.getPayment().getSafeKey())
						.equals("")
						&& !CommUtil.null2String(obj.getPayment().getPartner())
								.equals("")) {
					safe_key = obj.getPayment().getSafeKey();
					partner = obj.getPayment().getPartner();
					synch = true;
				}
				if (synch) {
					AlipayConfig config = new AlipayConfig();
					config.setKey(safe_key);
					config.setPartner(partner);
					Map<String, String> sParaTemp = new HashMap<String, String>();
					sParaTemp.put("service", "send_goods_confirm_by_platform");
					sParaTemp.put("partner", config.getPartner());
					sParaTemp.put("_input_charset", config.getInput_charset());
					sParaTemp.put("trade_no", obj.getOut_order_id());
					sParaTemp.put("logistics_name", ecc.getEcc_name());
					sParaTemp.put("invoice_no", shipCode);
					sParaTemp.put("transport_type", ecc.getEcc_ec_type());
					// 建立请求
					String sHtmlText = AlipaySubmit.buildRequest(config, "web",
							sParaTemp, "", "");
					// System.out.println(sHtmlText);
				}
			}
		}
		if (CommUtil.null2String(op).equals("self_order_ship")) {
			return "redirect:self_order_ship.htm?currentPage=" + currentPage;
		} else
			return "redirect:self_order.htm?currentPage=" + currentPage;
	}


	
	@SecurityMapping(title = "快递单打印", value = "/admin/order_ship_print.htm*", rtype = "admin", rname = "收货管理", rcode = "order_confirm", rgroup = "自营")
	@RequestMapping("/admin/order_ship_print.htm")
	public ModelAndView order_ship_print(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView("admin/blue/order_ship_print.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm order = this.orderFormService.getObjById(CommUtil
				.null2Long(id));
		if (order.getOrder_form() == 1) {// 只能打印自营的订单
			Map ec_map = Json.fromJson(Map.class, order.getExpress_info());
			ExpressCompanyCommon ecc = this.expressCompanyCommonService
					.getObjById(CommUtil.null2Long(ec_map
							.get("express_company_id")));
			if (ecc != null) {
				Map offset_map = Json.fromJson(Map.class,
						ecc.getEcc_template_offset());
				ShipAddress ship_addr = this.shipAddressService
						.getObjById(order.getShip_addr_id());
				mv.addObject("ecc", this.expressCompanyCommonService
						.getObjById(CommUtil.null2Long(ec_map
								.get("express_company_id"))));
				mv.addObject("offset_map", offset_map);
				mv.addObject("obj", order);
				mv.addObject("ship_addr", ship_addr);
				mv.addObject("area",
						this.areaService.getObjById(ship_addr.getSa_area_id()));
			} else {
				mv = new JModelAndView("admin/blue/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
				mv.addObject("op_title", "老版物流订单，无法打印！");
			}
		}
		return mv;
	}

	@SecurityMapping(title = "修改物流", value = "/admin/order_shipping_code.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_shipping_code.htm")
	public ModelAndView order_shipping_code(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/admin_order_shipping_code.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "修改物流保存", value = "/admin/order_shipping_code_save.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_shipping_code_save.htm")
	public String order_shipping_code_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String shipCode, String state_info) {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			obj.setShipCode(shipCode);
			this.orderFormService.update(obj);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info("修改物流信息");
			ofl.setState_info(state_info);
			ofl.setLog_user(SecurityUserHolder.getCurrentUser());
			ofl.setOf(obj);
			this.orderFormLogService.save(ofl);
			// 如果是收费接口，则通知快递100，建立订单物流查询推送
			if (this.configService.getSysConfig().getKuaidi_type() == 1) {
				TaskRequest req = new TaskRequest();
				Map express_map = Json.fromJson(Map.class,
						obj.getExpress_info());
				req.setCompany(CommUtil.null2String(express_map
						.get("express_company_mark")));
				String from_addr = obj.getShip_addr();
				req.setFrom(from_addr);
				req.setTo(obj.getReceiver_area());
				req.setNumber(obj.getShipCode());
				req.getParameters().put(
						"callbackurl",
						CommUtil.getURL(request)
								+ "/kuaidi100_callback.htm?order_id="
								+ obj.getId() + "&orderType=0");
				req.getParameters().put(
						"salt",
						Md5Encrypt.md5(CommUtil.null2String(obj.getId()))
								.toLowerCase());
				req.setKey(this.configService.getSysConfig().getKuaidi_id2());

				HashMap<String, String> p = new HashMap<String, String>();
				p.put("schema", "json");
				p.put("param", JacksonHelper.toJSON(req));
				try {
					String ret = HttpRequest.postData(
							"http://www.kuaidi100.com/poll", p, "UTF-8");
					TaskResponse resp = JacksonHelper.fromJSON(ret,
							TaskResponse.class);
					if (resp.getResult() == true) {
						ExpressInfo ei = new ExpressInfo();
						ei.setAddTime(new Date());
						ei.setOrder_id(obj.getId());
						ei.setOrder_express_id(obj.getShipCode());
						ei.setOrder_type(0);
						Map ec_map = Json.fromJson(Map.class,
								CommUtil.null2String(obj.getExpress_info()));
						if (ec_map != null) {
							ei.setOrder_express_name(CommUtil
									.null2String(ec_map
											.get("express_company_name")));
						}
						// System.out.println(Json.toJson(result.getData(),JsonFormat.compact()));
						this.expressInfoService.save(ei);
						System.out.println("订阅成功");
					} else {
						System.out.println("订阅失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "redirect:self_order.htm?currentPage=" + currentPage;
	}

	@SecurityMapping(title = "买家退货申请详情", value = "/admin/admin_order_return_apply_view.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/admin_order_return_apply_view.htm")
	public ModelAndView admin_order_return_apply_view(
			HttpServletRequest request, HttpServletResponse response,
			String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"admin/blue/admin_order_return_apply_view.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (1 == obj.getOrder_form()) {
			mv.addObject("obj", obj);
		} else {
			mv = new JModelAndView("admin/blue/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "打印订单", value = "/admin/order_print.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_print.htm")
	public ModelAndView order_print(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView("admin/blue/order_print.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (id != null && !id.equals("")) {
			OrderForm orderform = this.orderFormService.getObjById(CommUtil
					.null2Long(id));
			mv.addObject("obj", orderform);
		}
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "物流详情", value = "/admin/ship_view.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/ship_view.htm")
	public ModelAndView order_ship_view(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView("admin/blue/order_ship_view.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null && !obj.equals("")) {
			if (1 == obj.getOrder_form()) {
				mv.addObject("obj", obj);
				/*TransInfo transInfo = this.query_ship_getData(CommUtil
						.null2String(obj.getId()));*/
				TransInfo transInfo = this.query_ship_getData(this.orderFormTools.queryExInfo(
						obj.getExpress_info(), "express_company_mark"),CommUtil.null2String(obj.getShipCode()));
				mv.addObject("transInfo", transInfo);
				mv.addObject("express_company_name", this.orderFormTools
						.queryExInfo(obj.getExpress_info(),
								"express_company_name"));
			} else {
				mv = new JModelAndView("error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "您查询的物流不存在！");
				mv.addObject("list_url", CommUtil.getURL(request)
						+ "/admin/self_order.htm");
			}
		} else {
			mv = new JModelAndView("error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您查询的物流不存在！");
			mv.addObject("list_url", CommUtil.getURL(request)
					+ "/admin/self_order.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "导出表格", value = "/admin/order_excel.htm*", rtype = "admin", rname = "订单管理", rcode = "order_self", rgroup = "自营")
	@RequestMapping("/admin/order_excel.htm")
	public void order_excel(HttpServletRequest request,
			HttpServletResponse response, String order_status, String order_id,
			String beginTime, String endTime, String buyer_userName) {
		OrderFormQueryObject qo = new OrderFormQueryObject();
		qo.addQuery("obj.order_form", new SysMap("order_form", 1), "=");
		qo.setPageSize(1000000000);
		qo.setOrderBy("addTime");
		qo.setOrderType("desc");
		qo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
		if (!CommUtil.null2String(order_status).equals("")) {
			if (order_status.equals("order_submit")) {// 已经提交
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("order_status1", 10);
				map.put("order_status2", 16);
				qo.addQuery(
						"(obj.order_status=:order_status1 or obj.order_status=:order_status2)",
						map);
			}
			if (order_status.equals("order_pay")) {// 已经付款
				qo.addQuery("obj.order_status",
						new SysMap("order_status1", 16), ">=");
				qo.addQuery("obj.order_status",
						new SysMap("order_status2", 20), "<=");
			}
			if (order_status.equals("order_shipping")) {// 已经发货
				qo.addQuery("obj.order_status", new SysMap("order_status", 30),
						"=");
			}
			if (order_status.equals("order_evaluate")) {// 等待评价
				qo.addQuery("obj.order_status", new SysMap("order_status", 40),
						"=");
			}
			if (order_status.equals("order_finish")) {// 已经完成
				qo.addQuery("obj.order_status", new SysMap("order_status", 50),
						"=");
			}
			if (order_status.equals("order_cancel")) {// 已经取消
				qo.addQuery("obj.order_status", new SysMap("order_status", 0),
						"=");
			}
		}
		if (!CommUtil.null2String(order_id).equals("")) {
			qo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id
					+ "%"), "like");
		}
		if (!CommUtil.null2String(beginTime).equals("")) {
			qo.addQuery("obj.addTime",
					new SysMap("beginTime", CommUtil.formatDate(beginTime)),
					">=");
		}
		if (!CommUtil.null2String(endTime).equals("")) {
			String ends = endTime + " 23:59:59";
			qo.addQuery(
					"obj.addTime",
					new SysMap("endTime", CommUtil.formatDate(ends,
							"yyyy-MM-dd hh:mm:ss")), "<=");
		}
		if (!CommUtil.null2String(buyer_userName).equals("")) {
			qo.addQuery("obj.user_name",
					new SysMap("user_name", buyer_userName), "=");
		}
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_MONTH,
				ca.getActualMaximum(Calendar.DAY_OF_MONTH));
		qo.setOrderType("desc");
		IPageList pList = this.orderFormService.list(qo);
		if (pList.getResult() != null) {
			List<OrderForm> datas = pList.getResult();
			// 创建Excel的工作书册 Workbook,对应到一个excel文档
			HSSFWorkbook wb = new HSSFWorkbook();
			// 创建Excel的工作sheet,对应到一个excel文档的tab
			HSSFSheet sheet = wb.createSheet("订单列表");
			HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
			List<HSSFClientAnchor> anchor = new ArrayList<HSSFClientAnchor>();
			for (int i = 0; i < datas.size(); i++) {
				anchor.add(new HSSFClientAnchor(0, 0, 1000, 255, (short) 1,
						2 + i, (short) 1, 2 + i));
			}
			// 设置excel每列宽度
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 4000);
			sheet.setColumnWidth(2, 4000);
			sheet.setColumnWidth(3, 6000);
			sheet.setColumnWidth(4, 6000);
			sheet.setColumnWidth(5, 6000);
			sheet.setColumnWidth(6, 6000);
			sheet.setColumnWidth(7, 6000);
			sheet.setColumnWidth(8, 6000);
			sheet.setColumnWidth(9, 6000);
			// 创建字体样式
			HSSFFont font = wb.createFont();
			font.setFontName("Verdana");
			font.setBoldweight((short) 100);
			font.setFontHeight((short) 300);
			font.setColor(HSSFColor.BLUE.index);
			// 创建单元格样式
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			style.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			// 设置边框
			style.setBottomBorderColor(HSSFColor.RED.index);
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);
			style.setFont(font);// 设置字体
			// 创建Excel的sheet的一行
			HSSFRow row = sheet.createRow(0);
			row.setHeight((short) 500);// 设定行的高度
			// 创建一个Excel的单元格
			HSSFCell cell = row.createCell(0);
			// 合并单元格(startRow，endRow，startColumn，endColumn)
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
			// 给Excel的单元格设置样式和赋值
			cell.setCellStyle(style);
			String title = "订单列表";
			Date time1 = CommUtil.formatDate(beginTime);
			Date time2 = CommUtil.formatDate(endTime);
			String time = CommUtil.null2String(CommUtil.formatShortDate(time1)
					+ " - " + CommUtil.formatShortDate(time2));
			cell.setCellValue(this.configService.getSysConfig().getTitle()
					+ title + "（" + time + "）");
			// 设置单元格内容格式时间
			HSSFCellStyle style1 = wb.createCellStyle();
			style1.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-mm-dd"));
			style1.setWrapText(true);// 自动换行
			style1.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			HSSFCellStyle style2 = wb.createCellStyle();
			style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			row = sheet.createRow(1);
			cell = row.createCell(0);
			cell.setCellStyle(style2);
			cell.setCellValue("订单号");
			cell = row.createCell(1);
			cell.setCellStyle(style2);
			cell.setCellValue("商品名称");
			cell = row.createCell(2);
			cell.setCellStyle(style2);
			cell.setCellValue("商品数量");
			cell = row.createCell(3);
			cell.setCellStyle(style2);
			cell.setCellValue("订单总价");
			cell = row.createCell(4);
			cell.setCellStyle(style2);
			cell.setCellValue("支付时间");
			cell = row.createCell(5);
			cell.setCellStyle(style2);
			cell.setCellValue("联系人");
			cell = row.createCell(6);
			cell.setCellStyle(style2);
			cell.setCellValue("联系电话");
			cell = row.createCell(7);
			cell.setCellStyle(style2);
			cell.setCellValue("联系地址");
			cell = row.createCell(8);
			cell.setCellStyle(style2);
			cell.setCellValue("联系电话");
			cell = row.createCell(9);
			cell.setCellStyle(style2);
			cell.setCellValue("客户账号");
			
			int count = 1;
			for (int j = 2; j <= datas.size() + 1; j++) {
				
				List<Map> goods_json = new ArrayList<Map>();
				if (datas.size() >= j - 2 && datas.get(j - 2) != null) {
					goods_json = Json.fromJson(List.class, CommUtil
							.null2String(datas.get(j - 2).getGoods_info()));
				}
				StringBuilder sb = new StringBuilder();
				boolean whether_combin = false;
				if (goods_json != null) {
					for (Map map : goods_json) {
						row = sheet.createRow(++count);
						// 设置单元格的样式格式
						int i = 0;
						cell = row.createCell(i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j - 2).getOrder_id());
						
						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(map.get("goods_name").toString());
						
						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(map.get("goods_count").toString());
						
						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j - 2).getTotalPrice().toString());

						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(CommUtil.formatLongDate(datas.get(j - 2).getPayTime()));
						
						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j-2).getReceiver_Name());

						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j-2).getReceiver_mobile());

						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j-2).getReceiver_area()+datas.get(j-2).getReceiver_area_info());
						
						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j-2).getReceiver_mobile());

						cell = row.createCell(++i);
						cell.setCellStyle(style2);
						cell.setCellValue(datas.get(j-2).getUser_name());
					}
				}
				
			}
			
			/*double all_order_price = 0.00;// 订单总金额
			double all_total_amount = 0.00;// 商品总金额
			for (int j = 2; j <= datas.size() + 1; j++) {
				row = sheet.createRow(j);
				// 设置单元格的样式格式
				int i = 0;
				cell = row.createCell(i);
				cell.setCellStyle(style2);
				cell.setCellValue(datas.get(j - 2).getOrder_id());

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(CommUtil.formatLongDate(datas.get(j - 2)
						.getAddTime()));

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				if (datas.get(j - 2).getPayment() != null) {
					cell.setCellValue(PAYMENT_MAP.get(datas.get(j - 2)
							.getPayment().getMark()));
				} else {
					cell.setCellValue("未支付");
				}

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(TYPE_MAP
						.get(datas.get(j - 2).getOrder_type()));

				List<Map> goods_json = new ArrayList<Map>();
				if (datas.size() >= j - 2 && datas.get(j - 2) != null) {
					goods_json = Json.fromJson(List.class, CommUtil
							.null2String(datas.get(j - 2).getGoods_info()));
				}
				StringBuilder sb = new StringBuilder();
				boolean whether_combin = false;
				if (goods_json != null) {
					for (Map map : goods_json) {
						sb.append(map.get("goods_name") + "*"
								+ map.get("goods_count") + ",");
						if (map.get("goods_type").toString().equals("combin")) {
							whether_combin = true;
						}
					}
				}
				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(sb.toString());

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(datas.get(j - 2).getShipCode());

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(datas.get(j - 2).getShip_price().toString());

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(datas.get(j - 2).getGoods_amount().toString());
				all_total_amount = CommUtil.add(all_total_amount,
						datas.get(j - 2).getGoods_amount());// 计算商品总价

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(CommUtil.null2String(datas.get(j - 2)
						.getTotalPrice()));
				all_order_price = CommUtil.add(all_order_price, datas
						.get(j - 2).getTotalPrice());

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(STATUS_MAP.get(datas.get(j - 2)
						.getOrder_status()));

				cell = row.createCell(++i);
				cell.setCellStyle(style2);
				cell.setCellValue(CommUtil.formatLongDate(datas.get(j - 2)
						.getShipTime()));

				if (datas.get(j - 2).getWhether_gift() == 1) {
					List<Map> gifts_json = Json.fromJson(List.class,
							datas.get(j - 2).getGift_infos());
					StringBuilder gsb = new StringBuilder();
					for (Map map : gifts_json) {
						gsb.append(map.get("goods_name") + ",");
					}
					cell = row.createCell(++i);
					cell.setCellStyle(style2);
					cell.setCellValue(gsb.toString());
				}
				if (datas.get(j - 2).getEnough_reduce_amount() != null
						&& datas.get(j - 2).getEnough_reduce_amount()
								.compareTo(WHETHER_ENOUGH) == 1) {
					cell = row.createCell(++i);
					cell.setCellStyle(style2);
					cell.setCellValue("满减");
				}
				if (whether_combin) {
					cell = row.createCell(++i);
					cell.setCellStyle(style2);
					cell.setCellValue("组合销售");
				}

			}
			// 设置底部统计信息
			int m = datas.size() + 2;
			row = sheet.createRow(m);
			// 设置单元格的样式格式
			int i = 0;
			cell = row.createCell(i);
			cell.setCellStyle(style2);
			cell.setCellValue("总计");

			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue("本次订单金额：");

			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(all_order_price);

			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue("本次商品总金额：");

			cell = row.createCell(++i);
			cell.setCellStyle(style2);
			cell.setCellValue(all_total_amount);*/

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String excel_name = sdf.format(new Date());
			try {
				String path = request.getSession().getServletContext()
						.getRealPath("")
						+ File.separator + "excel";
				response.setContentType("application/x-download");
				response.addHeader("Content-Disposition",
						"attachment;filename=" + excel_name + ".xls");
				OutputStream os = response.getOutputStream();
				wb.write(os);
				os.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private TransInfo query_ship_getData(String id) {
		TransInfo info = new TransInfo();
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		try {
			ExpressCompany ec = this.queryExpressCompany(obj.getExpress_info());
			URL url = new URL("http://api.kuaidi100.com/api?id="
					+ this.configService.getSysConfig().getKuaidi_id()
					+ "&com=" + (ec != null ? ec.getCompany_mark() : "")
					+ "&nu=" + obj.getShipCode() + "&show=0&muti=1&order=asc");
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			InputStream urlStream = url.openStream();
			String type = con.guessContentTypeFromStream(urlStream);
			String charSet = null;
			if (type == null)
				type = con.getContentType();
			if (type == null || type.trim().length() == 0
					|| type.trim().indexOf("text/html") < 0)
				return info;
			if (type.indexOf("charset=") > 0)
				charSet = type.substring(type.indexOf("charset=") + 8);
			byte b[] = new byte[10000];
			int numRead = urlStream.read(b);
			String content = new String(b, 0, numRead, charSet);
			while (numRead != -1) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					// String newContent = new String(b, 0, numRead);
					String newContent = new String(b, 0, numRead, charSet);
					content += newContent;
				}
			}
			info = Json.fromJson(TransInfo.class, content);
			urlStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}

	private ExpressCompany queryExpressCompany(String json) {
		ExpressCompany ec = null;
		if (json != null && !json.equals("")) {
			HashMap map = Json.fromJson(HashMap.class, json);
			ec = this.expressCompanyService.getObjById(CommUtil.null2Long(map
					.get("express_company_id")));
		}
		return ec;
	}
	
	/**
	 * 企业版 快递100 接口
	 * @param id
	 * @param com
	 * @param nu
	 * @return
	 */
	private TransInfo query_ship_getData(String com,String nu) {
		TransInfo info = new TransInfo();

		String content = "";
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("com", com);
			map.put("num", nu);
			String param = JSONObject.fromObject(map).toString();
			String customer ="EF91A38461385824F6FB14D0C594E54E";
			String key = "CNbeXYJm2595";
			String sign =MD5.encode(param+key+customer);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("param",param);
			params.put("sign",sign);
			params.put("customer",customer);
			try {
				content = new HttpRequest().postData("http://poll.kuaidi100.com/poll/query.do", params, "utf-8").toString();
				System.out.println(content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			info = Json.fromJson(TransInfo.class, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}
}
