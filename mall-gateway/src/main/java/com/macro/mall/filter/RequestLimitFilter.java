package com.macro.mall.filter;


import com.macro.mall.common.exception.ApiException;
import com.macro.mall.component.LimitConfig;
import com.macro.mall.component.MyRedisLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(0)
@Slf4j
public class RequestLimitFilter implements GlobalFilter {

    @Autowired
    private MyRedisLimiter redisLimiter;
    @Autowired
    private LimitConfig limitConfig;
    private Map<String, LimitConfig.LimitConfiguration> map;
    @PostConstruct
    private void mapConfig(){
        map = limitConfig.getConfigs().stream().collect(Collectors.toMap(LimitConfig.LimitConfiguration::getType, Function.identity()));
        log.info("限流规则map初始化完成[{}]",map.keySet());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("限流触发");
        String limitChoose = limitConfig.getChoose();
        if (map.get(limitChoose) == null) {
            log.info("无限流规则");
            return chain.filter(exchange);
        }
        LimitConfig.LimitConfiguration limitConfiguration = map.get(limitChoose);
        String tokenKey = "limit:token:" + limitConfiguration.getType() + exchange.getRequest().getPath();
        String stampKey = "limit:stampKey:" + limitConfiguration.getType() + exchange.getRequest().getPath();
        boolean allowed = redisLimiter.isAllowed(tokenKey, stampKey, limitConfiguration.getReplenishRate(), limitConfiguration.getBurstCapacity(), 1);
        if (!allowed) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            throw new ApiException("限流....");
        } else {
            return chain.filter(exchange);
        }

    }






}
