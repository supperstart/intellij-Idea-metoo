package com.metoo.module.chatting.dao;
import org.springframework.stereotype.Repository;

import com.metoo.core.base.GenericDAO;
import com.metoo.module.chatting.domain.Chatting;
@Repository("chattingDAO")
public class ChattingDAO extends GenericDAO<Chatting> {

}