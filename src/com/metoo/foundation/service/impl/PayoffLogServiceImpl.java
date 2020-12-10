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
import com.metoo.foundation.domain.PayoffLog;
import com.metoo.foundation.service.IPayoffLogService;

@Service
@Transactional
public class PayoffLogServiceImpl implements IPayoffLogService{
	@Resource(name = "payoffLogDAO")
	private IGenericDAO<PayoffLog> payoffLogDao;
	
	public boolean save(PayoffLog payoffLog) {
		/**
		 * init other field here
		 */
		try {
			this.payoffLogDao.save(payoffLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public PayoffLog getObjById(Long id) {
		PayoffLog payoffLog = this.payoffLogDao.get(id);
		if (payoffLog != null) {
			return payoffLog;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.payoffLogDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> payoffLogIds) {
		// TODO Auto-generated method stub
		for (Serializable id : payoffLogIds) {
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
		GenericPageList pList = new GenericPageList(PayoffLog.class,construct, query,
				params, this.payoffLogDao);
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
	
	public boolean update(PayoffLog payoffLog) {
		try {
			this.payoffLogDao.update( payoffLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<PayoffLog> query(String query, Map params, int begin, int max){
		return this.payoffLogDao.query(query, params, begin, max);
		
	}
}
