package com.macro.mall.portal.component;

import com.alibaba.fastjson2.JSON;
import com.macro.mall.portal.domain.SeckillMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RabbitListener(queues="mall.seckill.success")
public class SeckillSuccessReceiver {
    @RabbitHandler
    public void handler(String seckillMsg){
        log.info("收到秒杀成功消息[{}]", JSON.parseObject(seckillMsg,SeckillMsg.class));

    }
}
