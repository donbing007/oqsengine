package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple2;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算初始化逻辑.
 *
 * @version 0.1 2021/12/1 16:43
 * @Auther weikai
 * @since 1.8
 */
public class DefaultCalculationInitLogic implements CalculationInitLogic {
    private final Logger logger = LoggerFactory.getLogger(DefaultCalculationInitLogic.class);

    @Resource(name = "taskThreadPool")
    private ExecutorService worker;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private InitIvalueFactory initIvalueFactory;

    private ConcurrentMap<IEntityClass, List<IEntity>> batchEntity;

    private static final int BATCH_LIMIT = 1000;

    private static final String SUCCESS = "success";

    private static final String FAILED = "failed";


    /**
     * 任务是否完成检查时长.
     */
    private static final long CHECK_DONE = 100L;

    @PostConstruct
    public void init() {
        if (worker == null) {
            throw new IllegalArgumentException("No execution thread pool is set.");
        }
        batchEntity = new ConcurrentHashMap<>();
    }

    @PreDestroy
    public void destroy() {
        batchEntity.clear();
    }


    public ExecutorService getWorker() {
        return worker;
    }

    public void setWorker(ExecutorService worker) {
        this.worker = worker;
    }

    @Override
    public Map<String, List<InitCalculationParticipant>> accept(List<Map<IEntityClass, Collection<InitCalculationParticipant>>> run) throws InterruptedException {
        Map<String, List<InitCalculationParticipant>> res = new HashMap<>();
        List<Future<Tuple2<Boolean, List<InitCalculationParticipant>>>> futures = new ArrayList<>();
        for (Map<IEntityClass, Collection<InitCalculationParticipant>> classCollectionMap : run) {

            for (IEntityClass entityClass : classCollectionMap.keySet()) {
                // 同组内entityClass可以并发
                futures.add(worker.submit(new Runner(entityClass, classCollectionMap.get(entityClass))));
            }
            // 等待所有任务完成.
            do {
                try {
                    TimeUnit.MICROSECONDS.sleep(CHECK_DONE);
                } catch (InterruptedException e) {
                    // 不做处理
                }
            } while (!futures.stream().allMatch(Future::isDone));

            futures.forEach(future -> {
                try {
                    Tuple2<Boolean, List<InitCalculationParticipant>> tuple2 = future.get();
                    if (tuple2._1()) {
                        if (res.get(SUCCESS) == null || res.get(SUCCESS).isEmpty()) {
                            res.put(SUCCESS, tuple2._2());
                        } else {
                            res.get(SUCCESS).addAll(tuple2._2());
                        }
                    } else {
                        if (res.get(FAILED) == null || res.get(FAILED).isEmpty()) {
                            res.put(FAILED, tuple2._2());
                        } else {
                            res.get(FAILED).addAll(tuple2._2());
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            futures.clear();
        }
        return res;
    }

    @Override
    public Tuple2<Boolean, List<InitCalculationParticipant>> initLogic(IEntityClass entityClass, Collection<InitCalculationParticipant> participants) {
        try {
            DataIterator<OriginalEntity> iterator = masterStorage.iterator(entityClass, 0, Long.MAX_VALUE, 0, 1);
            List<IEntity> failedList = new ArrayList<>();
            while (iterator.hasNext() || !failedList.isEmpty()) {
                // 先处理上次遍历失败的entity
                if (!failedList.isEmpty()) {
                    long[] ids = new long[failedList.size()];
                    for (int i = 0; i < failedList.size(); i++) {
                        ids[i] = failedList.get(i).id();
                    }
                    Collection<IEntity> entities = masterStorage.selectMultiple(ids);
                    for (IEntity entity : entities) {
                        // 计算字段具体类型执行初始化计算
                        initEntity(entity, participants);
                    }
                    failedList.clear();
                }

                if (iterator.hasNext()) {
                    OriginalEntity originalEntity = iterator.next();

                    Optional<IEntity> entity = masterStorage.selectOne(originalEntity.getId(), entityClass);
                    // 计算字段具体类型执行初始化计算
                    if (entity.isPresent()) {
                        initEntity(entity.get(), participants);
                    }
                }

                if (batchEntity.containsKey(entityClass)) {
                    if (batchEntity.get(entityClass).size() >= BATCH_LIMIT || !iterator.hasNext()) {
                        // 批量更新
                        Collection<IEntity> entities = batchEntity.get(entityClass);
                        if (entities.size() > 0) {
                            EntityPackage entityPackage = new EntityPackage();
                            entities.forEach(e -> entityPackage.put(e, entityClass));
                            masterStorage.replace(entityPackage);
                            entities.forEach(e -> {
                                if (e.isDirty()) {
                                    failedList.add(e);
                                }
                            });
                        }
                        batchEntity.get(entityClass).clear();
                    }
                }
            }
            batchEntity.remove(entityClass);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new Tuple2<>(false, (List<InitCalculationParticipant>) participants);
        }
        return new Tuple2<>(true, (List<InitCalculationParticipant>) participants);
    }


    private IEntity initEntity(IEntity entity, Collection<InitCalculationParticipant> participants) throws SQLException {
        for (InitCalculationParticipant participant : participants) {
            initIValue(entity, participant);
        }
        return entity;
    }

    private IEntity initIValue(IEntity entity, InitCalculationParticipant participant) throws SQLException {
        InitIvalueLogic logic = initIvalueFactory.getLogic(participant.getField().calculationType());
        logic.init(entity, participant);
        if (participant.isChange(entity)) {
            if (batchEntity.get(participant.getEntityClass()) == null) {
                batchEntity.put(participant.getEntityClass(), Stream.of(entity).collect(Collectors.toList()));
            } else {
                if (!batchEntity.get(participant.getEntityClass()).contains(entity)) {
                    batchEntity.get(participant.getEntityClass()).add(entity);
                }
            }
        }
        return entity;
    }


    class Runner implements Callable<Tuple2<Boolean, List<InitCalculationParticipant>>> {
        private final IEntityClass entityClass;
        private final Collection<InitCalculationParticipant> participants;

        public Runner(IEntityClass entityClass, Collection<InitCalculationParticipant> participants) {
            this.entityClass = entityClass;
            this.participants = participants;
        }

        @Override
        public Tuple2<Boolean, List<InitCalculationParticipant>> call() throws Exception {
            return initLogic(this.entityClass, this.participants);
        }
    }

}
