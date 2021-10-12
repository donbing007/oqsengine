package com.xforceplus.ultraman.oqsengine.storage.executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * AutoTransactionExecutor Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
@ExtendWith({RedisContainer.class})
public class AutoJoinTransactionExecutorTest {
    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    @BeforeEach
    public void before() throws Exception {
        redisClient = CommonInitialization.getInstance().getRedisClient();
        commitIdStatusService = StorageInitialization.getInstance().getCommitIdStatusService();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
    }

    /**
     * 测试没有外部事务.
     */
    @Test
    public void testNoTransaction() throws Exception {

        DataSource mockDataSource = mock(DataSource.class);
        Connection expectedConn = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(expectedConn);

        Selector<DataSource> dataSourceSelector = key -> mockDataSource;

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(StorageInitialization.getInstance()
            .getTransactionManager(),
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((transaction, resource, hint) -> {

            Connection conn = (Connection) resource.value();
            Assertions.assertEquals(expectedConn, conn);

            return null;
        });


        verify(expectedConn, times(1)).setAutoCommit(true);
        verify(expectedConn, times(1)).close();
    }

    @Test
    public void testAlreadyInTransactionNoResource() throws Exception {
        DataSource mockDataSource = mock(DataSource.class);
        Connection expectedConn = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(expectedConn);
        Selector<DataSource> dataSourceSelector = key -> mockDataSource;

        TransactionManager tm = StorageInitialization.getInstance()
            .getTransactionManager();

        Transaction tx = tm.create();
        tm.bind(tx.id());

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm,
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((transaction, resource, hint) -> {
            Connection conn = (Connection) resource.value();
            Assertions.assertEquals(expectedConn, conn);

            return null;
        });

        Optional<Transaction> t = tm.getCurrent();
        Assertions.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().queryTransactionResource(mockDataSource.toString() + ".table");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(expectedConn, resource.get().value());

    }

    @Test
    public void testAlreadyInTransactionResource() throws Exception {

        DataSource mockDataSource = mock(DataSource.class);
        Connection expectedConn = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(expectedConn);
        Selector<DataSource> dataSourceSelector = key -> mockDataSource;

        TransactionManager tm = StorageInitialization.getInstance()
            .getTransactionManager();

        Transaction currentT = tm.create();
        tm.bind(currentT.id());
        currentT.join(new MockConnectionTransactionResource(mockDataSource.toString(), expectedConn, false));

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm,
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((tx, resource, hint) -> {
            Assertions.assertEquals(currentT.queryTransactionResource(mockDataSource.toString() + ".table").get(), resource);

            return null;
        });

        Optional<Transaction> t = tm.getCurrent();
        Assertions.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().queryTransactionResource(mockDataSource.toString() + ".table");
        Assertions.assertTrue(resource.isPresent());
        Assertions.assertEquals(expectedConn, resource.get().value());

    }

    static class MockConnectionTransactionResource extends AbstractConnectionTransactionResource {

        public MockConnectionTransactionResource(String key, Connection value, boolean autoCommit) throws SQLException {
            super(key, value, autoCommit);
        }

        @Override
        public TransactionResourceType type() {
            return TransactionResourceType.MASTER;
        }
    }
}
