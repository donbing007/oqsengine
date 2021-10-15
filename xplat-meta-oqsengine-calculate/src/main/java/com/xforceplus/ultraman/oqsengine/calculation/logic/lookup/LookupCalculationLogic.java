package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lookup字段计算.
 *
 * @author dongbin
 * @version 0.1 2021/07/01 17:52
 * @since 1.8
 */
public class LookupCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(LookupCalculationLogic.class);

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        return null;
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {
        IEntityClass targetEntityClass = context.getFocusClass();
        IEntityField targetField = context.getFocusField();

        infuence.scan((parentClass, participant, infuenceInner) -> {

            return true;
        });
    }

    @Override
    public long[] getMaintainTarget(CalculationContext context, Participant participant, Collection<IEntity> entities)
        throws CalculationException {
        return new long[0];
    }

    //    @Override
    //    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
    //        Optional<IValue> lookupValueOp = findLookupValue(context);
    //        IValue lookupValue = null;
    //        if (lookupValueOp.isPresent()) {
    //            lookupValue = lookupValueOp.get();
    //        } else {
    //            return Optional.empty();
    //        }
    //
    //        /*
    //        如果是LookupValue类型,表示此次需要重新指定lookup字段的目标.否则原样返回.
    //         */
    //        if (!LookupValue.class.isInstance(lookupValue)) {
    //            return Optional.empty();
    //        }
    //
    //        IEntity targetEntity = findTargetEntity(context);
    //        if (targetEntity == null) {
    //            if (logger.isDebugEnabled()) {
    //                logger.debug("No target object found, ignoring the current lookup field ({}-{}) calculation.",
    //                    context.getFocusField().id(), context.getFocusField().name());
    //            }
    //            return Optional.empty();
    //        }
    //
    //        IValue targetValue = findTargetValue(context, targetEntity);
    //
    //        if (targetValue == null) {
    //            // 表示目标对象没有此字段,lookup本身也不需要此值.
    //            if (logger.isDebugEnabled()) {
    //                logger.debug(
    //                    "The target entity ({}) was found, but the value of the target field ({}-{}) could not be found in the entity.",
    //                    targetEntity.id(), context.getFocusField().id(), context.getFocusField().name());
    //            }
    //            return Optional.empty();
    //        }
    //
    //        logLink(context, targetEntity);
    //
    //        targetValue = targetValue.copy(context.getFocusField());
    //
    //        return Optional.of(targetValue);
    //    }
    //
    //    /**
    //     * 维护策略如下.
    //     * 检查当前对像中的所有关系对象,直接更新其对象的.
    //     * 所以当前焦点字段应该是一个非lookup的字段.
    //     *
    //     * @param context 计算上下文.
    //     * @throws CalculationException 计算异常.
    //     */
    //    @Override
    //    public void maintain(CalculationContext context) throws CalculationException {
    //        IEntityClass focusClass = context.getFocusEntityClass();
    //
    //        // 构造维护信息.
    //        forceClass.relationship().stream().map(r -> {
    //                IEntityClass relationshipClass = r.getRightEntityClass();
    //
    //                return relationshipClass.fields().stream().filter(f ->
    //                    /*
    //                     * lookup字段,同时指向当前焦点字段的字段.
    //                     */
    //                    f.calculationType() == CalculationType.LOOKUP
    //                        && ((Lookup) f.config().getCalculation()).getFieldId() == context.getFocusField().id()
    //
    //                ).map(f ->
    //
    //                    new LookupMaintaining(context.getFocusField(), context.getEntity(), relationshipClass, f, r.isStrong())
    //
    //                ).collect(Collectors.toList());
    //
    //            }).flatMap(m -> m.stream()).sorted(Comparator.comparing(LookupMaintaining::isStrong))
    //            .forEach(lm -> processLookupMaintaining(lm, context));
    //
    //        return true;
    //    }
    //
    //    @Override
    //    public CalculationScenarios[] needMaintenanceScenarios() {
    //        return new CalculationScenarios[] {
    //            CalculationScenarios.REPLACE,
    //            CalculationScenarios.DELETE
    //        };
    //    }
    //
    //    @Override
    //    public CalculationType supportType() {
    //        return CalculationType.LOOKUP;
    //    }
    //
    //    private Optional<IValue> findLookupValue(CalculationContext context) {
    //        IEntity sourceEntity = context.getFocusEntity();
    //        IEntityField sourceField = context.getFocusField();
    //
    //        return sourceEntity.entityValue().getValue(sourceField.id());
    //    }
    //
    //    // 找到目标实体.
    //    private IEntity findTargetEntity(CalculationContext context) throws CalculationException {
    //        IEntity sourceEntity = context.getFocusEntity();
    //        IEntityField sourceField = context.getFocusField();
    //
    //        /*
    //        定位发起lookup的entity中的指定实例值.
    //        其应该是一个long型指向目标target的id值.
    //         */
    //        Lookup lookup = (Lookup) context.getFocusField().config().getCalculation();
    //        Optional<IValue> sourceValueOp = sourceEntity.entityValue().getValue(sourceField.id());
    //        if (!sourceValueOp.isPresent()) {
    //            return null;
    //        }
    //
    //        IValue<Long> sourceValue = sourceValueOp.get();
    //        if (!LongValue.class.isInstance(sourceValue)) {
    //            throw new CalculationException(String.format(
    //                "The Lookup field pointer is expected to be a number, but is %s.",
    //                sourceValue.getClass().getSimpleName()));
    //        }
    //        MetaManager metaManager = context.getMetaManager();
    //        Optional<IEntityClass> targetEntityClassOp = metaManager.load(
    //            lookup.getClassId());
    //        if (!targetEntityClassOp.isPresent()) {
    //            throw new CalculationException(
    //                String.format("Invalid target meta information.[entityClassid = %d]",
    //                    lookup.getClassId()));
    //        }
    //        IEntityClass targetEntityClass = targetEntityClassOp.get();
    //
    //        MasterStorage masterStorage = context.getMasterStorage();
    //
    //        Optional<IEntity> targetEntityOp;
    //        try {
    //            targetEntityOp = masterStorage.selectOne(sourceValue.getValue(), targetEntityClass);
    //        } catch (SQLException ex) {
    //            throw new CalculationException(ex.getMessage(), ex);
    //        }
    //
    //        if (!targetEntityOp.isPresent()) {
    //            throw new CalculationException(
    //                String.format("Invalid instance.[entityId = %d, entityClassId = %d]",
    //                    sourceValue.getValue(), targetEntityClass.id())
    //            );
    //        }
    //
    //        return targetEntityOp.get();
    //    }
    //
    //    // 查找目标值.
    //    private IValue findTargetValue(CalculationContext context, IEntity targetEntity) {
    //        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
    //        Optional<IValue> targetValue = targetEntity.entityValue().getValue(targetFieldId);
    //        return targetValue.orElse(null);
    //    }
    //
    //    /**
    //     * 记录当前lookup关系.
    //     * 用以在之后查询那些实例lookup了目标.
    //     * 记录以KV方式记录.
    //     * value为目标target的实例标识.
    //     *
    //     * @see ByteUtil
    //     * @see com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper
    //     */
    //    private void logLink(CalculationContext context, IEntity targetEntity) throws CalculationException {
    //        Optional<IEntityClass> targetEntityClassOp = context.getMetaManager().load(
    //            targetEntity.entityClassRef().getId());
    //        if (!targetEntityClassOp.isPresent()) {
    //            throw new CalculationException(
    //                String.format("Invalid target meta information.[entityClassid = %d]",
    //                    targetEntity.entityClassRef().getId()));
    //        }
    //
    //        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
    //        Optional<IEntityField> targetFieldOp = targetEntityClassOp.get().field(targetFieldId);
    //        if (!targetFieldOp.isPresent()) {
    //            throw new CalculationException(
    //                String.format("No instance field to lookup target.[entityFieldId = %d]", targetFieldId));
    //        }
    //
    //        LookupHelper.LookupLinkKey key = LookupHelper.buildLookupLinkKey(
    //            targetEntity, targetFieldOp.get(),
    //            context.getFocusEntity(), context.getFocusField());
    //
    //        context.getKvStorage().save(key.toString(), null);
    //    }
    //
    //    /**
    //     * 处理单个维护信息,如果是强关系那么将会在当前事务中进行处理.
    //     * 不过处理会控制在一个阀值数据量中.
    //     */
    //    private void processLookupMaintaining(LookupMaintaining lookupMaintaining, CalculationContext context) {
    //
    //        LookupHelper.LookupLinkIterKey iterLinkKey = LookupHelper.buildIteratorPrefixLinkKey(
    //            lookupMaintaining.getTargetEntity(),
    //            lookupMaintaining.getTargetField(),
    //            lookupMaintaining.getLookupClass(),
    //            lookupMaintaining.getLookupField());
    //
    //        LookupMaintainingTask task = new LookupMaintainingTask(iterLinkKey.toString());
    //
    //        if (lookupMaintaining.isStrong()) {
    //
    //            // 强关系需要部份保证在事务内一致性,所以这里会先执行一次任务.
    //
    //            Optional<TaskRunner> runnerOp = context.getTaskCoordinator().getRunner(task.runnerType());
    //            if (!runnerOp.isPresent()) {
    //                logger.warn("Unable to find task Runner of type {}.", task.getClass());
    //                return;
    //            }
    //            TaskRunner runner = runnerOp.get();
    //            runner.run(context.getTaskCoordinator(), task);
    //
    //        } else {
    //            context.getTaskCoordinator().addTask(task);
    //        }
    //    }

    // 维护信息
    static class LookupMaintaining implements Comparable<LookupMaintaining> {
        private IEntityField targetField;
        private IEntity targetEntity;
        private IEntityClass lookupClass;
        private IEntityField lookupField;
        private boolean strong;

        public LookupMaintaining(IEntityField targetField,
                                 IEntity targetEntity,
                                 IEntityClass lookupClass,
                                 IEntityField lookupField,
                                 boolean strong) {
            this.targetField = targetField;
            this.targetEntity = targetEntity;
            this.lookupClass = lookupClass;
            this.lookupField = lookupField;
            this.strong = strong;
        }

        public IEntityField getTargetField() {
            return targetField;
        }

        public IEntity getTargetEntity() {
            return targetEntity;
        }

        public IEntityClass getLookupClass() {
            return lookupClass;
        }

        public IEntityField getLookupField() {
            return lookupField;
        }

        public boolean isStrong() {
            return strong;
        }

        @Override
        public int compareTo(LookupMaintaining o) {
            if (this.strong && !o.strong) {
                return -1;
            } else if (!this.strong && o.strong) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
