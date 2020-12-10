package com.metoo.foundation.service.impl;

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
import com.metoo.foundation.domain.Point;
import com.metoo.foundation.service.IPointService;

@Service
@Transactional
public class PointServiceImpl implements IPointService{

	@Resource(name = "pointDAO")
	private IGenericDAO<Point> pointDao;

	@Override
	public boolean save(Point point) {
		// TODO Auto-generated method stub
		try {
			this.pointDao.save(point);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	@Override
	public Point getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.pointDao.get(id);
	}

	@Override
	public boolean update(Point point) {
		// TODO Auto-generated method stub
		try {
			this.pointDao.update(point);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	@Override
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		 try {
			this.pointDao.remove(id);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(Point.class, construct,
				query, params, this.pointDao);
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

	@Override
	public List<Point> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.pointDao.query(query, params, begin, max);
	}
	
}
