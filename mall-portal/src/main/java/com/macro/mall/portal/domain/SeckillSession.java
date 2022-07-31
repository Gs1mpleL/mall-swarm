package com.macro.mall.portal.domain;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class SeckillSession {
    /*参加的商品*/
    private Long skuId;
    /*开始时间*/
    private String startTime;
    /*结束时间*/
    private String endTime;
    /*商品名称*/
    private String name;
    /*商品价格*/
    private BigDecimal price;
}
