package com.metoo.module.weixin.view.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.mv.JModelAndView;
import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.GoldLog;
import com.metoo.foundation.domain.GoldRecord;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.GoodsLog;
import com.metoo.foundation.domain.GoodsSpecProperty;
import com.metoo.foundation.domain.GroupGoods;
import com.metoo.foundation.domain.GroupInfo;
import com.metoo.foundation.domain.GroupLifeGoods;
import com.metoo.foundation.domain.IntegralGoods;
import com.metoo.foundation.domain.IntegralGoodsOrder;
import com.metoo.foundation.domain.Message;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.Payment;
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.domain.Predeposit;
import com.metoo.foundation.domain.PredepositLog;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoldLogService;
import com.metoo.foundation.service.IGoldRecordService;
import com.metoo.foundation.service.IGoodsLogService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IGroupGoodsService;
import com.metoo.foundation.service.IGroupInfoService;
import com.metoo.foundation.service.IGroupLifeGoodsService;
import com.metoo.foundation.service.IIntegralGoodsOrderService;
import com.metoo.foundation.service.IIntegralGoodsService;
import com.metoo.foundation.service.IMessageService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.IPayoffLogService;
import com.metoo.foundation.service.IPredepositLogService;
import com.metoo.foundation.service.IPredepositService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.lucene.LuceneUtil;
import com.metoo.lucene.tools.LuceneVoTools;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.msg.MsgTools;
import com.metoo.msg.email.SpelTemplate;
import com.metoo.pay.alipay.config.AlipayConfig;
import com.metoo.pay.alipay.util.AlipayNotify;
import com.metoo.pay.tenpay.ReponseHandlerForWx;
import com.metoo.pay.tenpay.RequestHandler;
import com.metoo.pay.tenpay.client.TenpayHttpClient;
import com.metoo.pay.tenpay.util.Sha1Util;
import com.metoo.view.web.tools.BuyGiftViewTools;
import com.metoo.view.web.tools.GoodsViewTools;

/**
 * 移动端在线支付毁掉控制器
 * 
 * <p>
 * Title:WapPayViewAction.java
 * </p>
 * 
 * <p>
 * Description:
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
 * @author jinxinzhe
 * 
 * @date 2014年8月20日
 * 
 * @version metoo_b2b2c_2015
 */
@Controller
public class WeixinPayViewAction {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IPredepositService predepositService;
	@Autowired
	private IPredepositLogService predepositLogService;
	@Autowired
	private IGoldRecordService goldRecordService;
	@Autowired
	private IGoldLogService goldLogService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IIntegralGoodsOrderService integralGoodsOrderService;
	@Autowired
	private IIntegralGoodsService integralGoodsService;
	@Autowired
	private IGroupGoodsService groupGoodsService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private MsgTools msgTools;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private IGroupLifeGoodsService groupLifeGoodsService;
	@Autowired
	private IGroupInfoService groupInfoService;
	@Autowired
	private IMessageService messageService;
	@Autowired
	private IPayoffLogService payoffservice;
	@Autowired
	private LuceneVoTools luceneVoTools;
	@Autowired
	private BuyGiftViewTools buyGiftViewTools;
	@Autowired
	private IGoodsLogService goodsLogService;
	@Autowired
	private GoodsViewTools goodsViewTools;

	@RequestMapping("/wap/alipay_return.htm")
	public ModelAndView wap_alipay_return(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mv = new JModelAndView("wap/order_pay_finish.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		String trade_no = request.getParameter("trade_no"); // 支付宝交易号
		String order_nos = request.getParameter("out_trade_no"); // 获取订单号
		String order_no = order_nos.split("-")[2];
		String total_fee = request.getParameter("price"); // 获取总金额
		String subject = request.getParameter("subject");// 交易参数
		String result = request.getParameter("result");// 交易状态
		// System.out.println("支付返回结果是：" + result);
		// String(request.getParameter("subject").getBytes("ISO-8859-1"),
		// "UTF-8");//
		// 商品名称、订单名称
		String type = CommUtil.null2String(request.getParameter("pay_body"))
				.trim();
		if (type.equals("")) {
			type = "goods";
		}
		OrderForm main_order = null;
		Predeposit obj = null;
		GoldRecord gold = null;
		IntegralGoodsOrder ig_order = null;
		if (type.equals("goods")) {
			main_order = this.orderFormService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("cash")) {
			obj = this.predepositService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("gold")) {
			gold = this.goldRecordService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("integral")) {
			ig_order = this.integralGoodsOrderService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("group")) {
			main_order = this.orderFormService.getObjById(CommUtil
					.null2Long(order_no));
		}
		// 获取支付宝GET过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			// 如果没有配置Tomcat的get编码为UTF-8，需要下面一行代码转码，否则会出现乱码，导致回调失败
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		AlipayConfig config = new AlipayConfig();
		if (type.equals("goods") || type.equals("group")) {
			config.setKey(main_order.getPayment().getSafeKey());
			config.setPartner(main_order.getPayment().getPartner());
			config.setSeller_email(main_order.getPayment().getSeller_email());
		}
		if (type.equals("cash") || type.equals("gold")
				|| type.equals("integral")) {
			Map q_params = new HashMap();
			q_params.put("install", true);
			if (type.equals("cash")) {
				q_params.put("mark", obj.getPd_payment());
			}
			if (type.equals("gold")) {
				q_params.put("mark", gold.getGold_payment());
			}
			if (type.equals("integral")) {
				q_params.put("mark", ig_order.getIgo_payment());
			}
			List<Payment> payments = this.paymentService
					.query("select obj from Payment obj where obj.install=:install and obj.mark=:mark",
							q_params, -1, -1);
			config.setKey(payments.get(0).getSafeKey());
			config.setPartner(payments.get(0).getPartner());
			config.setSeller_email(payments.get(0).getSeller_email());
		}
		config.setNotify_url(CommUtil.getURL(request)
				+ "/wap/alipay_notify.htm");
		config.setReturn_url(CommUtil.getURL(request)
				+ "/wap/aplipay_return.htm");
		boolean verify_result = AlipayNotify.verify(config, params);
		if (verify_result) {// 验证成功
			if (type.equals("goods")) {
				User buyer = this.userService.getObjById(CommUtil
						.null2Long(main_order.getUser_id()));
				if (main_order.getOrder_status() < 20) {// 异步没有出来订单，则同步处理订单
					main_order.setOrder_status(20);
					main_order.setOut_order_id(trade_no);
					main_order.setPayTime(new Date());
					this.orderFormService.update(main_order);
					// 主订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
					this.update_goods_inventory(main_order);
					OrderFormLog main_ofl = new OrderFormLog();
					main_ofl.setAddTime(new Date());
					main_ofl.setLog_info("支付宝在线支付");
					main_ofl.setLog_user(buyer);
					main_ofl.setOf(main_order);
					this.orderFormLogService.save(main_ofl);
					// 主订单付款成功，发送邮件提示
					// 向加盟商家发送付款成功邮件提示，自营商品无需发送邮件提示
					this.send_msg_tobuyer(request, main_order);
					this.send_msg_toseller(request, main_order);
					// 子订单操作
					if (main_order.getOrder_main() == 1
							&& !CommUtil.null2String(
									main_order.getChild_order_detail()).equals(
									"")) {// 同步完成子订单付款状态调整
						List<Map> maps = this.orderFormTools
								.queryGoodsInfo(main_order
										.getChild_order_detail());
						for (Map child_map : maps) {
							OrderForm child_order = this.orderFormService
									.getObjById(CommUtil.null2Long(child_map
											.get("order_id")));
							if (child_order.getOrder_status() != 20) {
								child_order.setOrder_status(20);
								child_order.setPayTime(new Date());
								this.orderFormService.update(child_order);
								// 子订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
								this.update_goods_inventory(child_order);
								OrderFormLog child_ofl = new OrderFormLog();
								child_ofl.setAddTime(new Date());
								child_ofl.setLog_info("支付宝在线支付");
								child_ofl.setLog_user(buyer);
								child_ofl.setOf(child_order);
								this.orderFormLogService.save(child_ofl);
								// 向加盟商家发送付款成功短信提示，自营商品无需发送短信提示
								// 付款成功，发送邮件提示
								this.send_msg_toseller(request, child_order);
							}
						}
					}
				}
				mv.addObject("all_price", this.orderFormTools
						.query_order_price(CommUtil.null2String(main_order
								.getId())));
				mv.addObject("obj", main_order);
			}
			if (type.equals("group")) {
				if (main_order.getOrder_status() != 20) {// 异步没有出来订单，则同步处理订单
					this.generate_groupInfos(request, main_order, "alipay",
							"支付宝在线支付", trade_no);
				}
				mv.addObject("all_price", main_order.getTotalPrice());
				mv.addObject("obj", main_order);
			}
			if (type.equals("integral")) {
				if (ig_order.getIgo_status() < 20) {
					ig_order.setIgo_status(20);
					ig_order.setIgo_pay_time(new Date());
					ig_order.setIgo_payment("alipay");
					this.integralGoodsOrderService.update(ig_order);
					List<Map> ig_maps = this.orderFormTools
							.query_integral_goodsinfo(ig_order.getGoods_info());
					for (Map map : ig_maps) {
						IntegralGoods goods = this.integralGoodsService
								.getObjById(CommUtil.null2Long(map.get("id")));
						goods.setIg_goods_count(goods.getIg_goods_count()
								- CommUtil.null2Int(map.get("ig_goods_count")));
						goods.setIg_exchange_count(goods.getIg_exchange_count()
								+ CommUtil.null2Int(map.get("ig_goods_count")));
						this.integralGoodsService.update(goods);
					}
				}
				mv = new JModelAndView("wap/integral_order_finish.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("obj", ig_order);
			}
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "支付回调失败");
			mv.addObject("url", CommUtil.getURL(request) + "wap/index.htm");
		}
		return mv;
	}

	@RequestMapping("/wap/alipay_notify.htm")
	public void wap_alipay_notify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			// 如果没有配置Tomcat的get编码为UTF-8，需要下面一行代码转码，否则会出现乱码，导致回调失败
			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//

		// 解密（如果是RSA签名需要解密，如果是MD5签名则下面一行清注释掉）
		// Map<String,String> decrypt_params = AlipayNotify.decrypt(params);
		// XML解析notify_data数据
		// System.out.println(params);
		Document doc_notify_data = DocumentHelper.parseText(params
				.get("notify_data"));
		// 商户订单号
		String order_no = doc_notify_data
				.selectSingleNode("//notify/out_trade_no").getText().split("-")[2];
		// 支付宝交易号
		String trade_no = doc_notify_data.selectSingleNode("//notify/trade_no")
				.getText();
		// 交易状态
		String trade_status = doc_notify_data.selectSingleNode(
				"//notify/trade_status").getText();
		// 商品名称、订单名称
		String type = CommUtil.null2String(request.getParameter("pay_body"))
				.trim();
		if ("".endsWith(type)) {
			type = "goods";
		}
		OrderForm main_order = null;
		Predeposit obj = null;
		GoldRecord gold = null;
		IntegralGoodsOrder ig_order = null;
		if (type.equals("goods") || type.equals("group")) {
			main_order = this.orderFormService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("cash")) {
			obj = this.predepositService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("gold")) {
			gold = this.goldRecordService.getObjById(CommUtil
					.null2Long(order_no));
		}
		if (type.equals("integral")) {
			ig_order = this.integralGoodsOrderService.getObjById(CommUtil
					.null2Long(order_no));
		}
		AlipayConfig config = new AlipayConfig();
		if (type.equals("goods") || type.equals("group")) {
			config.setKey(main_order.getPayment().getSafeKey());
			config.setPartner(main_order.getPayment().getPartner());
			config.setSeller_email(main_order.getPayment().getSeller_email());
		}
		if (type.equals("cash") || type.equals("gold")
				|| type.equals("integral")) {
			Map q_params = new HashMap();
			q_params.put("install", true);
			if (type.equals("cash")) {
				q_params.put("mark", obj.getPd_payment());
			}
			if (type.equals("gold")) {
				q_params.put("mark", gold.getGold_payment());
			}
			if (type.equals("integral")) {
				q_params.put("mark", ig_order.getIgo_payment());
			}
			List<Payment> payments = this.paymentService
					.query("select obj from Payment obj where obj.install=:install and obj.mark=:mark",
							q_params, -1, -1);
			config.setKey(payments.get(0).getSafeKey());
			config.setPartner(payments.get(0).getPartner());
			config.setSeller_email(payments.get(0).getSeller_email());
		}
		config.setNotify_url(CommUtil.getURL(request)
				+ "/wap/alipay_notify.htm");
		config.setReturn_url(CommUtil.getURL(request)
				+ "/wap/aplipay_return.htm");
		boolean verify_result = AlipayNotify.verify(config, params);
		if (verify_result) {// 验证成功
			if (type.equals("goods")) {
				if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
						|| trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					User buyer = this.userService.getObjById(CommUtil
							.null2Long(main_order.getUser_id()));
					if (main_order.getOrder_status() < 20) {// 异步没有出来订单，则同步处理订单
						main_order.setOrder_status(20);
						main_order.setOut_order_id(trade_no);
						main_order.setPayTime(new Date());
						this.orderFormService.update(main_order);
						// 主订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
						this.update_goods_inventory(main_order);
						OrderFormLog main_ofl = new OrderFormLog();
						main_ofl.setAddTime(new Date());
						main_ofl.setLog_info("支付宝在线支付");
						main_ofl.setLog_user(buyer);
						main_ofl.setOf(main_order);
						this.orderFormLogService.save(main_ofl);
						// 主订单付款成功，发送邮件提示
						// 向加盟商家发送付款成功邮件提示，自营商品无需发送邮件提示
						this.send_msg_tobuyer(request, main_order);
						this.send_msg_toseller(request, main_order);
						// 子订单操作
						if (main_order.getOrder_main() == 1
								&& !CommUtil.null2String(
										main_order.getChild_order_detail())
										.equals("")) {// 同步完成子订单付款状态调整
							List<Map> maps = this.orderFormTools
									.queryGoodsInfo(main_order
											.getChild_order_detail());
							for (Map child_map : maps) {
								OrderForm child_order = this.orderFormService
										.getObjById(CommUtil
												.null2Long(child_map
														.get("order_id")));
								if (child_order.getOrder_status() != 20) {
									child_order.setOrder_status(20);
									child_order.setPayTime(new Date());
									this.orderFormService.update(child_order);
									// 子订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
									this.update_goods_inventory(child_order);
									OrderFormLog child_ofl = new OrderFormLog();
									child_ofl.setAddTime(new Date());
									child_ofl.setLog_info("支付宝在线支付");
									child_ofl.setLog_user(buyer);
									child_ofl.setOf(child_order);
									this.orderFormLogService.save(child_ofl);
									// 向加盟商家发送付款成功短信提示，自营商品无需发送短信提示
									// 付款成功，发送邮件提示
									this.send_msg_toseller(request, child_order);

								}
							}
						}
					}
				}
			}
			if (type.equals("group")) {
				if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
						|| trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					if (main_order.getOrder_status() != 20) {// 异步没有出来订单，则同步处理订单
						this.generate_groupInfos(request, main_order, "alipay",
								"支付宝在线支付", trade_no);
					}
				}
			}
			if (type.equals("cash")) {
				if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
						|| trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					if (obj.getPd_pay_status() < 2) {
						obj.setPd_status(1);
						obj.setPd_pay_status(2);
						this.predepositService.update(obj);
						User user = this.userService.getObjById(obj
								.getPd_user().getId());
						user.setAvailableBalance(BigDecimal.valueOf(CommUtil
								.add(user.getAvailableBalance(),
										obj.getPd_amount())));
						this.userService.update(user);
						PredepositLog log = new PredepositLog();
						log.setAddTime(new Date());
						log.setPd_log_amount(obj.getPd_amount());
						log.setPd_log_user(obj.getPd_user());
						log.setPd_op_type("充值");
						log.setPd_type("可用预存款");
						log.setPd_log_info("支付宝在线支付");
						this.predepositLogService.save(log);
					}
				}
			}
			if (type.equals("gold")) {
				if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
						|| trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					if (gold.getGold_pay_status() < 2) {
						gold.setGold_status(1);
						gold.setGold_pay_status(2);
						this.goldRecordService.update(gold);
						User user = this.userService.getObjById(gold
								.getGold_user().getId());
						user.setGold(user.getGold() + gold.getGold_count());
						this.userService.update(user);
						GoldLog log = new GoldLog();
						log.setAddTime(new Date());
						log.setGl_payment(gold.getGold_payment());
						log.setGl_content("支付宝在线支付");
						log.setGl_money(gold.getGold_money());
						log.setGl_count(gold.getGold_count());
						log.setGl_type(0);
						log.setGl_user(gold.getGold_user());
						log.setGr(gold);
						this.goldLogService.save(log);
					}
				}
			}
			if (type.equals("integral")) {
				if (trade_status.equals("WAIT_SELLER_SEND_GOODS")
						|| trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					if (ig_order.getIgo_status() < 20) {
						ig_order.setIgo_status(20);
						ig_order.setIgo_pay_time(new Date());
						ig_order.setIgo_payment("alipay");
						this.integralGoodsOrderService.update(ig_order);
						List<Map> ig_maps = this.orderFormTools
								.query_integral_goodsinfo(ig_order
										.getGoods_info());
						for (Map map : ig_maps) {
							IntegralGoods goods = this.integralGoodsService
									.getObjById(CommUtil.null2Long(map
											.get("id")));
							goods.setIg_goods_count(goods.getIg_goods_count()
									- CommUtil.null2Int(map
											.get("ig_goods_count")));
							goods.setIg_exchange_count(goods
									.getIg_exchange_count()
									+ CommUtil.null2Int(map
											.get("ig_goods_count")));
							this.integralGoodsService.update(goods);
						}
					}
				}
			}
			response.setContentType("text/plain");
			response.setHeader("Cache-Control", "no-cache");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer;
			try {
				writer = response.getWriter();
				writer.print("success");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			response.setContentType("text/plain");
			response.setHeader("Cache-Control", "no-cache");
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer;
			try {
				writer = response.getWriter();
				writer.print("fail");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 微信扫码支付回调函数
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/wap/weixin_return1.htm")
	public void weixin_return1(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ReponseHandlerForWx resHandler = new ReponseHandlerForWx(request,
				response);
		// 给订单添加支付方式 ,
		List<Payment> payments = new ArrayList<Payment>();
		Map params = new HashMap();
		params.put("mark", "wx_pay");
		payments = this.paymentService.query(
				"select obj from Payment obj where obj.mark=:mark", params, -1,
				-1);
		Payment payment = null;
		if (payments.size() > 0) {
			payment = payments.get(0);
		}
		String app_key = payment.getWx_paySignKey();
		String partner_key = payment.getTenpay_key();
		resHandler.setKey(partner_key);
		resHandler.setKey(app_key);
		// 创建请求对象
		RequestHandler queryReq = new RequestHandler(null, null);
		queryReq.init();
		if (resHandler.isValidSign(partner_key) == true) {
			if (resHandler.isWXsign(app_key) == true) {
				// 商户订单号
				String out_trade_no = resHandler.getParameter("out_trade_no");
				// 财付通订单号
				String transaction_id = resHandler
						.getParameter("transaction_id");
				// 金额,以分为单位
				String total_fee = resHandler.getParameter("total_fee");
				// 如果有使用折扣券，discount有值，total_fee+discount=原请求的total_fee
				String discount = resHandler.getParameter("discount");
				// 支付结果
				String trade_state = resHandler.getParameter("trade_state");
				String[] attachs = request.getParameter("attach").split("_");
				String type = attachs[3];
				if ("0".equals(trade_state)) {
					OrderForm main_order = null;
					Predeposit obj = null;
					GoldRecord gold = null;
					IntegralGoodsOrder ig_order = null;
					// ------------------------------
					// 即时到账处理业务开始
					// ------------------------------
					if ("goods".equals(type) || "group".equals(type)) {
						main_order = this.orderFormService.getObjById(CommUtil
								.null2Long(attachs[0]));
						User buyer = this.userService.getObjById(CommUtil
								.null2Long(main_order.getUser_id()));
						if (main_order != null
								&& main_order.getOrder_status() < 20) {// 异步没有出来订单，则同步处理订单
							main_order.setOrder_status(20);
							main_order
									.setOut_order_id(main_order.getTrade_no());
							main_order.setPayTime(new Date());
							this.orderFormService.update(main_order);
							// 主订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
							this.update_goods_inventory(main_order);
							OrderFormLog main_ofl = new OrderFormLog();
							main_ofl.setAddTime(new Date());
							main_ofl.setLog_info("微信支付");
							main_ofl.setLog_user(buyer);
							main_ofl.setOf(main_order);
							this.orderFormLogService.save(main_ofl);
							// 主订单付款成功，发送邮件提示
							// 向加盟商家发送付款成功邮件提示，自营商品无需发送邮件提示
							this.send_msg_tobuyer(request, main_order);
							this.send_msg_toseller(request, main_order);
							// 子订单操作
							if (main_order.getOrder_main() == 1
									&& !CommUtil.null2String(
											main_order.getChild_order_detail())
											.equals("")) {// 同步完成子订单付款状态调整
								List<Map> maps = this.orderFormTools
										.queryGoodsInfo(main_order
												.getChild_order_detail());
								for (Map child_map : maps) {
									OrderForm child_order = this.orderFormService
											.getObjById(CommUtil
													.null2Long(child_map
															.get("order_id")));
									if (child_order.getOrder_status() != 20) {
										child_order.setOrder_status(20);
										child_order.setPayTime(new Date());
										this.orderFormService
												.update(child_order);
										// 子订单付款成功，订单状态更新，同时更新商品库存，如果是团购商品，则更新团购库存
										this.update_goods_inventory(child_order);
										OrderFormLog child_ofl = new OrderFormLog();
										child_ofl.setAddTime(new Date());
										child_ofl.setLog_info("微信支付");
										child_ofl.setLog_user(buyer);
										child_ofl.setOf(child_order);
										this.orderFormLogService
												.save(child_ofl);
										// 向加盟商家发送付款成功短信提示，自营商品无需发送短信提示
										// 付款成功，发送邮件提示
										this.send_msg_toseller(request,
												child_order);

									}
								}
							}
						}
					}

					if ("integral".equals(type)) {
						ig_order = this.integralGoodsOrderService
								.getObjById(CommUtil.null2Long(attachs[0]));
						if (ig_order != null && ig_order.getIgo_status() < 20) {
							ig_order.setIgo_status(20);
							ig_order.setIgo_pay_time(new Date());
							ig_order.setIgo_payment("wx_pay");
							this.integralGoodsOrderService.update(ig_order);
							List<Map> ig_maps = this.orderFormTools
									.query_integral_goodsinfo(ig_order
											.getGoods_info());
							for (Map map : ig_maps) {
								IntegralGoods goods = this.integralGoodsService
										.getObjById(CommUtil.null2Long(map
												.get("id")));
								goods.setIg_goods_count(goods
										.getIg_goods_count()
										- CommUtil.null2Int(map
												.get("ig_goods_count")));
								goods.setIg_exchange_count(goods
										.getIg_exchange_count()
										+ CommUtil.null2Int(map
												.get("ig_goods_count")));
								this.integralGoodsService.update(goods);
							}
						}
					}

					if ("group".equals(type)) {
						if (main_order != null
								&& main_order.getOrder_status() != 20) {// 异步没有出来订单，则同步处理订单
							this.generate_groupInfos(request, main_order,
									"wx_pay", "微信支付", main_order.getTrade_no());
						}
					}
					System.out.println("success 后台通知成功");
					// 给财付通系统发送成功信息，财付通系统收到此结果后不再进行后续通知
					resHandler.sendToCFT("success");
				} else {
					System.out.println("fail 支付失败");
					resHandler.sendToCFT("fail");
				}
			} else {// sha1签名失败
				System.out.println("fail -SHA1 failed");
				resHandler.sendToCFT("fail");
			}
		} else {// MD5签名失败
			System.out.println("fail -Md5 failed");
			resHandler.sendToCFT("fail");
		}
	}

	@RequestMapping("/wx_pay_success.htm")
	public ModelAndView wx_pay_success(HttpServletRequest request,
			HttpServletResponse response, String id) {
		ModelAndView mv = new JModelAndView("wap/order_pay_finish.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		OrderForm obj = this.orderFormService
				.getObjById(CommUtil.null2Long(id));
		if (obj != null) {
			mv.addObject("obj", obj);
			mv.addObject("all_price", obj.getTotalPrice());
		} else {
			mv = new JModelAndView("wap/error.html",
					configService.getSysConfig(),
					this.userConfigService.getUserConfig(), 1, request,
					response);
			mv.addObject("op_title", "支付回调失败！");
			mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
		}
		return mv;
	}

	@RequestMapping("/weixin/pay/wx_pay.htm")
	public ModelAndView wx_pay(HttpServletRequest request,
			HttpServletResponse response, String id, String type)
			throws Exception {
		ModelAndView mv = new JModelAndView("wap/wx_pay.html",
				configService.getSysConfig(),
				this.userConfigService.getUserConfig(), 1, request, response);
		RequestHandler reqHandler = new RequestHandler(request, response);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		TreeMap<String, String> outParams = new TreeMap<String, String>();
		// 给订单添加支付方式 ,
		List<Payment> payments = new ArrayList<Payment>();
		Map params = new HashMap();
		params.put("mark", "wx_pay");
		payments = this.paymentService.query(
				"select obj from Payment obj where obj.mark=:mark", params, -1,
				-1);
		Payment payment = null;
		if (payments.size() > 0) {
			payment = payments.get(0);
		}
		if ("integral".equals(type)) {
			IntegralGoodsOrder of = this.integralGoodsOrderService
					.getObjById(CommUtil.null2Long(id));
			if (payment != null && of != null) {
				String app_id = payment.getWx_appid();
				String app_secret = payment.getWx_appSecret();
				String app_key = payment.getWx_paySignKey();
				String partner_key = payment.getTenpay_key();
				String partner = payment.getTenpay_partner();
				reqHandler.init();
				reqHandler.init(app_id, app_secret, partner_key, app_key);

				// 当前时间 yyyyMMddHHmmss
				// String currTime = TenpayUtil.getCurrTime();
				// 8位日期
				// String strTime = currTime.substring(8, currTime.length());
				// 四位随机数
				// String strRandom = TenpayUtil.buildRandom(4) + "";
				// 10位序列号,可以自行调整。
				String strReq = of.getIgo_order_sn(); // order_id

				// 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
				String out_trade_no = strReq;

				// 获取提交的商品价格
				double total_fee = Double.valueOf(of.getIgo_trans_fee()
						.toString()) * 100;
				int order_price = (int) total_fee;
				// 获取提交的商品名称
				List<Map> ig_maps = Json.fromJson(List.class,
						of.getGoods_info());
				String product_name = ig_maps.get(0).get("ig_goods_name")
						.toString();
				// 设置package订单参数
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("bank_type", "WX"); // 支付类型
				packageParams.put("body", out_trade_no); // 商品描述
				packageParams.put("fee_type", "1"); // 银行币种
				packageParams.put("input_charset", "UTF-8"); // 字符集
				String path = request.getContextPath();
				String basePath = request.getScheme() + "://"
						+ request.getServerName() + path + "/";
				packageParams.put("notify_url", basePath
						+ "wap/weixin_return.htm"); // 通知地址
				packageParams.put("out_trade_no", out_trade_no); // 商户订单号
				packageParams.put("partner", partner); // 设置商户号
				packageParams.put("total_fee", order_price + ""); // 商品总金额,以分为单位
				packageParams.put("spbill_create_ip", request.getRemoteAddr()); // 订单生成的机器IP，指用户浏览器端IP
				packageParams.put("attach",
						of.getId() + "_" + of.getIgo_order_sn() + "_"
								+ of.getIgo_user().getId() + "_" + type); // id
																			// order_id
																			// user_id用_分割

				// 获取package包
				// reqHandler.setKey(partner_key);
				String packageValue = reqHandler.genPackage(packageParams,
						partner_key);
				String noncestr = Sha1Util.getNonceStr();
				String timestamp = Sha1Util.getTimeStamp();

				// 设置支付参数
				SortedMap<String, String> signParams = new TreeMap<String, String>();
				signParams.put("appid", app_id);
				signParams.put("noncestr", noncestr);
				signParams.put("package", packageValue);
				signParams.put("timestamp", timestamp);
				signParams.put("appkey", app_key);
				// 生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
				String sign = Sha1Util.createSHA1Sign(signParams);

				// 增加非参与签名的额外参数
				signParams.put("paySign", sign);
				signParams.put("signType", "sha1");

				mv.addObject("app_id", app_id);
				mv.addObject("timestamp", timestamp);
				mv.addObject("noncestr", noncestr);
				mv.addObject("packageValue", packageValue);
				mv.addObject("sign", sign);
				mv.addObject("obj", of);
			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "支付方式错误！");
				mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
			}
		} else {
			OrderForm of = this.orderFormService.getObjById(CommUtil
					.null2Long(id));
			if (payment != null && of != null) {
				String app_id = payment.getWx_appid();
				String app_secret = payment.getWx_appSecret();
				String app_key = payment.getWx_paySignKey();
				String partner_key = payment.getTenpay_key();
				String partner = payment.getTenpay_partner();
				reqHandler.init();
				reqHandler.init(app_id, app_secret, partner_key, app_key);

				// 当前时间 yyyyMMddHHmmss
				// String currTime = TenpayUtil.getCurrTime();
				// 8位日期
				// String strTime = currTime.substring(8, currTime.length());
				// 四位随机数
				// String strRandom = TenpayUtil.buildRandom(4) + "";
				// 10位序列号,可以自行调整。
				String strReq = of.getOrder_id(); // order_id

				// 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
				String out_trade_no = strReq;

				// 获取提交的商品价格
				double total_fee = Double
						.valueOf(of.getTotalPrice().toString()) * 100;
				int order_price = (int) total_fee;
				// 获取提交的商品名称
				List<Map> list = Json.fromJson(ArrayList.class,
						of.getGoods_info());
				String product_name = this.configService.getSysConfig()
						.getTitle();
				if (list.size() > 0) {
					product_name = list.get(0).get("goods_name").toString()
							+ "等";
				}

				// 设置package订单参数
				SortedMap<String, String> packageParams = new TreeMap<String, String>();
				packageParams.put("bank_type", "WX"); // 支付类型
				packageParams.put("body", out_trade_no); // 商品描述
				packageParams.put("fee_type", "1"); // 银行币种
				packageParams.put("input_charset", "UTF-8"); // 字符集
				String path = request.getContextPath();
				String basePath = request.getScheme() + "://"
						+ request.getServerName() + path + "/";
				packageParams.put("notify_url", basePath
						+ "wap/weixin_return.htm"); // 通知地址
				packageParams.put("out_trade_no", out_trade_no); // 商户订单号
				packageParams.put("partner", partner); // 设置商户号
				packageParams.put("total_fee", order_price + ""); // 商品总金额,以分为单位
				packageParams.put("spbill_create_ip", request.getRemoteAddr()); // 订单生成的机器IP，指用户浏览器端IP
				packageParams.put("attach", of.getId() + "_" + of.getOrder_id()
						+ "_" + of.getUser_id() + "_" + type); // id order_id
																// user_id用_分割

				// 获取package包
				// reqHandler.setKey(partner_key);
				String packageValue = reqHandler.genPackage(packageParams,
						partner_key);
				String noncestr = Sha1Util.getNonceStr();
				String timestamp = Sha1Util.getTimeStamp();

				// 设置支付参数
				SortedMap<String, String> signParams = new TreeMap<String, String>();
				signParams.put("appid", app_id);
				signParams.put("noncestr", noncestr);
				signParams.put("package", packageValue);
				signParams.put("timestamp", timestamp);
				signParams.put("appkey", app_key);
				// 生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
				String sign = Sha1Util.createSHA1Sign(signParams);

				// 增加非参与签名的额外参数
				signParams.put("paySign", sign);
				signParams.put("signType", "sha1");

				mv.addObject("app_id", app_id);
				mv.addObject("timestamp", timestamp);
				mv.addObject("noncestr", noncestr);
				mv.addObject("packageValue", packageValue);
				mv.addObject("sign", sign);
				mv.addObject("obj", of);
			} else {
				mv = new JModelAndView("wap/error.html",
						configService.getSysConfig(),
						this.userConfigService.getUserConfig(), 1, request,
						response);
				mv.addObject("op_title", "支付方式错误！");
				mv.addObject("url", CommUtil.getURL(request) + "/wap/index.htm");
			}
		}

		// 初始化

		return mv;

	}

	private void generate_groupInfos(HttpServletRequest request,
			OrderForm order, String mark, String pay_info, String trade_no)
			throws Exception {
		if (order.getOrder_status() < 20) {
			order.setOrder_status(20);
			order.setOut_order_id(trade_no);
			order.setPayTime(new Date());
			// 生活团购订单付款时增加退款时效
			if (order.getOrder_cat() == 2) {
				Calendar ca = Calendar.getInstance();
				ca.add(ca.DATE, this.configService.getSysConfig()
						.getGrouplife_order_return());
				SimpleDateFormat bartDateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String latertime = bartDateFormat.format(ca.getTime());
				order.setReturn_shipTime(CommUtil.formatDate(latertime));
			}
			this.orderFormService.update(order);
			OrderFormLog ofl = new OrderFormLog();
			ofl.setAddTime(new Date());
			ofl.setLog_info(pay_info);
			User buyer = this.userService.getObjById(CommUtil.null2Long(order
					.getUser_id()));
			ofl.setLog_user(buyer);
			ofl.setOf(order);
			this.orderFormLogService.save(ofl);
			Store store = null;
			if (order.getStore_id() != null && !"".equals(order.getStore_id())) {
				store = this.storeService.getObjById(CommUtil.null2Long(order
						.getStore_id()));
			}

			if (order.getOrder_cat() == 2) {
				Map map = this.orderFormTools.queryGroupInfo(order
						.getGroup_info());
				int count = CommUtil
						.null2Int(map.get("goods_count").toString());
				String goods_id = map.get("goods_id").toString();
				GroupLifeGoods goods = this.groupLifeGoodsService
						.getObjById(CommUtil.null2Long(goods_id));
				goods.setGroup_count(goods.getGroup_count()
						- CommUtil.null2Int(count));
				goods.setSelled_count(goods.getSelled_count()
						+ CommUtil.null2Int(count));
				this.groupLifeGoodsService.update(goods);
				Map pay_params = new HashMap();
				pay_params.put("mark", mark);
				List<Payment> payments = this.paymentService.query(
						"select obj from Payment obj where obj.mark=:mark",
						pay_params, -1, -1);
				int i = 0;
				List<String> code_list = new ArrayList();// 存放团购消费码
				String codes = "";
				while (i < count) {
					GroupInfo info = new GroupInfo();
					info.setAddTime(new Date());
					info.setLifeGoods(goods);
					info.setPayment(payments.get(0));
					info.setOrder_id(order.getId());
					info.setUser_id(buyer.getId());
					info.setUser_name(buyer.getUserName());
					info.setGroup_sn(buyer.getId()
							+ CommUtil.formatTime("yyyyMMddHHmmss" + i,
									new Date()));
					Calendar ca2 = Calendar.getInstance();
					ca2.add(ca2.DATE, this.configService.getSysConfig()
							.getGrouplife_order_return());
					SimpleDateFormat bartDateFormat2 = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String latertime2 = bartDateFormat2.format(ca2.getTime());
					info.setRefund_Time(CommUtil.formatDate(latertime2));
					this.groupInfoService.save(info);
					codes = codes + info.getGroup_sn() + " ";
					code_list.add(info.getGroup_sn());
					i++;
				}
				// 更新lucene索引
				String goods_lucene_path = System.getProperty("metoob2b2c.root")
						+ File.separator + "luence" + File.separator
						+ "grouplifegoods";
				File file = new File(goods_lucene_path);
				if (!file.exists()) {
					CommUtil.createFolder(goods_lucene_path);
				}
				LuceneUtil lucene = LuceneUtil.instance();
				lucene.setIndex_path(goods_lucene_path);
				lucene.update(CommUtil.null2String(goods.getId()),
						luceneVoTools.updateLifeGoodsIndex(goods));
				// 如果为运营商发布的团购则进行结算日志生成
				if (order.getOrder_form() == 0) {
					PayoffLog plog = new PayoffLog();
					plog.setPl_sn("pl"
							+ CommUtil.formatTime("yyyyMMddHHmmss", new Date())
							+ store.getUser().getId());
					plog.setPl_info("团购码生成成功");
					plog.setAddTime(new Date());
					plog.setSeller(store.getUser());
					plog.setO_id(CommUtil.null2String(order.getId()));
					plog.setOrder_id(order.getOrder_id().toString());
					plog.setCommission_amount(BigDecimal.valueOf(CommUtil
							.null2Double("0.00")));// 该订单总佣金费用
					plog.setGoods_info(order.getGroup_info());
					plog.setOrder_total_price(order.getTotalPrice());// 该订单总商品金额
					plog.setTotal_amount(order.getTotalPrice());// 该订单应结算金额：结算金额=订单总商品金额-总佣金费用
					this.payoffservice.save(plog);
					store.setStore_sale_amount(BigDecimal.valueOf(CommUtil.add(
							order.getGoods_amount(),
							store.getStore_sale_amount())));// 店铺本次结算总销售金额
					store.setStore_commission_amount(BigDecimal
							.valueOf(CommUtil.add(order.getCommission_amount(),
									store.getStore_commission_amount())));// 店铺本次结算总佣金
					store.setStore_payoff_amount(BigDecimal.valueOf(CommUtil
							.add(plog.getTotal_amount(),
									store.getStore_payoff_amount())));// 店铺本次结算总佣金
					this.storeService.update(store);
				}
				// 增加系统总销售金额、总佣金
				SysConfig sc = this.configService.getSysConfig();
				sc.setPayoff_all_sale(BigDecimal.valueOf(CommUtil.add(
						order.getGoods_amount(), sc.getPayoff_all_sale())));
				sc.setPayoff_all_commission(BigDecimal.valueOf(CommUtil.add(
						order.getCommission_amount(),
						sc.getPayoff_all_commission())));
				this.configService.update(sc);
				String msg_content = "恭喜您成功购买团购" + map.get("goods_name")
						+ ",团购消费码分别为：" + codes + "您可以到用户中心-我的生活购中查看消费码的使用情况";
				// 发送系统站内信给买家
				Message tobuyer_msg = new Message();
				tobuyer_msg.setAddTime(new Date());
				tobuyer_msg.setStatus(0);
				tobuyer_msg.setType(0);
				tobuyer_msg.setContent(msg_content);
				tobuyer_msg.setFromUser(this.userService.getObjByProperty(null,
						"userName", "admin"));
				tobuyer_msg.setToUser(buyer);
				this.messageService.save(tobuyer_msg);
				// 发送系统站内信给卖家
				Message toSeller_msg = new Message();
				toSeller_msg.setAddTime(new Date());
				toSeller_msg.setStatus(0);
				toSeller_msg.setType(0);
				toSeller_msg.setContent(buyer.getUsername());
				toSeller_msg.setFromUser(this.userService.getObjByProperty(
						null, "userName", "admin"));
				toSeller_msg.setToUser(goods.getUser());
				this.messageService.save(toSeller_msg);
				// 付款成功，发送短信团购消费码
				this.send_groupInfo_sms(request, order, buyer.getMobile(),
						"sms_tobuyer_online_ok_send_groupinfo", code_list,
						buyer.getId().toString(), goods.getUser().getId()
								.toString());
			}
			this.send_msg_tobuyer(request, order);
		}
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
						gg.setGg_selled_count(gg.getGg_selled_count()
								+ goods_count);
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
			String spectype = "";
			for (GoodsSpecProperty gsp : temp_gsp_list) {
				gsps.add(gsp.getId().toString());
				spectype += gsp.getSpec().getName() + ":" + gsp.getValue()
						+ " ";
			}
			String[] gsp_list = new String[gsps.size()];
			gsps.toArray(gsp_list);
			goods.setGoods_salenum(goods.getGoods_salenum() + goods_count);
			GoodsLog todayGoodsLog = this.goodsViewTools.getTodayGoodsLog(goods
					.getId());
			todayGoodsLog.setGoods_salenum(todayGoodsLog.getGoods_salenum()
					+ goods_count);

			Map<String, Integer> logordermap = (Map<String, Integer>) Json
					.fromJson(todayGoodsLog.getGoods_order_type());
			String ordertype = order.getOrder_type();
			if (logordermap.containsKey(ordertype)) {
				logordermap.put(ordertype, logordermap.get(ordertype)
						+ goods_count);
			} else {
				logordermap.put(ordertype, goods_count);
			}
			todayGoodsLog.setGoods_order_type(Json.toJson(logordermap,
					JsonFormat.compact()));

			Map<String, Integer> logspecmap = (Map<String, Integer>) Json
					.fromJson(todayGoodsLog.getGoods_sale_info());

			if (logspecmap.containsKey(spectype)) {
				logspecmap
						.put(spectype, logspecmap.get(spectype) + goods_count);
			} else {
				logspecmap.put(spectype, goods_count);
			}
			todayGoodsLog.setGoods_sale_info(Json.toJson(logspecmap,
					JsonFormat.compact()));

			this.goodsLogService.update(todayGoodsLog);
			String inventory_type = goods.getInventory_type() == null ? "all"
					: goods.getInventory_type();
			boolean inventory_warn = false;
			if (inventory_type.equals("all")) {
				goods.setGoods_inventory(goods.getGoods_inventory()
						- goods_count);
				if (goods.getGoods_inventory() <= goods
						.getGoods_warn_inventory()) {
					inventory_warn = true;
				}
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
						if (CommUtil.null2Int(temp.get("count")) <= CommUtil
								.null2Int(temp.get("supp"))) {
							inventory_warn = true;
						}
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
			if (inventory_warn) {
				goods.setWarn_inventory_status(-1);// 该商品库存预警状态
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
		// 判断是否有满就送如果有则进行库存操作
		if (order.getWhether_gift() == 1) {
			this.buyGiftViewTools.update_gift_invoke(order);
		}
	}

	/**
	 * 发送短信团购消费码
	 * 
	 * @param request
	 * @param order
	 * @param mobile
	 * @param mark
	 * @param codes
	 * @param buyer_id
	 * @param seller_id
	 * @throws Exception
	 */
	private void send_groupInfo_sms(HttpServletRequest request,
			OrderForm order, String mobile, String mark, List<String> codes,
			String buyer_id, String seller_id) throws Exception {
		Template template = this.templateService.getObjByProperty(null, "mark",
				mark);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < codes.size(); i++) {
			sb.append(codes.get(i) + ",");
		}
		String code = sb.toString();
		if (template != null && template.isOpen()) {
			ExpressionParser exp = new SpelExpressionParser();
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("buyer",
					this.userService.getObjById(CommUtil.null2Long(buyer_id)));
			context.setVariable("seller",
					this.userService.getObjById(CommUtil.null2Long(seller_id)));
			context.setVariable("config", this.configService.getSysConfig());
			context.setVariable("send_time",
					CommUtil.formatLongDate(new Date()));
			context.setVariable("webPath", CommUtil.getURL(request));
			context.setVariable("order", order);
			context.setVariable("code", code);
			Expression ex = exp.parseExpression(template.getContent(),
					new SpelTemplate());
			String content = ex.getValue(context, String.class);
			this.msgTools.sendSMS(mobile, content);
		}
	}

	/**
	 * 在线支付回调后，向买家、商家发送短信、邮件提醒订单在线付款成功！
	 * 
	 * @param request
	 * @param order
	 * @throws Exception
	 */
	private void send_msg_tobuyer(HttpServletRequest request, OrderForm order)
			throws Exception {
		User buyer = this.userService.getObjById(CommUtil.null2Long(order
				.getUser_id()));
		if (order.getOrder_form() == 0) {
			Store store = this.storeService.getObjById(CommUtil.null2Long(order
					.getStore_id()));
			User seller = store.getUser();
			this.msgTools.sendEmailCharge(CommUtil.getURL(request),
					"email_tobuyer_online_pay_ok_notify", buyer.getEmail(),
					null, CommUtil.null2String(order.getId()),
					order.getStore_id());
			this.msgTools.sendSmsCharge(CommUtil.getURL(request),
					"sms_tobuyer_online_pay_ok_notify", buyer.getMobile(),
					null, CommUtil.null2String(order.getId()),
					order.getStore_id());
		} else {
			/*this.msgTools.sendEmailFree(CommUtil.getURL(request),
					"email_tobuyer_online_pay_ok_notify", buyer.getEmail(),
					CommUtil.null2String(order.getId()), order.getStore_id());*/
			this.msgTools.sendSmsFree(CommUtil.getURL(request),
					"sms_tobuyer_online_pay_ok_notify", buyer.getMobile(),
					null, CommUtil.null2String(order.getId()));
		}
	}

	/**
	 * 在线支付回调后，向买家、商家发送短信、邮件提醒订单在线付款成功！
	 * 
	 * @param request
	 * @param order
	 * @throws Exception
	 */
	private void send_msg_toseller(HttpServletRequest request, OrderForm order)
			throws Exception {
		User buyer = this.userService.getObjById(CommUtil.null2Long(order
				.getUser_id()));
		if (order.getOrder_form() == 0) {
			Store store = this.storeService.getObjById(CommUtil.null2Long(order
					.getStore_id()));
			User seller = store.getUser();
			this.msgTools.sendEmailCharge(CommUtil.getURL(request),
					"email_toseller_online_pay_ok_notify", seller.getEmail(),
					null, CommUtil.null2String(order.getId()),
					order.getStore_id());
			this.msgTools.sendSmsCharge(CommUtil.getURL(request),
					"sms_toseller_online_pay_ok_notify", seller.getMobile(),
					null, CommUtil.null2String(order.getId()),
					order.getStore_id());
		}
	}

}
