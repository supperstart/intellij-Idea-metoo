package com.metoo.foundation.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.service.ICGoodsService;

@Service
@Transactional
public class ICGoodsServiceImpl implements ICGoodsService{
	
	@Resource(name = "CGoods")
	private IGenericDAO<CGoods> cGoodsDao;
	
	
	@Override
	public CGoods getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.cGoodsDao.get(id);
	}

	@Override
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		try {
			this.cGoodsDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public boolean save(CGoods cgds) {
		// TODO Auto-generated method stub
		try {
			this.cGoodsDao.save(cgds);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean update(CGoods cgoods) {
		// TODO Auto-generated method stub
		try {
			this.cGoodsDao.update(cgoods);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public List<CGoods> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.cGoodsDao.query(query, params, begin, max);
	}


}
