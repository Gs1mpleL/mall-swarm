package com.macro.mall.aspect;

import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.constant.AuthConstant;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.service.UmsAdminCacheService;
import com.macro.mall.service.UmsAdminService;
import com.macro.mall.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Order(2)
@Slf4j
@Component
public class UserAspect {
    @Autowired
    private UmsAdminService adminService;
    @Autowired
    private UmsAdminCacheService cacheService;

    @Pointcut("execution(public * com.macro.mall.portal.controller.*.*(..))")
    public void pointcut(){}

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 在网关处已经将用户信息装入header 可以获取到用户的id
        if (request.getHeader(AuthConstant.USER_TOKEN_HEADER) != null){
            String user = request.getHeader(AuthConstant.USER_TOKEN_HEADER);
            JSONObject jsonObject = JSONObject.parseObject(user);
            String id = jsonObject.getString("id");
            UmsAdmin admin = cacheService.getAdmin(Long.parseLong(id));
            if (admin == null){
                log.info("从MySQL中获取用户信息");
                admin = adminService.getItem(Long.parseLong(id));
                cacheService.setAdmin(admin);
            }else {
                log.info("从Redis中获取用户信息");
            }
            // 用户信息装入ThreadLocal
            UserUtils.setUserDetail(admin);
            log.info("用户信息放入ThreadLocal");
            Object proceed = joinPoint.proceed();
            log.info("请求结束");
            UserUtils.removeUserDetail();
            log.info("删除ThreadLocal中的用户信息");
            return proceed;
        }
        else {
            log.info("本次请求为未登陆状态");
            return joinPoint.proceed();
        }

    }
}
