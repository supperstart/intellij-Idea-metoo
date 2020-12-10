package com.metoo.msg.email;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.domain.Template;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.ITemplateService;
import com.metoo.manage.admin.tools.OrderFormTools;
import com.metoo.msg.MsgTools;

@Controller
public class VelocityController {
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private VelocitySendEmail sendEmail;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private SendEmailService sendEmailService;
	@Autowired
	private ITemplateService templateService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private OrderFormTools orderFormTools;
	@Autowired
	private MsgTools msgTools;
	
	@RequestMapping("sendEmail.json")
	public void sendEmail(HttpServletRequest request, HttpServletResponse response) {

		try {
			// this.sendEmail.sendEmail2("460751446@qq.com", "soarmall",
			// "soarmall", "");
			// "460751446@qq.com","2906205882@qq.com","11943732@qq.com","1393813658@qq.com"
			String email[] = { "460751446@qq.com", "11943732@qq.com" };
			this.sendEmailService.sendEmail(email);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 拼接模板文件调用底层发送邮件接口
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("test_sendEmail.json")
	public void testVolecity(HttpServletRequest request, HttpServletResponse response) throws Exception {
		VelocityEngine velocityEngine = new VelocityEngine();

		velocityEngine.init();

		Velocity.init();

		/* lets make a Context and put data into it */
		VelocityContext context = new VelocityContext();
/*		OrderForm order = orderFormService.getObjById(CommUtil.null2Long(2610));
		if(order != null){
			List<Map> goodsList = this.orderFormTools.queryGoodsInfo(order.getGoods_info());
			context.put("objs", goodsList);
			context.put("order", order);
			context.put("userName", "hkk");
			context.put("webServer", configService.getSysConfig().getImageWebServer());
//			String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "mail_template.vm",
//					"UTF-8", modal);
		}*/
		Set<Long> set = new HashSet<Long>();
		set.add(CommUtil.null2Long(2716));
		Map params = new HashMap();
		params.put("order_ids", set);
		List<OrderForm> orders =  this.orderFormService.query("select obj from OrderForm obj where obj.id in (:order_ids)", params, -1, -1);
		
		context.put("orders", orders);
		context.put("webPath", this.configService.getSysConfig().getImageWebServer());
		context.put("orderFormTools", orderFormTools);
		
		context.put("name", "VelocityTest");
		context.put("project", "Jakarta");
		context.put("now", new Date());
		context.put("dateFormatUtils", new org.apache.commons.lang.time.DateFormatUtils());

		/* lets make our own string to render */
		Template template = this.templateService.getObjByProperty(null, "mark", "goods_cart2");
		String str = template.getContent();
		
		//String str = "We are using $project $name to render this. 中文测试  $!dateFormatUtils.format($!now,'yyyy-MM-dd')";
		StringWriter stringWriter = new StringWriter();
		Velocity.evaluate(context, stringWriter, "mystring", str);
		String email[] = { "460751446@qq.com" };
		
		//this.sendEmailService.sendVelocityContextEmail(email, stringWriter.toString());
		
		this.msgTools.sendEmail(email, "testEmail", stringWriter.toString(), "Soarmall");
	}
}
