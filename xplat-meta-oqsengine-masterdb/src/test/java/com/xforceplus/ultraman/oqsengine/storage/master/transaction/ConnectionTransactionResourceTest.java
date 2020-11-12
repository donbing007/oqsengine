package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.Mockito.*;

/**
 * ConnectionTransactionResource Tester.
 *
 * @author <Authors name>
 * @version 1.0 11/11/2020
 * @since <pre>Nov 11, 2020</pre>
 */
public class ConnectionTransactionResourceTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: type()
     */
    @Test
    public void testType() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: commit(long txId, long commitId)
     */
    @Test
    public void testCommit() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1);

        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(
            "update test set " + FieldDefine.COMMITID + " = ? where " + FieldDefine.TX + " = ?")).thenReturn(ps);
        Transaction transaction = mock(Transaction.class);
        when(transaction.id()).thenReturn(1L);

        //TODO
        ConnectionTransactionResource resource = new ConnectionTransactionResource("test", connection, true, "test", null);
        resource.bind(transaction);
        resource.commit(2);
        verify(ps).setLong(1, 2);
        verify(ps).setLong(2, 1);
        verify(ps).executeUpdate();

    }

} 