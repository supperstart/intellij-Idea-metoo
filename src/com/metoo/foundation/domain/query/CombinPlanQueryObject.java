package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class CombinPlanQueryObject extends QueryObject {

	public CombinPlanQueryObject(String currentPage, ModelAndView mv,
			String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public CombinPlanQueryObject(String construct, String currentPage,
			ModelAndView mv, String orderBy, String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public CombinPlanQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}
}
