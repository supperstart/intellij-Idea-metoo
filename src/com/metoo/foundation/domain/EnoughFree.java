package com.metoo.foundation.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.iskyshop.core.domain.IdEntity;
import com.metoo.core.constant.Globals;
/**
 * <p>
 * Title: EnoughFree.java
 * </p>
 * 
 * <p>
 * Description: 满包邮实体类--满包邮为店铺活动
 * </p>
 * 
 * * <p>
 * Company: metoo
 * </p>
 * 
 * @author hk
 * 
 * @data 2019-11-18
 *
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "enough_free")
public class EnoughFree extends IdEntity{
	private String eftitle;//活动标题
	@Temporal(TemporalType.DATE)
	private Date efbegin_time;//活动开始时间
	@Temporal(TemporalType.DATE)
	private Date efend_time;//活动结束时间
	private int efsequence;//活动序号
	@Column(columnDefinition = "int default 0")
	private int efstatus;//审核状态默认为0待审核 10为 审核通过 -10为审核未通过 20为已结束。5为提交审核，此时商家不能再修改
	@Column(columnDefinition = "LongText")
	private String failed_reason;//请求失败的原因
	@Column(columnDefinition = "LongText")
	private String efcontent;//活动说明
	private String eftag;//活动标识
	private String store_id;// 对应的店铺id
	private String store_name;// 对应的店铺名字
	private int ef_type;// 满包邮类型，0为自营，1为商家
	@Column(columnDefinition = "LongText")
	private String efgoods_ids_json;// 活动商品json
	@Column(precision = 12, scale = 2)
	private BigDecimal  condition_amount;//满足满包邮条件金额 需要大于此金额才可满足满就送条件
	private int ef_frequency;//满包邮价格调整次数
	
	
	public String getEfgoods_ids_json() {
		return efgoods_ids_json;
	}
	public void setEfgoods_ids_json(String efgoods_ids_json) {
		this.efgoods_ids_json = efgoods_ids_json;
	}
	public int getEf_frequency() {
		return ef_frequency;
	}
	public void setEf_frequency(int ef_frequency) {
		this.ef_frequency = ef_frequency;
	}
	public String getEftitle() {
		return eftitle;
	}
	public void setEftitle(String eftitle) {
		this.eftitle = eftitle;
	}
	public Date getEfbegin_time() {
		return efbegin_time;
	}
	public void setEfbegin_time(Date efbegin_time) {
		this.efbegin_time = efbegin_time;
	}
	public Date getEfend_time() {
		return efend_time;
	}
	public void setEfend_time(Date efend_time) {
		this.efend_time = efend_time;
	}
	public int getEfsequence() {
		return efsequence;
	}
	public void setEfsequence(int efsequence) {
		this.efsequence = efsequence;
	}
	public int getEfstatus() {
		return efstatus;
	}
	public void setEfstatus(int efstatus) {
		this.efstatus = efstatus;
	}
	public String getFailed_reason() {
		return failed_reason;
	}
	public void setFailed_reason(String failed_reason) {
		this.failed_reason = failed_reason;
	}
	public String getEfcontent() {
		return efcontent;
	}
	public void setEfcontent(String efcontent) {
		this.efcontent = efcontent;
	}
	public String getEftag() {
		return eftag;
	}
	public void setEftag(String eftag) {
		this.eftag = eftag;
	}
	public String getStore_id() {
		return store_id;
	}
	public void setStore_id(String store_id) {
		this.store_id = store_id;
	}
	public String getStore_name() {
		return store_name;
	}
	public void setStore_name(String store_name) {
		this.store_name = store_name;
	}
	public int getEf_type() {
		return ef_type;
	}
	public void setEf_type(int ef_type) {
		this.ef_type = ef_type;
	}
	public BigDecimal getCondition_amount() {
		return condition_amount;
	}
	public void setCondition_amount(BigDecimal condition_amount) {
		this.condition_amount = condition_amount;
	}
	
	
}
