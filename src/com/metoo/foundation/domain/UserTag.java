package com.metoo.foundation.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * @author Administrator
 *
 * @description 用户标签表，用于记录用户行为商品标签所占权重。智能推荐
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "user_tag")
public class UserTag extends IdEntity{

	private Long user_id; // 用户id
	private String user_name; // 用户名称
	private Long tag_id; // 标签id
	private String tag_name; // 标签名称
	private int tag_type; // 标签类型
	private BigDecimal weight_value; // 行为权重值
	@ManyToOne(fetch = FetchType.LAZY)
	private Behavior behavior; // 行为
	
	public Long getUser_id() {
		return user_id;
	}
	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public Long getTag_id() {
		return tag_id;
	}
	public void setTag_id(Long tag_id) {
		this.tag_id = tag_id;
	}
	public BigDecimal getWeight_value() {
		return weight_value;
	}
	public void setWeight_value(BigDecimal weight_value) {
		this.weight_value = weight_value;
	}
	
}
