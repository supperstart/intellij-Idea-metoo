package com.metoo.foundation.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.GroupJoiner;

public interface IGroupJoinerService {
	/**
	 * 保存一个GroupJoiner，如果保存成功返回true，否则返回false
	 * 
	 * @param instance
	 * @return 是否保存成功
	 */
	boolean save(GroupJoiner instance);
	
	/**
	 * 根据一个ID得到GroupGoods
	 * 
	 * @param id
	 * @return
	 */
	GroupJoiner getObjById(Long id);
	
	/**
	 * 删除一个GroupGoods
	 * 
	 * @param id
	 * @return
	 */
	boolean delete(Long id);
	
	/**
	 * 批量删除GroupGoods
	 * 
	 * @param ids
	 * @return
	 */
	boolean batchDelete(List<Serializable> ids);
	
	/**
	 * 通过一个查询对象得到GroupGoods
	 * 
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * 更新一个GroupGoods
	 * 
	 * @param id
	 *            需要更新的GroupGoods的id
	 * @param dir
	 *            需要更新的GroupGoods
	 */
	boolean update(GroupJoiner instance);
	/**
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<GroupJoiner> query(String query, Map params, int begin, int max);
}
