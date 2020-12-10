package com.metoo.foundation.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.LuckyDraw;
import com.metoo.foundation.service.ILuckyDrawService;

@Service
@Transactional
public class LuckyDrawServiceImpl implements ILuckyDrawService{

	@Resource(name = "luckyDrawDAO")
	private IGenericDAO<LuckyDraw> luckyDrawDao;
	
	@Override
	public boolean save(LuckyDraw luckyDraw) {
		// TODO Auto-generated method stub
		try {
			this.luckyDrawDao.save(luckyDraw);
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
		return false;
	}

	@Override
	public boolean update(LuckyDraw luckyDraw) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LuckyDraw getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.luckyDrawDao.get(id);
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LuckyDraw> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return null;
	}

}
