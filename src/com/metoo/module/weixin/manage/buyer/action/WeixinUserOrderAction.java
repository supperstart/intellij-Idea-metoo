package com.metoo.module.weixin.manage.buyer.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.service.IQueryService;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.ExpressCompany;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.ReturnGoodsLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.StorePoint;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.virtual.TransInfo;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IExpressCompanyService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IReturnGoodsLogService;
import com.metoo.foundation.service.IStorePointService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.kuaidi100.post.HttpRequest;
import com.metoo.kuaidi100.utils.MD5;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.manage.admin.tools.PaymentTools;
import com.metoo.msg.MsgTools;

import net.sf.json.JSONObject;

/**
 * 
 * 
 * <p>
 * Title:MobileUserOrderAction.java
 * </p>
 * 
 * <p>
 * Description: 手机端买家中心订单控制器
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.metoo.com
 * </p>
 * 
 * @author jy
 * 
 * @date 2014年8月20日
 * 
 * @version metoo_b2b2c_2015
 */
@Controller
public class WeixinUserOrderAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IEvaluateService evaluateService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IStorePointService storePointService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private IExpressCompanyService expressCompayService;
	@Autowired
	private IReturnGoodsLogService returnGoodsLogService;
	@Autowired
	private PaymentTools paymentTools;
	@Autowired
	private IPayoffLogService payoffLogservice;
	@Autowired
	private IGroupGoodsService ggService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IQueryService queryService;

	@SecurityMapping(title = "订单列表", value = "/wap/buyer/order_list.htm*", rtype = "buyer", rname = "移动端用户订单列表", rcode = "wap_order_list", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_list.htm")
	public ModelAndView order_list(HttpServletRequest request,
			HttpServletResponse response, String type, String begin_count,
			String order_status) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<OrderForm> orders = null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("user_id", SecurityUserHolder.getCurrentUser().getId()
				.toString());
		map.put("order_main", 1);
		String sql = "select obj from OrderForm obj where obj.user_id=:user_id and obj.order_main=:order_main and (obj.order_cat!=2 and obj.order_cat!=3)";
		if (CommUtil.null2String(type).equals("order_nopay")) {
			map.put("status", 10);
			sql = sql + " and obj.order_status=:status";
		}
		if (CommUtil.null2String(type).equals("order_noship")) {
			map.put("status", 20);
			sql = sql + " and obj.order_status=:status";
		}
		if (CommUtil.null2String(type).equals("order_notake")) {
			map.put("status1", 30);
			map.put("status2", 35);
			sql = sql
					+ " and (obj.order_status=:status1 or obj.order_status=:status2)";
		}
		if (CommUtil.null2String(type).equals("order_over")) {
			map.put("status", 40);
			sql = sql + " and obj.order_status>=:status";
		}
		sql = sql + " order by obj.addTime desc";
		orders = this.orderFormService.query(sql, map, -1, 12);
		mv.addObject("orders", orders);
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("order_status", order_status);
		mv.addObject("type", type);
		return mv;
	}

	@SecurityMapping(title = "订单列表", value = "/wap/buyer/order_data.htm*", rtype = "buyer", rname = "移动端用户订单列表", rcode = "wap_order_list", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_data.htm")
	public ModelAndView order_data(HttpServletRequest request,
			HttpServletResponse response, String type, String begin_count,
			String order_status) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_data.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<OrderForm> orders = null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("user_id", SecurityUserHolder.getCurrentUser().getId()
				.toString());
		map.put("order_main", 1);
		String sql = "select obj from OrderForm obj where obj.user_id=:user_id and obj.order_main=:order_main and obj.order_cat!=2";
		if (CommUtil.null2String(type).equals("order_nopay")) {
			map.put("status", 10);
			sql = sql + " and obj.order_status=:status";
		}
		if (CommUtil.null2String(type).equals("order_noship")) {
			map.put("status", 20);
			sql = sql + " and obj.order_status=:status";
		}
		if (CommUtil.null2String(type).equals("order_notake")) {
			map.put("status1", 30);
			map.put("status2", 35);
			sql = sql
					+ " and (obj.order_status=:status1 or obj.order_status=:status2)";
		}
		if (CommUtil.null2String(type).equals("order_over")) {
			map.put("status", 40);
			sql = sql + " and obj.order_status>=:status";
		}
		sql = sql + " order by obj.addTime desc";
		orders = this.orderFormService.query(sql, map,
				CommUtil.null2Int(begin_count), 12);
		mv.addObject("orders", orders);
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("order_status", order_status);
		mv.addObject("type", type);
		return mv;
	}

	@SecurityMapping(title = "订单详情", value = "/wap/buyer/order_view.htm*", rtype = "buyer", rname = "移动端用户订单详情", rcode = "wap_order_detail", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_view.htm")
	public ModelAndView order_detail(HttpServletRequest request,
			HttpServletResponse response, String id, String type) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_detail.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null
				&& obj.getUser_id()
						.toString()
						.compareTo(
								SecurityUserHolder.getCurrentUser().getId()
										.toString()) == 0) {
			int orderCat = obj.getOrder_cat();
			
			if (orderCat == 1) {// 手机充值订单
				mv = new JModelAndView(
						"user/wap/usercenter/recharge_order_detail.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request,
						response);
			}
			
			if (orderCat == 3) { //团购订单，取参团信息
				
				
				mv = new JModelAndView(
						"user/wap/usercenter/order_detail_group.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 0, request, response);
				
				String sql = "SELECT a.is_group_creator, DATE_FORMAT(a.create_time, '%Y-%m-%d %H:%i:%s'), a.joiner_count, c.trueName, c.headImgUrl, d.gg_group_count, (d.gg_group_count-a.joiner_count), a.status "+
				 "from metoo_group_joiner a, (SELECT  t.child_group_id from metoo_group_joiner t, metoo_orderform t1 where "+
				"t.rela_order_form_id=t1.id and t.rela_order_form_id=:orderId) b, metoo_user c, metoo_group_goods d "+
				"where a.child_group_id=b.child_group_id "+
				"and a.user_id=c.id "+
				"and a.rela_group_goods_id=d.id "
				//"and a.status=1"
				;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("orderId", obj.getId());
				List list = this.queryService.nativeQuery(sql, map, -1, -1);
				List<Map<String, Object>> nList = new ArrayList<Map<String,Object>>();
				String groupBTime = "";
				if (null != list && list.size() > 0) {
					
					for (Object o : list) {
						
						Object[] oa = (Object[]) o;
						map = new HashMap<String, Object>();
						map.put("isCreator", oa[0]);
						if (((String)oa[0]).equals("1")) {
							
							groupBTime = (String)oa[1];
						}
						map.put("createTime", oa[1]);
						map.put("joinerCount", oa[2]);
						map.put("userName", oa[3]);
						map.put("imgUrl", oa[4]);
						map.put("groupCount", oa[5]);
						map.put("remainGCount", oa[6]);
						map.put("gStatus", oa[7]);
						nList.add(map);
					}
				}
				mv.addObject("groupBTime", groupBTime);
				mv.addObject("gList", nList);
			}
			
			boolean query_ship = false;// 是否查询物流
			if (!CommUtil.null2String(obj.getShipCode()).equals("")) {
				query_ship = true;
			}
			mv.addObject("obj", obj);
			mv.addObject("orderFormTools", orderFormTools);
			mv.addObject("query_ship", query_ship);
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "订单编号错误");
			mv.addObject("url", CommUtil.getURL(request) + "/mobile/index.htm");
		}
		if (!CommUtil.null2String(type).equals("") && !type.equals("0")) {
			mv.addObject("type", type);
		}
		return mv;
	}

	@SecurityMapping(title = "订单取消", value = "/wap/buyer/order_cancel.htm*", rtype = "buyer", rname = "移动端用户中心", rcode = "wap_order_cancel", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/order_cancel.htm")
	public String order_cancel(HttpServletRequest request,
			HttpServletResponse response, String id) {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null
				&& obj.getUser_id().compareTo(
						SecurityUserHolder.getCurrentUser().getId().toString()) == 0
				&& obj.getOrder_status() == 10) {
			obj.setOrder_status(0);
			this.orderFormService.save(obj);
			
			User user = this.userService.getObjById(SecurityUserHolder
					.getCurrentUser().getId());
			//订单中的积分退回
			if(obj.getOrder_integral().intValue() > 0){
				user.setIntegral(user.getIntegral() + obj.getOrder_integral().intValue()) ;
				this.userService.save(user);
				
				IntegralLog log = new IntegralLog();
				log.setAddTime(new Date());
				log.setContent("取消订单回退"
						+ obj.getOrder_integral().intValue() + "分");
				log.setIntegral(obj.getOrder_integral().intValue());
				log.setIntegral_user(user);
				log.setType("integral_rollback");
				this.integralLogService.save(log);
			}
		}
		return "redirect:order_view.htm?id=" + id;
	}

	@SecurityMapping(title = "买家已经购买商品付款", value = "/wap/buyer/go_pay.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "移动端用户中心")
	@RequestMapping("/wap/buyer/go_pay.htm")
	public ModelAndView go_pay(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String id) {
		ModelAndView mv = new JModelAndView("wap/goods_cart3.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		OrderForm order = this.orderFormService.getObjById(CommUtil
				.null2Long(id));
		if (order.getUser_id().toString()
				.equals(SecurityUserHolder.getCurrentUser().getId().toString())) {
			double all_of_price = this.orderFormTools.query_order_price(id);
			mv.addObject("order", order);
			mv.addObject("all_of_price", all_of_price);
			mv.addObject("paymentTools", paymentTools);
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			if (order.getOrder_cat() == 2) {
				mv.addObject("op_title", "团购订单编号错误");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/wap/buyer/group_list.htm");
			} else {
				mv.addObject("op_title", "订单编号错误");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/wap/buyer/order_list.htm");
			}
		}
		return mv;
	}

	/**
	 * 买家确认收货，确认收货后，订单状态值改变为40，如果是预存款支付，买家冻结预存款中同等订单账户金额自动转入商家预存款，如果开启预存款分润，
	 * 则按照分润比例，买家预存款分别进入商家及平台商的账户
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @param currentPage
	 * @return
	 * @throws Exception
	 */
	@SecurityMapping(title = "确认收货", value = "/wap/buyer/order_cofirm.htm*", rtype = "buyer", rname = "移动端用户订单确认收货", rcode = "wap_order_cofirm", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_cofirm.htm")
	public ModelAndView order_cofirm(HttpServletRequest request,
			HttpServletResponse response, String id) throws Exception {
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		ModelAndView mv = new JModelAndView("wap/success.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		if (obj != null
				&& obj.getUser_id()
						.toString()
						.compareTo(
								SecurityUserHolder.getCurrentUser().getId()
										.toString()) == 0) {
			obj.setOrder_status(40);
			Calendar ca = Calendar.getInstance();
			ca.add(ca.DATE, this.configService.getSysConfig()
					.getAuto_order_return());
			SimpleDateFormat bartDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String latertime = bartDateFormat.format(ca.getTime());
			obj.setReturn_shipTime(CommUtil.formatDate(latertime));
			obj.setConfirmTime(new Date());// 设置确认收货时间
			boolean ret = this.orderFormService.update(obj);
			if (obj.getPayment().getMark().equals("payafter")) {// 如果买家支付方式为货到付款，买家确认收货时更新商品库存
				this.update_goods_inventory(obj);// 更新商品库存
			}
			if (ret) {// 订单状态更新成功，更新相关信息
				if (obj.getOrder_main() == 1
						&& !CommUtil.null2String(obj.getChild_order_detail())
								.equals("")) {// 更新子订单状态信息
					List<Map> maps = this.orderFormTools.queryGoodsInfo(obj
							.getChild_order_detail());
					for (Map map : maps) {
						OrderForm child_order = this.orderFormService
								.getObjById(CommUtil.null2Long(map
										.get("order_id")));
						child_order.setOrder_status(40);
						child_order.setReturn_shipTime(CommUtil
								.formatDate(latertime));
						child_order.setConfirmTime(new Date());// 设置确认收货时间
						this.orderFormService.update(child_order);
						if (obj.getPayment().getMark().equals("payafter")) {// 如果买家支付方式为货到付款，买家确认收货，子订单商品销量增加
							List<Map> goods_map = this.orderFormTools
									.queryGoodsInfo(child_order.getGoods_info());
							for (Map child_map : goods_map) {
								Goods goods = this.goodsService
										.getObjById(CommUtil
												.null2Long(child_map
														.get("goods_id")));
								goods.setGoods_salenum(goods.getGoods_salenum()
										+ CommUtil.null2Int(child_map
												.get("goods_count")));// 增加商品销量
								goods.setGoods_inventory(goods
										.getGoods_inventory()
										- CommUtil.null2Int(child_map
												.get("goods_count")));// 库存减少
								if (goods.getGroup_buy() == 2) {// 如果为团购商品，增加团购销量,减少团购库存
									for (GroupGoods gg : goods
											.getGroup_goods_list()) {
										if (gg.getGroup()
												.getId()
												.equals(goods.getGroup()
														.getId())) {
											gg.setGg_selled_count(CommUtil.null2Int(gg
													.getGg_selled_count()
													+ CommUtil.null2Int(map
															.get("goods_count"))));// 增加团购销量
											if (gg.getGg_count()// 减少团购库存
													- CommUtil
															.null2Int(map
																	.get("goods_count")) > 0) {
												gg.setGg_count(gg.getGg_count()
														- CommUtil.null2Int(map
																.get("goods_count")));
											} else {
												gg.setGg_count(0);
											}
											this.ggService.update(gg);
										}
									}
								}
								this.goodsService.update(goods);
							}
						}
						// 向子订单商家发送提醒信息，同时生成结算日志，如果子订单为平台自营，则不发送短信和邮件,
						if (child_order.getOrder_form() == 0) {
							Store store = this.storeService.getObjById(CommUtil
									.null2Long(child_order.getStore_id()));
							Map<String,Object> json_map = new HashMap<String,Object>();
							json_map.put("seller_id", store.getUser().getId()
									.toString());
							json_map.put("childorder_id", child_order.getId()
									.toString());
							String json = Json.toJson(json_map);
							if (obj.getOrder_form() == 0) {
								this.msgTools
										.sendEmailCharge(
												CommUtil.getURL(request),
												"email_toseller_order_receive_ok_notify",
												store.getUser().getEmail(),
												json, null, obj.getStore_id());
								this.msgTools.sendSmsCharge(
										CommUtil.getURL(request),
										"sms_toseller_order_receive_ok_notify",
										store.getUser().getEmail(), json, null,
										obj.getStore_id());
							}
							// 订单生成商家结算日志
							PayoffLog plog = new PayoffLog();
							plog.setPl_sn("pl"
									+ CommUtil.formatTime("yyyyMMddHHmmss",
											new Date())
									+ store.getUser().getId());
							plog.setPl_info("确认收货");
							plog.setAddTime(new Date());
							plog.setSeller(store.getUser());
							plog.setO_id(CommUtil.null2String(child_order
									.getId()));
							plog.setOrder_id(child_order.getOrder_id()
									.toString());
							plog.setCommission_amount(child_order
									.getCommission_amount());// 该订单总佣金费用
							plog.setGoods_info(child_order.getGoods_info());
							plog.setOrder_total_price(child_order
									.getGoods_amount());// 该订单总商品金额
							plog.setTotal_amount(BigDecimal.valueOf(CommUtil
									.subtract(child_order.getGoods_amount(),
											child_order.getCommission_amount())));// 该订单应结算金额：结算金额=订单总商品金额-总佣金费用
							this.payoffLogservice.save(plog);
							store.setStore_sale_amount(BigDecimal
									.valueOf(CommUtil.add(
											child_order.getGoods_amount(),
											store.getStore_sale_amount())));// 店铺本次结算总销售金额
							store.setStore_commission_amount(BigDecimal.valueOf(CommUtil
									.add(child_order.getCommission_amount(),
											store.getStore_commission_amount())));// 店铺本次结算总佣金
							store.setStore_payoff_amount(BigDecimal
									.valueOf(CommUtil.add(
											plog.getTotal_amount(),
											store.getStore_payoff_amount())));// 店铺本次结算总佣金
							this.storeService.update(store);
							// 增加系统总销售金额、总佣金
							SysConfig sc = this.configService.getSysConfig();
							sc.setPayoff_all_sale(BigDecimal.valueOf(CommUtil
									.add(child_order.getGoods_amount(),
											sc.getPayoff_all_sale())));
							sc.setPayoff_all_commission(BigDecimal
									.valueOf(CommUtil.add(
											child_order.getCommission_amount(),
											sc.getPayoff_all_commission())));
							this.configService.update(sc);
						}
					}
				}
				OrderFormLog ofl = new OrderFormLog();
				ofl.setAddTime(new Date());
				ofl.setLog_info("确认收货");
				ofl.setLog_user(SecurityUserHolder.getCurrentUser());
				ofl.setOf(obj);
				this.orderFormLogService.save(ofl);
				// 主订单生成商家结算日志
				if (obj.getOrder_form() == 0) {
					Store store = this.storeService.getObjById(CommUtil
							.null2Long(obj.getStore_id()));
					PayoffLog plog = new PayoffLog();
					plog.setPl_sn("pl"
							+ CommUtil.formatTime("yyyyMMddHHmmss", new Date())
							+ store.getUser().getId());
					plog.setPl_info("确认收货");
					plog.setAddTime(new Date());
					plog.setSeller(store.getUser());
					plog.setO_id(CommUtil.null2String(obj.getId()));
					plog.setOrder_id(obj.getOrder_id().toString());
					plog.setCommission_amount(obj.getCommission_amount());// 该订单总佣金费用
					plog.setGoods_info(obj.getGoods_info());
					plog.setOrder_total_price(obj.getGoods_amount());// 该订单总商品金额
					plog.setTotal_amount(BigDecimal.valueOf(CommUtil.subtract(
							obj.getGoods_amount(), obj.getCommission_amount())));// 该订单应结算金额：结算金额=订单总商品金额-总佣金费用
					this.payoffLogservice.save(plog);
					store.setStore_sale_amount(BigDecimal.valueOf(CommUtil.add(
							obj.getGoods_amount(), store.getStore_sale_amount())));// 店铺本次结算总销售金额
					store.setStore_commission_amount(BigDecimal
							.valueOf(CommUtil.add(obj.getCommission_amount(),
									store.getStore_commission_amount())));// 店铺本次结算总佣金
					store.setStore_payoff_amount(BigDecimal.valueOf(CommUtil
							.add(plog.getTotal_amount(),
									store.getStore_payoff_amount())));// 店铺本次结算总佣金
					this.storeService.update(store);
					// 增加系统总销售金额、总佣金
					SysConfig sc = this.configService.getSysConfig();
					sc.setPayoff_all_sale(BigDecimal.valueOf(CommUtil.add(
							obj.getGoods_amount(), sc.getPayoff_all_sale())));
					sc.setPayoff_all_commission(BigDecimal.valueOf(CommUtil
							.add(obj.getCommission_amount(),
									sc.getPayoff_all_commission())));
					this.configService.update(sc);
				}
			}
			mv.addObject("op_title", "确认成功");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_list.htm");
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "订单编号错误");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_list.htm");
		}
		return mv;
	}

	/**
	 * 订单评论
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@SecurityMapping(title = "订单评论", value = "/wap/buyer/order_discuss.htm*", rtype = "buyer", rname = "移动端用户订单评论", rcode = "wap_order_cofirm", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_discuss.htm")
	public ModelAndView order_discuss(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_discuss.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null
				&& obj.getUser_id()
						.toString()
						.compareTo(
								SecurityUserHolder.getCurrentUser().getId()
										.toString()) == 0) {
			mv.addObject("obj", obj);
			mv.addObject("orderFormTools", orderFormTools);
			String evaluate_session = CommUtil.randomString(32);
			request.getSession(false).setAttribute("evaluate_session",
					evaluate_session);
			mv.addObject("evaluate_session", evaluate_session);
			if (obj.getOrder_status() >= 50) {
				mv = new JModelAndView("wap/success.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "订单已经评价！");
				mv.addObject("url", CommUtil.getURL(request)
						+ "/user/wap/usercenter/order_list.htm");
			}
		} else {
			mv = new JModelAndView("wap/error.html", configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您没有编号为" + id + "的订单！");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
		}
		mv.addObject("orderFormTools", orderFormTools);
		return mv;
	}

	@SecurityMapping(title = "买家评价保存", value = "/wap/buyer/order_discuss_save.htm*", rtype = "buyer", rname = "移动端用户订单评论", rcode = "user_center", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_discuss_save.htm")
	public ModelAndView order_discuss_save(HttpServletRequest request,
			HttpServletResponse response, String evaluate_session)
			throws Exception {
		String[] ids = request.getParameterValues("id");
		String evaluate_session1 = (String) request.getSession(false)
				.getAttribute("evaluate_session");
		if (CommUtil.null2String(evaluate_session1).equals(evaluate_session)) {
			for (String id : ids) {
				OrderForm obj = this.orderFormService.getObjById(CommUtil
						.null2Long(id));
				if (obj != null
						&& obj.getUser_id()
								.toString()
								.compareTo(
										SecurityUserHolder.getCurrentUser()
												.getId().toString()) == 0) {
					if (obj.getOrder_status() == 40) {
						obj.setOrder_status(50);
						this.orderFormService.update(obj);
						OrderFormLog ofl = new OrderFormLog();
						ofl.setAddTime(new Date());
						ofl.setLog_info("评价订单");
						ofl.setLog_user(SecurityUserHolder.getCurrentUser());
						ofl.setOf(obj);
						this.orderFormLogService.save(ofl);
						List<Map> json = this.orderFormTools.queryGoodsInfo(obj
								.getGoods_info());
						for (Map map : json) {
							map.put("orderForm", obj.getId());
						}
						for (Map map : json) {
							Evaluate eva = new Evaluate();
							Goods goods = this.goodsService.getObjById(CommUtil
									.null2Long(map.get("goods_id")));
							eva.setAddTime(new Date());
							eva.setEvaluate_goods(goods);
							eva.setGoods_num(CommUtil.null2Int(map
									.get("goods_count")));
							eva.setGoods_price(map.get("goods_price")
									.toString());
							eva.setGoods_spec(map.get("goods_gsp_val")
									.toString());
							eva.setEvaluate_info(request
									.getParameter("evaluate_info_"
											+ goods.getId()));
							eva.setEvaluate_buyer_val(CommUtil.null2Int(request
									.getParameter("evaluate_buyer_val"
											+ goods.getId())));
							eva.setDescription_evaluate(BigDecimal.valueOf(CommUtil.null2Double(request
									.getParameter("description_evaluate"
											+ goods.getId()))));
							eva.setService_evaluate(BigDecimal.valueOf(CommUtil
									.null2Double(request
											.getParameter("service_evaluate"
													+ goods.getId()))));
							eva.setShip_evaluate(BigDecimal.valueOf(CommUtil
									.null2Double(request
											.getParameter("ship_evaluate"
													+ goods.getId()))));
							eva.setEvaluate_type("goods");
							eva.setEvaluate_user(SecurityUserHolder
									.getCurrentUser());
							eva.setOf(this.orderFormService.getObjById(CommUtil
									.null2Long(map.get("orderForm"))));
							this.evaluateService.save(eva);
							Map<String,Object> params = new HashMap<String,Object>();
							User sp_user = this.userService.getObjById(obj
									.getEva_user_id());
							params.put("user_id", SecurityUserHolder
									.getCurrentUser().getId().toString());
							List<Evaluate> evas = this.evaluateService
									.query("select obj from Evaluate obj where obj.of.user_id=:user_id",
											params, -1, -1);
							double user_evaluate1 = 0;
							double user_evaluate1_total = 0;
							double description_evaluate = 0;
							double description_evaluate_total = 0;
							double service_evaluate = 0;
							double service_evaluate_total = 0;
							double ship_evaluate = 0;
							double ship_evaluate_total = 0;
							DecimalFormat df = new DecimalFormat("0.0");
							for (Evaluate eva1 : evas) {
								user_evaluate1_total = user_evaluate1_total
										+ eva1.getEvaluate_buyer_val();
								description_evaluate_total = description_evaluate_total
										+ CommUtil.null2Double(eva1
												.getDescription_evaluate());
								service_evaluate_total = service_evaluate_total
										+ CommUtil.null2Double(eva1
												.getService_evaluate());
								ship_evaluate_total = ship_evaluate_total
										+ CommUtil.null2Double(eva1
												.getShip_evaluate());
							}
							user_evaluate1 = CommUtil
									.null2Double(df.format(user_evaluate1_total
											/ evas.size()));
							description_evaluate = CommUtil.null2Double(df
									.format(description_evaluate_total
											/ evas.size()));
							service_evaluate = CommUtil.null2Double(df
									.format(service_evaluate_total
											/ evas.size()));
							ship_evaluate = CommUtil.null2Double(df
									.format(ship_evaluate_total / evas.size()));
							params.clear();
							params.put("user_id", obj.getEva_user_id());
							List<StorePoint> sps = this.storePointService
									.query("select obj from StorePoint obj where obj.user.id=:user_id",
											params, -1, -1);
							StorePoint point = null;
							if (sps.size() > 0) {
								point = sps.get(0);
							} else {
								point = new StorePoint();
							}
							point.setAddTime(new Date());
							point.setUser(sp_user);
							point.setDescription_evaluate(BigDecimal
									.valueOf(description_evaluate>5?5:description_evaluate));
							point.setService_evaluate(BigDecimal
									.valueOf(service_evaluate>5?5:service_evaluate));
							point.setShip_evaluate(BigDecimal
									.valueOf(ship_evaluate>5?5:ship_evaluate));
							if (sps.size() > 0) {
								this.storePointService.update(point);
							} else {
								this.storePointService.save(point);
							}
							// 增加用户积分和消费金额
							User user = this.userService.getObjById(CommUtil
									.null2Long(obj.getUser_id()));
							user.setIntegral(user.getIntegral()
									+ this.configService.getSysConfig()
											.getIndentComment());
							user.setUser_goods_fee(BigDecimal.valueOf(CommUtil
									.add(user.getUser_goods_fee(),
											obj.getTotalPrice())));
							this.userService.update(user);
						}
					}
					if (obj.getOrder_form() == 0) {
						Store store = this.storeService.getObjById(CommUtil
								.null2Long(obj.getStore_id()));
						Map map = new HashMap();
						map.put("seller_id", store.getUser().getId().toString());
						map.put("order_id", obj.getId().toString());
						String json = Json.toJson(map);
						this.msgTools.sendEmailCharge(CommUtil.getURL(request),
								"email_toseller_evaluate_ok_notify", store
										.getUser().getEmail(), json, null,
								CommUtil.null2String(store.getId()));
					}
				}
			}
			request.getSession(false).removeAttribute("evaluate_session");
			ModelAndView mv = new JModelAndView("wap/success.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "订单评价成功！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_list.htm");
			return mv;
		} else {
			ModelAndView mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "禁止重复评价!");
			mv.addObject("url", CommUtil.getURL(request)
					+ "/wap/buyer/order_list.htm");
			return mv;
		}

	}

	@SecurityMapping(title = "物流信息1", value = "/wap/buyer/ship_detail1.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/ship_detail1.htm")
	public ModelAndView ship_detail1(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String type) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/ship_detail.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<TransInfo> transInfo_list = new ArrayList<TransInfo>();
		Map<String, OrderForm> order_map = new HashMap<String, OrderForm>();
		OrderForm order = this.orderFormService.getObjById(CommUtil
				.null2Long(order_id));
		if (order.getShipCode() != null) {
			order_map.put(order.getShipCode(), order);
		}
		/*TransInfo transInfo = this.query_ship_getData(CommUtil
				.null2String(order_id));*/
		TransInfo transInfo = this.query_ship_getData(this.orderFormTools.queryExInfo(
				order.getExpress_info(), "express_company_mark"),CommUtil.null2String(order.getShipCode()));
		if (transInfo != null) {
			transInfo.setExpress_company_name(this.orderFormTools.queryExInfo(
					order.getExpress_info(), "express_company_name"));
			transInfo.setExpress_ship_code(order.getShipCode());
			transInfo_list.add(transInfo);
		}
		if (order.getOrder_main() == 1
				&& !CommUtil.null2String(order.getChild_order_detail()).equals(
						"")) {// 查询子订单的物流跟踪信息
			List<Map> maps = this.orderFormTools.queryGoodsInfo(order
					.getChild_order_detail());
			for (Map child_map : maps) {
				OrderForm child_order = this.orderFormService
						.getObjById(CommUtil.null2Long(child_map
								.get("order_id")));
				if (child_order.getShipCode() != null) {
					order_map.put(child_order.getShipCode(), child_order);
				}
				/*TransInfo transInfo1 = this.query_ship_getData(CommUtil
						.null2String(child_order.getId()));*/
				TransInfo transInfo1 = this.query_ship_getData(this.orderFormTools.queryExInfo(
						order.getExpress_info(), "express_company_mark"),CommUtil.null2String(order.getShipCode()));
				if (transInfo1 != null) {
					transInfo1.setExpress_company_name(this.orderFormTools
							.queryExInfo(child_order.getExpress_info(),
									"express_company_name"));
					transInfo1.setExpress_ship_code(child_order.getShipCode());
					transInfo_list.add(transInfo1);
				}
			}

		}
		mv.addObject("order_map", order_map);
		mv.addObject("order", order);
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("transInfo_list", transInfo_list);
		if (!CommUtil.null2String(type).equals("") && !type.equals("0")) {
			mv.addObject("type", type);
		}
		return mv;
	}

	@SecurityMapping(title = "物流信息2", value = "/wap/buyer/ship_detail2.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/ship_detail2.htm")
	public ModelAndView ship_detail2(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String type) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/ship_detail.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<TransInfo> transInfo_list = new ArrayList<TransInfo>();
		OrderForm order = this.orderFormService.getObjById(CommUtil
				.null2Long(order_id));
		TransInfo transInfo = this.query_ship_getData(this.orderFormTools.queryExInfo(
				order.getExpress_info(), "express_company_mark"),CommUtil.null2String(order.getShipCode()));
		if (transInfo != null) {
			transInfo.setExpress_company_name(this.orderFormTools.queryExInfo(
					order.getExpress_info(), "express_company_name"));
			transInfo.setExpress_ship_code(order.getShipCode());
			transInfo_list.add(transInfo);
		}
		mv.addObject("transInfo_list", transInfo_list);
		mv.addObject("order", order);
		mv.addObject("orderFormTools", orderFormTools);
		if (!CommUtil.null2String(type).equals("") && !type.equals("0")) {
			mv.addObject("type", type);
		}
		return mv;
	}

	@SecurityMapping(title = "买家物流详情", value = "/user/wap/ship_view.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "移动端用户订单")
	@RequestMapping("/mobile/ship_view.htm")
	public ModelAndView order_ship_view(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/ship_detail.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		OrderForm order = this.orderFormService.getObjById(CommUtil
				.null2Long(id));
		if (order != null && !order.equals("")) {
			if (order.getUser_id().equals(
					SecurityUserHolder.getCurrentUser().getId().toString())) {
				List<TransInfo> transInfo_list = new ArrayList<TransInfo>();
				//TransInfo transInfo = this.query_ship_getData(id);
				TransInfo transInfo = this.query_ship_getData(this.orderFormTools.queryExInfo(
						order.getExpress_info(), "express_company_mark"),CommUtil.null2String(order.getShipCode()));
				transInfo.setExpress_company_name(this.orderFormTools
						.queryExInfo(order.getExpress_info(),
								"express_company_name"));
				transInfo.setExpress_ship_code(order.getShipCode());
				transInfo_list.add(transInfo);
				// if (order.getOrder_main() == 1
				// && !CommUtil.null2String(order.getChild_order_detail())
				// .equals("")) {// 查询子订单的物流跟踪信息
				// List<Map> maps = this.orderFormTools.queryGoodsInfo(order
				// .getChild_order_detail());
				// for (Map child_map : maps) {
				// OrderForm child_order = this.orderFormService
				// .getObjById(CommUtil.null2Long(child_map
				// .get("order_id")));
				// if (child_order.getExpress_info() != null) {
				// TransInfo transInfo1 = this
				// .query_ship_getData(CommUtil
				// .null2String(child_order.getId()));
				// transInfo1
				// .setExpress_company_name(this.orderFormTools
				// .queryExInfo(child_order
				// .getExpress_info(),
				// "express_company_name"));
				// transInfo1.setExpress_ship_code(child_order
				// .getShipCode());
				// transInfo_list.add(transInfo1);
				// }
				// }
				//
				// }
				mv.addObject("transInfo_list", transInfo_list);
				mv.addObject("obj", order);
			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "您查询的物流不存在！");
				mv.addObject("url", CommUtil.getURL(request)
						+ "user/wap/buyer/order.htm");
			}
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "您查询的物流不存在！");
			mv.addObject("url", CommUtil.getURL(request)
					+ "user/wap/buyer/order.htm");
		}
		return mv;
	}

	private void update_goods_inventory(OrderForm order) {
		// 付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
		List<Goods> goods_list = this.orderFormTools.queryOfGoods(CommUtil
				.null2String(order.getId()));
		for (Goods goods : goods_list) {
			int goods_count = this.orderFormTools.queryOfGoodsCount(
					CommUtil.null2String(order.getId()),
					CommUtil.null2String(goods.getId()));
			if (goods.getGroup() != null && goods.getGroup_buy() == 2) {
				for (GroupGoods gg : goods.getGroup_goods_list()) {
					if (gg.getGroup().getId().equals(goods.getGroup().getId())) {
						gg.setGg_count(gg.getGg_count() - goods_count);
						this.groupGoodsService.update(gg);
						// 更新lucene索引
						String goods_lucene_path = System
								.getProperty("user.dir")
								+ File.separator
								+ "luence" + File.separator + "groupgoods";
						File file = new File(goods_lucene_path);
						if (!file.exists()) {
							CommUtil.createFolder(goods_lucene_path);
						}
						LuceneUtil lucene = LuceneUtil.instance();
						lucene.setIndex_path(goods_lucene_path);
						lucene.update(CommUtil.null2String(goods.getId()),
								luceneVoTools.updateGroupGoodsIndex(gg));
					}
				}
			}
			List<String> gsps = new ArrayList<String>();
			List<GoodsSpecProperty> temp_gsp_list = this.orderFormTools
					.queryOfGoodsGsps(CommUtil.null2String(order.getId()),
							CommUtil.null2String(goods.getId()));
			for (GoodsSpecProperty gsp : temp_gsp_list) {
				gsps.add(gsp.getId().toString());
			}
			String[] gsp_list = new String[gsps.size()];
			gsps.toArray(gsp_list);
			goods.setGoods_salenum(goods.getGoods_salenum() + goods_count);
			String inventory_type = goods.getInventory_type() == null ? "all"
					: goods.getInventory_type();
			if (inventory_type.equals("all")) {
				goods.setGoods_inventory(goods.getGoods_inventory()
						- goods_count);
			} else {
				List<HashMap> list = Json
						.fromJson(ArrayList.class, CommUtil.null2String(goods
								.getGoods_inventory_detail()));
				for (Map temp : list) {
					String[] temp_ids = CommUtil.null2String(temp.get("id"))
							.split("_");
					Arrays.sort(temp_ids);
					Arrays.sort(gsp_list);
					if (Arrays.equals(temp_ids, gsp_list)) {
						temp.put("count", CommUtil.null2Int(temp.get("count"))
								- goods_count);
					}
				}
				goods.setGoods_inventory_detail(Json.toJson(list,
						JsonFormat.compact()));
			}
			for (GroupGoods gg : goods.getGroup_goods_list()) {
				if (gg.getGroup().getId().equals(goods.getGroup().getId())
						&& gg.getGg_count() == 0) {
					goods.setGroup_buy(3);// 标识商品的状态为团购数量已经结束
				}
			}
			this.goodsService.update(goods);
			// 更新lucene索引
			String goods_lucene_path = System.getProperty("metoob2b2c.root")
					+ File.separator + "luence" + File.separator + "goods";
			File file = new File(goods_lucene_path);
			if (!file.exists()) {
				CommUtil.createFolder(goods_lucene_path);
			}
			LuceneUtil lucene = LuceneUtil.instance();
			lucene.setIndex_path(goods_lucene_path);
			lucene.update(CommUtil.null2String(goods.getId()),
					luceneVoTools.updateGoodsIndex(goods));
		}
	}

	private TransInfo query_ship_getData(String id) {
		TransInfo info = null;
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null && !CommUtil.null2String(obj.getShipCode()).equals("")) {
			info = new TransInfo();
			try {
				ExpressCompany ec = this.queryExpressCompany(obj
						.getExpress_info());
				String query_url = "http://api.kuaidi100.com/api?id="
						+ this.configService.getSysConfig().getKuaidi_id()
						+ "&com=" + (ec != null ? ec.getCompany_mark() : "")
						+ "&nu=" + obj.getShipCode()
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
				String content = new String(b, 1, numRead, charSet);
				while (numRead != -1) {
					numRead = urlStream.read(b);
					if (numRead != -1) {
						// String newContent = new String(b, 1, numRead);
						String newContent = new String(b, 1, numRead, charSet);
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
		return info;
	}

	private TransInfo query_Returnship_getData(String id) {
		TransInfo info = new TransInfo();
		ReturnGoodsLog obj = this.returnGoodsLogService.getObjById(CommUtil
				.null2Long(id));
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
			String content = new String(b, 1, numRead, charSet);
			while (numRead != -1) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					// String newContent = new String(b, 1, numRead);
					String newContent = new String(b, 1, numRead, charSet);
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
			ec = this.expressCompayService.getObjById(CommUtil.null2Long(map
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
	
	@SecurityMapping(title = "团购订单列表", value = "/wap/buyer/order_group_list.htm*", rtype = "buyer", rname = "移动端用户订单列表", rcode = "wap_order_list", rgroup = "移动端用户订单")
	@RequestMapping("/wap/buyer/order_group_list.htm")
	public ModelAndView order_group_list(HttpServletRequest request,
			HttpServletResponse response, String type, String begin_count,
			String order_status) {
		ModelAndView mv = new JModelAndView(
				"user/wap/usercenter/order_group_list.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 0, request, response);
		List<OrderForm> orders = null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("user_id", SecurityUserHolder.getCurrentUser().getId()
				.toString());
		map.put("order_main", 1);
		String sql = "select obj from OrderForm obj where obj.user_id=:user_id and obj.order_main=:order_main and obj.order_cat=3 and obj.order_status > 10";
//		if (CommUtil.null2String(type).equals("order_nopay")) {
//			map.put("status", 10);
//			sql = sql + " and obj.order_status=:status";
//		}
		if (CommUtil.null2String(type).equals("order_noship")) {
			map.put("status", 20);
			sql = sql + " and (obj.order_status=:status or obj.order_status=66)";
		}
		if (CommUtil.null2String(type).equals("order_notake")) {
			map.put("status1", 30);
			map.put("status2", 35);
			sql = sql
					+ " and (obj.order_status=:status1 or obj.order_status=:status2)";
		}
		if (CommUtil.null2String(type).equals("order_over")) {
			map.put("status", 40);
			sql = sql + " and obj.order_status=:status";
		}
		sql = sql + " order by obj.addTime desc";
		orders = this.orderFormService.query(sql, map, -1, 12);
		mv.addObject("orders", orders);
		mv.addObject("orderFormTools", orderFormTools);
		mv.addObject("order_status", order_status);
		mv.addObject("type", type);
		return mv;
	}
	
}
