package com.xforceplus.ultraman.oqsengine.storage.master.executor;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.resource.AbstractConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * .
 *
 * @author leo
 * @version 0.1 6/8/21 11:30 AM
 * @since 1.8
 */
public class UniqueExecutorTest {

    private static final String TABLE_NAME = "oqsunique";

    private TransactionResource<Connection> resource;

    private DataSource dataSource;

    private DataSourcePackage dataSourcePackage;

    private IEntityClass entityClass;


    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
    }

    @AfterClass
    public static void afterClass() {
        ContainerStarter.reset();
    }

    @Before
    public void before() throws SQLException {
        System.setProperty(
            "MYSQL_JDBC",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));
        dataSource = buildDataSource("./src/test/resources/sql_master_storage_build.conf");
        resource = new MockConnectionTransactionResource("", dataSource.getConnection(), true);

        FieldConfig config = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U1:2,test:IDX_U2:1").build();
        IEntityField f1 =
            EntityField.Builder.anEntityField().withId(100000).withName("f1").withFieldType(FieldType.STRINGS)
                .withConfig(config).build();
        List<IEntityField> fields = new ArrayList<>();
        fields.add(f1);
        entityClass = OqsEntityClass.Builder.anEntityClass().withCode("test").withFields(fields).withId(1008l).build();


    }

    @After
    public void after() {
        dataSourcePackage.close();
    }

    @Test
    public void testBuildExecutor() throws SQLException {

        StorageUniqueEntity entity =
            StorageUniqueEntity.builder().key("hello").entityClasses(new long[] {1008l}).build();
        BuildUniqueExecutor executor = (BuildUniqueExecutor) BuildUniqueExecutor.build(TABLE_NAME, resource, 30000);
        int ret = executor.execute(entity);
        Assert.assertEquals(1, ret);

        UpdateUniqueExecutor updateUniqueExecutor =
            (UpdateUniqueExecutor) UpdateUniqueExecutor.build(TABLE_NAME, resource, 30000);
        entity.setKey("hello-world");
        int ret2 = updateUniqueExecutor.execute(entity);
        Assert.assertEquals(1, ret2);


        QueryUniqueExecutor queryUniqueExecutor = new QueryUniqueExecutor(TABLE_NAME, resource, entityClass);
        Optional<StorageUniqueEntity> entityOptional = queryUniqueExecutor.execute("hello-world");
        Assert.assertEquals(true, entityOptional.isPresent());


        DeleteUniqueExecutor deleteUniqueExecutor =
            (DeleteUniqueExecutor) DeleteUniqueExecutor.build(TABLE_NAME, resource, 30000);
        int ret1 = deleteUniqueExecutor.execute(entity);


        Assert.assertEquals(1, ret1);
    }


    static class MockConnectionTransactionResource extends AbstractConnectionTransactionResource {

        public MockConnectionTransactionResource(String key, Connection value, boolean autoCommit) throws SQLException {
            super(key, value, autoCommit);
        }

        @Override
        public TransactionResourceType type() {
            return TransactionResourceType.MASTER;
        }
    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        dataSourcePackage = DataSourceFactory.build(true);
        return dataSourcePackage.getMaster().get(0);
    }
}
