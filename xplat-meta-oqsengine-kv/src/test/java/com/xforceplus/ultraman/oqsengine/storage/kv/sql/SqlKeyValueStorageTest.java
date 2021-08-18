package com.xforceplus.ultraman.oqsengine.storage.kv.sql;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.ReflectionUtils;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.kv.AbstractKVTest;
import com.xforceplus.ultraman.oqsengine.storage.kv.sql.transaction.SqlKvConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
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
public class SqlKeyValueStorageTest extends AbstractKVTest {

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

        ResourceLocker locker = new LocalResourceLocker();
        fields = ReflectionUtils.printAllMembers(storage);
        ReflectionUtils.reflectionFieldValue(fields, "locker", storage, locker);
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


    @Override
    public KeyValueStorage getKv() {
        return this.storage;
    }
}