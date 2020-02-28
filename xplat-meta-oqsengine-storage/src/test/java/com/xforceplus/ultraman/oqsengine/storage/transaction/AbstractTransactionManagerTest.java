package com.xforceplus.ultraman.oqsengine.storage.transaction;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        tm.bind(tx);
        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.unbind();
        Assert.assertFalse(tm.getCurrent().isPresent());

        tm.rebind(tx.id());
        Assert.assertEquals(tx, tm.getCurrent().get());

        tm.finish(tx);
        Assert.assertFalse(tm.getCurrent().isPresent());

    }

    static class MockTransactionManager extends AbstractTransactionManager {

        private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        @Override
        public Transaction doCreate() {

            Transaction newTx = mock(Transaction.class);
            long id = idGenerator.next();
            when(newTx.id()).thenReturn(id);
            this.bind(newTx);

            return newTx;

        }
    }

} 
