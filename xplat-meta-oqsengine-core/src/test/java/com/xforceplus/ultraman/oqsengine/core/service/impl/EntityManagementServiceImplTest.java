package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * EntityManagementServiceImpl Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/18/2021
 * @since <pre>Mar 18, 2021</pre>
 */
public class EntityManagementServiceImplTest {

    private LongIdGenerator idGenerator;
    private MetaManager metaManager;
    private TransactionExecutor transactionExecutor;

    private EntityManagementServiceImpl impl;

    @Before
    public void before() throws Exception {

        idGenerator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));
        metaManager = new MockMetaManager();
        transactionExecutor = new MockTransactionExecutor();

        impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(impl, "transactionExecutor", transactionExecutor);
        ReflectionTestUtils.setField(impl, "metaManager", metaManager);
        impl.init();
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testBuildSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.build(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        Assert.assertEquals(ResultStatus.SUCCESS, impl.build(targetEntity));
    }

    @Test
    public void testBuildFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.build(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        Assert.assertEquals(ResultStatus.UNCREATED, impl.build(targetEntity));
    }

    @Test
    public void testReplaceNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        when(masterStorage.selectOne(100, MockMetaManager.l2EntityClass)).thenReturn(Optional.empty());

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        Assert.assertEquals(ResultStatus.NOT_FOUND, impl.replace(targetEntity));
    }

    @Test
    public void testReplaceFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.selectOne(1, MockMetaManager.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.replace(actualTargetEntity, MockMetaManager.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.CONFLICT, impl.replace(replaceEntity));
    }

    @Test
    public void testReplaceSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.selectOne(1, MockMetaManager.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.replace(actualTargetEntity, MockMetaManager.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.SUCCESS, impl.replace(replaceEntity));
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(true);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        when(masterStorage.delete(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.SUCCESS, impl.delete(targetEntity));
    }

    @Test
    public void testDeleteFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(true);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        when(masterStorage.delete(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.CONFLICT, impl.delete(targetEntity));
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(false);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(MockMetaManager.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(MockMetaManager.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.NOT_FOUND, impl.delete(targetEntity));
    }

    static class MockTransactionExecutor implements TransactionExecutor {

        @Override
        public Object execute(ResourceTask storageTask) throws SQLException {
            return storageTask.run(null, new DefaultExecutorHint());
        }
    }

    static class MockMetaManager implements MetaManager {


        //-------------level 0--------------------
        static IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withLevel(0)
            .withCode("l0")
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE)
                .withFieldType(FieldType.LONG)
                .withName("l0-long")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 1)
                .withFieldType(FieldType.STRING)
                .withName("l0-string")
                .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 2)
                .withFieldType(FieldType.STRINGS)
                .withName("l0-strings")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .build();

        //-------------level 1--------------------
        static IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withLevel(1)
            .withCode("l1")
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 3)
                .withFieldType(FieldType.LONG)
                .withName("l1-long")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 4)
                .withFieldType(FieldType.STRING)
                .withName("l1-string")
                .withConfig(FieldConfig.Builder.aFieldConfig()
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build())
            .withFather(l0EntityClass)
            .build();

        //-------------level 2--------------------
        static IEntityClass l2EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withLevel(2)
            .withCode("l2")
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 5)
                .withFieldType(FieldType.STRING)
                .withName("l2-string")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 6)
                .withFieldType(FieldType.DATETIME)
                .withName("l2-time")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 7)
                .withFieldType(FieldType.ENUM)
                .withName("l2-enum")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 8)
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withFather(l1EntityClass)
            .build();

        private Collection<IEntityClass> entities;

        public MockMetaManager() {
            entities = Arrays.asList(
                l0EntityClass, l1EntityClass, l2EntityClass
            );
        }

        @Override
        public Optional<IEntityClass> load(long id) {
            return entities.stream().filter(e -> e.id() == id).findFirst();
        }

        @Override
        public IEntityClass loadHistory(long id, int version) {
            return null;
        }

        @Override
        public int need(String appId, String env) {
            return 0;
        }
    }

} 
