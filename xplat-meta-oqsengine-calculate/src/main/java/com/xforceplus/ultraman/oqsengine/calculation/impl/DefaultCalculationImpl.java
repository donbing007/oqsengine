package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraphConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

    /*
     * 每一个字段修改维护构造影响树的最大迭代次数.
     */
    private static int MAX_BUILD_INFUENCE_NUMBER = 1000;

    private final Logger logger = LoggerFactory.getLogger(DefaultCalculationImpl.class);

    @Timed(
        value = MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
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

                throw new CalculationException(
                    String.format("An error occurred in the calculation field (%d-%s) due to %s.",
                        field.id(), field.name(), ex.getMessage()));
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

                // 计算没有结果,清理字段.
                targetEntity.entityValue().remove(context.getFocusField());

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
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"logic", "all", "action", "maintain"}
    )
    @Override
    public void maintain(CalculationContext context) throws CalculationException {

        if (!doMaintain(context)) {

            throw new CalculationException("Conflicts are calculated and the attempt limit is reached. To give up!");

        }
    }

    /**
     * 维护.
     *
     * @param context 上下文.
     */
    private boolean doMaintain(CalculationContext context) throws CalculationException {
        /*
            根据所有影响树得到目标更新实例集合.
            entityClass 表示当前影响树被影响的实例元信息.
            field 表示被影响的字段.
            parentEntityClass 表示传递影响者是谁.

            依赖产生的影响树.
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
        InfuenceGraph graph = scope(context);

        CalculationLogicFactory calculationLogicFactory =
            context.getResourceWithEx(() -> context.getCalculationLogicFactory());

        if (logger.isDebugEnabled()) {
            logger.debug("Maintain computed fields, whose impact graph is as follows.\n{}\n", graph);
        }

        /*
        依赖图的扫描,从0层开始层层迭代.
         */
        graph.scan((parentParticipants, participant, inner) -> {
            // 根参与者不需要计算,跳过.无需计算节点，跳过.
            if (participant.isSource() || participant.isNeedless()) {
                return InfuenceGraphConsumer.Action.CONTINUE;
            }

            /*
            非计算字段的静态字段,直接跳过.其影响实例已经在构造图时被填充.
             */
            if (CalculationType.STATIC == participant.getField().calculationType()) {
                return InfuenceGraphConsumer.Action.CONTINUE;
            }

            /*
             * 如果树中出现当前源头对象参与者,那么直接忽略.
             * 原因是已经在计算阶段处理完成.
             */
            IEntity sourceEntity = context.getSourceEntity();
            IEntityClass sourceEntityClass =
                context.getResourceWithEx(() -> context.getMetaManager()).load(sourceEntity.entityClassRef()).get();

            if (sourceEntityClass.field(participant.getField().id()).isPresent()) {
                // 虽然不再计算,但是需要把影响实例传播.
                collectingImpactInstances(parentParticipants).forEach(e -> participant.addAffectedEntity(e));

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The participant field ({}) belongs to the origin object ({}) and is no longer counted but propagates the impact.",
                        participant.getField().name(), sourceEntityClass.name()
                    );
                }

                return InfuenceGraphConsumer.Action.CONTINUE;
            }

            CalculationLogic logic =
                calculationLogicFactory.getCalculationLogic(participant.getField().calculationType());

            Timer.Sample sample = Timer.start(Metrics.globalRegistry);

            Collection<AffectedInfo> affectedInfos = null;
            try {

                Collection<IEntity> triggerEntities = collectingImpactInstances(parentParticipants);
                affectedInfos = new ArrayList(
                    logic.getMaintainTarget(context, participant, triggerEntities));

                if (logger.isDebugEnabled()) {
                    logger.debug("The participant {} is affected by {} instances by {} instances.",
                        participant, triggerEntities.size(), affectedInfos.size());
                }

            } catch (CalculationException ex) {
                processTimer(
                    logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "getTarget", true);
                throw ex;
            }

            processTimer(
                logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "getTarget", false);

            // 影响实例增加独占锁.
            if (!affectedInfos.isEmpty()) {
                long[] affectedEntityIds = affectedInfos.stream()
                    .filter(a -> {
                        /*
                            一个优化,减少加锁.
                            如果是创建场景同时影响的对象又和源头触发对象一致,那么跳过无需再加锁.
                         */
                        if (CalculationScenarios.BUILD == context.getScenariso()) {
                            if (a.getAffectedEntityId() == context.getSourceEntity().id()) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .mapToLong(a -> a.getAffectedEntityId()).distinct().toArray();
                if (affectedEntityIds.length > 0) {
                    if (!context.tryLocksEntity(affectedEntityIds)) {
                        throw new CalculationException(
                            String.format(
                                "Conflicts are calculated and the attempt limit is reached [%d ms]. To give up!",
                                context.getLockTimeoutMs()));
                    }
                }
            }

            IEntity[] affectedEntities = loadEntities(context, affectedInfos);

            if (logger.isDebugEnabled()) {
                if (affectedEntities.length == 0) {
                    logger.debug("The number of instances affected by the field {} of entityclass {} is 0.",
                        participant.getField().fieldName(),
                        participant.getEntityClass().code()
                    );
                }
            }

            // 重新计算影响的entity.
            for (IEntity affectedEntitiy : affectedEntities) {
                context.focusEntity(affectedEntitiy, participant.getEntityClass());
                context.focusField(participant.getField());

                Optional<IValue> oldValueOp =
                    affectedEntitiy.entityValue().getValue(participant.getField().id());

                if (logger.isDebugEnabled()) {
                    logger.debug("Start using {} logic to compute instance {} fields {} of type {}.",
                        logic.getClass().getSimpleName(),
                        affectedEntitiy.id(),
                        participant.getField().name(),
                        participant.getEntityClass().code());
                }

                AffectedInfo affectedInfo = null;
                for (AffectedInfo a : affectedInfos) {
                    if (a.getAffectedEntityId() == affectedEntitiy.id()) {
                        affectedInfo = a;
                        break;
                    }
                }

                if (affectedInfo == null) {
                    throw new CalculationException(
                        "An unexpected error occurred and the expected instance was not found in the calculation.");
                } else {
                    affectedInfos.remove(affectedInfo);
                }

                context.startMaintenance(affectedInfo.getTriggerEntity());
                Optional<IValue> newValueOp;
                try {
                    newValueOp = logic.calculate(context);
                } catch (CalculationException ex) {

                    logger.error("Maintenance error occurred, the current impact tree is: \n {}.",
                        inner.toString());

                    throw new CalculationException(
                        String.format("An error occurred in the calculation field (%d-%s) due to %s.",
                            participant.getField().id(), participant.getField().name(), ex.getMessage()));
                }
                context.stopMaintenance();

                if (newValueOp.isPresent()) {

                    IValue oldValue =
                        oldValueOp.isPresent() ? oldValueOp.get() : new EmptyTypedValue(participant.getField());
                    IValue newValue = newValueOp.get();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Instance {} field {} evaluates to {}.",
                            affectedEntitiy.id(), participant.getField().name(), newValueOp.get().getValue());
                    }

                    if (!valueEquals(oldValue, newValue)) {
                        context.addValueChange(ValueChange.build(affectedEntitiy.id(), oldValue, newValue));

                        affectedEntitiy.entityValue().addValue(newValueOp.get());

                    } else {

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "Calculate field {}, the result is the same before and after calculation so do not change.",
                                context.getFocusField().name());
                        }

                        // 没有任何改变,所有受此字段影响的后续都将被忽略.
                        return InfuenceGraphConsumer.Action.OVER_SELF;

                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Instance {} field {} evaluates to {}.",
                            affectedEntitiy.id(), participant.getField().name(), "NULL");
                    }
                }
            }
            // 加入到当前参与者的影响entity记录中.
            for (IEntity affectedEntitiy : affectedEntities) {
                participant.addAffectedEntity(affectedEntitiy);
            }

            return InfuenceGraphConsumer.Action.CONTINUE;
        });


        return true;
    }

    // 比较值是否相等.
    private boolean valueEquals(IValue oldValue, IValue newValue) {
        return Objects.equals(oldValue.getValue(), newValue.getValue());
    }

    // 加载实体,缓存+储存.
    private IEntity[] loadEntities(CalculationContext context, Collection<AffectedInfo> affectedInfos)
        throws CalculationException {

        if (affectedInfos.isEmpty()) {
            return new IEntity[0];
        }

        long[] ids = affectedInfos.stream().mapToLong(a -> a.getAffectedEntityId()).toArray();

        if (logger.isDebugEnabled()) {
            logger.debug("Load instance. Identity list [{}]", ids);
        }


        // 过滤掉缓存中已经存在的.
        long[] notCacheIds = Arrays.stream(ids)
            .filter(id -> !context.getEntityToCache(id).isPresent())
            .distinct().toArray();

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

        return Arrays.stream(ids).mapToObj(id -> context.getEntityToCache(id))
            .filter(op -> op.isPresent())
            .map(op -> op.get())
            .toArray(IEntity[]::new);
    }

    // 获取影响树列表.
    private InfuenceGraph scope(CalculationContext context) throws CalculationException {

        Timer.Sample allSample = Timer.start(Metrics.globalRegistry);

        Participant sourceParticipant = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(context.getFocusClass())
            .withField(EntityField.ILLUSORY_FIELD)
            .withAffectedEntities(Arrays.asList(context.getFocusEntity()))
            .build();
        // 指定为影响源参与者.
        sourceParticipant.source();
        InfuenceGraph graph = context.getInfuenceGraph() != null
            ? context.getInfuenceGraph() : new InfuenceGraph(sourceParticipant);

        try {
            // 得到按优先级排序好的计算字段.并且过滤只处理改变的字段.
            Collection<IEntityField> fields = parseChangeFields(context, false);

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
                    fields.add(EntityField.ID_ENTITY_FIELD);
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
                    fields.add(EntityField.ID_ENTITY_FIELD);
                    break;
                }
                case REPLACE: {
                    LongValue value = new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id());
                    context.addValueChange(ValueChange.build(context.getFocusEntity().id(), value, value));
                    fields.add(EntityField.ID_ENTITY_FIELD);
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
            所有当前场景需要维护的计算逻辑片根据改变的字段最终汇聚一份影响图.
            图的结构如下.
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
             这里 B lookup 和A,但是D又SUM了B,所以需要多个logic来共同构造这个图.
            */

            // 只处理需要处理当前维护场景的logic.
            Collection<CalculationLogic> logics =
                getNeedMaintainScenariosLogic(calculationLogicFactory, context.getScenariso());

            // 将所有发生改变的静态字段加入到影响图中的第一层,并且过滤掉没有改变的.
            Collection<IEntityField> changeFields = fields.stream()
                .filter(f -> context.getValueChange(context.getFocusEntity(), f).isPresent())
                .map(f -> {
                    graph.impact(
                        CalculationParticipant.Builder.anParticipant()
                            .withEntityClass(context.getFocusClass())
                            .withField(f)
                            .withAffectedEntities(Arrays.asList(
                                context.getFocusEntity()
                            ))
                            .build()
                    );
                    return f;
                }).collect(Collectors.toList());

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "The current processing scenario is {}, and the field changed by object {} instance {} is {}.",
                    context.getScenariso().name(),
                    context.getFocusClass().name(),
                    context.getFocusEntity().id(),
                    changeFields.stream().map(f -> f.name()).collect(Collectors.joining(",", "[", "]"))
                );
            }

            /*
            不断将影响树交由所有相关的logic构建,只到影响树不再改变或者达到 MAX_BUILD_INFUENCE_NUMBER 上限.
             */
            int oldSize = graph.size();
            for (int i = 0; i < MAX_BUILD_INFUENCE_NUMBER; i++) {
                for (CalculationLogic logic : logics) {
                    Timer.Sample sample = Timer.start(Metrics.globalRegistry);

                    logic.scope(context, graph);

                    processTimer(
                        logic, sample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "scope", false);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("The {} round affects the graph construction, resulting in \n {}.", i + 1, graph);
                }


                if (oldSize != graph.size()) {
                    oldSize = graph.size();
                } else {
                    break;
                }
            }

            return graph;

        } finally {

            processTimer(
                null, allSample, MetricsDefine.CALCULATION_LOGIC_DELAY_LATENCY_SECONDS, "scope", false);

        }
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

    // 收集多个参与者的影响实例, 去掉合并为一个列表.
    private Collection<IEntity> collectingImpactInstances(Collection<Participant> participants) {
        return participants.stream()
            .map(p -> p.getAffectedEntities())
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    // 停止计时并输出指标
    private void processTimer(CalculationLogic logic, Timer.Sample sample, String metricName, String action,
                              boolean ex) {
        sample.stop(Timer.builder(metricName)
            .tags(
                "logic", logic != null ? logic.getClass().getSimpleName() : "all",
                "action", action,
                "exception", ex ? CalculationException.class.getSimpleName() : "none"
            )
            .publishPercentileHistogram(false)
            .publishPercentiles(0.5, 0.9, 0.99)
            .register(Metrics.globalRegistry));
    }
}
