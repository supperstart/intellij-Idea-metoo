package com.metoo.msg.email;

import org.springframework.expression.ParserContext;
/**
 * 
* <p>Title: SpelTemplate.java</p>

* <p> * Description:自定义SPEL模板。定义为以${#user.userName} 获取 user对象的userName。
 </p>

* <p>Copyright: Copyright (c) 2015</p>

* <p>Company: 沈阳网之商科技有限公司 www.koala.com</p>

* @author jinxinzhe

* @date 2015-2-27

* @version koala_b2b2c 2015
 */
public class SpelTemplate implements ParserContext{
	@Override
	public String getExpressionPrefix() {
		// TODO Auto-generated method stub
		return "${";
	}

	@Override
	public String getExpressionSuffix() {
		// TODO Auto-generated method stub
		return "}";
	}

	@Override
	public boolean isTemplate() {
		// TODO Auto-generated method stub
		return true;
	}
}
