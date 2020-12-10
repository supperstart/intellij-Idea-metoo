package com.metoo.foundation.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.EnoughFree;
import com.metoo.foundation.domain.EnoughReduce;

public interface IEnoughFreeService {
	
	/**
	 * 保存一个EnoughFree 成功返回true 失败返回false
	 * @param instance
	 */
	boolean save(EnoughFree instance);
	
	/**
	 * 根据id获取EnoughFree
	 * @param id
	 * @return
	 */
	EnoughFree getObjById(Long id);
	
	/**
	 * 根据id删除一个EnoughFree
	 * @param id
	 * @return
	 */
	boolean delete(Long id);
	
	/**
	 * 批量删除EnoughFree
	 * @param ids
	 * @return
	 */
	boolean batchdelete(List<Serializable> ids);
	
	/**
	 *  通过一个查询对象得到EnoughFree
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * 更新一个EnoughFree
	 * @param instance
	 * @return
	 */
	boolean update(EnoughFree instance);
	
	/**
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<EnoughFree> query(String query, Map params, int begin, int max);
	
	
}
