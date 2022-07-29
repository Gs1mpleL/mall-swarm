package com.macro.mall.portal.aop;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.macro.mall.common.constant.AuthConstant;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.service.UmsMemberCacheService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Order(2)
@Slf4j
@Component
public class UserAspect {
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private UmsMemberCacheService cacheService;

    @Pointcut("execution(public * com.macro.mall.portal.controller.*.*(..))")
    public void pointcut(){}

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("请求进入");
        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 在网关处已经将用户信息装入header 可以获取到用户的id
        String user = request.getHeader(AuthConstant.USER_TOKEN_HEADER);
        JSONObject jsonObject = JSONObject.parseObject(user);
        String id = jsonObject.getString("id");
        UmsMember userDetail = cacheService.getMember(Long.parseLong(id));
        if (userDetail == null){
             userDetail = memberService.getById(Long.parseLong(id));
             cacheService.setMember(userDetail);
        }
        // 用户信息装入ThreadLocal
        UserUtils.setUserDetail(userDetail);
        log.info("用户信息放入ThreadLocal");
        Object proceed = joinPoint.proceed();
        log.info("请求结束");
        UserUtils.removeUserDetail();
        log.info("删除ThreadLocal中的用户信息");
        return proceed;
    }
}
