package com.xforceplus.ultraman.oqsengine.common.thread;


/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public interface LifeCycledThread<T> {

    void start();

    void stop();
}
