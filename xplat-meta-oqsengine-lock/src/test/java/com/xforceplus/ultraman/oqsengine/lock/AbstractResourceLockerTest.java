package com.xforceplus.ultraman.oqsengine.lock;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
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
        MultiResourceLocker mlocker = getMultiLocker();
        if (mlocker == null) {
            return;
        }

        String[] keys = new String[2];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        // 先将某个锁加锁,此锁应该会造成后续的连锁加锁失败.
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture.runAsync(() -> {
            mlocker.locks(keys[0]);
            latch.countDown();
        });
        latch.await();

        Assertions.assertFalse(mlocker.tryLocks(keys));
        // 确认加锁状态,应该只有第一个key处于锁定状态.
        Assertions.assertFalse(mlocker.tryLocks(keys[0]));
        for (int i = 1; i < keys.length; i++) {
            Assertions.assertTrue(mlocker.tryLock(keys[i]));
        }
        Assertions.assertTrue(mlocker.isLocking(keys[0]));

    }

    /**
     * 如果锁实现支持连锁,那么此测试将测试在并发情况下是否会造成死锁.
     */
    @Test
    public void testMultiLockerConcurrent() throws Exception {
        MultiResourceLocker mlocker = getMultiLocker();
        if (mlocker == null) {
            return;
        }

        String[] keys = new String[100];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "test.new.key." + (Long.MAX_VALUE - i);
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

                String[] currentKeys = Arrays.stream(keys).toArray(String[]::new);
                shuffle(currentKeys);

                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }

                mlocker.locks(currentKeys);
                logger.info("Task {} lock successful!", finalI);

                try {
                    expectedNumber.value++;
                } finally {

                    logger.info("Task {} unlock successful!", finalI);

                    mlocker.unlocks(currentKeys);

                    unlockNumber.decrementAndGet();

                    finishLatch.countDown();

                }

            });
        }

        startLatch.countDown();
        finishLatch.await(50, TimeUnit.SECONDS);


        Assertions.assertEquals(size, expectedNumber.value);
        Assertions.assertEquals(0, unlockNumber.longValue());
    }

    @Test
    public void test() throws Exception {
        String[] keys = IntStream.range(0, 10000).mapToObj(i -> "locker.key" + i).toArray(String[]::new);
        long lockStart;
        long lockEnd;
        long unlockStart;
        long unlockEnd;
        for (int i = 0; i < 10000; i++) {

            try {
                lockStart = System.currentTimeMillis();
                getMultiLocker().locks(keys);
                lockEnd = System.currentTimeMillis();
            } finally {
                unlockStart = System.currentTimeMillis();
                getMultiLocker().unlocks(keys);
                unlockEnd = System.currentTimeMillis();
            }

            System.out.printf("lock ms: %d, unlock ms: %d\n", lockEnd - lockStart, unlockEnd - unlockStart);
        }
    }

    static class IntegerHolder {
        int value;

        public IntegerHolder() {
            this.value = 0;
        }
    }


    static Random rand = new Random();

    private <T> void shuffle(T[] arr) {
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randInd = rand.nextInt(i);
            T temp = arr[randInd];
            arr[randInd] = arr[i - 1];
            arr[i - 1] = temp;
        }
    }

    public abstract ResourceLocker getLocker();

    public abstract MultiResourceLocker getMultiLocker();
}
