package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.mock.StorageInitialization;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import io.lettuce.core.RedisClient;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * AbstractTransactionManager Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
@ExtendWith({RedisContainer.class})
public class TestAbstractTransactionManagerTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    /**
     * 测试初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        redisClient = CommonInitialization.getInstance().getRedisClient();

        commitIdStatusService = StorageInitialization.getInstance().getCommitIdStatusService();
    }

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void test() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        tm.bind(tx.id());

        Assertions.assertEquals(tx, tm.getCurrent().get());

        tm.unbind();
        Assertions.assertFalse(tm.getCurrent().isPresent());

        tm.bind(tx.id());
        Assertions.assertEquals(tx, tm.getCurrent().get());

        tm.finish(tx);
        Assertions.assertFalse(tm.getCurrent().isPresent());
        Assertions.assertTrue(tx.isCompleted());
        Assertions.assertTrue(tx.isRollback());
        Assertions.assertFalse(tx.isCommitted());
    }

    @Test
    public void testCommitAfterFinish() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
        tm.bind(tx.id());
        tx.commit();

        tm.finish();

        Assertions.assertFalse(tm.getCurrent().isPresent());
        Assertions.assertTrue(tx.isCompleted());
        Assertions.assertFalse(tx.isRollback());
        Assertions.assertTrue(tx.isCommitted());
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
        Assertions.assertEquals(3, tm.size());
    }

    @Test
    public void testFreeze() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            MockTransactionManager tm = new MockTransactionManager();
            Transaction tx = tm.create();
            Assertions.assertNotNull(tx);

            tm.freeze();
            tm.create();
        });
    }

    @Test
    public void testTimeoutTooSmall() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MockTransactionManager tm = new MockTransactionManager(100);
        });
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

        Assertions.assertTrue(tx.isCommitted());
        Assertions.assertFalse(tx.isRollback());
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
            Assertions.fail("An unbound exception is expected, but it is not.");
        } catch (RuntimeException ex) {
            Assertions.assertEquals(
                String.format("Invalid transaction(%s), transaction may have timed out.", tx.id()), ex.getMessage());
        }

        Assertions.assertFalse(tm.getCurrent().isPresent());
        Assertions.assertTrue(tx.isCompleted());
        Assertions.assertFalse(tx.isCommitted());
        Assertions.assertTrue(tx.isRollback());
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

        Assertions.assertEquals(0, tm.size());

        Field survivalField = AbstractTransactionManager.class.getDeclaredField("survival");
        survivalField.setAccessible(true);
        ConcurrentMap<Long, Transaction> survival = (ConcurrentMap<Long, Transaction>) survivalField.get(tm);
        Assertions.assertEquals(0, survival.size());

        Field usingField = AbstractTransactionManager.class.getDeclaredField("using");
        usingField.setAccessible(true);
        ConcurrentMap<Long, Transaction> using = (ConcurrentMap<Long, Transaction>) usingField.get(tm);
        Assertions.assertEquals(0, using.size());
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
        public Optional<TransactionResource> queryTransactionResource(String key) {
            return transaction.queryTransactionResource(key);
        }

        @Override
        public Collection<TransactionResource> listTransactionResource(TransactionResourceType type) {
            return transaction.listTransactionResource(type);
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
