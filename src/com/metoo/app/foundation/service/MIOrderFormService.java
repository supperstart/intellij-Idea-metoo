package com.metoo.app.foundation.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.OrderForm;

public interface MIOrderFormService {
	/**
	 * 根据一个ID得到OrderForm
	 * 
	 * @param id
	 * @return
	 */
	OrderForm getObjById(Long id);
	/**
	 * 通过一个查询对象得到OrderForm
	 * 
	 * @param properties
	 * @return
	 */
	IPageList list(IQueryObject properties);
	
	/**
	 * 根据一个查询条件及其参数，还有开始查找的位置和查找的个数来查找任意类型的对象。
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<OrderForm> query(String query, Map params, int begin, int max);
	
	//多条件查询订单列表
	String order(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language);
	
	String orderMain(HttpServletRequest request,
			HttpServletResponse response, String currentPage, String order_id,
			String beginTime, String endTime, String order_status, String token, String name, String language);
}
