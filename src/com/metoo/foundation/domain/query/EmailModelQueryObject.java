package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class EmailModelQueryObject  extends QueryObject {

	public EmailModelQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EmailModelQueryObject(String currentPage, ModelAndView mv, String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public EmailModelQueryObject(String construct, String currentPage, ModelAndView mv, String orderBy,
			String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	
}
