package com.xforceplus.ultraman.oqsengine.event;

import java.util.function.Consumer;

/**
 * 实际什么也不做的总线,主要用以占位.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 14:47
 * @since 1.8
 */
public class DoNothingEventBus implements EventBus {

    private static final EventBus INSTANCE = new DoNothingEventBus();

    public static EventBus getInstance() {
        return INSTANCE;
    }

    @Override
    public void watch(EventType type, Consumer<Event> listener) {

    }

    @Override
    public void notify(Event event) {

    }
}
