package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * TransactionManagementServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 06/30/2020
 * @since <pre>Jun 30, 2020</pre>
 */
public class TransactionManagementServiceImplTest {

    @BeforeEach
    public void before() throws Exception {
    }

    @AfterEach
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
        Assertions.assertEquals(123L, id);

        // 不应该 bind
        Assertions.assertFalse(tm.getCurrent().isPresent());
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
        Assertions.assertEquals(123L, id);

        // 不应该 bind
        Assertions.assertFalse(tm.getCurrent().isPresent());
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

        TransactionManager tm = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withWaitCommitSync(false)
            .build();

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long txId = impl.begin();
        impl.restore(txId);
        Transaction tx = tm.getCurrent().get();

        impl.restore(txId);
        impl.commit();

        Assertions.assertTrue(tx.isCompleted());
        Assertions.assertTrue(tx.isCommitted());
        Assertions.assertFalse(tx.isRollback());
        Assertions.assertEquals(0, tm.size());
    }

    @Test
    public void testRollback() throws Exception {
        CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.save(0, true)).thenReturn(true);
        TransactionManager tm = DefaultTransactionManager.Builder.anDefaultTransactionManager()
            .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
            .withCommitIdStatusService(commitIdStatusService)
            .withWaitCommitSync(false)
            .build();

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        long txId = impl.begin();
        impl.restore(txId);
        Transaction tx = tm.getCurrent().get();

        impl.restore(txId);
        impl.rollback();

        Assertions.assertTrue(tx.isCompleted());
        Assertions.assertFalse(tx.isCommitted());
        Assertions.assertTrue(tx.isRollback());

        Assertions.assertEquals(0, tm.size());
    }

    @Test
    public void testCompleted() throws Exception {
        assertThrows(SQLException.class, () -> {
            CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
            when(commitIdStatusService.save(0, true)).thenReturn(true);
            TransactionManager tm = DefaultTransactionManager.Builder.anDefaultTransactionManager()
                .withTxIdGenerator(new IncreasingOrderLongIdGenerator(0))
                .withCommitIdGenerator(new IncreasingOrderLongIdGenerator(0))
                .withCommitIdStatusService(commitIdStatusService)
                .withWaitCommitSync(false)
                .build();

            TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
            ReflectionTestUtils.setField(impl, "transactionManager", tm);

            impl.commit();
        });
    }

} 
