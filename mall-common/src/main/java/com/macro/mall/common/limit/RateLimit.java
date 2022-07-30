package com.macro.mall.common.limit;

import java.lang.annotation.*;
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    int capacity() default 100;

    int time() default 10;
}