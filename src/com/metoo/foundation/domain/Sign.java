package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * <p>
 * 	Title: Sign.java
 * </p>
 * 
 * <p>
 * 	Description: 用户签到类
 * </p>
 * @author 46075
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name=Globals.DEFAULT_TABLE_SUFFIX + "sign")
public class Sign extends IdEntity{

	@OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private User user;
	
	private String make;//签到信息
	
	private int continueSign;// 七天连续签到
	
	private int count;//总签到天数
	private int integral;//总签到积分
	
	private Date update_time;// 更新时间

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public int getContinueSign() {
		return continueSign;
	}

	public void setContinueSign(int continueSign) {
		this.continueSign = continueSign;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Date getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}
	
}
