package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AbstractTransactionManager Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
@RunWith(ContainerRunner.class)
@DependentContainers(ContainerType.REDIS)
public class TestAbstractTransactionManagerTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();
    }

    @After
    public void after() throws Exception {
        commitIdStatusService.destroy();

        redisClient.connect().sync().flushall();
        redisClient.shutdown();
        redisClient = null;
    }

    @Test
    public void test() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        tm.bind(tx.id());

        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.unbind();
        Assert.assertFalse(tm.getCurrent().isPresent());

        tm.bind(tx.id());
        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.finish(tx);
        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertTrue(tx.isRollback());
        Assert.assertFalse(tx.isCommitted());
    }

    @Test
    public void testCommitAfterFinish() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        tm.bind(tx.id());
        tx.commit();

        tm.finish();

        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertFalse(tx.isRollback());
        Assert.assertTrue(tx.isCommitted());
    }

    @Test
    public void testSize() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        ExecutorService worker = Executors.newFixedThreadPool(3);
        CountDownLatch startLatch = new CountDownLatch(3);

        worker.submit(() -> {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            tm.create();
        });
        worker.submit(() -> {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            tm.create();
        });
        worker.submit(() -> {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            tm.create();
        });
        startLatch.countDown();
        startLatch.countDown();
        startLatch.countDown();
        worker.shutdown();

        TimeUnit.SECONDS.sleep(1);
        Assert.assertEquals(3, tm.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testFreeze() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        Assert.assertNotNull(tx);

        tm.freeze();
        tm.create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeoutTooSmall() throws Exception {
        MockTransactionManager tm = new MockTransactionManager(100);
    }

    /**
     * 测试如果处于提交状态中,是不可以超时的.
     */
    @Test
    public void testCommitBlockTimeout() throws Exception {
        MockTransactionManager tm = new MockTransactionManager(201, 600);
        Transaction tx = tm.create();
        tm.bind(tx.id());
        tm.unbind();

        tm.bind(tx.id());
        tm.getCurrent().get().commit();
        tm.unbind();

        TimeUnit.SECONDS.sleep(2);

        Assert.assertTrue(tx.isCommitted());
        Assert.assertFalse(tx.isRollback());

    }

    @Test
    public void testTimeoutAfterCommit() throws Exception {
        MockTransactionManager tm = new MockTransactionManager(201);
        Transaction tx = tm.create();
        tm.bind(tx.id());
        tm.unbind();
        TimeUnit.SECONDS.sleep(1);

        try {
            tm.bind(tx.id());
            Assert.fail("An unbound exception is expected, but it is not.");
        } catch (RuntimeException ex) {
            Assert.assertEquals(
                String.format("Invalid transaction(%s), transaction may have timed out.", tx.id()), ex.getMessage());
        }

        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertFalse(tx.isCommitted());
        Assert.assertTrue(tx.isRollback());
    }

    @Test
    public void testClean() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        tm.bind(tx.id());
        tm.unbind();

        TimeUnit.SECONDS.sleep(1);

        tm.bind(tx.id());
        tx.commit();
        tm.finish(tx);

        Assert.assertEquals(0, tm.size());

        Field survivalField = AbstractTransactionManager.class.getDeclaredField("survival");
        survivalField.setAccessible(true);
        ConcurrentMap<Long, Transaction> survival = (ConcurrentMap<Long, Transaction>) survivalField.get(tm);
        Assert.assertEquals(0, survival.size());

        Field usingField = AbstractTransactionManager.class.getDeclaredField("using");
        usingField.setAccessible(true);
        ConcurrentMap<Long, Transaction> using = (ConcurrentMap<Long, Transaction>) usingField.get(tm);
        Assert.assertEquals(0, using.size());
    }


    static class MockTransactionManager extends AbstractTransactionManager {

        private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        private LongIdGenerator commitIdGenerator = new IncreasingOrderLongIdGenerator();
        private long waitMs = 0;
        private CommitIdStatusService commitIdStatusService;
        private DoNothingCacheEventHandler cacheEventHandler;

        public MockTransactionManager() {
            this(3000, 0);
        }

        public MockTransactionManager(int survivalTimeMs) {
            this(survivalTimeMs, 0);
        }

        public MockTransactionManager(int survivalTimeMs, long waitMs) {
            super(survivalTimeMs);
            this.waitMs = waitMs;
            this.cacheEventHandler = new DoNothingCacheEventHandler();
        }

        public void setCommitIdStatusService(CommitIdStatusService commitIdStatusService) {
            this.commitIdStatusService = commitIdStatusService;
        }

        @Override
        public Transaction doCreate(String msg) {

            long id = idGenerator.next();
            return new MockTransaction(id, waitMs, commitIdGenerator, this.commitIdStatusService, cacheEventHandler);

        }
    }

    static class MockTransaction implements Transaction {

        private MultiLocalTransaction transaction;
        private long waitMs = 0;
        private int commitNumber;
        private int rollbackNumber;

        public MockTransaction(
            long id, long watiMs, LongIdGenerator longIdGenerator, CommitIdStatusService commitIdStatusService,
            DoNothingCacheEventHandler cacheEventHandler) {
            this.waitMs = watiMs;

            transaction = MultiLocalTransaction.Builder.anMultiLocalTransaction()
                .withId(id)
                .withLongIdGenerator(longIdGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(cacheEventHandler)
                .build();
        }

        @Override
        public long id() {
            return transaction.id();
        }

        @Override
        public void commit() throws SQLException {
            commitNumber++;
            transaction.commit();
            sleep();
        }

        @Override
        public void rollback() throws SQLException {
            rollbackNumber++;
            transaction.rollback();
            sleep();
        }

        @Override
        public boolean isCommitted() {
            return transaction.isCommitted();
        }

        @Override
        public boolean isRollback() {
            return transaction.isRollback();
        }

        @Override
        public boolean isCompleted() {
            return transaction.isCompleted();
        }

        @Override
        public void join(TransactionResource transactionResource) throws SQLException {
            transaction.join(transactionResource);
        }

        @Override
        public Optional<TransactionResource> query(String key) {
            return transaction.query(key);
        }

        @Override
        public long attachment() {
            return transaction.attachment();
        }

        @Override
        public void attach(long id) {
            transaction.attach(id);
        }

        @Override
        public boolean isReadyOnly() {
            return transaction.isReadyOnly();
        }

        @Override
        public TransactionAccumulator getAccumulator() {
            return transaction.getAccumulator();
        }

        @Override
        public void exclusiveAction(TransactionExclusiveAction action) throws SQLException {
            transaction.exclusiveAction(action);
        }

        public int getCommitNumber() {
            return commitNumber;
        }

        public int getRollbackNumber() {
            return rollbackNumber;
        }

        private void sleep() throws SQLException {
            if (waitMs > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(waitMs);
                } catch (InterruptedException e) {
                    throw new SQLException(e.getMessage(), e);
                }
            }
        }


    }

} 