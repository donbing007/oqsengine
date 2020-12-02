package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.AbstractContainerTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SphinxQLTransactionResource Tester.
 *
 * @author <Authors name>
 * @version 1.0 12/02/2020
 * @since <pre>Dec 2, 2020</pre>
 */
public class SphinxQLTransactionResourceTest extends AbstractContainerTest {

    private DataSourcePackage dataSourcePackage;

    @Before
    public void before() throws Exception {
        buildDataSourcePackage();
    }

    @After
    public void after() throws Exception {
        for (DataSource ds : dataSourcePackage.getIndexWriter()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("truncate table oqsindex");
                }
            }
        }

        dataSourcePackage.close();
    }

    @Test
    public void testCommit() throws Exception {
        DataSource ds = dataSourcePackage.getIndexWriter().get(0);
        Connection conn = ds.getConnection();
        SphinxQLTransactionResource sqtr = new SphinxQLTransactionResource("test", conn, false);
        try (Statement stat = conn.createStatement()) {
            stat.executeUpdate("insert into oqsindex (id,entity,fullfields) values(1,100,'v1')");
            stat.executeUpdate("insert into oqsindex (id,entity,fullfields) values(2,100,'v2')");
        }
        sqtr.commit();
        sqtr.destroy();


        try (Connection conn1 = ds.getConnection()) {
            try (Statement statement = conn1.createStatement()) {
                ResultSet rs = statement.executeQuery("select count(*)  from oqsindex");
                rs.next();
                Assert.assertEquals(2, rs.getInt(1));
            }
        }
    }

    private void buildDataSourcePackage() {
        if (dataSourcePackage == null) {
            System.setProperty(DataSourceFactory.CONFIG_FILE, "./src/test/resources/sql_index_storage.conf");

            dataSourcePackage = DataSourceFactory.build();
        }

    }

} 
