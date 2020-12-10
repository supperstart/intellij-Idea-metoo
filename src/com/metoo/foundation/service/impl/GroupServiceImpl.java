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
import com.metoo.foundation.domain.Group;
import com.metoo.foundation.service.IGroupService;

@Service
@Transactional
public class GroupServiceImpl implements IGroupService{
	@Resource(name = "groupDAO")
	private IGenericDAO<Group> groupDao;
	
	public boolean save(Group group) {
		/**
		 * init other field here
		 */
		try {
			this.groupDao.save(group);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Group getObjById(Long id) {
		Group group = this.groupDao.get(id);
		if (group != null) {
			return group;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.groupDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> groupIds) {
		// TODO Auto-generated method stub
		for (Serializable id : groupIds) {
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
		GenericPageList pList = new GenericPageList(Group.class,construct, query,
				params, this.groupDao);
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
	
	public boolean update(Group group) {
		try {
			this.groupDao.update( group);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<Group> query(String query, Map params, int begin, int max){
		return this.groupDao.query(query, params, begin, max);
		
	}
}
