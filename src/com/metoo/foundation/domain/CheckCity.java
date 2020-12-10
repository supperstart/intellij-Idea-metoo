package com.metoo.foundation.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "check_city")
public class CheckCity extends IdEntity{
	
	private String name;//城市名称
	private String abbr;//城市代码 0:不可以派送 1：可以派送
	@ManyToOne(fetch = FetchType.LAZY)
	private ExpressCompany express_company;
	
	public String getAbbr() {
		return abbr;
	}
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ExpressCompany getExpress_company() {
		return express_company;
	}
	public void setExpress_company(ExpressCompany express_company) {
		this.express_company = express_company;
	}
	
	
}
