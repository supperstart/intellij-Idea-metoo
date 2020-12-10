package com.metoo.foundation.domain;

import javax.persistence.Column;

/*@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "c_goods_photo")*/
public class CgoodsPhoto{
	
	@Column(unique = true, nullable = false)
	private Long c_goods_id;
	@Column(unique = true, nullable = false)
	private Long c_photo_id;
	@Column(columnDefinition = "int default 0")
	private int sort;
	
	public Long getC_goods_id() {
		return c_goods_id;
	}
	public void setC_goods_id(Long c_goods_id) {
		this.c_goods_id = c_goods_id;
	}
	public Long getC_photo_id() {
		return c_photo_id;
	}
	public void setC_photo_id(Long c_photo_id) {
		this.c_photo_id = c_photo_id;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	
	
	
}
