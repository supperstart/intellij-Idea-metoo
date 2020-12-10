package com.metoo.module.app.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.module.app.domain.QRLogin;

@Repository("qRLoginDAO")
public class QRLoginDAO extends GenericDAO<QRLogin> {

}