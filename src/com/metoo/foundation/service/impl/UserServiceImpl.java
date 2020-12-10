package com.metoo.foundation.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.metoo.app.buyer.domain.Result;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.core.mv.JModelAndView;
import com.metoo.core.query.GenericPageList;
import com.metoo.core.query.PageObject;
import com.metoo.core.query.support.IPageList;
import com.metoo.core.query.support.IQueryObject;
import com.metoo.core.security.support.SecurityUserHolder;
import com.metoo.foundation.domain.User;
import com.metoo.foundation.service.IUserService;
import com.sun.media.sound.AiffFileReader;


@Service
@Transactional
public class UserServiceImpl implements IUserService {
	
	@Resource(name = "userDAO")
	private IGenericDAO<User> userDAO;
	


	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		try {
			this.userDAO.remove(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public User getObjById(Long id) {
		// TODO Auto-generated method stub
		return this.userDAO.get(id);
	}

	public boolean save(User user) {
		// TODO Auto-generated method stub
		try {
			Map params = new HashMap();
			params.put("userName", user.getUserName());
			List users = this.userDAO.query(
					"select obj.id from User obj where obj.userName=:userName",
					params, -1, -1);
			if (users.size() == 0) {
				this.userDAO.save(user);
				/*int divisor = 2;
				int dividend = 0;
				int result = divisor/dividend;*/
				return true;
			} else
				return false;
		} catch (Exception e) {
			throw new ArithmeticException();
			//return false;
		}
	}

	public boolean update(User user) {
		// TODO Auto-generated method stub
		try {
			this.userDAO.update(user);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<User> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.userDAO.query(query, params, begin, max);

	}

	public IPageList list(IQueryObject properties) {
		// TODO Auto-generated method stub
		if (properties == null) {
			return null;
		}
		String query = properties.getQuery();
		String construct = properties.getConstruct();
		Map params = properties.getParameters();
		GenericPageList pList = new GenericPageList(User.class,construct, query, params,
				this.userDAO);
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
	public User getObjByProperty(String construct, String propertyName,
			String value) {
		// TODO Auto-generated method stub
		return this.userDAO.getBy(construct, propertyName, value);
	}
	
}
