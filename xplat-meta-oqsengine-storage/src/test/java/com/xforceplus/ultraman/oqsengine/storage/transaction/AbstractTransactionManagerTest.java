package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AbstractTransactionManager Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class AbstractTransactionManagerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
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
                String.format("Invalid transaction({}), transaction may have timed out.", tx.id()), ex.getMessage());
        }

        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertFalse(tx.isCommitted());
        Assert.assertTrue(tx.isRollback());
    }


    static class MockTransactionManager extends AbstractTransactionManager {

        private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        private long waitMs = 0;

        public MockTransactionManager() {
        }

        public MockTransactionManager(int survivalTimeMs) {
            this(survivalTimeMs, 0);
        }

        public MockTransactionManager(int survivalTimeMs, long waitMs) {
            super(survivalTimeMs);
            this.waitMs = waitMs;
        }

        @Override
        public Transaction doCreate() {

            long id = idGenerator.next();
            return new MockTransaction(id, waitMs);

        }
    }

    static class MockTransaction extends MultiLocalTransaction {

        private long waitMs = 0;
        private int commitNumber;
        private int rollbackNumber;

        public MockTransaction(long id, long watiMs) {
            super(id);
            this.waitMs = watiMs;
        }

        @Override
        public void commit() throws SQLException {
            commitNumber++;
            super.commit();
            sleep();
        }

        @Override
        public void rollback() throws SQLException {
            rollbackNumber++;
            super.rollback();
            sleep();
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
