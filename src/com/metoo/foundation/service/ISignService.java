package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.foundation.domain.Sign;

public interface ISignService {

	Sign getObjById(Long id);
	
	boolean save(Sign instance);
	
	boolean update(Sign instance);
	
	boolean delete(Long id);
	
	/**
	 * 
	 * @param query 查询语句
	 * @param params 参数
	 * @param begin 
	 * @param max 
	 * @return
	 */
	List<Sign> query(String query, Map params, int begin, int max);
	
}
