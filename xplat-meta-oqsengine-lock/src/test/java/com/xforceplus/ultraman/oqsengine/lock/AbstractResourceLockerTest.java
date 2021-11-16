package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 锁测试的基础.
 *
 * @author dongbin
 * @version 0.1 2021/08/09 18:04
 * @since 1.8
 */
@Disabled
public abstract class AbstractResourceLockerTest {

    final Logger logger = LoggerFactory.getLogger(AbstractResourceLockerTest.class);

    @Test
    public void testLock() throws Exception {
        String key = "test";
        ExecutorService worker = Executors.newFixedThreadPool(20, ExecutorHelper.buildNameThreadFactory("lock"));

        int size = 2;
        CountDownLatch latch = new CountDownLatch(size);
        List<Integer> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            worker.submit(() -> {
                logger.info("Attempt to lock with number {}.", finalI);
                getLocker().lock(key);
                logger.info("Gets the lock with the current serial number {}.", finalI);
                try {
                    values.add(finalI);
                    logger.info("Add value {} to list.", finalI);
                } finally {
                    getLocker().unlock(key);
                    latch.countDown();
                }
            });
        }

        latch.await();
        worker.shutdown();

        Assertions.assertEquals(size, values.size());
    }

    @Test
    public void testReentrant() throws Exception {
        String key = "test";
        Assertions.assertTrue(getLocker().tryLock(key));
        Assertions.assertTrue(getLocker().tryLock(key));
        Assertions.assertTrue(getLocker().tryLock(key, 5, TimeUnit.SECONDS));

        Assertions.assertTrue(getLocker().unlock(key));
        Assertions.assertTrue(getLocker().unlock(key));
        Assertions.assertTrue(getLocker().unlock(key));

    }

    public abstract ResourceLocker getLocker();
}
