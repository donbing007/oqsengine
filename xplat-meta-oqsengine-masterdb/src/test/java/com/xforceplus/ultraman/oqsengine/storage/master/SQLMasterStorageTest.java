package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.HashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.SuffixNumberHashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoJoinTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.value.MasterDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.master.transaction.SqlConnectionTransactionResourceFactory;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.SQLJsonIEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SQLMasterStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
public class SQLMasterStorageTest extends AbstractMysqlTest {

    private TransactionManager transactionManager;

    private DataSource dataSource;
    private SQLMasterStorage storage;
    private List<IEntity> expectedEntitys;
    private IEntityField fixStringsField = new EntityField(100000, "strings", FieldType.STRINGS);
    private StringsValue fixStringsValue = new StringsValue(fixStringsField, "1,2,3,500002,测试".split(","));

    @Before
    public void before() throws Exception {

        DataSource ds = buildDataSource("./src/test/resources/sql_master_storage_build.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        long commitId = 0;
        StatusService statusService = mock(StatusService.class);
        when(statusService.getCommitId()).thenReturn(commitId++);

        transactionManager = new DefaultTransactionManager(new IncreasingOrderLongIdGenerator(0), statusService);

        TransactionExecutor executor = new AutoJoinTransactionExecutor(
            transactionManager, new SqlConnectionTransactionResourceFactory("oqsbigentity", statusService));


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();
        storageStrategyFactory.register(FieldType.DECIMAL, new MasterDecimalStorageStrategy());
        SQLJsonIEntityValueBuilder entityValueBuilder = new SQLJsonIEntityValueBuilder();
        ReflectionTestUtils.setField(entityValueBuilder, "storageStrategyFactory", storageStrategyFactory);

        storage = new SQLMasterStorage();
        ReflectionTestUtils.setField(storage, "masterDataSource", ds);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        ReflectionTestUtils.setField(storage, "entityValueBuilder", entityValueBuilder);
        storage.setTableName("oqsbigentity");
        storage.setQueryTimeout(100000000);
        storage.init();

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        expectedEntitys = initData(storage, 10);
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        Connection conn = dataSource.getConnection();
        Statement stat = conn.createStatement();
        stat.execute("truncate table oqsbigentity");
        stat.close();
        conn.close();
    }

    /**
     * 测试写入并查询.
     *
     * @throws Exception
     */
    @Test
    public void testBuildEntity() throws Exception {

        List<IEntity> queryEntitys = expectedEntitys.stream().map(e -> {
            try {
                Optional<IEntity> entity = storage.selectOne(e.id(), e.entityClass());
                if (entity.isPresent()) {
                    return entity.get();
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }).collect(Collectors.toList());

        Assert.assertEquals(expectedEntitys.size(), queryEntitys.size());


        Assert.assertEquals(0,
            queryEntitys.stream().filter(e -> !expectedEntitys.contains(e)).collect(Collectors.toList()).size());
    }


    @Test
    public void testSelectMultipleIdQuery() throws Exception {
        Map<Long, IEntityClass> idMap = expectedEntitys.stream().collect(toMap(IEntity::id, IEntity::entityClass));

        Collection<IEntity> queryEntitys = storage.selectMultiple(idMap);

        Assert.assertEquals(expectedEntitys.size(), queryEntitys.size());

        Assert.assertEquals(0,
            queryEntitys.stream().filter(e -> !expectedEntitys.contains(e)).collect(Collectors.toList()).size());
    }

    @Test
    public void testReplace() throws Exception {

        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        IEntity entity = expectedEntitys.get(0);
        entity.entityValue().remove(entity.entityClass().fields().stream().findAny().get());
        storage.replace(entity);

        entity = expectedEntitys.get(1);
        entity.entityValue().remove(entity.entityClass().fields().stream().findAny().get());
        storage.replace(entity);
        transactionManager.getCurrent().get().commit();
        transactionManager.finish();

        Map<Long, IEntityClass> idMap = expectedEntitys.stream().collect(toMap(IEntity::id, IEntity::entityClass));
        Collection<IEntity> queryEntitys = storage.selectMultiple(idMap);

        Assert.assertEquals(expectedEntitys.size(), queryEntitys.size());

        // 因为最后更新时间和版本变化,应该有两个不同.
        Assert.assertEquals(2,
            queryEntitys.stream().filter(e -> !expectedEntitys.contains(e)).collect(Collectors.toList()).size());

        // 忽略版本号和更新时间的比对.
        Assert.assertEquals(0,
            queryEntitys.stream().filter(e -> {

                return !(expectedEntitys.stream().filter(ee -> {
                    return e.id() == ee.id() &&
                        Objects.equals(e.entityClass(), ee.entityClass()) &&
                        Objects.equals(e.entityValue(), ee.entityValue()) &&
                        Objects.equals(e.family(), ee.family());
                }).collect(Collectors.toList()).size() > 0);

            }).collect(Collectors.toList()).size());

    }

    @Test
    public void testSync() throws Exception {
        IEntity expectedEntity = expectedEntitys.stream().findFirst().get();
        IEntity source = storage.selectOne(expectedEntity.id(), expectedEntity.entityClass()).get();
        Assert.assertEquals(0, source.version());

        storage.replace(source);
        source = storage.selectOne(expectedEntity.id(), expectedEntity.entityClass()).get();

        Assert.assertEquals(1, source.version());


        IEntity target = expectedEntitys.stream().skip(1).findFirst().get();
        target = storage.selectOne(target.id(), target.entityClass()).get();

        Assert.assertEquals(0, target.version());

        storage.synchronize(source.id(), target.id());
        target = storage.selectOne(target.id(), target.entityClass()).get();

        Assert.assertEquals(source.version(), target.version());

    }

    @Test
    public void testDelete() throws Exception {

        Map<Long, IEntityClass> idMap = expectedEntitys.stream().collect(toMap(IEntity::id, IEntity::entityClass));
        IEntity targetEntity = expectedEntitys.stream().findAny().get();
        storage.delete(targetEntity);
        Collection<IEntity> queryEntitys = storage.selectMultiple(idMap);
        Assert.assertEquals(expectedEntitys.size() - 1, queryEntitys.size());

        Assert.assertEquals(expectedEntitys.size() - 1, queryEntitys.size());
        queryEntitys.stream().forEach(e -> {
            Assert.assertNotEquals(targetEntity.id(), e.id());
        });
    }

    @Test
    public void testDeleteWithoutVersion() throws Exception {
        IEntity targetEntity = expectedEntitys.get(0);
        storage.replace(targetEntity);

        targetEntity = storage.selectOne(targetEntity.id(), targetEntity.entityClass()).get();
        Assert.assertEquals(1, targetEntity.version());

        targetEntity.resetVersion(VersionHelp.OMNIPOTENCE_VERSION);
        Assert.assertEquals(1, storage.delete(targetEntity));

        Assert.assertFalse(storage.selectOne(targetEntity.id(), targetEntity.entityClass()).isPresent());
    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        try {
            expectedEntitys.stream().forEach(e -> {
                try {
                    storage.build(e);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            transactionManager.getCurrent().get().rollback();
            throw ex;
        }

        //将事务正常提交,并从事务管理器中销毁事务.
        Transaction tx = transactionManager.getCurrent().get();
        tx.commit();
        transactionManager.finish();

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        Collection<IEntityField> fields = buildRandomFields(baseId, 3);
        fields.add(fixStringsField);

        IEntity entity = new Entity(
            baseId,
            new EntityClass(baseId, "test", fields),
            buildRandomValue(baseId, fields)
        );
        return entity;
    }

    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(
                new EntityField(
                    fieldId,
                    "c" + fieldId,
                    ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING,
                    FieldConfig.build().searchable(true)));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                case STRINGS:
                    return fixStringsValue;
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    private DataSource buildDataSource(String file) throws SQLException {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build();

        AtomicInteger index = new AtomicInteger(0);
        Map<String, DataSource> dsMap = dataSourcePackage.getMaster().stream().collect(Collectors.toMap(
            d -> "ds" + index.getAndIncrement(), d -> d));

        int dsSize = dsMap.size();

        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(
            "oqsbigentity", "ds${0..1}.oqsbigentity${0..2}");
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("id", new HashPreciseShardingAlgorithm()));
        tableRuleConfiguration.setTableShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("id", new SuffixNumberHashPreciseShardingAlgorithm()));


        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfiguration);

        Properties prop = new Properties();
        prop.put("sql.show", "true");
        dataSource = ShardingDataSourceFactory.createDataSource(dsMap, shardingRuleConfig, prop);
        return dataSource;
    }

}
