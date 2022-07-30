package com.macro.mall.portal.config;

import com.macro.mall.portal.component.RateLimiterIntercept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    RateLimiterIntercept rateLimiterIntercept;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimiterIntercept)
                .addPathPatterns("/**") ;                        // 拦截全部路径// 放行部分路径
    }
}
