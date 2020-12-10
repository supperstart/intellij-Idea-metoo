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
import com.metoo.foundation.domain.ShipAddress;
import com.metoo.foundation.service.IShipAddressService;

@Service
@Transactional
public class ShipAddressServiceImpl implements IShipAddressService{
	@Resource(name = "shipAddressDAO")
	private IGenericDAO<ShipAddress> shipAddressDao;
	
	public boolean save(ShipAddress shipAddress) {
		/**
		 * init other field here
		 */
		try {
			this.shipAddressDao.save(shipAddress);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ShipAddress getObjById(Long id) {
		ShipAddress shipAddress = this.shipAddressDao.get(id);
		if (shipAddress != null) {
			return shipAddress;
		}
		return null;
	}
	
	public boolean delete(Long id) {
		try {
			this.shipAddressDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean batchDelete(List<Serializable> shipAddressIds) {
		// TODO Auto-generated method stub
		for (Serializable id : shipAddressIds) {
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
		GenericPageList pList = new GenericPageList(ShipAddress.class,construct, query,
				params, this.shipAddressDao);
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
	
	public boolean update(ShipAddress shipAddress) {
		try {
			this.shipAddressDao.update( shipAddress);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}	
	public List<ShipAddress> query(String query, Map params, int begin, int max){
		return this.shipAddressDao.query(query, params, begin, max);
		
	}
}
