package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.foundation.domain.CGoods;

public interface ICGoodsService {
	
	CGoods getObjById(Long id);
	
	boolean delete(Long id);
	
	public boolean save(CGoods cgds);
	
	boolean update(CGoods instance);
	
	List<CGoods> query(String query, Map params, int begin, int max);
}
