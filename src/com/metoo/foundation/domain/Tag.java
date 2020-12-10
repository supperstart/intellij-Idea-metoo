package com.metoo.foundation.domain;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
 * @Description 标签表：用来描述商品，用户等属性
 * 
 * @Company 
 * 
 * @Date 2020-3-30
 *
 */

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "tag")
public class Tag extends IdEntity{

	private String tag_name; // 标签名称
	private String type; // 商品标签 or 类目标签 or 用户标签
	private int sequence; // 索引
	private int level; //标签等级
	private Tag parent;// 父级标签
	@OneToMany(mappedBy = "parent")
	@OrderBy(value = "sequence asc")
//	private Set<Tag> childs = new TreeSet<Tag>(); // 子标签
//	@ManyToMany(mappedBy = "goods_tags")
//	private List<Goods> goods = new ArrayList<Goods>();
//	
//	public List<Goods> getGoods() {
//		return goods;
//	}
//	public void setGoods(List<Goods> goods) {
//		this.goods = goods;
//	}
	public String getTag_name() {
		return tag_name;
	}
	public void setTag_name(String tag_name) {
		this.tag_name = tag_name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Tag getParent() {
		return parent;
	}
	public void setParent(Tag parent) {
		this.parent = parent;
	}
//	public Set<Tag> getChilds() {
//		return childs;
//	}
//	public void setChilds(Set<Tag> childs) {
//		this.childs = childs;
//	}
//	
}
