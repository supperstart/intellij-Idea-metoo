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
import com.metoo.foundation.domain.SmsGoldLog;
import com.metoo.foundation.service.ISmsGoldLogService;

@Service
@Transactional
public class SmsGoldLogServiceImpl implements ISmsGoldLogService{
	@Resource(name = "smsGoldLogDAO")
	private IGenericDAO<SmsGoldLog> smsGoldLogDao;
	
	public boolean save(SmsGoldLog smsGoldLog) {
		/**
		 * init other field here
		 */
		try {
			this.smsGoldLogDao.save(smsGoldLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public SmsGoldLog getObjById(Long id) {
		SmsGoldLog smsGoldLog = this.smsGoldLogDao.get(id);
		if (smsGoldLog != null) {
			return smsGoldLog;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.smsGoldLogDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> smsGoldLogIds) {
		// TODO Auto-generated method stub
		for (Serializable id : smsGoldLogIds) {
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
		GenericPageList pList = new GenericPageList(SmsGoldLog.class,construct, query,
				params, this.smsGoldLogDao);
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
	
	public boolean update(SmsGoldLog smsGoldLog) {
		try {
			this.smsGoldLogDao.update( smsGoldLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<SmsGoldLog> query(String query, Map params, int begin, int max){
		return this.smsGoldLogDao.query(query, params, begin, max);
		
	}
}
