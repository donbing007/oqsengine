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
    private Map<Long, IEntityValue> convertToEntityValueMap(Map<Long, RawEntityValue> rawEntityValueMap) throws SQLException {
        Map<Long, IEntityValue> valueMap = Maps.newHashMap();
        for (Map.Entry<Long, RawEntityValue> vEntry : rawEntityValueMap.entrySet()) {

            valueMap.put(vEntry.getKey(), buildEntityValue(vEntry.getKey(), vEntry.getValue().getMeta(), vEntry.getValue().getAttr()));
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
        AbstractMap.SimpleEntry<Boolean, CdcErrorTask> result = null;

        boolean isDelete = false;
        try {
            isDelete = getBooleanFromColumn(rawEntry.getColumns(), DELETED);
        } catch (Exception e) {
            result = generateErrorTask(rawEntry.getId(), rawEntry.getCommitId(),
                    String.format("get delete column error, id : %d, commitId : %d, message : %s",
                                                rawEntry.getId(), rawEntry.getCommitId(), e.getMessage()));
        }

        if (null == result) {
            if (isDelete) {
                result = doDelete(rawEntry.getId(), rawEntry.getCommitId());
            } else {
                result = doReplace(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId(), prefEntityValueMaps);
            }
        }

        if (!result.getKey()) {
              errorHandle(result.getValue());
        } else {
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
            synced.incrementAndGet();
        }
    }

    //  删除
    private AbstractMap.SimpleEntry<Boolean, CdcErrorTask> doDelete(long id, long commitId) throws SQLException {
        try {
            sphinxQLIndexStorage.delete(id);

            return new AbstractMap.SimpleEntry<Boolean, CdcErrorTask>(true, null);
        } catch (Exception e) {
            String message = String.format("delete error, id : %d, message : %s", id, e.getMessage());
            logger.error(message);
            return new AbstractMap.SimpleEntry<Boolean, CdcErrorTask>(false,
                    CdcErrorTask.buildErrorTask(seqNoGenerator.next(), id, commitId, message));
        }
    }

    //  同步使用时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    //  replace操作
    private AbstractMap.SimpleEntry<Boolean, CdcErrorTask>
                doReplace(List<CanalEntry.Column> columns, long id, long commitId, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {
        //  通过解析binlog获取
        try {
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
                    null                                     //  由sphinxQLIndexStorage内部转换  entityValue
            );

            //  取自己的entityValue
            IEntityValue entityValue = buildEntityValue(
                    storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));

            /*
                有父类, 合并父类entityValue
            */
            if (pref > 0) {
                try {
                    //  通过pref拿到父类的EntityValue
                    IEntityValue entityValueF = entityValueGet(pref, prefEntityValueMaps);
                    entityValue.addValues(entityValueF.values());
                } catch (Exception ex) {
                    return generateErrorTask(id, commitId, String.format(
                            "get pref entityValue failed. no match data, record will be ignored, id : %d, pref : %d, message : %s",
                                                                        id, pref, ex.getMessage())
                            );
                }
            }
            /*
                replacement is always true, 所有的OQS同步对于CDC来说都是replace
            */
            sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, true);

            return new AbstractMap.SimpleEntry<>(true, null);
        } catch (Exception e) {
            return generateErrorTask(id, commitId, String.format("buildEntityValue, id : %d, message : %s", id, e.getMessage()));
        }
    }

    //  生成错误任务
    private AbstractMap.SimpleEntry<Boolean, CdcErrorTask> generateErrorTask(long id, long commitId, String message) throws SQLException {
        logger.warn(message);
        return new AbstractMap.SimpleEntry<Boolean, CdcErrorTask>(false,
                CdcErrorTask.buildErrorTask(seqNoGenerator.next(), id, commitId, message));
    }

    //  IEntityValue build
    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

    //  通过pref获得IEntityValue，当拉取不到数据时从主库拉取
    private IEntityValue entityValueGet(long pref, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {
        IEntityValue entityValue = prefEntityValueMaps.get(pref);
        if (null == entityValue) {
            try {
                return masterStorage.selectEntityValue(pref).orElse(null);
            } catch (Exception e) {
                String error = String.format("query entityValue from master db error..., id : %d, " +
                        "message : %s", pref, e.getMessage());
                throw new SQLException(error);
            }
        }
//        if (null == entityValue) {
//            throw new SQLException(
//                    String.format("pref's entityValue could not be null in relation pool when have cref, need pref id : %d", pref));
//        }
        return entityValue;
    }

    private void errorHandle(CdcErrorTask cdcErrorTask) throws SQLException {
        devOpsStorage.buildCdcError(cdcErrorTask);
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
