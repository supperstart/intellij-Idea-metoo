package com.metoo.foundation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * <p>
 * Title: Goods.java
 * </p>
 * 
 * <p>
 * Description:商品实体类,用来描述系统商品信息，系统核心实体类
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
 * 
 * @version koala_b2b2c v2.0 2015版
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "goods")
public class Goods extends IdEntity {
	private String seo_keywords;// 关键字
	@Column(columnDefinition = "LongText")
	private String seo_description;// 描述
	private String goods_name;// 商品名称
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_price;// 商品原价
	@Column(precision = 12, scale = 2)
	private BigDecimal store_price;// 店铺价格
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_discount_rate;// 商品折扣率
	@Column(columnDefinition = "int default 0")
	private int goods_inventory;// 库存数量
	private String inventory_type;// 库存方式，分为all全局库存，spec按规格库存
	@Column(columnDefinition = "int default 0")
	private int goods_salenum;// 商品售出数量
	private String goods_serial;// 商品货号 [主spu]
	private String eid;// 商品eid
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_weight;// 商品重量
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_volume;// 商品体积
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_length;// 商品长
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_width;// 商品宽
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_high;// 商品高
	@Column(columnDefinition = "LongText")//产品特征--亮点 -- 调整为json
	private String features_one;
	@Column(columnDefinition = "LongText")
	private String features_two;
	@Column(columnDefinition = "LongText")
	private String features_three;
	@Column(columnDefinition = "LongText")
	private String features_four;
	@Column(columnDefinition = "LongText")
	private String features_five;
	
	private String goods_fee;// 运费
	@Column(columnDefinition = "LongText")
	private String goods_details;// 详细说明pc网页
	@Column(columnDefinition = "LongText")
	private String goods_details_mobile;// 详细说明app
	@Column(columnDefinition = "LongText")
	private String pack_details;// 商品包装清单
	@Column(columnDefinition = "LongText")
	private String goods_service;// 商品售后保障
	private boolean store_recommend;// 商城推荐
	private Date store_recommend_time;// 商品商城推荐时间
	private boolean store_creativity; // 商城精选
	private Date store_creativity_time; // 商品商城精选时间
	private boolean store_deals;// 商城秒杀 
	private Date store_deals_time;// 商品商城秒杀时间
	@Column(columnDefinition = "int default 0")
	private int store_deals_inventory;// 秒杀商品初始化库存
	@Column(columnDefinition = "int default 0")
	private int store_seckill_inventory;//
	private boolean store_china;//
	private Date store_china_time;//
	private boolean goods_recommend;// 是否店铺推荐，推荐后在店铺首页推荐位显示[目前用在精选商品]
	private int goods_click;// 商品浏览次数
	@Column(columnDefinition = "int default 0")
	private int goods_collect;// 商品收藏次数
	@Column(columnDefinition = "int default 0")
	private int goods_cod;// 是否支持货到付款，默认为0：支持货到付款，-1为不支持货到付款
	@ManyToOne(fetch = FetchType.LAZY)
	private Store goods_store;// 所属店铺
	@Column(columnDefinition = "int default 0")
	private int goods_status;// 商品当前状态，
								// 0为上架，1为在仓库中，2为定时自动上架，3为店铺过期自动下架，4为活动商品非出售商品， -1为手动下架状态，-2为违规下架状态， -3为删除
								// -5为平台未审核，-6审核未通过
	@Column(columnDefinition = "int default 0")
	private int publish_goods_status;// 商品发布审核后状态，
										// 0为发布后上架，1为发布后在仓库中，2为发布后定时自动上架，平台审核商品后根据该字段设定商品当前状态
	private Date goods_seller_time;// 商品上架时间，系统根据商品上架时间倒序排列
	@Column(columnDefinition = "int default 0")
	private int goods_transfee;// 商品运费承担方式，0为买家承担，1为卖家承担
	@ManyToOne(fetch = FetchType.LAZY)
	private GoodsClass gc;// 商品对应的大分类
	@ManyToOne(fetch = FetchType.LAZY)
	private Accessory goods_main_photo;// 商品主图片
	@ManyToOne(fetch = FetchType.LAZY)
	private Accessory accessory;// 商品主图片
	@ManyToMany
	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_photo", joinColumns = @JoinColumn(name = "goods_id"), inverseJoinColumns = @JoinColumn(name = "photo_id"))
	private List<Accessory> goods_photos = new ArrayList<Accessory>();// 商品其他图片，目前只允许7张,图片可以重复使用
	@ManyToMany
	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_ugc", joinColumns = @JoinColumn(name = "goods_id"), inverseJoinColumns = @JoinColumn(name = "class_id"))
	private List<UserGoodsClass> goods_ugcs = new ArrayList<UserGoodsClass>();// 店铺中商品所在分类
	@ManyToMany
	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_spec", joinColumns = @JoinColumn(name = "goods_id"), inverseJoinColumns = @JoinColumn(name = "spec_id"))
	@OrderBy(value = "sequence asc")
	private List<GoodsSpecProperty> goods_specs = new ArrayList<GoodsSpecProperty>();// 商品对应的规格值
	@ManyToOne(fetch = FetchType.LAZY)
	private GoodsBrand goods_brand;// 商品品牌
	@Column(columnDefinition = "LongText")
	private String goods_property;// 使用json管理数据
									// [{"val":"中长款（衣长50-70CM）","id":"32769","name":"衣长"},
									// {"val":"纯棉","id":"32768","name":"材质"},
									// {"val":"短袖","id":"1","name":"款式"}]
	@Column(columnDefinition = "LongText")
	private String goods_inventory_detail;// 商品规格详细库存,使用json管理，[{"id":"131072_131080_","price":"144","count":"106",supp:102}]supp：折扣价格
	private int ztc_status;// 直通车状态，1为开通申请待审核，2为审核通过,-1为审核失败,3为已经开通
	@Column(columnDefinition = "int default 0")
	private int ztc_pay_status;// 直通车金币支付状态，1为支付成功，0为待支付
	@Column(columnDefinition = "int default 30")
	private int ztc_price;// 直通车价格,按照金币计算，单位为个
	@Column(columnDefinition = "int default 30")
	private int ztc_dredge_price;// 已经开通的直通车价格,和ztc_price值一样，由系统定制器控制该值，在用户设定的开始日期后该值才会存在
	private Date ztc_apply_time;// 直通车申请时间
	@Temporal(TemporalType.DATE)
	private Date ztc_begin_time;// 直通车开始时间
	@Column(columnDefinition = "int default 0")
	private int ztc_gold;// 直通车开通金币，扣除完以后自动取消直通车状态
	@Column(columnDefinition = "int default 0")
	private int ztc_click_num;// 直通车商品浏览数
	@ManyToOne(fetch = FetchType.LAZY)
	private User ztc_admin;// 直通车审核管理员
	@Column(columnDefinition = "LongText")
	private String ztc_admin_content;// 直通车审核信息
	@OneToMany(mappedBy = "gg_goods", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<GroupGoods> group_goods_list = new ArrayList<GroupGoods>();// 商品对应的团购信息，一个商品可以参加多个团购，但是团购同一时间段只能发起一个
	@ManyToOne(fetch = FetchType.LAZY)
	private Group group;// 商品对应的团购,通过该字段判断商品当前参与的团购，团购商品审核通过后，给该字段赋值，同一时间段，一款商品只能保持一个团购活动状态
	@Column(columnDefinition = "int default 0")
	private int group_buy;// 团购状态，0为无团购，1为待审核，2为审核通过,3为团购商品已经卖完 4为审核通过 但未开始
	@OneToMany(mappedBy = "evaluate_goods", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private List<Evaluate> evaluates = new ArrayList<Evaluate>();// 商品对应的用户评价，映射评价实体中的evaluate_goods
	@Column(columnDefinition = "int default 0")
	private int evaluate_count;// 评论数，和evaluates总数对应，前端显示数量时候不需要查询evaluates对象
	@Column(columnDefinition = "int default 0")
	private int goods_choice_type;// 0实体商品，1为虚拟商品
	@Column(columnDefinition = "int default 0")
	private int activity_status;// 活动状态，0为无活动，1为待审核，2为审核通过，3为活动已经过期活结束，审核未通过时状态为0
	private Long activity_goods_id;// 对应商城活动商品id，当活动商品过期时该字段将清空
	@Column(precision = 12, scale = 2)
	private BigDecimal goods_current_price;// 商品当前价格,默认等于store_price，参加团购即为团购价，如团购、活动结束后,该价格自动变为store_price
	@Column(columnDefinition = "int default 0")
	private int combin_status;// 组合销售商品，0为无组合销售，1为有组合销售，当该商品参加组合销售时（无论组合商品是否通过审核），该状态为1，当组合时间到期或者组合方案完全删除后该商品状态变为0
	private Long combin_suit_id;// 组合套装方案id
	private Long combin_parts_id;// 组合配件方案id
	@Column(precision = 12, scale = 2)
	private BigDecimal mail_trans_fee;// 平邮费用
	@Column(precision = 12, scale = 2)
	private BigDecimal express_trans_fee;// 快递费用
	@Column(precision = 12, scale = 2)
	private BigDecimal ems_trans_fee;// EMS费用
	/*@ManyToOne(fetch = FetchType.LAZY)*/
	private Transport transport;// 调用的运费模板信息
	@Column(precision = 4, scale = 1, columnDefinition = "Decimal default 5.0")
	private BigDecimal description_evaluate;// 商品描述相符评分，默认为5分
	@Column(precision = 3, scale = 2)
	private BigDecimal well_evaluate;// 商品好评率,例如：该值为0.96，好评率即为96%
	@Column(precision = 3, scale = 2)
	private BigDecimal middle_evaluate;// 商品中评率
	@Column(precision = 3, scale = 2)
	private BigDecimal bad_evaluate;// 商品差评率
	@OneToMany(mappedBy = "ag_goods", cascade = CascadeType.REMOVE)
	private List<ActivityGoods> ag_goods_list = new ArrayList<ActivityGoods>();// 商城活动商品
	@Column(columnDefinition = "int default 0")
	private int goods_type;// 商品类型，0为自营商品，1为第三方经销商
	private int goods_global;//国际直邮 普通商品为2 国际直邮为1
	@ManyToOne(fetch = FetchType.LAZY)
	private User user_admin;// 当商品类型为自营商品时，对应的商品发布管理员
	@OneToMany(mappedBy = "goods")
	private List<GoodsCart> carts = new ArrayList<GoodsCart>();// 商品对应的购物车，反向映射
	@OneToMany(mappedBy = "goods")
	private List<ComplaintGoods> cgs = new ArrayList<ComplaintGoods>();// 商品对应的投诉，反向映射
	@Column(columnDefinition = "int default 0")
	private int mobile_recommend;// 手机客户端推荐， 1为推荐，推荐后在手机客户端首页显示
	@Temporal(TemporalType.DATE)
	private Date mobile_recommendTime;// 手机推荐时间，
	@Column(columnDefinition = "int default 0")
	private int mobile_hot;// 手机客户端热卖， 1为热卖，设置后在手机客户端首页显示
	@Temporal(TemporalType.DATE)
	private Date mobile_hotTime;// 手机热卖时间，
	@Column(columnDefinition = "int default 0")
	private int point; // 是否为邀请兑换商品 0:未参加 1:已参加
	private int point_status;// 0：已关闭  5:待审核 10:审核通过  -10: 审核未通过   -20：隐藏
	private String point_id;// 对应的邀约活动id
	private String point_msg;
	@Column(columnDefinition = "int default 0")
	private int pointNum;// 邀请人数
	@Column(columnDefinition = "int default 0")
	private int enough_reduce;// 0为未参加满就减，1为已参加
	private String order_enough_reduce_id;// 对应的满就减id
	@Column(columnDefinition = "int default 0")
	private int enough_free;// 0为未参加满包邮，1为已参加
	private String order_enough_free_id;// 对应的满包邮id
	@Column(columnDefinition = "int default 0")
	private int order_enough_give_status;// 满就送状态，0为非满就送商品，1为满就送商品
	@Column(columnDefinition = "int default 0")
	private int order_enough_if_give;// 是否是满就送，赠送的商品。0为不送
										// 1为送。送的商品正常价格出售，只有满足订单金额时才已0元出售。
	@Column(precision = 12, scale = 2)
	private BigDecimal buyGift_amount;// 对应的满就送条件金额
	private Long buyGift_id;// 对应的满就送id
	@Column(columnDefinition = "int default 0")
	private int tax_invoice;// 是否支持增值税发票,默认为0不支持，1为支持
	@Column(columnDefinition = "int default 0")
	private int f_sale_type;// 是否为F码销售商品，0为不是F码销售商品，1为F码销售商品，F码商品不可以参加任何商城活动
	@Column(columnDefinition = "LongText")
	private String goods_f_code;// F码信息，使用json管理[{"code":xxx,"status":0},{"code":xxx,"status":1}]
	@Column(columnDefinition = "int default 0")
	private int advance_sale_type;// 是否为预售商品，0为非预售商品，1为预售商品,预售商品不可以参加任何商城活动
	@Temporal(TemporalType.DATE)
	private Date advance_date;// 预售时间
	private int goods_warn_inventory;// 商品预警数量,库存少于预警数量，
	@Column(columnDefinition = "int default 0")
	private int warn_inventory_status;// 预警状态，0为正常，-1为预警
	private Long goods_top_format_id;// 商品顶部版式id
	@Column(columnDefinition = "LongText")
	private String goods_top_format_content;// 商品顶部版式内容
	private Long goods_bottom_format_id;// 商品底部版式id
	@Column(columnDefinition = "LongText")
	private String goods_bottom_format_content;// 商品底部版式内容
	private String qr_img_path;// 商品二维码，V2.0 2015版新增功能，erikzhang
	@Column(precision = 12, scale = 2)
	private BigDecimal price_history;// 记录每次商品价格变动时的价格，用于发送收藏商品降价通知
	private String delivery_area;// 发货地址
	private Long delivery_area_id;// 发货地址Id
	@Column(columnDefinition = "int default 0")
	private int whether_free;// 是否有0元试用 0为无 1为是
	@Column(columnDefinition = "int default 0")
	private int weixin_recommend;// 微信端推荐， 1为推荐，推荐后在微信端首页显示
	@Temporal(TemporalType.DATE)
	private Date weixin_recommendTime;// 微信推荐时间，
	@Column(columnDefinition = "int default 0")
	private int weixin_hot;// 微信热卖， 1为热卖，设置后在微信首页显示
	@Temporal(TemporalType.DATE)
	private Date weixin_hotTime;// 微信热卖时间，
	@OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private Accessory ad_acc;// 广告图片
	private int site;// 发货地址 1广州、0深圳、-1香港
	private int number_days;//天数 1为 一天、2为两天、3为三天
	@OneToMany(mappedBy = "goods", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE) //<set>标签的inverse="true" 效果相同
	private List<CGoods> cgoods = new ArrayList<CGoods>();
	@Column(precision = 12, scale = 2)
	private BigDecimal weightiness; // 商品综合权重
	@Column(precision = 12, scale = 2)
	private BigDecimal sequence; //商品手动排序
	@Column(precision = 12, scale = 2)
	private BigDecimal weightiness_rate; // 用来提升自营店铺的商品排行
	@Column(columnDefinition = "int default 0")
	private int after_purchase; //复购率
//	@ManyToMany
//	@JoinTable(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_tag", joinColumns = @JoinColumn(name= "goods_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
//	private List<Tag> goods_tags = new ArrayList<Tag>();// 商品所属标签
	private String goods_msg; //商品违规下架附言.
	@Column(columnDefinition = "LongText")
	private String goods_tiered_price; // 商品阶梯价格
	@Column(columnDefinition = "int default 0")
	private int is_tag;// 该商品是否打过标签 0：未打过  1为打过 3:图文
	
	private String ksa_goods_name;// 阿文商品名称
	@Column(columnDefinition = "LongText")
	private String ksa_goods_detail;// 阿文商品描述
	@Column(columnDefinition = "LongText")
	private String ksa_features;// 阿文产品亮点
	private String goods_correlation;//商品关联商品
	@Column(columnDefinition = "int default 0")
	private int local_inventory; //商品库存 本地仓
	private int oversea_inventory; //商品库存 海外仓

	public BigDecimal getSequence() {
		return sequence;
	}

	public void setSequence(BigDecimal sequence) {
		this.sequence = sequence;
	}

	public Goods() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Goods(String goods_name, Store goods_store) {
		this.goods_name = goods_name;
		this.goods_store = goods_store;
	}
	
	public Goods(Long id, String goods_name) {
		super(id);
		this.goods_name = goods_name;
	}

	public Goods(Store goods_store, int goods_status) {
		super();
		this.goods_store = goods_store;
		this.goods_status = goods_status;
	}

	public Goods(Long id, String goods_name, BigDecimal goods_current_price,
			BigDecimal goods_price, Accessory goods_main_photo) {
		super.setId(id);
		this.goods_name = goods_name;
		this.goods_current_price = goods_current_price;
		this.goods_price = goods_price;
		this.goods_main_photo = goods_main_photo;
		// TODO Auto-generated constructor stub
	}

	public Goods(Long id, Accessory goods_main_photo) {
		super.setId(id);
		this.goods_main_photo = goods_main_photo;
	}

	public Goods(Long id, String goods_name, BigDecimal goods_current_price,
			int goods_inventory) {
		super.setId(id);
		this.goods_name = goods_name;
		this.goods_current_price = goods_current_price;
		this.goods_inventory = goods_inventory;
		// TODO Auto-generated constructor stub
	}

	public Goods(Long id, String goods_name, BigDecimal goods_current_price,
			int goods_collect, int goods_salenum, Accessory goods_main_photo) {
		super.setId(id);
		this.goods_name = goods_name;
		this.goods_current_price = goods_current_price;
		this.goods_collect = goods_collect;
		this.goods_salenum = goods_salenum;
		this.goods_main_photo = goods_main_photo;
		// TODO Auto-generated constructor stub
	}

	public Goods(Long id, String goods_name, BigDecimal goods_price, Store goods_store, Accessory goods_main_photo,
			List<Evaluate> evaluates, BigDecimal goods_current_price, BigDecimal well_evaluate, int goods_type) {
		super.setId(id);
		this.goods_name = goods_name;
		this.goods_price = goods_price;
		this.goods_store = goods_store;
		this.goods_main_photo = goods_main_photo;
		this.evaluates = evaluates;
		this.goods_current_price = goods_current_price;
		this.well_evaluate = well_evaluate;
		this.goods_type = goods_type;
	}
	
	public Goods(Long id, String goods_name, BigDecimal goods_price, BigDecimal goods_current_price,
			int goods_collect, BigDecimal well_evaluate, Accessory goods_main_photo) {
		super.setId(id);
		this.goods_name = goods_name;
		this.goods_price = goods_price;
		this.goods_current_price = goods_current_price;
		this.goods_collect = goods_collect;
		this.well_evaluate = well_evaluate;
		this.goods_main_photo = goods_main_photo;
	}

	public Goods(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}
	
	public Goods(Long id, String goods_name, BigDecimal goods_price, int goods_collect, Store goods_store,
		Accessory goods_main_photo, BigDecimal goods_current_price, BigDecimal well_evaluate) {
	super.setId(id);
	this.goods_name = goods_name;
	this.goods_price = goods_price;
	this.goods_collect = goods_collect;
	this.goods_store = goods_store;
	this.goods_main_photo = goods_main_photo;
	this.goods_current_price = goods_current_price;
	this.well_evaluate = well_evaluate;
}
	public String getPoint_msg() {
		return point_msg;
	}
	public void setPoint_msg(String point_msg) {
		this.point_msg = point_msg;
	}

	public int getPoint_status() {
		return point_status;
	}

	public void setPoint_status(int point_status) {
		this.point_status = point_status;
	}

	public int getStore_seckill_inventory() {
		return store_seckill_inventory;
	}

	public void setStore_seckill_inventory(int store_seckill_inventory) {
		this.store_seckill_inventory = store_seckill_inventory;
	}

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

	public String getGoods_correlation() {
		return goods_correlation;
	}

	public void setGoods_correlation(String goods_correlation) {
		this.goods_correlation = goods_correlation;
	}

	public String getKsa_goods_name() {
		return ksa_goods_name;
	}

	public void setKsa_goods_name(String ksa_goods_name) {
		this.ksa_goods_name = ksa_goods_name;
	}

	public String getKsa_goods_detail() {
		return ksa_goods_detail;
	}

	public void setKsa_goods_detail(String ksa_goods_detail) {
		this.ksa_goods_detail = ksa_goods_detail;
	}

	public String getKsa_features() {
		return ksa_features;
	}

	public void setKsa_features(String ksa_features) {
		this.ksa_features = ksa_features;
	}

	public int getIs_tag() {
		return is_tag;
	}

	public void setIs_tag(int is_tag) {
		this.is_tag = is_tag;
	}

	public int getStore_deals_inventory() {
		return store_deals_inventory;
	}

	public void setStore_deals_inventory(int store_deals_inventory) {
		this.store_deals_inventory = store_deals_inventory;
	}

	public BigDecimal getWeightiness_rate() {
		return weightiness_rate;
	}

	public void setWeightiness_rate(BigDecimal weightiness_rate) {
		this.weightiness_rate = weightiness_rate;
	}

	public String getGoods_tiered_price() {
		return goods_tiered_price;
	}

	public void setGoods_tiered_price(String goods_tiered_price) {
		this.goods_tiered_price = goods_tiered_price;
	}

//	public List<Tag> getGoods_tags() {
//		return goods_tags;
//	}
//
//	public void setGoods_tags(List<Tag> goods_tags) {
//		this.goods_tags = goods_tags;
//	}

	public String getGoods_msg() {
		return goods_msg;
	}

	public void setGoods_msg(String goods_msg) {
		this.goods_msg = goods_msg;
	}

	public boolean isStore_china() {
		return store_china;
	}

	public void setStore_china(boolean store_china) {
		this.store_china = store_china;
	}

	public Date getStore_china_time() {
		return store_china_time;
	}

	public void setStore_china_time(Date store_china_time) {
		this.store_china_time = store_china_time;
	}

	public Date getStore_deals_time() {
		return store_deals_time;
	}

	public boolean isStore_deals() {
		return store_deals;
	}

	public void setStore_deals(boolean store_deals) {
		this.store_deals = store_deals;
	}

	public Date isStore_deals_time() {
		return store_deals_time;
	}

	public void setStore_deals_time(Date store_deals_time) {
		this.store_deals_time = store_deals_time;
	}

	public String getPoint_id() {
		return point_id;
	}

	public void setPoint_id(String point_id) {
		this.point_id = point_id;
	}

	public int getPointNum() {
		return pointNum;
	}

	public void setPointNum(int pointNum) {
		this.pointNum = pointNum;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getAfter_purchase() {
		return after_purchase;
	}

	public void setAfter_purchase(int after_purchase) {
		this.after_purchase = after_purchase;
	}

	public String getOrder_enough_free_id() {
		return order_enough_free_id;
	}

	public BigDecimal getWeightiness() {
		return weightiness;
	}

	public void setWeightiness(BigDecimal weightiness) {
		this.weightiness = weightiness;
	}

	public void setOrder_enough_free_id(String order_enough_free_id) {
		this.order_enough_free_id = order_enough_free_id;
	}

	public int getEnough_free() {
		return enough_free;
	}

	public void setEnough_free(int enough_free) {
		this.enough_free = enough_free;
	}

	public String getFeatures_one() {
		return features_one;
	}

	public void setFeatures_one(String features_one) {
		this.features_one = features_one;
	}

	public String getFeatures_two() {
		return features_two;
	}

	public void setFeatures_two(String features_two) {
		this.features_two = features_two;
	}

	public String getFeatures_three() {
		return features_three;
	}

	public void setFeatures_three(String features_three) {
		this.features_three = features_three;
	}

	public String getFeatures_four() {
		return features_four;
	}

	public void setFeatures_four(String features_four) {
		this.features_four = features_four;
	}

	public String getFeatures_five() {
		return features_five;
	}

	public void setFeatures_five(String features_five) {
		this.features_five = features_five;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public List<CGoods> getCgoods() {
		return cgoods;
	}

	public void setCgoods(List<CGoods> cgoods) {
		this.cgoods = cgoods;
	}

	public int getNumber_days() {
		return number_days;
	}

	public void setNumber_days(int number_days) {
		this.number_days = number_days;
	}

	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}

	public BigDecimal getGoods_length() {
		return goods_length;
	}

	public void setGoods_length(BigDecimal goods_length) {
		this.goods_length = goods_length;
	}

	public BigDecimal getGoods_width() {
		return goods_width;
	}

	public void setGoods_width(BigDecimal goods_width) {
		this.goods_width = goods_width;
	}

	public BigDecimal getGoods_high() {
		return goods_high;
	}

	public void setGoods_high(BigDecimal goods_high) {
		this.goods_high = goods_high;
	}

	public int getGoods_global() {
		return goods_global;
	}

	public void setGoods_global(int goods_global) {
		this.goods_global = goods_global;
	}

	public Accessory getAccessory() {
		return accessory;
	}

	public void setAccessory(Accessory accessory) {
		this.accessory = accessory;
	}

	public int getWeixin_recommend() {
		return weixin_recommend;
	}

	public void setWeixin_recommend(int weixin_recommend) {
		this.weixin_recommend = weixin_recommend;
	}

	public Date getWeixin_recommendTime() {
		return weixin_recommendTime;
	}

	public void setWeixin_recommendTime(Date weixin_recommendTime) {
		this.weixin_recommendTime = weixin_recommendTime;
	}

	public int getWeixin_hot() {
		return weixin_hot;
	}

	public void setWeixin_hot(int weixin_hot) {
		this.weixin_hot = weixin_hot;
	}

	public Date getWeixin_hotTime() {
		return weixin_hotTime;
	}

	public void setWeixin_hotTime(Date weixin_hotTime) {
		this.weixin_hotTime = weixin_hotTime;
	}

	public int getEvaluate_count() {
		return evaluate_count;
	}

	public void setEvaluate_count(int evaluate_count) {
		this.evaluate_count = evaluate_count;
	}

	public String getGoods_details_mobile() {
		return goods_details_mobile;
	}

	public void setGoods_details_mobile(String goods_details_mobile) {
		this.goods_details_mobile = goods_details_mobile;
	}

	public int getWhether_free() {
		return whether_free;
	}

	public void setWhether_free(int whether_free) {
		this.whether_free = whether_free;
	}

	public String getDelivery_area() {
		return delivery_area;
	}

	public void setDelivery_area(String delivery_area) {
		this.delivery_area = delivery_area;
	}

	public Long getDelivery_area_id() {
		return delivery_area_id;
	}

	public void setDelivery_area_id(Long delivery_area_id) {
		this.delivery_area_id = delivery_area_id;
	}

	public int getWarn_inventory_status() {
		return warn_inventory_status;
	}

	public void setWarn_inventory_status(int warn_inventory_status) {
		this.warn_inventory_status = warn_inventory_status;
	}

	public BigDecimal getPrice_history() {
		return price_history;
	}

	public void setPrice_history(BigDecimal price_history) {
		this.price_history = price_history;
	}

	public Long getActivity_goods_id() {
		return activity_goods_id;
	}

	public void setActivity_goods_id(Long activity_goods_id) {
		this.activity_goods_id = activity_goods_id;
	}

	public String getGoods_top_format_content() {
		return goods_top_format_content;
	}

	public void setGoods_top_format_content(String goods_top_format_content) {
		this.goods_top_format_content = goods_top_format_content;
	}

	public String getGoods_bottom_format_content() {
		return goods_bottom_format_content;
	}

	public void setGoods_bottom_format_content(
			String goods_bottom_format_content) {
		this.goods_bottom_format_content = goods_bottom_format_content;
	}

	public Long getGoods_top_format_id() {
		return goods_top_format_id;
	}

	public void setGoods_top_format_id(Long goods_top_format_id) {
		this.goods_top_format_id = goods_top_format_id;
	}

	public Long getGoods_bottom_format_id() {
		return goods_bottom_format_id;
	}

	public void setGoods_bottom_format_id(Long goods_bottom_format_id) {
		this.goods_bottom_format_id = goods_bottom_format_id;
	}

	public String getGoods_f_code() {
		return goods_f_code;
	}

	
	public BigDecimal getGoods_discount_rate() {
		return goods_discount_rate;
	}

	public void setGoods_discount_rate(BigDecimal goods_discount_rate) {
		this.goods_discount_rate = goods_discount_rate;
	}

	public int getGoods_warn_inventory() {
		return goods_warn_inventory;
	}

	public void setGoods_warn_inventory(int goods_warn_inventory) {
		this.goods_warn_inventory = goods_warn_inventory;
	}

	public void setGoods_f_code(String goods_f_code) {
		this.goods_f_code = goods_f_code;
	}

	public Date getAdvance_date() {
		return advance_date;
	}

	public void setAdvance_date(Date advance_date) {
		this.advance_date = advance_date;
	}

	public int getF_sale_type() {
		return f_sale_type;
	}

	public void setF_sale_type(int f_sale_type) {
		this.f_sale_type = f_sale_type;
	}

	public int getAdvance_sale_type() {
		return advance_sale_type;
	}

	public void setAdvance_sale_type(int advance_sale_type) {
		this.advance_sale_type = advance_sale_type;
	}

	public int getTax_invoice() {
		return tax_invoice;
	}

	public void setTax_invoice(int tax_invoice) {
		this.tax_invoice = tax_invoice;
	}

	public Long getCombin_suit_id() {
		return combin_suit_id;
	}

	public void setCombin_suit_id(Long combin_suit_id) {
		this.combin_suit_id = combin_suit_id;
	}

	public Long getCombin_parts_id() {
		return combin_parts_id;
	}

	public void setCombin_parts_id(Long combin_parts_id) {
		this.combin_parts_id = combin_parts_id;
	}

	public Long getBuyGift_id() {
		return buyGift_id;
	}

	public void setBuyGift_id(Long buyGift_id) {
		this.buyGift_id = buyGift_id;
	}

	public String getQr_img_path() {
		return qr_img_path;
	}

	public void setQr_img_path(String qr_img_path) {
		this.qr_img_path = qr_img_path;
	}

	public int getEnough_reduce() {
		return enough_reduce;
	}

	public void setEnough_reduce(int enough_reduce) {
		this.enough_reduce = enough_reduce;
	}

	public String getOrder_enough_reduce_id() {
		return order_enough_reduce_id;
	}

	public void setOrder_enough_reduce_id(String order_enough_reduce_id) {
		this.order_enough_reduce_id = order_enough_reduce_id;
	}

	public String getGoods_service() {
		return goods_service;
	}

	public int getGoods_cod() {
		return goods_cod;
	}

	public void setGoods_cod(int goods_cod) {
		this.goods_cod = goods_cod;
	}

	public BigDecimal getBuyGift_amount() {
		return buyGift_amount;
	}

	public void setBuyGift_amount(BigDecimal buyGift_amount) {
		this.buyGift_amount = buyGift_amount;
	}

	public void setGoods_service(String goods_service) {
		this.goods_service = goods_service;
	}

	public String getPack_details() {
		return pack_details;
	}

	public void setPack_details(String pack_details) {
		this.pack_details = pack_details;
	}

	public int getOrder_enough_if_give() {
		return order_enough_if_give;
	}

	public void setOrder_enough_if_give(int order_enough_if_give) {
		this.order_enough_if_give = order_enough_if_give;
	}

	public int getOrder_enough_give_status() {
		return order_enough_give_status;
	}

	public void setOrder_enough_give_status(int order_enough_give_status) {
		this.order_enough_give_status = order_enough_give_status;
	}

	public int getPublish_goods_status() {
		return publish_goods_status;
	}

	public void setPublish_goods_status(int publish_goods_status) {
		this.publish_goods_status = publish_goods_status;
	}

	public int getMobile_recommend() {
		return mobile_recommend;
	}

	public void setMobile_recommend(int mobile_recommend) {
		this.mobile_recommend = mobile_recommend;
	}

	public Date getMobile_recommendTime() {
		return mobile_recommendTime;
	}

	public void setMobile_recommendTime(Date mobile_recommendTime) {
		this.mobile_recommendTime = mobile_recommendTime;
	}

	public int getMobile_hot() {
		return mobile_hot;
	}

	public void setMobile_hot(int mobile_hot) {
		this.mobile_hot = mobile_hot;
	}

	public Date getMobile_hotTime() {
		return mobile_hotTime;
	}

	public void setMobile_hotTime(Date mobile_hotTime) {
		this.mobile_hotTime = mobile_hotTime;
	}

	public Date getStore_creativity_time() {
		return store_creativity_time;
	}

	public void setStore_creativity_time(Date store_creativity_time) {
		this.store_creativity_time = store_creativity_time;
	}

	public List<ComplaintGoods> getCgs() {
		return cgs;
	}

	public void setCgs(List<ComplaintGoods> cgs) {
		this.cgs = cgs;
	}

	public List<GoodsCart> getCarts() {
		return carts;
	}

	public void setCarts(List<GoodsCart> carts) {
		this.carts = carts;
	}

	public BigDecimal getWell_evaluate() {
		return well_evaluate;
	}

	public void setWell_evaluate(BigDecimal well_evaluate) {
		this.well_evaluate = well_evaluate;
	}

	public BigDecimal getMiddle_evaluate() {
		return middle_evaluate;
	}

	public void setMiddle_evaluate(BigDecimal middle_evaluate) {
		this.middle_evaluate = middle_evaluate;
	}

	public BigDecimal getBad_evaluate() {
		return bad_evaluate;
	}

	public void setBad_evaluate(BigDecimal bad_evaluate) {
		this.bad_evaluate = bad_evaluate;
	}

	public User getUser_admin() {
		return user_admin;
	}

	public void setUser_admin(User user_admin) {
		this.user_admin = user_admin;
	}

	public int getGoods_type() {
		return goods_type;
	}

	public void setGoods_type(int goods_type) {
		this.goods_type = goods_type;
	}

	public BigDecimal getDescription_evaluate() {
		return description_evaluate;
	}

	public void setDescription_evaluate(BigDecimal description_evaluate) {
		this.description_evaluate = description_evaluate;
	}

	public List<ActivityGoods> getAg_goods_list() {
		return ag_goods_list;
	}

	public void setAg_goods_list(List<ActivityGoods> ag_goods_list) {
		this.ag_goods_list = ag_goods_list;
	}

	public int getGroup_buy() {
		return group_buy;
	}

	public void setGroup_buy(int group_buy) {
		this.group_buy = group_buy;
	}

	public int getZtc_status() {
		return ztc_status;
	}

	public void setZtc_status(int ztc_status) {
		this.ztc_status = ztc_status;
	}

	public int getZtc_price() {
		return ztc_price;
	}

	public void setZtc_price(int ztc_price) {
		this.ztc_price = ztc_price;
	}

	public Date getZtc_begin_time() {
		return ztc_begin_time;
	}

	public void setZtc_begin_time(Date ztc_begin_time) {
		this.ztc_begin_time = ztc_begin_time;
	}

	public int getZtc_gold() {
		return ztc_gold;
	}

	public void setZtc_gold(int ztc_gold) {
		this.ztc_gold = ztc_gold;
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

	public int getGoods_inventory() {
		return goods_inventory;
	}

	public void setGoods_inventory(int goods_inventory) {
		this.goods_inventory = goods_inventory;
	}

	public String getInventory_type() {
		return inventory_type;
	}

	public void setInventory_type(String inventory_type) {
		this.inventory_type = inventory_type;
	}

	public int getGoods_salenum() {
		return goods_salenum;
	}

	public void setGoods_salenum(int goods_salenum) {
		this.goods_salenum = goods_salenum;
	}

	public String getGoods_serial() {
		return goods_serial;
	}

	public void setGoods_serial(String goods_serial) {
		this.goods_serial = goods_serial;
	}

	public BigDecimal getGoods_price() {
		return goods_price;
	}

	public void setGoods_price(BigDecimal goods_price) {
		this.goods_price = goods_price;
	}

	public BigDecimal getStore_price() {
		return store_price;
	}

	public void setStore_price(BigDecimal store_price) {
		this.store_price = store_price;
	}

	public BigDecimal getGoods_weight() {
		return goods_weight;
	}

	public void setGoods_weight(BigDecimal goods_weight) {
		this.goods_weight = goods_weight;
	}

	public String getGoods_fee() {
		return goods_fee;
	}

	public void setGoods_fee(String goods_fee) {
		this.goods_fee = goods_fee;
	}

	public String getGoods_details() {
		return goods_details;
	}

	public void setGoods_details(String goods_details) {
		this.goods_details = goods_details;
	}

	public boolean isStore_recommend() {
		return store_recommend;
	}

	public void setStore_recommend(boolean store_recommend) {
		this.store_recommend = store_recommend;
	}

	public boolean isStore_creativity() {
		return store_creativity;
	}

	public void setStore_creativity(boolean store_creativity) {
		this.store_creativity = store_creativity;
	}

	public Date getStore_recommend_time() {
		return store_recommend_time;
	}

	public void setStore_recommend_time(Date store_recommend_time) {
		this.store_recommend_time = store_recommend_time;
	}

	public boolean isGoods_recommend() {
		return goods_recommend;
	}

	public void setGoods_recommend(boolean goods_recommend) {
		this.goods_recommend = goods_recommend;
	}

	public int getGoods_click() {
		return goods_click;
	}

	public void setGoods_click(int goods_click) {
		this.goods_click = goods_click;
	}

	public Store getGoods_store() {
		return goods_store;
	}

	public void setGoods_store(Store goods_store) {
		this.goods_store = goods_store;
	}

	public int getGoods_status() {
		return goods_status;
	}

	public void setGoods_status(int goods_status) {
		this.goods_status = goods_status;
	}

	public Date getGoods_seller_time() {
		return goods_seller_time;
	}

	public void setGoods_seller_time(Date goods_seller_time) {
		this.goods_seller_time = goods_seller_time;
	}

	public int getGoods_transfee() {
		return goods_transfee;
	}

	public void setGoods_transfee(int goods_transfee) {
		this.goods_transfee = goods_transfee;
	}

	public GoodsClass getGc() {
		return gc;
	}

	public void setGc(GoodsClass gc) {
		this.gc = gc;
	}

	public Accessory getGoods_main_photo() {
		return goods_main_photo;
	}

	public void setGoods_main_photo(Accessory goods_main_photo) {
		this.goods_main_photo = goods_main_photo;
	}

	public List<Accessory> getGoods_photos() {
		return goods_photos;
	}

	public void setGoods_photos(List<Accessory> goods_photos) {
		this.goods_photos = goods_photos;
	}

	public List<UserGoodsClass> getGoods_ugcs() {
		return goods_ugcs;
	}

	public void setGoods_ugcs(List<UserGoodsClass> goods_ugcs) {
		this.goods_ugcs = goods_ugcs;
	}

	public List<GoodsSpecProperty> getGoods_specs() {
		return goods_specs;
	}

	public void setGoods_specs(List<GoodsSpecProperty> goods_specs) {
		this.goods_specs = goods_specs;
	}

	public GoodsBrand getGoods_brand() {
		return goods_brand;
	}

	public void setGoods_brand(GoodsBrand goods_brand) {
		this.goods_brand = goods_brand;
	}

	public String getGoods_property() {
		return goods_property;
	}

	public void setGoods_property(String goods_property) {
		this.goods_property = goods_property;
	}

	public String getGoods_inventory_detail() {
		return goods_inventory_detail;
	}

	public void setGoods_inventory_detail(String goods_inventory_detail) {
		this.goods_inventory_detail = goods_inventory_detail;
	}

	public int getZtc_pay_status() {
		return ztc_pay_status;
	}

	public void setZtc_pay_status(int ztc_pay_status) {
		this.ztc_pay_status = ztc_pay_status;
	}

	public User getZtc_admin() {
		return ztc_admin;
	}

	public void setZtc_admin(User ztc_admin) {
		this.ztc_admin = ztc_admin;
	}

	public String getZtc_admin_content() {
		return ztc_admin_content;
	}

	public void setZtc_admin_content(String ztc_admin_content) {
		this.ztc_admin_content = ztc_admin_content;
	}

	public Date getZtc_apply_time() {
		return ztc_apply_time;
	}

	public void setZtc_apply_time(Date ztc_apply_time) {
		this.ztc_apply_time = ztc_apply_time;
	}

	public int getZtc_click_num() {
		return ztc_click_num;
	}

	public void setZtc_click_num(int ztc_click_num) {
		this.ztc_click_num = ztc_click_num;
	}

	public int getZtc_dredge_price() {
		return ztc_dredge_price;
	}

	public void setZtc_dredge_price(int ztc_dredge_price) {
		this.ztc_dredge_price = ztc_dredge_price;
	}

	public int getGoods_collect() {
		return goods_collect;
	}

	public void setGoods_collect(int goods_collect) {
		this.goods_collect = goods_collect;
	}

	public List<GroupGoods> getGroup_goods_list() {
		return group_goods_list;
	}

	public void setGroup_goods_list(List<GroupGoods> group_goods_list) {
		this.group_goods_list = group_goods_list;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public List<Evaluate> getEvaluates() {
		return evaluates;
	}

	public void setEvaluates(List<Evaluate> evaluates) {
		this.evaluates = evaluates;
	}

	public int getGoods_choice_type() {
		return goods_choice_type;
	}

	public void setGoods_choice_type(int goods_choice_type) {
		this.goods_choice_type = goods_choice_type;
	}

	public int getActivity_status() {
		return activity_status;
	}

	public void setActivity_status(int activity_status) {
		this.activity_status = activity_status;
	}

	public BigDecimal getGoods_current_price() {
		return goods_current_price;
	}

	public void setGoods_current_price(BigDecimal goods_current_price) {
		this.goods_current_price = goods_current_price;
	}

	public BigDecimal getGoods_volume() {
		return goods_volume;
	}

	public void setGoods_volume(BigDecimal goods_volume) {
		this.goods_volume = goods_volume;
	}

	public BigDecimal getMail_trans_fee() {
		return mail_trans_fee;
	}

	public void setMail_trans_fee(BigDecimal mail_trans_fee) {
		this.mail_trans_fee = mail_trans_fee;
	}

	public BigDecimal getExpress_trans_fee() {
		return express_trans_fee;
	}

	public void setExpress_trans_fee(BigDecimal express_trans_fee) {
		this.express_trans_fee = express_trans_fee;
	}

	public BigDecimal getEms_trans_fee() {
		return ems_trans_fee;
	}

	public void setEms_trans_fee(BigDecimal ems_trans_fee) {
		this.ems_trans_fee = ems_trans_fee;
	}

	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public int getCombin_status() {
		return combin_status;
	}

	public void setCombin_status(int combin_status) {
		this.combin_status = combin_status;
	}

	public Accessory getAd_acc() {
		return ad_acc;
	}

	public void setAd_acc(Accessory ad_acc) {
		this.ad_acc = ad_acc;
	}

}
