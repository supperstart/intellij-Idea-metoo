package com.metoo.core.security;

import java.util.Map;

/**
 * 
 * <p>
 * Title: SecurityManager.java
 * </p>
 * 
 * <p>
 * Description: 权限管理接口
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: metoo科技有限公司 ebuyair.metoo-souq.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版
 */
public interface SecurityManager {

	public Map<String, String> loadUrlAuthorities();

}
