package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.LuckyDraw;

public interface ILuckyDrawService {
	
	  boolean save(LuckyDraw luckyDraw);
	
	 boolean delete(Long id);
	
	 boolean update(LuckyDraw luckyDraw);
	
	 LuckyDraw getObjById(Long id); 
	
	 IPageList list(IQueryObject properties);
	
	 List<LuckyDraw> query(String query, Map params, int begin, int max);
}
