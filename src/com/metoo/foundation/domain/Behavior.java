package com.metoo.foundation.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;
/**
 * 
 * @author Administrator
 * 
 * @Title Tag.class
 * 
 * @Description 用户行为
 * 
 * @Company 
 * 
 * @Date 2020-3-30
 *
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "behavior")
public class Behavior extends IdEntity{

	private String behavior_name; // 行为名称
	@Column(precision = 12, scale = 2)
	private BigDecimal behavior_weightiness; //行为权重 
	private int type; // 行为类型 1：浏览 2：收藏 3：取消收藏 4：加购 5：取消加购 6：购买 7：退货 8：评论 9：搜索 
	
	
	public String getBehavior_name() {
		return behavior_name;
	}
	public void setBehavior_name(String behavior_name) {
		this.behavior_name = behavior_name;
	}
	public BigDecimal getBehavior_weightiness() {
		return behavior_weightiness;
	}
	public void setBehavior_weightiness(BigDecimal behavior_weightiness) {
		this.behavior_weightiness = behavior_weightiness;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
