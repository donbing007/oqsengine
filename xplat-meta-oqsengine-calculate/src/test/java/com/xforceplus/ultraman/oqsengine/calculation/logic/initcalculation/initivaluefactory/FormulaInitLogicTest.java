package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationAbstractParticipant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FormulaInitLogicTest {
    private FormulaInitLogic formulaInitLogic;
    private static IEntityClass B_CLASS;
    private static IEntityField B_FML;
    private static IEntityField B1;
    private IEntity entity;
    private InitCalculationAbstractParticipant participant;

   @BeforeEach
   public void before() {
       formulaInitLogic = new FormulaInitLogic();
       B1 = EntityField.Builder.anEntityField()
               .withId(Long.MAX_VALUE - 1)
               .withFieldType(FieldType.LONG)
               .withName("b1")
               .withConfig(
                       FieldConfig.Builder.anFieldConfig()
                               .withCalculation(Aggregation.Builder.anAggregation()
                                       .withClassId(Long.MAX_VALUE)
                                       .withFieldId(Long.MAX_VALUE)
                                       .withRelationId(Long.MAX_VALUE - 10)
                                       .withAggregationType(AggregationType.SUM)
                                       .build()
                               ).build()
               ).build();

       B_FML = EntityField.Builder.anEntityField()
               .withId(Long.MAX_VALUE - 11)
               .withFieldType(FieldType.LONG)
               .withName("b-fml-b-sum")
               .withConfig(
                       FieldConfig.Builder.anFieldConfig()
                               .withCalculation(Formula.Builder.anFormula()
                                       .withLevel(1)
                                       .withExpression("${b1} * 2")
                                       .withFailedDefaultValue(0)
                                       .withFailedPolicy(Formula.FailedPolicy.USE_FAILED_DEFAULT_VALUE)
                                       .withArgs(Collections.singletonList("b1"))
                                       .build()
                               ).build()
               ).build();

       B_CLASS = EntityClass.Builder.anEntityClass()
               .withId(Long.MAX_VALUE - 1)
               .withCode("b-class")
               .withFields(Arrays.asList(B1, B_FML, EntityField.ID_ENTITY_FIELD))
               .build();

       entity = Entity.Builder.anEntity().withId(10000).withEntityValue(EntityValue.build().addValues(Arrays.asList(
               new LongValue(B1, 10)
       ))).build();

       participant = InitCalculationAbstractParticipant.Builder.anParticipant().withField(B_FML).withEntityClass(B_CLASS).withSourceEntityClass(B_CLASS).withSourceField(Stream.of(B1).collect(Collectors.toList())).build();
   }
    @Test
    public void testInit() {
        IEntity init = formulaInitLogic.init(entity, participant);
        Assertions.assertEquals(20, (Long) init.entityValue().getValue(B_FML.id()).get().getValue());
        Assertions.assertEquals(20, (Long) entity.entityValue().getValue(B_FML.id()).get().getValue());
    }
}