package com.xforceplus.ultraman.oqsengine.common.lock.process;

/**
 * desc :
 * name : DefaultProcessLockHandler
 *
 * @author : xujia
 * date : 2020/9/7
 * @since : 1.8
 */
public interface DefaultProcessLockHandler {
    boolean remove(String key);

    boolean lock(String key, String value);

    String isLocked(String key);
}
