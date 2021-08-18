package com.xforceplus.ultraman.oqsengine.storage.kv;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * kv实现的基础测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/17 11:45
 * @since 1.8
 */
@Disabled
public abstract class AbstractKVTest {

    public abstract KeyValueStorage getKv();

    @Test
    public void testIncr() throws Exception {
        final int size = 100;
        final String key = "incr";
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(size);
        ExecutorService worker = Executors.newFixedThreadPool(size);
        for (int i = 0; i < size; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                getKv().incr(key);
                finishLatch.countDown();
            }, worker);
        }

        startLatch.countDown();
        finishLatch.await();

        Assertions.assertEquals(size, getKv().incr(key, 0));

        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    @Test
    public void testGetAndGets() throws Exception {
        int size = 100;

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(
                String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes()));
        }
        Assertions.assertEquals(size, getKv().save(kvs));

        for (Map.Entry<String, byte[]> kv : kvs) {
            Assertions.assertArrayEquals(kv.getValue(), getKv().get(kv.getKey()).get());
        }


        Map<String, byte[]> results = getKv().get(kvs.stream().map(kv -> kv.getKey()).toArray(String[]::new))
            .stream().collect(Collectors.toMap(r -> r.getKey(), r -> r.getValue(), (r0, r1) -> r0));
        Assertions.assertEquals(results.size(), kvs.size());
        for (Map.Entry<String, byte[]> kv : kvs) {
            Assertions.assertArrayEquals(kv.getValue(), results.get(kv.getKey()));
        }
    }

    @Test
    public void testDeleteAndDeletes() throws Exception {
        int size = 100;

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(
                String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes()));
        }
        Assertions.assertEquals(size, getKv().save(kvs));

        String targetKey = kvs.stream().findFirst().get().getKey();
        getKv().delete(targetKey);
        Assertions.assertFalse(getKv().get(targetKey).isPresent());

        getKv().delete(kvs.stream().map(r -> r.getKey()).toArray(String[]::new));
        for (Map.Entry<String, byte[]> kv : kvs) {
            Assertions.assertFalse(getKv().exist(kv.getKey()));
        }
    }

    @Test
    public void testAdd() throws Exception {
        int size = 100;

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(
                String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes()));
        }
        Assertions.assertEquals(size, getKv().save(kvs));

        for (int i = 0; i < size; i++) {
            Assertions.assertFalse(
                getKv().add(
                    String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                    String.format("testvalue-%d", i).getBytes()));
        }
    }

    @Test
    public void testConcurrentAdd() throws Exception {
        ExecutorService worker = Executors.newFixedThreadPool(30,
            ExecutorHelper.buildNameThreadFactory("kv-add-test"));

        String key = "lock-test-key";
        byte[] value = key.getBytes();

        int size = 10000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(size);
        LongAdder success = new LongAdder();
        LongAdder fail = new LongAdder();
        for (int i = 0; i < size; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                boolean result = false;
                try {

                    result = getKv().add(key, value);

                } finally {

                    if (result) {
                        success.increment();
                    } else {
                        fail.increment();
                    }

                    endLatch.countDown();
                }
            }, worker);
        }
        startLatch.countDown();
        endLatch.await();

        ExecutorHelper.shutdownAndAwaitTermination(worker);

        Assertions.assertEquals(1, success.longValue());
        Assertions.assertEquals(size - 1, fail.longValue());
    }

    @Test
    public void testSave() throws Exception {
        int size = 100;
        for (int i = 0; i < size; i++) {
            getKv().save(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes());
        }

        for (int i = 0; i < size; i++) {
            Assertions.assertArrayEquals(String.format("testvalue-%d", i).getBytes(),
                getKv().get(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i)).get());
        }
    }

    @Test
    public void testSaveBatch() throws Exception {
        int size = 100;

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(
                String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes()));
        }
        Assertions.assertEquals(size, getKv().save(kvs));

        for (int i = 0; i < size; i++) {
            Assertions.assertArrayEquals(String.format("testvalue-%d", i).getBytes(),
                getKv().get(String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i)).get());
        }
    }

    @Test
    public void testIteratorSeek() throws Exception {
        String keyPrefix = "seek-prefix-";
        buildData(keyPrefix, 0, 10,
            (i) -> String.join("-", NumberUtils.zeroFill(i)),
            (i) -> null);

        KeyIterator iter = getKv().iterator(keyPrefix);
        // 应该从第 seek-preifx-0000000000000000006 即 seek-preifx-0000000000000000005 之后.
        String seekKey = String.format("%s%s", keyPrefix, String.join("-", NumberUtils.zeroFill(5L)));
        iter.seek(seekKey);

        long startIndex = 6;
        while (iter.hasNext()) {
            String key = iter.next();

            Assertions.assertEquals(iter.currentKey(), key);

            String expectedKey = String.format("%s%s", keyPrefix,
                String.join("-", NumberUtils.zeroFill(startIndex++)));

            Assertions.assertEquals(expectedKey, key);
        }

    }

    @Test
    public void testIterator() throws Exception {
        // 填充数据,主要起到干扰作用.
        buildData("chaos-prefix-",
            0,
            1000,
            (i) -> String.join("", buildRandomString(10), Long.toString(i)),
            (i) -> buildRandomString(100).getBytes());

        String keyPrefix = String.format("l-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - 1);
        buildData(
            keyPrefix,
            0,
            1000,
            (i) -> {
                // 填充末尾数字为22位.
                StringBuilder buff = new StringBuilder();
                for (int p = 0; p < 22 - NumberUtils.size(i); p++) {
                    buff.append('0');
                }
                buff.append(i);
                return buff.toString();
            },
            (i) -> Long.toString(i).getBytes());

        KeyIterator iter = getKv().iterator(keyPrefix);
        if (iter.provideSize()) {
            Assertions.assertEquals(1000, iter.size());
        }

        String key = null;
        int i = 0;
        StringBuilder buff = new StringBuilder();
        while (iter.hasNext()) {
            key = iter.next();

            Assertions.assertEquals(iter.currentKey(), key);

            for (int p = 0; p < 22 - NumberUtils.size(i); p++) {
                buff.append('0');
            }
            buff.append(i++);

            Assertions.assertEquals(keyPrefix + buff.toString(), key);

            buff.delete(0, buff.length());
        }

        Assertions.assertEquals(1000, i);

        iter = getKv().iterator(keyPrefix, false);
        if (iter.provideSize()) {
            Assertions.assertEquals(1000, iter.size());
        }

        i = 1000 - 1;
        buff = new StringBuilder();
        while (iter.hasNext()) {
            key = iter.next();

            for (int p = 0; p < 22 - NumberUtils.size(i); p++) {
                buff.append('0');
            }
            buff.append(i--);

            Assertions.assertEquals(keyPrefix + buff.toString(), key);

            buff.delete(0, buff.length());
        }
    }

    private void buildData(String prefix, long start, long size,
                           Function<Long, String> keySuffix,
                           Function<Long, byte[]> value) throws IOException {
        if (size <= 0) {
            return;
        }

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>();
        for (long i = start; i < start + size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(prefix + keySuffix.apply(i), value.apply(i)));
        }

        long successNumber = getKv().save(kvs);
        Assertions.assertEquals(size, successNumber);
    }

    static String buildRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
