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
import com.metoo.foundation.domain.UserRecommend;
import com.metoo.foundation.service.IUserRecommendService;

@Service
@Transactional
public class UserRecommendServiceImpl implements IUserRecommendService{
	
	@Resource(name = "userRecommendDAO")
	private IGenericDAO<UserRecommend> userRecommendDao;

	@Override
	public boolean save(UserRecommend userRecommend) {
		// TODO Auto-generated method stub
		try {
			this.userRecommendDao.save(userRecommend);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public UserRecommend getObjById(Long id) {
		// TODO Auto-generated method stub
		UserRecommend userRecommend = userRecommendDao.get(id);
		if(userRecommend ==null){
			return null;
		}
		return userRecommend;
	}

	@Override
	public boolean update(UserRecommend instance) {
		// TODO Auto-generated method stub
		try {
			this.userRecommendDao.update(instance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<UserRecommend> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.userRecommendDao.query(query, params, begin, max);
	}

	@Override
	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
			if (properties == null) {
				return null;
			}
			String query = properties.getQuery();
			String construct = properties.getConstruct();
			Map params = properties.getParameters();
			GenericPageList pList = new GenericPageList(UserRecommend.class, construct,query,
					params, this.userRecommendDao);
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

}
