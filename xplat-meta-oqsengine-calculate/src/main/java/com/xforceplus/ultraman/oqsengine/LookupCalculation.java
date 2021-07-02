package com.xforceplus.ultraman.oqsengine;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.CalculationException;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.util.Optional;

/**
 * lookup字段计算.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:52
 * @since 1.8
 */
public class LookupCalculation implements Calculation {

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        IEntity targetEntity = findTargetEntity(context);
        IValue targetValue = findTargetValue(context, targetEntity);

        if (targetValue == null) {
            // 表示目标对象没有此字段,lookup本身也不需要此值.
            return Optional.empty();
        }

        targetValue.copy(context.getSourceValue().getField());

        return Optional.of(targetValue);
    }

    // 找到目标实体.
    private IEntity findTargetEntity(CalculationContext context) throws CalculationException {
        IValue<Long> sourceValue = context.getSourceValue();
        MetaManager metaManager = context.getMetaManager();
        Optional<IEntityClass> targetEntityClassOp = metaManager.load(
            sourceValue.getField().config().getLookupEntityClassId());
        if (!targetEntityClassOp.isPresent()) {
            throw new CalculationException(
                String.format("Invalid target meta information.[entityClassid = %d]",
                    sourceValue.getField().config().getLookupEntityClassId()));
        }
        IEntityClass targetEntityClass = targetEntityClassOp.get();

        MasterStorage masterStorage = context.getMasterStorage();

        Optional<IEntity> targetEntityOp;
        try {
            targetEntityOp = masterStorage.selectOne(sourceValue.getValue(), targetEntityClass);
        } catch (SQLException ex) {
            throw new CalculationException(ex.getMessage(), ex);
        }

        if (!targetEntityOp.isPresent()) {
            throw new CalculationException(
                String.format("Invalid instance.[entityId = %d, entityClassId = %d]",
                    sourceValue.getValue(), targetEntityClass.id())
            );
        }

        return targetEntityOp.get();
    }

    private IValue findTargetValue(CalculationContext context, IEntity targetEntity) {
        long targetFieldId = context.getSourceValue().getField().config().getLookupEntityFieldId();
        Optional<IValue> targetValue = targetEntity.entityValue().getValue(targetFieldId);
        return targetValue.orElse(null);
    }
}
