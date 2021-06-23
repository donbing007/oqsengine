package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.common.version.VersionHelp;
import com.xforceplus.ultraman.oqsengine.core.service.impl.mock.EntityClassDefine;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.util.Optional;
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

    private EntityManagementServiceImpl impl;

    @Before
    public void before() throws Exception {
        impl = BaseInit.entityManagementService(EntityClassDefine.getMockMetaManager());

        impl.init();
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testPreview() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
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
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
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
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.build(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        Assert.assertEquals(ResultStatus.SUCCESS, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testBuildFail() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.build(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        Assert.assertEquals(ResultStatus.UNCREATED, impl.build(targetEntity).getResultStatus());
    }

    @Test
    public void testBuildFieldCheckFailure() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(EntityClassDefine.l2EntityClass.ref())
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l1-long").get(), 10000L)
                )
            )
            .build();

        Assert.assertEquals(ResultStatus.FIELD_TOO_LONG, impl.build(targetEntity).getResultStatus());


        targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(EntityClassDefine.mustEntityClass.ref())
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new StringValue(EntityClassDefine.mustEntityClass.field("not-must-field").get(), "test")
                )
            )
            .build();

        Assert.assertEquals(ResultStatus.FIELD_MUST, impl.build(targetEntity).getResultStatus());
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
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
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
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.replace(actualTargetEntity, EntityClassDefine.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.CONFLICT, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testReplaceSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));

        // 这是请求的.
        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L)))
            .build();
        // 这是实际被发送的
        IEntity actualTargetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(replaceEntity.time())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 20000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        when(masterStorage.replace(actualTargetEntity, EntityClassDefine.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.SUCCESS, impl.replace(replaceEntity).getResultStatus());
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);


        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));
        when(masterStorage.delete(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(1);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.SUCCESS, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteForce() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);

        // 已经存在的
        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withVersion(200)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));
        when(masterStorage.delete(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(1);
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        // 删除目标
        IEntity deletedEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withVersion(200)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
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
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();

        when(masterStorage.selectOne(1, EntityClassDefine.l2EntityClass)).thenReturn(Optional.of(targetEntity));
        when(masterStorage.delete(targetEntity, EntityClassDefine.l2EntityClass)).thenReturn(0);

        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.CONFLICT, impl.delete(targetEntity).getResultStatus());
    }

    @Test
    public void testDeleteNotFound() throws Exception {
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.exist(1)).thenReturn(false);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(EntityClassDefine.l2EntityClass.id(), EntityClassDefine.l2EntityClass.code()))
            .withId(1)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(EntityClassDefine.l2EntityClass.field("l0-long").get(), 10000L))
                .addValue(
                    new StringValue(EntityClassDefine.l2EntityClass.field("l1-string").get(), "l2value"))
                .addValue(
                    new EnumValue(EntityClassDefine.l2EntityClass.field("l2-enum").get(), "E")
                )
            )
            .build();
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        Assert.assertEquals(ResultStatus.NOT_FOUND, impl.delete(targetEntity).getResultStatus());
    }

} 
