package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 * 多时间轮实现测试.
 *
 * @author weikai
 * @data 2021/5/21 10:01
 * @mail weikai@xforceplus.com
 */
public class MultipleTimerWheelTest {

    @Test
    public void add() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification<Long>() {
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

        wheel.add(System.currentTimeMillis(), 5000);
        latch.await();

        Assert.assertEquals(1, result.get());
    }

    @Test
    public void parallelAdd() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(500000);
        final AtomicInteger result = new AtomicInteger(0);
        MultipleTimerWheel wheel = new MultipleTimerWheel(500, 100, new TimeoutNotification<Object>() {
            @Override
            public long notice(Object addTime) {
                try {
                    result.incrementAndGet();
                    return 0;

                } finally {
                    latch.countDown();
                }
            }
        });

        for (int i = 0; i < 500000; i++) {
            int finalI1 = i;
            executorService.submit(() -> {
                Random random = new Random();
                wheel.add(new Object(), 100 + random.nextInt(5000));
            });
        }

        latch.await();
        int size = wheel.size();
        Assert.assertEquals(500000, result.get());
        Assert.assertEquals(0, size);
    }

    @Test
    public void size() {
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification() {
            @Override
            public long notice(Object t) {
                Assert.fail("Unexpected elimination can be known.");

                return 0;
            }
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assert.assertEquals(1, wheel.size());
    }

    @Test
    public void parallelSize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification() {
            @Override
            public long notice(Object t) {
//                Assert.fail("Unexpected elimination can be known.");

                return 0;
            }
        });

        for (int i = 0; i < 500000; i++) {
            int finalI1 = i;
            executorService.submit(() -> {
                wheel.add(new Object(), 18000000);
            });
        }

        TimeUnit.MILLISECONDS.sleep(2000);

        Assert.assertEquals(500000, wheel.size());
    }

    @Test
    public void remove() {
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification() {
            @Override
            public long notice(Object t) {
                Assert.fail("Unexpected elimination can be known.");

                return 0;
            }
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assert.assertEquals(1, wheel.size());
        wheel.remove(target);
        Assert.assertEquals(0, wheel.size());
    }

    @Test
    public void exist() {
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification() {
            @Override
            public long notice(Object t) {
                Assert.fail("Unexpected elimination can be known.");

                return 0;
            }
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assert.assertTrue(wheel.exist(target));
    }

    @Test
    public void cleanExpire() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification<String>() {
            @Override
            public long notice(String text) {
                try {
                    return 0;
                } finally {
                    latch.countDown();
                }
            }
        });

        wheel.add("test", 3000);
        latch.await();

        TimeUnit.SECONDS.sleep(1L);
        Assert.assertEquals(0, wheel.size());
        Assert.assertFalse(wheel.exist("test"));
    }

    @Test
    public void ExpireAddValue() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        MultipleTimerWheel wheel = new MultipleTimerWheel(new TimeoutNotification<Long>() {
            @Override
            public long notice(Long addTime) {
                try {
                    long expireTime = System.currentTimeMillis();

                    long space = expireTime - addTime;
//                    System.out.println(space);
                    if (result.get() == 0 && space <= 6000) {//6000是因为wheel不是一个绝对准确的实现,所以终止时间会有误差.
                        result.incrementAndGet();
                    } else if (result.get() == 1 && space <= 12000) {
                        result.incrementAndGet();
                    }

                    if (result.get() == 1) {
                        return 5000;
                    } else {
                        return 0;
                    }

                } finally {
                    if (result.get() == 2) {
                        latch.countDown();
                    }
                }
            }
        });

        wheel.add(System.currentTimeMillis(), 5000);
//        wheel.add("test", 5000);
        latch.await();
        TimeUnit.SECONDS.sleep(5L);
        wheel.size();
        Assert.assertEquals(2, result.get());
        Assert.assertEquals(0, wheel.size());
        Assert.assertFalse(wheel.exist("test"));

    }
}