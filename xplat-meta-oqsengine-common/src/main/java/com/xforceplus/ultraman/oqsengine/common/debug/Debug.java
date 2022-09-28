package com.xforceplus.ultraman.oqsengine.common.debug;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 为了模似难以重现的问题而存在的Hack信息.<br>
 * 其依赖实现都在代码中的引用,所以需要实现者在关键点埋点.<br>
 * 注意: 所有时间单位都为毫秒.
 *
 * @author dongbin
 * @version 0.1 2022/9/20 14:37
 * @since 1.8
 */
public final class Debug {

    private static volatile boolean masterAndIndexSelectWait = false;

    /**
     * 无限制等待.用以在主库和索引之间创建一个人为可控的间隔.
     */
    public static void awaitNoticeMasterAndIndexSelect() {
        while (masterAndIndexSelectWait) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
    }

    /**
     * 通知现在主库和索引查询之间需要等待.
     */
    public static void setMasterAndIndexSelectWait() {
        masterAndIndexSelectWait = true;
    }

    /**
     * 通知主库和索引之间查询解除等待.
     */
    public static void noticeMasterAndIndexSelectWarkup() {
        masterAndIndexSelectWait = false;
    }

    /**
     * 判断当前是否需要等待.
     *
     * @return true 需要等待, false不需要.
     */
    public static boolean needMasterAndIndexSelectWait() {
        return masterAndIndexSelectWait;
    }
}
