package com.macro.mall.portal.domain;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class SignDayRes {
    private String msg;
    private Integer allCount;
    private Integer maxContinuityCount;
    private String countMap;
}
