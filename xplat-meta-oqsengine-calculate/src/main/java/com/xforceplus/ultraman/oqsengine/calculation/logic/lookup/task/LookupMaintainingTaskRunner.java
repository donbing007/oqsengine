package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.utils.LookupEntityIterator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lookup维护运行者.
 *
 * @author dongbin
 * @version 0.1 2021/08/16 14:54
 * @since 1.8
 */
public class LookupMaintainingTaskRunner implements TaskRunner {

    final Logger logger = LoggerFactory.getLogger(LookupMaintainingTaskRunner.class);

    final int entityIterBuffer = 1000;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private MetaManager metaManager;

    @Resource(name = "combinedSelectStorage")
    private ConditionsSelectStorage conditionsSelectStorage;

    @Resource(name = "serviceTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    private boolean withTx;

    /**
     * 默认运行时启用事务.
     */
    public LookupMaintainingTaskRunner() {
        this(true);
    }

    /**
     * 是否以事务方式执行.
     *
     * @param withTx true 启用,false 不启用.
     */
    public LookupMaintainingTaskRunner(boolean withTx) {
        this.withTx = withTx;
    }

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
        LookupMaintainingTask lookupMaintainingTask = (LookupMaintainingTask) task;

        Optional<IEntityClass> lookupEntityClassOp = metaManager.load(lookupMaintainingTask.getLookupClassRef());
        if (!lookupEntityClassOp.isPresent()) {
            logger.warn("The type {} that initiated the Lookup could not be found.",
                lookupMaintainingTask.getLookupClassRef());
            return;
        }

        IEntityClass lookupEntityClass = lookupEntityClassOp.get();
        Optional<IEntityField> lookupFieldOp = lookupEntityClass.field(lookupMaintainingTask.getLookupFieldId());
        if (!lookupFieldOp.isPresent()) {
            logger.warn("The field ({}) that initiated the lookup could not be found in the meta-information ({}-{}).",
                lookupMaintainingTask.getLookupFieldId(), lookupEntityClass.id(), lookupEntityClass.code());
            return;
        }
        IEntityField lookupField = lookupFieldOp.get();

        /*
        构造由于targetEntity字段修改造成的发起改变的对象迭代器.
         */
        LookupEntityIterator lookupEntityIterator =
            new LookupEntityIterator(entityIterBuffer, lookupMaintainingTask.getMaxSize());
        lookupEntityIterator.setCombinedSelectStorage(conditionsSelectStorage);
        lookupEntityIterator.setMasterStorage(masterStorage);
        lookupEntityIterator.setEntityClass(lookupEntityClass);
        lookupEntityIterator.setField(lookupField);
        lookupEntityIterator.setTargetEntityId(lookupMaintainingTask.getTargetEntityId());
        lookupEntityIterator.setStartId(lookupMaintainingTask.getLastStartLookupEntityId());

        Optional<IValue> targetValueOp = findTargetValue(lookupMaintainingTask);

        final int bufferSize = 1000;
        List<IEntity> lookupEntities = new ArrayList<>(bufferSize);

        for (int i = 0; i < lookupMaintainingTask.getMaxSize(); i++) {
            if (lookupEntityIterator.hasNext()) {
                lookupEntities.add(lookupEntityIterator.next());
            } else {
                break;
            }
        }

        try {
            // 进行一次更新
            if (withTx) {
                transactionExecutor.execute((transaction, resource, hint) -> {
                    adjustLookupEntities(
                        lookupMaintainingTask,
                        lookupEntities,
                        lookupEntityClass,
                        lookupField,
                        targetValueOp,
                        lookupMaintainingTask.getTargetEntityId());
                    return null;
                });
            } else {

                adjustLookupEntities(
                    lookupMaintainingTask,
                    lookupEntities,
                    lookupEntityClass,
                    lookupField,
                    targetValueOp,
                    lookupMaintainingTask.getTargetEntityId());
            }
        } catch (Exception ex) {
            // 重新加入任务队列进行计算.
            logger.error(ex.getMessage(), ex);
            coordinator.addTask(task);
            return;
        }

        // 如果还有可迭代的数据,只是受限于失代上限限制造成的结束.
        if (lookupEntityIterator.more()) {
            coordinator.addTask(
                LookupMaintainingTask.Builder.anLookupMaintainingTask()
                    .withTargetEntityId(lookupMaintainingTask.getTargetEntityId())
                    .withTargetClassRef(lookupMaintainingTask.getTargetClassRef())
                    .withTargetFieldId(lookupMaintainingTask.getTargetFieldId())
                    .withLookupClassRef(lookupMaintainingTask.getLookupClassRef())
                    .withLookupFieldId(lookupMaintainingTask.getLookupFieldId())
                    .withMaxSize(lookupMaintainingTask.getMaxSize())
                    .withLastStartLookupEntityId(lookupEntityIterator.getStartId())
                    .build()
            );
        }

    }

    // 调整lookup实例.
    private void adjustLookupEntities(
        LookupMaintainingTask task,
        List<IEntity> lookupEntities,
        IEntityClass lookupEntityClass,
        IEntityField lookupField,
        Optional<IValue> targetValueOp,
        long targetEntityId) {

        updateLookupEntityesValue(lookupEntities, lookupField, targetValueOp, targetEntityId);

        long[] notSuccessIds = persist(lookupEntities, lookupEntityClass);

        List<IEntity> needReplayEntities = null;
        // 发生错误,进入重试.
        while (notSuccessIds.length > 0) {

            // 等待3秒后重试.
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(3000L));

            Optional<IValue> replayTargetValueOp = findTargetValue(task);

            try {
                needReplayEntities = masterStorage.selectMultiple(notSuccessIds).stream().collect(Collectors.toList());
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                continue;
            }

            if (!needReplayEntities.isEmpty()) {
                updateLookupEntityesValue(needReplayEntities, lookupField, replayTargetValueOp, targetEntityId);
                notSuccessIds = persist(needReplayEntities, lookupEntityClass);
            }
        }
    }

    private Optional<IValue> findTargetValue(LookupMaintainingTask lookupMaintainingTask) {
        Optional<IEntityClass> entityClassOp = metaManager.load(lookupMaintainingTask.getTargetClassRef());
        if (!entityClassOp.isPresent()) {
            return Optional.empty();
        }

        IEntityClass entityClass = entityClassOp.get();

        Optional<IEntity> entityOp;
        try {
            entityOp = masterStorage.selectOne(lookupMaintainingTask.getTargetEntityId(), entityClass);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Optional.empty();
        }

        if (!entityOp.isPresent()) {
            return Optional.empty();
        }

        IEntity entity = entityOp.get();
        return entity.entityValue().getValue(lookupMaintainingTask.getTargetFieldId());
    }

    private long[] persist(List<IEntity> lookupEntities, IEntityClass lookupEntityClass) {
        EntityPackage entityPackage = new EntityPackage();
        for (IEntity lookupEntity : lookupEntities) {
            entityPackage.put(lookupEntity, lookupEntityClass);
        }

        int[] results;
        try {
            results = masterStorage.replace(entityPackage);
        } catch (SQLException e) {
            return lookupEntities.stream().mapToLong(le -> le.id()).toArray();
        }

        return IntStream.range(0, results.length)
            .filter(i -> results[i] < 0)
            .mapToLong(i -> results[i])
            .toArray();
    }

    private void updateLookupEntityesValue(
        List<IEntity> lookupEntities, IEntityField lookupField, Optional<IValue> targetValueOp, long targetEntityId) {

        int len = lookupEntities.size();
        if (!targetValueOp.isPresent()) {
            for (int i = 0; i < len; i++) {
                lookupEntities.get(i).entityValue().remove(lookupField);
            }
        } else {
            IValue targetValue = targetValueOp.get();
            IValue newLookupValue = targetValue.copy(lookupField, Long.toString(targetEntityId));

            for (int i = 0; i < len; i++) {
                lookupEntities.get(i).entityValue().addValue(newLookupValue);
            }
        }
    }


}