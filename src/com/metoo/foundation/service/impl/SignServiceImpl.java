package com.metoo.foundation.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.foundation.domain.Sign;
import com.metoo.foundation.service.ISignService;

@Service
@Transactional
public class SignServiceImpl implements ISignService{
	
	@Resource(name="singDAO")
	private IGenericDAO<Sign> signDao;

	@Override
	public Sign getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.signDao.get(id);
	}

	@Override
	public boolean save(Sign instance) {
		// TODO Auto-generated method stub
		try {
			this.signDao.save(instance);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean update(Sign instance) {
		// TODO Auto-generated method stub
		try {
			this.signDao.update(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		try {
			this.signDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<Sign> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.signDao.query(query, params, begin, max);
	}

}
