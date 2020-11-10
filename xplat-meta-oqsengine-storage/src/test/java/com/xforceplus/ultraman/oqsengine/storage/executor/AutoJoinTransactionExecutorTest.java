package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.transaction.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * AutoTransactionExecutor Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class AutoJoinTransactionExecutorTest {

    private LongIdGenerator idGenerator;
    private TransactionManager tm;

    @Before
    public void before() throws Exception {
        idGenerator = new IncreasingOrderLongIdGenerator();
        tm = new DefaultTransactionManager(idGenerator);
    }

    @After
    public void after() throws Exception {
        Optional<Transaction> t = tm.getCurrent();
        if (t.isPresent()) {
            if (!t.get().isCompleted()) {
                t.get().rollback();
            }

            tm.finish(t.get());
        }
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

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm, MockConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingStorageTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                Connection conn = (Connection) resource.value();
                Assert.assertEquals(expectedConn, conn);

                return null;
            }
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

        Transaction tx = tm.create();
        tm.bind(tx.id());

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm, MockConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingStorageTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                Connection conn = (Connection) resource.value();
                Assert.assertEquals(expectedConn, conn);

                return null;
            }
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource.toString());
        Assert.assertTrue(resource.isPresent());
        Assert.assertEquals(expectedConn, resource.get().value());

    }

    @Test
    public void testAlreadyInTransactionResource() throws Exception {

        DataSource mockDataSource = mock(DataSource.class);
        Connection expectedConn = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(expectedConn);
        Selector<DataSource> dataSourceSelector = key -> mockDataSource;

        Transaction currentT = tm.create();
        tm.bind(currentT.id());
        currentT.join(new MockConnectionTransactionResource(mockDataSource.toString(), expectedConn, false));

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm, MockConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingStorageTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                Assert.assertEquals(currentT.query(mockDataSource.toString()).get(), resource);

                return null;
            }
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource.toString());
        Assert.assertTrue(resource.isPresent());
        Assert.assertEquals(expectedConn, resource.get().value());

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
