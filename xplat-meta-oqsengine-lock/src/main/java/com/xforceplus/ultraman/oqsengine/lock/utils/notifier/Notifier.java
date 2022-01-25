package com.xforceplus.ultraman.oqsengine.lock.utils.notifier;

import java.util.function.Consumer;

/**
 * 通知器.
 *
 * @author dongbin
 * @version 0.1 2022/1/24 10:16
 * @since 1.8
 */
public interface Notifier {

    /**
     * 订单指定KEY的解锁.
     *
     * @param key 目标加锁key.
     * @param action 触锁后的行动.
     */
    public void subscribeUnLock(String key, Consumer<String> action);

    /**
     * 通知解锁.
     *
     * @param key 目标key.
     */
    public void noticeUnlock(String key);

}
