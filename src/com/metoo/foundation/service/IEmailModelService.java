package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.EmailModel;

public interface IEmailModelService {
	
	/**
	 * 根据 ID 获取一个 EmailModel对象
	 * @param id
	 * @return
	 */
	EmailModel getObjById(Long id);
	
	/**
	 * 保存一个EmailModel对象
	 * @param instance
	 * @return
	 */
	boolean save(EmailModel instance);
	
	/**
	 * 更新一个EmailModel对象
	 * @param instance
	 * @return
	 */
	boolean update(EmailModel instance);
	
	/**
	 * 根据id删除一个EmailModel对象
	 * @param id
	 * @return
	 */
	boolean remove(Long id);
	
	/**
	 * 通过一个查询对象获取EmailModel对象集合
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * 通过jpql获取EmailModel集合对象
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<EmailModel> query(String query, Map params, int begin, int max);
	
}
