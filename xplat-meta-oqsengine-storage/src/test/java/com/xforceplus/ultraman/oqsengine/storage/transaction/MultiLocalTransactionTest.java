package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.status.impl.CommitIdStatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.RedisEventHandler;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@RunWith(ContainerRunner.class)
@DependentContainers(ContainerType.REDIS)
public class MultiLocalTransactionTest {

    private RedisClient redisClient;
    private CommitIdStatusServiceImpl commitIdStatusService;
    private RedisEventHandler redisEventHandler;

    @Before
    public void before() throws Exception {
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());

        commitIdStatusService = new CommitIdStatusServiceImpl();
        ReflectionTestUtils.setField(commitIdStatusService, "redisClient", redisClient);
        commitIdStatusService.init();

        redisEventHandler = new RedisEventHandler(redisClient, new ObjectMapper(), 10);
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

        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .withMaxWaitCommitIdSyncMs(0)
                .build();

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.getAccumulator().accumulateBuild(Entity.Builder.anEntity().withId(1)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(1L).build()).build());

        tx.commit();

        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isCommitted());
        }
    }

    /**
     * 提交时应该进行等等同步.
     *
     * @throws Exception
     */
    @Test
    public void testCommitWaitSync() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .build();

        List<MockResource> resources = buildResources(10, false);

        for (MockResource resource : resources) {
            tx.join(resource);
        }

        tx.getAccumulator().accumulateReplace(Entity.Builder.anEntity().withId(1).withVersion(1)
                        .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(1L).build()).build(),
                Entity.Builder.anEntity().withId(1).withVersion(0)
                        .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(3L).build()).build());
        // 没有真实的操作,这里手动填入一个提交号.
        commitIdStatusService.save(1, true);

        CompletableFuture.runAsync(() -> {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
            commitIdStatusService.obsoleteAll();
        });
        tx.commit();

        Assert.assertTrue(tx.isWaitedSync());

        for (MockResource resource : resources) {
            Assert.assertTrue(resource.isCommitted());
        }
    }

    @Test
    public void testRollback() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .build();

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
        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .withMaxWaitCommitIdSyncMs(0)
                .build();

        List<MockResource> exResources = buildResources(2, true); // 这里提交会异常.
        List<MockResource> correctResources = buildResources(1, false); // 这里可以提交

        for (MockResource resource : exResources) {
            tx.join(resource);
        }

        for (MockResource resource : correctResources) {
            tx.join(resource);
        }

        tx.getAccumulator().accumulateDelete(Entity.Builder.anEntity().withId(3L)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(3L).build()).build());

        try {
            tx.commit();
            Assert.fail("No expected exception was thrown.");
        } catch (SQLException ex) {

        }

        for (MockResource r : exResources) {
            Assert.assertFalse(r.isCommitted());
            Assert.assertTrue(r.isDestroyed());
        }

        for (MockResource r : correctResources) {
            Assert.assertFalse(r.isRollback());
            Assert.assertTrue(r.isDestroyed());
        }

    }

    @Test
    public void testRollbackEx() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .build();

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

    @Test
    public void testIsReady() throws Exception {
        LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();
        MultiLocalTransaction tx = MultiLocalTransaction.Builder.aMultiLocalTransaction()
                .withId(1)
                .withLongIdGenerator(idGenerator)
                .withCommitIdStatusService(commitIdStatusService)
                .withCacheEventHandler(redisEventHandler)
                .build();

        tx.getAccumulator().accumulateDelete(Entity.Builder.anEntity().withId(8)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(8L).build()).build());
        Assert.assertFalse(tx.isReadyOnly());
        tx.getAccumulator().reset();

        tx.getAccumulator().accumulateBuild(Entity.Builder.anEntity().withId(9)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(9L).build()).build());
        Assert.assertFalse(tx.isReadyOnly());
        tx.getAccumulator().reset();

        tx.getAccumulator().accumulateReplace(Entity.Builder.anEntity().withId(10)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(10L).build()).build(),
                Entity.Builder.anEntity().withId(10)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(10L).build()).build());
        Assert.assertFalse(tx.isReadyOnly());
        tx.getAccumulator().reset();

        tx.getAccumulator().accumulateReplace(Entity.Builder.anEntity().withId(1)
                        .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(1L).build()).build(),
                Entity.Builder.anEntity().withId(1)
                        .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(1L).build()).build());
        tx.getAccumulator().accumulateBuild(Entity.Builder.anEntity().withId(2)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(2L).build()).build());
        tx.getAccumulator().accumulateDelete(Entity.Builder.anEntity().withId(3)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(3L).build()).build());
        tx.getAccumulator().accumulateDelete(Entity.Builder.anEntity().withId(4)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(4L).build()).build());
        tx.getAccumulator().accumulateDelete(Entity.Builder.anEntity().withId(5)
                .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(5L).build()).build());
        Assert.assertFalse(tx.isReadyOnly());
        tx.getAccumulator().reset();

        Assert.assertTrue(tx.isReadyOnly());
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
            return TransactionResourceType.MASTER;
        }

        @Override
        public boolean isDestroyed() throws SQLException {
            return this.destroyed;
        }

    }

}
