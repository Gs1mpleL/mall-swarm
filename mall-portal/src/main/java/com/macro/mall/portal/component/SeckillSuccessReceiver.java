package com.macro.mall.portal.component;

import com.alibaba.fastjson2.JSON;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.domain.SeckillMsg;
import com.macro.mall.portal.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queues="mall.seckill.success")
public class SeckillSuccessReceiver {
    @Autowired
    private OmsPortalOrderService orderService;
    @RabbitHandler
    public void handler(String seckillMsg){
        log.info("收到秒杀成功消息[{}]", seckillMsg);
        SeckillMsg msg = JSON.parseObject(seckillMsg, SeckillMsg.class);
        OrderParam orderParam = new OrderParam();
        orderParam.setSeckill(true);
        orderService.generateOrder(orderParam);
    }
}
