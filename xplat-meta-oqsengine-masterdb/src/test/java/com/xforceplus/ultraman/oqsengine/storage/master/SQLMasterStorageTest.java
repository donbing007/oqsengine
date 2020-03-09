package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoShardTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.selector.SuffixNumberHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.TakeTurnsSelector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.sql.ConnectionTransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.BoolStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.LongStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * SQLMasterStorage Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/25/2020
 * @since <pre>Feb 25, 2020</pre>
 */
public class SQLMasterStorageTest {

    private TransactionManager transactionManager = new DefaultTransactionManager(
        new IncreasingOrderLongIdGenerator(0));

    private DataSourcePackage dataSourcePackage;
    private SQLMasterStorage storage;
    private List<IEntity> expectedEntitys;

    @Before
    public void before() throws Exception {

        Selector<String> tableNameSelector = buildTableNameSelector("oqsbigentity", 3);

        Selector<DataSource> dataSourceSelector = buildDataSourceSelector("./src/test/resources/sql_master_storage_build.conf");

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        TransactionExecutor executor = new AutoShardTransactionExecutor(
            transactionManager, ConnectionTransactionResource.class);


        StorageStrategyFactory storageStrategyFactory = StorageStrategyFactory.getDefaultFactory();

        storage = new SQLMasterStorage();
        ReflectionTestUtils.setField(storage, "dataSourceSelector", dataSourceSelector);
        ReflectionTestUtils.setField(storage, "tableNameSelector", tableNameSelector);
        ReflectionTestUtils.setField(storage, "transactionExecutor", executor);
        ReflectionTestUtils.setField(storage, "storageStrategyFactory", storageStrategyFactory);
        storage.init();

        transactionManager.create();
        expectedEntitys = initData(storage, 10);
    }

    @After
    public void after() throws Exception {

        transactionManager.finish();

        storage.destroy();

        dataSourcePackage.close();

    }

    /**
     * 测试写入并查询.
     * @throws Exception
     */
    @Test
    public void testBuildEntity() throws Exception {

        List<IEntity> queryEntitys = expectedEntitys.stream().map(e -> {
            try {
                Optional<IEntity> entity = storage.select(e.id(), e.entityClass());
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

        transactionManager.create();
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
    public void testDelete() throws Exception {

        storage.delete(expectedEntitys.stream().findAny().get());
        Map<Long, IEntityClass> idMap = expectedEntitys.stream().collect(toMap(IEntity::id, IEntity::entityClass));
        Collection<IEntity> queryEntitys = storage.selectMultiple(idMap);
        Assert.assertEquals(expectedEntitys.size() - 1, queryEntitys.size());

        Assert.assertEquals(0,
            queryEntitys.stream().filter(e -> !expectedEntitys.contains(e)).collect(Collectors.toList()).size());

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
        Collection<IEntityField> fields = buildRandomFields(baseId,3);
        return new Entity(
            baseId,
            new EntityClass(baseId, "test", fields),
            buildRandomValue(baseId, fields)
            );
    }

    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new Field(fieldId, "c" + fieldId,
                ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
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

    private Selector<DataSource> buildDataSourceSelector(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);

        dataSourcePackage = DataSourceFactory.build();

        return new TakeTurnsSelector<>(dataSourcePackage.getMaster());

    }

    private Selector<String> buildTableNameSelector(String base, int size) {
        return new SuffixNumberHashSelector(base, size);
    }


}