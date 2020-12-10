package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "lucky_draw")
public class LuckyDraw extends IdEntity{

	private String name;
	private String en_rule;//英文规则描述
	private String sa_rule;//阿语描述
	private int num;//抽奖次数
	private int switchs;//活动开启关闭
	private Date created_at;//创建时间
	private Date updated_at;//更新时间
	private int register;//注册抽奖次数
	private int order;//下单抽奖次数
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEn_rule() {
		return en_rule;
	}
	public void setEn_rule(String en_rule) {
		this.en_rule = en_rule;
	}
	public String getSa_rule() {
		return sa_rule;
	}
	public void setSa_rule(String sa_rule) {
		this.sa_rule = sa_rule;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public int getSwitchs() {
		return switchs;
	}
	public void setSwitchs(int switchs) {
		this.switchs = switchs;
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}
	public Date getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}
	public int getRegister() {
		return register;
	}
	public void setRegister(int register) {
		this.register = register;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
	
}
