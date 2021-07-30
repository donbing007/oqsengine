package com.xforceplus.ultraman.oqsengine.storage.kv.sql;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.serializable.KryoSerializeStrategy;
import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction.SqlKvConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import javax.sql.DataSource;
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

    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:kv/oqsengine-ds.conf");


        storage = new SqlKeyValueStorage();
        storage.setTableName("kv");
        storage.setTimeout(200);


        DataSource ds = CommonInitialization.getInstance().getDataSourcePackage(true).getFirstMaster();

        AutoJoinTransactionExecutor executor = new AutoJoinTransactionExecutor(
            StorageInitialization.getInstance().getTransactionManager(),
            new SqlKvConnectionTransactionResourceFactory(),
            NoSelector.build(ds),
            NoSelector.build("kv"));

        Collection<Field> fields = ReflectionUtils.printAllMembers(storage);
        ReflectionUtils.reflectionFieldValue(fields, "transactionExecutor", storage, executor);

        SerializeStrategy serializeStrategy = new KryoSerializeStrategy();
        ReflectionUtils.reflectionFieldValue(fields, "serializeStrategy", storage, serializeStrategy);

    }

    @Test
    public void testFailKey() throws Exception {

        Assertions.assertThrows(SQLException.class, () -> {
            storage.save(null, null);
        });

        Assertions.assertThrows(SQLException.class, () -> {
            storage.save("12%3", "123");
        });
    }

    @Test
    public void testSave() throws Exception {
        int size = 100;
        for (int i = 0; i < size; i++) {
            storage.save(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i), "testvalue-" + i);
        }

        for (int i = 0; i < size; i++) {
            Assertions.assertEquals("testvalue-" + i,
                storage.get(String.format("lookup-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i)).get());
        }
    }

    @Test
    public void testSaveBatch() throws Exception {
        int size = 100;

        Collection<Map.Entry<String, Object>> kvs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            kvs.add(new AbstractMap.SimpleEntry<>(
                String.format("batch-%d-%d", Long.MAX_VALUE, Long.MAX_VALUE - i), "testvalue-" + i));
        }
        Assertions.assertEquals(size, storage.save(kvs));

        for (int i = 0; i < size; i++) {
            Assertions.assertEquals("testvalue-" + i,
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
            (i) -> buildRandomString(100));

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
            (i) -> Long.toString(i));

        DataIterator<String> iter = storage.iterator(keyPrefix);
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
    }

    private void buildData(String prefix, long start, long size,
                           Function<Long, String> keySuffix,
                           Function<Long, String> value)
        throws SQLException {
        if (size <= 0) {
            return;
        }

        Collection<Map.Entry<String, Object>> kvs = new ArrayList<>();
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