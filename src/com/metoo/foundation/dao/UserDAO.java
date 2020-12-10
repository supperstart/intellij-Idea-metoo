package com.metoo.foundation.dao;

import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.User;

@Repository("userDAO")
public class UserDAO extends GenericDAO<User> {

}
