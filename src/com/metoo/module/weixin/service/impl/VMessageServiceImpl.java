package com.metoo.module.weixin.service.impl;
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
import com.metoo.module.weixin.domain.VMessage;
import com.metoo.module.weixin.service.IVMessageService;

@Service
@Transactional
public class VMessageServiceImpl implements IVMessageService{
	@Resource(name = "vMessageDAO")
	private IGenericDAO<VMessage> vMessageDao;
	
	public boolean save(VMessage vMessage) {
		/**
		 * init other field here
		 */
		try {
			this.vMessageDao.save(vMessage);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public VMessage getObjById(Long id) {
		VMessage vMessage = this.vMessageDao.get(id);
		if (vMessage != null) {
			return vMessage;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.vMessageDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> vMessageIds) {
		// TODO Auto-generated method stub
		for (Serializable id : vMessageIds) {
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
		GenericPageList pList = new GenericPageList(VMessage.class, query,construct,
				params, this.vMessageDao);
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
	
	public boolean update(VMessage vMessage) {
		try {
			this.vMessageDao.update( vMessage);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<VMessage> query(String query, Map params, int begin, int max){
		return this.vMessageDao.query(query, params, begin, max);
		
	}
}
