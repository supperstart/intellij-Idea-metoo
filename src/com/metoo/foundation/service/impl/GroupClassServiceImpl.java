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
import com.metoo.foundation.domain.GroupClass;
import com.metoo.foundation.service.IGroupClassService;

@Service
@Transactional
public class GroupClassServiceImpl implements IGroupClassService{
	@Resource(name = "groupClassDAO")
	private IGenericDAO<GroupClass> groupClassDao;
	
	public boolean save(GroupClass groupClass) {
		/**
		 * init other field here
		 */
		try {
			this.groupClassDao.save(groupClass);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GroupClass getObjById(Long id) {
		GroupClass groupClass = this.groupClassDao.get(id);
		if (groupClass != null) {
			return groupClass;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.groupClassDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> groupClassIds) {
		// TODO Auto-generated method stub
		for (Serializable id : groupClassIds) {
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
		GenericPageList pList = new GenericPageList(GroupClass.class,construct, query,
				params, this.groupClassDao);
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
	
	public boolean update(GroupClass groupClass) {
		try {
			this.groupClassDao.update( groupClass);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<GroupClass> query(String query, Map params, int begin, int max){
		return this.groupClassDao.query(query, params, begin, max);
		
	}
}
