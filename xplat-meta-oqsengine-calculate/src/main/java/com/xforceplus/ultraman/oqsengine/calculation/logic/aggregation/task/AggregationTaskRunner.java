package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 聚合任务初始化.
 *
 * @author weikai.
 * @version 1.0 2021/8/27 11:27
 * @since 1.8
 */
public class AggregationTaskRunner implements TaskRunner {
    final Logger logger = LoggerFactory.getLogger(AggregationTaskRunner.class);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private IndexStorage indexStorage;

    @Resource
    private MetaManager metaManager;

    private final long taskStopGapTimeMs = 500L;

    private final int retryCount = 3;

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
        logger.info(String.format("start agg init task: %s", task.toString()));
        // 执行聚合初始化，step 1: 查询被聚合  Step 2: 调用聚合函数  Step 3: 乐观锁更新
        AggregationTask aggregationTask = (AggregationTask) task;
        List<PTNode> ptNodes = aggregationTask.getParseTree().toList();

        for (int i = 0; i < ptNodes.size(); i++) {
            doInit(ptNodes.get(i));
        }
    }

    private void doInit(PTNode ptNode) {
        DataIterator<OriginalEntity> iterator;
        int count = 0;
        try {
            if (!metaManager.load(ptNode.getEntityClassRef()).isPresent()) {
                throw new RuntimeException(String.format("entityClass not found by entityClassId %s",
                        ptNode.getEntityClassRef().getId()));
            }
            IEntityClass entityClass = metaManager.load(ptNode.getEntityClassRef()).get();

            if (!metaManager.load(ptNode.getAggEntityClassRef()).isPresent()) {
                throw new RuntimeException(String.format("aggEntityClass not found by entityClassId %s",
                           ptNode.getEntityClassRef().getId()));
            }
            IEntityClass aggEntityClass = metaManager.load(ptNode.getAggEntityClassRef()).get();

            if (entityClass.relationship().stream().filter(s -> s.getId() == ptNode.getRelationId())
                    .collect(Collectors.toList()).size() <= 0) {
                throw new RuntimeException(String.format("not found relation by relationId %s", ptNode.getRelationId()));
            }
            Relationship relationship = entityClass.relationship().stream().filter(s ->
                    s.getId() == ptNode.getRelationId()).collect(Collectors.toList()).get(0);

            if (!entityClass.field(ptNode.getEntityFieldId()).isPresent()) {
                throw new RuntimeException(String.format("entityField not found by entityClassId %s",
                        ptNode.getEntityFieldId()));
            }
            IEntityField entityField = entityClass.field(ptNode.getEntityFieldId()).get();

            if (!aggEntityClass.field(ptNode.getAggEntityFieldId()).isPresent()) {
                throw new RuntimeException(String.format("aggEntityField not found by entityClassId %s",
                        ptNode.getAggEntityFieldId()));
            }
            IEntityField aggEntityField = aggEntityClass.field(ptNode.getAggEntityFieldId()).get();



            iterator = masterStorage.iterator(entityClass, 0, System.currentTimeMillis(), 0, 1);
            while (iterator.hasNext()) {
                // 获取主信息id，得到entity信息
                OriginalEntity originalEntity = iterator.next();
                int retry = 0;
                while (retry < retryCount) {
                    try {
                        logger.info(String.format("start agg entity: %s", originalEntity.toString()));
                        Optional<IEntity> aggMainEntity = masterStorage.selectOne(originalEntity.getId(), entityClass);
                        if (aggMainEntity.isPresent()) {
                            if (logger.isInfoEnabled()) {
                                logger.debug(String.format("start aggregate , entityClassId is %s , entityId is %s ",
                                        aggMainEntity.get().entityClassRef().getId(),
                                        aggMainEntity.get().id()));
                            }
                            //构造查询被聚合信息条件
                            Condition condition = new Condition(relationship.getEntityField(),
                                    ConditionOperator.EQUALS,
                                    new LongValue(relationship.getEntityField(), aggMainEntity.get().id()));
                            Conditions conditions = ptNode.getConditions().addAnd(condition);
                            //获取未提交最小commitId号
                            long minUnSyncCommitId = getMinCommitId();

                            // 获取主库符合条件数据
                            Collection<EntityRef> entityRefs = masterStorage.select(conditions, aggEntityClass, SelectConfig.Builder.anSelectConfig().withSort(
                                    Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withCommitId(minUnSyncCommitId).build());
                            Set<Long> ids = entityRefs.stream().map(EntityRef::getId).collect(Collectors.toSet());
                            if (logger.isInfoEnabled()) {
                                logger.debug(String.format("masterStorage select by conditions , entityClassId is %s, mainEntityId is %s, result id list is %s ", aggEntityClass.id(), aggMainEntity.get().id(), ids));
                            }
                            entityRefs = null;


                            //按照一页1000条数据查询索引库
                            long defaultPageSize = 1000;
                            Page page = new Page(1L, defaultPageSize);
                            Collection<EntityRef> indexEntityRefs = indexStorage.select(conditions, aggEntityClass, SelectConfig.Builder.anSelectConfig().withSort(
                                    Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).withCommitId(minUnSyncCommitId).withExcludedIds(ids).build());
                            Set<Long> indexIds = indexEntityRefs.stream().map(EntityRef::getId).collect(Collectors.toSet());
                            indexIds.addAll(ids);
                            long[] masterIds = indexIds.stream().mapToLong(Long::longValue).toArray();

                            if (ptNode.getAggregationType() == AggregationType.COUNT) {
                                if (updateAgg(Optional.of(IValueUtils.toIValue(entityField, masterIds.length)), entityClass, aggMainEntity)) {
                                    break;
                                } else {
                                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                                }
                            }


                            // 得到所有聚合明细
                            Collection<IEntity> entities = masterStorage.selectMultiple(masterIds);
                            //获取符合条件的所有明细值
                            List<Optional<IValue>> ivalues = entities.stream().map(i -> i.entityValue().getValue(aggEntityField.id())).collect(Collectors.toList());
                            entities = null;

                            // 数据量较大，分批将ivalue加载到内存，释放entitys
                            if (page.getTotalCount() > defaultPageSize) {
                                while (page.hasNextPage()) {
                                    page.getNextPage();
                                    Collection<EntityRef> refCollection = indexStorage.select(conditions, aggEntityClass, SelectConfig.Builder.anSelectConfig().withSort(
                                            Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).withCommitId(minUnSyncCommitId).withExcludedIds(ids).build());
                                    entities = masterStorage.selectMultiple(refCollection.stream().map(EntityRef::getId).collect(Collectors.toSet())
                                            .stream().mapToLong(Long::longValue).toArray());
                                    ivalues.addAll(entities.stream().map(i -> i.entityValue().getValue(aggEntityField.id())).collect(Collectors.toList()));
                                    entities = null;
                                }
                            }

                            // ivalus包含完整明细数据被聚合字段value，可能占用较大内存,只更新decimal和long类型
                            if (ivalues.size() <= 0) {
                                if (entityField.type().equals(FieldType.DECIMAL)) {
                                    aggMainEntity.get().entityValue().addValue(IValueUtils.toIValue(entityField, new BigDecimal("0.0")));
                                    masterStorage.replace(aggMainEntity.get(), entityClass);
                                } else {
                                    aggMainEntity.get().entityValue().addValue(IValueUtils.toIValue(entityField, 0));
                                    masterStorage.replace(aggMainEntity.get(), entityClass);
                                }
                                break;
                            }

                            logger.info(String.format("doAgg begin, ivalues is: %s, ptNode is: %s", ivalues, ptNode));
                            Optional<IValue> aggMainIValue = doAgg(ivalues, ptNode.getAggregationType(), entityField);
                            logger.info(String.format("doAgg result is: %s", aggMainIValue.get().toString()));
                            if (updateAgg(aggMainIValue, entityClass, aggMainEntity)) {
                                break;
                            } else {
                                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                            }
                        }
                    } catch (Exception e) {
                        retry++;
                        logger.error(e.getMessage(), e);
                    }
                }

                // 每次更新指定条数后，短暂等待，给CDC同步预留时间
                if (count++ > AggregationTask.DEFAULT_SIZE) {
                    count = 0;
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(taskStopGapTimeMs));
                }

            }
            logger.info(String.format("==============entityClass %s entityField %s has doAggInit complete.", entityClass.id(), entityField.id()));
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage(), throwables);
        }
    }

    private boolean updateAgg(Optional<IValue> ivalue, IEntityClass entityClass, Optional<IEntity> entity) {
        // 乐观锁更新聚合信息,不成功会重试
        if (ivalue.isPresent()) {
            entity.get().entityValue().addValue(ivalue.get());
            try {
                masterStorage.replace(entity.get(), entityClass);
                logger.info("++++++++++++++++++++++ replace entity with " + entity.get().entityValue().toString());
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    private Optional<IValue> doAgg(List<Optional<IValue>> ivalues, AggregationType aggregationType, IEntityField entityField) {
        // 工厂获取聚合函数，执行运算
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);

        if (entityField.type().equals(FieldType.DATETIME)) {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, LocalDateTime.now())), ivalues);
        } else if (entityField.type().equals(FieldType.DECIMAL)) {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, new BigDecimal("0.0"))), ivalues);
        } else {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, 0)), ivalues);
        }
    }

    private long getMinCommitId() {
        long minUnSyncCommitId;
        Optional<Long> minUnSyncCommitIdOp = commitIdStatusService.getMin();
        if (minUnSyncCommitIdOp.isPresent()) {
            minUnSyncCommitId = minUnSyncCommitIdOp.get();
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "The minimum commit number {} that is currently uncommitted was successfully obtained.",
                        minUnSyncCommitId);
            }
        } else {
            minUnSyncCommitId = 0;
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to fetch the commit number, use the default commit number 0.");
            }
        }
        return minUnSyncCommitId;
    }



}
