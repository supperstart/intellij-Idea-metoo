package com.metoo.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 	Title: EmailMapping.class
 * </p>
 * 
 * <p>
 * 	Description: 系统指定api发送邮件
 * </p>
 * 
 * <p>
 * Company: 湖南觅通科技
 * </p>
 * 
 * 
 * @author hkk
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmailMapping {
	
	String title() default "";// 

	String value() default "";//方法名 
}
