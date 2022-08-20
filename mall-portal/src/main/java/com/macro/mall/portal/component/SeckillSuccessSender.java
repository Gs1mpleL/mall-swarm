package com.macro.mall.portal.component;

import com.alibaba.fastjson2.JSON;
import com.macro.mall.portal.domain.QueueEnum;
import com.macro.mall.portal.domain.SeckillMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillSuccessSender {
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendSuccessMsg(SeckillMsg seckillMsg){
        amqpTemplate.convertAndSend(QueueEnum.SECKILL_SUCCESS.getExchange(),QueueEnum.SECKILL_SUCCESS.getRouteKey(), JSON.toJSONString(seckillMsg));
        log.info("发送秒杀成功消息 [{}]",seckillMsg);
    }


}
