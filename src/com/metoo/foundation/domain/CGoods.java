package com.metoo.foundation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "c_goods")
public class CGoods extends IdEntity {
	
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_price;//商品原价
	@Column(precision = 12, scale = 2)
	private BigDecimal discount_price;//商品折后价格
	@Column(columnDefinition = "LongText")
	private String goods_tiered_price; // 商品阶梯价格
	@Column(precision = 12, scale = 2)
	private BigDecimal cgoods_discount_rate;//商品折扣率
	private String goods_serial;//商品编码[sku]
	private String eid;//平台商品编码
	@Column(columnDefinition = "int default 0")
	private int goods_inventory;//[库存]
	@Column(columnDefinition = "LongText")
	private String combination_id;
	@ManyToOne(fetch = FetchType.LAZY)
	private Goods goods;// 所属商品
	@Column(columnDefinition = "int default 0")
	private int goods_weight;// 商品重量
	@Column(columnDefinition = "int default 0")
	private int goods_volume;// 商品体积
	@Column(columnDefinition = "int default 0")
	private int goods_length;// 商品长
	@Column(columnDefinition = "int default 0")
	private int goods_width;// 商品宽
	@Column(columnDefinition = "int default 0")
	private int goods_high;// 商品高
	@Column(columnDefinition = "LongText")
	private String goods_disabled;// 0为解除 1为禁用
	@Column(columnDefinition = "LongText")
	private String spec_color;//子商品颜色
	private int goods_warn_inventory;// 商品预警数量,库存少于预警数量，
	@Column(columnDefinition = "int default 0")
	private int warn_inventory_status;// 预警状态，0为正常，-1为预警
	@ManyToMany
	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "c_goods_photo", joinColumns = @JoinColumn(name = "c_goods_id"), inverseJoinColumns = @JoinColumn(name = "c_photo_id"))
	private List<Accessory> c_goods_photos = new ArrayList<Accessory>();// sku商品图片，目前只允许8张,图片可以重复使用
	@ManyToMany
	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "c_goods_spec", joinColumns = @JoinColumn(name = "c_goods_id"), inverseJoinColumns = @JoinColumn(name = "c_spec_id"))
	private List<GoodsSpecProperty> c_goods_specs = new ArrayList<GoodsSpecProperty>();
	private int local_inventory; //商品库存 本地仓
	private int oversea_inventory; //商品库存 海外仓
	
	public int getOversea_inventory() {
		return oversea_inventory;
	}

	public void setOversea_inventory(int oversea_inventory) {
		this.oversea_inventory = oversea_inventory;
	}

	public int getLocal_inventory() {
		return local_inventory;
	}

	public void setLocal_inventory(int local_inventory) {
		this.local_inventory = local_inventory;
	}

	public String getGoods_tiered_price() {
		return goods_tiered_price;
	}
	public void setGoods_tiered_price(String goods_tiered_price) {
		this.goods_tiered_price = goods_tiered_price;
	}
	public int getGoods_warn_inventory() {
		return goods_warn_inventory;
	}
	public void setGoods_warn_inventory(int goods_warn_inventory) {
		this.goods_warn_inventory = goods_warn_inventory;
	}
	public int getWarn_inventory_status() {
		return warn_inventory_status;
	}
	public void setWarn_inventory_status(int warn_inventory_status) {
		this.warn_inventory_status = warn_inventory_status;
	}
	public String getSpec_color() {
		return spec_color;
	}
	public void setSpec_color(String spec_color) {
		this.spec_color = spec_color;
	}
	
	public BigDecimal getCgoods_discount_rate() {
		return cgoods_discount_rate;
	}
	public void setCgoods_discount_rate(BigDecimal cgoods_discount_rate) {
		this.cgoods_discount_rate = cgoods_discount_rate;
	}
	public String getGoods_disabled() {
		return goods_disabled;
	}
	public void setGoods_disabled(String goods_disabled) {
		this.goods_disabled = goods_disabled;
	}
	public List<GoodsSpecProperty> getC_goods_spec() {
		return c_goods_specs;
	}
	public void setC_goods_spec(List<GoodsSpecProperty> c_goods_specs) {
		this.c_goods_specs = c_goods_specs;
	}
	
	public List<Accessory> getC_goods_photos() {
		return c_goods_photos;
	}
	public void setC_goods_photos(List<Accessory> c_goods_photos) {
		this.c_goods_photos = c_goods_photos;
	}
	public List<Accessory> getGoods_photos() {
		return c_goods_photos;
	}
	public void setGoods_photos(List<Accessory> c_goods_photos) {
		this.c_goods_photos = c_goods_photos;
	}
	public String getCombination_id() {
		return combination_id;
	}
	public void setCombination_id(String combination_id) {
		this.combination_id = combination_id;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public BigDecimal getGoods_price() {
		return goods_price;
	}
	public void setGoods_price(BigDecimal goods_price) {
		this.goods_price = goods_price;
	}
	public BigDecimal getDiscount_price() {
		return discount_price;
	}
	public void setDiscount_price(BigDecimal discount_price) {
		this.discount_price = discount_price;
	}
	public String getGoods_serial() {
		return goods_serial;
	}
	public void setSku(String goods_serial) {
		this.goods_serial = goods_serial;
	}
	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}
	public int getGoods_inventory() {
		return goods_inventory;
	}
	public void setGoods_inventory(int goods_inventory) {
		this.goods_inventory = goods_inventory;
	}
	
	public int getGoods_weight() {
		return goods_weight;
	}
	public void setGoods_weight(int goods_weight) {
		this.goods_weight = goods_weight;
	}
	public int getGoods_volume() {
		return goods_volume;
	}
	public void setGoods_volume(int goods_volume) {
		this.goods_volume = goods_volume;
	}
	public int getGoods_length() {
		return goods_length;
	}
	public void setGoods_length(int goods_length) {
		this.goods_length = goods_length;
	}
	public int getGoods_width() {
		return goods_width;
	}
	public void setGoods_width(int goods_width) {
		this.goods_width = goods_width;
	}
	public int getGoods_high() {
		return goods_high;
	}
	public void setGoods_high(int goods_high) {
		this.goods_high = goods_high;
	}
	public List<GoodsSpecProperty> getC_goods_specs() {
		return c_goods_specs;
	}
	public void setC_goods_specs(List<GoodsSpecProperty> c_goods_specs) {
		this.c_goods_specs = c_goods_specs;
	}
	public void setGoods_serial(String goods_serial) {
		this.goods_serial = goods_serial;
	}
	public CGoods() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public CGoods(String combination_id, Long id) {
		super.setId(id);
		this.combination_id = combination_id;
	}
	public CGoods(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}
	public CGoods(BigDecimal goods_price, BigDecimal discount_price, String goods_serial, String eid, int goods_inventory) {
		super();
		this.goods_price = goods_price;
		this.discount_price = discount_price;
		this.goods_serial = goods_serial;
		this.eid = eid;
		this.goods_inventory = goods_inventory;
	}

	

}
