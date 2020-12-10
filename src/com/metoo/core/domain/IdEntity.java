package com.metoo.core.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.metoo.core.annotation.Lock;

/**
 * 
 * <p>
 * Title: IdEntity.java
 * </p>
 * 
 * <p>
 * Description:
 * 系统域模型基类，该类包含3个常用字段，其中id为自增长类型，该类实现序列化，只有序列化后才可以实现tomcat集群配置session共享
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: 湖南创发科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版
 * 
 * @warning 
 * 1.标注为@MappedSuperclass的类将不是一个完整的实体类，他将不会映射到数据库表，但是他的属性都将映射到其子类的数据库字段中。
 * 
 * 2.标注为@MappedSuperclass的类不能再标注@Entity或@Table注解，也无需实现序列化接口。
 * 
 * IDENTITY：采用数据库 ID自增长的方式来自增主键字段，Oracle 不支持这种方式；
 * AUTO： JPA自动选择合适的策略，是默认选项；
 * SEQUENCE：通过序列产生主键，通过 @SequenceGenerator 注解指定序列名，MySql 不支持这种方式；
 * TABLE：通过表产生主键，框架借由表模拟序列产生主键，使用该策略可以使应用更易于数据库移植。
 * 
 */
@MappedSuperclass
public class IdEntity implements Serializable {
	/**
	 * 序列化接口，自动生成序列号
	 */
	private static final long serialVersionUID = -7741168269971132706L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;// 域模型id，这里为自增类型
	private Date addTime;// 添加时间，这里为长时间格式
	@Lock
	@Column(columnDefinition = "int default 0")
	private int deleteStatus;// 是否删除,默认为0未删除，-1表示删除状态

	public IdEntity() {
		super();
	}

	public IdEntity(Long id, Date addTime) {
		super();
		this.id = id;
		this.addTime = addTime;
	}

	public IdEntity(Long id) {
		super();
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public int getDeleteStatus() {
		return deleteStatus;
	}

	public void setDeleteStatus(int deleteStatus) {
		this.deleteStatus = deleteStatus;
	}

}
