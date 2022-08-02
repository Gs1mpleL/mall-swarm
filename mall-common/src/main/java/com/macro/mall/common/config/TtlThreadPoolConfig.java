package com.macro.mall.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class TtlThreadPoolConfig {
    private static final int SIZE = Math.max(Runtime.getRuntime().availableProcessors()*2+1,16);

    @Bean("ttlThreadExecutor")
    public TtlThreadPoolExecutor executorService(){
        return new TtlThreadPoolExecutor(SIZE, SIZE, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10240), new ThreadFactory() {
            private AtomicInteger currentThread = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"mall-ttl-thread-" + currentThread.getAndIncrement());
            }
        },new ThreadPoolExecutor.AbortPolicy());
    }

}
