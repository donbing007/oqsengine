package com.xforceplus.ultraman.oqsengine.devops.utils;

import com.xforceplus.ultraman.oqsengine.common.lock.ILock;
import com.xforceplus.ultraman.oqsengine.common.lock.ILockFactory;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.xforceplus.ultraman.oqsengine.devops.enums.ERROR.DUPLICATE_KEY_ERROR;


/**
 * desc :
 * name : LockExecutor
 *
 * @author : xujia
 * date : 2020/9/3
 * @since : 1.8
 */
public class LockExecutor {
    final Logger logger = LoggerFactory.getLogger(LockExecutor.class);

    @Resource(name = "distributeLockFactory")
    ILockFactory lockFactory;


    public <V, R> Either<SQLException, R> executorWithLock(String resourceId, long timeout, TimeUnit timeUnit,
                                                           Function<V, Either<SQLException, R>> function, V params) throws SQLException {

        ILock lock = lockFactory.buildLock(resourceId, timeout, timeUnit);
        boolean locked = false;
        try {
            //  获得锁
            if (locked = lock.tryLock()) {
                //  执行方法
                return function.apply(params);
            }
            return Either.left(new SQLException(DUPLICATE_KEY_ERROR.name(), DUPLICATE_KEY_ERROR.name(), DUPLICATE_KEY_ERROR.ordinal()));
        } catch (SQLException e) {
            return Either.left(new SQLException(e.getMessage(), e));
        } finally {
            //  解锁
            if (locked) {
                if (!lock.releaseLock()) {
                    logger.error("release lock '{}' failed.", resourceId);
                }
            }
        }
    }

    private <V, R> R noPassPortRun(Function<V, R> function, V params) {
        return function.apply(params);
    }
}
