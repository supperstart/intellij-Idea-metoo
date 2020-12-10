package com.metoo.app.foundation.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.metoo.foundation.domain.Area;

public interface MIAreaService {
	/**
	 * 根据一个ID得到Area
	 * 
	 * @param id
	 * @return
	 */
	Area getObjById(Long id);
	
	/**
	 * 
	 * @param query
	 * @param params
	 * @param begin
	 * @param max
	 * @return
	 */
	List<Area> query(String query, Map params, int begin, int max);
	
	/**
	 * 获取区域最高级
	 * @param request
	 * @param response
	 * @param currentPage
	 * @return
	 */
	String areaParent(HttpServletRequest request, HttpServletResponse response, String currentPage);
	
}
