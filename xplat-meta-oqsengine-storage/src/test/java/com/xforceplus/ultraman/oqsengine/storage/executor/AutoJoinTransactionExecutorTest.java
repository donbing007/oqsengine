package com.xforceplus.ultraman.oqsengine.storage.executor;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.selector.NoSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.transaction.*;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.TransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.*;
import org.springframework.test.util.ReflectionTestUtils;

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
public class AutoJoinTransactionExecutorTest extends AbstractContainer {

    private LongIdGenerator idGenerator;
    private LongIdGenerator commitIdGenerator;
    private TransactionManager tm;
    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    @BeforeClass
    public static void beforeTestClass() {
        startRedis();
    }

    @Before
    public void before() throws Exception {

        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        idGenerator = new IncreasingOrderLongIdGenerator();
        commitIdGenerator = new IncreasingOrderLongIdGenerator();

        tm = new DefaultTransactionManager(idGenerator, commitIdGenerator, commitIdStatusService);
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

        commitIdStatusService.destroy();
        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
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

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm,
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((resource, hint) -> {

            Connection conn = (Connection) resource.value();
            Assert.assertEquals(expectedConn, conn);

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

        Transaction tx = tm.create();
        tm.bind(tx.id());

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm,
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((resource, hint) -> {
            Connection conn = (Connection) resource.value();
            Assert.assertEquals(expectedConn, conn);

            return null;
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource.toString() + ".table");
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

        AutoJoinTransactionExecutor te = new AutoJoinTransactionExecutor(tm,
            (TransactionResourceFactory<Connection>) (key, resource, autocommit) ->
                new MockConnectionTransactionResource(key, resource, autocommit), dataSourceSelector,
            new NoSelector<>("table"));
        // 分片键不关心
        te.execute((resource, hint) -> {
            Assert.assertEquals(currentT.query(mockDataSource.toString() + ".table").get(), resource);

            return null;
        });

        Optional<Transaction> t = tm.getCurrent();
        Assert.assertTrue(t.isPresent());

        Optional<TransactionResource> resource = t.get().query(mockDataSource.toString() + ".table");
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
