package com.metoo.foundation.domain.virtual;

import java.math.BigDecimal;

public class FootPointView {
	private String fpv_goods_name;
	private String fpv_ksa_goods_name;
	private Long fpv_goods_id;
	private String fpv_goods_img_path;
	private int fpv_goods_sale;
	private BigDecimal fpv_goods_price;
	private BigDecimal fpv_goods_current_price;
	private Long fpv_goods_class_id;
	private String fpv_goods_class_name;
	private BigDecimal fpv_goods_discount_rate;
	private int fpv_goods_status;
	private int fpv_store_status;
	
	public int getFpv_goods_status() {
		return fpv_goods_status;
	}

	public void setFpv_goods_status(int fpv_goods_status) {
		this.fpv_goods_status = fpv_goods_status;
	}

	public int getFpv_store_status() {
		return fpv_store_status;
	}

	public void setFpv_store_status(int fpv_store_status) {
		this.fpv_store_status = fpv_store_status;
	}

	public BigDecimal getFpv_goods_discount_rate() {
		return fpv_goods_discount_rate;
	}

	public void setFpv_goods_discount_rate(BigDecimal fpv_goods_discount_rate) {
		this.fpv_goods_discount_rate = fpv_goods_discount_rate;
	}

	public BigDecimal getFpv_goods_current_price() {
		return fpv_goods_current_price;
	}

	public void setFpv_goods_current_price(BigDecimal fpv_goods_current_price) {
		this.fpv_goods_current_price = fpv_goods_current_price;
	}

	public Long getFpv_goods_class_id() {
		return fpv_goods_class_id;
	}

	public void setFpv_goods_class_id(Long fpv_goods_class_id) {
		this.fpv_goods_class_id = fpv_goods_class_id;
	}

	public String getFpv_goods_class_name() {
		return fpv_goods_class_name;
	}

	public void setFpv_goods_class_name(String fpv_goods_class_name) {
		this.fpv_goods_class_name = fpv_goods_class_name;
	}

	public BigDecimal getFpv_goods_price() {
		return fpv_goods_price;
	}

	public void setFpv_goods_price(BigDecimal fpv_goods_price) {
		this.fpv_goods_price = fpv_goods_price;
	}

	public int getFpv_goods_sale() {
		return fpv_goods_sale;
	}

	public void setFpv_goods_sale(int fpv_goods_sale) {
		this.fpv_goods_sale = fpv_goods_sale;
	}

	public String getFpv_goods_name() {
		return fpv_goods_name;
	}

	public void setFpv_goods_name(String fpv_goods_name) {
		this.fpv_goods_name = fpv_goods_name;
	}

	public Long getFpv_goods_id() {
		return fpv_goods_id;
	}

	public void setFpv_goods_id(Long fpv_goods_id) {
		this.fpv_goods_id = fpv_goods_id;
	}

	public String getFpv_goods_img_path() {
		return fpv_goods_img_path;
	}

	public void setFpv_goods_img_path(String fpv_goods_img_path) {
		this.fpv_goods_img_path = fpv_goods_img_path;
	}

	public String getFpv_ksa_goods_name() {
		return fpv_ksa_goods_name;
	}

	public void setFpv_ksa_goods_name(String fpv_ksa_goods_name) {
		this.fpv_ksa_goods_name = fpv_ksa_goods_name;
	}

}
