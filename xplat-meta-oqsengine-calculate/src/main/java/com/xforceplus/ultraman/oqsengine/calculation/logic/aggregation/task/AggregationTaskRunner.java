package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;


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
    private KeyValueStorage kv;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private EntitySearchService entitySearchService;

    private final long taskStopGapTimeMs = 500L;

    @Override
    public void run(TaskCoordinator coordinator, Task task) {
        // TODO 执行聚合初始化，step 1: 查询被聚合  Step 2: 调用聚合函数 Step 3: 乐观锁更新
        AggregationTask aggregationTask = (AggregationTask) task;
        List<Tuple2<IEntityClass, IEntityField>> avgEntity = aggregationTask.getAvgEntitys();

        for (int i = 0; i < avgEntity.size(); i++) {
            doInit(avgEntity.get(i));
        }
    }

    private void doInit(Tuple2<IEntityClass, IEntityField> classIEntityFieldTuple2) {
        DataIterator<OriginalEntity> iterator;
        int count = 0;
        try {
            iterator = masterStorage.iterator(classIEntityFieldTuple2._1(), 0, System.currentTimeMillis(), 0);
            while (iterator.hasNext()) {
                while (true) {
                    try {
                        // TODO 获取主信息id，根据relationCode构建明细查询条件
                        OriginalEntity originalEntity = iterator.next();
                        Optional<IEntity> entity = entitySearchService.selectOne(originalEntity.getId(), classIEntityFieldTuple2._1().ref());
                        if (entity.isPresent()) {
                            //TODO 入参要改成明细的
                            Collection<IEntity> entities = entitySearchService.selectByConditions(Conditions.buildEmtpyConditions(), classIEntityFieldTuple2._1.ref(), ServiceSelectConfig.Builder.anSearchConfig().build());

                            //TODO fieldId 要改成明细的
                            List<Optional<IValue>> ivalues = entities.stream().map(i -> i.entityValue().getValue(classIEntityFieldTuple2._2().id())).collect(Collectors.toList());


                            // TODO 工厂获取聚合函数，执行运算
                            AvgFunction avgFunction = new AvgFunction();

                            Optional<IValue> ivalue = avgFunction.init(Optional.of(IValueUtils.toIValue(classIEntityFieldTuple2._2(), classIEntityFieldTuple2._2().type().equals(FieldType.DATETIME) ? LocalDateTime.now() : 0)), ivalues);

                            if (ivalue.isPresent()) {
                                entity.get().entityValue().addValue(ivalue.get());
                                masterStorage.replace(entity.get(), classIEntityFieldTuple2._1());
                            }
                            break;
                        }
                    } catch (Exception   e) {
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

}
