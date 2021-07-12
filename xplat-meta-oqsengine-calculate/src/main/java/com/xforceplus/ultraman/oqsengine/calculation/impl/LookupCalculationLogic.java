package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
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
public class LookupCalculationLogic implements CalculationLogic {

    @Override
    public Optional<IValue> calculate(CalculationLogicContext context) throws CalculationLogicException {
        IEntity targetEntity = findTargetEntity(context);
        IValue targetValue = findTargetValue(context, targetEntity);

        if (targetValue == null) {
            // 表示目标对象没有此字段,lookup本身也不需要此值.
            return Optional.empty();
        }

        targetValue.copy(context.getFocusField());

        return Optional.of(targetValue);
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.LOOKUP;
    }

    // 找到目标实体.
    private IEntity findTargetEntity(CalculationLogicContext context) throws CalculationLogicException {
        IEntity sourceEntity = context.getEntity();
        IEntityField sourceField = context.getFocusField();

        Lookup lookup = (Lookup) context.getFocusField().config().getCalculation();

        Optional<IValue> sourceValueOp = sourceEntity.entityValue().getValue(sourceField.id());
        if (!sourceValueOp.isPresent()) {
            return null;
        }

        IValue<Long> sourceValue = sourceValueOp.get();
        MetaManager metaManager = context.getMetaManager();
        Optional<IEntityClass> targetEntityClassOp = metaManager.load(
            lookup.getClassId());
        if (!targetEntityClassOp.isPresent()) {
            throw new CalculationLogicException(
                String.format("Invalid target meta information.[entityClassid = %d]",
                    lookup.getClassId()));
        }
        IEntityClass targetEntityClass = targetEntityClassOp.get();

        MasterStorage masterStorage = context.getMasterStorage();

        Optional<IEntity> targetEntityOp;
        try {
            targetEntityOp = masterStorage.selectOne(sourceValue.getValue(), targetEntityClass);
        } catch (SQLException ex) {
            throw new CalculationLogicException(ex.getMessage(), ex);
        }

        if (!targetEntityOp.isPresent()) {
            throw new CalculationLogicException(
                String.format("Invalid instance.[entityId = %d, entityClassId = %d]",
                    sourceValue.getValue(), targetEntityClass.id())
            );
        }

        return targetEntityOp.get();
    }

    private IValue findTargetValue(CalculationLogicContext context, IEntity targetEntity) {
        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
        Optional<IValue> targetValue = targetEntity.entityValue().getValue(targetFieldId);
        return targetValue.orElse(null);
    }
}
