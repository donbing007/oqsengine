package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValueFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorage.class);

    @Resource
    SQLMasterAction sqlMasterAction;

    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource(name = "tableNameSelector")
    private Selector<String> tableNameSelector;

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    private long queryTimeout;

    private int workerSize;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setWorkerSize(int workerSize) {
        this.workerSize = workerSize;
    }

    /**
     * 工作者线程.
     */
    private ExecutorService worker;

    @PostConstruct
    public void init() {
        if (workerSize <= 0) {
            setWorkerSize(Runtime.getRuntime().availableProcessors());
        }

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }

        worker = new ThreadPoolExecutor(workerSize, workerSize,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(500),
            ExecutorHelper.buildNameThreadFactory("Master-worker", false));
    }

    @PreDestroy
    public void destroy() {
        ExecutorHelper.shutdownAndAwaitTermination(worker);
    }

    @Override
    public Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(id)) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return sqlMasterAction.select((Connection) resource.value(), id, entityClass);
                }
            });
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
        Map<DataSource, List<Long>> groupedMap = ids.keySet().stream().collect(
                Collectors.groupingBy(id -> dataSourceSelector.select(Long.toString(id))));

        CountDownLatch latch = new CountDownLatch(groupedMap.keySet().size());
        List<Future> futures = new ArrayList(groupedMap.keySet().size());

        for (List<Long> groupedIds : groupedMap.values()) {
            futures.add(worker.submit(new MultipleSelectCallable(latch, groupedIds, ids)));
        }

        try {
            if (!latch.await(queryTimeout, TimeUnit.MILLISECONDS)) {
                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
        }

        List<IEntity> results = new ArrayList<>(ids.size());
        for (Future<Collection<IEntity>> f : futures) {
            try {
                results.addAll(f.get());
            } catch (Exception e) {
                throw new SQLException(e.getMessage(), e);
            }
        }

        return results;
    }

    @Override
    public void synchronize(long sourceId, long targetId) throws SQLException {
        // 需要在内部类中修改,所以使用了引用类型.
        final int[] newVersion = new int[1];
        final long[] newTime = new long[1];
        transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(sourceId)) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    Map result = sqlMasterAction.selectVersionTime((Connection) resource.value(), sourceId, targetId);
                    newVersion[0] = (int)result.get("version");
                    newTime[0] = (long)result.get("time");

                    return null;
                }
            }
        );

        transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(targetId)) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return sqlMasterAction.replaceVersionTime((Connection) resource.value(), sourceId, targetId, newVersion, newTime);
                }
            }

        );


    }

    @Override
    public void build(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id()), OpTypeEnum.BUILD) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return sqlMasterAction.build((Connection) resource.value(), entity);
                }
            });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id()), OpTypeEnum.REPLACE) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return sqlMasterAction.replace((Connection) resource.value(), entity);
                }
            });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(
            new DataSourceShardingTask(
                dataSourceSelector, Long.toString(entity.id()), OpTypeEnum.DELETE) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return sqlMasterAction.delete((Connection) resource.value(), entity);
                }
            });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    /**
     * 多重 id 查询任务,每一个任务表示一个分库的查询任务.
     */
    private class MultipleSelectCallable implements Callable<Collection<IEntity>> {

        private CountDownLatch latch;
        // 按照表名分区的 id.
        private Map<String, List<Long>> ids;
        // 目标 id总量.
        private int size;
        // id 对应 entityClass 速查表.
        private Map<Long, IEntityClass> entityTable;

        private String dataSourceShardKey;

        public MultipleSelectCallable(CountDownLatch latch, List<Long> ids, Map<Long, IEntityClass> entityTable) {
            this.latch = latch;
            this.entityTable = entityTable;
            // 按表区分.
            this.ids = ids.stream().collect(Collectors.groupingBy(id -> tableNameSelector.select(id.toString())));
            size = ids.size();

            dataSourceShardKey = Long.toString(ids.get(0));
        }

        @Override
        public Collection<IEntity> call() throws Exception {
            try {
                return (Collection<IEntity>) transactionExecutor.execute(
                    new DataSourceShardingTask(
                        dataSourceSelector, dataSourceShardKey) {

                        @Override
                        public Object run(TransactionResource resource) throws SQLException {
                            List<IEntity> entities = new ArrayList(size);
                            for (String table : ids.keySet()) {
                                entities.addAll(select(table, ids.get(table), resource));
                            }

                            return entities;
                        }

                        private Collection<IEntity> select(
                            String tableName, List<Long> partitionTableIds, TransactionResource res)
                            throws SQLException {
                            return sqlMasterAction.select((Connection)res.value(), tableName, partitionTableIds, entityTable);
                        }
                    });
            } finally {
                latch.countDown();
            }
        }
    }
}
