package com.macro.mall.portal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    private String address;

    private String password;

    private int timeout = 3000;

    private int database = 0;

    private int connectionPoolSize = 64;

    private int connectionMinimumIdleSize=10;

}