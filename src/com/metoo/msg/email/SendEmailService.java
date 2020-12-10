package com.metoo.msg.email;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.manage.admin.tools.OrderFormTools;
/**
 * jmail是一种服务器端的邮件发送组件，和个人用的客户端邮件软件不一样的。jmail是在服务器上给程序用来发邮件用的，除了软件编程人员，其他人一般平常用不上。
注册时的激活邮件的发送用到的就是它。
 * @author 46075
 *
 */
public class SendEmailService {
	
	private JavaMailSender sender;
	
	private VelocityEngine velocityEngine;
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IOrderFormService orderFormService;
	@Autowired
	private OrderFormTools orderFormTools;
	
	/**
	 * 使用volecity .vm模板下单
	 * @param email
	 */
	@Async
	public void sendEmail(final String[] email) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				// 设置内容显示的编码格式
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
				String to[] = email;
				InternetAddress[] sendTo = new InternetAddress[to.length];
				for(int i=0; i<to.length; i++){
					sendTo[i] = new InternetAddress(to[i]);
				}
				helper.setTo(sendTo);
				helper.setFrom("service@soarmall.com");
				Map modal = new HashMap();
				OrderForm order = orderFormService.getObjById(CommUtil.null2Long(2638));
				if(order != null){
					List<Map> goodsList = orderFormTools.queryGoodsInfo(order.getGoods_info());
					modal.put("objs", goodsList);
					modal.put("order", order);
					modal.put("userName", "hkk");
					modal.put("webServer", configService.getSysConfig().getImageWebServer());
					String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "mail_template.vm",
							"UTF-8", modal);
					helper.setSubject("购物车下单");
					helper.setText(content, true);
				}
			}
		};
		this.sender.send(preparator);
	}
	
	@Async
	public void sendVelocityContextEmail(final String[] email, final String content) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				// 设置内容显示的编码格式
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
				String to[] = email;
				InternetAddress[] sendTo = new InternetAddress[to.length];
				for(int i=0; i<to.length; i++){
					sendTo[i] = new InternetAddress(to[i]);
				}
				helper.setTo(sendTo);
				helper.setFrom("service@soarmall.com");
			/*	Map modal = new HashMap();
				OrderForm order = orderFormService.getObjById(CommUtil.null2Long(2610));
				if(order != null){
					List<Map> goodsList = orderFormTools.queryGoodsInfo(order.getGoods_info());
					modal.put("objs", goodsList);
					modal.put("order", order);
					modal.put("userName", "hkk");
					modal.put("webServer", configService.getSysConfig().getImageWebServer());
					String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "mail_template.vm",
							"UTF-8", modal);
					helper.setSubject("购物车下单");
					helper.setText(content, true);
				}*/
				helper.setSubject("购物车下单");
				helper.setText(content, true); //true 是否为html
			}
		};
		this.sender.send(preparator);
	}

	public void setSender(JavaMailSender sender) {
		this.sender = sender;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
}
