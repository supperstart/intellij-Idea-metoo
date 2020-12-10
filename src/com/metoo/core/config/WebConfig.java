package com.metoo.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.metoo.core.version.ApiHandlerMapping;
/**
 * <p>Title: WebConfig.java</p>
 * 
 * <p>Description: </p>
 * @author 46075
 *
 */
//@Configuration
public class WebConfig extends WebMvcConfigurationSupport{
	
	@Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new ApiHandlerMapping();
        handlerMapping.setOrder(0);
        handlerMapping.setInterceptors(getInterceptors());
        return handlerMapping;
    }
}
