package com.macro.mall.portal.component;

import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.limit.RateLimit;
import com.macro.mall.common.limit.RedisLimiterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

@Component
@Slf4j
public class RateLimiterIntercept implements HandlerInterceptor {
    @Autowired
    private RedisLimiterUtils redisLimiterUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod=(HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            /**
             * 首先获取方法上的注解
             */
            RateLimit rateLimit = AnnotationUtils.findAnnotation(method, RateLimit.class);
            //方法上没有标注该注解，尝试获取类上的注解
            if (Objects.isNull(rateLimit)){
                //获取类上的注解
                rateLimit = AnnotationUtils.findAnnotation(handlerMethod.getBean().getClass(), RateLimit.class);
            }

            //没有标注注解，放行
            if (Objects.isNull(rateLimit))
                return true;
            log.info("访问[{}]，需要限流处理",request.getRequestURI());
            //尝试获取令牌，如果没有令牌了
            if (!redisLimiterUtils.tryAcquire(request.getRequestURI(),rateLimit.capacity(),rateLimit.time())){
                //抛出请求超时的异常
                throw new ApiException("接口限流触发");
            }
        }
        return true;
    }
}
