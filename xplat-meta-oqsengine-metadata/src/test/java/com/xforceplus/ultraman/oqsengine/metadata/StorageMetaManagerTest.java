package com.xforceplus.ultraman.oqsengine.metadata;

import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class StorageMetaManagerTest extends MetaTestHelper {

    final Logger logger = LoggerFactory.getLogger(StorageMetaManagerTest.class);

    private static final String NEED_CONCURRENT_APP_ID = "test";
    private static final String[] NEED_CONCURRENT_APP_ENV_LIST = {"1", "2", "3"};

    ExecutorService executorService = Executors.newFixedThreadPool(3);

    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
    }

    @Test
    public void needConcurrentTest() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch startCountDown = new CountDownLatch(1);
        CountDownLatch endCountDown = new CountDownLatch(NEED_CONCURRENT_APP_ENV_LIST.length);
        List<Future> futures = Lists.newArrayList();
        for (int j = 0; j < NEED_CONCURRENT_APP_ENV_LIST.length; j++) {
            final int pos = j;
            Future future = executorService.submit(() -> {
                try {
                    startCountDown.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    MetaInitialization.getInstance().getMetaManager().need(NEED_CONCURRENT_APP_ID, NEED_CONCURRENT_APP_ENV_LIST[pos]);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    logger.warn(e.getMessage());
                } finally {
                    endCountDown.countDown();
                }
            });
            futures.add(future);
        }

        startCountDown.countDown();

        endCountDown.await();

        Assertions.assertEquals(1, successCount.get());
        Assertions.assertEquals(NEED_CONCURRENT_APP_ENV_LIST.length - 1, failCount.get());
    }
}
