package com.metoo.foundation.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "goodsTest")
public class GoodsTest extends IdEntity{
	private String seo_keywords;// 关键字
	@Column(columnDefinition = "LongText")
	private String seo_description;// 描述
	private String goods_name;// 商品名称
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_price;// 商品原价
	
	
	
	public GoodsTest(String seo_keywords, String seo_description, String goods_name, BigDecimal goods_price) {
		super();
		this.seo_keywords = seo_keywords;
		this.seo_description = seo_description;
		this.goods_name = goods_name;
		this.goods_price = goods_price;
	}
	public GoodsTest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getSeo_keywords() {
		return seo_keywords;
	}
	public void setSeo_keywords(String seo_keywords) {
		this.seo_keywords = seo_keywords;
	}
	public String getSeo_description() {
		return seo_description;
	}
	public void setSeo_description(String seo_description) {
		this.seo_description = seo_description;
	}
	public String getGoods_name() {
		return goods_name;
	}
	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}
	public BigDecimal getGoods_price() {
		return goods_price;
	}
	public void setGoods_price(BigDecimal goods_price) {
		this.goods_price = goods_price;
	}
	
		
	}

