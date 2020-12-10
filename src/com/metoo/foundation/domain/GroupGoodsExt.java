package com.metoo.foundation.domain;

import java.math.BigDecimal;
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
 * Title: GroupGoodsExt.java
 * </p>
 * 
 * <p>
 * Description: 团购商品管理控制类，用来管理团购商品信息
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
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
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "group_goods_ext")
public class GroupGoodsExt extends IdEntity {
	
	
	private long group_goods_id;             //对应的团购商品ID
	private int group_duration;              //团购有效时长，默认24小时，从开团时起算
	private int group_stage_1_pc;            //第一阶段参团人数要求
	private BigDecimal group_stage_1_price;  //第一阶段价格
	private int group_stage_2_pc;            //第二阶段参团人数要求
	private BigDecimal group_stage_2_price;  //第二阶段价格
	private int group_stage_3_pc;			 //第三阶段参团人数要求	
	private BigDecimal group_stage_3_price;  //第三阶段价格

	
	

	public long getGroup_goods_id() {
		return group_goods_id;
	}


	public void setGroup_goods_id(long group_goods_id) {
		this.group_goods_id = group_goods_id;
	}


	public int getGroup_duration() {
		return group_duration;
	}


	public void setGroup_duration(int group_duration) {
		this.group_duration = group_duration;
	}


	public int getGroup_stage_1_pc() {
		return group_stage_1_pc;
	}


	public void setGroup_stage_1_pc(int group_stage_1_pc) {
		this.group_stage_1_pc = group_stage_1_pc;
	}


	public BigDecimal getGroup_stage_1_price() {
		return group_stage_1_price;
	}


	public void setGroup_stage_1_price(BigDecimal group_stage_1_price) {
		this.group_stage_1_price = group_stage_1_price;
	}


	public int getGroup_stage_2_pc() {
		return group_stage_2_pc;
	}


	public void setGroup_stage_2_pc(int group_stage_2_pc) {
		this.group_stage_2_pc = group_stage_2_pc;
	}


	public BigDecimal getGroup_stage_2_price() {
		return group_stage_2_price;
	}


	public void setGroup_stage_2_price(BigDecimal group_stage_2_price) {
		this.group_stage_2_price = group_stage_2_price;
	}


	public int getGroup_stage_3_pc() {
		return group_stage_3_pc;
	}


	public void setGroup_stage_3_pc(int group_stage_3_pc) {
		this.group_stage_3_pc = group_stage_3_pc;
	}


	public BigDecimal getGroup_stage_3_price() {
		return group_stage_3_price;
	}


	public void setGroup_stage_3_price(BigDecimal group_stage_3_price) {
		this.group_stage_3_price = group_stage_3_price;
	}


	public GroupGoodsExt(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}


	public GroupGoodsExt() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
