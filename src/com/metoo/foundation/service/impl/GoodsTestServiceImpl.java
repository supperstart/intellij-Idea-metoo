package com.metoo.foundation.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.foundation.domain.GoodsTest;
import com.metoo.foundation.service.IGoodsTestService;
@Service
@Transactional
public class GoodsTestServiceImpl implements IGoodsTestService{

	@Resource(name = "goodsTestDAO")
	private IGenericDAO<GoodsTest> goodsTestDao;
	
	public GoodsTest getObjById(Long id) {
	GoodsTest goodsTest = this.goodsTestDao.get(id);
	if(goodsTest != null){
		return goodsTest;
	}
		return null;
	}

}
