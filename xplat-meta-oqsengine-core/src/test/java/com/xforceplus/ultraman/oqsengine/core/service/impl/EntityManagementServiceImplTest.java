package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.impl.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EntityManagementServiceImpl Tester.
 *
 * @author dongbin
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
        ReflectionTestUtils.setField(impl, "eventBus", new EventBus() {
            @Override
            public void watch(EventType type, Consumer<Event> listener) {

            }

            @Override
            public void notify(Event event) {

            }
        });
        impl.init();
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testVerify() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()).build();

        try {
            impl.build(targetEntity);
            Assert.fail("The SQLException was expected to be thrown, but it was not.");
        } catch (SQLException ex) {
            Assert.assertEquals(String.format("Entity(%d-%s) does not have any attributes.",
                targetEntity.id(), targetEntity.entityClassRef().getCode()), ex.getMessage());
        }

        try {
            impl.replace(targetEntity);
            Assert.fail("The SQLException was expected to be thrown, but it was not.");
        } catch (SQLException ex) {
            Assert.assertEquals(String.format("Entity(%d-%s) does not have any attributes.",
                targetEntity.id(), targetEntity.entityClassRef().getCode()), ex.getMessage());
        }

        targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis()).build();
        try {
            impl.build(targetEntity);
            Assert.fail("The SQLException was expected to be thrown, but it was not.");
        } catch (SQLException ex) {
            Assert.assertEquals(String.format("Entity(%d-%s) does not have any attributes.",
                targetEntity.id(), targetEntity.entityClassRef().getCode()), ex.getMessage());
        }
        try {
            impl.replace(targetEntity);
            Assert.fail("The SQLException was expected to be thrown, but it was not.");
        } catch (SQLException ex) {
            Assert.assertEquals(String.format("Entity(%d-%s) does not have any attributes.",
                targetEntity.id(), targetEntity.entityClassRef().getCode()), ex.getMessage());
        }

    }

    @Test
    public void testBuildSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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

        Assert.assertEquals(ResultStatus.SUCCESS, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testBuildFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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

        Assert.assertEquals(ResultStatus.UNCREATED, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testReplaceNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        when(masterStorage.selectOne(100, MockMetaManager.l2EntityClass)).thenReturn(Optional.empty());

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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

        Assert.assertEquals(ResultStatus.NOT_FOUND, impl.replace(targetEntity).getResultStatus());
    }

    @Test
    public void testReplaceFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
        Assert.assertEquals(ResultStatus.CONFLICT, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testReplaceSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockMetaManager.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
        Assert.assertEquals(ResultStatus.SUCCESS, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);


        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
        when(masterStorage.delete(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.SUCCESS, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteForce() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        // 已经存在的
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withVersion(200)
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
        when(masterStorage.delete(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(1);
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        // 删除目标
        IEntity deletedEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
            .withId(1)
            .withVersion(200)
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

        Assert.assertEquals(ResultStatus.SUCCESS, impl.deleteForce(deletedEntity).getResultStatus());
        Assert.assertEquals(VersionHelp.OMNIPOTENCE_VERSION, targetEntity.version());

    }

    @Test
    public void testDeleteFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
        when(masterStorage.delete(targetEntity, MockMetaManager.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.CONFLICT, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(false);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(MockMetaManager.l2EntityClass.id(), MockMetaManager.l2EntityClass.code()))
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
        Assert.assertEquals(ResultStatus.NOT_FOUND, impl.delete(targetEntity).getResultStatus());
    }

    static class MockTransactionExecutor implements TransactionExecutor {

        @Override
        public Object execute(ResourceTask storageTask) throws SQLException {
            return storageTask.run(
                MultiLocalTransaction.Builder.anMultiLocalTransaction()
                    .withId(1)
                    .withCacheEventHandler(new DoNothingCacheEventHandler())
                    .withEventBus(
                        new EventBus() {
                            @Override
                            public void watch(EventType type, Consumer<Event> listener) {
                            }

                            @Override
                            public void notify(Event event) {
                            }
                        }
                    )
                    .build(),
                null,
                new DefaultExecutorHint());
        }
    }

} 
