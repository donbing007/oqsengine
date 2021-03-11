package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;

/**
 * changelog handler
 */
public interface ChangelogHandler<T> {

    /**
     * transformer
     * @param source
     * @return
     */
    TransactionalChangelogEvent getEvent(T source);

    default void handle(T source){
        handle(getEvent(source));
    }

    void handle(TransactionalChangelogEvent changelogEvent);

    /**
     * consumer
     */
    void prepareConsumer();
}
