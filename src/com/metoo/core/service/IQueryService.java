package com.metoo.core.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * <p>
 * Title: IQueryService.java
 * </p>
 * 
 * <p>
 * Description: 基础查询service接口
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * 
 * <p>
 * Company: 湖南创发科技有限公司 www.koala.com
 * </p>
 * 
 * @author erikzhang
 * 
 * @date 2014-4-24
 * 
 * @version koala_b2b2c v2.0 2015版 
 */
public interface IQueryService {
	List query(String scope, Map params, int page, int pageSize);
	List nativeQuery(String scope, Map params, int page, int pageSize);
	int executeNativeSQL (String nnq);
	int executeNativeSQL (String nnq, Map params);
	void flush();
}
