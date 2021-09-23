package com.xforceplus.ultraman.oqsengine.common.timerwheel;

/**
 * 时间轮转超时回调处理.
 * 通知器实现不得阻塞,不建议抛出异常.
 * 1.阻塞会造成时间轮的停止.
 * 2.抛出异常会被拦截.
 *
 * @version 1.0 2017-12-13 17:37:38
 * @param <T> 超时的目标.
 * @since 1.5
 * @author dongbin
 */
public interface TimeoutNotification<T> {

    /**
     * 返回此值表示过期.
     */
    public static long OVERDUE = 0;

    /**
     * 通知对象过期.
     *
     * @param t 过期地象.
     * @return 如果大于0,表示将此目标重新以返回值时间增加到环中.否则就真的进行过期.
     */
    public long notice(T t);
}
