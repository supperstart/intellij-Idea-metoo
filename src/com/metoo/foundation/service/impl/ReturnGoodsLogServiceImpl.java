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
import com.metoo.foundation.domain.ReturnGoodsLog;
import com.metoo.foundation.service.IReturnGoodsLogService;

@Service
@Transactional
public class ReturnGoodsLogServiceImpl implements IReturnGoodsLogService{
	@Resource(name = "returnGoodsLogDAO")
	private IGenericDAO<ReturnGoodsLog> returnGoodsLogDao;
	
	public boolean save(ReturnGoodsLog returnGoodsLog) {
		/**
		 * init other field here
		 */
		try {
			this.returnGoodsLogDao.save(returnGoodsLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ReturnGoodsLog getObjById(Long id) {
		ReturnGoodsLog returnGoodsLog = this.returnGoodsLogDao.get(id);
		if (returnGoodsLog != null) {
			return returnGoodsLog;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.returnGoodsLogDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> returnGoodsLogIds) {
		// TODO Auto-generated method stub
		for (Serializable id : returnGoodsLogIds) {
			delete((Long) id);
		}
		return true;
	}
	
	public IPageList list(IQueryObject properties) {
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		Map params = properties.getParameters();
		String construct = properties.getConstruct();
		GenericPageList pList = new GenericPageList(ReturnGoodsLog.class,construct, query,
				params, this.returnGoodsLogDao);
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
	
	public boolean update(ReturnGoodsLog returnGoodsLog) {
		try {
			this.returnGoodsLogDao.update( returnGoodsLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<ReturnGoodsLog> query(String query, Map params, int begin, int max){
		return this.returnGoodsLogDao.query(query, params, begin, max);
		
	}
}
