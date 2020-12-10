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
import com.metoo.foundation.domain.EnoughFree;
import com.metoo.foundation.service.IEnoughFreeService;

@Service
@Transactional
public class EnoughFreeServiceImpl implements IEnoughFreeService{
	@Resource(name = "enoughFreeDAO")
	private IGenericDAO<EnoughFree> enoughFreeDao;
	
	@Override
	public boolean save(EnoughFree instance) {
		// TODO Auto-generated method stub
		try {
			this.enoughFreeDao.save(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public EnoughFree getObjById(Long id) {
		// TODO Auto-generated method stub
		EnoughFree enoughFree = this.enoughFreeDao.get(id);
		if(enoughFree != null){
			return enoughFree;
		}
		return null;
	}

	@Override
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		try {
			this.enoughFreeDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public boolean batchdelete(List<Serializable> enoughReduceIds) {
		// TODO Auto-generated method stub
		for (Serializable id : enoughReduceIds) {
			delete((Long) id);
		}
		return true;
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if(properties == null){
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(EnoughFree.class,
				construct, query, params, this.enoughFreeDao);
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
	public boolean update(EnoughFree instance) {
		// TODO Auto-generated method stub
		try {
			this.enoughFreeDao.update(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<EnoughFree> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.enoughFreeDao.query(query, params, begin, max);
	}

}
