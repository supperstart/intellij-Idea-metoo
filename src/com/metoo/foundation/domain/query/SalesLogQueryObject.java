package com.metoo.foundation.domain.query;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class SalesLogQueryObject extends QueryObject {
	public SalesLogQueryObject(String currentPage, ModelAndView mv,
			String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}
	public SalesLogQueryObject(String construct, String currentPage,
			ModelAndView mv, String orderBy, String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}
	public SalesLogQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}
}
