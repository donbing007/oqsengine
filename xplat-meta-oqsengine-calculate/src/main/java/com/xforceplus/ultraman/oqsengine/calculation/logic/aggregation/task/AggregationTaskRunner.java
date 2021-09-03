package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.impl.PTNode;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    private MetaManager metaManager;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private EntitySearchService entitySearchService;

    private final long taskStopGapTimeMs = 500L;

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
        // 执行聚合初始化，step 1: 查询被聚合  Step 2: 调用聚合函数 Step 3: 乐观锁更新
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
                try {
                    // 获取主信息id，得到entity信息
                    OriginalEntity originalEntity = iterator.next();
                    Optional<IEntity> entity = entitySearchService.selectOne(originalEntity.getId(), ptNode.getEntityClass().ref());
                    if (entity.isPresent()) {
                        //构造查询被聚合信息条件
                        Condition condition = new Condition(ptNode.getRelationship().getEntityField(),  ConditionOperator.EQUALS, new LongValue(ptNode.getEntityField(), entity.get().id()));
                        Collection<IEntity> entities = entitySearchService.selectByConditions(new Conditions(condition), ptNode.getAggEntityClass().ref(), ServiceSelectConfig.Builder.anSearchConfig().build());

                        if (entities.size() > 0) {
                            //获取符合条件的所有明细值
                            List<Optional<IValue>> ivalues = entities.stream().map(i -> i.entityValue().getValue(ptNode.getAggEntityField().id())).collect(Collectors.toList());


                            // 工厂获取聚合函数，执行运算
                            AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(ptNode.getAggregationType());

                            Optional<IValue> ivalue = function.init(Optional.of(IValueUtils.toIValue(ptNode.getEntityField(), ptNode.getEntityField().type().equals(FieldType.DATETIME) ? LocalDateTime.now() : 0)), ivalues);

                            // 乐观锁更新聚合信息,不成功会重试
                            if (ivalue.isPresent()) {
                                entity.get().entityValue().addValue(ivalue.get());
                                while (true) {
                                    try {
                                        masterStorage.replace(entity.get(), ptNode.getEntityClass());
                                        break;
                                    } catch (SQLException e) {
                                        entity = entitySearchService.selectOne(originalEntity.getId(), ptNode.getEntityClass().ref());
                                        entity.get().entityValue().addValue(ivalue.get());
                                        logger.error(e.getMessage(), e);
                                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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

}
