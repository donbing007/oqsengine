package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactory;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.sql.SQLException;
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

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {
        IEntity entity = context.getEntity();
        IEntityField targetField = context.getFocusField();
        long targetFieldId = ((Aggregation) targetField.config().getCalculation()).getFieldId();
        AggregationType aggregationType = ((Aggregation) targetField.config().getCalculation()).getAggregationType();

        Optional<IValue> n = entity.entityValue().getValue(targetFieldId);

        // 获取当前的原始版本.
        Optional<IValue> o = Optional.empty();
        try {
            Optional<IEntity> entityOptional = context.getMasterStorage().selectOne(entity.id(), context.getEntityClass());
            if (entityOptional.isPresent()) {
                o = entityOptional.get().entityValue().getValue(targetFieldId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        AggregationFunction function = aggregationFunctionFactory.getAggregationFunction(aggregationType);
        Optional<IValue> targetValue;
        if (aggregationType.equals(AggregationType.AVG)) {
            int count = 1;
            // 求平均值需要count信息
            targetValue = ((AvgFunction) function).excuteAvg(n, n, o, count);
        } else {
            targetValue = function.excute(n, n, o);
        }
        return targetValue;
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AGGREGATION;
    }

}
