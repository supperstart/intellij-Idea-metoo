package com.metoo.foundation.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.GoodsSimilar;

public interface IGoodsSimilarService {

	/**
	 * @description 根据ID获取一个GoodsSimilar
	 * @param id
	 * @return
	 */
	GoodsSimilar getObjById(Long id);
	
	/**
	 * @description 保存一个GoodsSimilar 成功：true 失败：false
	 * @param instance
	 * @return 是否保存成功
	 */
	boolean save(GoodsSimilar instance);
	
	/**
	 * @description 根据ID删除一个GoodsSimilar
	 * @param id
	 * @return 是否删除成功
	 */
	boolean delete(Long id);
	
	/**
	 * @description 批量删除
	 * @param ids
	 * @return 批量删除是否成功
	 */
	boolean batchDelete(List<Serializable> ids);
	
	/**
	 * @description 通过一个查询对象得到一个 GoodsSimilar
	 * @param properties
	 * @return List<GoodsSimilar>
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * @description 更新一个GoodsSimilar
	 * @param instance
	 * @return 是否更新成功
	 */
	boolean update(GoodsSimilar instance);
	
	/**
	 * @description 根据sql查询GoodsSimilar
	 * @param query
	 * @param map
	 * @param begin
	 * @param max
	 * @return
	 */
	List<GoodsSimilar> query(String query, Map params, int begin, int max);

	/**
	 * 
	 * @param construct
	 * @param propertyName
	 * @param value
	 * @return
	 */
	GoodsSimilar getObjByProperty(String construct, String propertyName, Object value);
	
}
