package com.metoo.core.log.util;

/**
 * 日志接口
 * 
 * @author hjw
 * 
 */
public interface BaseLogger {

	/**
	 * 日志分类枚举类型<br />
	 * A:访问类日志<br />
	 * 
	 * @author hjw
	 * 
	 */
	public enum LoggerType {
		/** 访问日志类 */
		A, /** 通用开发日志类 */
		B, /** 业务日志 */
		D, /** 内部接口日志 */
		N
	};

}
