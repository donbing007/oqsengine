package com.xforceplus.ultraman.oqsengine.event;

import java.io.Serializable;
import java.util.Optional;

/**
 * 事件.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 10:33
 * @since 1.8
 */
public interface Event<T extends Serializable> {

    /**
     * 事件类型.
     *
     * @return 事件类型.
     */
    EventType type();

    /**
     * 事件负载.
     *
     * @return 负载.
     */
    Optional<T> payload();

    /**
     * 事件产生的时间戳.
     *
     * @return 时间戳.
     */
    default long time() {
        return System.currentTimeMillis();
    }

    /**
     * 优先级.高的优先级会比低优先级优先处理.
     *
     * @return 优先级, 默认为正常.
     */
    default EventPriority priority() {
        return EventPriority.NORMAL;
    }
}
