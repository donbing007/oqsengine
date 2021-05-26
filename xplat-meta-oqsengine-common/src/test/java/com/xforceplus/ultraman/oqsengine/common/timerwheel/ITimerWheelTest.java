package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author weikai
 * @data 2021/5/21 16:30
 * @mail weikai@xforceplus.com
 */
public class ITimerWheelTest {
    @Test
    public void add() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        ITimerWheel multipleTimerWheel = new MultipleTimerWheel(new TimeoutNotification<Long>() {
            @Override
            public long notice(Long addTime) {
                try {
                    long expireTime = System.currentTimeMillis();

                    long space = expireTime - addTime;
                    //6000是因为wheel不是一个绝对准确的实现,所以终止时间会有误差.
                    if (space <= 6000) {
                        result.incrementAndGet();
                    }

                    return 0;

                } finally {
                    latch.countDown();
                }
            }
        });
        multipleTimerWheel.add(System.currentTimeMillis(), 5000);
        latch.await();

        Assert.assertEquals(1, result.get());
    }

    @Test
    public void add1() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        ITimerWheel multipleTimerWheel = new TimerWheel(new TimeoutNotification<Long>() {
            @Override
            public long notice(Long addTime) {
                try {
                    long expireTime = System.currentTimeMillis();

                    long space = expireTime - addTime;
                    //6000是因为wheel不是一个绝对准确的实现,所以终止时间会有误差.
                    if (space <= 6000) {
                        result.incrementAndGet();
                    }

                    return 0;

                } finally {
                    latch.countDown();
                }
            }
        });
        multipleTimerWheel.add(System.currentTimeMillis(), 5000);
        latch.await();

        Assert.assertEquals(1, result.get());
    }

}