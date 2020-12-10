package com.metoo.foundation.service.impl;

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
import com.metoo.foundation.domain.StoreLog;
import com.metoo.foundation.service.IStoreLogService;

@Transactional
@Service
public class StoreLogServiceImpl implements IStoreLogService{

	@Resource(name = "storeLogDAO")
	private IGenericDAO<StoreLog> storeLogDao;
	
	@Override
	public boolean save(StoreLog sp) {
		// TODO Auto-generated method stub
		try {
			this.storeLogDao.save(sp);
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
			this.storeLogDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean update(StoreLog sp) {
		try {
			this.storeLogDao.update(sp);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(StoreLog.class, construct,
				query, params, this.storeLogDao);
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
	public StoreLog getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.storeLogDao.get(id);
	}

	@Override
	public StoreLog getObjByProperty(String construct, String propertyName, String value) {
		// TODO Auto-generated method stub
		return this.storeLogDao.getBy(construct, propertyName, value);
	}

	@Override
	public List<StoreLog> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.storeLogDao.query(query, params, begin, max);
	}

}
