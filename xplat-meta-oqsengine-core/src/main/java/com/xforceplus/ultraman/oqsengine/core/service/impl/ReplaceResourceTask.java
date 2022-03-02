package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.lock.ResourceLocker;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntitys;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.ValueVerifier;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierFactory;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier.VerifierResult;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dongbin
 * @version 0.1 2022/3/2 19:27
 * @since 1.8
 */
public class ReplaceResourceTask implements ResourceTask<OqsResult> {
    final Logger logger = LoggerFactory.getLogger(ReplaceResourceTask.class);
    private static final int ONE_INCREMENT_POS = 1;
    private EventBus eventBus;
    private IEntity entity;
    private ResourceLocker resourceLocker;
    private MasterStorage masterStorage;
    private IEntityClass entityClass;
    private long lockTimeoutMs;
    private CalculationContext calculationContext;
    private Calculation calculation;

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setEntity(IEntity entity) {
        this.entity = entity;
    }

    public void setResourceLocker(ResourceLocker resourceLocker) {
        this.resourceLocker = resourceLocker;
    }

    public void setMasterStorage(MasterStorage masterStorage) {
        this.masterStorage = masterStorage;
    }

    public void setEntityClass(IEntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public void setLockTimeoutMs(long lockTimeoutMs) {
        this.lockTimeoutMs = lockTimeoutMs;
    }

    public void setCalculationContext(
        CalculationContext calculationContext) {
        this.calculationContext = calculationContext;
    }

    public void setCalculation(Calculation calculation) {
        this.calculation = calculation;
    }

    @Override
    public OqsResult run(Transaction tx, TransactionResource resource, ExecutorHint hint) throws Exception {
        String lockResource = IEntitys.resource(entity.id());
        boolean lockResult = resourceLocker.tryLock(lockTimeoutMs, lockResource);
        if (!lockResult) {
            return OqsResult.conflict();
        }

        IEntity newEntity;
        IEntity oldEntity;
        ReplacePayload replacePayload;
        try {
            /*
             * 获取当前的原始版本.
             */
            Optional<IEntity> targetEntityOp = masterStorage.selectOne(entity.id(), entityClass);
            if (!targetEntityOp.isPresent()) {
                return OqsResult.notFound();
            }

            // 新实例.
            newEntity = targetEntityOp.get();

            // 保留旧实例.
            oldEntity = newEntity.copy();

            // 操作时间
            newEntity.markTime();

            // 新的字段值加入当前实例.
            for (IValue newValue : entity.entityValue().values()) {
                newEntity.entityValue().addValue(newValue);
            }

            // 没有任何更新.
            if (!newEntity.isDirty()) {
                return OqsResult.success();
            }

            calculationContext.focusTx(tx);
            calculationContext.focusSourceEntity(newEntity);
            calculationContext.focusEntity(newEntity, entityClass);
            newEntity = calculation.calculate(calculationContext);
            setValueChange(calculationContext, Optional.of(newEntity), Optional.of(oldEntity));

            Map.Entry<VerifierResult, IEntityField> verifyResult = verifyFields(entityClass, newEntity);
            if (VerifierResult.OK != verifyResult.getKey()) {
                hint.setRollback(true);
                return transformVerifierResultToOperationResult(verifyResult, newEntity);
            }

            calculation.maintain(calculationContext);

            replacePayload = new ReplacePayload(tx.id());
            replacePayload.addChange(oldEntity,
                newEntity.entityValue().values().stream().filter(v -> v.isDirty()).toArray(IValue[]::new));

            masterStorage.replace(newEntity, entityClass);
            if (newEntity.isDirty()) {
                hint.setRollback(true);
                return OqsResult.unReplaced(newEntity.id());
            }

            if (!calculationContext.persist()) {
                hint.setRollback(true);
                return OqsResult.conflict();
            }


            //  这里将版本+1，使得外部获取的版本为当前成功版本
            newEntity.resetVersion(newEntity.version() + ONE_INCREMENT_POS);

            if (!tx.getAccumulator().accumulateReplace(oldEntity)) {
                hint.setRollback(true);
                return OqsResult.unAccumulate();
            }
        } finally {
            resourceLocker.unlock(lockResource);
        }

        eventBus.notify(new ActualEvent(EventType.ENTITY_REPLACE, replacePayload));

        return OqsResult.success(replacePayload.getChanage(oldEntity).get());
    }

    private void setValueChange(
        CalculationContext context, Optional<IEntity> newEntityOp, Optional<IEntity> oldEntityOp) {
        if (!newEntityOp.isPresent() && !oldEntityOp.isPresent()) {
            return;
        }

        if (newEntityOp.isPresent() && !oldEntityOp.isPresent()) {
            // build
            IEntity newEntity = newEntityOp.get();
            newEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(newEntity.id(), new EmptyTypedValue(field), v));
            });
        } else if (!newEntityOp.isPresent() && oldEntityOp.isPresent()) {
            // delete
            IEntity oldEntity = oldEntityOp.get();
            oldEntity.entityValue().scan(v -> {
                IEntityField field = v.getField();
                context.addValueChange(ValueChange.build(oldEntity.id(), v, new EmptyTypedValue(field)));
            });
        } else {
            // replace
            IEntity oldEntity = oldEntityOp.get();
            IEntity newEntity = newEntityOp.get();
            IValue oldValue;
            IEntityField field;
            for (IValue newValue : newEntity.entityValue().values()) {
                field = newValue.getField();
                oldValue = oldEntity.entityValue().getValue(field.id()).orElse(new EmptyTypedValue(field));

                if (newValue.equals(oldValue)) {
                    continue;
                } else {
                    context.addValueChange(ValueChange.build(oldEntity.id(), oldValue, newValue));
                }
            }
        }
    }

    private Map.Entry<VerifierResult, IEntityField> verifyFields(IEntityClass entityClass, IEntity entity) {
        VerifierResult result;
        for (IEntityField field : entityClass.fields()) {
            // 跳过主标识类型的检查.
            if (field.config().isIdentifie()) {
                continue;
            }

            ValueVerifier verifier = VerifierFactory.getVerifier(field.type());
            Optional<IValue> valueOp = entity.entityValue().getValue(field.id());
            IValue value = valueOp.orElse(null);
            try {
                result = verifier.verify(field, value);
                if (VerifierResult.OK != result) {
                    return new AbstractMap.SimpleEntry(result, field);
                }
            } catch (Exception e) {
                logger.warn("verify error, fieldId : {}, code : {}, value : {}, message : {}",
                    field.id(), field.name(), null == value ? null : value.getValue(), e.getMessage());
                throw e;
            }

        }

        return new AbstractMap.SimpleEntry(VerifierResult.OK, null);
    }

    private OqsResult transformVerifierResultToOperationResult(
        Map.Entry<VerifierResult, IEntityField> verify, IEntity entity) {

        IEntityField field = verify.getValue();
        IValue value = entity.entityValue().getValue(verify.getValue().id()).orElse(null);
        if (logger.isDebugEnabled()) {
            logger.debug("Field {}({}) validation result {}, validation is based on {}.[%s]",
                field.name(),
                field.id(),
                verify.getKey().name(),
                field.config().toString(),
                value != null ? value.getValue().toString() : "NULL");
        }

        switch (verify.getKey()) {
            case REQUIRED:
                return OqsResult.fieldMust(field);
            case TOO_LONG:
                return OqsResult.fieldTooLong(field);
            case HIGH_PRECISION:
                return OqsResult.fieldHighPrecision(field);
            case NON_EXISTENT:
                return OqsResult.fieldNonExist(field);
            default:
                return OqsResult.unknown();
        }
    }
}
