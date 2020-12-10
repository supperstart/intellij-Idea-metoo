package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class ExpressCompanyCommonQueryObject extends QueryObject {

	public ExpressCompanyCommonQueryObject(String currentPage, ModelAndView mv,
			String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public ExpressCompanyCommonQueryObject(String construct,
			String currentPage, ModelAndView mv, String orderBy,
			String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public ExpressCompanyCommonQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}
}
