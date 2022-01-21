package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * 当前系统CPU的负载因子测试.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 14:43
 * @since 1.8
 */
@Disabled("非常规测试")
public class CpuLoadFactorTest {

    private CpuLoadFactor loadFactor = new CpuLoadFactor();

    /**
     * 测试CPU满负载.
     */
    @Test
    public void testHightCpu() throws Exception {

        CpuHightHolderTask cpuHightHolderTask = new CpuHightHolderTask();
        Thread thread = new Thread(cpuHightHolderTask);
        thread.setDaemon(true);
        thread.start();

        // 运行3秒,等等CPU占比上升.
        TimeUnit.SECONDS.sleep(3);

        double load = loadFactor.now();
        Assertions.assertTrue(load > 50D);

        cpuHightHolderTask.close();
    }

    @Test
    public void testLowCpu() throws Exception {
        CpuLowHolderTask cpuLowHolderTask = new CpuLowHolderTask();
        Thread thread = new Thread(cpuLowHolderTask);
        thread.setDaemon(true);
        thread.start();

        // 运行3秒,等等CPU占比上升.
        TimeUnit.SECONDS.sleep(3);

        double load = loadFactor.now();
        Assertions.assertTrue(load < 30D);

        cpuLowHolderTask.close();
    }

    class CpuLowHolderTask implements Runnable {

        private volatile boolean closed;

        public void close() {
            this.closed = true;
        }

        @Override
        public void run() {
            while (!closed) {
                double pi = 1D;
                long n = 0;
                while (!closed) {
                    pi = 3 * Math.pow(2, n++) * pi;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }
    }

    // 让CPU满负载
    class CpuHightHolderTask implements Runnable {

        private volatile boolean closed;

        public CpuHightHolderTask() {
            this.closed = false;
        }

        public void close() {
            this.closed = true;
        }

        @Override
        public void run() {
            double pi = 1D;
            long n = 0;
            while (!closed) {
                pi = 3 * Math.pow(2, n++) * pi;
            }
        }
    }
}