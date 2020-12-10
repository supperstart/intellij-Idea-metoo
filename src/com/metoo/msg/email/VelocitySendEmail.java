package com.metoo.msg.email;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.metoo.core.tools.CommUtil;
import com.metoo.core.tools.PopupAuthenticator;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.domain.OrderForm;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IOrderFormService;
import com.metoo.foundation.service.ISysConfigService;

@Component
public class VelocitySendEmail {
	
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IOrderFormService orderFormService;
	/** 发送类 */
    private JavaMailSender mailSender;
    /** Velocity引擎 */
    private VelocityEngine velocityEngine;
    
	
	
	/**
	 * 发送邮件底层工具
	 * 
	 * @param mobile
	 * @param content
	 * @return
	 * @throws Exception 
	 * @throws UnsupportedEncodingException
	 */
	public boolean sendEmail2(String email, String subject, String content, String order_id)
			throws Exception {
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
					MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
					
					Map params = new HashMap();
					params.put("goods_id", CommUtil.null2Long(6655));
					List<Goods> productList = this.goodsService
							.query("select obj from Goods obj where obj.id=:goods_id", params, -1, -1);
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("productList", productList);
					
					
					String result = null;
					VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();
					Properties properties = new Properties();
					properties.put("input.encoding", "utf-8");
					properties.put("output.encoding", "utf-8");
					Velocity.init(properties);
					VelocityEngine velocityEngine = bean.createVelocityEngine();
					try {
						result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "/WEB-INF/template/test.vm", "UTF-8", model);
					} catch (Exception e) {
					}
					helper.setText(result, true);
					
					Transport.send(message);
					
					/*
					 * MimeMultipart multi = new MimeMultipart("related");
					 * BodyPart bodyPart = new MimeBodyPart();
					 * bodyPart.setDataHandler(new DataHandler(content,
					 * "text/html;charset=UTF-8"));// 网页格式 //
					 * bodyPart.setText(content); multi.addBodyPart(bodyPart);
					 * message.setContent(multi); message.saveChanges();
					 * Transport.send(message);// 指定smtp服务器的路径和端口的
					 */ ret = true;
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

	
	/* public boolean sendMail(String personal,String from,String to,String subject,String tplName,Map<String,Object> data){
	        try {
	        	MimeMessage message = new MimeMessage();
	            MimeMessageHelper msgHelper = new MimeMessageHelper(msg, "UTF-8");
	            msgHelper.setFrom(from, personal);
	            msgHelper.setTo(to);
	            msgHelper.setSubject(subject);
	            msgHelper.setText(getHtmlText(tplName,data), true);
	            mailSender.send(msg);
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return false;
	    }
	 */
	 /**
	     * velocity 模板转 html
	     * @param tplName 模板
	     * @param data 数据
	     * @return
	     */
	    public String getHtmlText(String tplName,Map<String,Object> data){
	        return VelocityEngineUtils.mergeTemplateIntoString(this.velocityEngine, tplName,"UTF-8",data);
	    }

}
