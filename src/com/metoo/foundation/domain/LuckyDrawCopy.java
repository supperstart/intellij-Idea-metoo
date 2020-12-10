package com.metoo.foundation.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.metoo.core.constant.Globals;
import com.metoo.core.domain.IdEntity;

@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = Globals.DEFAULT_TABLE_SUFFIX + "lucky_draw_copy")
public class LuckyDrawCopy extends IdEntity{

	private String naem;
	private String nike_name;
	
	public String getNaem() {
		return naem;
	}
	public void setNaem(String naem) {
		this.naem = naem;
	}
	public String getNike_name() {
		return nike_name;
	}
	public void setNike_name(String nike_name) {
		this.nike_name = nike_name;
	}
	
}
