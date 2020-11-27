package com.xforceplus.ultraman.oqsengine.common.lock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LocalResourceLocker Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/26/2020
 * @since <pre>Nov 26, 2020</pre>
 */
public class LocalResourceLockerTest {

    final Logger logger = LoggerFactory.getLogger(LocalResourceLockerTest.class);

    private LocalResourceLocker locker;

    @Before
    public void before() throws Exception {
        locker = new LocalResourceLocker();
    }

    @After
    public void after() throws Exception {
        locker = null;
    }

    @Test
    public void testLock() throws Exception {
        String key = "test";
        ExecutorService worker = Executors.newFixedThreadPool(20);

        int size = 100;
        CountDownLatch latch = new CountDownLatch(size);
        List<Integer> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            worker.submit(() -> {
                locker.lock(key);
                logger.info("Gets the lock with the current serial number {}.", finalI);
                try {
                    values.add(finalI);
                    logger.info("Add value {} to list.", finalI);
                } finally {
                    locker.unlock(key);
                    latch.countDown();
                }
            });
        }

        latch.await();
        worker.shutdown();

        Assert.assertEquals(size, values.size());
    }


} 
