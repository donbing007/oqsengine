package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 多时间轮实现测试.
 *
 * @author weikai
 * @data 2021/5/21 10:01
 * @mail weikai@xforceplus.com
 */
public class MultipleTimerWheelTest {

    private MultipleTimerWheel wheel;

    /**
     * 清理.
     */
    @AfterEach
    public void tearDown() throws Exception {
        if (this.wheel != null) {
            this.wheel.destroy();
        }

        this.wheel = null;
    }

    @Test
    public void testAdd() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        wheel = new MultipleTimerWheel(new TimeoutNotification<Long>() {
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

        Assertions.assertEquals(1, result.get());
    }

    @Test
    public void testParallelAdd() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(500000);
        final AtomicInteger result = new AtomicInteger(0);
        wheel = new MultipleTimerWheel(500, 100, (TimeoutNotification<Object>) addTime -> {
            try {
                result.incrementAndGet();
                return 0;

            } finally {
                latch.countDown();
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
        TimeUnit.SECONDS.sleep(5);
        int size = wheel.size();
        Assertions.assertEquals(500000, result.get());
        Assertions.assertEquals(0, size);
    }

    @Test
    public void testSize() throws Exception {
        wheel = new MultipleTimerWheel(t -> {
            Assertions.fail("Unexpected elimination can be known.");

            return 0;
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assertions.assertEquals(1, wheel.size());
    }

    @Test
    public void testParallelSize() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        wheel = new MultipleTimerWheel(t -> 0);

        for (int i = 0; i < 500000; i++) {
            int finalI1 = i;
            executorService.submit(() -> {
                wheel.add(new Object(), 18000000);
            });
        }

        for (int i = 0; i < 100000; i++) {
            if (500000 == wheel.size()) {
                break;
            } else {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
            }
        }

        Assertions.assertEquals(500000, wheel.size());
    }

    @Test
    public void testRemove() throws Exception {
        wheel = new MultipleTimerWheel(t -> {
            Assertions.fail("Unexpected elimination can be known.");

            return 0;
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assertions.assertEquals(1, wheel.size());
        wheel.remove(target);
        Assertions.assertEquals(0, wheel.size());
    }

    @Test
    public void testExist() throws Exception {
        wheel = new MultipleTimerWheel(t -> {
            Assertions.fail("Unexpected elimination can be known.");

            return 0;
        });

        Object target = new Object();
        wheel.add(target, 180000000);

        Assertions.assertTrue(wheel.exist(target));
    }

    @Test
    public void testCleanExpire() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        wheel = new MultipleTimerWheel((TimeoutNotification<String>) text -> {
            try {
                return 0;
            } finally {
                latch.countDown();
            }
        });

        wheel.add("test", 3000);
        latch.await();

        TimeUnit.SECONDS.sleep(1L);
        Assertions.assertEquals(0, wheel.size());
        Assertions.assertFalse(wheel.exist("test"));
    }

    @Test
    public void testExpireAddValue() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        wheel = new MultipleTimerWheel((TimeoutNotification<Long>) addTime -> {
            try {
                long expireTime = System.currentTimeMillis();

                long space = expireTime - addTime;
                // 6000是因为wheel不是一个绝对准确的实现,所以终止时间会有误差.
                if (result.get() == 0 && space <= 6000) {
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
        });

        wheel.add(System.currentTimeMillis(), 5000);
        latch.await();
        TimeUnit.SECONDS.sleep(5L);
        wheel.size();
        Assertions.assertEquals(2, result.get());
        Assertions.assertEquals(0, wheel.size());
        Assertions.assertFalse(wheel.exist("test"));
    }
}
