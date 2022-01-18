package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * JDK自带线程池实现的负载因子.
 * 计算线程池的活跃线程和最大线程数比例.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 14:20
 * @since 1.5
 */
public class ThreadPoolExecutorLoadFactor extends AbstractLoadFactor {

    private ThreadPoolExecutor target;

    public ThreadPoolExecutorLoadFactor(ThreadPoolExecutor target) {
        super(LoadFactor.MAX_WEIGHT);
        this.target = target;
    }

    public ThreadPoolExecutorLoadFactor(ThreadPoolExecutor target, double weight) {
        super(weight);
        this.target = target;
    }

    @Override
    public double now() {
        double maxWorker = target.getMaximumPoolSize();
        double nowWorker = target.getActiveCount();

        return nowWorker / maxWorker;
    }
}
