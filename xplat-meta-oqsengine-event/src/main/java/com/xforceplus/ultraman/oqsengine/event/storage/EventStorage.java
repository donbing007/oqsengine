package com.xforceplus.ultraman.oqsengine.event.storage;

import com.xforceplus.ultraman.oqsengine.event.Event;
import java.util.Optional;

/**
 * 事件储存.
 * 实现必须保证事件不能弹出,已弹出的事件不可再次弹出.
 * 由于使用场景,必须保证并发安全.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 10:30
 * @since 1.8
 */
public interface EventStorage {

    /**
     * 储存事件.
     *
     * @param event 事件.
     * @return true成功, false失败.
     */
    boolean push(Event event);

    /**
     * 弹出最合适的一个任意类型的事件.
     * 弹出后事件不再存在于事件储存中.
     * 无论有无事件都会立即返回.
     *
     * @return 弹出的事件.
     */
    Optional<Event> pop();

    /**
     * 清空.
     */
    void clear();

    /**
     * 当前的持有的事件数量.
     *
     * @return 事件数量.
     */
    int size();
}
