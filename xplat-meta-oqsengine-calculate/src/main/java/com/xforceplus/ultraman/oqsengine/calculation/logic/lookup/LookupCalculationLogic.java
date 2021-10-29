package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import io.micrometer.core.annotation.Timed;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    /**
     * 事务内处理的最大极限数量.
     */
    private static final int TRANSACTION_LIMIT_NUMBER = 1000;

    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC,
        extraTags = {"logic", "lookup", "action", "calculate"}
    )
    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        IEntityField focusField = context.getFocusField();
        IEntity focusEntity = context.getFocusEntity();

        Optional<IValue> lookupValueOp = focusEntity.entityValue().getValue(focusField.id());
        if (!lookupValueOp.isPresent()) {
            return Optional.empty();
        }

        if (!context.isMaintenance()) {
            IValue lookupValue = lookupValueOp.get();
            /*
            非维护状态计算只会处理LookupValue类型的值.
             */
            if (!LookupValue.class.isInstance(lookupValue)) {
                return Optional.empty();
            } else {

                return doLookup(context, (LookupValue) lookupValue);
            }
        } else {
            /*
            由维护触发的计算,由于lookup只能指向静态字段同时也不能被lookup所以只会出现在影响树的第二层,第一层为影响源.
            所以这里直接获得最终的触发entity实体.
             */
            IEntity sourceEntity = context.getSourceEntity();

            /*
            判断当前的值和目标值是否一致,一致将不进行重新lookup.
             */
            IValue nowLookupValue = focusEntity.entityValue().getValue(focusField.id()).get();
            long lookUpFieldId = ((Lookup) focusField.config().getCalculation()).getFieldId();
            IValue nowTargetValue = sourceEntity.entityValue().getValue(lookUpFieldId).get();
            if (nowTargetValue.equals(nowLookupValue)) {
                return Optional.empty();
            }

            LookupValue lookupValue = new LookupValue(focusField, sourceEntity.id());
            return doLookup(context, lookupValue);
        }

    }

    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC,
        extraTags = {"logic", "lookup", "action", "scope"}
    )
    @Override
    public void scope(CalculationContext context, Infuence infuence) {

        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            IEntityClass participantClass = participant.getEntityClass();
            IEntityField participantField = participant.getField();

            /*
            迭代所有关系中的字段,判断是否有可能会对当前参与者发起lookup.
             */
            for (Relationship r : participantClass.relationship()) {
                IEntityClass relationshipClass = r.getRightEntityClass();
                relationshipClass.fields().stream()
                    .filter(f -> f.calculationType() == CalculationType.LOOKUP)
                    .filter(f -> ((Lookup) f.config().getCalculation()).getFieldId() == participantField.id())
                    .forEach(f -> {
                        /*
                        指定当前参与者的新lookup发起者信息,包含如下.
                        1. 发起lookup元信息.
                        2. 发起lookup字段.
                        3. 不含目标实例的lookup link key.
                         */
                        infuenceInner.impact(
                            participant,
                            Participant.Builder.anParticipant()
                                .withEntityClass(relationshipClass)
                                .withField(f)
                                .withAttachment(r.isStrong())
                                .build()
                        );
                    });
            }

            return InfuenceConsumer.Action.CONTINUE;
        });
    }

    /**
     * lookup 只允许指向静态字段,同时不能指向其他lookup字段.
     * 所以只会处于影响树的第二层,第一层为触发源.
     */
    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC,
        extraTags = {"logic", "lookup", "action", "getTarget"}
    )
    @Override
    public long[] getMaintainTarget(CalculationContext context, Participant participant,
                                    Collection<IEntity> triggerEntities)
        throws CalculationException {

        Optional attachmentOp = participant.getAttachment();
        if (!attachmentOp.isPresent()) {
            return new long[0];
        } else {

            // 判断是否为强关系,只有强关系才会在当前事务进行部份更新.
            boolean strong = (boolean) attachmentOp.get();
            LookupHelper.LookupLinkIterKey lookupLinkIterKey = LookupHelper.buildIteratorPrefixLinkKey(
                context.getFocusField(), participant.getEntityClass(), participant.getField(),
                context.getFocusEntity());

            if (!strong) {
                /*
                弱关系,不会在事务内处理.
                交由异步队列异步处理.
                 */
                TaskCoordinator coordinator = context.getResourceWithEx(() -> context.getTaskCoordinator());
                context.getResourceWithEx(() -> context.getCurrentTransaction()).registerCommitHook(t -> {
                    coordinator.addTask(
                        new LookupMaintainingTask(lookupLinkIterKey.toString(), TRANSACTION_LIMIT_NUMBER));
                });

                return new long[0];

            } else {

                /*
                强关系,会在事务内处理最多 TRANSACTION_LIMIT_NUMBER 实例.
                 */
                KeyValueStorage kv = context.getResourceWithEx(() -> context.getKvStorage());

                String iterKey = lookupLinkIterKey.toString();
                KeyIterator iter = kv.iterator(iterKey);
                String key;
                LookupHelper.LookupLinkKey linkKey;
                List<LookupHelper.LookupLinkKey> links = new ArrayList<>();
                int index = 0;
                while (iter.hasNext()) {
                    if (index >= TRANSACTION_LIMIT_NUMBER) {
                        break;
                    }

                    key = iter.next();

                    try {
                        linkKey = LookupHelper.parseLinkKey(key);
                    } catch (Exception ex) {
                        continue;
                    }

                    links.add(linkKey);
                    index++;
                }

                if (index >= TRANSACTION_LIMIT_NUMBER) {
                    /*
                    超出 TRANSACTION_LIMIT_NUMBER 数量将进行事务外处理.
                    这里会利用事务提供的回调在事务提交后执行.
                    */
                    TaskCoordinator coordinator = context.getResourceWithEx(() -> context.getTaskCoordinator());
                    context.getResourceWithEx(() -> context.getCurrentTransaction()).registerCommitHook(t -> {
                        String lastKey = iter.currentKey();
                        coordinator.addTask(new LookupMaintainingTask(iterKey, lastKey, TRANSACTION_LIMIT_NUMBER));
                    });
                }

                return links.stream().mapToLong(l -> l.getLookupEntityId()).toArray();
            }
        }
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.LOOKUP;
    }

    @Override
    public CalculationScenarios[] needMaintenanceScenarios() {
        return new CalculationScenarios[] {
            CalculationScenarios.REPLACE,
        };
    }

    /**
     * 实际进行lookup.
     */
    private Optional<IValue> doLookup(CalculationContext context, LookupValue lookupValue) {
        Optional<IEntity> targetEntityOp = findTargetEntity(context, lookupValue.valueToLong());
        if (!targetEntityOp.isPresent()) {
            logger.warn("Unable to find the target of the lookup ({}).", lookupValue.valueToLong());
            return Optional.empty();
        }

        IEntity targetEntity = targetEntityOp.get();

        Optional<IValue> targetValueOp = findTargetValue(context, targetEntity);
        if (!targetValueOp.isPresent()) {

            IEntityField focusField = context.getFocusField();
            // 没有找到目标值.
            logger.warn(
                "The target ({}) field ({}) value of the lookup could not be found.",
                lookupValue.valueToLong(),
                ((Lookup) focusField.config().getCalculation()).getFieldId()
            );

            return Optional.empty();

        } else {

            IValue targetValue = targetValueOp.get();


            targetValue = targetValue.copy(context.getFocusField());

            // 非维护模式才需要log.
            if (!context.isMaintenance()) {
                logLink(context, targetEntity);
            }

            return Optional.of(targetValue);
        }
    }

    private Optional<IEntity> findTargetEntity(CalculationContext context, long targetEntityId) {
        Optional<IEntity> targetEntityOp = context.getEntityToCache(targetEntityId);
        if (!targetEntityOp.isPresent()) {
            MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
            try {
                targetEntityOp = masterStorage.selectOne(targetEntityId);
            } catch (SQLException ex) {
                throw new CalculationException(ex.getMessage(), ex);
            }
        }

        return targetEntityOp;
    }

    // 查找目标值.
    private Optional<IValue> findTargetValue(CalculationContext context, IEntity targetEntity) {

        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
        return targetEntity.entityValue().getValue(targetFieldId);
    }

    // 记录lookup的link.
    private void logLink(CalculationContext context, IEntity targetEntity) throws CalculationException {
        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());
        Optional<IEntityClass> targetEntityClassOp = metaManager.load(targetEntity.entityClassRef());
        if (!targetEntityClassOp.isPresent()) {
            throw new CalculationException(
                String.format("Invalid target meta information.[entityClassid = %d]",
                    targetEntity.entityClassRef().getId()));
        }

        long targetFieldId = ((Lookup) context.getFocusField().config().getCalculation()).getFieldId();
        Optional<IEntityField> targetFieldOp = targetEntityClassOp.get().field(targetFieldId);
        if (!targetFieldOp.isPresent()) {
            throw new CalculationException(
                String.format("No instance field to lookup target.[entityFieldId = %d]", targetFieldId));
        }

        LookupHelper.LookupLinkKey key = LookupHelper.buildLookupLinkKey(
            targetEntity, targetFieldOp.get(),
            context.getFocusEntity(), context.getFocusField());

        context.getResourceWithEx(() -> context.getKvStorage()).save(key.toString(), null);
    }
}
