package com.metoo.foundation.service;


import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.Point;

public interface IPointService {


	public boolean save(Point point);
	
	public Point getObjById(Long id);
	
	public boolean update(Point point);
	
	public boolean delete(Long id);
	
	IPageList list(IQueryObject properties);
	
	public List<Point> query(String query, Map params, int begin, int max);
}
