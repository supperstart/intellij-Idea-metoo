package com.metoo.foundation.service.impl;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.IntegralGoods;
import com.metoo.foundation.service.IIntegralGoodsService;

@Service
@Transactional
public class IntegralGoodsServiceImpl implements IIntegralGoodsService{
	@Resource(name = "integralGoodsDAO")
	private IGenericDAO<IntegralGoods> integralGoodsDao;
	
	public boolean save(IntegralGoods integralGoods) {
		/**
		 * init other field here
		 */
		try {
			this.integralGoodsDao.save(integralGoods);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public IntegralGoods getObjById(Long id) {
		IntegralGoods integralGoods = this.integralGoodsDao.get(id);
		if (integralGoods != null) {
			return integralGoods;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.integralGoodsDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> integralGoodsIds) {
		// TODO Auto-generated method stub
		for (Serializable id : integralGoodsIds) {
			delete((Long) id);
		}
		return true;
	}
	
	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(IntegralGoods.class,construct, query,
				params, this.integralGoodsDao);
		if (properties != null) {
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(pageObj.getCurrentPage() == null ? 0 : pageObj
						.getCurrentPage(), pageObj.getPageSize() == null ? 0
						: pageObj.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}
	
	public boolean update(IntegralGoods integralGoods) {
		try {
			this.integralGoodsDao.update( integralGoods);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<IntegralGoods> query(String query, Map params, int begin, int max){
		return this.integralGoodsDao.query(query, params, begin, max);
		
	}
}
