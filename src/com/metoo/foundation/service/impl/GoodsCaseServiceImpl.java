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
import com.metoo.foundation.domain.GoodsCase;
import com.metoo.foundation.service.IGoodsCaseService;

@Service
@Transactional
public class GoodsCaseServiceImpl implements IGoodsCaseService{
	@Resource(name = "goodsCaseDAO")
	private IGenericDAO<GoodsCase> goodsCaseDao;
	
	public boolean save(GoodsCase goodsCase) {
		/**
		 * init other field here
		 */
		try {
			this.goodsCaseDao.save(goodsCase);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GoodsCase getObjById(Long id) {
		GoodsCase goodsCase = this.goodsCaseDao.get(id);
		if (goodsCase != null) {
			return goodsCase;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.goodsCaseDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> goodsCaseIds) {
		// TODO Auto-generated method stub
		for (Serializable id : goodsCaseIds) {
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
		GenericPageList pList = new GenericPageList(GoodsCase.class,construct,query,
				params, this.goodsCaseDao);
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
	
	public boolean update(GoodsCase goodsCase) {
		try {
			this.goodsCaseDao.update( goodsCase);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<GoodsCase> query(String query, Map params, int begin, int max){
		return this.goodsCaseDao.query(query, params, begin, max);
		
	}
}
