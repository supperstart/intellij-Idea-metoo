package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class ResQueryObject extends QueryObject {

	public ResQueryObject(String currentPage, ModelAndView mv, String orderBy,
			String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public ResQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResQueryObject(String construct, String currentPage,
			ModelAndView mv, String orderBy, String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}


}
