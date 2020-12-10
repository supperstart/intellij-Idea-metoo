package com.metoo.module.chatting.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.module.chatting.domain.ChattingLog;
@Repository("chattingLogDAO")
public class ChattingLogDAO extends GenericDAO<ChattingLog> {

}