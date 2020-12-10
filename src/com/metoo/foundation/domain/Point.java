package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;
/**
 * @description 邀请活动 -- 推广
 * @author Administrator
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "point")
public class Point extends IdEntity{
	private String title;// 活动标题
	private int pointNum;// 邀请数量
	@Temporal(TemporalType.DATE)
	private Date ptbegin_time;// 开始时间
	@Temporal(TemporalType.DATE)
	private Date ptend_time;// 结束时间
	@Column(columnDefinition = "LongText")
	private String goods_ids_json;// 活动商品json
	@Column(columnDefinition = "int default 0")
	private int type;//预留字段 活动类型 0：自营   1:商家
	private String rules;// 规则
	private String link;// 下载链接
	private Long store_id;// 活动店铺id
	@Column(columnDefinition = "int default 0")
	private int point_status;// 活动状态 0:开启  1:关闭 20为已结束
	
	public int getPoint_status() {
		return point_status;
	}
	public void setPoint_status(int point_status) {
		this.point_status = point_status;
	}
	public Long getStore_id() {
		return store_id;
	}
	public void setStore_id(Long store_id) {
		this.store_id = store_id;
	}
	public String getRules() {
		return rules;
	}
	public void setRules(String rules) {
		this.rules = rules;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Point() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Point(Long id, Date addTime) {
		super(id, addTime);
		// TODO Auto-generated constructor stub
	}
	public Point(Long id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	public Point(String title, int pointNum, Date ptbegin_time, Date ptend_time, String goods_ids_json, int type) {
		super();
		this.title = title;
		this.pointNum = pointNum;
		this.ptbegin_time = ptbegin_time;
		this.ptend_time = ptend_time;
		this.goods_ids_json = goods_ids_json;
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getPointNum() {
		return pointNum;
	}
	public void setPointNum(int pointNum) {
		this.pointNum = pointNum;
	}
	public Date getPtbegin_time() {
		return ptbegin_time;
	}
	public void setPtbegin_time(Date ptbegin_time) {
		this.ptbegin_time = ptbegin_time;
	}
	public Date getPtend_time() {
		return ptend_time;
	}
	public void setPtend_time(Date ptend_time) {
		this.ptend_time = ptend_time;
	}
	public String getGoods_ids_json() {
		return goods_ids_json;
	}
	public void setGoods_ids_json(String goods_ids_json) {
		this.goods_ids_json = goods_ids_json;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
