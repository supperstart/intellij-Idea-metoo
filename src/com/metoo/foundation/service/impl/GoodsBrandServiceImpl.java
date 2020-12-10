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
import com.metoo.foundation.domain.GoodsBrand;
import com.metoo.foundation.service.IGoodsBrandService;

@Service
@Transactional
public class GoodsBrandServiceImpl implements IGoodsBrandService{
	@Resource(name = "goodsBrandDAO")
	private IGenericDAO<GoodsBrand> goodsBrandDao;
	
	public boolean save(GoodsBrand goodsBrand) {
		/**
		 * init other field here
		 */
		try {
			this.goodsBrandDao.save(goodsBrand);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GoodsBrand getObjById(Long id) {
		GoodsBrand goodsBrand = this.goodsBrandDao.get(id);
		if (goodsBrand != null) {
			return goodsBrand;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.goodsBrandDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> goodsBrandIds) {
		// TODO Auto-generated method stub
		for (Serializable id : goodsBrandIds) {
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
		GenericPageList pList = new GenericPageList(GoodsBrand.class, construct,query,
				params, this.goodsBrandDao);
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
	
	public boolean update(GoodsBrand goodsBrand) {
		try {
			this.goodsBrandDao.update( goodsBrand);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<GoodsBrand> query(String query, Map params, int begin, int max){
		return this.goodsBrandDao.query(query, params, begin, max);
		
	}
}
