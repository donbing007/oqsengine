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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 计算字段计算器..
 *
 * @author dongbin
 * @version 0.1 2021/09/17 15:40
 * @since 1.8
 */
public class DefaultCalculationImpl implements Calculation {

    @Override
    public IEntity calculate(CalculationContext context) throws CalculationException {
        IEntity targetEntity = context.getFocusEntity();

        // 得到按优先级排序好的计算字段.并且过滤只处理改变的字段.
        Collection<IEntityField> calculationFields = parseChangeFields(context, true);
        // 计算逻辑工厂.
        CalculationLogicFactory calculationLogicFactory = context.getCalculationLogicFactory();

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

        Map<Long, IEntity> cache = new HashMap<>();

        CalculationLogicFactory calculationLogicsFactory = context.getCalculationLogicFactory();
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
            infuence.scan((parentEntityClass, entityClass, field, infuenceInner) -> {
                CalculationLogic logic = calculationLogicsFactory.getCalculationLogic(field.calculationType());

                long[] affectedEntityIds = logic.getMaintainTarget(context, entityClass, field, triggerEntities);

                Collection<IEntity> affectedEntities = loadEntities(context, affectedEntityIds);

                for (IEntity affectedEntitiy : affectedEntities) {
                    context.focusEntity(affectedEntitiy, entityClass);
                    context.focusField(field);

                    Optional<IValue> newValueOp = logic.calculate(context);
                    Optional<IValue> oldValueOp = affectedEntitiy.entityValue().getValue(field.id());

                    if (newValueOp.isPresent()) {

                        IValue oldValue = oldValueOp.isPresent() ? oldValueOp.get() : new EmptyTypedValue(field);
                        IValue newValue = newValueOp.get();

                        context.addValueChange(ValueChange.build(affectedEntitiy.id(), oldValue, newValue));

                        affectedEntitiy.entityValue().addValue(newValueOp.get());
                    }
                }

                return true;
            });


            //缓存中的实例entityClass分类
            Map<EntityClassRef, List<IEntity>> entitiesGroup =
                context.getEntitiesFormCache().stream().collect(Collectors.groupingBy(IEntity::entityClassRef));

            Optional<IEntityClass> entityClassOp;
            IEntityClass entityClass;
            EntityPackage entityPackage;
            List<IEntity> classGroupEntities;

            MetaManager metaManager = context.getResourceWithEx(() -> context.getMetaManager());
            MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
            for (EntityClassRef ref : entitiesGroup.keySet()) {
                entityClassOp = metaManager.load(ref);
                if (!entityClassOp.isPresent()) {
                    throw new CalculationException(String.format("Meta information (%s) does not exist.", ref));
                }

                entityClass = entityClassOp.get();

                classGroupEntities = entitiesGroup.get(ref);
                entityPackage = new EntityPackage(entityClass);
                if (classGroupEntities != null) {
                    classGroupEntities.forEach(e -> entityPackage.put());
                }
            }


        }

    }

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

    private Infuence[] scope(CalculationContext context) throws CalculationException {
        // 得到按优先级排序好的计算字段.并且过滤只处理改变的字段.
        Collection<IEntityField> calculationFields = parseChangeFields(context, false);
        CalculationLogicFactory calculationLogicFactory = context.getCalculationLogicFactory();

        /*
        所有当前场景需要维护的计算逻辑片根据改变的字段最终汇聚一份影响树列表.
        每一个改变的字段对应着一棵树.树的结构如下.
                 当前被改变的实例和改变的字段信息
                    /             \
             被影响的元信息和字段   被影响的元信息和字段
               /           \
       被影响的元信息和字段   被影响的元信息和字段
         */
        CalculationScenarios current = context.getScenariso();

        Infuence[] infuences = calculationFields.stream().map(f -> {
            CalculationLogic logic = calculationLogicFactory.getCalculationLogic(f.calculationType());
            if (isSupportScenarios(current, logic)) {
                context.focusField(f);

                Optional<ValueChange> changeOp = context.getValueChange(context.getFocusEntity(), f);
                if (changeOp.isPresent()) {
                    Infuence infuence = new Infuence(
                        context.getFocusEntity(), context.getFocusClass(), changeOp.get());
                    logic.scope(context, infuence);
                    return Optional.of(infuence);
                }
            }
            return Optional.empty();
        }).filter(o -> o.isPresent()).map(o -> o.get()).toArray(Infuence[]::new);

        /*
        将得到的影响树再次交由所有logic处理,处理有可能的级连影响.
                    A           t1
                  /   \
                 B     C
        由于A的改变造成的影响树如上,但是现在有一个E对象依赖B的改变,这时候需要将E也增加到树上.
                   A            t2
                 /   \
                B     C
               /
              E
         所以会将首次构造的影响树,即t1会交由所有的logic进行处理,其中有可能某个logic会将t1修改为t2.
         这里会跳过生成t1树的那个logic,只交由没有处理过t1树的logic处理.
         */
        Collection<CalculationLogic> logics = calculationLogicFactory.getCalculationLogics();

        for (Infuence infuence : infuences) {

            for (CalculationLogic logic : logics) {

                if (logic.supportType() != infuence.getValueChange().getField().calculationType()) {
                    logic.scope(context, infuence);
                }
            }
        }
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
        if (onlyCalculationField) {
            return context.getValueChanges().stream().map(v -> v.getField()).filter(f ->
                    f.calculationType() != CalculationType.STATIC && f.calculationType() != CalculationType.UNKNOWN
                )
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());
        } else {
            return context.getValueChanges().stream().map(v -> v.getField())
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());
        }
    }

    // 判断当前计算逻辑片是否需要在当前场景下维护.
    private boolean isSupportScenarios(CalculationScenarios current, CalculationLogic logic) {
        CalculationScenarios[] scenarioses = logic.needMaintenanceScenarios();
        for (CalculationScenarios scenarios : scenarioses) {
            if (current == scenarios) {
                return true;
            }
        }

        return false;
    }
}
