package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * TransactionManagementServiceImpl Tester.
 *
 * @author <Authors name>
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
        Transaction t = mock(Transaction.class);
        when(t.isCompleted()).thenReturn(false);

        TransactionManager tm = mock(TransactionManager.class);
        when(tm.getCurrent()).thenReturn(Optional.of(t));

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        impl.commit();

        verify(t, times(1)).commit();
        verify(tm, times(1)).finish(t);
    }

    @Test
    public void testRollback() throws Exception {
        Transaction t = mock(Transaction.class);
        when(t.isCompleted()).thenReturn(false);

        TransactionManager tm = mock(TransactionManager.class);
        when(tm.getCurrent()).thenReturn(Optional.of(t));

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        impl.rollback();

        verify(t, times(1)).rollback();
        verify(tm, times(1)).finish(t);
    }

    @Test(expected = SQLException.class)
    public void testCompleted() throws Exception {
        Transaction t = mock(Transaction.class);
        when(t.isCompleted()).thenReturn(true);

        TransactionManager tm = mock(TransactionManager.class);
        when(tm.getCurrent()).thenReturn(Optional.of(t));

        TransactionManagementServiceImpl impl = new TransactionManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "transactionManager", tm);

        impl.commit();

    }

} 