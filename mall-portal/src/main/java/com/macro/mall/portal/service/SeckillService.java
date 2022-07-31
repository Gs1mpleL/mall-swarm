package com.macro.mall.portal.service;

import com.macro.mall.portal.domain.SeckillReq;
import com.macro.mall.portal.domain.SeckillSession;

public interface SeckillService {
    boolean preHeat(SeckillSession seckillSession);

    String getLingpai(Long skuId);

    boolean kill(SeckillReq seckillReq);
}
