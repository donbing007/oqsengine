package com.xforceplus.ultraman.oqsengine.common.lock.runner;

import com.xforceplus.ultraman.oqsengine.common.lock.ILock;
import com.xforceplus.ultraman.oqsengine.common.lock.ILockFactory;
import com.xforceplus.ultraman.oqsengine.common.lock.runner.compose.BiFunctionCompose;
import com.xforceplus.ultraman.oqsengine.common.lock.runner.compose.FunctionCompose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.BUILD_LOCK_FAILED;


/**
 * desc :
 * name : LockRunner
 *
 * @author : xujia
 * date : 2020/10/14
 * @since : 1.8
 */
public class LockRunner implements ILockRunner {
    final Logger logger = LoggerFactory.getLogger(LockRunner.class);

    @Resource(name = "distributeLockFactory")
    private ILockFactory distributeLockFactory;

    private long timeout;
    private TimeUnit timeUnit;
    private int maxUnLockRetry;
    private boolean pessimisticLockOn;

    public void init(long timeout, TimeUnit timeUnit, int maxUnLockRetry, boolean pessimisticLockOn) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.maxUnLockRetry = maxUnLockRetry;
        this.pessimisticLockOn = pessimisticLockOn;
    }

    public <V, R> Optional<R> autoSelectExecuting(String resourceId, int retry,
                                                  FunctionCompose<V, R> functionCompose,
                                                  FunctionCompose<V, R> lockFunctionCompose) throws SQLException {
        //  当重试次数小于最大重试次数、且当前functionCompose不为空时，进入乐观锁，否则进入悲观锁
        if (retry < maxUnLockRetry && null != functionCompose) {
            ILock lock = buildLock(resourceId);
            if (lock.isFreeLock()) {
                logger.info(String.format("开始无锁执行-> resourceId : %s, function : %s, retry %d.",
                        resourceId, functionCompose.getFunctionName(), retry));
                // do function
                return predicate(resourceId, functionCompose.getFunctionName(), functionCompose.getExpected(), retry,
                        functionCompose.getFunction().apply(functionCompose.getParams()));
            }
            //  当前锁正在被使用，或者不支持悲观锁模式, 直接返回空结果集让SDK端发起重试
            logger.warn(String.format("执行失败-> resourceId : %s, function : %s, retry %d.",
                    resourceId, functionCompose.getFunctionName(), retry));
            return Optional.empty();
        } else if (pessimisticLockOn) {
            //  未设置悲观执行方法
            if (null == lockFunctionCompose) {
                logger.warn(String.format("未执行悲观锁序列，因为悲观锁lockFunctionCompose对象为空 -> resourceId : %s.", resourceId));
                return Optional.empty();
            } else {
                logger.info(String.format("加入悲观锁执行序列 -> resourceId : %s, lockFunction : %s."
                        , resourceId, lockFunctionCompose.getFunctionName()));
                //  加锁执行、判断结果是否符合预期
                return predicateWithLock(resourceId, lockFunctionCompose.getFunctionName(),
                        lockFunctionCompose.getExpected(), executing(resourceId, lockFunctionCompose));
            }
        }

        //  当前锁正在被使用，或者不支持悲观锁模式, 直接返回空结果集让SDK端发起重试
        logger.warn(String.format("执行失败, 不满足执行条件 -> resourceId : %s.", resourceId));
        return Optional.empty();
    }

    public <T, U, R> Optional<R> autoSelectExecuting(String resourceId, int retry,
                                                     BiFunctionCompose<T, U, R> functionCompose,
                                                     BiFunctionCompose<T, U, R> lockFunctionCompose) throws SQLException {
        //  当重试次数小于最大重试次数、且当前functionCompose不为空时，进入乐观锁，否则进入悲观锁
        if (retry < maxUnLockRetry && null != functionCompose) {
            //  进入乐观锁
            ILock lock = buildLock(resourceId);
            if (lock.isFreeLock()) {
                logger.info(String.format("开始执行-> resourceId : %s, function : %s, retry %d.",
                        resourceId, functionCompose.getFunctionName(), retry));
                // do function
                return predicate(resourceId, functionCompose.getFunctionName(), functionCompose.getExpected(), retry,
                        functionCompose.getBiFunction().apply(functionCompose.gettParams(), functionCompose.getuParams()));
            }

            //  当前锁正在被使用，或者不支持悲观锁模式, 直接返回空结果集让SDK端发起重试
            logger.warn(String.format("执行失败-> resourceId : %s, function : %s, retry %d.",
                    resourceId, functionCompose.getFunctionName(), retry));
            return Optional.empty();
        } else if (pessimisticLockOn) {
            //  未设置悲观执行方法
            if (null == lockFunctionCompose) {
                logger.warn(String.format("未执行悲观锁序列，因为悲观锁lockFunctionCompose对象为空 -> resourceId : %s.", resourceId));
                return Optional.empty();
            } else {
                logger.info(String.format("加入悲观锁执行序列 -> resourceId : %s, lockFunction : %s."
                        , resourceId, lockFunctionCompose.getFunctionName()));
                //  加锁执行、判断结果是否符合预期
                return predicateWithLock(resourceId, lockFunctionCompose.getFunctionName(),
                        lockFunctionCompose.getExpected(), executing(resourceId, lockFunctionCompose));
            }
        }
        logger.warn(String.format("执行失败, 不满足执行条件 -> resourceId : %s.", resourceId));
        return Optional.empty();
    }

    private <V, R> R executing(String resourceId, FunctionCompose<V, R> functionCompose) throws SQLException {

        boolean locked = false;
        ILock lock = buildLock(resourceId);

        try {
            //  获得锁
            if (locked = lock.tryLock()) {
                //  执行方法
                return functionCompose.getFunction().apply(functionCompose.getParams());
            }
            logger.warn(String.format("悲观锁执行序列获取锁超时 -> resourceId : %s, lockFunction : %s.",
                    resourceId, functionCompose.getFunctionName()));
            return null;
        } catch (Exception e) {
            exceptionHandler(e, resourceId, functionCompose.getFunctionName());
            return null;
        } finally {
            releaseLock(lock, locked, resourceId);
        }
    }

    private <T, U, R> R executing(String resourceId, BiFunctionCompose<T, U, R> biFunctionCompose) throws SQLException {

        boolean locked = false;
        ILock lock = buildLock(resourceId);

        try {
            //  获得锁
            if (locked = lock.tryLock()) {
                //  执行方法
                return biFunctionCompose.getBiFunction().apply(biFunctionCompose.gettParams(), biFunctionCompose.getuParams());
            }
            logger.warn(String.format("悲观锁执行序列获取锁超时 -> resourceId : %s, lockFunction : %s.",
                    resourceId, biFunctionCompose.getFunctionName()));
            return null;
        } catch (Exception e) {
            exceptionHandler(e, resourceId, biFunctionCompose.getFunctionName());
            return null;
        } finally {
            releaseLock(lock, locked, resourceId);
        }
    }

    private void exceptionHandler(Exception e, String resourceId, String functionName) throws SQLException {
        logger.warn(String.format("悲观锁执行序列失败 -> resourceId : %s, lockFunction : %s, message ：%s ",
                resourceId, functionName, e.getMessage()));
        if (e instanceof SQLException) {
            throw (SQLException) e;
        } else {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private ILock buildLock(String resourceId) throws SQLException {
        try {
            return distributeLockFactory.buildLock(resourceId, timeout, timeUnit);
        } catch (Exception e) {
            throw new SQLException(BUILD_LOCK_FAILED.name(), BUILD_LOCK_FAILED.name(), BUILD_LOCK_FAILED.ordinal());
        }
    }

    private void releaseLock(ILock lock, boolean locked, String resourceId) {
        //  解锁
        if (locked) {
            try {
                if (!lock.releaseLock()) {
                    logger.error("release lock '{}' failed.", resourceId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private <R> Optional<R> predicateWithLock(String resourceId, String functionName, Predicate<R> expected, R res) {
        if (innerPredicate(expected, res)) {
            logger.info(String.format("悲观锁执行序列执行完毕 -> resourceId : %s, lockFunction : %s."
                    , resourceId, functionName));
            return Optional.of(res);
        }
        logger.warn(String.format("悲观锁执行序列执行失败-> resourceId : %s, lockFunction : %s.",
                resourceId, functionName));
        return Optional.empty();
    }

    private <R> Optional<R> predicate(String resourceId, String functionName, Predicate<R> expected, int retry, R res) {
        if (innerPredicate(expected, res)) {
            logger.info(String.format("无锁执行完毕-> resourceId : %s, function : %s, retry %d.",
                    resourceId, functionName, retry));
            return Optional.of(res);
        }
        logger.warn(String.format("无锁执行序列执行失败-> resourceId : %s, function : %s.",
                resourceId, functionName));
        return Optional.empty();
    }

    private <R> boolean innerPredicate(Predicate<R> expected, R res) {
        return null != res && expected.test(res);
    }
}
