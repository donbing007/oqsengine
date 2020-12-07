package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.IncreasingOrderLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.executor.AutoCreateTransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.DefaultTransactionManager;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * EntityManagementServiceImpl Tester.
 *
 * @author dongbin
 * @version 1.0 03/12/2020
 * @since <pre>Mar 12, 2020</pre>
 */
public class EntityManagementServiceImplTest {

    private EntityManagementServiceImpl service;
    private LongIdGenerator idGenerator;

    private MasterStorage masterStorage;
    private IndexStorage indexStorage;

    private IEntityClass fatherEntityClass = new EntityClass(1, "father", Arrays.asList(
        new EntityField(1, "f1", FieldType.LONG, FieldConfig.build().searchable(true)),
        new EntityField(2, "f2", FieldType.STRING, FieldConfig.build().searchable(false)),
        new EntityField(3, "f3", FieldType.DECIMAL, FieldConfig.build().searchable(true))
    ));

    private IEntityClass childEntityClass = new EntityClass(
        2,
        "chlid",
        null,
        null,
        fatherEntityClass,
        Arrays.asList(
            new EntityField(4, "c1", FieldType.LONG, FieldConfig.build().searchable(true))
        )
    );

    private IEntity noExtendEntity;
    private IEntity fatherEntity;
    private IEntity childEntity;

    @Before
    public void before() throws Exception {

        idGenerator = new IncreasingOrderLongIdGenerator();
        noExtendEntity = new Entity(100, fatherEntityClass, new EntityValue(100).addValues(
            Arrays.asList(
                new LongValue(fatherEntityClass.field("f1").get(), 10000L),
                new StringValue(fatherEntityClass.field("f2").get(), "v1"),
                new DecimalValue(fatherEntityClass.field("f3").get(), new BigDecimal("123.456"))
            )
        ));

        fatherEntity = new Entity(1000, fatherEntityClass, new EntityValue(1000).addValues(
            Arrays.asList(
                new LongValue(fatherEntityClass.field("f1").get(), 10000L),
                new StringValue(fatherEntityClass.field("f2").get(), "v1"),
                new DecimalValue(fatherEntityClass.field("f3").get(), new BigDecimal("123.456"))
            )
        ), new EntityFamily(0, 2000), 0, OqsVersion.MAJOR);

        childEntity = new Entity(2000, childEntityClass, new EntityValue(2000).addValues(
            Arrays.asList(
                new LongValue(fatherEntityClass.field("f1").get(), 10000L),
                new StringValue(fatherEntityClass.field("f2").get(), "v1"),
                new DecimalValue(fatherEntityClass.field("f3").get(), new BigDecimal("123.456")),
                new LongValue(childEntityClass.field("c1").get(), 20000L)
            )
        ), new EntityFamily(1000, 0), 0, OqsVersion.MAJOR);


        TransactionManager tm = new DefaultTransactionManager(idGenerator, new IncreasingOrderLongIdGenerator(0), null);
        TransactionExecutor te = new AutoCreateTransactionExecutor(tm);

        masterStorage = mock(MasterStorage.class);
        indexStorage = mock(IndexStorage.class);

        CommitIdStatusService commitIdStatusService = mock(CommitIdStatusService.class);
        when(commitIdStatusService.getMin()).thenReturn(Optional.of(100L));

        service = new EntityManagementServiceImpl(true);
        ReflectionTestUtils.setField(service, "idGenerator", idGenerator);
        ReflectionTestUtils.setField(service, "transactionExecutor", te);
        ReflectionTestUtils.setField(service, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(service, "indexStorage", indexStorage);
        ReflectionTestUtils.setField(service, "commitIdStatusService", commitIdStatusService);

    }

    @After
    public void after() throws Exception {
    }

    /**
     * 测试删除没有继承关系.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNoExtend() throws Exception {

        when(masterStorage.delete(argThat(IEntityMatcher.buildIgnoreTime(noExtendEntity)))).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.delete(noExtendEntity));

        verify(masterStorage).delete(argThat(IEntityMatcher.buildIgnoreTime(noExtendEntity)));
        verify(indexStorage).delete(noExtendEntity.id());
    }

    /**
     * 删除一个父类.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteFather() throws Exception {

        IEntity anyEntity = new Entity(
            childEntity.id(),
            AnyEntityClass.getInstance(),
            new EntityValue(2000),
            fatherEntity.version(),
            OqsVersion.MAJOR
        );

        when(masterStorage.delete(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)))).thenReturn(1);
        when(indexStorage.delete(fatherEntity.id())).thenReturn(1);

        when(masterStorage.delete(argThat(IEntityMatcher.buildIgnoreTime(anyEntity)))).thenReturn(1);
        when(indexStorage.delete(anyEntity.id())).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.delete(fatherEntity));

        verify(masterStorage).delete(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)));
        verify(masterStorage).delete(argThat(IEntityMatcher.buildIgnoreTime(anyEntity)));
        verify(indexStorage).delete(fatherEntity.id());
        verify(indexStorage).delete(anyEntity.id());
    }

    /**
     * 测试删除一个子类.
     * @throws Exception
     */
    @Test
    public void testDeleteChild() throws Exception {
        when(masterStorage.delete(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)))).thenReturn(1);
        when(indexStorage.delete(fatherEntity.id())).thenReturn(1);
        when(masterStorage.delete(argThat(IEntityMatcher.buildIgnoreTime(childEntity)))).thenReturn(1);
        when(indexStorage.delete(childEntity.id())).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.delete(childEntity));

        verify(masterStorage).delete(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)));
        verify(masterStorage).delete(argThat(IEntityMatcher.buildIgnoreTime(childEntity)));
        verify(indexStorage).delete(fatherEntity.id());
        verify(indexStorage).delete(childEntity.id());
    }

    /**
     * 更新一个不存在的对象.
     *
     * @throws Exception
     */
    @Test(expected = SQLException.class)
    public void testReplaceNoExtendNotExist() throws Exception {
        when(masterStorage.selectOne(noExtendEntity.id(), noExtendEntity.entityClass())).thenReturn(Optional.empty());
        service.replace(noExtendEntity);
    }

    /**
     * 更新一个已经存在的非继承对象.
     */
    @Test
    public void testReplaceNoExtendExist() throws Exception {
        when(masterStorage.selectOne(
            noExtendEntity.id(), noExtendEntity.entityClass())).thenReturn(Optional.of(noExtendEntity));
        when(masterStorage.replace(argThat(IEntityMatcher.buildIgnoreTime(noExtendEntity)))).thenReturn(1);
        when(indexStorage.delete(noExtendEntity.id())).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.replace(noExtendEntity));

        verify(masterStorage).selectOne(noExtendEntity.id(), noExtendEntity.entityClass());
        verify(masterStorage).replace(argThat(IEntityMatcher.buildIgnoreTime(noExtendEntity)));
        verify(indexStorage).delete(noExtendEntity.id());
    }

    /**
     * 更新一个已经存在的父类.
     *
     * @throws Exception
     */
    @Test
    public void testReplaceFather() throws Exception {
        when(masterStorage.selectOne(fatherEntity.id(), fatherEntity.entityClass())).thenReturn(Optional.of(fatherEntity));
        when(masterStorage.replace(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)))).thenReturn(1);
        when(masterStorage.synchronize(fatherEntity.id(), fatherEntity.family().child())).thenReturn(1);
        when(indexStorage.delete(fatherEntity.id())).thenReturn(1);
        when(indexStorage.delete(fatherEntity.family().child())).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.replace(fatherEntity));

        verify(masterStorage).selectOne(fatherEntity.id(), fatherEntityClass);
        verify(masterStorage).replace(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)));
        verify(masterStorage).synchronize(fatherEntity.id(), fatherEntity.family().child());
        verify(indexStorage).delete(fatherEntity.id());
        verify(indexStorage).delete(fatherEntity.family().child());
    }

    @Test
    public void testReplaceChild() throws Exception {
        when(masterStorage.selectOne(childEntity.id(), childEntity.entityClass())).thenReturn(Optional.of(childEntity));
        when(masterStorage.replace(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)))).thenReturn(1);
        when(masterStorage.replace(argThat(IEntityMatcher.buildIgnoreTime(childEntity)))).thenReturn(1);

        Assert.assertEquals(ResultStatus.SUCCESS, service.replace(childEntity));

        verify(masterStorage).selectOne(childEntity.id(), childEntity.entityClass());
        verify(masterStorage).replace(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)));
        verify(masterStorage).replace(argThat(IEntityMatcher.buildIgnoreTime(childEntity)));
    }

    @Test
    public void testBuildNotExtend() throws Exception {
        noExtendEntity.resetId(0);
        when(masterStorage.build(argThat(IEntityMatcher.buildIgnoreIdAndTime(noExtendEntity)))).thenReturn(1);

        noExtendEntity = service.build(noExtendEntity);

        Assert.assertTrue(noExtendEntity.id() > 0);

        verify(masterStorage).build(argThat(IEntityMatcher.buildIgnoreIdAndTime(noExtendEntity)));
    }

    @Test
    public void testBuildChild() throws Exception {
        fatherEntity.resetId(2);
        childEntity.resetId(3);
        fatherEntity.resetFamily(new EntityFamily(0, 3));
        childEntity.resetFamily(new EntityFamily(2, 0));
        when(masterStorage.build(argThat(IEntityMatcher.buildIgnoreTime(childEntity)))).thenReturn(1);
        when(masterStorage.build(argThat(IEntityMatcher.buildIgnoreTime(fatherEntity)))).thenReturn(1);

        childEntity = service.build(childEntity);

        Assert.assertTrue(childEntity.id() > 0);

        verify(masterStorage).build(argThat(IEntityMatcher.buildIgnoreIdAndTime(childEntity)));
        verify(masterStorage).build(argThat(IEntityMatcher.buildIgnoreIdAndTime(fatherEntity)));
    }

    /**
     * 参数比较器.
     */
    static class IEntityMatcher implements ArgumentMatcher<IEntity> {

        private IEntity target;
        private boolean ignoreId;
        private boolean ignoreTime;

        public static ArgumentMatcher<IEntity> buildIgnoreTime(IEntity target) {
            return new IEntityMatcher(target, false, true);
        }

        public static ArgumentMatcher<IEntity> buildIgnoreIdAndTime(IEntity target) {
            return new IEntityMatcher(target, true, true);
        }

        public IEntityMatcher(IEntity target) {
            this.target = target;
        }

        public IEntityMatcher(IEntity target, boolean ignoreId, boolean ignoreTime) {
            this.target = target;
            this.ignoreId = ignoreId;
            this.ignoreTime = ignoreTime;
        }

        @Override
        public boolean matches(IEntity argument) {
            if (argument == null) {
                return target == null;
            }
            if (!ignoreId) {
                if (target.id() != argument.id()) {
                    return false;
                }
            } else {
                if (argument.id() <= 0) {
                    return false;
                }
            }
            if (!ignoreTime) {
                if (target.time() != argument.time()) {
                    return false;
                }
            }
            if (target.version() != argument.version()) {
                return false;
            }
            if (target.major() != argument.major()) {
                return false;
            }
            if (!target.entityClass().equals(argument.entityClass())) {
                return false;
            }
            if (!target.entityValue().equals(argument.entityValue())) {
                return false;
            }
            if (!target.family().equals(argument.family())) {
                return false;
            }
            return true;
        }
    }
} 
