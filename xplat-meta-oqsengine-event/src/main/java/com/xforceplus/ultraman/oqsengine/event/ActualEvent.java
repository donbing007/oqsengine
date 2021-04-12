package com.xforceplus.ultraman.oqsengine.event;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty(value = "type")
    private EventType type;

    @JsonProperty(value = "payload")
    private T payload;

    @JsonProperty(value = "time")
    private long time;

    public ActualEvent(EventType type, T payload, long time) {
        this.type = type;
        this.payload = payload;
        this.time = time;
    }

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

    @Override
    public String toString() {
        return "ActualEvent{" +
                "type=" + type +
                ", payload=" + payload +
                ", time=" + time +
                '}';
    }
}
