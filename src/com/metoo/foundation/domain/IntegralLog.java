package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * <p>
 * Title: IntegralLog.java
 * </p>
 * 
 * <p>
 * Description:用户积分操作日志记录
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: 沈阳网之商科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-25
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "integrallog")
public class IntegralLog extends IdEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	private User integral_user;// 积分用户
	@ManyToOne(fetch = FetchType.LAZY)
	private User operate_user;// 操作用户
	private int integral;// 操作积分数
	private String type;// 积分操作类型 add reduce
	private String integral_from; //积分来源操作类型，包括reg：注册赠送，system：管理员操作,login:用户登录,order:订单获得,integral_order:积分兑换
	// chind_order：子会员消费  third_order ：第三级会员消费  present : 系统赠送 share:分享获得  buyNow:立即购买, cartOrder:购物车下单 sign：签到
	@Lob
	@Column(columnDefinition = "LongText")
	private String content;// 操作说明
	@Column(columnDefinition = "int default 0")
	private int freeze;//是否为冻结积分 默认为0  1：该积分为冻结积分
	@OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private OrderForm order;

	public IntegralLog() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IntegralLog(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public User getIntegral_user() {
		return integral_user;
	}

	public void setIntegral_user(User integral_user) {
		this.integral_user = integral_user;
	}

	public User getOperate_user() {
		return operate_user;
	}

	public void setOperate_user(User operate_user) {
		this.operate_user = operate_user;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIntegral_from() {
		return integral_from;
	}

	public void setIntegral_from(String integral_from) {
		this.integral_from = integral_from;
	}

	public int getFreeze() {
		return freeze;
	}

	public void setFreeze(int freeze) {
		this.freeze = freeze;
	}

	public OrderForm getOrder() {
		return order;
	}

	public void setOrder(OrderForm order) {
		this.order = order;
	}
	
	
}
