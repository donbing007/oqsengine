package com.xforceplus.ultraman.oqsengine.meta.common.utils;

import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : TimeWaitUtils
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class TimeWaitUtils {
    /**
     * 等待timeSeconds秒后进行重试
     */
    public static void wakeupAfter(long duration, TimeUnit timeUnit) {
        try {
            Thread.sleep(timeUnit.toMillis(duration));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
