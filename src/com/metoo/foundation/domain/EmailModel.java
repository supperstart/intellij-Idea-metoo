package com.metoo.foundation.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;
/**
 * <p>
 * 	Title: EmailModel.class
 * </p>
 * 
 * <p>
 * 	Description: api邮件模板类,记录api名称，api类型，使用指定邮件模板发送指定邮件
 * </p>
 * 
 * <p>
 * 	Company: 湖南觅通科技有限公司
 * </p>
 * 
 * 
 * @author hkk
 *
 */

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "email_model")
public class EmailModel extends IdEntity{

	private String name; //Api 名称
	
	private String value; //api值
	
	private int sequence;// 排序
	
	private String type;// 类型 BUYER SELLER
	
	private String url;// api接口
	
	private boolean display;// 是否启用
	
	private Long template_id;// 对应模板id

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public Long getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(Long template_id) {
		this.template_id = template_id;
	}
	
}
