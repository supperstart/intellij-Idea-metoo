package com.metoo.foundation.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.Address;

public interface IAddressService {
	
	
	
	/**
	 * 保存一个Address，如果保存成功返回true，否则返回false
	 * 
	 * @param instance
	 * @return 是否保存成功
	 */
	boolean save(Address instance);
	
	/**
	 * 根据一个ID得到Address
	 * 
	 * @param id
	 * @return
	 */
	Address getObjById(Long id);
	
	/**
	 * 删除一个Address
	 * 
	 * @param id
	 * @return
	 */
	boolean delete(Long id);
	
	/**
	 * 批量删除Address
	 * 
	 * @param ids
	 * @return
	 */
	boolean batchDelete(List<Serializable> ids);
	
	/**
	 * 通过一个查询对象得到Address
	 * 
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * 更新一个Address
	 * 
	 * @param id
	 *            需要更新的Address的id
	 * @param dir
	 *            需要更新的Address
	 */
	boolean update(Address instance);
	/**
	 * 
	 * @param query 查询语句
	 * @param params 参数
	 * @param begin 
	 * @param max 
	 * @return
	 */
	List<Address> query(String query, Map params, int begin, int max);
	
	Address getObjByProperty(String construct, String propertyName, Object value);

}
