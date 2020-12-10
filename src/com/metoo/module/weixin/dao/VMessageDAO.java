package com.metoo.module.weixin.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.module.weixin.domain.VMessage;
@Repository("vMessageDAO")
public class VMessageDAO extends GenericDAO<VMessage> {

}