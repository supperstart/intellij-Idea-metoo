package com.metoo.core.tools;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 初始配置文件加载
 * 
 * @author hjw
 * 
 */
public class PropertiesHelper {
	private static Logger log = LoggerFactory.getLogger(PropertiesHelper.class);
	private static Properties prop = new Properties();
	
	static {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream in = loader.getResourceAsStream("jdbc.properties");
			prop = new Properties();
			prop.load(in);
			log.info("加载配置文件成功。共加载项：" + prop.size()
					+ "; 配置内容：" + prop.toString());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("加载配置文件时发生异常。", e);
		}
	}

	/**
	 * 获取配置内容
	 * @param key
	 * @return
	 */
	public String getProperties(String key) {
		return prop.getProperty(key);
	}
}
