package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.defaultSleepDuration;

/**
 * desc :
 * name : ThreadUtils
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class ThreadUtils {
    public static final long interruptLoops = 5;

    public static <T> Thread create(Supplier<T> supplier) {
        return new Thread(supplier::get);
    }

    /**
     * 默认5秒关闭
     * @param thread
     */
    public static void shutdown(Thread thread) {
        if (null != thread) {
            int count = 0;
            thread.interrupt();
            while (count < interruptLoops) {
                count ++;
                if (thread.isInterrupted()) {
                    break;
                }
                TimeWaitUtils.wakeupAfter(defaultSleepDuration, TimeUnit.MILLISECONDS);
            }
        }
    }
}
