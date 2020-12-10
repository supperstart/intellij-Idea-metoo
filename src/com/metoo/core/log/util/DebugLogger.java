package com.metoo.core.log.util;

import com.metoo.core.log.model.LogModel;

/**
 * 通用开发用日志方法接口
 * 
 * @author hjw
 * 
 */
public interface DebugLogger extends BaseLogger {

	/**
	 * 最细粒度日志级别：轨迹级<br />
	 * 一般用于最细粒度的所有事件与数据打印。<br />
	 * 根据常规该级别打印量太大，一般不启用。<br />
	 * 
	 * @param clazz
	 * @param logModel
	 */
	public void trace(Class<?> clazz, LogModel logModel);

	/**
	 * 调试级<br />
	 * 一般用于细粒度事件与数据打印。<br />
	 * 根据常规在开发调试时使用，生产环境中关闭。<br />
	 * 
	 * @param clazz
	 * @param logModel
	 */
	public void debug(Class<?> clazz, LogModel logModel);

	/**
	 * 信息级<br />
	 * 用于普通事件打印。<br />
	 * 
	 * @param clazz
	 * @param logModel
	 */
	public void info(Class<?> clazz, LogModel logModel);

	/**
	 * 警告级<br />
	 * 当代码存在可能的潜在风险，但并不引起异常时使用。<br />
	 * 
	 * @param clazz
	 * @param logModel
	 */
	public void warn(Class<?> clazz, LogModel logModel);

	/**
	 * 错误级<br />
	 * 代码运行出现异常时使用。<br />
	 * 
	 * @param clazz
	 * @param logModel
	 */
	public void error(Class<?> clazz, LogModel logModel,Throwable e);

}
