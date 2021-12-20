package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

import com.xforceplus.ultraman.oqsengine.calculation.event.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AppMetaChangePayLoad;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class AutoFillEventExecutor implements CalculationEventExecutor {

    private Logger log = LoggerFactory.getLogger(AutoFillEventExecutor.class);

    @Override
    public boolean execute(CalculationEvent calculationEvent, CachedEntityClass cachedEntityClass, CalculationEventResource resource)  throws SQLException {
        int version = Math.max(calculationEvent.getVersion(), NOT_EXIST_VERSION);
        for (Map.Entry<Long, List<AppMetaChangePayLoad.FieldChange>> entry : calculationEvent.getFieldChanges().entrySet()) {
            for (AppMetaChangePayLoad.FieldChange change : entry.getValue()) {

                IEntityClass entityClass = cachedEntityClass.findEntityClassWithCache(
                    resource.getMetaManager(), entry.getKey(), change.getProfile(), version);

                if (null != entityClass) {
                    autoFillSchemaUpdate(entityClass, change, resource);
                } else {
                    log.warn("entityClass not found in autoFill upgrade, event-");
                }
            }
        }
        return true;
    }


    private void autoFillSchemaUpdate(IEntityClass entityClass, AppMetaChangePayLoad.FieldChange fieldChange, CalculationEventResource resource)
        throws SQLException {
        SegmentStorage storage = resource.getSegmentStorage();
        String bizType = String.valueOf(fieldChange.getFieldId());
        Optional<SegmentInfo> segmentInfo = storage.query(bizType);
        Optional<IEntityField> entityFieldOp = entityClass.field(fieldChange.getFieldId());
        if (entityFieldOp.isPresent()) {
            AutoFill calculator = (AutoFill) entityFieldOp.get().config().getCalculation();
            if (!segmentInfo.isPresent()) {
                SegmentInfo info = SegmentInfo.builder().withVersion(0L)
                    .withCreateTime(new Timestamp(System.currentTimeMillis()))
                    .withUpdateTime(new Timestamp(System.currentTimeMillis()))
                    .withStep(calculator.getStep())
                    .withPatten(calculator.getPatten())
                    .withMode(Integer.valueOf(calculator.getModel()))
                    .withMaxId(Long.valueOf(calculator.getMin())).withBizType(bizType)
                    .withPatternKey("").withResetable(calculator.getResetType())
                    .withBeginId(1L).build();
                storage.build(info);
            } else {
                SegmentInfo info = segmentInfo.get();
                info.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                info.setStep(calculator.getStep());
                info.setPattern(calculator.getPatten());
                info.setMode(Integer.valueOf(calculator.getModel()));
                info.setResetable(Integer.valueOf(calculator.getResetType()));
                storage.udpate(info);
            }
        }
    }
}
