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
import com.metoo.foundation.domain.GroupJoiner;
import com.metoo.foundation.service.IGroupJoinerService;

@Service
@Transactional
public class GroupJoinerServiceImpl implements IGroupJoinerService{
	@Resource(name = "groupJoinerDAO")
	private IGenericDAO<GroupJoiner> groupJoinerDao;
	
	public boolean save(GroupJoiner groupJoiner) {
		/**
		 * init other field here
		 */
		try {
			this.groupJoinerDao.save(groupJoiner);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GroupJoiner getObjById(Long id) {
		GroupJoiner groupGoods = this.groupJoinerDao.get(id);
		if (groupGoods != null) {
			return groupGoods;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.groupJoinerDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> groupGoodsIds) {
		// TODO Auto-generated method stub
		for (Serializable id : groupGoodsIds) {
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
		GenericPageList pList = new GenericPageList(GroupJoiner.class,construct, query,
				params, this.groupJoinerDao);
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
	
	public boolean update(GroupJoiner groupJoiner) {
		try {
			this.groupJoinerDao.update( groupJoiner);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<GroupJoiner> query(String query, Map params, int begin, int max){
		return this.groupJoinerDao.query(query, params, begin, max);
		
	}
}
