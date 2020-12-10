package com.metoo.core.ip;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
* <p>Title: LogFactory.java</p>

* <p>Description:纯真ip查询日志记录 </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南创发科技有限公司 www.koala.com</p>

* @author erikzhang

* @date 2014-4-24

* @version koala_b2b2c v2.0 2015版 
 */
public class LogFactory {

	private static final Logger logger;
	static {
		logger = Logger.getLogger("stdout");
		logger.setLevel(Level.INFO);
	}

	public static void log(String info, Level level, Throwable ex) {
		logger.log(level, info, ex);
	}

	public static Level getLogLevel() {
		return logger.getLevel();
	}

}
