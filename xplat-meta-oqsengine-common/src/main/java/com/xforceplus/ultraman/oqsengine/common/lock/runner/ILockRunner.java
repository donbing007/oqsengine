package com.xforceplus.ultraman.oqsengine.common.lock.runner;

import com.xforceplus.ultraman.oqsengine.common.lock.runner.compose.BiFunctionCompose;
import com.xforceplus.ultraman.oqsengine.common.lock.runner.compose.FunctionCompose;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : ILockRunner
 *
 * @author : xujia
 * date : 2020/10/14
 * @since : 1.8
 */
public interface ILockRunner {

    void init(long timeout, TimeUnit timeUnit, int maxUnLockRetry, boolean openPessimisticLock);

    /**
     * 当资源未加锁，且当前Retry值低于最大重试次数时，以无锁方式运行，否则将以有锁状态运行.
     * 无锁状态下执行失败、重试次数未达阈值时但该资源已被加入等待队列，将立即返回失败(Option.empty())到SDK端，由SDK端发起重试.
     *
     * @param resourceId 资源ID，加锁对象.
     * @param retry 标明当前第几次重试
     * @param functionCompose 无锁执行的方法对象.
     * @param lockFunctionCompose 有锁执行的方法对象.
     * @return Optional<R> 结果集
     */
    <T, U, R> Optional<R> autoSelectExecuting(String resourceId, int retry,
                                              BiFunctionCompose<T, U, R> functionCompose,
                                              BiFunctionCompose<T, U, R> lockFunctionCompose) throws SQLException;
    /**
     * 当资源未加锁，且当前Retry值低于最大重试次数时，以无锁方式运行，否则将以有锁状态运行.
     * 无锁状态下执行失败、重试次数未达阈值时但该资源已被加入等待队列，将立即返回失败(Option.empty())到SDK端，由SDK端发起重试.
     *
     * @param resourceId 资源ID，加锁对象.
     * @param retry 标明当前第几次重试
     * @param functionCompose 无锁执行的方法对象.
     * @param lockFunctionCompose 有锁执行的方法对象.
     * @return Optional<R> 结果集
     */
    <V, R> Optional<R> autoSelectExecuting(String resourceId, int retry,
                                           FunctionCompose<V, R> functionCompose,
                                           FunctionCompose<V, R> lockFunctionCompose) throws SQLException;
}
