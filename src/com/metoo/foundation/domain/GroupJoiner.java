package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * <p>
 * Title: Group.java
 * </p>
 * 
 * <p>
 * Description:
 * 团购开团参团信息表，与团购商品，订单表关联
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company:  www.koala.com
 * </p>
 * 
 * @author jackylau
 * 
 * @date 2014-4-25
 * 
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "group_joiner")
public class GroupJoiner extends IdEntity {
	
	private String user_id;           //用户账号
	private long rela_order_form_id;  //订单id
	private long rela_group_goods_id; //团购商品id
	private String child_group_id;    //团id
	private String is_group_creator;  //是
	private Date create_time;         //开团或参团时间
	private long joiner_count;        //参团人数
	private String status;            //状态 0-未支付  1-已支付 2-已退款
	private long add_integral;        //本次团购增加 的积分
	

	public String getUser_id() {
		return user_id;
	}



	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}



	public long getRela_order_form_id() {
		return rela_order_form_id;
	}



	public void setRela_order_form_id(long rela_order_form_id) {
		this.rela_order_form_id = rela_order_form_id;
	}



	public long getRela_group_goods_id() {
		return rela_group_goods_id;
	}



	public void setRela_group_goods_id(long rela_group_goods_id) {
		this.rela_group_goods_id = rela_group_goods_id;
	}



	public String getChild_group_id() {
		return child_group_id;
	}



	public void setChild_group_id(String child_group_id) {
		this.child_group_id = child_group_id;
	}



	public String getIs_group_creator() {
		return is_group_creator;
	}



	public void setIs_group_creator(String is_group_creator) {
		this.is_group_creator = is_group_creator;
	}



	public Date getCreate_time() {
		return create_time;
	}



	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	

	public long getJoiner_count() {
		return joiner_count;
	}



	public void setJoiner_count(long joiner_count) {
		this.joiner_count = joiner_count;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}

	

	public long getAdd_integral() {
		return add_integral;
	}



	public void setAdd_integral(long add_integral) {
		this.add_integral = add_integral;
	}



	public GroupJoiner() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
