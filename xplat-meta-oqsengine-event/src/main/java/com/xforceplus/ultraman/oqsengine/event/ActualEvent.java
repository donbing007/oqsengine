package com.xforceplus.ultraman.oqsengine.event;

import java.io.Serializable;
import java.util.Optional;

/**
 * 实际事件.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 13:42
 * @since 1.8
 */
public class ActualEvent<T extends Serializable> implements Event<T> {

    private EventType type;
    private T payload;
    private long time;

    public ActualEvent(EventType type, T payload) {
        this.type = type;
        this.payload = payload;
        this.time = System.currentTimeMillis();
    }

    @Override
    public EventType type() {
        return type;
    }

    @Override
    public Optional<T> payload() {
        return Optional.ofNullable(payload);
    }

    @Override
    public long time() {
        return time;
    }
}
