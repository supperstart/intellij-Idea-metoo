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
import com.metoo.foundation.domain.Document;
import com.metoo.foundation.domain.EmailModel;
import com.metoo.foundation.service.IEmailModelService;

@Service
@Transactional
public class EmailModelServiceImpl implements IEmailModelService{

	@Resource(name = "emailModelDAO")
	private IGenericDAO<EmailModel> emailDao;
	
	@Override
	public EmailModel getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.emailDao.get(id);
	}

	@Override
	public boolean save(EmailModel instance) {
		// TODO Auto-generated method stub
		try {
			this.emailDao.save(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
		
	}

	@Override
	public boolean update(EmailModel instance) {
		// TODO Auto-generated method stub
		try {
			this.emailDao.update(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	@Override
	public boolean remove(Long id) {
		// TODO Auto-generated method stub
		try {
			this.emailDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		GenericPageList pList = new GenericPageList(EmailModel.class, construct,
				query, params, this.emailDao);
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
	public List<EmailModel> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.emailDao.query(query, params, begin, max);
	}

}
