package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
public abstract class AbstractRetryResourceLockerTest {

    final Logger logger = LoggerFactory.getLogger(AbstractRetryResourceLockerTest.class);

    static ThreadPoolExecutor WORKER;

    /**
     * 全局初始化.
     */
    @BeforeAll
    public static void beforeAll() throws Exception {
        WORKER = new ThreadPoolExecutor(10, 30,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(),
            ExecutorHelper.buildNameThreadFactory("lock"));
    }

    /**
     * 全局清理.
     */
    @AfterAll
    public static void afterAll() throws Exception {
        if (WORKER != null) {
            ExecutorHelper.shutdownAndAwaitTermination(WORKER);
        }
    }

    /**
     * every test init.
     */
    @BeforeEach
    public void before() throws Exception {
        for (int i = 0; i < 10000; i++) {
            if (WORKER.getActiveCount() > 0) {

                logger.info("Task thread pool still has tasks, wait 1 second.");

                LockSupport.parkNanos(this, TimeUnit.SECONDS.toNanos(1));
            }
        }
    }

    @AfterEach
    public void after() throws Exception {

    }

    /**
     * 对没有加锁的资源进行解锁.
     */
    @Test
    public void testUnlockedAndUnlocked() throws Exception {
        String[] resources = new String[1000];
        for (int i = 0; i < resources.length; i++) {
            resources[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        String[] failResource = getLocker().unlocks(resources);
        Assertions.assertTrue(failResource.length == 0);

        Assertions.assertTrue(getLocker().unlock(resources[0]));
    }

    /**
     * 解锁测试,
     */
    @Test
    public void testUnlock() throws Exception {
        String[] resources = new String[3];
        resources[0] = "l1.locked";
        resources[1] = "l2.locker";
        resources[2] = "l1.no.locked";

        getLocker().lock(resources[0]);
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            try {
                getLocker().lock(resources[1]);
            } catch (InterruptedException e) {
                // donothing
            }
            latch.countDown();
        });
        latch.await();

        Assertions.assertTrue(getLocker().isLocking(resources[0]));
        Assertions.assertTrue(getLocker().isLocking(resources[1]));
        Assertions.assertFalse(getLocker().isLocking(resources[2]));

        String[] failResources = getLocker().unlocks(resources);
        String[] expectedFailResources = new String[] {
            resources[1]
        };

        Assertions.assertArrayEquals(expectedFailResources, failResources);
    }

    /**
     * 额外线程试图加锁,应该被拒绝.
     */
    @Test
    public void testLockNotTheSameThread() throws Exception {
        String resource = "test.resource";
        getLocker().lock(resource);

        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                return getLocker().tryLock(resource);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        Assertions.assertFalse(future.get());
        Assertions.assertTrue(getLocker().isLocking(resource));
        Assertions.assertTrue(getLocker().unlock(resource));
        Assertions.assertFalse(getLocker().isLocking(resource));
    }

    /**
     * 非加锁者试图解锁,应该失败.
     */
    @Test
    public void testUnlockNotTheSameThread() throws Exception {
        String resource = "test.resource";
        getLocker().lock(resource);

        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                return getLocker().unlock(resource);
            } finally {
                latch.countDown();
            }
        });
        latch.await();

        Assertions.assertFalse(future.get());
        Assertions.assertTrue(getLocker().isLocking(resource));
        Assertions.assertTrue(getLocker().unlock(resource));
        Assertions.assertFalse(getLocker().isLocking(resource));
    }

    @Test
    public void testLock() throws Exception {
        String key = "test";

        int size = 2;
        CountDownLatch latch = new CountDownLatch(size);
        List<Integer> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            WORKER.submit(() -> {
                logger.info("Attempt to lock with number {}.", finalI);
                try {
                    getLocker().lock(key);
                } catch (InterruptedException e) {
                    return;
                }
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

        Assertions.assertEquals(size, values.size());
    }

    @Test
    public void testReentrant() throws Exception {
        String key = "test";
        Assertions.assertTrue(getLocker().tryLock(key));
        Assertions.assertTrue(getLocker().tryLock(key));
        Assertions.assertTrue(getLocker().tryLock(5000, key));

        Assertions.assertTrue(getLocker().unlock(key));
        Assertions.assertTrue(getLocker().unlock(key));
        Assertions.assertTrue(getLocker().unlock(key));
    }

    /**
     * 测试连锁时某个锁失败,是否成功清除同批次中已经加锁的锁.
     * 即连锁加锁失败不能改变原有锁的状态.
     */
    @Test
    public void testMultiLockerNoGc() throws Exception {
        ResourceLocker mlocker = getLocker();

        String[] resources = new String[2];
        for (int i = 0; i < resources.length; i++) {
            resources[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        // 先将某个锁加锁,此锁应该会造成后续的连锁加锁失败.
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            try {
                mlocker.locks(resources[0]);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                return;
            }
            latch.countDown();
        }, WORKER);
        latch.await();

        Assertions.assertFalse(mlocker.tryLocks(resources));
        // 确认加锁状态,应该只有第一个key处于锁定状态.
        Assertions.assertFalse(mlocker.tryLocks(resources[0]));
        for (int i = 1; i < resources.length; i++) {
            Assertions.assertTrue(mlocker.tryLock(resources[i]));
        }
        Assertions.assertTrue(mlocker.isLocking(resources[0]));
    }

    /**
     * 如果锁实现支持连锁,那么此测试将测试在并发情况下是否会造成死锁.
     */
    @Test
    public void testMultiLockerConcurrent() throws Exception {
        ResourceLocker mlocker = getLocker();

        String[] resources = new String[1000];
        for (int i = 0; i < resources.length; i++) {
            resources[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        int size = 30;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(size);
        /*
         * expectedNumber用以确认是否最终加锁成功,所以此计算器不能为线程安全的.
         */
        IntegerHolder expectedNumber = new IntegerHolder();
        AtomicInteger unlockNumber = new AtomicInteger(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {

                String[] copyResources = Arrays.stream(resources).toArray(String[]::new);
                shuffle(copyResources);

                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }

                try {
                    mlocker.locks(copyResources);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
                logger.info("Task {} lock successful!", finalI);

                try {
                    expectedNumber.value++;
                } finally {

                    logger.info("Task {} unlock successful!", finalI);

                    mlocker.unlocks(copyResources);

                    unlockNumber.decrementAndGet();

                    finishLatch.countDown();

                }

            }, WORKER);
        }

        startLatch.countDown();
        finishLatch.await();


        Assertions.assertEquals(size, expectedNumber.value);
        Assertions.assertEquals(0, unlockNumber.longValue());
        for (String resource : resources) {
            Assertions.assertFalse(mlocker.isLocking(resource));
        }
    }

    /**
     * 大列表和单个冲突,应该正确互斥.
     */
    @Test
    public void testMultipleAndSingleCompetition() throws Exception {
        ResourceLocker locker = getLocker();

        String[] resources = IntStream.range(0, 10000).mapToObj(i -> "res." + i).toArray(String[]::new);
        // 此key排在第3个位置,下标为2.
        String oneResoruce = "res.10";

        locker.lock(oneResoruce);

        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            boolean ok = false;
            try {
                ok = locker.tryLocks(1000, resources);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
            latch.countDown();
            return ok;
        }, WORKER);
        latch.await();

        Assertions.assertFalse(future.get());
        long unlockSize =
            Arrays.stream(resources).filter(r -> !r.equals(oneResoruce)).filter(r -> !locker.isLocking(r)).count();
        Assertions.assertEquals(resources.length - 1, unlockSize);
    }

    static class IntegerHolder {
        int value;

        public IntegerHolder() {
            this.value = 0;
        }
    }

    // 打乱顺序
    private <T> void shuffle(T[] arr) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randInd = rand.nextInt(i);
            T temp = arr[randInd];
            arr[randInd] = arr[i - 1];
            arr[i - 1] = temp;
        }
    }

    public abstract ResourceLocker getLocker();
}
