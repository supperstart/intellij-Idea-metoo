package com.metoo.module.sns.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.module.sns.domain.UserShare;

@Repository("userShareDAO")
public class UserShareDAO extends GenericDAO<UserShare> {

}