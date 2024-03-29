package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.infuence.LookupInfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.utils.LookupEntityRefIterator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LookupValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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

    /**
     * 事务内处理的最大极限数量.
     */
    private static final int TRANSACTION_LIMIT_NUMBER = 1000;
    /**
     * 单个任务处理的实例上限.
     */
    private static final int TASK_LIMIT_NUMBER = 10000;

    private LookupInfuenceConsumer infuenceConsumer;

    public LookupCalculationLogic() {
        this.infuenceConsumer = new LookupInfuenceConsumer();
    }

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        IEntityField focusField = context.getFocusField();
        IEntity focusEntity = context.getFocusEntity();

        long targetEntityClassId = ((Lookup) focusField.config().getCalculation()).getClassId();
        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());
        Optional<IEntityClass> targetEntityClassOp =
            metaManager.load(targetEntityClassId, OqsProfile.UN_DEFINE_PROFILE);
        if (!targetEntityClassOp.isPresent()) {
            throw new CalculationException(
                String.format("The expected target object meta information was not found.[%s]", targetEntityClassId));
        }

        IEntityClass targetEntityClass = targetEntityClassOp.get();

        if (!context.isMaintenance()) {

            Optional<IValue> lookupValueOp = focusEntity.entityValue().getValue(focusField.id());
            if (!lookupValueOp.isPresent()) {

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Unable to instantiate the field that launched the lookup.[entityClass={}, field={}]",
                        focusEntity.entityClassRef(), focusField.fieldName()
                    );
                }

                return Optional.empty();
            }

            IValue lookupValue = lookupValueOp.get();
            /*
            非维护状态计算只会处理LookupValue类型的值.
             */
            if (!LookupValue.class.isInstance(lookupValue)) {
                // 保持原样.

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The current state is not maintenance, so the field ({}) remains unchanged.",
                        focusField.fieldName());
                }

                return Optional.ofNullable(lookupValue);

            } else {

                return doLookup(context, (LookupValue) lookupValue, targetEntityClass);
            }
        } else {
            /*
            由维护触发的计算,由于lookup只能指向静态字段同时也不能被lookup所以只会出现在影响树的第二层,第一层为影响源.
            所以这里直接获得最终的触发entity实体.
             */
            IEntity sourceEntity = context.getSourceEntity();

            /*
            判断当前的值和目标值是否一致,一致将不进行重新lookup.
            当前lookup为空,如果目标不为空将认为是不一致.
             */

            // 当前发起lookup对象的字段值.
            Optional<IValue> nowLookupValueOp = focusEntity.entityValue().getValue(focusField.id());

            // 当前lookup目录对象的字段值.
            long lookUpFieldId = ((Lookup) focusField.config().getCalculation()).getFieldId();
            Optional<IValue> nowTargetValueOp = sourceEntity.entityValue().getValue(lookUpFieldId);

            if (!nowLookupValueOp.isPresent() && !nowTargetValueOp.isPresent()) {
                // 发起方和目标方都不存在此字段,不需改变.
                return nowLookupValueOp;

            } else if (nowTargetValueOp.isPresent() && !nowLookupValueOp.isPresent()) {
                // 目标存在,但是发起方不存在.表示目标从 null -> value 的转变.

                LookupValue lookupValue = new LookupValue(focusField, sourceEntity.id());

                return doLookup(context, lookupValue, targetEntityClass);

            } else if (!nowTargetValueOp.isPresent() && nowLookupValueOp.isPresent()) {
                // 目标不存在,但是发起方存在.表示目标从 value -> null 的转变.

                LookupValue lookupValue = new LookupValue(focusField, sourceEntity.id());

                return doLookup(context, lookupValue, targetEntityClass);

            } else {
                //表示目标和发起方都存在,现在判断两者是否相等.
                IValue nowLookupValue = nowLookupValueOp.get();
                IValue nowTargetValue = nowTargetValueOp.get();

                if (nowTargetValue.equals(nowLookupValue)) {
                    // 保持原样.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Recalculation is not required because the values of the current instance "
                                + "({}) field ({}) and the target instance ({}) field are the same.",
                            focusEntity.id(), focusField.fieldName(), nowTargetValue.getField().fieldName());
                    }

                    return Optional.ofNullable(nowLookupValue);
                } else {

                    LookupValue lookupValue = new LookupValue(focusField, sourceEntity.id());

                    return doLookup(context, lookupValue, targetEntityClass);

                }
            }
        }

    }

    @Override
    public void scope(CalculationContext context, InfuenceGraph infuence) {

        infuence.scanNoSource(this.infuenceConsumer);
    }

    /**
     * lookup 只允许指向静态字段,同时不能指向其他lookup字段.
     * 所以只会处于影响树的第二层,第一层为触发源.
     */
    @Override
    public Collection<AffectedInfo> getMaintainTarget(
        CalculationContext context, Participant participant, Collection<IEntity> triggerEntities)
        throws CalculationException {

        if (participant.getField().calculationType() != CalculationType.LOOKUP) {
            throw new CalculationException(
                "Wrong field type, only computed fields of the type of the Lookup can be handled.");
        }

        Optional attachmentOp = participant.getAttachment();
        if (!attachmentOp.isPresent()) {

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "[lookup]The current participant [{},{}] has no attachments, so the impact instance cannot be calculated.",
                    participant.getEntityClass().code(),
                    participant.getField().fieldName());
            }

            return Collections.emptyList();
        } else {
            /*
            注意: 这里直接使用了 context.getSourceEntity() 作为了目标源.
            由于lookup类型不允许级连,所以影响树会有二层,第一层为发起操作的对象实例第二层为指向这个对象实例的参与者.
            这些参与者限制不会再有参与者指向他们.
             */

            // 判断是否为强关系,只有强关系才会在当前事务进行部份更新.
            boolean strong = (boolean) attachmentOp.get();

            if (!strong) {
                /*
                弱关系,不会在事务内处理.
                交由异步队列异步处理.
                 */
                LookupMaintainingTask lookupMaintainingTask = LookupMaintainingTask.Builder.anLookupMaintainingTask()
                    .withTargetClassRef(context.getFocusEntity().entityClassRef())
                    .withTargetFieldId(((Lookup) participant.getField().config().getCalculation()).getFieldId())
                    .withLookupClassRef(participant.getEntityClass().ref())
                    .withLookupFieldId(participant.getField().id())
                    .withLastStartLookupEntityId(0)
                    .withMaxSize(TASK_LIMIT_NUMBER)
                    .withTargetEntityId(context.getSourceEntity().id())
                    .build();


                addAfterCommitTask(context, lookupMaintainingTask);

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "[lookup] Because the relationship is weak, the current influence node is returned empty.[{},{}]",
                        participant.getEntityClass().code(),
                        participant.getField().fieldName());
                }

                return Collections.emptyList();

            } else {

                /*
                强关系,会在事务内处理最多 TRANSACTION_LIMIT_NUMBER 实例.
                 */
                LookupEntityRefIterator refIter =
                    new LookupEntityRefIterator(TRANSACTION_LIMIT_NUMBER, TRANSACTION_LIMIT_NUMBER);
                refIter.setCombinedSelectStorage(context.getResourceWithEx(() -> context.getConditionsSelectStorage()));
                refIter.setEntityClass(participant.getEntityClass());
                refIter.setField(participant.getField());
                refIter.setTargetEntityId(context.getSourceEntity().id());
                refIter.setStartId(0);

                List<EntityRef> refs = new ArrayList<>();
                while (refIter.hasNext()) {
                    refs.add(refIter.next());
                }

                List<AffectedInfo> affectedInfos =
                    refs.stream()
                        .map(r -> new AffectedInfo(context.getFocusEntity(), r.getId()))
                        .collect(Collectors.toList());

                if (refIter.more()) {
                    LookupMaintainingTask lookupMaintainingTask =
                        LookupMaintainingTask.Builder.anLookupMaintainingTask()
                            .withTargetEntityId(context.getFocusEntity().id())
                            .withTargetClassRef(context.getFocusEntity().entityClassRef())
                            .withTargetFieldId(
                                ((Lookup) participant.getField().config().getCalculation()).getFieldId())
                            .withLookupClassRef(participant.getEntityClass().ref())
                            .withLookupFieldId(participant.getField().id())
                            .withLastStartLookupEntityId(
                                affectedInfos.get(affectedInfos.size() - 1).getAffectedEntityId())
                            .withMaxSize(TASK_LIMIT_NUMBER)
                            .build();
                    addAfterCommitTask(context, lookupMaintainingTask);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("The number of instances affected by the participant ({},{}) is {}.",
                        participant.getEntityClass().code(),
                        participant.getField().fieldName(),
                        affectedInfos.size());
                }

                return affectedInfos;
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
    private Optional<IValue> doLookup(CalculationContext context, LookupValue lookupValue,
                                      IEntityClass targetEntityClass) {
        Optional<IEntity> targetEntityOp = findTargetEntity(context, lookupValue.valueToLong(), targetEntityClass);
        if (!targetEntityOp.isPresent()) {
            logger.warn("Unable to find the target of the lookup ({}).", lookupValue.valueToLong());
            // 清理发起方,目标对象不存在,完整清理发起方.
            return Optional.of(new EmptyTypedValue(context.getFocusField()));
        }

        IEntity targetEntity = targetEntityOp.get();

        Optional<IValue> targetValueOp = findTargetValue(context, targetEntity);
        if (!targetValueOp.isPresent()) {

            IEntityField focusField = context.getFocusField();
            // 没有找到目标值.
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "The target ({}) field ({}) value of the lookup could not be found.",
                    lookupValue.valueToLong(),
                    ((Lookup) focusField.config().getCalculation()).getFieldId()
                );
            }

            /*
            没有找到目标值,创建一个空的IValue实例,只记录附件用以在目标更新时候更新.
            这里利用了 MasterStorage 的特性, EmptyTypeValue 如果含有附件那么会更新保留附件.
             */
            return Optional.of(new EmptyTypedValue(context.getFocusField(), Long.toString(targetEntity.id())));

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

    private Optional<IEntity> findTargetEntity(CalculationContext context, long targetEntityId,
                                               IEntityClass targetEntityClass) {
        Optional<IEntity> targetEntityOp = context.getEntityToCache(targetEntityId);
        if (!targetEntityOp.isPresent()) {
            MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
            try {
                targetEntityOp = masterStorage.selectOne(targetEntityId, targetEntityClass);
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
