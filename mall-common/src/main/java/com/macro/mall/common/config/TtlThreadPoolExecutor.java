package com.macro.mall.common.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.macro.mall.common.exception.ApiException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author huibin.wei
 * @date 2022/1/14 2:41 下午
 */
public class TtlThreadPoolExecutor extends ThreadPoolExecutor {
    private volatile Field threadLocalsField;
    private volatile Field tableField;

    public TtlThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    public List<ThreadLocal> getThreadLocal() {
        Thread thread = Thread.currentThread();
        try {
            Object threadLocalMap = getThreadLocalsField().get(thread);
            Field tableField = getTableField();
            Object[] table = (Object[]) tableField.get(threadLocalMap);
            List<ThreadLocal> collect = Arrays.stream(table)
                    .filter(o -> o != null)
                    .map(entry -> ((WeakReference<ThreadLocal>) entry).get())
                    .filter(o -> o != null)
                    .collect(Collectors.toList());
            return collect;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ApiException("系统错误");
        }
    }

    private Field getThreadLocalsField() throws NoSuchFieldException {
        if (threadLocalsField == null) {
            synchronized (this) {
                if (threadLocalsField == null) {
                    threadLocalsField = Thread.class.getDeclaredField("threadLocals");
                    threadLocalsField.setAccessible(true);
                }
            }
        }
        return threadLocalsField;
    }

    private Field getTableField() throws NoSuchFieldException, IllegalAccessException {
        if (tableField == null) {
            synchronized (this) {
                if (tableField == null) {
                    tableField = getThreadLocalsField().get(Thread.currentThread()).getClass().getDeclaredField("table");
                    tableField.setAccessible(true);
                }
            }
        }

        return tableField;
    }

    @Override
    public void execute(Runnable command) {
        List<ThreadLocal> local = getThreadLocal();

        for (ThreadLocal threadLocal : local) {
            TransmittableThreadLocal.Transmitter.registerThreadLocalWithShadowCopier(threadLocal);
        }
        super.execute(TtlRunnable.get(command));

    }

    @Override
    public Future<?> submit(Runnable task) {
        List<ThreadLocal> local = getThreadLocal();

        for (ThreadLocal threadLocal : local) {
            TransmittableThreadLocal.Transmitter.registerThreadLocalWithShadowCopier(threadLocal);
        }
        return super.submit(TtlRunnable.get(task));

    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        List<ThreadLocal> local = getThreadLocal();

        for (ThreadLocal threadLocal : local) {
            TransmittableThreadLocal.Transmitter.registerThreadLocalWithShadowCopier(threadLocal);
        }
        return super.submit(TtlRunnable.get(task), result);

    }
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        List<ThreadLocal> local = getThreadLocal();

        for (ThreadLocal threadLocal : local) {
            TransmittableThreadLocal.Transmitter.registerThreadLocalWithShadowCopier(threadLocal);
        }
        return super.submit(TtlCallable.get(task));

    }
}

