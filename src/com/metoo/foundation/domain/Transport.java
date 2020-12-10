package com.metoo.foundation.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * <p>
 * Title: Transport.java
 * </p>
 * 
 * <p>
 * Description:系统运费模板类
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
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "transport")
public class Transport extends IdEntity {
	private String trans_name;// 运费模板名称
	@Column(columnDefinition = "int default 0")
	private int trans_time;// 发货时间
	@Column(columnDefinition = "int default 0")
	private int trans_type;// 0按件数，1按重量，2按体积
	/*@OneToOne(fetch = FetchType.LAZY)
	private Store store;*/
	/*@OneToMany(mappedBy = "transport", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private List<Store> store_list;*/
	private boolean trans_mail;// 是否启用平邮
	@Column(columnDefinition = "LongText")
	private String trans_mail_info;// 平邮信息,使用json管理[{"city_id":-1,"city_name":"全国","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2},{"city_id":1,"city_name":"沈阳","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2}]
	private boolean trans_express;// 是否启用快递
	@Column(columnDefinition = "LongText")
	private String trans_express_info;// 快递信息,使用json管理[{"city_id":-1,"city_name":"全国","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2},{"city_id":1,"city_name":"沈阳","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2}]
	private boolean trans_ems;// 是否启用EMS
	@Column(columnDefinition = "LongText")
	private String trans_ems_info;// EMS信息,使用json管理[{"city_id":-1,"city_name":"全国","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2},{"city_id":1,"city_name":"沈阳","trans_weight":1,"trans_fee":13.5,"trans_add_weight":1,"trans_add_fee":2}]
	@Column(columnDefinition = "int default 0")
	private int trans_user;// 运费模板类型，0为自营模板，1为商家模板
	@Column(columnDefinition = "int default 0")
	private int trans_global;// 运费模板类型 0:本地发货  1：国际直发
	@ManyToOne(fetch = FetchType.LAZY)
	private ExpressCompany express_company;//快递公司
	
	public ExpressCompany getExpress_company() {
		return express_company;
	}

	public void setExpress_company(ExpressCompany express_company) {
		this.express_company = express_company;
	}

	public int getTrans_global() {
		return trans_global;
	}

	public void setTrans_global(int trans_global) {
		this.trans_global = trans_global;
	}

	public Transport() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Transport(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}

	public int getTrans_user() {
		return trans_user;
	}

	public void setTrans_user(int trans_user) {
		this.trans_user = trans_user;
	}

	public int getTrans_time() {
		return trans_time;
	}

	public void setTrans_time(int trans_time) {
		this.trans_time = trans_time;
	}

	public int getTrans_type() {
		return trans_type;
	}

	public void setTrans_type(int trans_type) {
		this.trans_type = trans_type;
	}

	public boolean isTrans_mail() {
		return trans_mail;
	}

	public void setTrans_mail(boolean trans_mail) {
		this.trans_mail = trans_mail;
	}

	public boolean isTrans_express() {
		return trans_express;
	}

	public void setTrans_express(boolean trans_express) {
		this.trans_express = trans_express;
	}

	public boolean isTrans_ems() {
		return trans_ems;
	}

	public void setTrans_ems(boolean trans_ems) {
		this.trans_ems = trans_ems;
	}

	public String getTrans_name() {
		return trans_name;
	}

	public void setTrans_name(String trans_name) {
		this.trans_name = trans_name;
	}

	public String getTrans_mail_info() {
		return trans_mail_info;
	}

	public void setTrans_mail_info(String trans_mail_info) {
		this.trans_mail_info = trans_mail_info;
	}

	public String getTrans_express_info() {
		return trans_express_info;
	}

	public void setTrans_express_info(String trans_express_info) {
		this.trans_express_info = trans_express_info;
	}

	public String getTrans_ems_info() {
		return trans_ems_info;
	}

	public void setTrans_ems_info(String trans_ems_info) {
		this.trans_ems_info = trans_ems_info;
	}

	/*public List<Store> getStore_list() {
		return store_list;
	}

	public void setStore_list(List<Store> store_list) {
		this.store_list = store_list;
	}*/

}
