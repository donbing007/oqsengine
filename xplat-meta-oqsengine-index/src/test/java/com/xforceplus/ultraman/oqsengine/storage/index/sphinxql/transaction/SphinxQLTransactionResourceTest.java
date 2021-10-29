package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.common.mock.CommonInitialization;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * SphinxQLTransactionResource Tester.
 *
 * @author dongbin
 * @version 1.0 12/02/2020
 * @since <pre>Dec 2, 2020</pre>
 */
@ExtendWith({ManticoreContainer.class})
public class SphinxQLTransactionResourceTest {

    @AfterEach
    public void after() throws Exception {
        InitializationHelper.clearAll();
        InitializationHelper.destroy();
    }

    @Test
    public void testCommit() throws Exception {
        DataSource ds = CommonInitialization.getInstance().getDataSourcePackage(true).getIndexWriter().get(0);
        Connection conn = ds.getConnection();
        SphinxQLTransactionResource sqtr = new SphinxQLTransactionResource("test", conn, false);
        try (Statement stat = conn.createStatement()) {
            stat.executeUpdate(String.format("insert into oqsindex (%s,%s,%s) values(1,'100','v1')",
                FieldDefine.ID, FieldDefine.ENTITYCLASSF, FieldDefine.ATTRIBUTEF));
        }
        sqtr.commit();
        sqtr.destroy();


        try (Connection conn1 = ds.getConnection()) {
            try (Statement statement = conn1.createStatement()) {
                ResultSet rs = statement.executeQuery("select count(*) from oqsindex");
                rs.next();
                Assertions.assertEquals(1, rs.getInt(1));
            }
        }
    }
} 
