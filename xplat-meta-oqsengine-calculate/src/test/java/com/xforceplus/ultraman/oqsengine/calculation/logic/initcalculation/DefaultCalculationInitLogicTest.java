package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.AggregationInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.AutoFillInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.FormulaInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
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
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultCalculationInitLogicTest {
    @InjectMocks
    private DefaultCalculationInitLogic defaultCalculationInitLogic;

    @Mock
    private MasterStorage masterStorage;

    @Mock
    private DataIterator<OriginalEntity> iterator;

    @Spy
    private ExecutorService worker;

    @Spy
    private InitIvalueFactory initIvalueFactory;

    private OriginalEntity originalEntity;

    private IEntity entity;
    private static IEntityClass B_CLASS;
    private static IEntityField B_FML;
    private static IEntityField B1;

    private InitCalculationParticipant participant;


    @BeforeEach
    public void before() throws SQLException {
        defaultCalculationInitLogic = new DefaultCalculationInitLogic();
        initIvalueFactory = new InitIvalueFactory();
        Map<CalculationType, InitIvalueLogic> initIvalueLogicMap = new HashMap<>();
        initIvalueLogicMap.put(CalculationType.FORMULA, new FormulaInitLogic());
        initIvalueLogicMap.put(CalculationType.AGGREGATION, new AggregationInitLogic());
        initIvalueLogicMap.put(CalculationType.AUTO_FILL, new AutoFillInitLogic());
        initIvalueFactory.setInitIvalueLogicMap(initIvalueLogicMap);

        worker = Executors.newFixedThreadPool(5);

        MockitoAnnotations.initMocks(this);
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

        entity = Entity.Builder.anEntity().withId(10000).withEntityClassRef(B_CLASS.ref()).withEntityValue(EntityValue.build().addValues(Arrays.asList(
                new LongValue(B1, 10)
        ))).build();

        participant = InitCalculationParticipant.Builder.anParticipant().withField(B_FML).withEntityClass(B_CLASS).withSourceEntityClass(B_CLASS).withSourceField(Stream.of(B1).collect(Collectors.toList())).build();

        originalEntity = new OriginalEntity();

        originalEntity.setId(10000);

        Mockito.when(masterStorage.iterator(Mockito.any(IEntityClass.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(iterator);

        Mockito.when(iterator.hasNext()).thenReturn(true, false);

        Mockito.when(iterator.next()).thenReturn(originalEntity);

        Mockito.when(masterStorage.selectOne(Mockito.anyLong(), Mockito.any(IEntityClass.class))).thenReturn(Optional.of(entity));

        Mockito.when(masterStorage.replace(Mockito.any(EntityPackage.class))).thenReturn(new int[] {1}, new int[] {0});

        Mockito.when(masterStorage.selectMultiple(Mockito.any())).thenReturn(Stream.of(entity).collect(Collectors.toList()));

        defaultCalculationInitLogic.init();

    }

    @Test
    public void testAccept() throws InterruptedException {
        ArrayList<Map<IEntityClass, Collection<InitCalculationParticipant>>> run = new ArrayList<>();
        Map<IEntityClass, Collection<InitCalculationParticipant>> map = new HashMap<>();
        IEntityClass c = EntityClass.Builder.anEntityClass().withId(B_CLASS.id()).build();
        map.put(B_CLASS, Stream.of(participant).collect(Collectors.toList()));
        map.put(c, Stream.of(participant).collect(Collectors.toList()));
        run.add(map);

        Map<String, List<InitCalculationParticipant>> accept = defaultCalculationInitLogic.accept(run);
        Assertions.assertFalse(accept.containsKey("false"));
    }

    @Test
    public void testInitLogic() {
        Tuple2<Boolean, List<InitCalculationParticipant>> tuple2 = defaultCalculationInitLogic.initLogic(B_CLASS, Stream.of(participant).collect(Collectors.toList()));
        Assertions.assertTrue(tuple2._1());
    }


}