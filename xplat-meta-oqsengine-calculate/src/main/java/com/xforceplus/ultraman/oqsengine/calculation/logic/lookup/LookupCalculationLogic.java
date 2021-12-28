package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.utils.LookupEntityRefIterator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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
    /**
     * 单个任务处理的实例上限.
     */
    private static final int TASK_LIMIT_NUMBER = 10000;

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
                // 保持原样.
                return Optional.ofNullable(lookupValue);
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
                // 保持原样.
                return Optional.ofNullable(nowLookupValue);
            }

            LookupValue lookupValue = new LookupValue(focusField, sourceEntity.id());
            return doLookup(context, lookupValue);
        }

    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {

        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            IEntityClass participantClass = participant.getEntityClass();
            IEntityField participantField = participant.getField();

            /*
            迭代所有关系中的字段,判断是否有可能会对当前参与者发起lookup.
             */
            for (Relationship r : participantClass.relationship()) {
                // 应该包含所有定制的元信息.
                Collection<IEntityClass> relationshipClasss = r.getRightFamilyEntityClasses();

                for (IEntityClass relationshipClass : relationshipClasss) {
                    relationshipClass.fields().stream()
                        .filter(f -> f.calculationType() == CalculationType.LOOKUP)
                        .filter(f -> ((Lookup) f.config().getCalculation()).getFieldId() == participantField.id())
                        .forEach(f -> {
                            infuenceInner.impact(
                                participant,
                                    CalculationParticipant.Builder.anParticipant()
                                    .withEntityClass(relationshipClass)
                                    .withField(f)
                                    .withAttachment(r.isStrong())
                                    .build()
                            );
                        });
                }
            }

            return InfuenceConsumer.Action.CONTINUE;
        });
    }

    /**
     * lookup 只允许指向静态字段,同时不能指向其他lookup字段.
     * 所以只会处于影响树的第二层,第一层为触发源.
     */
    @Override
    public long[] getMaintainTarget(CalculationContext context, Participant abstractParticipant,
                                    Collection<IEntity> triggerEntities)
        throws CalculationException {

        Optional attachmentOp = abstractParticipant.getAttachment();
        if (!attachmentOp.isPresent()) {
            return new long[0];
        } else {

            // 判断是否为强关系,只有强关系才会在当前事务进行部份更新.
            boolean strong = (boolean) attachmentOp.get();

            if (!strong) {
                /*
                弱关系,不会在事务内处理.
                交由异步队列异步处理.
                 */
                LookupMaintainingTask lookupMaintainingTask = LookupMaintainingTask.Builder.anLookupMaintainingTask()
                    .withTargetEntityId(context.getFocusEntity().id())
                    .withTargetClassRef(context.getFocusEntity().entityClassRef())
                    .withTargetFieldId(((Lookup) abstractParticipant.getField().config().getCalculation()).getFieldId())
                    .withLookupClassRef(abstractParticipant.getEntityClass().ref())
                    .withLookupFieldId(abstractParticipant.getField().id())
                    .withLastStartLookupEntityId(0)
                    .withMaxSize(TASK_LIMIT_NUMBER)
                    .build();


                addAfterCommitTask(context, lookupMaintainingTask);

                return new long[0];

            } else {

                /*
                强关系,会在事务内处理最多 TRANSACTION_LIMIT_NUMBER 实例.
                 */
                LookupEntityRefIterator refIter =
                    new LookupEntityRefIterator(TRANSACTION_LIMIT_NUMBER, TRANSACTION_LIMIT_NUMBER);
                refIter.setCombinedSelectStorage(context.getResourceWithEx(() -> context.getConditionsSelectStorage()));
                refIter.setEntityClass(abstractParticipant.getEntityClass());
                refIter.setField(abstractParticipant.getField());
                refIter.setTargetEntityId(context.getFocusEntity().id());
                refIter.setStartId(0);

                List<EntityRef> refs = new ArrayList<>();
                while (refIter.hasNext()) {
                    refs.add(refIter.next());
                }
                long[] ids = refs.stream().mapToLong(r -> r.getId()).toArray();

                if (refIter.more()) {
                    LookupMaintainingTask lookupMaintainingTask =
                        LookupMaintainingTask.Builder.anLookupMaintainingTask()
                            .withTargetEntityId(context.getFocusEntity().id())
                            .withTargetClassRef(context.getFocusEntity().entityClassRef())
                            .withTargetFieldId(
                                ((Lookup) abstractParticipant.getField().config().getCalculation()).getFieldId())
                            .withLookupClassRef(abstractParticipant.getEntityClass().ref())
                            .withLookupFieldId(abstractParticipant.getField().id())
                            .withLastStartLookupEntityId(ids[ids.length - 1])
                            .withMaxSize(TASK_LIMIT_NUMBER)
                            .build();
                    addAfterCommitTask(context, lookupMaintainingTask);
                }

                return ids;
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

    // 提交的任务,必须保证是在事务提交后执行.
    private void addAfterCommitTask(CalculationContext context, LookupMaintainingTask lookupMaintainingTask) {
        TaskCoordinator coordinator = context.getResourceWithEx(() -> context.getTaskCoordinator());
        ExecutorService taskExecutor = context.getResourceWithEx(() -> context.getTaskExecutorService());
        context.getResourceWithEx(() -> context.getCurrentTransaction()).registerCommitHook(t -> {
            /*
             * 开启线程是为了不和当前事务共享连接.
             * 当前事务已经结束,底层的TaskCoordinator的实现中依赖基于数据库的KV.
             * 有可能会使用一个已经被关闭的事务,另启一个线程保证新开启事务.
             */
            taskExecutor.submit(() -> {

                if (logger.isDebugEnabled()) {
                    logger.info("Added a Lookup maintenance asynchronous task.{}", lookupMaintainingTask);
                }

                coordinator.addTask(lookupMaintainingTask);
            });
        });
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

            /*
            创建新的被替换的IValue实例.
            新值填充了一个IValue的附件,其为目标的实体标识.
            其用以在目标更新后可以找到当前实例.
             */
            targetValue = targetValue.copy(context.getFocusField(), Long.toString(targetEntity.id()));

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
}
