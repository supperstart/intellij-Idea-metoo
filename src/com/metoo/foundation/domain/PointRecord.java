package com.metoo.foundation.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "point_record")
public class PointRecord extends IdEntity{
	
	private Long goods_id;// 商品id
	private Long user_id;// 用户id
	@Column(columnDefinition = "int default 0")
	private int point_num;// 兑换消耗人数
	private Date pay_time; //兑换时间
	@Column(columnDefinition = "int default 0")
	private int point_status; // 该兑换记录状态 0: 已兑换 1 过期未兑换  (兑换方式：加购兑换)
	private int remaining_num; //该用户剩余兑换人数
	public Long getGoods_id() {
		return goods_id;
	}
	public void setGoods_id(Long goods_id) {
		this.goods_id = goods_id;
	}
	public Long getUser_id() {
		return user_id;
	}
	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}
	public int getPoint_num() {
		return point_num;
	}
	public void setPoint_num(int point_num) {
		this.point_num = point_num;
	}
	public Date getPay_time() {
		return pay_time;
	}
	public void setPay_time(Date pay_time) {
		this.pay_time = pay_time;
	}
	public int getPoint_status() {
		return point_status;
	}
	public void setPoint_status(int point_status) {
		this.point_status = point_status;
	}
	public int getRemaining_num() {
		return remaining_num;
	}
	public void setRemaining_num(int remaining_num) {
		this.remaining_num = remaining_num;
	}
	
}
