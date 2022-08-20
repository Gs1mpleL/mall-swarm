package com.macro.mall.portal.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class ConfirmCallBackListener implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        //指定 ConfirmCallback
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("correlation>>>>>>>{},ack>>>>>>>>>{},cause>>>>>>>>{}", correlationData, ack, cause);
        if (ack) {
            //确认收到消息
            log.info("收到ACK确认");
        } else {
            //收到消息失败，可以开启重试机制，或者将失败的存起来，进行补偿
            log.info("发送MQ消息ACL失败，保存到数据库中，通过定时任务补偿");
        }
    }

    /*
     *
     * @param returnedMessage
     * 消息是否从Exchange路由到Queue, 只有消息从Exchange路由到Queue失败才会回调这个方法
     * @author xiaojie
     * @date 2021/9/29 13:53
     * @return void
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("Exchange->Queue失败！");
        log.info("被退回信息是》》》》》》{}", returnedMessage.getMessage());
        log.info("replyCode》》》》》》{}", returnedMessage.getReplyCode());
        log.info("replyText》》》》》》{}", returnedMessage.getReplyText());
        log.info("exchange》》》》》》{}", returnedMessage.getExchange());
        log.info("routingKey>>>>>>>{}", returnedMessage.getRoutingKey());
    }

}
