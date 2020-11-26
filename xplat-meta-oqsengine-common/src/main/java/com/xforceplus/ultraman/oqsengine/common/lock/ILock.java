package com.xforceplus.ultraman.oqsengine.common.lock;

import java.sql.SQLException;

/**
 * desc :
 * name : ILock
 *
 * @author : xujia
 * date : 2020/9/1
 * @since : 1.8
 */
public interface ILock {
    boolean isFreeLock() throws SQLException;

    boolean tryLock() throws SQLException;

    boolean releaseLock() throws SQLException;

    boolean forceUnLock(String key);
}
