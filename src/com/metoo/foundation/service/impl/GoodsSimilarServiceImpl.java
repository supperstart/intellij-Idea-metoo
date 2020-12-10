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
import com.metoo.foundation.domain.GoodsSimilar;
import com.metoo.foundation.service.IGoodsSimilarService;

@Transactional
@Service
public class GoodsSimilarServiceImpl implements IGoodsSimilarService{
	
	@Resource(name = "goodsSimilarDao")
	private IGenericDAO<GoodsSimilar> goodsSimilarDao;

	@Override
	public GoodsSimilar getObjById(Long id) {
		// TODO Auto-generated method stub
		GoodsSimilar goodsSimilar = this.goodsSimilarDao.get(id);
		if(goodsSimilar != null){
			return goodsSimilar;
		}
		return null;
	}

	@Override
	public boolean save(GoodsSimilar instance) {
		// TODO Auto-generated method stub
		try {
			this.goodsSimilarDao.save(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		try {
			this.goodsSimilarDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean batchDelete(List<Serializable> ids) {
		// TODO Auto-generated method stub
		for(Serializable id : ids){
			delete((Long) id);
		}
		return true;
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if(properties != null ){
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(GoodsSimilar.class, construct,
				query, params, this.goodsSimilarDao);
		if (properties != null) {
			PageObject pageObj = properties.getPageObj();
			if (pageObj != null)
				pList.doList(
						pageObj.getCurrentPage() == null ? 0 : pageObj
								.getCurrentPage(),
						pageObj.getPageSize() == null ? 0 : pageObj
								.getPageSize());
		} else
			pList.doList(0, -1);
		return pList;
	}

	@Override
	public boolean update(GoodsSimilar instance) {
		// TODO Auto-generated method stub
		try {
			this.update(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<GoodsSimilar> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.goodsSimilarDao.query(query, params, begin, max);
	}
	
	@Override
	public GoodsSimilar getObjByProperty(String construct, String propertyName,
			Object value) {
		// TODO Auto-generated method stub
		return this.goodsSimilarDao.getBy(construct, propertyName, value);
	}

}
