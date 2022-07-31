package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.limit.RateLimit;
import com.macro.mall.portal.domain.SeckillReq;
import com.macro.mall.portal.domain.SeckillSession;
import com.macro.mall.portal.service.SeckillService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 商品预热，准备开始秒杀活动
     */
    @PostMapping("/preHeat")
    public CommonResult preHeat(SeckillSession seckillSession) {
        return CommonResult.success(seckillService.preHeat(seckillSession));
    }


    /**
     * 向客户端发放秒杀令牌
     */
    @GetMapping("/lingpai")
    public CommonResult getLingpai(Long skuId){
        return CommonResult.success(seckillService.getLingpai(skuId));
    }

    /**
     * 秒杀商品
     */
    /*60秒内只允许访问100次*/
    @RateLimit(capacity = 100,time = 60)
    @PostMapping("/kill")
    public CommonResult kill(SeckillReq seckillReq){
        return CommonResult.success(seckillService.kill(seckillReq));
    }
}
