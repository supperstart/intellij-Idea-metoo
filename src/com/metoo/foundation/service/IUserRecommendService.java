package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.foundation.domain.UserRecommend;

public interface IUserRecommendService {

	/**
	 * @description 保存一个userRecommend对象 成功返回true 错误返回false
	 * @param userRecommend
	 * @return
	 */
	boolean save(UserRecommend userRecommend);
	
	/**
	 * @description 根据id获取一个userRecommend对象
	 * @param id
	 * @return
	 */
	UserRecommend getObjById(Long id);
	
	/**
	 * @description 更新一个userRecommend对象
	 * @param instance
	 * @return
	 */
	boolean update(UserRecommend instance);
	
	List<UserRecommend> query(String query, Map params, int begin, int max);
	
	IPageList list(IQueryObject properties);
	
}
