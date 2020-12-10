package com.metoo.foundation.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.foundation.domain.VerifyCode;
@Repository("mobileVerifyCodeDAO")
public class VerifyCodeDAO extends GenericDAO<VerifyCode> {

}