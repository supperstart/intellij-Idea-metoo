package com.metoo.foundation.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/**
* <p>
 * Title: GoodsBehavior.java
 * </p>
 * 
 * <p>
 * Description: 产品相似表
 * </p>
 * 
 * <p>
 * Company: 
 * </p>
 * 
 * @author Administrator
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_similar")
public class GoodsSimilar extends IdEntity{

	private Long goods_main_id; // 主商品ID
	private Long goods_vice_id; // 次商品ID
	@Column(precision = 12, scale = 2)
	private Double be_similar; // 产品相似度
	@Column(columnDefinition = "int default 0")
	private int is_add; //
	
	public Long getGoods_main_id() {
		return goods_main_id;
	}
	public void setGoods_main_id(Long goods_main_id) {
		this.goods_main_id = goods_main_id;
	}
	public Long getGoods_vice_id() {
		return goods_vice_id;
	}
	public void setGoods_vice_id(Long goods_vice_id) {
		this.goods_vice_id = goods_vice_id;
	}
	
	public Double getBe_similar() {
		return be_similar;
	}
	public void setBe_similar(Double be_similar) {
		this.be_similar = be_similar;
	}
	public int getIs_add() {
		return is_add;
	}
	public void setIs_add(int is_add) {
		this.is_add = is_add;
	}
	public GoodsSimilar(Long id, Long goods_vice_id) {
		super(id);
		this.goods_vice_id = goods_vice_id;
	}
}
