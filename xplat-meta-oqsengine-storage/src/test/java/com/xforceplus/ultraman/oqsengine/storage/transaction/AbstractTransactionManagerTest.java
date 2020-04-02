package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.unbind();
        Assert.assertFalse(tm.getCurrent().isPresent());

        tm.rebind(tx.id());
        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.finish(tx);
        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertTrue(tx.isRollback());
        Assert.assertFalse(tx.isCommitted());
    }

    @Test
    public void testTimeout() throws Exception {
        MockTransactionManager tm = new MockTransactionManager(200);
        Transaction tx = tm.create();

        TimeUnit.MILLISECONDS.sleep(320);

        Assert.assertFalse(tm.getCurrent().isPresent());
        Assert.assertTrue(tx.isCompleted());
        Assert.assertTrue(tx.isRollback());
        Assert.assertFalse(tx.isCommitted());
    }

    @Test
    public void testCommitAfterFinish() throws Exception {
        MockTransactionManager tm = new MockTransactionManager();
        Transaction tx = tm.create();
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

    static class MockTransactionManager extends AbstractTransactionManager {

        private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        public MockTransactionManager() {
        }

        public MockTransactionManager(int survivalTimeMs) {
            super(survivalTimeMs);
        }

        @Override
        public Transaction doCreate() {

            long id = idGenerator.next();
            return new MultiLocalTransaction(id);

        }
    }

} 
