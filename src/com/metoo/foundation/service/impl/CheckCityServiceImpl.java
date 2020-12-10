package com.metoo.foundation.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.foundation.domain.CheckCity;
import com.metoo.foundation.service.ICheckCityService;
@Service
@Transactional
public class CheckCityServiceImpl implements ICheckCityService{
	@Resource(name = "checkCityDao")
	private IGenericDAO<CheckCity> checkCityDao;
	
	@Override
	public List<CheckCity> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.query(query, params, begin, max);
	}

}
