package com.xforceplus.ultraman.oqsengine.event;

import java.util.function.Consumer;

/**
 * 事件总线.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 10:30
 * @since 1.8
 */
public interface EventBus {

    /**
     * 关注某个事件.
     *
     * @param type     事件类型.
     * @param listener 事件处理器.
     */
    void watch(EventType type, Consumer<Event> listener);

    /**
     * 通知产生事件.
     * 不允许抛出异常.
     *
     * @param event 事件.
     */
    void notify(Event event);
}
