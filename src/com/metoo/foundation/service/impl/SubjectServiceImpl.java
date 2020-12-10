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
import com.metoo.foundation.domain.Subject;
import com.metoo.foundation.service.ISubjectService;

@Service
@Transactional
public class SubjectServiceImpl implements ISubjectService{
	@Resource(name = "subjectDAO")
	private IGenericDAO<Subject> subjectDao;
	
	public boolean save(Subject subject) {
		/**
		 * init other field here
		 */
		try {
			this.subjectDao.save(subject);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Subject getObjById(Long id) {
		Subject subject = this.subjectDao.get(id);
		if (subject != null) {
			return subject;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.subjectDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> subjectIds) {
		// TODO Auto-generated method stub
		for (Serializable id : subjectIds) {
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
		GenericPageList pList = new GenericPageList(Subject.class,construct, query,
				params, this.subjectDao);
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
	
	public boolean update(Subject subject) {
		try {
			this.subjectDao.update( subject);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<Subject> query(String query, Map params, int begin, int max){
		return this.subjectDao.query(query, params, begin, max);
		
	}
}
