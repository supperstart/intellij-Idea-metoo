package com.metoo.msg.email;

import java.io.File;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class MailSenderService {
	private JavaMailSender mailSender;//spring配置中定义
	private VelocityEngine velocityEngine;//spring配置中定义
	private SimpleMailMessage simpleMailMessage;//spring配置中定义
	private String from;
    private String to;
    private String subject;
    private String content;
    private String templateName;
	    // 是否需要身份验证   
    private boolean validate = false; 
     
     
    public JavaMailSender getMailSender() {
        return mailSender;
    }
    public void setMailSender(JavaMailSender mailSender) {
         this.mailSender = mailSender;
     }
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
     public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
	    /**
	      * 发送模板邮件
	     *
	      */
	    public void sendWithTemplate(Map model){
	        mailSender = this.getMailSender();
	        simpleMailMessage.setTo(this.getTo()); //接收人  
	        simpleMailMessage.setFrom(simpleMailMessage.getFrom()); //发送人,从配置文件中取得
	        simpleMailMessage.setSubject(this.getSubject());
	        String result = null;
	        try {
	            result = VelocityEngineUtils.mergeTemplateIntoString(this.getVelocityEngine(), this.getTemplateName(), "UTF-8",model);
	        } catch (Exception e) {}
	       simpleMailMessage.setText(result);
	        mailSender.send(simpleMailMessage);
	    }
	    /**
	     * 发送普通文本邮件
	     *
	     */
	    public void sendText(){
	        mailSender = this.getMailSender();
	        simpleMailMessage.setTo(this.getTo()); //接收人  
	        simpleMailMessage.setFrom(simpleMailMessage.getFrom()); //发送人,从配置文件中取得
	         simpleMailMessage.setSubject(this.getSubject());
	         simpleMailMessage.setText(this.getContent());
	        mailSender.send(simpleMailMessage);
	    }
	     /**
	     * 发送普通Html邮件
	     *
	     */
	    public void sendHtml(){
	        mailSender = this.getMailSender();
	         MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
	        try {
	            messageHelper.setTo(this.getTo());
	             messageHelper.setFrom(simpleMailMessage.getFrom());
	            messageHelper.setSubject(this.getSubject());
	             messageHelper.setText(this.getContent(),true);      
	        } catch (MessagingException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        mailSender.send(mimeMessage);
	    }
	    
	    /**
	     * 发送普通带一张图片的Html邮件
	     *
	     */
	   public void sendHtmlWithImage(String imagePath){
	        mailSender = this.getMailSender();
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        try {
	           MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
	            messageHelper.setTo(this.getTo());
	           messageHelper.setFrom(simpleMailMessage.getFrom());
	            messageHelper.setSubject(this.getSubject());
	           messageHelper.setText(this.getContent(),true);
//	            Content="<html><head></head><body><img src=\"cid:image\"/></body></html>";
	            //图片必须这样子：<img src='cid:image'/>
	            FileSystemResource img = new FileSystemResource(new File(imagePath));
	            messageHelper.addInline("image",img);
	       } catch (MessagingException e) {
	           // TODO Auto-generated catch block
	            e.printStackTrace();
	       }
	        mailSender.send(mimeMessage);
	    }
	   
	    /**
	     * 发送普通带附件的Html邮件
	     *
	     */
	    public void sendHtmlWithAttachment(String filePath){
	        mailSender = this.getMailSender();
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        try {
	           MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true);
	            messageHelper.setTo(this.getTo());
	            messageHelper.setFrom(simpleMailMessage.getFrom());
	            messageHelper.setSubject(this.getSubject());
	            messageHelper.setText(this.getContent(),true);
	            FileSystemResource file = new FileSystemResource(new File(filePath));
//	            System.out.println("file.getFilename==="+file.getFilename());
	            messageHelper.addAttachment(file.getFilename(),file);
	        } catch (MessagingException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        mailSender.send(mimeMessage);
	    }
	    
	    
	    
	    
	    
	    public String getFrom() {
	        return from;
	    }
	    public void setFrom(String from) {
	        this.from = from;
	    }
	    public String getSubject() {
	        return subject;
	    }
	   public void setSubject(String subject) {
	        this.subject = subject;
	    }
	    public String getTo() {
	        return to;
	    }
	    public void setTo(String to) {
	        this.to = to;
	    }
	    public String getTemplateName() {
	        return templateName;
	    }
	   public void setTemplateName(String templateName) {
	        this.templateName = templateName;
	    }
	    public String getContent() {
	        return content;
	    }
	    public void setContent(String content) {
	        this.content = content;
	    }
	    public boolean isValidate() {
	        return validate;
	    }
	   public void setValidate(boolean validate) {
	        this.validate = validate;
	    }
	    public SimpleMailMessage getSimpleMailMessage() {
	        return simpleMailMessage;
	    }
	    public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage) {
	        this.simpleMailMessage = simpleMailMessage;
	   }
	}
