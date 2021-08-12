package com.xforceplus.ultraman.oqsengine.storage.kv.sql;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction.SqlKvConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.AbstractKeyIterator;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
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
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 基于SQL的kv储存测试.
 *
 * @author dongbin
 * @version 0.1 2021/07/28 13:57
 * @since 1.8
 */
@ExtendWith({MysqlContainer.class, RedisContainer.class})
public class SqlKeyValueStorageTest {

    private SqlKeyValueStorage storage;
    private DataSource ds;

    /**
     * 初始化测试实例.
     */
    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:kv/oqsengine-ds.conf");


        storage = new SqlKeyValueStorage();
        storage.setTableName("kv");
        storage.setTimeout(200);


        ds = CommonInitialization.getInstance().getDataSourcePackage(true).getFirstMaster();

        AutoJoinTransactionExecutor executor = new AutoJoinTransactionExecutor(
            StorageInitialization.getInstance().getTransactionManager(),
            new SqlKvConnectionTransactionResourceFactory(),
            NoSelector.build(ds),
            NoSelector.build("kv"));

        Collection<Field> fields = ReflectionUtils.printAllMembers(storage);
        ReflectionUtils.reflectionFieldValue(fields, "transactionExecutor", storage, executor);

    }

    /**
     * 每次测试后的清理.
     */
    @AfterEach
    public void after() throws Exception {
        try (Connection conn = ds.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("truncate table kv");
            }
        }
    }

    @Test
    public void testFailKey() throws Exception {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            storage.save(null, null);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            storage.save("12%3", null);
        });
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
        Assertions.assertEquals(size, storage.save(kvs));

        for (Map.Entry<String, byte[]> kv : kvs) {
            Assertions.assertArrayEquals(kv.getValue(), storage.get(kv.getKey()).get());
        }


        Map<String, byte[]> results = storage.get(kvs.stream().map(kv -> kv.getKey()).toArray(String[]::new))
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
        Assertions.assertEquals(size, storage.save(kvs));

        String targetKey = kvs.stream().findFirst().get().getKey();
        storage.delete(targetKey);
        Assertions.assertFalse(storage.get(targetKey).isPresent());

        storage.delete(kvs.stream().map(r -> r.getKey()).toArray(String[]::new));
        for (Map.Entry<String, byte[]> kv : kvs) {
            Assertions.assertFalse(storage.exist(kv.getKey()));
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
        Assertions.assertEquals(size, storage.save(kvs));

        for (int i = 0; i < size; i++) {
            Assertions.assertFalse(
                storage.add(
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

                    result = storage.add(key, value);

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
            storage.save(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i),
                String.format("testvalue-%d", i).getBytes());
        }

        for (int i = 0; i < size; i++) {
            Assertions.assertArrayEquals(String.format("testvalue-%d", i).getBytes(),
                storage.get(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i)).get());
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
        Assertions.assertEquals(size, storage.save(kvs));

        for (int i = 0; i < size; i++) {
            Assertions.assertArrayEquals(String.format("testvalue-%d", i).getBytes(),
                storage.get(String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i)).get());
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

        AbstractKeyIterator iter = storage.iterator(keyPrefix);
        if (iter.provideSize()) {
            Assertions.assertEquals(1000, iter.size());
        }

        String key;
        int i = 0;
        StringBuilder buff = new StringBuilder();
        while (iter.hasNext()) {
            key = iter.next();

            for (int p = 0; p < 22 - NumberUtils.size(i); p++) {
                buff.append('0');
            }
            buff.append(i++);

            Assertions.assertEquals(keyPrefix + buff.toString(), key);

            buff.delete(0, buff.length());
        }

        Assertions.assertEquals(1000, i);

        iter = storage.iterator(keyPrefix, false);
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
                           Function<Long, byte[]> value)
        throws IOException {
        if (size <= 0) {
            return;
        }

        Collection<Map.Entry<String, byte[]>> kvs = new ArrayList<>();
        for (long i = start; i < start + size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(prefix + keySuffix.apply(i), value.apply(i)));
        }

        long successNumber = storage.save(kvs);
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