package com.metoo.foundation.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "userlog")
public class UserLog extends IdEntity{

	private int type;// 1：用户信息 2：支付行为 
	@Column(columnDefinition = "int default 0")
	private int logType; //日志类型 0:商家  1:自营
	//1用户信息
	private Long userId; // 用户ID
	@Column(unique = true)
	private String userName; // 用户姓名
	private int years; // 用户年龄
	private int sex; // 用户性别 （性别对使用购物车的影响）
	
	//2商品信息 -- 一个订单包含多个商品时记录使用json
	private Long goodsId; // 商品ID
	private String goodsName; // 商品名称
	private String goodsMainImage; // 商品主图
	private String goodsCount; // 购买商品数量
	private String goodsLabel; // 商品标签
	private String goodsinfo; // 商品json信息
	
	//3店铺信息
	private Long storeId; // 店铺ID
	private String storeName; // 店铺名称
	private String storeLogo; // 店铺LOGO
	private String storeGrade; // 店铺类型 
	private String storeStatus; // 店铺状态
	  
	//4浏览相关
	private String clikeForm; // 浏览来源 
	private int clickCount; // 浏览次数
	private String clickBehavior; // 浏览行为 例如：浏览-购买， 浏览-加购
	
	//5登陆相关 
	private Date lastLoginDate; // 上次登陆时间
	private Date loginTime; // 登陆时间 
	private int loginCount; // 登陆次数 
	private String lastLoginIp; // 上次登录IP
	private String loginIp; // 登陆Ip
	private String loginType; // 1：APP 2:PC
	
	//6支付相关
	@Column(precision = 12, scale = 2)
	private BigDecimal order_total; //用户支付总额度
	private BigDecimal finishingRate; // 支付完成率 （支付的金额对完成率的影响）（性别对支付的影响）（不同属性商品的支付完成率）  
	private String paymentMethod; // 常用的支付方式 1：货到付款 2：线上支付  
	private String paymentMethodRate;// 支付方式所占比率
	private Date orderIntervalTime; //下单和支付的间隔时间
	private String orderName; // 收货人姓名
	
	//7搜索相关
	//private String searchEntrance; // 搜索入口
	private String searchContent; // 搜索内容
	private String searchComparison;//比对搜索和最后成交商品的分类和属性 
	private String searchSemantic; //搜索关键词词义属性（目的性，商品性。如：“质量好的MP3”“N730手机”） 
	private String searchChange; // 搜索的变化 更改搜索词 热门搜索 
	
	//8购物车相关
	private int addCart; // 添加购物车 
	private Date cartIntervalTime; // 添加购物车和提交购物车的时间间隔
	private BigDecimal cartPrice; // 记录购物车价格 （记录大额商品和小额商品用户加购-结算的时间）
	private BigDecimal cencelRate; // 购物车取消率
	
	//9评价相关
	private String evaluateContent; // 评价内容
	private String goodsPhotos;// 评论图片
	private String addEvaluateContent; // 追评内容
	private String addGoodsPhotos; // 追评图片
	private String evaluateIntervalTime; // 商品评论和下单时间间隔
	private String addEvaluateIntervalTime; // 追平时间和评论时间间隔
	
	//10收藏相关
	private int favoriteType; // 收藏类型 商品 or 店铺
	private int favoriteGoodsNum;//商品收藏数量
	private int favoriteStoreNum;//店铺收藏数量
	
	//11抽奖次数来源
	private String lotteryForm;// 添加抽奖次数来源
	private Date lotteryTime;// 抽奖次数获取时间
	private int lotteryNumber;//当前获取抽奖次数
	
	//用户行为
	//private Behavior behavior; // 用户行为记录
	private Long behavior_id; // 行为id
	private String behaviorType; //行为类型
	
	//用户标签-未使用
	private UserTag userTag;
	
	public String getClickBehavior() {
		return clickBehavior;
	}

	public void setClickBehavior(String clickBehavior) {
		this.clickBehavior = clickBehavior;
	}

	public String getLotteryForm() {
		return lotteryForm;
	}

	public void setLotteryForm(String lotteryForm) {
		this.lotteryForm = lotteryForm;
	}

	public Date getLotteryTime() {
		return lotteryTime;
	}

	public void setLotteryTime(Date lotteryTime) {
		this.lotteryTime = lotteryTime;
	}

	public int getLotteryNumber() {
		return lotteryNumber;
	}

	public void setLotteryNumber(int lotteryNumber) {
		this.lotteryNumber = lotteryNumber;
	}

	public Long getBehavior_id() {
		return behavior_id;
	}

	public void setBehavior_id(Long behavior_id) {
		this.behavior_id = behavior_id;
	}

	public String getBehaviorType() {
		return behaviorType;
	}

	public void setBehaviorType(String behaviorType) {
		this.behaviorType = behaviorType;
	}

	public UserTag getUserTag() {
		return userTag;
	}

	public void setUserTag(UserTag userTag) {
		this.userTag = userTag;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getYears() {
		return years;
	}

	public void setYears(int years) {
		this.years = years;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getGoodsMainImage() {
		return goodsMainImage;
	}

	public void setGoodsMainImage(String goodsMainImage) {
		this.goodsMainImage = goodsMainImage;
	}

	public String getGoodsCount() {
		return goodsCount;
	}

	public void setGoodsCount(String goodsCount) {
		this.goodsCount = goodsCount;
	}

	public String getGoodsLabel() {
		return goodsLabel;
	}

	public void setGoodsLabel(String goodsLabel) {
		this.goodsLabel = goodsLabel;
	}

	public String getGoodsinfo() {
		return goodsinfo;
	}

	public void setGoodsinfo(String goodsinfo) {
		this.goodsinfo = goodsinfo;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreLogo() {
		return storeLogo;
	}

	public void setStoreLogo(String storeLogo) {
		this.storeLogo = storeLogo;
	}

	public String getStoreGrade() {
		return storeGrade;
	}

	public void setStoreGrade(String storeGrade) {
		this.storeGrade = storeGrade;
	}

	public String getStoreStatus() {
		return storeStatus;
	}

	public void setStoreStatus(String storeStatus) {
		this.storeStatus = storeStatus;
	}

	public String getClikeForm() {
		return clikeForm;
	}

	public void setClikeForm(String clikeForm) {
		this.clikeForm = clikeForm;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}


	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public BigDecimal getOrder_total() {
		return order_total;
	}

	public void setOrder_total(BigDecimal order_total) {
		this.order_total = order_total;
	}

	public BigDecimal getFinishingRate() {
		return finishingRate;
	}

	public void setFinishingRate(BigDecimal finishingRate) {
		this.finishingRate = finishingRate;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getPaymentMethodRate() {
		return paymentMethodRate;
	}

	public void setPaymentMethodRate(String paymentMethodRate) {
		this.paymentMethodRate = paymentMethodRate;
	}

	public Date getOrderIntervalTime() {
		return orderIntervalTime;
	}

	public void setOrderIntervalTime(Date orderIntervalTime) {
		this.orderIntervalTime = orderIntervalTime;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public String getSearchContent() {
		return searchContent;
	}

	public void setSearchContent(String searchContent) {
		this.searchContent = searchContent;
	}

	public String getSearchComparison() {
		return searchComparison;
	}

	public void setSearchComparison(String searchComparison) {
		this.searchComparison = searchComparison;
	}

	public String getSearchSemantic() {
		return searchSemantic;
	}

	public void setSearchSemantic(String searchSemantic) {
		this.searchSemantic = searchSemantic;
	}

	public String getSearchChange() {
		return searchChange;
	}

	public void setSearchChange(String searchChange) {
		this.searchChange = searchChange;
	}

	public int getAddCart() {
		return addCart;
	}

	public void setAddCart(int addCart) {
		this.addCart = addCart;
	}

	public Date getCartIntervalTime() {
		return cartIntervalTime;
	}

	public void setCartIntervalTime(Date cartIntervalTime) {
		this.cartIntervalTime = cartIntervalTime;
	}

	public BigDecimal getCartPrice() {
		return cartPrice;
	}

	public void setCartPrice(BigDecimal cartPrice) {
		this.cartPrice = cartPrice;
	}

	public BigDecimal getCencelRate() {
		return cencelRate;
	}

	public void setCencelRate(BigDecimal cencelRate) {
		this.cencelRate = cencelRate;
	}

	public String getEvaluateContent() {
		return evaluateContent;
	}

	public void setEvaluateContent(String evaluateContent) {
		this.evaluateContent = evaluateContent;
	}

	public String getGoodsPhotos() {
		return goodsPhotos;
	}

	public void setGoodsPhotos(String goodsPhotos) {
		this.goodsPhotos = goodsPhotos;
	}

	public String getAddEvaluateContent() {
		return addEvaluateContent;
	}

	public void setAddEvaluateContent(String addEvaluateContent) {
		this.addEvaluateContent = addEvaluateContent;
	}

	public String getAddGoodsPhotos() {
		return addGoodsPhotos;
	}

	public void setAddGoodsPhotos(String addGoodsPhotos) {
		this.addGoodsPhotos = addGoodsPhotos;
	}

	public String getEvaluateIntervalTime() {
		return evaluateIntervalTime;
	}

	public void setEvaluateIntervalTime(String evaluateIntervalTime) {
		this.evaluateIntervalTime = evaluateIntervalTime;
	}

	public String getAddEvaluateIntervalTime() {
		return addEvaluateIntervalTime;
	}

	public void setAddEvaluateIntervalTime(String addEvaluateIntervalTime) {
		this.addEvaluateIntervalTime = addEvaluateIntervalTime;
	}

	public int getFavoriteType() {
		return favoriteType;
	}

	public void setFavoriteType(int favoriteType) {
		this.favoriteType = favoriteType;
	}
	
	
}
