package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorCalculateInstance;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ErrorFieldUnit;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationInitException;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;

/**
 * 默认的实例重算器.
 *
 */
public class DefaultCalculationInitInstanceImpl implements CalculationInitInstance {
    @Resource
    private MasterStorage masterStorage;

    @Resource
    private MetaManager metaManager;

    @Resource
    private CalculationInit calculationInit;

    @Override
    public Optional<IEntity> initInstance(Long id, IEntityClass entityClass, boolean force) {
        return initField(id, entityClass, calculateFields(entityClass), force);
    }

    @Override
    public List<IEntity> initInstances(List<Long> ids, IEntityClass entityClass, boolean force, Long limit) {
        return initFields(ids, entityClass, calculateFields(entityClass), force, limit);
    }

    @Override
    public List<IEntity> initFields(List<Long> ids, IEntityClass entityClass, List<IEntityField> fields, boolean force, Long limit) {
        try {
            if (fields.size() <= 0) {
                throw new CalculationInitException("reCalculate fields must exist");
            }
            if (ids.size() > limit) {
                throw new CalculationInitException(String.format("reCalculate entities size can not over %d", limit));
            }
            Collection<IEntity> entities = masterStorage.selectMultiple(ids.stream().mapToLong(t -> t).toArray(), entityClass);
            if (entities.isEmpty()) {
                throw new CalculationInitException(String.format("instance not found by ids: %s",
                        Arrays.toString(ids.stream().mapToLong(t -> t).toArray())));
            }
            List<InitInstance> initInstances = buildMultiInstance((List<IEntity>) entities, entityClass, fields);
            return calculationInit.init(initInstances);
        } catch (SQLException e) {
            throw new CalculationInitException(e);
        }
    }


    @Override
    public Optional<IEntity> initField(Long id, IEntityClass entityClass, List<IEntityField> fields, boolean force) {
        if (fields.size() <= 0) {
            throw new CalculationInitException("reCalculate fields must exist");
        }
        // 当前重算实例
        try {
            Optional<IEntity> entity = masterStorage.selectOne(id, entityClass);
            if (!entity.isPresent()) {
                throw new CalculationInitException(String.format("instance not found by id: %d", id));
            }
            Optional<InitInstance> initInstance = buildInstance(entity.get(), entityClass, fields);

            return initInstance.map(instance -> calculationInit.init(instance));

        } catch (SQLException e) {
            throw new CalculationInitException(e);
        }
    }

    @Override
    public List<ErrorCalculateInstance> initCheckFields(List<Long> ids, IEntityClass entityClass, List<IEntityField> fields, Long limit) {
        try {
            if (fields.size() <= 0) {
                throw new CalculationInitException("reCalculate fields must exist");
            }
            if (ids.size() > limit) {
                throw new CalculationInitException(String.format("dryRun entities size can not over %d", limit));
            }
            Collection<IEntity> entities = masterStorage.selectMultiple(ids.stream().mapToLong(t -> t).toArray(), entityClass);
            ArrayList<ErrorCalculateInstance> errorCalculateInstances = new ArrayList<>();
            for (IEntity entity : entities) {
                Optional<ErrorCalculateInstance> errorCalculateInstance = doCheckField(entity, entityClass, fields);
                errorCalculateInstance.ifPresent(errorCalculateInstances::add);
            }
            return errorCalculateInstances;
        } catch (SQLException e) {
            throw new CalculationInitException(e);
        }

    }

    @Override
    public Optional<ErrorCalculateInstance> initCheckField(Long id, IEntityClass entityClass, List<IEntityField> fields) {
        if (fields.size() <= 0) {
            throw new CalculationInitException("reCalculate fields must exist");
        }
        // 当前重算实例
        try {
            Optional<IEntity> entity = masterStorage.selectOne(id, entityClass);
            if (!entity.isPresent()) {
                throw new CalculationInitException(String.format("instance not found by id: %d", id));
            }

            return doCheckField(entity.get(), entityClass, fields);

        } catch (SQLException e) {
            throw new CalculationInitException(e);
        }
    }

    private Optional<ErrorCalculateInstance> doCheckField(IEntity entity, IEntityClass entityClass, List<IEntityField> fields) {
        Optional<InitInstance> initInstance = buildInstance(entity.copy(), entityClass, fields);
        if (!initInstance.isPresent()) {
            return Optional.empty();
        }
        IEntity checked = calculationInit.init(initInstance.get());
        IEntityValue expect = checked.entityValue();
        IEntityValue now = entity.entityValue();
        List<ErrorFieldUnit> errorFieldUnits = new ArrayList<>();
        for (IEntityField field : initInstance.get().getInitInstanceUnits().stream().map(InitInstanceUnit::getField).collect(Collectors.toList())) {
            Optional<IValue> expectValue = expect.getValue(field.id());
            Optional<IValue> nowValue = now.getValue(field.id());
            if (!nowValue.isPresent()) {
                expectValue.ifPresent(value ->
                        errorFieldUnits.add(new ErrorFieldUnit(field, null, value)));
            } else {
                if (!expectValue.isPresent()) {
                    errorFieldUnits.add(new ErrorFieldUnit(field, nowValue.get(), null));
                } else {
                    // 新旧值对比
                    if (!nowValue.get().include(expectValue.get())) {
                        errorFieldUnits.add(new ErrorFieldUnit(field, nowValue.get(), expectValue.get()));
                    }
                }
            }
        }

        return errorFieldUnits.isEmpty() ? Optional.empty() : Optional.of(new ErrorCalculateInstance(entity.id(), errorFieldUnits));
    }

    @Override
    public Optional<ErrorCalculateInstance> initCheckInstance(Long id, IEntityClass entityClass) {

        return initCheckField(id, entityClass, calculateFields(entityClass));
    }

    @Override
    public List<ErrorCalculateInstance> initCheckInstances(List<Long> ids, IEntityClass entityClass, Long limit) {
        return initCheckFields(ids, entityClass, calculateFields(entityClass), limit);
    }


    private Optional<InitInstance> buildInstance(IEntity entity, IEntityClass entityClass, List<IEntityField> fields) {
        // 计算字段排序
        List<IEntityField> sortedFields = fields.stream().filter(field -> field.calculationType().equals(CalculationType.FORMULA)
                        || field.calculationType().equals(CalculationType.AGGREGATION))
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        // 最小重算单元排序
        List<InitInstanceUnit> initInstanceUnits = sortedFields.stream()
                .map(field -> new InitInstanceUnit(entity, entityClass, field))
                .collect(Collectors.toList());

        // 构建重算实例单元
        return initInstanceUnits.isEmpty() ? Optional.empty() : Optional.of(new InitInstance(entity, entityClass, initInstanceUnits));
    }


    private List<InitInstance> buildMultiInstance(List<IEntity> entities, IEntityClass entityClass, List<IEntityField> fields) {
        // 计算字段排序
        List<IEntityField> sortedFields = fields.stream().filter(field -> field.calculationType().equals(CalculationType.FORMULA)
                        || field.calculationType().equals(CalculationType.AGGREGATION))
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        // 构建重算实例单元集合
        List<InitInstance> initInstances = new ArrayList<>();
        for (IEntity entity : entities) {
            if (sortedFields.isEmpty()) {
                continue;
            }
            List<InitInstanceUnit> initInstanceUnits = sortedFields.stream()
                    .map(field -> new InitInstanceUnit(entity, entityClass, field))
                    .collect(Collectors.toList());

            InitInstance initInstance = new InitInstance(entity, entityClass, initInstanceUnits);
            initInstances.add(initInstance);
        }

        return initInstances;
    }


    // 只针对公式、聚合
    private List<IEntityField> calculateFields(IEntityClass entityClass) {
        return entityClass.fields().stream().filter(field -> field.calculationType().equals(CalculationType.FORMULA)
                || field.calculationType().equals(CalculationType.AGGREGATION))
                .collect(Collectors.toList());
    }
}
