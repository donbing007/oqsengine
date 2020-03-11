package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import org.junit.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimerWheel Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/10/2020
 * @since <pre>Mar 10, 2020</pre>
 */
public class TimerWheelTest {

    public TimerWheelTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * 测试只有一个过期对象,将在5秒后过期,误差在1秒内.
     */
    @Test
    public void testAddValue() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        TimerWheel wheel = new TimerWheel(new TimeoutNotification<Long>() {
            @Override
            public long notice(Long addTime) {
                try {
                    long expireTime = System.currentTimeMillis();

                    long space = expireTime - addTime;
                    if (space <= 6000) {//6000是因为wheel不是一个绝对准确的实现,所以终止时间会有误差.
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

    /**
     * 测试淘汰后,直接在回调中再次写回环中.
     */
    @Test
    public void testExpireAddValue() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger(0);
        TimerWheel wheel = new TimerWheel(new TimeoutNotification<Long>() {
            @Override
            public long notice(Long addTime) {
                try {
                    long expireTime = System.currentTimeMillis();

                    long space = expireTime - addTime;
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
        latch.await();

        Assert.assertEquals(2, result.get());
    }

    /**
     * 不淘汰,主动删除.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveValue() throws Exception {
        TimerWheel wheel = new TimerWheel(new TimeoutNotification() {
            @Override
            public long notice(Object t) {
                Assert.fail("Unexpected elimination can be known.");

                return 0;
            }
        });

        Object target = new Object();
        wheel.add(target, 5000);

        Assert.assertEquals(1, wheel.size());
        wheel.remove(target);
        Assert.assertEquals(0, wheel.size());
    }

}
