package com.macro.mall.component;

import com.macro.mall.common.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MyRedisLimiter {
    @Resource
    @Qualifier("redisRequestRateLimiterScript")
    private RedisScript redisScript;

    @Resource
    private RedisService redisService;

    /**
     * 基于令牌桶算法实现的限流
     * @param tokenKey 存储token的key
     * @param timeStampKey 存储timeStamp的key
     * @param replenishRate 令牌填充速率
     * @param burstCapacity 令牌桶容量
     * @return 是否允许通过
     */
    public boolean isAllowed(String tokenKey,String timeStampKey,int replenishRate,int burstCapacity,int need){
        List<String> keys = Arrays.asList(tokenKey, timeStampKey);
        try {
            Object data = redisService.execLua(redisScript, keys, replenishRate, burstCapacity, "时间戳参数并不需要", need);
            List<Long> returnData = (List<Long>) data;
            log.info("令牌桶限流结果 [是否成功{},当前剩余令牌{}]",returnData.get(0) == 1,returnData.get(1));
            return returnData.get(0) == 1;
        }catch (Exception e){
            log.info("令牌桶出错");
            e.printStackTrace();
            return true;
        }
    }



}
