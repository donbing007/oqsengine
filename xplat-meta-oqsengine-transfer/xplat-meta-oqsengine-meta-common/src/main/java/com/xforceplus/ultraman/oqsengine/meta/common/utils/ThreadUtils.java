package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;



/**
 * desc :
 * name : ThreadUtils
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class ThreadUtils {
    public static <T> Thread create(Supplier<T> supplier) {
        return new Thread(supplier::get);
    }

    /**
     * 默认5秒关闭
     * @param thread
     */
    public static void shutdown(Thread thread, long timeout) {
        if (null != thread) {
            //  等待timeout秒后结束线程
            TimeWaitUtils.wakeupAfter(timeout, TimeUnit.SECONDS);
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }
}
