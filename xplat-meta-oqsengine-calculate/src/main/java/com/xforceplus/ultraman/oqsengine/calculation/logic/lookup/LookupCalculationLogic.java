package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
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

        logLink(context, targetEntity);

        targetValue = targetValue.copy(context.getFocusField());

        return Optional.of(targetValue);
    }

    @Override
    public void maintain(CalculationLogicContext context) throws CalculationLogicException {
        IEntityClass forceClass = context.getEntityClass();
        //forceClass.relations()
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.LOOKUP;
    }

    @Override
    public boolean needMaintenance() {
        return true;
    }

    // 找到目标实体.
    private IEntity findTargetEntity(CalculationLogicContext context) throws CalculationLogicException {
        IEntity sourceEntity = context.getEntity();
        IEntityField sourceField = context.getFocusField();

        /*
        定位发起lookup的entity中的指定实例值.
        其应该是一个long型指向目标target的id值.
         */
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

    /**
     * 记录当前lookup关系.
     * 用以在之后查询那些实例lookup了目标.
     * 记录以KV方式记录.
     * value为目标target的实例标识.
     *
     * @see ByteUtil
     * @see com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper
     */
    private void logLink(CalculationLogicContext context, IEntity targetEntity) throws CalculationLogicException {
        Optional<IEntityClass> targetEntityClassOp = context.getMetaManager().load(
            targetEntity.entityClassRef().getId());
        if (!targetEntityClassOp.isPresent()) {
            throw new CalculationLogicException(
                String.format("Invalid target meta information.[entityClassid = %d]",
                    targetEntity.entityClassRef().getId()));
        }

        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
        Optional<IEntityField> targetFieldOp = targetEntityClassOp.get().field(targetFieldId);
        if (!targetFieldOp.isPresent()) {
            throw new CalculationLogicException(
                String.format("No instance field to lookup target.[entityFieldId = %d]", targetFieldId));
        }

        String key = LookupHelper.buildLookupLinkKey(targetFieldOp.get(), context.getEntity());

        context.getKvStorage().save(key, ByteUtil.longToByte(targetEntity.id()));
    }
}
