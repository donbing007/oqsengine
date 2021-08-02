package com.xforceplus.ultraman.oqsengine.calculation.lookup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * lookup 字段计算逻辑.
 *
 * @author dongbin
 * @version 0.1 2021/08/02 15:49
 * @since 1.8
 */
public class LookupCalculationLogicTest {

    private IEntityField targetLongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("target-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField targetStringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("target-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass targetEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(targetLongField)
        .withField(targetStringField)
        .build();

    private IEntityField lookLongField = EntityField.Builder.anEntityField()
        .withId(1000)
        .withFieldType(FieldType.LONG)
        .withName("look-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField lookStringField = EntityField.Builder.anEntityField()
        .withId(1001)
        .withFieldType(FieldType.STRING)
        .withName("look-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField lookStringLookupField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.STRING)
        .withName("look-string-lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withClassId(targetEntityClass.id())
                        .withFieldId(targetStringField.id()).build()
                )
                .build()
        ).build();
    private IEntityClass lookupEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("lookupClass")
        .withField(lookLongField)
        .withField(lookStringField)
        .withField(lookStringLookupField)
        .build();


    @Test
    public void testCalculate() throws Exception {
        // 目标被lookup的实体.
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(1000)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new StringValue(targetStringField, "targetValue"),
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(100)
            .withEntityClassRef(lookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(lookStringLookupField, 1000),
                        new LongValue(lookLongField, 2000L)
                    )
                )
            ).build();

        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (lookStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.of(targetEntity));


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withBuild(true)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withEntity(lookupEntity).build();
        context.focusField(lookStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> actualValueOp = logic.calculate(context);

        Assertions.assertTrue(actualValueOp.isPresent());

        IValue actualValue = actualValueOp.get();
        Assertions.assertEquals("targetValue", actualValue.getValue());
        Assertions.assertEquals(StringValue.class, actualValue.getClass());
        Assertions.assertEquals(lookStringLookupField.id(), actualValue.getField().id());
    }

    /**
     * lookup 不存在的实体.
     * 应该不产生任何字段.
     */
    @Test
    public void testNoTarget() throws Exception {
        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(100)
            .withEntityClassRef(lookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(lookStringLookupField, 1000),
                        new LongValue(lookLongField, 2000L)
                    )
                )
            ).build();
        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (lookStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.empty());

        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withBuild(true)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withEntity(lookupEntity).build();
        context.focusField(lookStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();

        try {
            Optional<IValue> actualValueOp = logic.calculate(context);
            Assertions.fail("An exception should be thrown.");
        } catch (Exception ex) {
            // do nothing.
        }
    }

    /**
     * 有目标实体,但是目标实体没有具体字段属性.
     */
    @Test
    public void testHaveTargetNoFieldValue() throws Exception {
        // 目标被lookup的实体.
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(1000)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(100)
            .withEntityClassRef(lookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(lookStringLookupField, 1000),
                        new LongValue(lookLongField, 2000L)
                    )
                )
            ).build();

        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (lookStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.of(targetEntity));


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withBuild(true)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withEntity(lookupEntity).build();
        context.focusField(lookStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> actualValueOp = logic.calculate(context);
        Assertions.assertFalse(actualValueOp.isPresent());
    }
}