package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.StoreLog;

public interface IStoreLogService {
	public boolean save(StoreLog sl);

	public boolean delete(Long id);

	public boolean update(StoreLog acc);

	IPageList list(IQueryObject properties);

	public StoreLog getObjById(Long id);

	public StoreLog getObjByProperty(String construct,String propertyName,String value);

	public List<StoreLog> query(String query, Map params, int begin, int max);
}
