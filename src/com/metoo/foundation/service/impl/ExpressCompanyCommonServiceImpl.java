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
import com.metoo.foundation.domain.ExpressCompanyCommon;
import com.metoo.foundation.service.IExpressCompanyCommonService;

@Service
@Transactional
public class ExpressCompanyCommonServiceImpl implements
		IExpressCompanyCommonService {
	@Resource(name = "expressCompanyCommonDAO")
	private IGenericDAO<ExpressCompanyCommon> expressCompanyCommonDao;

	public boolean save(ExpressCompanyCommon expressCompanyCommon) {
		/**
		 * init other field here
		 */
		try {
			this.expressCompanyCommonDao.save(expressCompanyCommon);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public ExpressCompanyCommon getObjById(Long id) {
		ExpressCompanyCommon expressCompanyCommon = this.expressCompanyCommonDao
				.get(id);
		if (expressCompanyCommon != null) {
			return expressCompanyCommon;
		}
		return null;
	}

	public boolean delete(Long id) {
		try {
			this.expressCompanyCommonDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean batchDelete(List<Serializable> expressCompanyCommonIds) {
		// TODO Auto-generated method stub
		for (Serializable id : expressCompanyCommonIds) {
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
		GenericPageList pList = new GenericPageList(ExpressCompanyCommon.class,
				construct, query, params, this.expressCompanyCommonDao);
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

	public boolean update(ExpressCompanyCommon expressCompanyCommon) {
		try {
			this.expressCompanyCommonDao.update(expressCompanyCommon);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<ExpressCompanyCommon> query(String query, Map params,
			int begin, int max) {
		return this.expressCompanyCommonDao.query(query, params, begin, max);

	}
}
