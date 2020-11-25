package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.google.common.collect.Maps;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.devops.DevOpsStorage;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.INVALID_ENTITY_ID;
import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.PARSE_COLUMNS_ERROR;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SECOND;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SINGLE_CONSUMER_MAX_ROW;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.*;
import static com.xforceplus.ultraman.oqsengine.storage.master.utils.EntityFieldBuildUtils.metaToFieldTypeMap;

/**
 * desc :
 * name : SphinxSyncService
 *
 * @author : xujia
 * date : 2020/11/13
 * @since : 1.8
 */
public class SphinxSyncExecutor {

    final Logger logger = LoggerFactory.getLogger(SphinxSyncExecutor.class);

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

    @Resource(name = "masterStorage")
    private MasterStorage masterStorage;

    @Resource(name = "devOpsStorage")
    private DevOpsStorage devOpsStorage;

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService consumerPool;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator seqNoGenerator;

    private boolean isSingleSyncConsumer = true;

    private int executionTimeout = 3 * 1000;

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public void setSingleSyncConsumer(boolean singleSyncConsumer) {
        isSingleSyncConsumer = singleSyncConsumer;
    }


    //  执行同步到Sphinx操作
    public int sync(List<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        Map<Long, IEntityValue> prefEntityValueMaps =
                convertToEntityValueMap(cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues());

        return syncSphinx(rawEntries, prefEntityValueMaps, cdcMetrics);
    }

    //  将unCommitEntityValues中Attr + meta 键值对转为Map<Long, IEntityValue>
    private Map<Long, IEntityValue> convertToEntityValueMap(Map<Long, RawEntityValue> rawEntityValueMap) {

        Map<Long, IEntityValue> valueMap = Maps.newHashMap();
        for (Map.Entry<Long, RawEntityValue> vEntry : rawEntityValueMap.entrySet()) {
            try {
                valueMap.put(vEntry.getKey(),
                        buildEntityValue(vEntry.getKey(), vEntry.getValue().getMeta(), vEntry.getValue().getAttr()));
            } catch (Exception e) {
                logger.warn("convertToEntityValueMap failed...will be ignore id : {}, message :{}", vEntry.getKey(), e.getMessage());
            }
        }
        return valueMap;
    }

    //  执行sphinx同步
    private int syncSphinx(List<RawEntry> rawEntries, Map<Long, IEntityValue> prefEntityValueMaps,
                           CDCMetrics cdcMetrics) throws SQLException {
        AtomicInteger synced = new AtomicInteger(0);
        if (!rawEntries.isEmpty()) {
            //  开启多线程写入
            if (isSingleSyncConsumer || rawEntries.size() <= SINGLE_CONSUMER_MAX_ROW) {
                for (RawEntry rawEntry : rawEntries) {
                    sphinxConsume(rawEntry, prefEntityValueMaps, cdcMetrics, synced);
                }
            } else {
                multiConsume(rawEntries, prefEntityValueMaps, cdcMetrics, synced);
            }
        }
        return synced.get();
    }

    //  多线程作业
    private void multiConsume(List<RawEntry> rawEntries, Map<Long, IEntityValue> prefEntityValueMaps,
                              CDCMetrics cdcMetrics, AtomicInteger synced) throws SQLException {
        CountDownLatch latch = new CountDownLatch(rawEntries.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(rawEntries.size());

        rawEntries.forEach((value) -> futures.add(consumerPool.submit(
                new SyncSphinxCallable(value, cdcMetrics, prefEntityValueMaps, synced, latch))));

        try {
            if (!latch.await(executionTimeout, TimeUnit.MILLISECONDS)) {

                for (Future<Boolean> f : futures) {
                    f.cancel(true);
                }

                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    //  作业
    private void sphinxConsume(RawEntry rawEntry, Map<Long, IEntityValue> prefEntityValueMaps,
                               CDCMetrics cdcMetrics, AtomicInteger synced) throws SQLException {
        try {
            boolean isDelete = getBooleanFromColumn(rawEntry.getColumns(), DELETED);

            if (isDelete) {
                doDelete(rawEntry.getId(), rawEntry.getCommitId());
            } else {
                doReplace(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId(), prefEntityValueMaps);
            }

            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
            synced.incrementAndGet();
        } catch (Exception e) {
            errorHandle(rawEntry.getId(), rawEntry.getCommitId(), e.getMessage());
        }
    }

    //  删除,不停的循环删除, 直到成功为止
    private int doDelete(long id, long commitId) throws SQLException {
        while (true) {
            try {
                return sphinxQLIndexStorage.delete(id);
            } catch (Exception e) {
                //  delete error
                logger.error("delete error, will retry, id : {}, commitId : {}, message : {}",
                                                                                id, commitId, e.getMessage());
                if (e instanceof SQLException) {
                    SQLException el = (SQLException) e;
                    if (el.getSQLState().equals(INVALID_ENTITY_ID.name())) {
                        throw new SQLException(String.format("replace-delete error, id : %d, commitId : %d, message : %s",
                                id, commitId, e.getMessage()));
                    }
                }
                sleepNoInterrupted(SECOND);
            }
        }
    }

    private int doReplace(StorageEntity storageEntity, IEntityValue entityValue) throws SQLException {
        while (true) {
            try {
                /*
                    replacement is always true, 所有的OQS同步对于CDC来说都是replace
                */
                return sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, true);
            } catch (Exception e) {
                //  delete error
                logger.error("replace error, will retry..., id : {}, commitId : {}, message : {}",
                        storageEntity.getId(), storageEntity.getCommitId(), e.getMessage());
                //  当数据存在错误时，抛出错误，否则可以认为是连接错误，将无限重试
                if (e instanceof SQLException) {
                    SQLException el = (SQLException) e;
                    if (el.getSQLState().equals(INVALID_ENTITY_ID.name()) || el.getSQLState().equals(PARSE_COLUMNS_ERROR.name())) {
                        throw new SQLException(String.format("replace-sync error, id : %d, commitId : %d, message : %s",
                                                        storageEntity.getId(), storageEntity.getCommitId(), e.getMessage()));
                    }
                }
                sleepNoInterrupted(SECOND);
            }
        }
    }

    //  同步使用时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    //  replace操作
    private int doReplace(List<CanalEntry.Column> columns, long id, long commitId, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {
        //  通过解析binlog获取

        long cref = getLongFromColumn(columns, CREF);
        long pref = getLongFromColumn(columns, PREF);

        StorageEntity storageEntity = new StorageEntity(
                id,                                               //  id
                getLongFromColumn(columns, ENTITY),               //  entity
                pref,                                             //  pref
                cref,                                             //  cref
                getLongFromColumn(columns, TX),                   //  tx
                commitId,                                         //  commitid
                null,                                   //  由sphinxQLIndexStorage内部转换  entityValue
                null,                                    //  由sphinxQLIndexStorage内部转换  entityValue
                getLongFromColumn(columns, TIME)                  //  time
        );

        //  取自己的entityValue
        IEntityValue entityValue = buildEntityValue(
                storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));

            /*
                有父类, 合并父类entityValue
            */
        if (pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = entityValueGet(pref, prefEntityValueMaps);
            entityValue.addValues(entityValueF.values());
        }

        return doReplace(storageEntity, entityValue);
    }

    //  IEntityValue build
    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

    //  通过pref获得IEntityValue，当拉取不到数据时从主库拉取
    private IEntityValue entityValueGet(long pref, Map<Long, IEntityValue> prefEntityValueMaps) {
        IEntityValue entityValue = prefEntityValueMaps.get(pref);
        if (null == entityValue) {
            while (true) {
                try {
                    entityValue = masterStorage.selectEntityValue(pref).orElse(null);
                    break;
                } catch (Exception e) {
                    logger.warn("entityValueGet from master db error, will retry..., id : {}, " +
                            "message : {}", pref, e.getMessage());
                    sleepNoInterrupted(SECOND);
                }
            }
        }
        return entityValue;
    }

    public void errorHandle(long id, long commitId, String message) throws SQLException {
        devOpsStorage.buildCdcError(CdcErrorTask.buildErrorTask(seqNoGenerator.next(), id, commitId, message));
    }

    private void sleepNoInterrupted(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ex) {
            //  ignore
        }
    }

    //  多线程作业封装类
    private class SyncSphinxCallable implements Callable<Boolean> {
        private CountDownLatch latch;
        private RawEntry rawEntry;
        private CDCMetrics cdcMetrics;
        private Map<Long, IEntityValue> prefEntityValueMaps;
        private AtomicInteger synced;

        public SyncSphinxCallable(RawEntry rawEntry, CDCMetrics cdcMetrics, Map<Long, IEntityValue> prefEntityValueMaps, AtomicInteger synced, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
            this.cdcMetrics = cdcMetrics;
            this.prefEntityValueMaps = prefEntityValueMaps;
            this.synced = synced;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                sphinxConsume(rawEntry, prefEntityValueMaps, cdcMetrics, synced);
            } finally {
                latch.countDown();
            }
            return true;
        }
    }
}
