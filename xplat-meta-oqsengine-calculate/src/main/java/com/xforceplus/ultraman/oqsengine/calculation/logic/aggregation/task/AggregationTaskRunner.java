package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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

    private final long taskStopGapTimeMs = 500L;

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
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
            iterator = masterStorage.iterator(ptNode.getEntityClass(), 0, System.currentTimeMillis(), 0);
            while (iterator.hasNext()) {
                while (true) {
                    try {
                        // 获取主信息id，得到entity信息
                        OriginalEntity originalEntity = iterator.next();
                        Optional<IEntity> aggMainEntity = masterStorage.selectOne(originalEntity.getId(), ptNode.getEntityClass());
                        if (aggMainEntity.isPresent()) {
                            //构造查询被聚合信息条件
                            Condition condition = new Condition(ptNode.getRelationship().getEntityField(), ConditionOperator.EQUALS, new LongValue(ptNode.getRelationship().getEntityField(), aggMainEntity.get().id()));
                            Conditions conditions = new Conditions(condition);
                            //获取未提交最小commitId号
                            long minUnSyncCommitId = getMinCommitId();

                            // 获取主库符合条件数据
                            Collection<EntityRef> entityRefs = masterStorage.select(conditions, ptNode.getAggEntityClass(), SelectConfig.Builder.anSelectConfig().withSort(
                                    Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withCommitId(minUnSyncCommitId).build());
                            Set<Long> ids = entityRefs.stream().map(EntityRef::getId).collect(Collectors.toSet());
                            entityRefs = null;


                            //按照一页1000条数据查询索引库
                            long defaultPageSize = 1000;
                            Page page = new Page(1L, defaultPageSize);
                            Collection<EntityRef> indexEntityRefs = indexStorage.select(conditions, ptNode.getAggEntityClass(), SelectConfig.Builder.anSelectConfig().withSort(
                                    Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).withCommitId(minUnSyncCommitId).withExcludedIds(ids).build());
                            Set<Long> indexIds = indexEntityRefs.stream().map(EntityRef::getId).collect(Collectors.toSet());
                            indexIds.addAll(ids);
                            long[] masterIds = indexIds.stream().mapToLong(Long::longValue).toArray();

                            // 得到所有聚合明细
                            Collection<IEntity> entities = masterStorage.selectMultiple(masterIds);
                            //获取符合条件的所有明细值
                            List<Optional<IValue>> ivalues = entities.stream().map(i -> i.entityValue().getValue(ptNode.getAggEntityField().id())).collect(Collectors.toList());
                            entities = null;

                            // 数据量较大，分批将ivalue加载到内存，释放entitys
                            if (page.getTotalCount() > defaultPageSize) {
                                while (page.hasNextPage()) {
                                    page.getNextPage();
                                    Collection<EntityRef> refCollection = indexStorage.select(conditions, ptNode.getAggEntityClass(), SelectConfig.Builder.anSelectConfig().withSort(
                                           Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).withCommitId(minUnSyncCommitId).withExcludedIds(ids).build());
                                    entities = masterStorage.selectMultiple(refCollection.stream().map(EntityRef::getId).collect(Collectors.toSet())
                                            .stream().mapToLong(Long::longValue).toArray());
                                    ivalues.addAll(entities.stream().map(i -> i.entityValue().getValue(ptNode.getAggEntityField().id())).collect(Collectors.toList()));
                                    entities = null;
                                }
                            }

                            // ivalus包含完整明细数据被聚合字段value，可能占用较大内存
                            if (ivalues.size() <= 0) {
                                break;
                            }

                            Optional<IValue> aggMainIValue = doAgg(ivalues, ptNode);
                            if (updateAgg(aggMainIValue, ptNode, aggMainEntity)) {
                                break;
                            } else {
                                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                // 每次更新指定条数后，短暂等待，给CDC同步预留时间
                if (count++ > AggregationTask.DEFAULT_SIZE) {
                    count = 0;
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(taskStopGapTimeMs));
                }

            }
        } catch (SQLException throwables) {
            logger.error(throwables.getMessage(), throwables);
        }
    }

    private boolean updateAgg(Optional<IValue> ivalue, PTNode ptNode, Optional<IEntity> entity) {
        // 乐观锁更新聚合信息,不成功会重试
        if (ivalue.isPresent()) {
            entity.get().entityValue().addValue(ivalue.get());
            try {
                masterStorage.replace(entity.get(), ptNode.getEntityClass());
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    private Optional<IValue> doAgg(List<Optional<IValue>> ivalues, PTNode ptNode) {
        // 工厂获取聚合函数，执行运算
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(ptNode.getAggregationType());

        return function.init(Optional.of(IValueUtils.toIValue(ptNode.getEntityField(), ptNode.getEntityField().type().equals(FieldType.DATETIME) ? LocalDateTime.now() : 0)), ivalues);
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
