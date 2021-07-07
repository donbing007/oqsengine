package com.xforceplus.ultraman.oqsengine.storage.master.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.Test;

/**
 * ConnectionTransactionResource Tester.
 *
 * @author dongbin
 * @version 1.0 11/11/2020
 * @since <pre>Nov 11, 2020</pre>
 */
public class SqlConnectionTransactionResourceTest {

    /**
     * Method: commit(long txId, long commitId).
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

        SqlConnectionTransactionResource resource = new SqlConnectionTransactionResource(
            "test", connection, true, "test");
        resource.bind(transaction);
        resource.commit(2);
        verify(ps).setLong(1, 2);
        verify(ps).setLong(2, 1);
        verify(ps).executeUpdate();
    }

} 
