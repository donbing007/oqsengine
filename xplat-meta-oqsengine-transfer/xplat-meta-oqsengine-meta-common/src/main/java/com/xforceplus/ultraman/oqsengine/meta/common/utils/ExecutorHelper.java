package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程管理者的方便工具
 * <p>
 *
 * @author donbing
 * @version 1.0 2014-9-21
 * @since 1.5
 */
public class ExecutorHelper {

    private ExecutorHelper() {
    }

    /**
     * 使用默认的等待时间1分钟，来关闭目标线程组。
     * <p>
     *
     * @param pool 需要关闭的线程组.
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        shutdownAndAwaitTermination(pool, 60L);
    }

    /**
     * 关闭ExecutorService的线程管理者
     * <p>
     *
     * @param pool    需要关闭的管理者
     * @param timeout 等待时间
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool,
                                                   long timeout) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 返回一个线程工厂,这是一个可以定义线程名字的线程工厂,返回的线程都将被命名.
     * 创建的线程都是非后台线程.
     *
     * @param name 名称.
     * @return 线程工厂实例.
     */
    public static ThreadFactory buildNameThreadFactory(final String name) {
        return buildNameThreadFactory(name, false);
    }

    /**
     * 返回一个线程工厂,这是一个可以定义线程名字的线程工厂,返回的线程都将被命名.
     *
     * @param name   名称.
     * @param daemon 是否为后台线程.
     * @return 线程工厂实例.
     */
    public static ThreadFactory buildNameThreadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {

            private final AtomicLong number = new AtomicLong();

            @Override
            public Thread newThread(Runnable r) {
                Thread newThread = Executors.defaultThreadFactory().newThread(r);
                newThread.setName(name + "-" + number.getAndIncrement());
                newThread.setDaemon(daemon);
                return newThread;
            }

        };
    }
}
