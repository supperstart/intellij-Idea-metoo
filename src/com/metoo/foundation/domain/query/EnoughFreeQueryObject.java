package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class EnoughFreeQueryObject extends QueryObject{
	public EnoughFreeQueryObject(String currentPage, ModelAndView mv,
			String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public EnoughFreeQueryObject(String construct, String currentPage,
			ModelAndView mv, String orderBy, String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public EnoughFreeQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}
}
