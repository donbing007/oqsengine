package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationLogicException;
import com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.common.ByteUtil;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
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

    /**
     * 维护策略如下.
     * 检查当前对像中的所有关系对象,直接更新其对象的.
     * 所以当前焦点字段应该是一个非lookup的字段.
     *
     * @param context 计算上下文.
     * @throws CalculationLogicException 计算异常.
     */
    @Override
    public void maintain(CalculationLogicContext context) throws CalculationLogicException {
        if (!context.isReplace()) {
            return;
        }
        // 这是一个lookup计算字段,本身不需要进行维护.只有其指向的字段改变才需要维护.
        if (context.getFocusField().calculationType() == CalculationType.LOOKUP) {
            return;
        }

        IEntityClass forceClass = context.getEntityClass();

        // 构造维护信息.
        forceClass.relationship().stream().map(r -> {
            IEntityClass relationshipClass = r.getRightEntityClass();

            return relationshipClass.fields().stream().filter(f ->
                /*
                 * lookup字段,同时指向当前焦点字段的字段.
                 */
                f.calculationType() == CalculationType.LOOKUP
                    && ((Lookup) f.config().getCalculation()).getFieldId() == context.getFocusField().id()

            ).map(f ->
                new LookupMaintaining(context.getFocusField(), context.getEntity(), relationshipClass, f, r.isStrong())
            ).collect(Collectors.toList());
        }).flatMap(m -> m.stream()).sorted(Comparator.comparing(LookupMaintaining::isStrong))
            .forEach(lm -> processLookupMaintaining(lm, context));
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.LOOKUP;
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

    // 查找目标值.
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

        LookupHelper.LookupLinkKey key = LookupHelper.buildLookupLinkKey(
            targetEntity, targetFieldOp.get(),
            context.getEntity(), context.getFocusField());

        context.getKvStorage().save(key.toString(), null);
    }

    /**
     * 处理单个维护信息,如果是强关系那么将会在当前事务中进行处理.
     * 不过处理会控制在一个阀值数据量中.
     */
    private void processLookupMaintaining(LookupMaintaining lookupMaintaining, CalculationLogicContext context) {

        LookupHelper.LookupLinkIterKey iterLinkKey = LookupHelper.buildIteratorPrefixLinkKey(
            lookupMaintaining.getTargetEntity(),
            lookupMaintaining.getTargetField(),
            lookupMaintaining.getLookupClass(),
            lookupMaintaining.getLookupField());

        LookupMaintainingTask task = new LookupMaintainingTask(iterLinkKey.toString());

        if (lookupMaintaining.isStrong()) {

            // 强关系需要部份保证在事务内一致性,所以这里会先执行一次任务.

            Optional<TaskRunner> runnerOp = context.getTaskCoordinator().getRunner(task.getClass());
            if (!runnerOp.isPresent()) {
                logger.warn("Unable to find task Runner of type {}.", task.getClass());
                return;
            }
            TaskRunner runner = runnerOp.get();
            runner.run(context.getTaskCoordinator(), task);

        } else {
            context.getTaskCoordinator().addTask(task);
        }
    }

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
