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
import com.metoo.foundation.domain.GroupLifeGoods;
import com.metoo.foundation.service.IGroupLifeGoodsService;

@Service
@Transactional
public class GroupLifeGoodsServiceImpl implements IGroupLifeGoodsService{
	@Resource(name = "groupLifeGoodsDAO")
	private IGenericDAO<GroupLifeGoods> groupLifeGoodsDao;
	
	public boolean save(GroupLifeGoods groupLifeGoods) {
		/**
		 * init other field here
		 */
		try {
			this.groupLifeGoodsDao.save(groupLifeGoods);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GroupLifeGoods getObjById(Long id) {
		GroupLifeGoods groupLifeGoods = this.groupLifeGoodsDao.get(id);
		if (groupLifeGoods != null) {
			return groupLifeGoods;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.groupLifeGoodsDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> groupLifeGoodsIds) {
		// TODO Auto-generated method stub
		for (Serializable id : groupLifeGoodsIds) {
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
		GenericPageList pList = new GenericPageList(GroupLifeGoods.class,construct, query,
				params, this.groupLifeGoodsDao);
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
	
	public boolean update(GroupLifeGoods groupLifeGoods) {
		try {
			this.groupLifeGoodsDao.update( groupLifeGoods);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<GroupLifeGoods> query(String query, Map params, int begin, int max){
		return this.groupLifeGoodsDao.query(query, params, begin, max);
		
	}
}
