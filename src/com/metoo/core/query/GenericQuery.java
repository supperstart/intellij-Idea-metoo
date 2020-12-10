package com.metoo.core.query;

import java.util.List;
import java.util.Map;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.query.support.IQuery;

/**
 * 
 * <p>
 * Title: GenericQuery.java
 * </p>
 * 
 * <p>
 * Description:面向对象基础查询类，通过查询对象的封装完成查询信息
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: 湖南创发科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版 
 */
public class GenericQuery implements IQuery {

	private IGenericDAO dao;

	private int begin;

	private int max;

	private Map params;

	/**
	 * 查询接口构造函数，构造一个基类查询
	 * 
	 * @param dao
	 */
	public GenericQuery(IGenericDAO dao) {
		this.dao = dao;
	}

	/**
	 * 根据查询条件查询结果(所有结果值)，返回结果数据集合List
	 */
	public List getResult(String construct, String condition) {
		// TODO Auto-generated method stub
		return dao.find(construct, condition, this.params, begin, max);
	}

	/**
	 * 根据查询条件查询对应区间的结果值，返回结果数据集合List
	 */
	public List getResult(String construct, String condition, int begin, int max) {
		// TODO Auto-generated method stub
		Object[] params = null;
		return this.dao.find(construct, condition, this.params, begin, max);
	}

	/**
	 * 根据查询条件查询结果总数，使用count(obj.id)来完成，该方法仅仅用在计算分页信息
	 */
	public int getRows(String condition) {
		// TODO Auto-generated method stub
		int n = condition.toLowerCase().indexOf("order by");
		Object[] params = null;
		if (n > 0) {
			condition = condition.substring(0, n);
		}
		List ret = dao.query(condition, this.params, 0, 0);
		if (ret != null && ret.size() > 0) {
			return ((Long) ret.get(0)).intValue();
		} else {
			return 0;
		}
	}

	public void setFirstResult(int begin) {
		this.begin = begin;
	}

	public void setMaxResults(int max) {
		this.max = max;
	}

	@Override
	public void setParaValues(Map params) {
		// TODO Auto-generated method stub
		this.params = params;
	}

}
