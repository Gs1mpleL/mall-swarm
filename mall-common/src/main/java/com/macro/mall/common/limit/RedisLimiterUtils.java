package com.macro.mall.common.limit;

import com.macro.mall.common.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 限流工具类
 * @Author CJB
 * @Date 2020/3/19 17:21
 */
@Component
public class RedisLimiterUtils {
   @Autowired
   private RedisService redisService;

    /**
     * 获取令牌
     * @param key 请求id
     * @param max 最大能同时承受多少的并发（桶容量）
     * @param time  每秒生成多少的令牌
     * @return 获取令牌返回true，没有获取返回false
     */
    public  boolean tryAcquire(String key, int max,int time) {
        if (redisService.get(key) == null) {
            redisService.set(key,1,time);
            return true;
        }else{
            Long incr = redisService.incr(key, 1);
            if (incr >= max){
                return false;
            }
        }
        return true;
    }
}