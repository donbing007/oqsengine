package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * 聚合字段初始化测试.
 */
public class AggregationInitLogicTest {
    @InjectMocks
    private AggregationInitLogic aggregationInitLogic;

    @Mock
    private MasterStorage masterStorage;

    @Mock
    private CommitIdStatusService commitIdStatusService;

    @Mock
    private IndexStorage indexStorage;

    private IEntity entity;

    private InitCalculationParticipant participant;



    EntityField a1 = EntityField.Builder.anEntityField()
            .withFieldType(FieldType.LONG)
            .withId(101)
            .withName("A1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
            .withCalculation(StaticCalculation.Builder.anStaticCalculation().build()).build())
            .build();

    EntityField a2 = EntityField.Builder.anEntityField()
            .withFieldType(FieldType.LONG)
            .withId(102)
            .withName("relation")
            .withConfig(FieldConfig.Builder.anFieldConfig()
            .withIdentifie(true)
            .withCalculation(StaticCalculation.Builder.anStaticCalculation().build()).build())
            .build();


    private IEntityClass aclass = EntityClass.Builder.anEntityClass()
            .withId(1)
            .withField(a1)
            .withField(a2)
            .build();


    EntityField b1 = EntityField.Builder.anEntityField()
            .withFieldType(FieldType.LONG)
            .withId(201)
            .withName("B1")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(aclass.id()).withFieldId(a1.id())
                            .withRelationId(aclass.id())
                            .withConditions(Conditions.buildEmtpyConditions())
                            .withAggregationType(AggregationType.MAX).build())
                    .build())
            .build();

    EntityField b2 = EntityField.Builder.anEntityField()
            .withFieldType(FieldType.LONG)
            .withId(202)
            .withName("B2")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(aclass.id()).withFieldId(0)
                            .withRelationId(aclass.id())
                            .withConditions(Conditions.buildEmtpyConditions())
                            .withAggregationType(AggregationType.COUNT).build())
                    .build())
            .build();

    EntityField b3 = EntityField.Builder.anEntityField()
            .withFieldType(FieldType.LONG)
            .withId(203)
            .withName("B3")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withCalculation(StaticCalculation.Builder.anStaticCalculation().build())
                    .build())
            .build();

    private IEntityClass bclass = EntityClass.Builder.anEntityClass()
            .withId(2)
            .withField(b1)
            .withField(b2)
            .withField(b3)
            .withRelations(Stream.of(Relationship.Builder.anRelationship().withId(aclass.id()).withEntityField(a2).withId(
                    aclass.id()).build())
                    .collect(Collectors.toList())).build();


    @BeforeEach
    public void before() throws SQLException {
        MockitoAnnotations.initMocks(this);

        entity = Entity.Builder.anEntity() // 1002
                .withId(10000)
                .withTime(
                        LocalDateTime.of(
                                        2021, Month.FEBRUARY, 27, 12, 32, 20)
                                .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
                .withVersion(0)
                .withValues(Arrays.asList(
                        new LongValue(b3, 0)
                ))
                .withMajor(OqsVersion.MAJOR)
                .build();


        Collection<IEntity> masterEntitys =  new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            masterEntitys.add(Entity.Builder.anEntity() // 1002
                    .withId(20000 + i)
                    .withTime(
                            LocalDateTime.of(
                                            2021, Month.FEBRUARY, 27, 12, 32, 20)
                                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
                    .withVersion(0)
                    .withValues(Arrays.asList(
                            new LongValue(a1, i)
                    ))
                    .withMajor(OqsVersion.MAJOR)
                    .build());
        }

        Collection<IEntity> indexEntitys =  new ArrayList<>();
        for (int i = 100; i < 2000; i++) {
            indexEntitys.add(Entity.Builder.anEntity() // 1002
                    .withId(20000 + i)
                    .withTime(
                            LocalDateTime.of(
                                            2021, Month.FEBRUARY, 27, 12, 32, 20)
                                    .atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli())
                    .withVersion(0)
                    .withValues(Arrays.asList(
                            new LongValue(a1, i)
                    ))
                    .withMajor(OqsVersion.MAJOR)
                    .build());
        }

        Mockito.when(commitIdStatusService.getMin()).thenReturn(Optional.of(0L));

        List<EntityRef> masterRef = masterEntitys.stream().map(entity1 -> EntityRef.Builder.anEntityRef().withId(entity1.id()).build()).collect(Collectors.toList());
        Mockito.when(masterStorage.select(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(masterRef);

        List<EntityRef> indexRef = indexEntitys.stream().map(entity1 -> EntityRef.Builder.anEntityRef().withId(entity1.id()).build()).collect(Collectors.toList());
        Mockito.when(indexStorage.select(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(indexRef);

        Mockito.when(masterStorage.selectMultiple(Mockito.any(), Mockito.any())).thenReturn(Stream.of(masterEntitys, indexEntitys).flatMap(Collection::stream).collect(Collectors.toList()));

    }

    @Test
    public void testInitMax() throws SQLException {
        participant = InitCalculationParticipant.Builder.anInitCalculationParticipant().withField(b1).withEntityClass(
            bclass).withSourceEntityClass(
            aclass).withSourceFields(Stream.of(a1).collect(Collectors.toList())).build();
        IEntity init = aggregationInitLogic.init(entity, participant);
        Long value = (Long) init.entityValue().getValue(bclass.field(201).get().id()).get().getValue();
        Long value1 = (Long) entity.entityValue().getValue(bclass.field(201).get().id()).get().getValue();
        Assertions.assertEquals(1999L, value);
        Assertions.assertEquals(1999L, value1);
    }

    @Test
    public void testInitCount() throws SQLException {
        participant =  InitCalculationParticipant.Builder.anInitCalculationParticipant().withField(b2).withEntityClass(
            bclass).withSourceEntityClass(
            aclass).withSourceFields(Stream.of(EntityField.Builder.anEntityField().withId(0).build()).collect(Collectors.toList())).build();
        IEntity init = aggregationInitLogic.init(entity, participant);
        Long value = (Long) init.entityValue().getValue(bclass.field(202).get().id()).get().getValue();
        Long value1 = (Long) entity.entityValue().getValue(bclass.field(202 ).get().id()).get().getValue();
        Assertions.assertEquals(2000L, value);
        Assertions.assertEquals(2000L, value1);
    }
}