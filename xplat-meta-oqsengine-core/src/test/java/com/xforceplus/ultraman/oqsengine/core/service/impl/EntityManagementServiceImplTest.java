package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImpl;
import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.service.impl.mock.EntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.event.DoNothingEventBus;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.DefaultExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.MultiLocalTransaction;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * EntityManagementServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 03/18/2021
 * @since <pre>Mar 18, 2021</pre>
 */
@ExtendWith({RedisContainer.class})
public class EntityManagementServiceImplTest {

    private EntityManagementServiceImpl impl;

    /**
     * 每个测试初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        impl = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(impl, "longContinuousPartialOrderIdGenerator",
            new IncreasingOrderLongIdGenerator());
        ReflectionTestUtils.setField(impl, "longNoContinuousPartialOrderIdGenerator",
            new IncreasingOrderLongIdGenerator());
        ReflectionTestUtils.setField(impl, "transactionExecutor", new MockTransactionExecutor());
        ReflectionTestUtils.setField(impl, "metaManager", EntityClassDefine.getMockMetaManager());
        ReflectionTestUtils.setField(impl, "calculation", new DefaultCalculationImpl());
        ReflectionTestUtils.setField(impl, "resourceLocker", new LocalResourceLocker());
        ReflectionTestUtils.setField(impl, "eventBus", DoNothingEventBus.getInstance());
        ReflectionTestUtils.setField(impl, "calculationLogicFactory", new CalculationLogicFactory());
        impl.init();
    }

    @AfterEach
    public void after() throws Exception {
    }

    @Test
    public void testPreview() throws Exception {
        EntityClassRef ref = EntityClassRef.Builder.anEntityClassRef()
            .withEntityClassId(0).withEntityClassCode("test").build();
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(ref)
            .build();

        OqsResult result = impl.build(targetEntity);
        Assertions.assertEquals(OqsResult.notExistMeta(ref), result);

        result = impl.replace(targetEntity);
        Assertions.assertEquals(OqsResult.notExistMeta(ref), result);

        result = impl.delete(targetEntity);
        Assertions.assertEquals(OqsResult.notExistMeta(ref), result);
    }

    @Test
    public void testBuildBatch() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity[] targetEntities = new IEntity[1000];
        EntityPackage expectedEntityPackage = new EntityPackage();
        for (int i = 0; i < 1000; i++) {

            targetEntities[i] = Entity.Builder.anEntity()
                .withEntityClassRef(
                    new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
                .withId(i + 1)
                .withTime(System.currentTimeMillis())
                .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), i),
                        new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                        new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E"))
                    .collect(Collectors.toList())
                )
                .build();

            expectedEntityPackage.put(targetEntities[i], EntityClassDefine.l2EntityClass);
        }


        doAnswer(invocation -> {
            EntityPackage entityPackage = invocation.getArgument(0);
            entityPackage.stream().forEach(ek -> ek.getKey().neat());
            return null;
        }).when(masterStorage).build(any(EntityPackage.class));

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        OqsResult<IEntity[]> results = impl.build(targetEntities);
        Assertions.assertEquals(ResultStatus.SUCCESS, results.getResultStatus());
        Assertions.assertEquals(targetEntities.length, results.getValue().get().length);

        verify(masterStorage, times(1)).build(expectedEntityPackage);
    }

    @Test
    public void testBuildSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            )
            .build();
        when(masterStorage.build(targetEntity, EntityClassDefine.l2EntityClass)).thenAnswer(inv -> {

            IEntity entity = inv.getArgument(0);
            entity.neat();

            return true;
        });

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        OqsResult<IEntity> result = impl.build(targetEntity);
        Assertions.assertEquals(ResultStatus.SUCCESS, result.getResultStatus());
        Assertions.assertEquals(targetEntity.id(), result.getValue().get().id());
    }

    @Test
    public void testBuildFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            )
            .build();
        when(masterStorage.build(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(false);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        Assertions.assertEquals(ResultStatus.UNCREATED, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testBuildFieldCheckFailure() throws Exception {
        // 超出长度.
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(EntityClassDefine.l2EntityClass.ref())
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Arrays.asList(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l1-long").get(), 1000L)
                )
            )
            .build();

        Assertions.assertEquals(ResultStatus.FIELD_TOO_LONG, impl.build(targetEntity).getResultStatus());


        // 必须字段没有设置.
        targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(EntityClassDefine.mustEntityClass.ref())
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Arrays.asList(
                    new StringValue(EntityClassDefine.mustEntityClass.field("not-must-field").get(), "test")
                )
            )
            .build();

        Assertions.assertEquals(ResultStatus.FIELD_MUST, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testReplaceNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        when(masterStorage.selectOne(100, EntityClassDefine.l2EntityClass)).thenReturn(Optional.empty());

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();

        Assertions.assertEquals(ResultStatus.NOT_FOUND, impl.replace(targetEntity).getResultStatus());
    }

    @Test
    public void testReplaceFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            )
            .build();
        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Arrays.asList(
                new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();
        when(masterStorage.replace(actualTargetEntity, EntityClassDefine.l2EntityClass)).thenReturn(false);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assertions.assertEquals(ResultStatus.UNREPLACE, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testReplaceSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();
        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Arrays.asList(
                new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();
        when(masterStorage.replace(actualTargetEntity, EntityClassDefine.l2EntityClass)).thenAnswer(inv -> {
            IEntity entity = inv.getArgument(0);
            entity.neat();
            return true;
        });

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assertions.assertEquals(ResultStatus.SUCCESS, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);


        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            )
            .build();

        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));
        when(masterStorage.delete(targetEntity, EntityClassDefine.l2EntityClass)).thenAnswer(inv -> {
            IEntity entity = inv.getArgument(0);
            entity.delete();
            return true;
        });

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assertions.assertEquals(ResultStatus.SUCCESS, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();

        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));
        when(masterStorage.delete(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(false);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assertions.assertEquals(ResultStatus.UNDELETED, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(-1);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withValues(Stream.of(new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L),
                new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"),
                new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")).collect(Collectors.toList())
            ).build();
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assertions.assertEquals(ResultStatus.NOT_FOUND, impl.delete(targetEntity).getResultStatus());
    }

    static class MockTransactionExecutor implements TransactionExecutor {

        private LongIdGenerator idGenerator = new IncreasingOrderLongIdGenerator();

        @Override
        public Object execute(ResourceTask storageTask) throws SQLException {
            try {
                return storageTask.run(
                    MultiLocalTransaction.Builder.anMultiLocalTransaction()
                        .withId(1)
                        .withLongIdGenerator(idGenerator)
                        .withEventBus(DoNothingEventBus.getInstance()).build(),
                    null,
                    new DefaultExecutorHint()
                );
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }
    }
} 
