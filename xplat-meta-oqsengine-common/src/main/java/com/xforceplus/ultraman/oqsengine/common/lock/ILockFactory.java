package com.xforceplus.ultraman.oqsengine.common.lock;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : ILockFactory
 *
 * @author : xujia
 * date : 2020/9/7
 * @since : 1.8
 */
public interface ILockFactory {

    ILock buildLock(String resourceId, long time, TimeUnit timeUnit) throws SQLException;
}
