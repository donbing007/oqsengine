package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.transaction;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.MANTICORE})
public class SphinxQLTransactionResourceTest {

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
                    statement.executeUpdate("truncate table oqsindex0");
                    statement.executeUpdate("truncate table oqsindex1");
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
            stat.executeUpdate("insert into oqsindex0 (id,entity,fullfields) values(1,100,'v1')");
        }
        sqtr.commit();
        sqtr.destroy();


        try (Connection conn1 = ds.getConnection()) {
            try (Statement statement = conn1.createStatement()) {
                ResultSet rs = statement.executeQuery("select count(*)  from oqsindex");
                rs.next();
                Assert.assertEquals(1, rs.getInt(1));
            }
        }
    }

    private void buildDataSourcePackage() {
        if (dataSourcePackage == null) {
            dataSourcePackage = DataSourceFactory.build(true);
        }

    }

} 
