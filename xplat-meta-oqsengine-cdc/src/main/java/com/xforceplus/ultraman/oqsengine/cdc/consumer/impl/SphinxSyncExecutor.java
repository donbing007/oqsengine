package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.google.common.collect.Maps;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
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
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
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

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService consumerPool;

    private boolean isSingleSyncConsumer = true;

    private int executionTimeout = 30 * 1000;

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
    private int syncSphinx(List<RawEntry> rawEntries, Map<Long, IEntityValue> prefEntityValueMaps, CDCMetrics cdcMetrics) throws SQLException {
        AtomicInteger synced = new AtomicInteger(0);
        if (!rawEntries.isEmpty()) {
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
    private void multiConsume(List<RawEntry> rawEntries, Map<Long, IEntityValue> prefEntityValueMaps, CDCMetrics cdcMetrics, AtomicInteger synced) throws SQLException {
        CountDownLatch latch = new CountDownLatch(rawEntries.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(rawEntries.size());

        rawEntries.forEach((value) -> futures.add(consumerPool.submit(
            new SphinxSyncExecutor.SyncSphinxCallable(value, cdcMetrics, prefEntityValueMaps, synced, latch))));

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
    private void sphinxConsume(RawEntry rawEntry, Map<Long, IEntityValue> prefEntityValueMaps, CDCMetrics cdcMetrics, AtomicInteger synced) throws SQLException {
        boolean isSynced = true;
        if (isDelete(rawEntry.getColumns())) {
            doDelete(rawEntry.getColumns());
        } else {
            isSynced = doReplace(rawEntry.getColumns(), prefEntityValueMaps);
        }

        if (isSynced) {
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
            synced.incrementAndGet();
        }
    }

    //  判断是否删除
    private boolean isDelete(List<CanalEntry.Column> columns) throws SQLException {
        return getBooleanFromColumn(columns, DELETED);
    }

    //  删除
    private void doDelete(List<CanalEntry.Column> columns) throws SQLException {
        sphinxQLIndexStorage.delete(getLongFromColumn(columns, ID));
    }

    //  同步使用时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    //  replace操作
    private boolean doReplace(List<CanalEntry.Column> columns, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {

        long id = getLongFromColumn(columns, ID);
        long cref = getLongFromColumn(columns, CREF);
        long pref = getLongFromColumn(columns, PREF);
        long commitid = getLongFromColumn(columns, COMMITID);             //  commitid

        StorageEntity storageEntity = new StorageEntity(
            id,                                               //  id
            getLongFromColumn(columns, ENTITY),               //  entity
            pref,                                             //  pref
            cref,                                             //  cref
            getLongFromColumn(columns, TX),                   //  tx
            commitid,                                         //  commitid
            null,                                   //  由sphinxQLIndexStorage内部转换  entityValue
            null                                     //  由sphinxQLIndexStorage内部转换  entityValue
        );

        IEntityValue entityValue = null;

        //  是父类
        if (cref > 0) {
            //  通过自己的ID拿到对应的EntityValue
            entityValue = entityValueGet(id, prefEntityValueMaps, false);
        }
        //  通过binlog获取
        if (null == entityValue) {
            entityValue = buildEntityValue(
                    storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));
        }

        /*
            有父类, 合并父类entityValue
        */
        if (pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = entityValueGet(pref, prefEntityValueMaps, true);
            if (null == entityValueF) {
                logger.warn("get pref entityValue failed. no match data, record will be ignored, id : {}, pref : {}", id, pref);
                return false;
            }
            entityValue.addValues(entityValueF.values());
        }

        /*
            replacement is always true, 所有的OQS同步对于CDC来说都是replace
         */
        sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, true);

        return true;
    }

    //  IEntityValue build
    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

    //  通过pref获得IEntityValue，searchMaster为当拉取不到数据时是否从主库拉取
    private IEntityValue entityValueGet(long pref, Map<Long, IEntityValue> prefEntityValueMaps, boolean searchMaster) throws SQLException {
        IEntityValue entityValue = prefEntityValueMaps.get(pref);
        if (null == entityValue && searchMaster) {
            Optional<IEntityValue> e1 = masterStorage.selectEntityValue(pref);
            return e1.orElse(null);
        }
//        if (null == entityValue) {
//            throw new SQLException(
//                    String.format("pref's entityValue could not be null in relation pool when have cref, need pref id : %d", pref));
//        }
        return entityValue;
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
