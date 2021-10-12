package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚合字段计算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class AggregationCalculationLogic implements CalculationLogic {
    @Resource
    private AggregationFunctionFactory aggregationFunctionFactory;

    final Logger logger = LoggerFactory.getLogger(AggregationCalculationLogic.class);

//    @Override
//    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationException {
//        IEntity entity = context.getEntity();
//        IEntityField targetField = context.getFocusField();
//        long targetFieldId = ((Aggregation) targetField.config().getCalculation()).getFieldId();
//        AggregationType aggregationType = ((Aggregation) targetField.config().getCalculation()).getAggregationType();
//
//        Optional<IValue> n = entity.entityValue().getValue(targetFieldId);
//
//        // 获取当前的原始版本.
//        Optional<IValue> o = Optional.empty();
//        try {
//            Optional<IEntity> entityOptional = context.getMasterStorage().selectOne(entity.id(), context.getEntityClass());
//            if (entityOptional.isPresent()) {
//                o = entityOptional.get().entityValue().getValue(targetFieldId);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);
//        Optional<IValue> targetValue;
//        if (aggregationType.equals(AggregationType.AVG)) {
//            int count = 1;
//            Optional<Object> countOp = context.getAttribute("count");
//            if (countOp.isPresent()) {
//                count = (int) countOp.get();
//            }
//            // 求平均值需要count信息
//            targetValue = ((AvgFunction) function).excuteAvg(n, n, o, count);
//        } else {
//            targetValue = function.excute(n, n, o);
//        }
//        return targetValue;
//    }

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        return Optional.empty();
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {

    }

    @Override
    public long[] getMaintainTarget(CalculationContext context, IEntityClass entityClass, IEntityField field,
                                    Collection<IEntity> entities) throws CalculationException {
        return new long[0];
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AGGREGATION;
    }


}
