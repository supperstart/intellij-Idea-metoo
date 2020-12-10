package com.metoo.module.weixin.view.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.core.service.IQueryService;
import com.metoo.core.tools.CommUtil;
import com.metoo.core.weixin.utils.PayCommonUtil;
import com.metoo.core.weixin.utils.WeixinUtil;
import com.metoo.foundation.domain.GroupJoiner;
import com.metoo.foundation.domain.IntegralLog;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.OrderFormLog;
import com.metoo.foundation.domain.Payment;
import com.metoo.foundation.domain.SysConfig;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.domain.wechat.WeixinTemplate;
import com.metoo.foundation.domain.wechat.WeixinTemplateParam;
import com.metoo.foundation.service.IGroupJoinerService;
import com.metoo.foundation.service.IIntegralLogService;
import com.metoo.foundation.service.IOrderFormLogService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.IPaymentService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserService;
import com.metoo.module.weixin.view.tools.OrderPayTools;

@Controller
public class WeixinOrderPayAction  {
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderFormLogService orderFormLogService;
	@Autowired
	private IIntegralLogService integralLogService;
	@Autowired
	private IPaymentService paymentService;
	@Autowired
	private IQueryService queryService;
	@Autowired
	private IGroupJoinerService groupJoinerService;
	@Autowired
	private OrderPayTools orderPayTools;
	
	private String nonceStr = "";//随机串
	private Logger log = Logger.getLogger(WeixinOrderPayAction.class);
	
	@RequestMapping("/wap/pay_order.htm")
	public void pay_order(HttpServletRequest request,
			HttpServletResponse response,String orderId){
		OrderForm order = this.orderFormService.getObjById(Long.valueOf(orderId));
		User user = SecurityUserHolder.getCurrentUser();
		SysConfig config = configService.getSysConfig();
		
		String notify_url = "http://wx.fensekaola.com/wap/weixin_return.htm";
		
		
		SortedMap<String,Object> parameters = new TreeMap<String, Object>();
		parameters.put("appId", config.getWeixin_appId());
		parameters.put("nonceStr", nonceStr); // 必填，生成签名的随机串
		parameters.put("key", config.getWeixin_mch_key());//商户密钥
		Map<String, String> map = weixinPrePay(user.getOpenId(),notify_url,config,order,request);  
		try {
			if("SUCCESS".equalsIgnoreCase(map.get("result_code"))){//统一下单api返回业务结果
				
				if(map != null){
					parameters.put("package", "prepay_id="+map.get("prepay_id"));//订单详情扩展字符串
				}
				parameters.put("signType", "MD5");//签名方式
				parameters.put("timeStamp",String.valueOf(System.currentTimeMillis() / 1000));//生成签名的时间戳
				
				//String paySign = PayCommonUtil.seconedCreateSign("UTF-8",parameters);
				String paySign = PayCommonUtil.createSign("UTF-8", parameters,config.getWeixin_mch_key());
				
				parameters.put("paySign", paySign);//签名
				
			}
			
//		String sendUrl = "http://wx.fensekaola.com/wap/buyer/center.htm";
//		if (order.getOrder_cat() == 3) {
//			
//			sendUrl = "http://wx.fensekaola.com/wap/goods.htm?id=" + order.get;
////		}
//			
		
		
		
		String send_url = "http://wx.fensekaola.com/wap/buyer/center.htm";
		
		if (order.getOrder_cat() == 3) {
			String sqls = "SELECT a.gg_goods_id from metoo_group_goods a " +
					"where a.id in ( " +
					"SELECT b.rela_group_goods_id from metoo_group_joiner b " + 
					"where b.rela_order_form_id=:orderId)";
			Map<String, Object> pMap = new HashMap<String, Object>();
			pMap.put("orderId", orderId);
			List list = this.queryService.nativeQuery(sqls, pMap, -1, -1);
			long goodsId = 0L;
			if (null != list && 0 < list.size()) {
				
				//Object[] ao = ()list.get(0);
				BigInteger bi = (BigInteger)list.get(0);
				goodsId = bi.longValue();
			}
			
			send_url = "http://wx.fensekaola.com/wap/goods.htm?id=" + goodsId;
		}
//		
		parameters.put("sendUrl", send_url);//支付后返回到用户中心页面中
			
		parameters.put("result_code", map.get("result_code"));//返回状态码
		parameters.put("err_code_des", map.get("err_code_des"));//错误描述
		String json = Json.toJson(parameters, JsonFormat.compact());
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer;
		
			writer = response.getWriter();
			writer.print(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@RequestMapping("/wap/pay_refund.htm")
	public void pay_refund(HttpServletRequest request,
			HttpServletResponse response,String orderId){
		OrderForm order = this.orderFormService.getObjById(Long.valueOf(orderId));
		SysConfig config = configService.getSysConfig();
		
		
		
		SortedMap<String,Object> parameters = new TreeMap<String, Object>();
		parameters.put("appId", config.getWeixin_appId());
		parameters.put("nonceStr", nonceStr); // 必填，生成签名的随机串
		parameters.put("key", config.getWeixin_mch_key());//商户密钥
		Map<String, String> map = weixinPayRefund(config, order);  
		try {
			if("SUCCESS".equalsIgnoreCase(map.get("return_code"))){//统一下单api返回业务结果
				
				order.setOrder_status(70);
				order.setRefund_fee(Integer.parseInt(map.get("refund_fee")));
				order.setRefund_id(map.get("refund_id"));
				order.setRefund_out_no(map.get("out_refund_no"));
				
					
//				
//				//String paySign = PayCommonUtil.seconedCreateSign("UTF-8",parameters);
//				String paySign = PayCommonUtil.createSign("UTF-8", parameters,config.getWeixin_mch_key());
//				
//				parameters.put("paySign", paySign);//签名
				
			} else {
				
				order.setOrder_status(75);
			}
			orderFormService.update(order);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
//		String sendUrl = "http://wx.fensekaola.com/wap/buyer/center.htm";
//		if (order.getOrder_cat() == 3) {
//			
//			sendUrl = "http://wx.fensekaola.com/wap/goods.htm?id=" + order.get;
////		}
//			
		
		
		
		
	}
	
	public Map<String, String> weixinPrePay(String openid,String notify_url,SysConfig config,OrderForm order, HttpServletRequest request) { 
        SortedMap<String, Object> parameterMap = new TreeMap<String, Object>();  
        parameterMap.put("appid", config.getWeixin_appId());  
        parameterMap.put("mch_id", config.getMch_id());  
        //随机串
        nonceStr = PayCommonUtil.getRandomString(32);
        parameterMap.put("nonce_str", nonceStr);  
        parameterMap.put("body", order.getId().toString());
        parameterMap.put("out_trade_no", order.getOrder_id());
		
        parameterMap.put("fee_type", "CNY");  
        BigDecimal total = order.getTotalPrice().multiply(new BigDecimal(100));  
        java.text.DecimalFormat df=new java.text.DecimalFormat("0");  
       parameterMap.put("total_fee", df.format(total)); 
      //  parameterMap.put("total_fee", "1");   测试使用1分钱支付
        parameterMap.put("spbill_create_ip", request.getRemoteAddr());  
        parameterMap.put("notify_url", notify_url);
        parameterMap.put("trade_type", "JSAPI");
        //trade_type为JSAPI是 openid为必填项
        parameterMap.put("openid", openid);
        String sign = "";
        try {
			sign = PayCommonUtil.createSign("UTF-8", parameterMap,config.getWeixin_mch_key());
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		} 
        parameterMap.put("sign", sign); 
        System.out.println("sign : "+ sign);
        String requestXML = PayCommonUtil.getRequestXml(parameterMap);  
        System.out.println(requestXML);  
        String result = PayCommonUtil.httpsRequest(  
                "https://api.mch.weixin.qq.com/pay/unifiedorder", "POST",  
                requestXML);  
        System.out.println(result);  
        Map<String, String> map = null;  
        try {  
            map = PayCommonUtil.doXMLParse(result);  
        } catch (JDOMException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return map;        
    }
	
	//微信返回结果
	@RequestMapping("/wap/weixin_return.htm")
	public String notify(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String result;//返回给微信的处理结果
		String inputLine;
		String notityXml = "";
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		//微信给返回的东西
		try {
			while ((inputLine = request.getReader().readLine()) != null) {
				notityXml += inputLine;
			}
			request.getReader().close();
		} catch (Exception e) {
			e.printStackTrace();
			result = setXml("fail","xml获取失败");
		}
		if (StringUtils.isEmpty(notityXml)) {
			result = setXml("fail","xml为空");
		}
		Map map = PayCommonUtil.doXMLParse(notityXml);
		// 解析各种数据
		String appid = (String) map.get("appid");//应用ID
		String attach = (String) map.get("attach");//商家数据包
		String bank_type = (String) map.get("bank_type");//付款银行
		String cash_fee = (String) map.get("cash_fee");//现金支付金额
		String fee_type = (String) map.get("fee_type");//货币种类
		String is_subscribe = (String) map.get("is_subscribe");//是否关注公众账号
		String mch_id = (String) map.get("mch_id");//商户号
		String nonce_str = (String) map.get("nonce_str");//随机字符串
		String openid = (String) map.get("openid");//用户标识
		String out_trade_no = (String) map.get("out_trade_no");// 获取商户订单号
		String result_code = (String) map.get("result_code");// 业务结果
		String return_code = (String) map.get("return_code");// SUCCESS/FAIL
		String sign = (String) map.get("sign");// 获取签名
		String time_end = (String) map.get("time_end");//支付完成时间
		String total_fee = (String) map.get("total_fee");// 获取订单金额
		String trade_type = (String) map.get("trade_type");//交易类型
		String transaction_id = (String) map.get("transaction_id");//微信支付订单号
		
		System.out.println("**************************************************************************************************");
		System.out.println(appid+"-------------------应用ID");
		System.out.println(attach+"-------------------商家数据包");
		System.out.println(bank_type+"-------------------付款银行");
		System.out.println(cash_fee+"-------------------现金支付金额");
		System.out.println(fee_type+"-------------------货币种类");
		System.out.println(is_subscribe+"-------------------是否关注公众账号");
		System.out.println(mch_id+"-------------------商户号");
		System.out.println(nonce_str+"-------------------随机字符串");
		System.out.println(openid+"-------------------用户标识");
		System.out.println(out_trade_no+"-------------------获取商户订单号");
		System.out.println(result_code+"-------------------业务结果");
		System.out.println(return_code+"------------------- SUCCESS/FAIL");
		System.out.println(sign+"-------------------获取签名-微信回调的签名");
		System.out.println(time_end+"-------------------支付完成时间");
		System.out.println(total_fee+"-------------------获取订单金额");
		System.out.println(trade_type+"-------------------交易类型");
		System.out.println(transaction_id+"-------------------微信支付订单号");
		System.out.println("**************************************************************************************************");
		
		if("SUCCESS".equalsIgnoreCase(result_code)){//业务结果为success 处理订单信息
			//获取订单信息
			OrderForm main_order = null;
			Map<String,Object> pmap = new HashMap<String, Object>();
			pmap.put("order_id", out_trade_no);
			String sql = "select obj from OrderForm obj where order_id = :order_id";
			List<OrderForm> orders = this.orderFormService.query(sql, pmap, 0, 1);
			if(orders != null){
				main_order = orders.get(0);
			}
			//获取用户信息
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", openid);
			List<User> userList = this.userService.query("select obj from User obj where obj.openId = :id ", params, -1, -1);
			User user = null;
			if(userList.size()>0){
				user = userList.get(0);
			}

			if (main_order != null
					&& main_order.getOrder_status() < 20) {// 异步没有出来订单，则同步处理订单
				try {
					main_order.setOut_order_id(transaction_id);//外部订单号 ,微信支付订单号
					SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");//小写的mm表示的是分钟  
					main_order.setPayTime(sdf.parse(time_end));//支付完成时间 
					if (main_order.getOrder_cat() == 3) {
						main_order.setOrder_status(66);//团购订单状态改为已付款未成团
					} else {
						
						main_order.setOrder_status(20);//订单状态改为已付款待发货
					}
					
					Payment pm  = this.paymentService.getObjByProperty(null, "mark", "wx_app");
					main_order.setPayment(pm);
					this.orderFormService.update(main_order);
					
					System.out.println("order_id: " + out_trade_no);
					System.out.println("order_form_key_id: " + main_order.getId());
					long moId = main_order.getId();
					//更新对应的团购状态
					String sqls = "update metoo_group_joiner a set a.joiner_count=a.joiner_count+1 where a.child_group_id in ( " +
							"select b.child_group_id from " +
							"(select t.child_group_id from metoo_group_joiner t where t.rela_order_form_id=:orderId) b)";
					pmap.clear();
					pmap.put("orderId", moId);
					this.queryService.executeNativeSQL(sqls, pmap);
					
					sqls = "update metoo_group_joiner t set t.status='1'" +
							" where t.rela_order_form_id=:orderId";
					

					this.queryService.executeNativeSQL(sqls, pmap);

					
					//更新商品库存，如果是团购商品，则更新团购库存
					orderPayTools.update_goods_inventory(main_order);
					OrderFormLog main_ofl = new OrderFormLog();
					main_ofl.setAddTime(new Date());
					main_ofl.setLog_info("微信支付");
					main_ofl.setLog_user(user);
					main_ofl.setOf(main_order);
					this.orderFormLogService.save(main_ofl);
				
					
					int integral  = ((Math.round(Float.parseFloat(total_fee)/100)) <= 0 ? 1 : Math.round(Float.parseFloat(total_fee)/100));
					
					//团购更新团购表积分，给团长增加积分
					if (main_order.getOrder_cat() == 3) {  //订单类型为团购
						
						sqls = "SELECT a.id from metoo_group_joiner a, " +
								"(SELECT t1.child_group_id from metoo_group_joiner t1, metoo_orderform t2 where " +
								"t1.rela_order_form_id=t2.id " +
								"and t2.order_id=:order_id " +
								") b " +
								"where a.child_group_id=b.child_group_id";
							//	+ "and a.is_group_creator=1";
						pmap.clear();
						pmap.put("order_id", main_order.getOrder_id());
						List list = this.queryService.nativeQuery(sqls, pmap, -1, -1);
						if (null != list && 0 < list.size()) {
							
							long id = ((BigInteger)list.get(0)).longValue();
							GroupJoiner gj = groupJoinerService.getObjById(id);
							if (gj.getRela_order_form_id() == main_order.getId()) {
								
								gj.setAdd_integral(CommUtil.null2LongNew(gj.getAdd_integral()) + integral);
								groupJoinerService.update(gj);
							} else {
								
								if ("1".equals(gj.getIs_group_creator())) {
//									String userId = gj.getUser_id();
//									
//									if (Long.parseLong(userId) != user.getId()) {
////										User u = this.userService.getObjById(Long.parseLong(userId));
////										if (null != u) {
////											
////											u.setIntegral(CommUtil.null2Int(u.getIntegral()) + integral);
////											this.userService.save(u);
////										}
										
										gj.setAdd_integral(CommUtil.null2LongNew(gj.getAdd_integral()) + integral);
										groupJoinerService.update(gj);
//									}
									
								}
								
							}
						}
						
					} else {
						
						//增加订单金额同等积分 
						
						user.setIntegral(user.getIntegral() + integral);
						this.userService.save(user);
						
						if(integral > 0){
							IntegralLog log = new IntegralLog();
							log.setAddTime(new Date());
							log.setContent("用户消费增加"
									+ integral + "分");
							log.setIntegral(integral);
							log.setIntegral_user(user);
							log.setType("order");
							this.integralLogService.save(log);
						
							//父用户增加积分
							User parent_user =  user.getParent();
							if(parent_user != null){
								parent_user.setIntegral(parent_user.getIntegral() + integral);
								this.userService.save(parent_user);
								
								IntegralLog log1 = new IntegralLog();
								log1.setAddTime(new Date());
								log1.setContent("子用户消费增加"
										+ integral + "分");
								log1.setIntegral(integral);
								log1.setIntegral_user(parent_user);
								log1.setType("chind_order");
								this.integralLogService.save(log1);
							}
							
							//父用户的上级用户增加50%积分
							User grant_user =  user.getParent().getParent();
							if(grant_user != null){
								Integer ti =Math.round(integral/2);
								grant_user.setIntegral( grant_user.getIntegral() + ti);
								this.userService.save(grant_user);
								
								IntegralLog log2 = new IntegralLog();
								log2.setAddTime(new Date());
								log2.setContent("第三级用户消费增加"
										+ ti + "分");
								log2.setIntegral(ti);
								log2.setIntegral_user(grant_user);
								log2.setType("third_order");
								this.integralLogService.save(log2);
							}
							
						}
					}

					//微信用户支付订单成功后发送模板
					WeixinTemplate tem=new WeixinTemplate();
					tem.setTemplateId("88_o40vOQtDcv-Uw4NETXyhDfFyFh7irWWhPIdDXD-M");
					tem.setTopColor("#00DD00");
					tem.setToUser(openid);
					tem.setUrl("http://wx.fensekaola.com/wap/buyer/center.htm?op=center"); 
					  
					List<WeixinTemplateParam> paras=new ArrayList<WeixinTemplateParam>();
					paras.add(new WeixinTemplateParam("first","您好，您的订单已提交成功","#FF3333"));
					paras.add(new WeixinTemplateParam("keyword1",main_order.getOrder_id(),"#0044BB"));
					paras.add(new WeixinTemplateParam("keyword2",main_order.getAddTime().toString(),"#0044BB"));
					paras.add(new WeixinTemplateParam("keyword3",String.valueOf(Float.parseFloat(total_fee)/100)+"元","#0044BB"));
					paras.add(new WeixinTemplateParam("keyword4",bank_type,"#0044BB"));
					paras.add(new WeixinTemplateParam("Remark","感谢你对我们商城的支持!!!!","#AAAAAA"));
							
					tem.setTemplateParamList(paras);
							
					boolean tempResult=WeixinUtil.sendTemplateMsg(configService.getSysConfig().getWeixin_token(),tem);
					
					System.out.println("........................");
					System.out.println(tempResult);

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}else{
				System.out.println("订单信息不正确");
				log.info("订单信息不正确");
			}
			
		}else{
			System.out.println("fail 支付失败");
			log.info("fail 支付失败");
		}
		return "redirect:http://wx.fensekaola.com/wap/buyer/center.htm";
	}
	
	//通过xml 发给微信消息
	public static String setXml(String return_code, String return_msg) {
		SortedMap<String, String> parameters = new TreeMap<String, String>();
		parameters.put("return_code", return_code);
		parameters.put("return_msg", return_msg);
		return "<xml><return_code><![CDATA[" + return_code + "]]>" + 
				"</return_code><return_msg><![CDATA[" + return_msg + "]]></return_msg></xml>";
	}
	
	
	
	/**
	 * 微信退款
	 * @param config
	 * @param order
	 * @param request
	 * @return
	 */
	public Map<String, String> weixinPayRefund(SysConfig config, OrderForm order) { 
        SortedMap<String, Object> parameterMap = new TreeMap<String, Object>();  
        parameterMap.put("appid", config.getWeixin_appId());
        String mch_id = config.getMch_id();
        parameterMap.put("mch_id", mch_id);  
        //随机串
        nonceStr = PayCommonUtil.getRandomString(32);
        parameterMap.put("nonce_str", nonceStr);
        parameterMap.put("out_trade_no", order.getOrder_id());
   //     parameterMap.put("transaction_id", order.getOut_order_id());
        String out_refund_no = SecurityUserHolder.getCurrentUser()
				.getId() + CommUtil.formatTime("yyyyMMddhhmmssSSS",
						new Date());
        parameterMap.put("out_refund_no", out_refund_no);
        
        BigDecimal total = order.getTotalPrice().multiply(new BigDecimal(100));  
        java.text.DecimalFormat df=new java.text.DecimalFormat("0");  
        String totalFee = df.format(total);
        parameterMap.put("total_fee", totalFee); 
        parameterMap.put("refund_fee", totalFee); 
        parameterMap.put("refund_fee_type", "CNY");  
        
      //  parameterMap.put("total_fee", "1");   测试使用1分钱支付
        String sign = "";
        try {
			sign = PayCommonUtil.createSign("UTF-8", parameterMap,config.getWeixin_mch_key());
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		} 
        parameterMap.put("sign", sign); 
        System.out.println("sign : "+ sign);
        String requestXML = PayCommonUtil.getRequestXml(parameterMap);  
        System.out.println("退款发起xml: " + requestXML);  
        Map<String, String> map = null;  
        try {  
        	String result = PayCommonUtil.executeBySslPost("https://api.mch.weixin.qq.com/secapi/pay/refund", requestXML,
        		"D:/cert/apiclient_cert.p12", mch_id);
	        System.out.println("退款结果xml: " + result);  
	       
            map = PayCommonUtil.doXMLParse(result);  
        } catch (JDOMException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  catch (Exception ex) {
        	
        	ex.printStackTrace();
        }
        return map;        
    }
	

}