package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算字段计算框架.
 * 计算字段分为计算和维护两个阶段.
 * 计算针对当前目标实例,维护针对当前字段改变所影响的实例.
 * 维护的流程如下.
 * 1. 根据当前改变构造影响树.
 * 2. 迭代影响树不断计算被影响的实例.
 * 3. 批量更新被影响的实例.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:40
 * @since 1.8
 */
public class DefaultCalculationImpl implements Calculation {

    /**
     * 如果出现冲突,最大重试的次数.
     */
    private static int MAX_REPLAY_NUMBER = 100;
    /**
     * 每一个字段修改维护构造影响树的最大迭代次数.
     */
    private static int MAX_BUILD_INFUENCE_NUMBER = 1000;

    final Logger logger = LoggerFactory.getLogger(DefaultCalculationImpl.class);

    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS,
        extraTags = {"logic", "all", "action", "calculate"}
    )
    @Override
    public IEntity calculate(CalculationContext context) throws CalculationException {
        IEntity targetEntity = context.getFocusEntity();

        // 得到按优先级排序好的计算字段.并且过滤只处理改变的字段.
        Collection<IEntityField> calculationFields = parseChangeFields(context, true);
        // 计算逻辑工厂.
        CalculationLogicFactory calculationLogicFactory =
            context.getResourceWithEx(() -> context.getCalculationLogicFactory());

        for (IEntityField field : calculationFields) {
            CalculationLogic logic = calculationLogicFactory.getCalculationLogic(field.calculationType());
            context.focusField(field);

            if (logger.isDebugEnabled()) {
                logger.debug("Start using {} logic to compute instance {} fields {} of type {}.",
                    logic.getClass().getSimpleName(), targetEntity.id(), field.name(), context.getFocusClass().code());
            }

            Timer.Sample sample = Timer.start(Metrics.globalRegistry);

            Optional<IValue> newValueOp;
            try {
                newValueOp = logic.calculate(context);
            } catch (CalculationException ex) {
                processTimer(
                    logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "calculate", true);
                throw ex;
            }

            processTimer(
                logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "calculate", false);

            if (newValueOp.isPresent()) {
                targetEntity.entityValue().addValue(newValueOp.get());

                if (logger.isDebugEnabled()) {
                    logger.debug("Instance {} field {} evaluates to {}.",
                        targetEntity.id(), field.name(), newValueOp.get().getValue());
                }
            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("Instance {} field {} evaluates to {}.",
                        targetEntity.id(), field.name(), "NULL");
                }

            }
        }

        return targetEntity;
    }

    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS,
        extraTags = {"logic", "all", "action", "maintain"}
    )
    @Override
    public void maintain(CalculationContext context) throws CalculationException {
        // 保留当前引起维护的目标实例标识.
        long targetEntityId = context.getFocusEntity().id();

        Infuence[] infuences = scope(context);

        CalculationLogicFactory calculationLogicFactory =
            context.getResourceWithEx(() -> context.getCalculationLogicFactory());

        for (Infuence infuence : infuences) {
            /*
            根据所有影响树得到目标更新实例集合.
            entityClass 表示当前影响树被影响的实例元信息.
            field 表示被影响的字段.
            parentEntityClass 表示传递影响者是谁.

            依赖之前产生的影响树.
                           A
                         /   \
                      B(f1)   C(f2)
                      /
                    D(f3)
                 A是改变的源头,其就是当前写事务的操作目标.
                 B C D 就是被A的改变影响的实例元信息和字段信息.
                 这里scan会从A的下层开始以广度优先的模式扫描.操作流程如下.
                 1. 得到 parentEntityClass = A, entityClass = b, field = f1.
                 2. 调用当前被影响的字段计算类型相应的计算逻辑,计算出当前元信息被影响的实例id列表.
                 3. 结合缓存加载出所有受影响的对象实例列表.
                 4. 对每一个实例都应用字段的相应计算.
             */
            if (logger.isDebugEnabled()) {
                logger.debug("Maintain computed fields, whose impact tree is as follows.");
                logger.debug(infuence.toString());
            }

            infuence.scan((parentParticipant, participant, infuenceInner) -> {
                if (!parentParticipant.isPresent()) {
                    // 发起源不需要维护.
                    return InfuenceConsumer.Action.CONTINUE;
                }
                CalculationLogic logic =
                    calculationLogicFactory.getCalculationLogic(participant.getField().calculationType());

                Timer.Sample sample = Timer.start(Metrics.globalRegistry);

                long[] affectedEntityIds = null;
                try {
                    affectedEntityIds = logic.getMaintainTarget(context, participant,
                        parentParticipant.get().getAffectedEntities());
                } catch (CalculationException ex) {
                    processTimer(
                        logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "getTarget", false);
                    throw ex;
                }

                processTimer(
                    logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "getTarget", false);

                Collection<IEntity> affectedEntities = loadEntities(context, affectedEntityIds);

                // 重新计算影响的entity.
                for (IEntity affectedEntitiy : affectedEntities) {
                    context.focusEntity(affectedEntitiy, participant.getEntityClass());
                    context.focusField(participant.getField());

                    Optional<IValue> oldValueOp = affectedEntitiy.entityValue().getValue(participant.getField().id());

                    if (logger.isDebugEnabled()) {
                        logger.debug("Start using {} logic to compute instance {} fields {} of type {}.",
                            logic.getClass().getSimpleName(),
                            affectedEntitiy.id(),
                            participant.getField().name(),
                            participant.getEntityClass().code());
                    }

                    context.startMaintenance();
                    Optional<IValue> newValueOp = logic.calculate(context);
                    context.stopMaintenance();

                    if (newValueOp.isPresent()) {

                        IValue oldValue =
                            oldValueOp.isPresent() ? oldValueOp.get() : new EmptyTypedValue(participant.getField());
                        IValue newValue = newValueOp.get();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Instance {} field {} evaluates to {}.",
                                affectedEntitiy.id(), participant.getField().name(), newValueOp.get().getValue());
                        }

                        if (!oldValue.equals(newValue)) {
                            context.addValueChange(ValueChange.build(affectedEntitiy.id(), oldValue, newValue));

                            affectedEntitiy.entityValue().addValue(newValueOp.get());
                        } else {

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Calculate field {}, the result is the same before and after calculation so do not change.",
                                    context.getFocusField().name());
                            }

                            // 没有任何改变,所有受此字段影响的后续都将被忽略.
                            return InfuenceConsumer.Action.OVER_SELF;

                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Instance {} field {} evaluates to {}.",
                                affectedEntitiy.id(), participant.getField().name(), "NULL");
                        }
                    }
                }
                // 加入到当前参与者的影响entity记录中.
                affectedEntities.forEach(e -> participant.addAffectedEntity(e));

                return InfuenceConsumer.Action.CONTINUE;
            });

            persist(context, targetEntityId);
        }
    }

    /**
     * 持久化当前上下文缓存的实例. 持久化会造成和更新失败,失败策略如下. 1. 数据被删除,放弃. 2. 数据版本冲突,重试直到成功.(有上限)
     */
    private void persist(CalculationContext context, long targetEntityId) throws CalculationException {

        List<IEntity> entities = context.getEntitiesFormCache().stream().filter(e ->
            // 过滤掉当前操作的实例.
            e.id() != targetEntityId

        ).collect(Collectors.toList());

        if (entities.isEmpty()) {
            return;
        }

        final int batchSize = 1000;

        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());

        EntityPackage entityPackage = new EntityPackage();
        boolean persistResult;
        for (int i = 0; i < entities.size(); i++) {
            if (entityPackage.size() == batchSize) {


                try {
                    persistResult = doPersist(context, entityPackage);
                } catch (SQLException ex) {
                    throw new CalculationException(ex.getMessage(), ex);
                }

                if (!persistResult) {
                    throw new CalculationException(
                        "An error occurred during maintenance and the number of conflicts reached the upper limit. Procedure");
                }

                entityPackage = null;

            } else {

                if (entityPackage == null) {
                    entityPackage = new EntityPackage();
                }

                Optional<IEntityClass> entityClassOp = metaManager.load(entities.get(i).entityClassRef());
                if (!entityClassOp.isPresent()) {
                    throw new CalculationException(
                        String.format("Not found entityClass.[%s]", entities.get(i).entityClassRef().getId()));
                }
                entityPackage.put(entities.get(i), entityClassOp.get());

            }
        }

        if (entityPackage != null && !entityPackage.isEmpty()) {
            try {
                doPersist(context, entityPackage);
            } catch (SQLException ex) {
                throw new CalculationException(ex.getMessage(), ex);
            }
        }
    }

    private boolean doPersist(CalculationContext context, EntityPackage entityPackage) throws SQLException {
        MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());

        EntityPackage replayPackage = entityPackage;
        for (int p = 0; p < MAX_REPLAY_NUMBER; p++) {

            int[] results = masterStorage.replace(replayPackage);
            EntityPackage finalReplayPackage = replayPackage;
            long[] errEntityIds = IntStream.range(0, results.length)
                .filter(i -> results[i] == 0)
                .mapToLong(i -> finalReplayPackage.get(i).get().getKey().id()).toArray();

            if (errEntityIds.length > 0) {

                Collection<IEntity> replyEntityes = masterStorage.selectMultiple(errEntityIds);

                if (replyEntityes.isEmpty()) {
                    // 目标数据已经不存在,被其他事务删除,放弃操作.
                    return true;
                }

                replyEntityes.forEach(e -> {
                    IEntityClass entityClass = metaManager.load(e.entityClassRef()).get();
                    for (IEntityField field : entityClass.fields()) {
                        Optional<ValueChange> vcOp = context.getValueChange(e, field);

                        if (!vcOp.isPresent()) {
                            continue;
                        } else {
                            ValueChange vc = vcOp.get();
                            Optional<IValue> valueOp = vc.getNewValue();
                            if (!valueOp.isPresent()) {
                                continue;
                            }
                            IValue newValue = valueOp.get();
                            if (EmptyTypedValue.class.isInstance(newValue)) {
                                e.entityValue().remove(field);
                            } else {
                                e.entityValue().addValue(newValue);
                            }
                        }
                    }
                });

                if (logger.isDebugEnabled()) {
                    logger.debug("Maintenance update instance conflict, wait 30 ms and try again.[{}/{}]",
                        p + 1, MAX_REPLAY_NUMBER);
                }

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(30L));

                replayPackage = new EntityPackage();
                for (IEntity replyEntity : replyEntityes) {
                    replayPackage.put(replyEntity, metaManager.load(replyEntity.entityClassRef()).get());
                }

            } else {
                return true;
            }
        }

        return false;
    }

    // 加载实体,缓存+储存.
    private Collection<IEntity> loadEntities(CalculationContext context, long[] ids)
        throws CalculationException {

        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Load instance. Identity list [{}]", Arrays.toString(ids));
        }

        // 过滤掉缓存中已经存在的.
        long[] notCacheIds = Arrays.stream(ids).filter(id -> !context.getEntityToCache(id).isPresent()).toArray();

        if (notCacheIds.length > 0) {

            MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());

            // 加载缓存中不存在的.
            Collection<IEntity> entities;
            try {
                entities = masterStorage.selectMultiple(notCacheIds);
            } catch (SQLException e) {
                throw new CalculationException(e.getMessage(), e);
            }

            entities.forEach(e -> context.putEntityToCache(e));
        }

        // 从缓存中加载出目标实例.
        return Arrays.stream(ids)
            .mapToObj(id -> context.getEntityToCache(id))
            .filter(op -> op.isPresent())
            .map(op -> op.get()).collect(Collectors.toList());
    }

    // 获取影响树列表.
    private Infuence[] scope(CalculationContext context) throws CalculationException {
        // 得到按优先级排序好的计算字段.并且过滤只处理改变的字段.
        Collection<IEntityField> calculationFields = parseChangeFields(context, false);

        // 固定增加一个表示数量改变的特殊改变.
        switch (context.getScenariso()) {
            case BUILD: {
                context.addValueChange(
                    ValueChange.build(
                        context.getFocusEntity().id(),
                        new EmptyTypedValue(EntityField.ID_ENTITY_FIELD),
                        new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id())
                    )
                );
                calculationFields.add(EntityField.ID_ENTITY_FIELD);
                break;
            }
            case DELETE: {

                context.addValueChange(
                    ValueChange.build(
                        context.getFocusEntity().id(),
                        new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id()),
                        new EmptyTypedValue(EntityField.ID_ENTITY_FIELD)
                    )
                );
                calculationFields.add(EntityField.ID_ENTITY_FIELD);
                break;
            }
            case REPLACE: {
                LongValue value = new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id());
                context.addValueChange(ValueChange.build(context.getFocusEntity().id(), value, value));
                calculationFields.add(EntityField.ID_ENTITY_FIELD);
                break;
            }
            default: {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The current scenario {} does not affect the number of instances and does not add a "
                            + "variable number of influence trees.",
                        context.getScenariso().name());
                }
            }
        }

        CalculationLogicFactory calculationLogicFactory =
            context.getResourceWithEx(() -> context.getCalculationLogicFactory());

        /*
        所有当前场景需要维护的计算逻辑片根据改变的字段最终汇聚一份影响树列表.
        每一个改变的字段对应着一棵树.树的结构如下.
                 当前被改变的实例和改变的字段信息
                    /             \
             被影响的元信息和字段   被影响的元信息和字段
               /           \
       被影响的元信息和字段   被影响的元信息和字段
       注意: 每一个改变字段会由所有logic共同来构造一颗影响树.
                               A
                           /       \
                      B(lookup)     C
                         /
                       D(sum)
             这里 B lookup 和A,但是D又SUM了B,所以需要多个logic来共同构造这个树.
         */

        // 只处理需要处理当前维护场景的logic.
        Collection<CalculationLogic> logics =
            getNeedMaintainScenariosLogic(calculationLogicFactory, context.getScenariso());

        Infuence[] infuences = calculationFields.stream().map(f -> {

            Optional<ValueChange> changeOp = context.getValueChange(context.getFocusEntity(), f);
            // 表示数量改变的id字段.
            if (changeOp.isPresent()) {
                Infuence infuence = new Infuence(
                    context.getFocusEntity(),
                    CalculationParticipant.Builder.anParticipant()
                        .withEntityClass(context.getFocusClass())
                        .withField(f)
                        .withAffectedEntities(Arrays.asList(
                            context.getFocusEntity()
                        ))
                        .build(),
                    changeOp.get());

                /*
                不断将影响树交由所有相关的logic构建,只到影响树不再改变或者达到 MAX_BUILD_INFUENCE_NUMBER 上限.
                 */
                int oldSize = infuence.getSize();
                for (int i = 0; i < MAX_BUILD_INFUENCE_NUMBER; i++) {
                    for (CalculationLogic logic : logics) {
                        context.focusField(f);

                        Timer.Sample sample = Timer.start(Metrics.globalRegistry);

                        logic.scope(context, infuence);

                        processTimer(
                            logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "scope", false);
                    }

                    if (oldSize == infuence.getSize()) {
                        break;
                    } else {
                        oldSize = infuence.getSize();
                    }
                }

                if (infuence.empty()) {
                    return Optional.empty();
                } else {
                    return Optional.ofNullable(infuence);
                }
            }

            return Optional.empty();
        }).filter(o -> o.isPresent()).map(o -> o.get()).toArray(Infuence[]::new);

        return infuences;
    }

    /**
     * 解析当前的需要关注的字段.
     *
     * @param context              上下文.
     * @param onlyCalculationField true 只处理计算字段,false 所有字段.
     * @return 相关字段列表.
     */
    private Collection<IEntityField> parseChangeFields(CalculationContext context, boolean onlyCalculationField) {
        IEntityClass entityClass = context.getFocusClass();

        Collection<IEntityField> fields;
        fields = entityClass.fields().stream().filter(f -> {
            /*
            如果只处理计算字段, 那么所有优先级小于0的将被过滤.
            如果处理所有字段,那么只有计算类型未知的才会被过滤.
             */
            if (onlyCalculationField) {
                return f.calculationType().getPriority() > (byte) 0;
            } else {
                return f.calculationType() != CalculationType.UNKNOWN;
            }
        }).filter(f -> {
            /*
               如果字段的计算类型定义即使用没有改变也需要被计算,否则判断是否发生改变.
            */
            CalculationScenarios scenarios = context.getScenariso();
            CalculationType calculationType = f.calculationType();
            switch (scenarios) {
                case BUILD: {
                    if (calculationType.isBuildNeedNotChange()) {
                        return true;
                    } else {
                        break;
                    }
                }
                case REPLACE: {
                    if (calculationType.isReplaceNeedNotChange()) {
                        return true;
                    } else {
                        break;
                    }
                }
                case DELETE: {
                    if (calculationType.isDeleteNeedNotChange()) {
                        return true;
                    } else {
                        break;
                    }
                }
                default:
                    return false;
            }

            return context.getValueChange(context.getFocusEntity(), f).isPresent();
        }).sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            if (onlyCalculationField) {
                logger.debug(
                    "Only the fields need to be computed, and the field currently being changed is [{}].",
                    fields.stream().map(f -> f.name()).collect(Collectors.joining(", "))
                );
            } else {
                logger.debug(
                    "All fields are required, and the field currently being changed is [{}].",
                    fields.stream().map(f -> f.name()).collect(Collectors.joining(", "))
                );
            }
        }

        return fields;
    }

    // 得到需要在当前场景下维护的logic.
    private Collection<CalculationLogic> getNeedMaintainScenariosLogic(
        CalculationLogicFactory calculationLogicFactory, CalculationScenarios currentScenarios) {

        Collection<CalculationLogic> logics = calculationLogicFactory.getCalculationLogics().stream().filter(l -> {
            for (CalculationScenarios scenarios : l.needMaintenanceScenarios()) {
                if (scenarios == currentScenarios) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            logger.debug(
                "The current scenario is {}, and the logic to be maintained is [{}].",
                currentScenarios.name(),
                logics.stream().map(l -> l.getClass().getSimpleName()).collect(Collectors.joining(", "))
            );
        }

        return logics;
    }

    // 停止计时并输出指标
    private void processTimer(CalculationLogic logic, Timer.Sample sample, String metricName, String action,
                              boolean ex) {
        sample.stop(Timer.builder(metricName)
            .tags(
                "logic", logic.getClass().getSimpleName(),
                "action", action,
                "exception", ex ? CalculationException.class.getSimpleName() : "none"
            )
            .publishPercentileHistogram(false)
            .publishPercentiles(null)
            .register(Metrics.globalRegistry));
    }
}
