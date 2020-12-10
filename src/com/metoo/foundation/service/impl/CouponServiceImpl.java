package com.metoo.foundation.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.Coupon;
import com.metoo.foundation.domain.CouponInfo;
import com.metoo.foundation.service.ICouponInfoService;
import com.metoo.foundation.service.ICouponService;

@Service
@Transactional
public class CouponServiceImpl implements ICouponService {
	@Resource(name = "couponDAO")
	private IGenericDAO<Coupon> couponDao;
	@Autowired
	private ICouponInfoService couponInfoService;

	public boolean save(Coupon coupon) {
		/**
		 * init other field here
		 */
		try {
			this.couponDao.save(coupon);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Coupon getObjById(Long id) {
		Coupon coupon = this.couponDao.get(id);
		if (coupon != null) {
			return coupon;
		}
		return null;
	}

	public boolean delete(Long id) {
		try {
			this.couponDao.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean batchDelete(List<Serializable> couponIds) {
		// TODO Auto-generated method stub
		for (Serializable id : couponIds) {
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
		GenericPageList pList = new GenericPageList(Coupon.class, construct,
				query, params, this.couponDao);
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

	public boolean update(Coupon coupon) {
		try {
			this.couponDao.update(coupon);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Coupon> query(String query, Map params, int begin, int max) {
		return this.couponDao.query(query, params, begin, max);
	}

	@Override
	public boolean testDel(Long id) {
		// TODO Auto-generated method stub
		Coupon coupon = this.getObjById(id);
		List<CouponInfo> infos = coupon.getCouponinfos();
		for (CouponInfo info : infos) {
			this.couponInfoService.delete(info.getId());
		}
		//int o = 1/0;
		this.delete(coupon.getId());
		return true;
	}
	
	
}
