package com.metoo.foundation.domain.query;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class UserRecommendQueryObject extends QueryObject{

	public UserRecommendQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserRecommendQueryObject(String currentPage, ModelAndView mv, String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

	public UserRecommendQueryObject(String construct, String currentPage, ModelAndView mv, String orderBy,
			String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}

}
