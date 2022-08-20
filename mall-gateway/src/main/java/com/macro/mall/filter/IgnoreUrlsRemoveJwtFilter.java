package com.macro.mall.filter;

import com.macro.mall.common.constant.AuthConstant;
import com.macro.mall.config.IgnoreUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * 白名单路径访问时需要移除JWT请求头
 * Created by macro on 2020/7/24.
 */
@Component
@Slf4j
public class IgnoreUrlsRemoveJwtFilter implements WebFilter {
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("白名单触发");
        ServerHttpRequest request = exchange.getRequest();
        logRequest(request);
        URI uri = request.getURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        //白名单路径移除JWT请求头
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri.getPath())) {
                request = exchange.getRequest().mutate().header(AuthConstant.JWT_TOKEN_HEADER, "").build();
                exchange = exchange.mutate().request(request).build();
                return chain.filter(exchange);
            }
        }
        return chain.filter(exchange);
    }

    private void logRequest(ServerHttpRequest request){
//        log.info("--------------------------------------------------------------------------------------");
//        log.info("请求进入");
//        log.info("请求地址 [{}]",request.getRemoteAddress());
        log.info("请求地址 [{}]",request.getPath());
//        log.info("请求方式 [{}]",request.getMethod());
//        request.getHeaders().forEach((k,v)->{
//        log.info("Header  {}:{}",k,v);
//        });
//        log.info("--------------------------------------------------------------------------------------");
    }


}
