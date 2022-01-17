package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 当前系统CPU的负载因子.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 14:43
 * @since 1.8
 */
public class CpuLoadFactor extends AbstractLoadFactor {

    private static ThreadMXBean THREAD_MX_BEAN;

    static {
        THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    }

    private volatile double load;
    private Thread monitorThread;

    public CpuLoadFactor() {
        this(LoadFactor.MAX_WEIGHT);
    }

    /**
     * 构造实例.
     */
    public CpuLoadFactor(double weight) {
        super(weight);
        monitorThread = new Thread(new MonitorTask());
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    @Override
    public double now() {
        return load;
    }

    class MonitorTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                // 忽略当前监控线程.
                long[] allThreadIds = Arrays.stream(THREAD_MX_BEAN.getAllThreadIds())
                    .filter(id -> id != monitorThread.getId()).toArray();

                // 线程的CPU占用时间,单位为纳秒.
                long[] startCpuTimes =
                    Arrays.stream(allThreadIds).map(id -> THREAD_MX_BEAN.getThreadCpuTime(id)).toArray();

                // 等待1秒,计算差值.
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    //do nothing
                }

                // 线程的CPU占用时间,单位为纳秒.
                long[] endCpuTimes =
                    Arrays.stream(allThreadIds).map(id -> THREAD_MX_BEAN.getThreadCpuTime(id)).toArray();

                double usage = 0.0D;
                for (int i = 0; i < allThreadIds.length; i++) {
                    if (endCpuTimes[i] < 0) {
                        continue;
                    }

                    // 计算在1秒中的占用比例.
                    usage += (endCpuTimes[i] - startCpuTimes[i]) / (1000 * 10000);
                }

                load = usage;

                // 1秒后再次统计
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }
    }
}
