package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.storage.master.command.*;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.SphinxQLTransactionResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/22/2020 2:30 PM
 * 功能描述:
 * 修改历史:
 */
public class StorageCommandTest {

    private Selector<String> tableNameSelector;

    private DataSourcePackage dataSourcePackage;

    private StorageEntity storageEntity;

    private TransactionResource resource;

    @Before
    public void init() throws InterruptedException {
        tableNameSelector = buildTableNameSelector("oqsbigentity", 1);

        buildDataSourceSelector("./src/test/resources/sql_master_storage_build.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        List<DataSource> ds = dataSourcePackage.getMaster();
        try {
            resource = new SphinxQLTransactionResource("1", ds.get(0).getConnection(), true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long longValue = 30000L;
        storageEntity = new StorageEntity(
                longValue,
                longValue,
                0,
                longValue,
                longValue,
                false,
                null
        );

    }

    @Test
    public void storageCommandTest() {
        Long longValue = 30000L;

        StorageEntity storageEntity = new StorageEntity(
                longValue,
                longValue,
                0,
                longValue,
                longValue,
                false,
                null
        );

        List<DataSource> ds = dataSourcePackage.getMaster();

        //test BuildStorageCommandTest
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                new BuildStorageCommand(tableNameSelector).execute(resource, storageEntity);

                resource.commit();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                StorageEntity res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
                //verify
                Assert.assertTrue(res != null);
                Assert.assertTrue(res.getId() == longValue);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        //test ReplaceStorageCommand
        storageEntity.setAttribute("test");
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                new ReplaceStorageCommand(tableNameSelector).execute(resource, storageEntity);

                resource.commit();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                StorageEntity res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
                //verify
                Assert.assertTrue(res != null);
                Assert.assertTrue("test".equals(res.getAttribute()));
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        //test DeleteStorageCommand
        storageEntity.setVersion(1);
        ds.forEach(dataSource -> {
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                new DeleteStorageCommand(tableNameSelector).execute(resource, storageEntity);

                resource.commit();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
        ds.forEach(dataSource -> {
            StorageEntity res = null;
            try {
                TransactionResource resource = new ConnectionTransactionResource("1", dataSource.getConnection(), true);
                res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
            Assert.assertTrue(res == null);
        });
    }

    @Test
    public void storageCommandUndoTest(){
        try {
            //prepare
//            new DeleteStorageCommand(tableNameSelector).execute(resource, storageEntity);

            // test BuildStorageCommand
            //do
            new BuildStorageCommand(tableNameSelector).execute(resource, storageEntity);
            StorageEntity res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getId() == 30000L);
            //undo
            new BuildStorageCommand(tableNameSelector).executeUndo(resource, storageEntity);
            res = null;
            try {
                res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            } catch (SQLException e) {

            }
            //verify
            Assert.assertTrue(res == null);

            // test DeleteStorageCommand
            //prepare
            new BuildStorageCommand(tableNameSelector).execute(resource, storageEntity);
            //do
            new DeleteStorageCommand(tableNameSelector).execute(resource, storageEntity);
            res = null;
            try {
                res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            } catch (SQLException e) {

            }
            //verify
            Assert.assertTrue(res == null);
            //undo
            new DeleteStorageCommand(tableNameSelector).executeUndo(resource, storageEntity);
            res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getId() == 30000L);

            // test ReplaceStorageCommand
            // do
            long oriEntity = storageEntity.getEntity();
            storageEntity.setAttribute("123456");
            new ReplaceStorageCommand(tableNameSelector).execute(resource, storageEntity);
            res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue("123456".equals(res.getAttribute()));
            // undo
            storageEntity.setEntity(oriEntity);
            new ReplaceStorageCommand(tableNameSelector).executeUndo(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getEntity() == oriEntity);

            //test ReplaceVersionStorageCommand
            // do
            int oriVersion = storageEntity.getVersion();
            storageEntity.setVersion(12);
            new ReplaceStorageCommand(tableNameSelector).execute(resource, storageEntity);
            res = new SelectByIdStorageCommand(tableNameSelector).execute(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getVersion() == 12);
            // undo
            storageEntity.setVersion(oriVersion);
            new ReplaceStorageCommand(tableNameSelector).executeUndo(resource, storageEntity);
            //verify
            Assert.assertTrue(res != null);
            Assert.assertTrue(res.getVersion() == oriVersion);
        } catch (SQLException e) {

        }
    }

    private void buildDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        dataSourcePackage = DataSourceFactory.build();

    }

    private Selector<String> buildTableNameSelector(String base, int size) {
        return new SuffixNumberHashSelector(base, size);
    }
}
