package com.metoo.core.log.util;

import com.metoo.core.log.util.impl.AccessLoggerImpl;
import com.metoo.core.log.util.impl.BizLoggerImpl;
import com.metoo.core.log.util.impl.DebugLoggerImpl;
import com.metoo.core.log.util.impl.InnerInterfaceLoggerImpl;

/**
 * 日志接口工厂
 * 
 * @author hjw
 */
public class LoggerUtil {

	private static AccessLogger accessLogger;
	private static DebugLogger debugLogger;

	private static BizLogger bizLogger;
	private static InnerInterfaceLogger innerInterfaceLogger;

	/**
	 * 工厂禁止直接使用new进行实例化
	 */
	private LoggerUtil() {
	}

	/**
	 * 获取通用开发类日志接口
	 * 
	 * @return
	 */
	public static DebugLogger getDebugLogger() {
		if (debugLogger == null) {
			debugLogger = new DebugLoggerImpl();
		}
		return debugLogger;
	}

	/**
	 * 获取访问类日志接口
	 * 
	 * @return
	 */
	public static AccessLogger getAccessLogger() {
		if (accessLogger == null) {
			accessLogger = new AccessLoggerImpl();
		}
		return accessLogger;
	}

	public static BizLogger getBizLogger() {
		if (bizLogger == null) {
			bizLogger = new BizLoggerImpl();
		}
		return bizLogger;
	}

	public static InnerInterfaceLogger getInnerInterfaceLogger() {
		if (innerInterfaceLogger == null) {
			innerInterfaceLogger = new InnerInterfaceLoggerImpl();
		}
		return innerInterfaceLogger;
	}
}
