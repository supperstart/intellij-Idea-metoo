package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.PointRecord;

public interface IPointRecordService {
	
	public boolean save(PointRecord pr);

	public boolean delete(Long id);

	public boolean update(PointRecord pr);

	IPageList list(IQueryObject properties);

	public PointRecord getObjById(Long id);

	public PointRecord getObjByProperty(String construct,String propertyName,String value);

	public List<PointRecord> query(String query, Map params, int begin, int max);

}
