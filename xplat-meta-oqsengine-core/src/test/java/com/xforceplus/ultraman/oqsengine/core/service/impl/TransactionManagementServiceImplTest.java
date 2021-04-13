package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static org.mockito.Mockito.*;

/**
 * TransactionManagementServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 06/30/2020
 * @since <pre>Jun 30, 2020</pre>
 */
public class TransactionManagementServiceImplTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testBegin() throws Exception {
        Transaction t = mock(Transaction.class);
        when(t.id()).thenReturn(123L);

        TransactionManager tm = mock(TransactionManager.class);
        when(tm.create()).thenReturn(t);
        when(tm.create(null)).thenReturn(t);

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long id = impl.begin();
        Assert.assertEquals(123L, id);

        // 不应该 bind
        Assert.assertFalse(tm.getCurrent().isPresent());
    }

    @Test
    public void testBeginTimeout() throws Exception {
        Transaction t = mock(Transaction.class);
        when(t.id()).thenReturn(123L);

        TransactionManager tm = mock(TransactionManager.class);
        when(tm.create(1000L)).thenReturn(t);
        when(tm.create(1000L, null)).thenReturn(t);

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long id = impl.begin(1000L);
        Assert.assertEquals(123L, id);

        // 不应该 bind
        Assert.assertFalse(tm.getCurrent().isPresent());
    }

    @Test
    public void testRestore() throws Exception {
        TransactionManager tm = mock(TransactionManager.class);

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        impl.restore(123L);

        verify(tm, times(1)).bind(123L);
    }

    @Test
    public void testCommit() throws Exception {

        CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.save(0, true)).thenReturn(true);

        TransactionManager tm = DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withWaitCommitSync(false)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .build();

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long txId = impl.begin();
        impl.restore(txId);
        Transaction tx = tm.getCurrent().get();

        impl.restore(txId);
        impl.commit();

        Assert.assertTrue(tx.isCompleted());
        Assert.assertTrue(tx.isCommitted());
        Assert.assertFalse(tx.isRollback());
        Assert.assertEquals(0, tm.size());
    }

    @Test
    public void testRollback() throws Exception {
        CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.save(0, true)).thenReturn(true);
        TransactionManager tm = DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long txId = impl.begin();
        impl.restore(txId);
        Transaction tx = tm.getCurrent().get();

        impl.restore(txId);
        impl.rollback();

        Assert.assertTrue(tx.isCompleted());
        Assert.assertFalse(tx.isCommitted());
        Assert.assertTrue(tx.isRollback());

        Assert.assertEquals(0, tm.size());
    }

    @Test(expected = SQLException.class)
    public void testCompleted() throws Exception {
        CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.save(0, true)).thenReturn(true);
        TransactionManager tm = DefaultTransactionManager.Builder.aDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withCacheEventHandler(new DoNothingCacheEventHandler())
            .withWaitCommitSync(false)
            .build();

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        impl.commit();
    }

} 
