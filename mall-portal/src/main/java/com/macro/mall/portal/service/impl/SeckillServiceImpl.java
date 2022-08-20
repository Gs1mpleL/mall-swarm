package com.macro.mall.portal.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.macro.mall.common.config.TtlThreadPoolExecutor;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.component.SeckillSuccessSender;
import com.macro.mall.portal.domain.SeckillMsg;
import com.macro.mall.portal.domain.SeckillRedisKey;
import com.macro.mall.portal.domain.SeckillReq;
import com.macro.mall.portal.domain.SeckillSession;
import com.macro.mall.portal.service.SeckillService;
import com.macro.mall.portal.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    @Qualifier("ttlThreadExecutor")
    private TtlThreadPoolExecutor ttlThreadPoolExecutor;
    @Autowired
    private RedisService redisService;

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private SeckillSuccessSender seckillSuccessSender;

    @Override
    public boolean preHeat(SeckillSession seckillSession) {
        String key = String.format(SeckillRedisKey.sessionKey,seckillSession.getSkuId());
        long now = System.currentTimeMillis();
        long end = DateUtil.parse(seckillSession.getEndTime(), "yyyy-MM-dd HH:mm:ss").getTime();
        long expire = end - now;
        if (expire < 0){
            throw new ApiException("时间已经过了哦");
        }
        try {
            redisService.setWithMill(key, JSON.toJSONString(seckillSession),expire);
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException("活动场次设置失败");
        }
        PmsSkuStock pmsSkuStock = skuStockMapper.selectByPrimaryKey(seckillSession.getSkuId());
        if (pmsSkuStock == null){
            throw new ApiException("该商品没有库存哦");
        }
        int stock = pmsSkuStock.getStock() - pmsSkuStock.getLockStock();
        log.info("预热商品全部库存[{}]到Redis",stock);
        RSemaphore semaphore = redissonClient.getSemaphore(String.format(SeckillRedisKey.lockKey, seckillSession.getSkuId()));
        try {
            semaphore.trySetPermits(stock);
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException("分布式锁设置失败");
        }
        log.info("分布式锁设置完成");
        return true;
    }

    @Override
    public String getLingpai(Long skuId) {
        String key = String.format(SeckillRedisKey.sessionKey,skuId);
        String s = (String) redisService.get(key);
        if (StringUtils.isBlank(s)){
            throw new ApiException("商品未参与秒杀哦");
        }
        SeckillSession seckillSession = JSON.parseObject(s, SeckillSession.class);
        long start = DateUtil.parse(seckillSession.getStartTime(), "yyyy-MM-dd HH:mm:ss").getTime();
        if (start > System.currentTimeMillis()){
            throw new ApiException("活动还未开始");
        }

        String lingpai = UUID.randomUUID().toString().replace("-", "");
        // 令牌时间 60s
        redisService.set(String.format(SeckillRedisKey.lingpaiKey, UserUtils.getUserDetail().getId(), skuId), lingpai, 60);
        return lingpai;
    }

    @Override
    public boolean kill(SeckillReq seckillReq) {
        log.info("校验Token");
        if (redisService.get(String.format(SeckillRedisKey.lingpaiKey, UserUtils.getUserDetail().getId(), seckillReq.getSkuId())) == null) {
            throw new ApiException("你从哪里来的接口！！！");
        }
        redisService.del(String.format(SeckillRedisKey.lingpaiKey, UserUtils.getUserDetail().getId(), seckillReq.getSkuId()));
        log.info("秒杀请求正常，删除Redis令牌");
//        if (redisService.get(String.format(SeckillRedisKey.userBuyKey,UserUtils.getUserDetail().getId(),seckillReq.getSkuId())) != null){
//            throw new ApiException("本场秒杀已经参与过了哦");
//        }
        RSemaphore semaphore = redissonClient.getSemaphore(String.format(SeckillRedisKey.lockKey, seckillReq.getSkuId()));
        if (!semaphore.tryAcquire(1)) {
            throw new ApiException("秒杀失败哦,无货了捏");
        }
        log.info("秒杀成功！！！");
        redisService.set(String.format(SeckillRedisKey.userBuyKey,UserUtils.getUserDetail().getId(),seckillReq.getSkuId()),1,1000);
        log.info("Redis存储用户购买记录");
        ttlThreadPoolExecutor.execute(() ->{
            Long id = UserUtils.getUserDetail().getId();
            log.info("在Redis中保存用户[{}]购买记录",id);
        });

        ttlThreadPoolExecutor.execute(() ->{
            Long id = UserUtils.getUserDetail().getId();
            log.info("这里就发送[{}]消息去减库存之类的操作",id);
            seckillSuccessSender.sendSuccessMsg(new SeckillMsg(UserUtils.getUserDetail().getId(),seckillReq.getSkuId()));
        });



        return true;
    }

}
