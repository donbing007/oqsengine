package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationInitException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的实例重算实现.
 */
public class DefaultCalculationInitImpl implements CalculationInit {
    private final Logger logger = LoggerFactory.getLogger(DefaultCalculationInitImpl.class);

    @Resource
    private InitIvalueFactory initIvalueFactory;

    @Resource
    private MetaManager metaManager;

    @Override
    public IEntity init(InitInstance initInstance) {
        List<InitInstanceUnit> initInstanceUnits = initInstance.getInitInstanceUnits();
        for (InitInstanceUnit initInstanceUnit : initInstanceUnits) {
            initUnit(initInstanceUnit);
        }
        return initInstance.getEntity();
    }

    @Override
    public List<IEntity> init(List<InitInstance> initInstances) {
        return initInstances.stream().map(this::init).collect(Collectors.toList());
    }

    /**
     * 最小重算单元执行.
     *
     * @param initInstanceUnit 最小执行单元 字段级别.
     * @return 重算后实例.
     */
    private IEntity initUnit(InitInstanceUnit initInstanceUnit) {
        IEntity entity = initInstanceUnit.getEntity();
        IEntityClass entityClass = initInstanceUnit.getEntityClass();
        IEntityField field = initInstanceUnit.getField();
        InitCalculationParticipant participant = build(entityClass, field, field.calculationType());
        InitIvalueLogic logic = initIvalueFactory.getLogic(field.calculationType());

        try {
            return logic.init(entity, participant);
        } catch (SQLException e) {
            throw new CalculationInitException(e);
        }

    }

    /**
     * 计算参与者构建.
     *
     * @param entityClass     当前对象
     * @param entityField     当前字段
     * @param calculationType 计算类型
     * @return 重算后实例.
     */
    private InitCalculationParticipant build(IEntityClass entityClass, IEntityField entityField,
                                             CalculationType calculationType) {
        InitCalculationParticipant.Builder builder =
            InitCalculationParticipant.Builder.anInitCalculationParticipant().withEntityClass(entityClass)
                .withField(entityField).withNeedInit(true);
        switch (calculationType) {
            case FORMULA:
                Formula formula = (Formula) entityField.config().getCalculation();
                if (formula.getArgs() == null || formula.getArgs().isEmpty()) {
                    return builder.build();
                }
                List<IEntityField> sourceFields = new ArrayList<>();
                formula.getArgs().forEach(s -> {
                    if (entityClass.field(s).isPresent()) {
                        sourceFields.add(entityClass.field(s).get());
                    }
                });
                return builder.withSourceFields(sourceFields).withSourceEntityClass(entityClass).build();
            case AGGREGATION:
                Aggregation aggregation = (Aggregation) entityField.config().getCalculation();
                Optional<IEntityClass> entityClassOptional = metaManager.load(aggregation.getClassId(), null);
                if (entityClassOptional.isPresent()) {
                    if (entityClassOptional.get().field(aggregation.getFieldId()).isPresent()) {
                        ArrayList<IEntityField> fields = new ArrayList<>();
                        fields.add(entityClassOptional.get().field(aggregation.getFieldId()).get());
                        return builder.withSourceEntityClass(entityClassOptional.get()).withSourceFields(fields)
                            .build();
                    } else if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                        return builder.withSourceEntityClass(entityClassOptional.get()).build();
                    } else {
                        logger.error(
                            String.format("can not find entityField %s in entityClass %s", aggregation.getFieldId(),
                                aggregation.getClassId()));
                        throw new CalculationException(String.format(
                            "init calculation error: can not find entityField %s in entityClass %s",
                            aggregation.getFieldId(), aggregation.getClassId()));
                    }
                } else {
                    logger.error(String.format("can not find entityClass %s", aggregation.getClassId()));
                    throw new CalculationException(String.format(
                        "init calculation error: can not find entityClass %s", aggregation.getAggregationType()));
                }
            case AUTO_FILL:
                AutoFill autoFill = (AutoFill) entityField.config().getCalculation();
                if (autoFill.getArgs() == null || autoFill.getArgs().isEmpty()) {
                    return builder.build();
                }
                List<IEntityField> fields = new ArrayList<>();
                autoFill.getArgs().forEach(s -> {
                    if (entityClass.field(s).isPresent()) {
                        fields.add(entityClass.field(s).get());
                    }
                });
                return builder.withSourceFields(fields).withSourceEntityClass(entityClass).build();
            default:
                throw new CalculationException(String.format(
                    "init calculation error: not support calculationType %s , can not transfer to InitCalculationParticipant",
                    calculationType.name()));

        }
    }

}
