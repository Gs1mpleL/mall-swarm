package com.macro.mall.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeckillMsg {
    private Long userId;
    private Long skuId;
}
