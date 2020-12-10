package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.AbstractRedisContainerTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * MultiLocalTransaction Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class MultiLocalTransactionTest extends AbstractRedisContainerTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("status.redis.ip");
        int redisPort = Integer.parseInt(System.getProperty("status.redis.port"));
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
    public void testCommit() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        MultiLocalTransaction tx = new MultiLocalTransaction(1, idGenerator, commitIdStatusService, 0);

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.declareWriteTransaction();

        tx.commit();

        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isCommitted());
        }
    }

    @Test
    public void testCommitWaitSync() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        MultiLocalTransaction tx = new MultiLocalTransaction(1, idGenerator, commitIdStatusService);

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.declareWriteTransaction();

        CompletableFuture.runAsync(() -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
            commitIdStatusService.obsoleteAll();
        });
        tx.commit();

        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isCommitted());
        }
    }

    @Test
    public void testRollback() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = new MultiLocalTransaction(1, idGenerator, commitIdStatusService);

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.rollback();
        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isRollback());
        }
    }

    @Test
    public void testCommitEx() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = new MultiLocalTransaction(1, idGenerator, commitIdStatusService, 0);

        List<MockResource> exResources = buildResources(2, true); // 这里提交会异常.
        List<MockResource> correctResources = buildResources(1, false); // 这里可以提交

        for (MockResource resource : exResources) {
            tx.join(resource);
        }

        for (MockResource resource : correctResources) {
            tx.join(resource);
        }

        // 非只读事务.
        tx.declareWriteTransaction();

        try {
            tx.commit();
            Assert.fail("No expected exception was thrown.");
        } catch (SQLException ex) {

        }

        for (MockResource r : exResources) {
            Assert.assertFalse(r.isCommitted());
        }

        for (MockResource r : correctResources) {
            Assert.assertTrue(r.isRollback());
        }

    }

    @Test
    public void testRollbackEx() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = new MultiLocalTransaction(1, idGenerator, commitIdStatusService);

        List<MockResource> exResources = buildResources(2, true); // 这里提交会异常.
        List<MockResource> correctResources = buildResources(1, false); // 这里可以提交

        for (MockResource resource : exResources) {
            tx.join(resource);
        }

        for (MockResource resource : correctResources) {
            tx.join(resource);
        }

        try {
            tx.rollback();
            Assert.fail("No expected exception was thrown.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (MockResource r : exResources) {
            Assert.assertFalse(r.isRollback());
        }

        for (MockResource r : correctResources) {
            Assert.assertTrue(r.isRollback());
        }
    }

    private List<MockResource> buildResources(int size, boolean ex) {
        List<MockResource> resources = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            resources.add(new MockResource(Integer.toString(i), Integer.toString(i), ex));
        }

        return resources;
    }

    static class MockResource implements TransactionResource<String> {

        private String key;
        private String value;
        private boolean committed;
        private boolean rollback;
        private boolean destroyed;
        private boolean exception;


        public MockResource(String key, String value) {
            this(key, value, false);
        }

        public MockResource(String key, String value, boolean ex) {
            this.key = key;
            this.value = value;
            this.exception = ex;
        }


        @Override
        public String key() {
            return key;
        }

        @Override
        public String value() {
            return null;
        }

        @Override
        public void commit(long commitId) throws SQLException {
            if (exception) {
                throw new SQLException("Expected SQLException.");
            }
            this.committed = true;
        }

        @Override
        public void commit() throws SQLException {
            commit(-1);
        }

        @Override
        public void rollback() throws SQLException {
            if (exception) {
                throw new SQLException("Expected SQLException.");
            }

            this.rollback = true;
        }

        @Override
        public void destroy() throws SQLException {
            this.destroyed = true;
        }

        public boolean isCommitted() {
            return committed;
        }

        public boolean isRollback() {
            return rollback;
        }

        @Override
        public void bind(Transaction transaction) {

        }

        @Override
        public Optional<Transaction> getTransaction() {
            return Optional.empty();
        }

        @Override
        public TransactionResourceType type() {
            return TransactionResourceType.INDEX;
        }

        @Override
        public boolean isDestroyed() throws SQLException {
            return false;
        }

    }

}
