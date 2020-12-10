package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.UserConfig;
@Repository("userConfigDAO")
public class UserConfigDAO extends GenericDAO<UserConfig> {

}