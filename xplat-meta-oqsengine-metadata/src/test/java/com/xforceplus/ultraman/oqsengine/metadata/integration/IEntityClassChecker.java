package com.xforceplus.ultraman.oqsengine.metadata.integration;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator.FailedPolicy.USE_FAILED_DEFAULT_VALUE;

import com.google.protobuf.Any;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ProtoAnyHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class IEntityClassChecker {

    public static void check(IEntityClass entityClass, List<EntityClassInfo> all) {
        Optional<EntityClassInfo> entityClassInfoOp = all.stream().filter(entityClassInfo -> {
            return entityClass.id() == entityClassInfo.getId();
        }).findFirst();

        Assert.assertTrue(entityClassInfoOp.isPresent());

        EntityClassInfo originExpected = entityClassInfoOp.get();

        Assert.assertEquals(originExpected.getId(), entityClass.id());
        Assert.assertEquals(originExpected.getName(), entityClass.name());
        Assert.assertEquals(originExpected.getCode(), entityClass.code());
        Assert.assertEquals(originExpected.getVersion(), entityClass.version());
        Assert.assertEquals(originExpected.getLevel(), entityClass.level());
        checkEntity(entityClass, originExpected);
        checkRelation(entityClass, originExpected);

        long fatherId = originExpected.getFather();
        Optional<IEntityClass> father = entityClass.father();
        if (father.isPresent()) {
            Assert.assertEquals(fatherId, father.get().id());
            check(father.get(), all);
        } else {
            Assert.assertEquals(0, fatherId);
        }
    }

    private static void checkRelation(IEntityClass entityClass, EntityClassInfo originExpected) {
        Collection<OqsRelation> opRelations = entityClass.oqsRelations();
        originExpected.getRelationsList().forEach(
            origin -> {
                Optional<OqsRelation> op = opRelations.stream()
                    .filter(relation -> {
                        return relation.getId() == origin.getId();
                    }).findFirst();
                Assert.assertTrue(op.isPresent());

                Assert.assertEquals(origin.getCode(), op.get().getCode());
                Assert.assertEquals(origin.getBelongToOwner(), op.get().isBelongToOwner());
                Assert.assertEquals(origin.getIdentity(), op.get().isIdentity());
                Assert.assertEquals(origin.getRelationType(), op.get().getRelationType().getValue());
                Assert.assertEquals(origin.getLeftEntityClassId(), op.get().getLeftEntityClassId());
                Assert.assertEquals(origin.getLeftEntityClassCode(), op.get().getLeftEntityClassCode());
                Assert.assertEquals(origin.getRightEntityClassId(), op.get().getRightEntityClassId());
                if (origin.getBelongToOwner()) {
                    checkOneFailed(op.get().getEntityField(), origin.getEntityField(), true);
                }
            }
        );
    }

    private static void checkEntity(IEntityClass entityClass, EntityClassInfo originExpected) {
        Collection<IEntityField> opEntityFields = entityClass.fields();
        originExpected.getEntityFieldsList().forEach(
            origin -> {
                Optional<IEntityField> op = opEntityFields.stream()
                    .filter(entityField -> {
                        return entityField.id() == origin.getId();
                    }).findFirst();
                Assert.assertTrue(op.isPresent());

                checkOneFailed(op.get(), origin, false);
            }
        );
    }

    private static void checkOneFailed(IEntityField entityField, EntityFieldInfo origin, boolean isRelationCheck) {
        Assert.assertEquals(origin.getFieldType().name().toLowerCase(), entityField.type().name().toLowerCase());

        //  check fieldConfig
        checkFieldConfig(entityField.config(), origin.getFieldConfig());
        if (!isRelationCheck) {
            Assert.assertEquals(origin.getCname(), entityField.cnName());
            Assert.assertEquals(origin.getDictId(), entityField.dictId());
            Assert.assertEquals(origin.getName(), entityField.name());
            Assert.assertEquals(origin.getDefaultValue(), entityField.defaultValue());

            //  check calculator
            checkCalculator(entityField.calculator(), origin.getCalculator());
        }
    }

    private static void checkCalculator(Calculator calculator,
                                        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator expected) {
        if (expected.isInitialized()) {
            Assert.assertNotNull(calculator);

            Assert.assertEquals(expected.getCalculateType(),  calculator.getType().getType());

            if (calculator.getType().equals(Calculator.Type.FORMULA)) {
                Assert.assertEquals(expected.getFailedPolicy(), calculator.getFailedPolicy().getPolicy());
                Assert.assertEquals(expected.getArgsCount(), calculator.getArgs().size());
                expected.getArgsList().forEach(
                    arg -> {
                        Assert.assertTrue(calculator.getArgs().contains(arg));
                    }
                );
                if (calculator.getFailedPolicy().equals(USE_FAILED_DEFAULT_VALUE)) {
                    Optional<Any> failedValueAny = ProtoAnyHelper.toAnyValue(calculator.getFailedDefaultValue());
                    Assert.assertTrue(failedValueAny.isPresent());
                    Assert
                        .assertEquals(expected.getFailedDefaultValue().getTypeUrl(), failedValueAny.get().getTypeUrl());
                    Assert.assertEquals(expected.getFailedDefaultValue().toString(), failedValueAny.get().toString());
                }
            }
        }
    }
    private static void checkFieldConfig(FieldConfig fieldConfig,
                                         com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig expected) {
        Assert.assertEquals(expected.getIsRequired(), fieldConfig.isRequired());
        Assert.assertEquals(expected.getIdentifier(), fieldConfig.isIdentifie());
        Assert.assertEquals(expected.getMetaFieldSenseValue(), fieldConfig.getFieldSense().getSymbol());
        Assert.assertEquals(expected.getUniqueName(), fieldConfig.getUniqueName());
        Assert.assertEquals(expected.getSearchable(), fieldConfig.isSearchable());
        Assert.assertEquals(expected.getCrossSearch(), fieldConfig.isCrossSearch());
    }
}
