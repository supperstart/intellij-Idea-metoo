package com.metoo.foundation.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "storelog")
public class StoreLog extends IdEntity{
	private String store_name; //店铺名称
	@Column(columnDefinition = "int default 0")
	private int store_click; //店铺浏览量
	@Column(columnDefinition = "int default 0")
	private int store_collect; //店铺收藏次数
	@Column(columnDefinition = "int default 0")
	private int signfor; //售出数量（店鋪成交量）
	@Column(columnDefinition = "int default 0")
	private int placeorder; //店铺下单数量
	private int repetition; //复购率
	@Column(columnDefinition = "int default 0")
	private int returnorder; //退货数量
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_point;//商品评分
	@ManyToOne(fetch = FetchType.LAZY)
	private Store store;
	@Column(columnDefinition = "int default 0")
	private int log_form; //日志类型 0为自营， 1为商家
	
	public int getLog_form() {
		return log_form;
	}
	public void setLog_form(int log_form) {
		this.log_form = log_form;
	}
	public String getStore_name() {
		return store_name;
	}
	public void setStore_name(String store_name) {
		this.store_name = store_name;
	}
	public int getRepetition() {
		return repetition;
	}
	public void setRepetition(int repetition) {
		this.repetition = repetition;
	}
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
	public int getStore_click() {
		return store_click;
	}
	public void setStore_click(int store_click) {
		this.store_click = store_click;
	}
	public int getStore_collect() {
		return store_collect;
	}
	public void setStore_collect(int store_collect) {
		this.store_collect = store_collect;
	}
	public int getSignfor() {
		return signfor;
	}
	public void setSignfor(int signfor) {
		this.signfor = signfor;
	}
	public int getPlaceorder() {
		return placeorder;
	}
	public void setPlaceorder(int placeorder) {
		this.placeorder = placeorder;
	}
	public int getReturnorder() {
		return returnorder;
	}
	public void setReturnorder(int returnorder) {
		this.returnorder = returnorder;
	}
	public BigDecimal getGoods_point() {
		return goods_point;
	}
	public void setGoods_point(BigDecimal goods_point) {
		this.goods_point = goods_point;
	}
	
}
