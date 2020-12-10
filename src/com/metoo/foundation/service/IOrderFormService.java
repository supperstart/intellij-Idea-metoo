package com.metoo.foundation.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.OrderForm;

public interface IOrderFormService {
	/**
	 * 保存一个OrderForm，如果保存成功返回true，否则返回false
	 * 
	 * @param instance
	 * @return 是否保存成功
	 */
	boolean save(OrderForm instance);

	/**
	 * 根据一个ID得到OrderForm
	 * 
	 * @param id
	 * @return
	 */
	OrderForm getObjById(Long id);

	/**
	 * 删除一个OrderForm
	 * 
	 * @param id
	 * @return
	 */
	boolean delete(Long id);

	/**
	 * 批量删除OrderForm
	 * 
	 * @param ids
	 * @return
	 */
	boolean batchDelete(List<Serializable> ids);

	/**
	 * 通过一个查询对象得到OrderForm
	 * 
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);

	/**
	 * 更新一个OrderForm
	 * 
	 * @param id
	 *            需要更新的OrderForm的id
	 * @param dir
	 *            需要更新的OrderForm
	 */
	boolean update(OrderForm instance);

	/**
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<OrderForm> query(String query, Map params, int begin, int max);

	/**
	 * 自定义查询语句，可以查询指定字段并返回，如select id from OrderForm obj where xxxx 返回id集合
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List  queryFromOrderForm(String query, Map params, int begin, int max);
	
	String orderMain(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language);


}
