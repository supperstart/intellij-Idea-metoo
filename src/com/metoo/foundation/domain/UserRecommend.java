package com.metoo.foundation.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
 * 
 * <p>
 * Title:UserRecommend.java
 * </p>
 * 
 * <p>
 * 	Description: 用户推荐日志类，用于智能推荐
 * </p>
 * 
 * @author Administrator
 * 
 * @date 2020/4/21
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "user_recommend")
public class UserRecommend extends IdEntity{
	
	private Long user_id;//用户
	
	private String userName;//用户名称
	
	private Long goods_id;//商品id
	
	private String goods_name;//商品名称
	
	private Long goods_brand_id;// 商品品牌id

	private int goods_click;// 商品当天浏点击量
	
	private Long category;//商品类别 用于给商品分类
	
	private Long store_id;// 对应的商家id

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getGoods_id() {
		return goods_id;
	}

	public void setGoods_id(Long goods_id) {
		this.goods_id = goods_id;
	}

	public String getGoods_name() {
		return goods_name;
	}

	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}

	public Long getGoods_brand_id() {
		return goods_brand_id;
	}

	public void setGoods_brand_id(Long goods_brand_id) {
		this.goods_brand_id = goods_brand_id;
	}

	public int getGoods_click() {
		return goods_click;
	}

	public void setGoods_click(int goods_click) {
		this.goods_click = goods_click;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	public Long getStore_id() {
		return store_id;
	}

	public void setStore_id(Long store_id) {
		this.store_id = store_id;
	}

	public UserRecommend() {
		super();
	}

	public UserRecommend(Long goods_id) {
		super();
		this.goods_id = goods_id;
	}

}
