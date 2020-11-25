package com.xforceplus.ultraman.oqsengine.storage.master.iterator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.BatchCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.BatchSummary;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.DataSourceSummary;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.OffsetSnapShot;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * desc :
 * name : DataSourceIterator
 *
 * @author : xujia
 * date : 2020/8/25
 * @since : 1.8
 */
public class DataQueryIterator implements QueryIterator {

    final Logger logger = LoggerFactory.getLogger(DataQueryIterator.class);

    private static final int START_ID_OFF_SET = 1;
    private static final int EMPTY_COLLECTION_SIZE = 0;
    private static final int EMPTY_ID = 0;

    private int pageSize;

    private BatchSummary batchSummary;

    private BatchCondition batchCondition;

    private ExecutorService executorService;

    private SQLMasterStorage sqlMasterStorage;

    private int queryTimeout;

    public DataQueryIterator(BatchCondition batchCondition,
                             List<DataSourceSummary> dataSourceSummaries,
                             SQLMasterStorage sqlMasterStorage,
                             ExecutorService executorService,
                             int queryTimeout, int pageSize) throws SQLException {
        this.batchSummary = new BatchSummary(dataSourceSummaries);
        this.batchCondition = batchCondition;
        this.sqlMasterStorage = sqlMasterStorage;
        this.executorService = executorService;
        this.queryTimeout = queryTimeout;
        this.pageSize = pageSize;
    }

    @Override
    public int size() {
        return null != batchSummary ? batchSummary.count() : EMPTY_COLLECTION_SIZE;
    }
    @Override
    public boolean hasNext() {
        return null != batchSummary && batchSummary.hasNext();
    }

    @Override
    public List<IEntity> next() throws SQLException {
        return batchQuery();
    }

    public OffsetSnapShot snapShot() {
        return batchSummary.snapShot();
    }

    public boolean resetCheckPoint(OffsetSnapShot offsetSnapShot) {
        return batchSummary.resetCheckPoint(offsetSnapShot);
    }

    /*
        batchQuery by iterator
        traverse checkPoint(ds->table) -> next(ds->table) ....produce CallableList
        then doBatchSelect
    */
    public List<IEntity> batchQuery() throws SQLException {
        if (hasNext()) {
            int lefts = pageSize;

            List<Callable<List<IEntity>>> callableList = new ArrayList<>();
            while (EMPTY_COLLECTION_SIZE < lefts && batchSummary.hasNext()) {

                BatchSummary.CheckPointOffset checkPointOffset = batchSummary.next();
                /*
                    practical size
                 */
                int part = Math.min(lefts, checkPointOffset.left());

                callableList.add(sqlMasterStorage.new BathQueryByTableSummaryCallable(batchCondition,
                        checkPointOffset.offset(), part, checkPointOffset.getActiveDataSource(),
                        checkPointOffset.offsetSnapShot().getTableName(), queryTimeout));

                checkPointOffset.decrease(part);

                lefts -= part;
            }

            return doBatchSelect(callableList);
        }
        return null;
    }

    private List<IEntity> doBatchSelect(List<Callable<List<IEntity>>> callableList) throws SQLException {
        List<Future> futures = new ArrayList(callableList.size());
        CountDownLatch latch = new CountDownLatch(callableList.size());
        for (Callable<List<IEntity>> c : callableList) {
            ((SQLMasterStorage.BathQueryByTableSummaryCallable) c).setLatch(latch);
            futures.add(executorService.submit(c));
        }
        try {
            if (!latch.await(queryTimeout, TimeUnit.MILLISECONDS)) {
                for (Future f : futures) {
                    f.cancel(true);
                }

                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }

        List<IEntity> entities = new ArrayList<>();

        for (Future future : futures) {
            try {
                entities.addAll((List<IEntity>) future.get());
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }

        if (EMPTY_COLLECTION_SIZE < entities.size()) {
            batchSummary.offsetReset(entities.get(entities.size() - 1).id());
            mergePrefEntity(entities);
        }

        return entities;
    }


    private void mergePrefEntity(List<IEntity> entities) throws SQLException {

        /*
            has pref ?
        */
        if (EMPTY_ID != entities.get(0).family().parent()) {
            try {
                /*
                    get prefs
                 */
                Collection<IEntity> pEntity = sqlMasterStorage.selectMultiple(
                        entities.stream()
                                .collect(Collectors.toMap(f -> f.family().parent(), f -> f.entityClass().extendEntityClass(), (f0, f1) -> f0))
                );

                Map<Long, IEntity> entityMap = pEntity
                        .stream()
                        .collect(Collectors.toMap(IEntity::id, f -> f, (f0, f1) -> f0));

                if (entityMap.size() != entities.size()) {
                    throw new SQLException(String.format("merge pref failed, pref's size %d not equals child's %d",
                            entityMap.size(), entities.size()));
                }
                /*
                    merge prefs
                 */
                for (IEntity entity : entities) {
                    IEntity fatherEntity = entityMap.get(entity.family().parent());
                    if (null == fatherEntity) {
                        throw new SQLException(String.format("merge pref failed, pref not found, child %d.", entity.id()));
                    }
                    //  filter self searchable
                    if (null != entity.entityValue()) {
                        entity.entityValue().filter(v -> v.getField().config().isSearchable());
                    }

                    //  filter father searchable
                    if (null != fatherEntity.entityValue()) {
                        fatherEntity.entityValue().filter(v -> v.getField().config().isSearchable());
                    }

                    //  merge father to self
                    entity.entityValue().addValues(fatherEntity.entityValue().values());
                }
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    throw e;
                }

                throw new SQLException("merge pref failed, " + e.getMessage());
            }
        }
    }
}
