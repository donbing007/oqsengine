package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.kv.KeyIterator;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
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

    @Resource
    private KeyValueStorage kv;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private MetaManager metaManager;

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
        LookupMaintainingTask lookupMaintainingTask = (LookupMaintainingTask) task;

        /*
        得到此次任务需要处理的批次key列表.
         */
        KeyIterator keyIterator = kv.iterator(lookupMaintainingTask.getIterKey());
        if (lookupMaintainingTask.getPointKey().isPresent()) {
            keyIterator.seek(lookupMaintainingTask.getPointKey().get());
        }
        // 已经处理的实例数量.
        int point = 0;
        List<LookupHelper.LookupLinkKey> linkKeys = new ArrayList<>(lookupMaintainingTask.getMaxSize());
        /*
        迭代目标key,这里保证了每一次的任务都只处理同一个lookup的同一个lookup字段.
         */
        long targetEntityId = 0;
        long targetFieldId = 0;
        long lookupFieldId = 0;
        long lookupEntityClassId = 0;
        while (keyIterator.hasNext()) {
            point++;

            if (point > lookupMaintainingTask.getMaxSize()) {
                break;
            }
            LookupHelper.LookupLinkKey key = LookupHelper.parseLinkKey(keyIterator.next());
            linkKeys.add(key);

            if (targetEntityId == 0) {
                targetEntityId = key.getTargetEntityId();
            }

            if (targetFieldId == 0) {
                targetFieldId = key.getTargetFieldId();
            }

            if (lookupFieldId == 0) {
                lookupFieldId = key.getLookupFieldId();
            }

            if (lookupEntityClassId == 0) {
                lookupEntityClassId = key.getLookupClassId();
            }
        }

        /*
         * 目标必须存在.
         */
        if (targetEntityId > 0) {
            if (updateLookupEntity(targetEntityId, targetFieldId, lookupEntityClassId, lookupFieldId, linkKeys)) {
                // 如果还有剩余数据,增加下一批执行数据.
                if (keyIterator.hasNext()) {

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "lookup-update-continue : target [id({}), field({})] - lookup [entityClass({}), field({})].",
                            targetEntityId,
                            targetFieldId,
                            lookupEntityClassId,
                            lookupFieldId
                        );
                    }

                    String seekKey = keyIterator.currentKey();
                    coordinator.addTask(new LookupMaintainingTask(lookupMaintainingTask.getIterKey(), seekKey));
                }
            }
        }

    }

    /**
     * 更新lookup实例.
     *
     * @param targetEntityId      目标实例id.
     * @param targetFieldId       目标字段id.
     * @param lookupEntityClassId lookup类型id.
     * @param lookupFieldId       lookup字段id.
     * @param linkKeys            连接key列表.
     * @return true, 需要继续, false不需要继续.
     */
    private boolean updateLookupEntity(
        long targetEntityId,
        long targetFieldId,
        long lookupEntityClassId,
        long lookupFieldId,
        List<LookupHelper.LookupLinkKey> linkKeys) {
        try {
            long[] entityIds = LongStream.concat(
                LongStream.of(targetEntityId), linkKeys.stream().mapToLong(l -> l.getLookupEntityId())).toArray();

            Collection<IEntity> entities = masterStorage.selectMultiple(entityIds);

            /*
              查询得到目标实体和lookup实体.
            */
            long finalTargetEntityId = targetEntityId;
            Optional<IEntity> targetEntityOp =
                entities.stream().filter(e -> e.id() == finalTargetEntityId).findFirst();
            if (!targetEntityOp.isPresent()) {
                // 目标对象没有找到,任务结束.
                logger.warn(
                    "The value of the lookup field is recalculated, but the target instance ({}) cannot be found.",
                    targetEntityId);
                return false;
            } else {
                IEntity targetEntity = targetEntityOp.get();
                Collection<IEntity> lookupEntities =
                    entities.stream().filter(e -> e.id() != finalTargetEntityId).collect(
                        Collectors.toList());

                Optional<IEntityClass> lookupEntityClassOp = metaManager.load(lookupEntityClassId);
                if (!lookupEntityClassOp.isPresent()) {
                    logger.warn("The meta information ({}) of the lookup initiator could not be found.",
                        lookupEntityClassId);
                    return false;
                }

                IEntityClass lookupClass = lookupEntityClassOp.get();
                Optional<IEntityField> lookupFieldOp = lookupClass.field(lookupFieldId);
                if (!lookupFieldOp.isPresent()) {
                    logger.warn(
                        "The field ({}) in the meta information ({}) of the lookup initiator could not be found.",
                        lookupFieldId, lookupEntityClassId);
                    return false;
                }
                IEntityField lookupField = lookupFieldOp.get();

                Optional<IValue> targetValueOp = targetEntity.entityValue().getValue(targetFieldId);
                if (!targetValueOp.isPresent()) {
                    for (IEntity lookupEntity : lookupEntities) {
                        lookupEntity.entityValue().remove(lookupField);
                    }
                } else {
                    for (IEntity lookupEntity : lookupEntities) {
                        lookupEntity.entityValue().addValue(targetValueOp.get().copy(lookupField));
                    }
                }

                IEntity updateLookupEntity;
                for (IEntity lookupEntity : lookupEntities) {
                    updateLookupEntity = lookupEntity;
                    // 不断重试直到成功.
                    while (masterStorage.replace(updateLookupEntity, lookupClass) == 0) {

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                "lookup-update-failure : target [id({}), entityClass({}), field({}), value{{})] - lookup [id({}), entityClass({}), field({})].",
                                targetEntityId,
                                targetEntity.entityClassRef().getId(),
                                targetFieldId,
                                targetValueOp.get().getValue().toString(),
                                lookupEntity.id(),
                                lookupEntityClassId,
                                lookupFieldId
                            );
                        }

                        Optional<IEntity> op = masterStorage.selectOne(updateLookupEntity.id());
                        if (!op.isPresent()) {
                            // 实例已经被删除,跳过.
                            break;
                        } else {
                            updateLookupEntity = op.get();
                        }
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(30L));
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "lookup-update-success : target [id({}), entityClass({}), field({}), value{{})] - lookup [id({}), entityClass({}), field({})].",
                            targetEntityId,
                            targetEntity.entityClassRef().getId(),
                            targetFieldId,
                            targetValueOp.get().getValue().toString(),
                            lookupEntity.id(),
                            lookupEntityClassId,
                            lookupFieldId
                        );
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return true;
    }
}