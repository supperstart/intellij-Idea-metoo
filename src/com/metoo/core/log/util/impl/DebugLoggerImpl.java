package com.metoo.core.log.util.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metoo.core.log.model.LogModel;
import com.metoo.core.log.util.BaseLogger;
import com.metoo.core.log.util.DebugLogger;

public class DebugLoggerImpl extends BaseLoggerImpl implements DebugLogger {

	@Override
	public void info(Class<?> clazz, LogModel logModel) {

		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.D + "."
				+ clazz.getName());
		log.info("{}", logModel);
	}

	@Override
	public void debug(Class<?> clazz, LogModel logModel) {
		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.D + "."
				+ clazz.getName());
		log.debug("{}", logModel);
	}

	@Override
	public void error(Class<?> clazz, LogModel logModel, Throwable e) {
		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.D + "."
				+ clazz.getName());
		log.error("{}", logModel);
		log.error("", e);
	}

	@Override
	public void trace(Class<?> clazz, LogModel logModel) {
		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.D + "."
				+ clazz.getName());
		log.trace("{}", logModel);
	}

	@Override
	public void warn(Class<?> clazz, LogModel logModel) {
		Logger log = LoggerFactory.getLogger(BaseLogger.LoggerType.D + "."
				+ clazz.getName());
		log.warn("{}", logModel);
	}

}
