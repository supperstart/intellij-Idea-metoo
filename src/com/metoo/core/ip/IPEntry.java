package com.metoo.core.ip;

/**
 * 
* <p>Title: IPEntry.java</p>

* <p>Description:纯真ip查询，该类用来读取QQWry.dat中的的IP记录信息 </p>

* <p>Copyright: Copyright (c) 2014</p>

* <p>Company: 湖南创发科技有限公司 www.koala.com</p>

* @author erikzhang

* @date 2014-4-24

* @version koala_b2b2c v2.0 2015版 
 */
public class IPEntry {
	public String beginIp;
	public String endIp;
	public String country;
	public String area;

	/**
	 * 14. * 构造函数
	 */
	public IPEntry() {
		beginIp = endIp = country = area = "";
	}

}
