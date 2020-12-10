package com.metoo.app.foundation.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metoo.foundation.domain.Goods;

public interface MIGoodsService {
	/**
	 * 根据一个ID得到Goods
	 * 
	 * @param id
	 * @return
	 */
	Goods getObjById(Long id);
	
	/**
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<Goods> query(String query, Map params, int begin, int max);

	String goodsdetail(HttpServletRequest request,
			HttpServletResponse response, String id);
}
