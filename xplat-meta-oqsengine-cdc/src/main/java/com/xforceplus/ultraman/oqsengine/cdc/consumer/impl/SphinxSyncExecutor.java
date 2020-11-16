package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.google.common.collect.Maps;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.*;
import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType.fromRawType;

/**
 * desc :
 * name : SphinxSyncService
 *
 * @author : xujia
 * date : 2020/11/13
 * @since : 1.8
 */
public class SphinxSyncExecutor {

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

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
        if (!rawEntries.isEmpty()) {
            if (isSingleSyncConsumer || rawEntries.size() <= SINGLE_CONSUMER_MAX_ROW) {
                for (RawEntry rawEntry : rawEntries) {
                    sphinxConsume(rawEntry, prefEntityValueMaps, cdcMetrics);
                }
            } else {
                multiConsume(rawEntries, prefEntityValueMaps, cdcMetrics);
            }
        }
        return rawEntries.size();
    }

    //  多线程作业
    private void multiConsume(List<RawEntry> rawEntries, Map<Long, IEntityValue> prefEntityValueMaps, CDCMetrics cdcMetrics) throws SQLException {
        CountDownLatch latch = new CountDownLatch(rawEntries.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(rawEntries.size());

        rawEntries.forEach((value) -> futures.add(consumerPool.submit(
            new SphinxSyncExecutor.SyncSphinxCallable(value, cdcMetrics, prefEntityValueMaps, latch))));

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
    private void sphinxConsume(RawEntry rawEntry, Map<Long, IEntityValue> prefEntityValueMaps, CDCMetrics cdcMetrics) throws SQLException {
        if (isDelete(rawEntry.getColumns())) {
            doDelete(rawEntry.getColumns());
        } else {
            doReplace(rawEntry.getColumns(), prefEntityValueMaps);
        }

        syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
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
    private void doReplace(List<CanalEntry.Column> columns, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {

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
            entityValue = entityValueGet(id, prefEntityValueMaps);
        } else {
            entityValue = buildEntityValue(
                storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));
        }

        /*
            有父类, 合并父类entityValue
        */
        if (pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = entityValueGet(pref, prefEntityValueMaps);
            entityValue.addValues(entityValueF.values());
        }

        /*
            replacement is always true, 所有的OQS同步对于CDC来说都是replace
         */
        sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, true);
    }

    //  IEntityValue build
    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

    //  meat -> Map<String, IEntityField>
    private Map<String, IEntityField> metaToFieldTypeMap(String meta) throws SQLException {

        Map<String, IEntityField> results = new HashMap<>();
        List<String> metaList = null;
        try {
            metaList = JSON.parseArray(meta, String.class);
        } catch (Exception e) {
            throw new SQLException(
                String.format("parse meta to array failed, [%s]", meta));
        }
        for (String metas : metaList) {
            String[] sMetas = metas.split(SPLITTER);
            if (sMetas.length != SPLIT_META_LENGTH) {
                throw new SQLException(
                    String.format("parse meta failed. meta value length error, should be [%d], actual [%d], meta [%s]",
                        SPLIT_META_LENGTH, sMetas.length, metas));
            }

            Long id = Long.parseLong(sMetas[0]);
            FieldType fieldType = fromRawType(sMetas[1]);

            results.put(sMetas[0], new EntityField(id, null, fieldType));
        }

        return results;
    }

    //  通过pref获得IEntityValue
    private IEntityValue entityValueGet(long pref, Map<Long, IEntityValue> prefEntityValueMaps) throws SQLException {
        IEntityValue entityValue = prefEntityValueMaps.get(pref);
        if (null == entityValue) {
            throw new SQLException("pref's entityValue could not be null in relation pool when have cref.");
        }
        return entityValue;
    }

    //  多线程作业封装类
    private class SyncSphinxCallable implements Callable<Boolean> {
        private CountDownLatch latch;
        private RawEntry rawEntry;
        private CDCMetrics cdcMetrics;
        private Map<Long, IEntityValue> prefEntityValueMaps;

        public SyncSphinxCallable(RawEntry rawEntry, CDCMetrics cdcMetrics, Map<Long, IEntityValue> prefEntityValueMaps, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
            this.cdcMetrics = cdcMetrics;
            this.prefEntityValueMaps = prefEntityValueMaps;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                sphinxConsume(rawEntry, prefEntityValueMaps, cdcMetrics);
            } finally {
                latch.countDown();
            }
            return true;
        }
    }
}
