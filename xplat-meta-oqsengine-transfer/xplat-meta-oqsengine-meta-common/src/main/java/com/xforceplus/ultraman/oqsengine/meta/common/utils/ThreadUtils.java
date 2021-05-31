package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 线程工具类.
 *
 * @author xujia
 * @since 1.8
 */
public class ThreadUtils {
    public static <T> Thread create(Supplier<T> supplier) {
        return new Thread(supplier::get);
    }

    /**
     * 默认5秒关闭.
     */
    public static void shutdown(Thread thread, long timeout) {
        TimeWaitUtils.wakeupAfter(timeout, TimeUnit.SECONDS);
        if (null != thread) {
            //  等待timeout秒后结束线程
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
}
