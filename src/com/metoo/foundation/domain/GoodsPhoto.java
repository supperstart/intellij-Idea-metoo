package com.metoo.foundation.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

/*@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "goods_photo")*/
public class GoodsPhoto{

	@Column(unique = true, nullable = false)
	private Long goods_id;
	@Column(unique = true, nullable = false)
	private Long photo_id;
	@Column(columnDefinition = "int default 0")
	private int sort;
	public Long getGoods_id() {
		return goods_id;
	}
	public void setGoods_id(Long goods_id) {
		this.goods_id = goods_id;
	}
	public Long getPhoto_id() {
		return photo_id;
	}
	public void setPhoto_id(Long photo_id) {
		this.photo_id = photo_id;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	
	
}
