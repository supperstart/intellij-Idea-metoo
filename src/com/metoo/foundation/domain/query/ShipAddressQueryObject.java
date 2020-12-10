package com.metoo.foundation.domain.query;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.QueryObject;

public class ShipAddressQueryObject extends QueryObject {
	public ShipAddressQueryObject(String currentPage, ModelAndView mv,
			String orderBy, String orderType) {
		super(currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}
	public ShipAddressQueryObject(String construct, String currentPage,
			ModelAndView mv, String orderBy, String orderType) {
		super(construct, currentPage, mv, orderBy, orderType);
		// TODO Auto-generated constructor stub
	}
	public ShipAddressQueryObject() {
		super();
		// TODO Auto-generated constructor stub
	}
}
