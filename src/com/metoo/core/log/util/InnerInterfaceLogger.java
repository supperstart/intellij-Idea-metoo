package com.metoo.core.log.util;

import com.metoo.core.log.model.InnerInterfaceLogModel;

/**
 * 内部接口调用 日志接口
 * 
 * @author hjw
 * 
 */
public interface InnerInterfaceLogger extends BaseLogger {

	/**
	 * 日志记录方法
	 * 
	 * @param clazz
	 *            日志产生的类
	 * @param logModel
	 *            日志内容
	 */
	public void info(Class<?> clazz, InnerInterfaceLogModel logModel);
}
