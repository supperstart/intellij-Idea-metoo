package com.metoo.msg;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.metoo.core.constant.Globals;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.PopupAuthenticator;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Store;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IStoreService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.foundation.service.IUserService;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.msg.email.SpelTemplate;

/**
 * 
 * <p>
 * Title: MsgTools.java<／p>
 * 
 * <p>
 * Description: 系统手机短信、邮件发送工具类，手机短信发送需要运营商购买短信平台提供的相关接口信息，邮件发送需要正确配置邮件服务器，
 * 运营商管理后台均有相关配置及发送测试（erikzhang） <／p>
 * <p>
 * 发送短信邮件工具类 参数json数据 buyer_id:如果有买家，则买家user.id seller_id:如果有卖家,卖家的user.id
 * sender_id:发送者的user.id receiver_id:接收者的user.id order_id:如果有订单 订单order.id
 * childorder_id：如果有子订单id goods_id:商品的id self_goods: 如果是自营商品 则在邮件或者短信显示 平台名称
 * SysConfig.title,（jinxinzhe）
 * 
 * 其中收费工具类只作为商家和用户在交易中的发送工具类，发送的短信邮件均收费，需要商家在商家中心购买相应数量的短信和邮件，
 * 在短信和邮件数量允许的情况下才能发送（hezeng）
 * </p>
 * 
 * 
 * <p>
 * Copyright: Copyright (c) 2015<／p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com<／p>
 * 
 * @author erikzhang，jinxinzhe，hezeng
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Component
public class MsgTools {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserService userService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IStoreService storeService;
	@Autowired
	private OrderFormTools orderFormTools;

	/**
	 * 收费短信发送方法，商家购买短信或者邮件后，当商家有交易订单需要发送短信提醒商家或者订单用户时使用该收费工具
	 * 
	 * @param request
	 * @param mark
	 * @param mobile
	 * @param json
	 *            :参数json，发送非订单信息的参数
	 * @param order_id
	 *            ：订单id，
	 * @throws Exception
	 */
	@Async
	public void sendSmsCharge(String web, String mark, String mobile, String json, String order_id, String store_id)
			throws Exception {
		if (this.configService.getSysConfig().isSmsEnbale()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			Store store = null;
			boolean flag = false;
			Map function_map = new HashMap();
			List<Map> function_maps = new ArrayList<Map>();
			if (store_id != null && !store_id.equals("")) {
				store = this.storeService.getObjById(CommUtil.null2Long(store_id));
				if (store.getStore_sms_count() > 0) {
					function_maps = (List<Map>) Json.fromJson(store.getSms_email_info());
					for (Map temp_map2 : function_maps) {

						if (template != null
								&& CommUtil.null2String(temp_map2.get("type"))
										.equals(CommUtil.null2String(template.getType()))
								&& CommUtil.null2String(temp_map2.get("mark")).equals(template.getMark())) {
							function_map = temp_map2;
							if (CommUtil.null2Int(function_map.get("sms_open")) == 1) {// 验证功能是否开启
								flag = true;
								break;
							} else {
								System.out.println("商家已关闭该短信发送功能");
							}
						}
					}
				}
			}
			if (flag && template != null && template.isOpen()) {
				ExpressionParser exp = new SpelExpressionParser();
				EvaluationContext context = new StandardEvaluationContext();
				Map map = this.queryJson(json);
				if (mobile != null && !mobile.equals("")) {
					if (order_id != null) {
						OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
						User buyer = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
						context.setVariable("buyer", buyer);
						if (store != null) {
							context.setVariable("seller", store.getUser());
						}
						context.setVariable("config", this.configService.getSysConfig());
						context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
						context.setVariable("webPath", web);
						context.setVariable("order", order);
					}
					if (map.get("receiver_id") != null) {
						Long receiver_id = CommUtil.null2Long(map.get("receiver_id"));
						User receiver = this.userService.getObjById(receiver_id);
						context.setVariable("receiver", receiver);
					}
					if (map.get("sender_id") != null) {
						Long sender_id = CommUtil.null2Long(map.get("sender_id"));
						User sender = this.userService.getObjById(sender_id);
						context.setVariable("sender", sender);
					}
					if (map.get("buyer_id") != null) {
						Long buyer_id = CommUtil.null2Long(map.get("buyer_id"));
						User buyer = this.userService.getObjById(buyer_id);
						context.setVariable("buyer", buyer);
					}
					if (map.get("seller_id") != null) {
						Long seller_id = CommUtil.null2Long(map.get("seller_id"));
						User seller = this.userService.getObjById(seller_id);
						context.setVariable("seller", seller);
					}
					if (map.get("order_id") != null) {
						Long temp_order_id = CommUtil.null2Long(map.get("order_id"));
						OrderForm orderForm = this.orderFormService.getObjById(temp_order_id);
						context.setVariable("orderForm", orderForm);
					}
					if (map.get("childorder_id") != null) {
						Long childorder_id = CommUtil.null2Long(map.get("childorder_id"));
						OrderForm orderForm = this.orderFormService.getObjById(childorder_id);
						context.setVariable("child_orderForm", orderForm);
					}
					if (map.get("goods_id") != null) {
						Long goods_id = CommUtil.null2Long(map.get("goods_id"));
						Goods goods = this.goodsService.getObjById(goods_id);
						context.setVariable("goods", goods);
					}
					if (map.get("self_goods") != null) {
						context.setVariable("seller", map.get("self_goods").toString());
					}
					context.setVariable("config", this.configService.getSysConfig());
					context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
					context.setVariable("webPath", web);
					Expression ex = exp.parseExpression(template.getContent(), new SpelTemplate());
					String content = ex.getValue(context, String.class);
					boolean result = this.sendSMS(mobile, content);
					if (result) {// 更新商家店铺发送短信邮件信息
						System.out.println("发送短信成功");
						if (store != null) {
							store.setStore_sms_count(store.getStore_sms_count() - 1);// 商家短信数量减1
							function_map.put("sms_count", CommUtil.null2Int(function_map.get("sms_count")) + 1);// 商家功能发送短信数量加1
							String sms_email_json = Json.toJson(function_maps, JsonFormat.compact());
							store.setSend_sms_count(store.getSend_sms_count() + 1);
							store.setSms_email_info(sms_email_json);
							this.storeService.update(store);
						}
					}
				}
			}
		} else {
			System.out.println("系统关闭了短信发送功能！");
		}
	}

	/**
	 * 收费邮件发送方法，商家购买短信或者邮件后，当商家有交易订单需要发送短信提醒商家或者订单用户时使用该收费工具
	 * 
	 * @param request
	 * @param mark
	 * @param mobile
	 * @param json
	 * @param order_id
	 *            :订单操作时发送邮件
	 * @param store_id
	 *            :发送邮件的店铺id
	 * @throws Exception
	 */
	@Async
	public void sendEmailCharge(String weburl, String mark, String email, String json, String order_id, String store_id)
			throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			Store store = null;
			boolean flag = false;
			Map function_map = new HashMap();
			List<Map> function_maps = new ArrayList<Map>();
			if (store_id != null && !store_id.equals("")) {
				store = this.storeService.getObjById(CommUtil.null2Long(store_id));
				if (store != null && store.getStore_email_count() > 0) {
					function_maps = (List<Map>) Json.fromJson(store.getSms_email_info());
					for (Map temp_map2 : function_maps) {
						if (template != null
								&& CommUtil.null2String(temp_map2.get("type"))
										.equals(CommUtil.null2String(template.getType()))
								&& CommUtil.null2String(temp_map2.get("mark")).equals(template.getMark())) {
							function_map = temp_map2;
							if (CommUtil.null2Int(function_map.get("email_open")) == 1) {// 验证功能是否开启
								flag = true;
								break;
							} else {
								flag = false;
								System.out.println("商家已关闭该邮件发送功能");
							}
						}
					}
				} else {
					System.out.println("商家没有购买邮件流量");
				}
			}
			if (flag && template != null && template.isOpen()) {
				ExpressionParser exp = new SpelExpressionParser();
				EvaluationContext context = new StandardEvaluationContext();
				Map map = this.queryJson(json);
				String subject = template.getTitle();
				if (order_id != null) {
					OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
					User buyer = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
					context.setVariable("buyer", buyer);
					if (store != null) {
						context.setVariable("seller", store.getUser());
					}
					context.setVariable("config", this.configService.getSysConfig());
					context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
					context.setVariable("webPath", weburl);
					context.setVariable("order", order);
				}
				if (map.get("receiver_id") != null) {
					Long receiver_id = CommUtil.null2Long(map.get("receiver_id"));
					User receiver = this.userService.getObjById(receiver_id);
					context.setVariable("receiver", receiver);
				}
				if (map.get("sender_id") != null) {
					Long sender_id = CommUtil.null2Long(map.get("sender_id"));
					User sender = this.userService.getObjById(sender_id);
					context.setVariable("sender", sender);
				}
				if (map.get("buyer_id") != null) {
					Long buyer_id = CommUtil.null2Long(map.get("buyer_id"));
					User buyer = this.userService.getObjById(buyer_id);
					context.setVariable("buyer", buyer);
				}
				if (map.get("seller_id") != null) {
					Long seller_id = CommUtil.null2Long(map.get("seller_id"));
					User seller = this.userService.getObjById(seller_id);
					context.setVariable("seller", seller);
				}
				if (map.get("order_id") != null) {
					Long temp_order_id = CommUtil.null2Long(map.get("order_id"));
					OrderForm orderForm = this.orderFormService.getObjById(temp_order_id);
					context.setVariable("orderForm", orderForm);
				}
				if (map.get("childorder_id") != null) {
					Long childorder_id = CommUtil.null2Long(map.get("childorder_id"));
					OrderForm orderForm = this.orderFormService.getObjById(childorder_id);
					context.setVariable("child_orderForm", orderForm);
				}
				if (map.get("goods_id") != null) {
					Long goods_id = CommUtil.null2Long(map.get("goods_id"));
					Goods goods = this.goodsService.getObjById(goods_id);
					context.setVariable("goods", goods);
				}
				if (map.get("self_goods") != null) {
					context.setVariable("seller", map.get("self_goods").toString());
				}
				context.setVariable("config", this.configService.getSysConfig());
				context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
				context.setVariable("webPath", weburl);
				Expression ex = exp.parseExpression(template.getContent(), new SpelTemplate());
				String content = ex.getValue(context, String.class);
				boolean result = this.sendEmail(email, subject, content);
				if (result) {// 更新商家店铺发送短信邮件信息
					System.out.println("发送邮件成功");
					if (store != null) {
						store.setStore_email_count(store.getStore_email_count() - 1);// 商家邮件数量减1
						function_map.put("email_count", CommUtil.null2Int(function_map.get("email_count")) + 1);// 商家功能发送邮件数量加1
						String sms_email_json = Json.toJson(function_maps, JsonFormat.compact());
						store.setSms_email_info(sms_email_json);
						store.setSend_email_count(store.getSend_email_count() + 1);
						this.storeService.update(store);
					}
				}
			}
		} else {
			System.out.println("系统关闭了邮件发送功能！");
		}
	}

	/**
	 * 免费短信发送方法，系统给用户发送的短信工具，
	 * 
	 * @param request
	 * @param mark
	 * @param mobile
	 * @param json
	 * @throws Exception
	 */
	@Async
	public void sendSmsFree(String web, String mark, String mobile, String json, String order_id) throws Exception {
		if (this.configService.getSysConfig().isSmsEnbale()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			if (template != null && template.isOpen()) {
				Map map = this.queryJson(json);
				if (mobile != null && !mobile.equals("")) {
					ExpressionParser exp = new SpelExpressionParser();
					EvaluationContext context = new StandardEvaluationContext();
					if (order_id != null) {
						OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
						User buyer = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
						context.setVariable("buyer", buyer);
						context.setVariable("config", this.configService.getSysConfig());
						context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
						context.setVariable("webPath", web);
						context.setVariable("order", order);
					}
					if (map.get("receiver_id") != null) {
						Long receiver_id = CommUtil.null2Long(map.get("receiver_id"));
						User receiver = this.userService.getObjById(receiver_id);
						context.setVariable("receiver", receiver);
					}
					if (map.get("sender_id") != null) {
						Long sender_id = CommUtil.null2Long(map.get("sender_id"));
						User sender = this.userService.getObjById(sender_id);
						context.setVariable("sender", sender);
					}
					if (map.get("buyer_id") != null) {
						Long buyer_id = CommUtil.null2Long(map.get("buyer_id"));
						User buyer = this.userService.getObjById(buyer_id);
						context.setVariable("buyer", buyer);
					}
					if (map.get("seller_id") != null) {
						Long seller_id = CommUtil.null2Long(map.get("seller_id"));
						User seller = this.userService.getObjById(seller_id);
						context.setVariable("seller", seller);
					}
					if (map.get("order_id") != null) {
						Long order_id_temp = CommUtil.null2Long(map.get("order_id"));
						OrderForm orderForm = this.orderFormService.getObjById(order_id_temp);
						context.setVariable("orderForm", orderForm);
					}
					if (map.get("childorder_id") != null) {
						Long childorder_id = CommUtil.null2Long(map.get("childorder_id"));
						OrderForm orderForm = this.orderFormService.getObjById(childorder_id);
						context.setVariable("child_orderForm", orderForm);
					}
					if (map.get("goods_id") != null) {
						Long goods_id = CommUtil.null2Long(map.get("goods_id"));
						Goods goods = this.goodsService.getObjById(goods_id);
						context.setVariable("goods", goods);
					}
					if (map.get("self_goods") != null) {
						context.setVariable("seller", map.get("self_goods").toString());
					}
					context.setVariable("config", this.configService.getSysConfig());
					context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
					context.setVariable("webPath", web);
					Expression ex = exp.parseExpression(template.getContent(), new SpelTemplate());
					String content = ex.getValue(context, String.class);
					boolean ret = this.sendSMS(mobile, content);
					if (ret) {
						System.out.println("发送短信成功");
					} else {
						System.out.println("发送短信失败");
					}
				}
			}
		} else {
			System.out.println("系统关闭了短信发送功能！");
		}
	}

	/**
	 * 免费邮件发送方法， 系统给用户发送的邮件工具，
	 * 
	 * @param request
	 * @param mark
	 * @param mobile
	 * @param json
	 * @throws Exception
	 */
	@Async
	public void sendEmailFree(String web, String mark, String email, String json, String order_id) throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			if (template != null && template.isOpen()) {
				Map map = this.queryJson(json);
				String subject = template.getTitle();
				ExpressionParser exp = new SpelExpressionParser();
				EvaluationContext context = new StandardEvaluationContext();
				if (order_id != null) {
					OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
					User buyer = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
					context.setVariable("buyer", buyer);
					context.setVariable("config", this.configService.getSysConfig());
					context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
					context.setVariable("webPath", web);
					context.setVariable("order", order);
				}
				if (map.get("receiver_id") != null) {
					Long receiver_id = CommUtil.null2Long(map.get("receiver_id"));
					User receiver = this.userService.getObjById(receiver_id);
					context.setVariable("receiver", receiver);
				}
				if (map.get("sender_id") != null) {
					Long sender_id = CommUtil.null2Long(map.get("sender_id"));
					User sender = this.userService.getObjById(sender_id);
					context.setVariable("sender", sender);
				}
				if (map.get("buyer_id") != null) {
					Long buyer_id = CommUtil.null2Long(map.get("buyer_id"));
					User buyer = this.userService.getObjById(buyer_id);
					context.setVariable("buyer", buyer);
				}
				if (map.get("seller_id") != null) {
					Long seller_id = CommUtil.null2Long(map.get("seller_id"));
					User seller = this.userService.getObjById(seller_id);
					context.setVariable("seller", seller);
				}
				if (map.get("order_id") != null) {
					Long order_id_temp = CommUtil.null2Long(map.get("order_id"));
					OrderForm orderForm = this.orderFormService.getObjById(order_id_temp);
					context.setVariable("orderForm", orderForm);
				}
				if (map.get("childorder_id") != null) {
					Long childorder_id = CommUtil.null2Long(map.get("childorder_id"));
					OrderForm orderForm = this.orderFormService.getObjById(childorder_id);
					context.setVariable("child_orderForm", orderForm);
				}
				if (map.get("goods_id") != null) {
					Long goods_id = CommUtil.null2Long(map.get("goods_id"));
					Goods goods = this.goodsService.getObjById(goods_id);
					context.setVariable("goods", goods);
				}
				if (map.get("self_goods") != null) {
					context.setVariable("seller", map.get("self_goods").toString());
				}
				context.setVariable("config", this.configService.getSysConfig());
				context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
				context.setVariable("webPath", web);
				Expression ex = exp.parseExpression(template.getContent(), new SpelTemplate());
				String content = ex.getValue(context, String.class);
				this.sendEmail(email, subject, content);
				System.out.println("发送邮件成功");
			} else {
				System.out.println("系统关闭了邮件发送功能");
			}
		}
	}

	/**
	 * 测试下单给用户发送邮件
	 * 
	 * @param request
	 * @param mark
	 * @param mobile
	 * @param json
	 * @throws Exception
	 */
	@Async
	public void sendEmail(String web, String mark, String email, String json, String order_id, User user)
			throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			if (template != null && template.isOpen()) {
				String[] str = email.split(",");
				for (String em : str) {
					Map map = this.queryJson(json);
					String subject = template.getTitle();
					String customName = template.getUser_name();
					// 创建SpEL表达式的解析器
					ExpressionParser exp = new SpelExpressionParser();
					// 如果使用其他的容器，则用下面的方法
					// 创建一个虚拟的容器EvaluationContext
					EvaluationContext context = new StandardEvaluationContext();
					if (order_id != null) {
						OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
						List<Map> goodsList = this.orderFormTools.queryGoodsInfo(order.getGoods_info());
						String goods_id = "";
						String goods_name = "";
						String goods_count = "";
						String goods_img = "";
						String goods_spec = "";
						Goods goods = null;
						for (Map obj : goodsList) {
							goods_name = obj.get("goods_name").toString();
							goods_count = obj.get("goods_count").toString();
							goods_img = this.configService.getSysConfig().getImageWebServer() + "/"
									+ obj.get("goods_mainphoto_path");
							goods_spec = obj.get("goods_color") + "," + obj.get("goods_gsp_val");
							goods_id = obj.get("goods_id").toString();
							goods = this.goodsService.getObjById(CommUtil.null2Long(goods_id));

						}
						User buyer = this.userService.getObjById(CommUtil.null2Long(order.getUser_id()));
						// 向容器内添加bean
						int[] list = new int[] { 10, 20, 30 };
						// context.setVariable("goodsList", list);
						context.setVariable("goodsList", goodsList);
						context.setVariable("goods", goods);
						context.setVariable("goods_name", goods_name);
						context.setVariable("goods_count", goods_count);
						context.setVariable("goods_img", goods_img);
						context.setVariable("goods_spec", goods_spec);
						context.setVariable("goods_id", goods_id);
						context.setVariable("order_total", order.getTotalPrice());
						context.setVariable("buyer", buyer.getUserName());
						context.setVariable("store", order.getStore_name());
						context.setVariable("order", order.getOrder_id());
						context.setVariable("orderPrice", order.getTotalPrice());
						context.setVariable("config", this.configService.getSysConfig());
						context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
						/* context.setVariable("webPath", web); */

					}
					if (map.get("receiver_id") != null) {
						Long receiver_id = CommUtil.null2Long(map.get("receiver_id"));
						User receiver = this.userService.getObjById(receiver_id);
						context.setVariable("receiver", receiver);
					}
					if (map.get("sender_id") != null) {
						Long sender_id = CommUtil.null2Long(map.get("sender_id"));
						User sender = this.userService.getObjById(sender_id);
						context.setVariable("sender", sender);
					}
					if (map.get("buyer_id") != null) {
						Long buyer_id = CommUtil.null2Long(map.get("buyer_id"));
						User buyer = this.userService.getObjById(buyer_id);
						context.setVariable("buyer", buyer);
					}
					if (map.get("seller_id") != null) {
						Long seller_id = CommUtil.null2Long(map.get("seller_id"));
						User seller = this.userService.getObjById(seller_id);
						context.setVariable("seller", seller);
					}
					if (map.get("order_id") != null) {
						Long order_id_temp = CommUtil.null2Long(map.get("order_id"));
						OrderForm orderForm = this.orderFormService.getObjById(order_id_temp);
						context.setVariable("orderForm", orderForm);
					}
					if (map.get("childorder_id") != null) {
						Long childorder_id = CommUtil.null2Long(map.get("childorder_id"));
						OrderForm orderForm = this.orderFormService.getObjById(childorder_id);
						context.setVariable("child_orderForm", orderForm);
					}
					if (map.get("goods_id") != null) {
						Long goods_id = CommUtil.null2Long(map.get("goods_id"));
						Goods goods = this.goodsService.getObjById(goods_id);
						context.setVariable("goods", goods);
					}
					if (map.get("self_goods") != null) {
						context.setVariable("seller", map.get("self_goods").toString());
					}
					if (null != user) {
						context.setVariable("userName", user.getUsername());
						context.setVariable("password", user.getPwd());
					}
					context.setVariable("config", this.configService.getSysConfig());
					context.setVariable("send_time", CommUtil.formatLongDate(new Date()));
					context.setVariable("webPath", web);
					System.out.println(template.getContent());
					// 表达式放置
					Expression ex = exp.parseExpression(template.getContent(), new TemplateParserContext());
					// 执行表达式，默认容器是spring本身的容器：ApplicationContext
					String content = ex.getValue(context, String.class);
					System.out.println("content :" + content);
					this.sendEmail(em, subject, content, customName);
					System.out.println("发送邮件成功");
				}
			} else {
				System.out.println("系统关闭了邮件发送功能");
			}
		}
	}

	/**
	 * @description velocity解析模板
	 * @param web
	 *            图片域名
	 * @param mark
	 *            邮件模板标识
	 * @param email
	 *            收件人email
	 * @param order_id
	 *            订单id
	 * @param user
	 *            收件人
	 * @throws Exception
	 */
	@Async
	public void sendJMail(String web, String mark, String email, List<Long> order_ids, User user) throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			VelocityEngine velocityEngine = new VelocityEngine();

			velocityEngine.init();

			Velocity.init();

			/* lets make a Context and put data into it */
			VelocityContext context = new VelocityContext();
			if (template != null && template.isOpen()) {
				String subject = template.getTitle();
				String customName = template.getUser_name();
				String content = template.getContent();
				Set<Long> set = new HashSet<Long>();
				/*
				 * for(Object id : order_ids){ set.add(CommUtil.null2Long(id));
				 * }
				 */
				Map params = new HashMap();
				params.put("order_ids", order_ids);
				List<OrderForm> orders = this.orderFormService
						.query("select obj from OrderForm obj where obj.id in (:order_ids)", params, -1, -1);
				context.put("webPath", web);
				context.put("orderFormTools", orderFormTools);
				context.put("orders", orders);
				if (user != null) {
					context.put("user", user);
				}
				StringWriter stringWriter = new StringWriter();
				try {
					Velocity.evaluate(context, stringWriter, "mystring", content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.sendEmail(email, subject, stringWriter.toString(), customName);
			}
		}
	}

	@Async
	public void sendJMail(String web, String mark, List<Long> order_ids, User user) throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			VelocityEngine velocityEngine = new VelocityEngine();

			velocityEngine.init();

			Velocity.init();

			/* lets make a Context and put data into it */
			VelocityContext context = new VelocityContext();
			if (template != null && template.isOpen()) {
				String subject = template.getTitle();
				String customName = template.getUser_name();
				String content = template.getContent();
				Set<Long> set = new HashSet<Long>();
				/*
				 * for(Object id : order_ids){ set.add(CommUtil.null2Long(id));
				 * }
				 */
				Map params = new HashMap();
				params.put("order_ids", order_ids);
				List<OrderForm> orders = this.orderFormService
						.query("select obj from OrderForm obj where obj.id in (:order_ids)", params, -1, -1);
				context.put("webPath", web);
				context.put("orderFormTools", orderFormTools);
				context.put("orders", orders);
				if (user != null) {
					context.put("user", user);
				}
				StringWriter stringWriter = new StringWriter();
				try {
					Velocity.evaluate(context, stringWriter, "mystring", content);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String user_email = "";
				Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
				Matcher matcher = emailPattern.matcher(CommUtil.null2String(user.getEmail()));
				if (user.getEmail() != null && !user.getEmail().equals("") && matcher.matches()) {// 邮箱验证不通过则不发送邮件
					user_email = user.getEmail();
				}
				this.sendEmail(user_email, subject, stringWriter.toString(), customName);
			}
		}
	}

	/**
	 * 
	 * @param web
	 * @param mark
	 * @param email
	 * @param order_id
	 * @param user
	 * @throws Exception
	 */
	@Async
	public void sendJMail(String web, String mark, String[] email, String order_id, User user, List<Map> list)
			throws Exception {
		if (this.configService.getSysConfig().isEmailEnable()) {
			Template template = this.templateService.getObjByProperty(null, "mark", mark);
			VelocityEngine velocityEngine = new VelocityEngine();

			velocityEngine.init();

			Velocity.init();

			/* lets make a Context and put data into it */
			VelocityContext context = new VelocityContext();
			if (template != null && template.isOpen() && !CommUtil.null2String(email).equals("")) {
				String subject = template.getTitle();
				String customName = template.getUser_name();
				String content = template.getContent();
				OrderForm order = this.orderFormService.getObjById(CommUtil.null2Long(order_id));
				List<Map> goodsList = new ArrayList<Map>();
				context.put("webPath", web);
				if (order != null) {
					goodsList = this.orderFormTools.queryGoodsInfo(order.getGoods_info());
					// context.put("objs", goodsList);
					context.put("order", order);
				} else {
					goodsList = list;
				}
				context.put("objs", goodsList);
				if (user != null) {
					context.put("user", user);
				}

				if (!context.equals("")) {
					StringWriter stringWriter = new StringWriter();
					Velocity.evaluate(context, stringWriter, "mystring", content);
					this.sendEmail(email, subject, stringWriter.toString(), customName);
				}
			}
		}
	}

	/**
	 * 发送短信底层工具
	 * 
	 * @param mobile
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public boolean sendSMS(String mobile, String content) throws UnsupportedEncodingException {
		boolean result = true;
		if (this.configService.getSysConfig().isSmsEnbale()) {
			String url = this.configService.getSysConfig().getSmsURL();
			String userName = this.configService.getSysConfig().getSmsUserName();
			String password = this.configService.getSysConfig().getSmsPassword();
			SmsBase sb = new SmsBase(Globals.DEFAULT_SMS_URL, userName, password);// 固定硬编码短信发送接口
			String ret = sb.SendSms(mobile, content);
			if (ret.equals("0")) {
				result = true;
			} else {
				result = false;
			}
			/*
			 * if (!ret.substring(0, 3).equals("000")) { result = false; }
			 */
		} else {
			result = false;
			System.out.println("系统关闭了短信发送功能");
		}
		return result;
	}

	/**
	 * 发送邮件底层工具
	 * 
	 * @param mobile
	 * @param content
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public boolean sendEmail(String email, String subject, String content) {
		boolean ret = true;
		if (this.configService.getSysConfig().isEmailEnable()) {
			String username = "";
			String password = "";
			String smtp_server = "";
			String from_mail_address = "";
			username = this.configService.getSysConfig().getEmailUserName();
			password = this.configService.getSysConfig().getEmailPws();
			smtp_server = this.configService.getSysConfig().getEmailHost();
			from_mail_address = this.configService.getSysConfig().getEmailUser();

			String to_mail_address = email;
			if (username != null && password != null && !username.equals("") && !password.equals("")
					&& smtp_server != null && !smtp_server.equals("") && to_mail_address != null
					&& !to_mail_address.trim().equals("")) {
				Authenticator auth = new PopupAuthenticator(username, password);
				Properties mailProps = new Properties();
				// 属性mail.smtp.auth设置发送时是否校验用户名和密码
				// 属性mail.transport.protocol设置要使用的邮件协议
				// 属性mail.host表示发送服务器的邮件服务器地址
				mailProps.put("mail.smtp.auth", "true");
				mailProps.put("username", username);
				mailProps.put("password", password);
				mailProps.put("mail.smtp.host", smtp_server);
				Session mailSession = Session.getInstance(mailProps, auth);
				MimeMessage message = new MimeMessage(mailSession);
				try {
					// message.setFrom(new InternetAddress(from_mail_address));
					try {
						message.setFrom(new InternetAddress(from_mail_address, "Soarmall"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(to_mail_address));
					message.setSubject(subject);
					MimeMultipart multi = new MimeMultipart("related");
					BodyPart bodyPart = new MimeBodyPart();
					bodyPart.setDataHandler(new DataHandler(content, "text/html;charset=UTF-8"));// 网页格式
					// bodyPart.setText(content);
					multi.addBodyPart(bodyPart);
					message.setContent(multi);
					message.saveChanges();
					Transport.send(message);// 指定smtp服务器的路径和端口的
					ret = true;
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					ret = false;
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					ret = false;
					e.printStackTrace();
				}
			} else {
				ret = false;
			}
		} else {
			ret = false;
			System.out.println("系统关闭了邮件发送功能");
		}
		return ret;
	}

	/**
	 * @desctiption 发送邮件底层工具 - 自定义发送人
	 * @param email
	 * @param subject
	 * @param content
	 * @param customName
	 * @return
	 */
	public boolean sendEmail(String email, String subject, String content, String customName) {
		boolean ret = true;
		if (this.configService.getSysConfig().isEmailEnable()) {
			String username = "";
			String password = "";
			String smtp_server = "";
			String from_mail_address = "";
			username = this.configService.getSysConfig().getEmailUserName();
			password = this.configService.getSysConfig().getEmailPws();
			smtp_server = this.configService.getSysConfig().getEmailHost();
			from_mail_address = this.configService.getSysConfig().getEmailUser();
			String to_mail_address = email;
			List<String> emails = new ArrayList<String>();
			emails.add("460751446@qq.com");
			emails.add("1223414075@qq.com");
			emails.add("11943732@qq.com");
			// emails.add(email);
			InternetAddress internetAddress[] = new InternetAddress[emails.size()];
			for (int i = 0; i < emails.size(); i++) {
				try {
					internetAddress[i] = new InternetAddress(emails.get(i));
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// && to_mail_address != null
			// && !to_mail_address.trim().equals("")
			if (username != null && password != null && !username.equals("") && !password.equals("")
					&& smtp_server != null && !smtp_server.equals("")) {
				Authenticator auth = new PopupAuthenticator(username, password);
				Properties mailProps = new Properties();// 定义properties对象，设置环境信息
														// //创建邮件对象
				// 属性mail.smtp.auth设置发送时是否校验用户名和密码
				// 属性mail.transport.protocol设置要使用的邮件协议
				// 属性mail.host表示发送服务器的邮件服务器地址
				mailProps.put("mail.smtp.auth", "true");// 指定是否需要SMTP验证
				mailProps.put("username", username);
				mailProps.put("password", password);
				mailProps.put("mail.smtp.host", smtp_server);
				// 用session对象来创建并初始化邮件对象
				Session mailSession = Session.getInstance(mailProps, auth);
				// 置true可以在控制台（console)上看到发送邮件的过程
				mailSession.setDebug(true);
				MimeMessage message = new MimeMessage(mailSession);// 建立邮件消息
				try {
					// message.setFrom(new InternetAddress(from_mail_address));
					try {
						// 2.设置发件人
						// 其中 InternetAddress 的三个参数分别为: 邮箱, 显示的昵称(只用于显示,
						// 没有特别的要求), 昵称的字符集编码
						// mimeMessage.setFrom(new
						// InternetAddress("from_mail_address","customName","UTF-8"));
						// 设置发件人
						message.setFrom(new InternetAddress(from_mail_address, customName)); // 自定义发送者
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 邮件回复人
					message.setReplyTo(new Address[] { new InternetAddress(from_mail_address) });
					// 设置多个收件人
					/*
					 * message.setRecipients(Message.RecipientType.TO,
					 * internetAddress);
					 */
					if (to_mail_address != null && !to_mail_address.equals("")) {
						message.setRecipient(Message.RecipientType.TO, new InternetAddress(to_mail_address));// 接受者
					}

					// 抄送人 密送：BCC 抄送地址：Message.RecipientType.CC
					// Message.RecipientType.TO
					message.addRecipients(MimeMessage.RecipientType.BCC, internetAddress);

					message.setSubject(subject);// 发送的标题
					// 一个Multipart对象包含一个或多个bodypart对象，组成邮件正文
					MimeMultipart multi = new MimeMultipart("related");
					// 创建文本节点
					BodyPart bodyPart = new MimeBodyPart();
					// 网页格式
					bodyPart.setDataHandler(new DataHandler(content, "text/html;charset=UTF-8"));
					// bodyPart.setText(content);
					multi.addBodyPart(bodyPart);

					message.setContent(multi); // 以html格式发送 发送的内容
					// 保存邮件
					message.saveChanges();
					Transport.send(message);// 指定smtp服务器的路径和端口的
					ret = true;
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					ret = false;
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					ret = false;
					e.printStackTrace();
				}
			} else {
				ret = false;
			}
		} else {
			ret = false;
			System.out.println("系统关闭了邮件发送功能");
		}
		return ret;
	}

	@Async
	public void sendEmail(String[] email, String subject, String content, String customName) {
		boolean ret = true;
		if (this.configService.getSysConfig().isEmailEnable()) {
			String username = "";
			String password = "";
			String smtp_server = "";
			String from_mail_address = "";
			username = this.configService.getSysConfig().getEmailUserName();
			password = this.configService.getSysConfig().getEmailPws();
			smtp_server = this.configService.getSysConfig().getEmailHost();
			from_mail_address = this.configService.getSysConfig().getEmailUser();
			InternetAddress internetAddress[] = new InternetAddress[email.length];
			for (int i = 0; i < email.length; i++) {
				try {
					internetAddress[i] = new InternetAddress(email[i]);
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!CommUtil.null2String(username).equals("") && !CommUtil.null2String(password).equals("")
					&& !CommUtil.null2String(smtp_server).equals("") && email.length > 0) {
				Authenticator auth = new PopupAuthenticator(username, password);
				Properties properties = new Properties();
				properties.put("mail.smtp.auth", "true");// 指定是否需要SMTP验证
				properties.put("username", username);
				properties.put("password", password);
				properties.put("mail.smtp.host", smtp_server);
				// 用session对象来创建并初始化邮件对象
				Session mailSession = Session.getInstance(properties, auth);
				// 置true可以在控制台（console)上看到发送邮件的过程
				mailSession.setDebug(true);
				MimeMessage message = new MimeMessage(mailSession);// 建立邮件消息
				// MimeMessageHelper helper = new MimeMessageHelper(message,
				// "UTF-8");
				try {
					// 设置发件人
					// 其中 InternetAddress 的三个参数分别为: 邮箱, 显示的昵称(只用于显示, 没有特别的要求),
					// 昵称的字符集编码
					message.setFrom(new InternetAddress(from_mail_address, customName)); // 自定义发送者
					// 收件人
					message.setReplyTo(internetAddress);
					message.setRecipients(Message.RecipientType.TO, internetAddress);
					// 发送的标题
					message.setSubject(subject);

					// 一个Multipart对象包含一个或多个bodypart对象，组成邮件正文
					MimeMultipart multi = new MimeMultipart("related");
					// 创建文本节点
					BodyPart bodyPart = new MimeBodyPart();
					// 网页格式
					bodyPart.setDataHandler(new DataHandler(content, "text/html;charset=UTF-8"));
					// bodyPart.setText(content);
					multi.addBodyPart(bodyPart);

					message.setContent(multi); // 以html格式发送 发送的内容

					// 保存邮件
					message.saveChanges();
					Transport.send(message);// 指定smtp服务器的路径和端口的
					ret = true;
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 解析json工具
	 * 
	 * @param json
	 * @return
	 */
	private Map queryJson(String json) {
		Map map = new HashMap();
		if (json != null && !json.equals("")) {
			map = Json.fromJson(HashMap.class, json);
		}
		return map;
	}

	/*
	 * SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,需要改为对应邮箱的 SMTP
	 * 服务器的端口, 具体可查看对应邮箱服务的帮助, QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
	 */
	/*
	 * final String smtpPort = "465"; props.setProperty("mail.smtp.port",
	 * smtpPort); props.setProperty("mail.smtp.socketFactory.class",
	 * "javax.net.ssl.SSLSocketFactory");
	 * props.setProperty("mail.smtp.socketFactory.fallback", "false");
	 * props.setProperty("mail.smtp.socketFactory.port", smtpPort);
	 */
}
