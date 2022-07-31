package com.macro.mall.portal.domain;

public class SeckillRedisKey {
    public final static String sessionKey = "mall:portal:seckill:session:%s";
    public final static String lingpaiKey = "mall:portal:seckill:lingpai:%s:%s";
    public final static String lockKey = "mall:portal:seckill:lock:%s";
    public final static String userBuyKey = "mall:portal:seckill:bug:%s:%s";
}
