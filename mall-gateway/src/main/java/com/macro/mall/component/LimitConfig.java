package com.macro.mall.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("limit")
public class LimitConfig {

    private List<LimitConfiguration> configs;
    private String choose;

    @Data
    public static class LimitConfiguration{
        private String type;
        private int replenishRate;
        private int burstCapacity;
    }
}

