package com.metoo.core.log.util.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metoo.core.log.model.InnerInterfaceLogModel;
import com.metoo.core.log.util.BaseLogger;
import com.metoo.core.log.util.InnerInterfaceLogger;

public class InnerInterfaceLoggerImpl extends BaseLoggerImpl implements
		InnerInterfaceLogger {

	@Override
	public void info(Class<?> clazz, InnerInterfaceLogModel logModel) {

		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.N + "."
				+ clazz.getName());
		log.info("{}", logModel);
	}

}
