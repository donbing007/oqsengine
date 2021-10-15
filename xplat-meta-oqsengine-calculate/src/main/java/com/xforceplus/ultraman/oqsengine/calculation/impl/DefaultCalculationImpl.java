package com.xforceplus.ultraman.oqsengine.calculation.impl;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算字段计算器.
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:40
 * @since 1.8
 */
public class DefaultCalculationImpl implements Calculation {

    /**
     * 如果出现冲突,最大重试的次数.
     */
    private static int MAX_REPLAY_TIMES = 100;

    final Logger logger = LoggerFactory.getLogger(DefaultCalculationImpl.class);

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

            Optional<IValue> newValueOp = logic.calculate(context);
            if (newValueOp.isPresent()) {
                targetEntity.entityValue().addValue(newValueOp.get());
            }
        }

        return targetEntity;
    }

    @Override
    public void maintain(CalculationContext context) throws CalculationException {
        Infuence[] infuences = scope(context);
        CalculationLogicFactory calculationLogicFactory =
            context.getResourceWithEx(() -> context.getCalculationLogicFactory());
        List<IEntity> triggerEntities = new LinkedList();
        for (Infuence infuence : infuences) {
            triggerEntities.clear();

            triggerEntities.add(infuence.getSourceEntity());

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
            infuence.scan((parentEntityClass, participant, infuenceInner) -> {
                CalculationLogic logic =
                    calculationLogicFactory.getCalculationLogic(participant.getField().calculationType());

                long[] affectedEntityIds = logic.getMaintainTarget(context, participant, triggerEntities);

                Collection<IEntity> affectedEntities = loadEntities(context, affectedEntityIds);

                for (IEntity affectedEntitiy : affectedEntities) {
                    context.focusEntity(affectedEntitiy, participant.getEntityClass());
                    context.focusField(participant.getField());

                    Optional<IValue> newValueOp = logic.calculate(context);
                    Optional<IValue> oldValueOp = affectedEntitiy.entityValue().getValue(participant.getField().id());

                    if (newValueOp.isPresent()) {

                        IValue oldValue =
                            oldValueOp.isPresent() ? oldValueOp.get() : new EmptyTypedValue(participant.getField());
                        IValue newValue = newValueOp.get();

                        context.addValueChange(ValueChange.build(affectedEntitiy.id(), oldValue, newValue));

                        affectedEntitiy.entityValue().addValue(newValueOp.get());
                    }
                }

                return true;
            });

            persist(context);
        }
    }

    /**
     * 持久化当前上下文缓存的实例. 持久化会造成和更新失败,失败策略如下. 1. 数据被删除,放弃. 2. 数据版本冲突,重试直到成功.(有上限)
     */
    private void persist(CalculationContext context) throws CalculationException {

        List<IEntity> entities = new ArrayList<>(context.getEntitiesFormCache());
        final int batchSize = 1000;

        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());

        EntityPackage entityPackage = new EntityPackage();
        for (int i = 0; i < entities.size(); i++) {
            if (entityPackage.size() == batchSize) {

                try {
                    doPersist(context, entityPackage);
                } catch (SQLException ex) {
                    throw new CalculationException(ex.getMessage(), ex);
                }

                entityPackage = new EntityPackage();

            } else {
                Optional<IEntityClass> entityClassOp = metaManager.load(entities.get(i).entityClassRef());
                if (!entityClassOp.isPresent()) {
                    throw new CalculationException(
                        String.format("Not found entityClass.[%s]", entities.get(i).entityClassRef().getId()));
                }
                entityPackage.put(entities.get(i), entityClassOp.get());
            }
        }
    }

    private void doPersist(CalculationContext context, EntityPackage entityPackage) throws SQLException {
        MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
        MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());

        EntityPackage replayPackage = entityPackage;
        for (int p = 0; p < MAX_REPLAY_TIMES; p++) {

            int[] results = masterStorage.replace(replayPackage);
            EntityPackage finalReplayPackage = replayPackage;
            long[] errEntityIds = IntStream.range(0, results.length)
                .filter(i -> results[i] == 0)
                .mapToLong(i -> finalReplayPackage.get(i).get().getKey().id()).toArray();

            if (errEntityIds.length > 0) {

                Collection<IEntity> replyEntityes = masterStorage.selectMultiple(errEntityIds);

                if (replyEntityes.isEmpty()) {
                    // 目标数据已经不存在,被其他事务删除,放弃操作.
                    break;
                }
                replayPackage = new EntityPackage();
                for (IEntity replyEntity : replyEntityes) {
                    replayPackage.put(replyEntity, metaManager.load(replyEntity.entityClassRef()).get());
                }

            } else {
                break;
            }
        }
    }

    // 加载实体,缓存+储存.
    private Collection<IEntity> loadEntities(CalculationContext context, long[] ids)
        throws CalculationException {

        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        // 过滤掉缓存中已经存在的.
        long[] notCacheIds = Arrays.stream(ids).filter(id -> !context.getEntityToCache(id).isPresent()).toArray();

        MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());

        // 加载缓存中不存在的.
        Collection<IEntity> entities;
        try {
            entities = masterStorage.selectMultiple(notCacheIds);
        } catch (SQLException e) {
            throw new CalculationException(e.getMessage(), e);
        }

        entities.forEach(e -> context.putEntityToCache(e));

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
        CalculationScenarios currentScenarios = context.getScenariso();

        Collection<CalculationLogic> logics = calculationLogicFactory.getCalculationLogics();

        Infuence[] infuences = calculationFields.stream().map(f -> {

            Optional<ValueChange> changeOp = context.getValueChange(context.getFocusEntity(), f);
            if (changeOp.isPresent()) {
                Infuence infuence = new Infuence(context.getFocusEntity(), context.getFocusClass(), changeOp.get());

                for (CalculationLogic logic : logics) {
                    if (isSupportScenarios(currentScenarios, logic)) {
                        context.focusField(f);

                        logic.scope(context, infuence);
                    }
                }

                return Optional.ofNullable(infuence);
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
        if (onlyCalculationField) {

            fields = entityClass.fields().stream().filter(f ->
                // 只处理计算字段.
                f.calculationType() != CalculationType.STATIC || f.calculationType() != CalculationType.UNKNOWN

            ).filter(f ->
                // 只处理改变的字段.
                context.getValueChange(context.getFocusEntity(), f).isPresent()

            ).sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        } else {

            fields = entityClass.fields().stream().filter(f ->
                // 只处理改变的字段.
                context.getValueChange(context.getFocusEntity(), f).isPresent()

            ).sorted(CalculationComparator.getInstance()).collect(Collectors.toList());
        }

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

    // 判断当前计算逻辑片是否需要在当前场景下维护.
    private boolean isSupportScenarios(CalculationScenarios current, CalculationLogic logic) {
        boolean result = false;
        CalculationScenarios[] scenarioses = logic.needMaintenanceScenarios();
        for (CalculationScenarios scenarios : scenarioses) {
            if (current == scenarios) {
                result = true;
                break;
            }
        }

        if (logger.isDebugEnabled()) {
            if (result) {
                logger.debug(
                    "The current scenario is {}, the target computing logic supports the scenario {}, so the current scenario is supported.",
                    current.name(), Arrays.toString(scenarioses)
                );
            } else {
                logger.debug(
                    "The current scenario is {}. The target computing logic supports {}, so the current scenario is not supported.",
                    current.name(), Arrays.toString(scenarioses)
                );
            }
        }

        return result;
    }
}