package com.metoo.module.weixin.manage.buyer.action;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
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

import com.metoo.core.annotation.SecurityMapping;
import com.metoo.core.domain.virtual.SysMap;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.Md5Encrypt;
import com.metoo.foundation.domain.ExpressCompany;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GroupInfo;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.ReturnGoodsLog;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.query.GroupInfoQueryObject;
import com.metoo.foundation.domain.query.OrderFormQueryObject;
import com.metoo.foundation.domain.query.ReturnGoodsLogQueryObject;
import com.metoo.foundation.domain.virtual.TransContent;
import com.metoo.foundation.domain.virtual.TransInfo;
import com.metoo.foundation.service.IAccessoryService;
import com.metoo.foundation.service.IAlbumService;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IGoodsCartService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsReturnService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IGroupInfoService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IPredepositLogService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.domain.ExpressInfo;
import com.metoo.kuaidi100.pojo.TaskRequest;
import com.metoo.kuaidi100.pojo.TaskResponse;
import com.metoo.kuaidi100.post.HttpRequest;
import com.metoo.kuaidi100.post.JacksonHelper;
import com.metoo.kuaidi100.service.IExpressInfoService;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.buyer.tools.ShipTools;
import com.metoo.msg.MsgTools;
import com.metoo.view.web.tools.GoodsViewTools;

/**
 * 
 * 
 * 
 * 
 * <p>
 * Title: WapUserOrderReturnAction.java
 * </p>
 * 
 * <p>
 * Description: wap端退货退款管理
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
 * @author zw
 * 
 * @date 2015年1月23日
 * 
 * @version b2b2c_2015
 */
@Controller
public class WeixinUserOrderReturnAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IGoodsCartService goodsCartService;
	@Autowired
	private IGroupInfoService groupinfoService;
	@Autowired
	private IGoodsReturnService goodsReturnService;
	@Autowired
	private IExpressCompanyService expressCompayService;
	@Autowired
	private IGroupGoodsService ggService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IPayoffLogService payoffLogservice;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IReturnGoodsLogService returnGoodsLogService;
	@Autowired
	private ShipTools shipTools;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IAlbumService albumService;
	@Autowired
	private IAccessoryService accessoryService;
	@Autowired
	private IExpressInfoService expressInfoService;
	@Autowired
	private GoodsViewTools goodsViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;

	@SecurityMapping(title = "买家退货申请", value = "/wap/buyer/order_return_apply.htm*", rtype = "buyer", rname = "用户中心退货管理", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_apply.htm")
	public ModelAndView order_return_apply(HttpServletRequest request,
			HttpServletResponse response, String id, String oid, String view,
			String goods_gsp_ids) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_return_apply.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService.getObjById(CommUtil
				.null2Long(oid));
		if (obj.getUser_id().equals(
				SecurityUserHolder.getCurrentUser().getId().toString())) {
			List<Map> maps = this.orderFormTools.queryGoodsInfo(obj
					.getGoods_info());
			Goods goods = this.goodsService.getObjById(CommUtil.null2Long(id));
			for (Map m : maps) {
				if (CommUtil.null2String(m.get("goods_id")).equals(id)) {
					mv.addObject("return_count", m.get("return_goods_count"));
					mv.addObject("oid", oid);
					mv.addObject("goods", goods);
					if (CommUtil.null2String(m.get("goods_return_status"))
							.equals("5")) {
						mv.addObject("view", true);
						List<Map> return_maps = this.orderFormTools
								.queryGoodsInfo(obj.getReturn_goods_info());
						for (Map map : return_maps) {
							if (CommUtil
									.null2String(map.get("return_goods_id"))
									.equals(id)) {
								mv.addObject("return_content",
										map.get("return_goods_content"));
							}
						}
					}
				}
			}
		}
		mv.addObject("goods_gsp_ids", goods_gsp_ids);
		return mv;
	}

	@SecurityMapping(title = "买家退货申请保存", value = "/wap/buyer/order_return_apply_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_apply_save.htm")
	public String order_return_apply_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String return_goods_content, String goods_id,
			String return_goods_count, String goods_gsp_ids) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		List<Goods> goods_list = this.orderFormTools.queryOfGoods(obj.getId()
				.toString());
		Goods goods = null;
		for (Goods g : goods_list) {
			if (g.getId().toString().equals(goods_id)) {
				goods = g;
			}
		}
		if (obj != null
				&& obj.getUser_id().equals(
						SecurityUserHolder.getCurrentUser().getId().toString())
				&& goods != null) {
			List<Map> list = new ArrayList<Map>();
			Map json = new HashMap();
			json.put("return_goods_id", goods.getId());
			json.put("return_goods_content",
					CommUtil.filterHTML(return_goods_content));
			json.put("return_goods_count", return_goods_count);
			json.put("return_goods_price", goods.getStore_price());
			json.put("return_goods_commission_rate", goods.getGc()
					.getCommission_rate());
			json.put("return_order_id", id);
			list.add(json);
			obj.setReturn_goods_info(Json.toJson(list, JsonFormat.compact()));
			List<Map> maps = this.orderFormTools.queryGoodsInfo(obj
					.getGoods_info());
			List<Map> new_maps = new ArrayList<Map>();
			Map gls = new HashMap();
			for (Map m : maps) {
				if (m.get("goods_id").toString().equals(goods_id)
						&& goods_gsp_ids.equals(m.get("goods_gsp_ids")
								.toString())) {
					m.put("goods_return_status", 5);
					gls.putAll(m);
				}
				new_maps.add(m);
			}
			obj.setGoods_info(Json.toJson(new_maps));
			this.orderFormService.update(obj);
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			ReturnGoodsLog rlog = new ReturnGoodsLog();
			rlog.setReturn_service_id("re" + user.getId()
					+ CommUtil.formatTime("yyyyMMddHHmmss", new Date()));
			rlog.setUser_name(user.getUserName());
			rlog.setUser_id(user.getId());
			rlog.setReturn_content(CommUtil.filterHTML(return_goods_content));
			rlog.setGoods_all_price(gls.get("goods_all_price").toString());
			rlog.setGoods_count(gls.get("goods_count").toString());
			rlog.setGoods_id(CommUtil.null2Long(gls.get("goods_id").toString()));
			rlog.setGoods_mainphoto_path(gls.get("goods_mainphoto_path")
					.toString());
			rlog.setGoods_commission_rate(BigDecimal.valueOf(CommUtil
					.null2Double(gls.get("goods_commission_rate"))));
			rlog.setGoods_name(gls.get("goods_name").toString());
			rlog.setGoods_price(gls.get("goods_price").toString());
			rlog.setGoods_return_status("5");
			rlog.setAddTime(new Date());
			rlog.setReturn_order_id(CommUtil.null2Long(id));
			rlog.setGoods_type(goods.getGoods_type());
			if (goods.getGoods_store() != null) {
				rlog.setStore_id(goods.getGoods_store().getId());
			}
			this.returnGoodsLogService.save(rlog);
			// 如果是收费接口，则通知快递100，建立订单物流查询推送
			if (this.configService.getSysConfig().getKuaidi_type() == 1) {
				TaskRequest req = new TaskRequest();
				Map express_map = Json.fromJson(Map.class,
						obj.getExpress_info());
				req.setCompany(CommUtil.null2String(express_map
						.get("express_company_mark")));
				String from_addr = "";
				req.setFrom(from_addr);
				req.setTo(obj.getReceiver_area());
				req.setNumber(obj.getShipCode());
				req.getParameters().put(
						"callbackurl",
						CommUtil.getURL(request)
								+ "/kuaidi100_callback.htm?order_id="
								+ obj.getId() + "&orderType=1");
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
						System.out.println("订阅成功");
					} else {
						System.out.println("订阅失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (obj.getOrder_form() == 0) {
				User seller = this.userService.getObjById(this.storeService
						.getObjById(CommUtil.null2Long(obj.getStore_id()))
						.getUser().getId());
				Map map = new HashMap();
				map.put("buyer_id", user.getId().toString());
				map.put("seller_id", seller.getId().toString());
				String map_json = Json.toJson(map);
				this.msgTools.sendEmailCharge(CommUtil.getURL(request),
						"email_toseller_order_return_apply_notify",
						seller.getEmail(), map_json, null, obj.getStore_id());
				map.clear();
				map.put("buyer_id", user.getId().toString());
				map.put("seller_id", seller.getId().toString());
				this.msgTools.sendSmsCharge(CommUtil.getURL(request),
						"sms_toseller_order_return_apply_notify",
						seller.getMobile(), map_json, null, obj.getStore_id());
			}
		}
		return "redirect:order_return_list.htm";
	}

	@SecurityMapping(title = "买家退货申请取消", value = "/wap/buyer/order_return_apply_cancel.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_apply_cancel.htm")
	public String order_return_apply_cancel(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String return_goods_content, String goods_id,
			String return_goods_count, String goods_gsp_ids) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		List<Goods> goods_list = this.orderFormTools.queryOfGoods(obj.getId()
				.toString());
		Goods goods = null;
		for (Goods g : goods_list) {
			if (g.getId().toString().equals(goods_id)) {
				goods = g;
			}
		}
		if (obj != null
				&& obj.getUser_id().equals(
						SecurityUserHolder.getCurrentUser().getId().toString())
				&& goods != null) {
			obj.setReturn_goods_info("");
			List<Map> maps = this.orderFormTools.queryGoodsInfo(obj
					.getGoods_info());
			List<Map> new_maps = new ArrayList<Map>();
			Map gls = new HashMap();
			for (Map m : maps) {
				if (m.get("goods_id").toString().equals(goods_id)
						&& goods_gsp_ids.equals(m.get("goods_gsp_ids")
								.toString())) {
					m.put("goods_return_status", "");
					gls.putAll(m);
				}
				new_maps.add(m);
			}
			obj.setGoods_info(Json.toJson(new_maps));
			this.orderFormService.update(obj);
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			Map map = new HashMap();
			map.put("goods_id",
					CommUtil.null2Long(gls.get("goods_id").toString()));
			map.put("return_order_id", CommUtil.null2Long(id));
			map.put("uid", SecurityUserHolder.getCurrentUser().getId());
			List<ReturnGoodsLog> objs = this.returnGoodsLogService
					.query("select obj from ReturnGoodsLog obj where obj.goods_id=:goods_id and obj.return_order_id=:return_order_id and obj.user_id=:uid",
							map, -1, -1);
			for (ReturnGoodsLog rl : objs) {
				this.returnGoodsLogService.delete(rl.getId());
			}
		}
		return "redirect:order_return_list.htm";
	}

	@SecurityMapping(title = "买家退货物流信息", value = "/wap/buyer/order_return_ship.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_ship.htm")
	public ModelAndView order_return_ship(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_return_ship.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null
				&& obj.getUser_id().equals(
						SecurityUserHolder.getCurrentUser().getId().toString())) {
			mv.addObject("obj", obj);
			mv.addObject("currentPage", currentPage);
			// 当前订单中的虚拟商品
			List<Goods> list_goods = this.orderFormTools.queryOfGoods(id);
			List<Goods> deliveryGoods = new ArrayList<Goods>();
			boolean physicalGoods = false;
			for (Goods g : list_goods) {
				if (g.getGoods_choice_type() == 1) {
					deliveryGoods.add(g);
				} else {
					physicalGoods = true;
				}
			}
			Map params = new HashMap();
			params.put("status", 0);
			List<ExpressCompany> expressCompanys = this.expressCompayService
					.query("select obj from ExpressCompany obj where obj.company_status=:status order by company_sequence asc",
							params, -1, -1);
			mv.addObject("expressCompanys", expressCompanys);
			mv.addObject("physicalGoods", physicalGoods);
			mv.addObject("deliveryGoods", deliveryGoods);
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_list.htm");
		}
		return mv;
	}

	@SecurityMapping(title = "买家退货物流信息保存", value = "/wap/buyer/order_return_ship_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_ship_save.htm")
	public ModelAndView order_return_ship_save(HttpServletRequest request,
			HttpServletResponse response, String id, String currentPage,
			String express_id, String express_code) {
		ModelAndView mv = new JModelAndView("wap/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil
				.null2Long(id));
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		if (obj != null && obj.getUser_id().equals(user.getId())) {
			ExpressCompany ec = this.expressCompayService.getObjById(CommUtil
					.null2Long(express_id));
			Map json_map = new HashMap();
			json_map.put("express_company_id", ec.getId());
			json_map.put("express_company_name", ec.getCompany_name());
			json_map.put("express_company_mark", ec.getCompany_mark());
			json_map.put("express_company_type", ec.getCompany_type());
			String express_json = Json.toJson(json_map);
			obj.setReturn_express_info(express_json);
			obj.setExpress_code(express_code);
			obj.setGoods_return_status("7");
			this.returnGoodsLogService.update(obj);
			mv.addObject("op_title", "保存退货物流成功！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_return_listlog.htm");
			OrderForm return_of = this.orderFormService.getObjById(obj
					.getReturn_order_id());
			List<Map> maps = this.orderFormTools.queryGoodsInfo(return_of
					.getGoods_info());
			List<Map> new_maps = new ArrayList<Map>();
			Map gls = new HashMap();
			for (Map m : maps) {
				if (m.get("goods_id").toString()
						.equals(CommUtil.null2String(obj.getGoods_id()))) {
					m.put("goods_return_status", 7);
					gls.putAll(m);
				}
				new_maps.add(m);
			}
			return_of.setGoods_info(Json.toJson(new_maps));
			this.orderFormService.update(return_of);
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您没有为" + obj.getReturn_service_id()
					+ "的退货服务号！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_return_listlog.htm");

		}
		return mv;
	}

	@SecurityMapping(title = "买家退货申请列表", value = "/wap/buyer/order_return_list.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_list.htm")
	public ModelAndView order_return_list(HttpServletRequest request,
			HttpServletResponse response, String id, String view,
			String currentPage, String order_id, String beginTime,
			String endTime) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_return_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (CommUtil.null2Int(currentPage) > 1) {
			mv = new JModelAndView(
					"user/wap/usercenter/order_return_data.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		}
		OrderFormQueryObject ofqo = new OrderFormQueryObject(currentPage, mv,
				"addTime", "desc");
		ofqo.addQuery("obj.user_id", new SysMap("user_id", SecurityUserHolder
				.getCurrentUser().getId().toString()), "=");
		ofqo.addQuery("obj.order_main", new SysMap("order_main", 1), "=");
		if (!CommUtil.null2String(order_id).equals("")) {
			ofqo.addQuery("obj.order_id", new SysMap("order_id", "%" + order_id
					+ "%"), "like");
			mv.addObject("order_id", order_id);
		}
		ofqo.addQuery("obj.order_cat", new SysMap("order_cat", 2), "!=");
		ofqo.addQuery("obj.order_status", new SysMap("order_status", 40), ">=");
		ofqo.addQuery("obj.return_shipTime", new SysMap("return_shipTime",
				new Date()), ">=");
		IPageList pList = this.orderFormService.list(ofqo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "生活购退款列表", value = "/wap/buyer/group_life_return.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/group_life_return.htm")
	public ModelAndView group_life_return(HttpServletRequest request,
			HttpServletResponse response, String id, String view,
			String currentPage, String order_id, String beginTime,
			String endTime) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/group_life_return.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (CommUtil.null2Int(currentPage) > 1) {
			mv = new JModelAndView(
					"user/wap/usercenter/group_life_return.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		}
		GroupInfoQueryObject giqo = new GroupInfoQueryObject(currentPage, mv,
				"addTime", "desc");
		giqo.addQuery("obj.user_id", new SysMap("user_id", SecurityUserHolder
				.getCurrentUser().getId()), "=");// 当前用户
		giqo.addQuery("obj.status", new SysMap("status", 1), "!=");
		giqo.addQuery("obj.status", new SysMap("status", -1), "!=");
		giqo.addQuery("obj.refund_Time", new SysMap("refund_Time", new Date()),
				">=");
		IPageList pList = this.groupinfoService.list(giqo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		return mv;
	}

	@SecurityMapping(title = "生活购退款申请", value = "/wap/buyer/group_life_return_apply.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/group_life_return_apply.htm")
	public ModelAndView group_life_return_apply(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/group_life_return_apply.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		GroupInfo obj = this.groupinfoService
				.getObjById(CommUtil.null2Long(id));
		if (obj.getUser_id()
				.equals(SecurityUserHolder.getCurrentUser().getId())) {
			mv.addObject("obj", obj);
		}
		return mv;
	}

	@SecurityMapping(title = "生活购退款申请取消", value = "/wap/buyer/grouplife_return_apply_cancel.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/grouplife_return_apply_cancel.htm")
	public String grouplife_return_apply_cancel(HttpServletRequest request,
			HttpServletResponse response, String id) {
		GroupInfo obj = this.groupinfoService
				.getObjById(CommUtil.null2Long(id));
		if (obj.getUser_id()
				.equals(SecurityUserHolder.getCurrentUser().getId())) {
			obj.setStatus(0);
			this.groupinfoService.update(obj);
		}
		return "redirect:group_life_return.htm";
	}

	@SecurityMapping(title = "生活购退款申请保存", value = "/wap/buyer/grouplife_return_apply_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/grouplife_return_apply_save.htm")
	public String grouplife_return_apply_save(HttpServletRequest request,
			HttpServletResponse response, String id,
			String return_group_content, String reasion) throws Exception {
		GroupInfo obj = this.groupinfoService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null) {
			if (obj.getUser_id().equals(
					SecurityUserHolder.getCurrentUser().getId())) {// 订单为生活购订单
				String mark = "";
				if (reasion != null && !reasion.equals("")) {
					String rs_ids[] = reasion.split(",");
					for (String rid : rs_ids) {
						if (!rid.equals("")) {
							if (rid.equals("1")) {
								mark = "买错了/重新买";
							} else if (rid.equals("2")) {
								mark = "计划有变，没时间消费";
							} else if (rid.equals("3")) {
								mark = "预约不上";
							} else if (rid.equals("4")) {
								mark = "去过了，不太满意";
							} else if (rid.equals("5")) {
								mark = "其他";
							}
						}
						obj.setStatus(3);// 申请退款中
						obj.setRefund_reasion(mark + "[" + return_group_content
								+ "]");// 退款说明
						this.groupinfoService.update(obj);
						OrderForm order = this.orderFormService.getObjById(obj
								.getOrder_id());
						if (order.getOrder_form() == 0) {
							User seller = this.userService
									.getObjById(this.storeService
											.getObjById(
													CommUtil.null2Long(order
															.getStore_id()))
											.getUser().getId());
							Map map = new HashMap();
							map.put("buyer_id", SecurityUserHolder
									.getCurrentUser().getId().toString());
							map.put("seller_id", seller.getId().toString());
							String map_json = Json.toJson(map);
							this.msgTools.sendEmailCharge(CommUtil.getURL(request),
									"email_toseller_order_refund_apply_notify",
									seller.getEmail(), map_json, null,
									order.getStore_id());
							map.clear();
							map.put("buyer_id", SecurityUserHolder
									.getCurrentUser().getId().toString());
							map.put("seller_id", seller.getId().toString());
							this.msgTools.sendSmsCharge(CommUtil.getURL(request),
									"sms_toseller_order_refund_apply_notify",
									seller.getMobile(), map_json, null,
									order.getStore_id());
						}
					}
				}
			}
		}
		return "redirect:group_life_return.htm";
	}

	@SecurityMapping(title = "买家退货申请列表记录", value = "/wap/buyer/order_return_listlog.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_return_listlog.htm")
	public ModelAndView order_return_listlog(HttpServletRequest request,
			HttpServletResponse response, String currentPage) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_return_listlog.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		if (CommUtil.null2Int(currentPage) > 1) {
			mv = new JModelAndView(
					"user/wap/usercenter/order_return_listlog.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 0, request,
					response);
		}
		ReturnGoodsLogQueryObject qo = new ReturnGoodsLogQueryObject(
				currentPage, mv, "addTime", "desc");
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		qo.addQuery("obj.user_id", new SysMap("user_id", user.getId()), "=");
		qo.addQuery("obj.goods_return_status", new SysMap(
				"goods_return_status", "1"), "!=");
		IPageList pList = this.returnGoodsLogService.list(qo);
		CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "买家退货填写物流", value = "/wap/buyer/order_returnlog_view.htm*", rtype = "buyer", rname = "用户中心", rcode = "wap_user_center", rgroup = "用户中心")
	@RequestMapping("/wap/buyer/order_returnlog_view.htm")
	public ModelAndView order_returnlog_view(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_returnlog_view.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil
				.null2Long(id));
		User user = this.userService.getObjById(SecurityUserHolder
				.getCurrentUser().getId());
		if (obj.getUser_id().equals(user.getId())) {
			if (obj.getGoods_return_status().equals("6")
					|| obj.getGoods_return_status().equals("7")) {
				Map params = new HashMap();
				params.put("status", 0);
				List<ExpressCompany> expressCompanys = this.expressCompayService
						.query("select obj from ExpressCompany obj where obj.company_status=:status order by company_sequence asc",
								params, -1, -1);
				mv.addObject("expressCompanys", expressCompanys);
				mv.addObject("obj", obj);
				mv.addObject("user", user);
				OrderForm of = this.orderFormService.getObjById(obj
						.getReturn_order_id());
				mv.addObject("of", of);
				Goods goods = this.goodsService.getObjById(obj.getGoods_id());
				if (goods.getGoods_type() == 1) {
					mv.addObject("name", goods.getGoods_store().getStore_name());
					mv.addObject("store_id", goods.getGoods_store().getId());
				} else {
					mv.addObject("name", "平台商");
				}
				if (obj.getGoods_return_status().equals("7")) {
					TransInfo transInfo = this
							.query_Returnship_getData(CommUtil.null2String(obj
									.getId()));
					mv.addObject("transInfo", transInfo);
					Map map = Json.fromJson(HashMap.class,
							obj.getReturn_express_info());
					mv.addObject("express_company_name",
							map.get("express_company_name"));
				}

			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "当前状态无法对退货服务单进行操作");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/wap/buyer/order_return_listlog.htm");
			}
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您没有为" + obj.getReturn_service_id()
					+ "的退货服务号！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_return_listlog.htm");
		}
		return mv;
	}

	private TransInfo query_Returnship_getData(String id) {
		TransInfo info = new TransInfo();
		ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil
				.null2Long(id));
		if (this.configService.getSysConfig().getKuaidi_type() == 0) {// 免费物流接口
			try {
				ExpressCompany ec = this.queryExpressCompany(obj
						.getReturn_express_info());
				String query_url = "http://api.kuaidi100.com/api?id="
						+ this.configService.getSysConfig().getKuaidi_id()
						+ "&com=" + (ec != null ? ec.getCompany_mark() : "")
						+ "&nu=" + obj.getExpress_code()
						+ "&show=0&muti=1&order=asc";
				URL url = new URL(query_url);
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
		}
		if (this.configService.getSysConfig().getKuaidi_type() == 1) {// 收费物流接口
			ExpressInfo ei = this.expressInfoService.getObjByPropertyWithType(
					"order_id", obj.getId(), 1);
			if (ei != null) {
				List<TransContent> data = (List<TransContent>) Json.fromJson(ei
						.getOrder_express_info());
				info.setData(data);
				info.setStatus("1");
			}
		}
		return info;
	}

	private TransInfo query_ship_getData(String id) {
		TransInfo info = new TransInfo();
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null && !CommUtil.null2String(obj.getShipCode()).equals("")) {
			if (this.configService.getSysConfig().getKuaidi_type() == 0) {// 免费物流接口
				try {
					ExpressCompany ec = this.queryExpressCompany(obj
							.getExpress_info());
					String query_url = "http://api.kuaidi100.com/api?id="
							+ this.configService.getSysConfig().getKuaidi_id()
							+ "&com="
							+ (ec != null ? ec.getCompany_mark() : "") + "&nu="
							+ obj.getShipCode() + "&show=0&muti=1&order=asc";
					URL url = new URL(query_url);
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
							String newContent = new String(b, 0, numRead,
									charSet);
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
			}
			if (this.configService.getSysConfig().getKuaidi_type() == 1) {// 收费物流接口
				ExpressInfo ei = this.expressInfoService
						.getObjByPropertyWithType("order_id", obj.getId(), 0);
				if (ei != null) {
					List<TransContent> data = (List<TransContent>) Json
							.fromJson(CommUtil.null2String(ei
									.getOrder_express_info()));
					info.setData(data);
					info.setStatus("1");
				}
			}
		}
		return info;
	}

	private ExpressCompany queryExpressCompany(String json) {
		ExpressCompany ec = null;
		if (json != null && !json.equals("")) {
			HashMap map = Json.fromJson(HashMap.class, json);
			ec = this.expressCompayService.getObjById(CommUtil.null2Long(map
					.get("express_company_id")));
		}
		return ec;
	}
}
