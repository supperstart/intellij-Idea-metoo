package com.metoo.core.log.util.impl;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metoo.core.log.model.BaseLogModel;
import com.metoo.core.log.model.BizLogModel;
import com.metoo.core.log.util.BaseLogger;
import com.metoo.core.log.util.BizLogger;

public class BizLoggerImpl extends BaseLoggerImpl implements BizLogger
{

	@Override
	public void info(Class<?> clazz, BizLogModel logModel)
	{

		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.B + "."
				+ clazz.getName());
		HttpSession session = logModel.session;
		if (session != null)
		{
			session.setAttribute(BaseLogModel.BIZLOGID_SESSION_KEY, logModel
					.getUuid());
		}
		log.info("{}", logModel);
	}

}
