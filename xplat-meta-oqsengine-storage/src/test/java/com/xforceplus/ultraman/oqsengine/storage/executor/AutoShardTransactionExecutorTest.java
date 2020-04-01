package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.ArgumentMatcher;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.Mockito.*;

/**
 * AutoTransactionExecutor Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class AutoShardTransactionExecutorTest {

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

        AutoShardTransactionExecutor te = new AutoShardTransactionExecutor(tm, ConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource) throws SQLException {
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

        tm.create();

        AutoShardTransactionExecutor te = new AutoShardTransactionExecutor(tm, ConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource) throws SQLException {
                Connection conn = (Connection) resource.value();
                Assert.assertEquals(expectedConn, conn);

                return null;
            }
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource);
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
        currentT.join(new ConnectionTransactionResource(mockDataSource, expectedConn, false));

        AutoShardTransactionExecutor te = new AutoShardTransactionExecutor(tm, ConnectionTransactionResource.class);
        // 分片键不关心
        te.execute(new DataSourceShardingTask(dataSourceSelector, "") {
            @Override
            public Object run(TransactionResource resource) throws SQLException {
                Assert.assertEquals(currentT.query(mockDataSource).get(), resource);

                return null;
            }
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource);
        Assert.assertTrue(resource.isPresent());
        Assert.assertEquals(expectedConn, resource.get().value());

    }
}
