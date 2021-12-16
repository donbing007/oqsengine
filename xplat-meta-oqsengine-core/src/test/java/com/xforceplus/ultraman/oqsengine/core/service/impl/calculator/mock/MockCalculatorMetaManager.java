package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.dto.metrics.MetaMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class MockCalculatorMetaManager implements MetaManager {

    //-------------level 0--------------------
    public static final IEntityClass L0_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("l0")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.LONG)
            .withName("longValue0")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(19)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        StaticCalculation.Builder.anStaticCalculation().build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1)
            .withFieldType(FieldType.LONG)
            .withName("longValue1")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(19)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        Formula.Builder.anFormula()
                            .withLevel(1)
                            .withExpression("${longValue0} * 3")
                            .withFailedDefaultValue(0)
                            .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                            .withArgs(Collections.singletonList("longValue0"))
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 2)
            .withFieldType(FieldType.LONG)
            .withName("longValue2")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(19)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        Formula.Builder.anFormula()
                            .withLevel(2)
                            .withExpression("${longValue1} / 2")
                            .withFailedDefaultValue(1)
                            .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                            .withArgs(Collections.singletonList("longValue1"))
                            .build()
                    ).build()
            ).build()
        )
        .build();

    //-------------level 1--------------------
    public static IEntityClass L1_ENTITY_CLASS = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("l1")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 3)
            .withFieldType(FieldType.DATETIME)
            .withName("dateValue0")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(40)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        Formula.Builder.anFormula()
                            .withLevel(1)
                            .withExpression("now()")
                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 4)
            .withFieldType(FieldType.STRING)
            .withName("stringAutoFill")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(256)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        AutoFill.Builder.anAutoFill()
                            .withLevel(1)
                            .withDomainNoType(AutoFill.DomainNoType.NORMAL)
                            .withPatten("{0000}")
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 5)
            .withFieldType(FieldType.STRING)
            .withName("stringValueMix")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(256)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        Formula.Builder.anFormula()
                            .withLevel(2)
                            .withExpression("string.join(seq.list(${longValue0}, ${stringAutoFill}), '-')")
                            .withFailedDefaultValue("0")
                            .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                            .withArgs(Arrays.asList("longValue0", "stringAutoFill"))
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 6)
            .withFieldType(FieldType.STRING)
            .withName("senior autoFill")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(256)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        AutoFill.Builder.anAutoFill()
                            .withLevel(3)
                            .withExpression("stringValueMix+\":\"+getId(\"{0000}\",stringValueMix)")
                            .withDomainNoType(AutoFill.DomainNoType.SENIOR)
                            .withArgs(Collections.singletonList("stringValueMix"))
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 7)
            .withFieldType(FieldType.DATETIME)
            .withName("offset data")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(256)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        Formula.Builder.anFormula()
                            .withLevel(1)
                            .withExpression("timeOffset(createTime,1,1)")
                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                            .withArgs(Arrays.asList("createTime"))
                            .build()
                    ).build()
            ).build()
        )
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 8)
            .withFieldType(FieldType.DATETIME)
            .withName("createTime")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(19)
                    .withSearchable(true)
                    .withRequired(true)
                    .withCalculation(
                        StaticCalculation.Builder.anStaticCalculation().build()
                    ).build()
            ).build()
        )
        .withFather(L0_ENTITY_CLASS).build();

    private final Collection<IEntityClass> entities;

    public MockCalculatorMetaManager() {
        entities = Arrays.asList(
            L0_ENTITY_CLASS, L1_ENTITY_CLASS
        );
    }

    public Optional<IEntityClass> load(long id) {
        return entities.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public Optional<IEntityClass> load(long id, String profile) {
        return entities.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public Optional<IEntityClass> load(long entityClassId, int version, String profile) {
        return Optional.empty();
    }

    @Override
    public Collection<IEntityClass> familyLoad(long entityClassId) {
        return null;
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public void invalidateLocal() {

    }

    @Override
    public boolean metaImport(String appId, String env, int version, String content) {
        return true;
    }

    @Override
    public Optional<MetaMetrics> showMeta(String appId) throws Exception {
        return Optional.empty();
    }

    @Override
    public int reset(String appId, String env) {
        return 0;
    }
}
