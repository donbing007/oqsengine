package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.RawEntry;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns.*;
import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType.fromRawType;

/**
 * desc :
 * name : SphinxConsumerService
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class SphinxConsumerService implements ConsumerService {

    final Logger logger = LoggerFactory.getLogger(SphinxConsumerService.class);

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService consumerPool;

    private int executionTimeout = 30 * 1000;

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCUnCommitMetrics cdcUnCommitMetrics) throws SQLException {
        CDCMetrics cdcMetrics = init(cdcUnCommitMetrics, batchId);

        filterAndSyncData(entries, cdcMetrics);

        logger.info("batchId {}, success sync raw data : {}",
                cdcMetrics.getBatchId(), cdcMetrics.getCdcUnCommitMetrics().getExecuteJobCount());

        return cdcMetrics;
    }

    @Override
    public void shutdown() {
        logger.info("Start closing the consumer worker thread.....");
        ExecutorHelper.shutdownAndAwaitTermination(consumerPool, 3600);
    }

    private CDCMetrics init(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        //  将上一次的剩余信息设置回来
        CDCMetrics cdcMetrics = new CDCMetrics();

        if (null != cdcUnCommitMetrics) {
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(cdcUnCommitMetrics.getUnCommitId());
            cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues().putAll(cdcUnCommitMetrics.getUnCommitEntityValues());
        }
        cdcMetrics.setBatchId(batchId);

        return cdcMetrics;
    }

    /*
        数据清洗、同步
    * */
    private void filterAndSyncData(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        int syncSize = ZERO;
        //  需要同步的列表
        List<RawEntry> rawEntries = new ArrayList<>();
        for (CanalEntry.Entry entry : entries) {
            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {

                case TRANSACTIONEND:
                    //  同步rawEntries到Sphinx
                    multiSyncSphinx(rawEntries, cdcMetrics);

                    //  每次Transaction结束,将unCommitId加入到commitList中
                    if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitId() > INIT_ID) {
                        cdcMetrics.getCdcAckMetrics().getCommitList().add(cdcMetrics.getCdcUnCommitMetrics().getUnCommitId());
                        cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(INIT_ID);
                    }

                    //  统计同步的数量
                    syncSize += rawEntries.size();

                    //  每个Transaction的结束需要将rawEntries清空
                    rawEntries.clear();

                    //  每个Transaction的结束需要将unCommitEntityValues清空
                    cdcMetrics.getCdcUnCommitMetrics().setUnCommitEntityValues(new ConcurrentHashMap<>());
                    break;
                case ROWDATA:
                    rawEntries.addAll(internalDataSync(entry, cdcMetrics));
                    break;
            }
        }

        //  unCommit RawEntry should sync to sphinx
        if (!rawEntries.isEmpty()) {
            multiSyncSphinx(rawEntries, cdcMetrics);
            syncSize += rawEntries.size();
        }

        cdcMetrics.getCdcUnCommitMetrics().setExecuteJobCount(syncSize);
    }

    private void multiSyncSphinx(List<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        if (!rawEntries.isEmpty()) {
            if (rawEntries.size() == SINGLE_CONSUMER) {
                sphinxConsume(rawEntries.get(0), cdcMetrics);
            } else {
                multiConsume(rawEntries, cdcMetrics);
            }
        }
    }

    private void multiConsume(List<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        CountDownLatch latch = new CountDownLatch(rawEntries.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(rawEntries.size());

        rawEntries.forEach((value) -> futures.add(consumerPool.submit(
                new SyncSphinxCallable(value, cdcMetrics, latch))));

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

    private List<RawEntry> internalDataSync(CanalEntry.Entry entry, CDCMetrics cdcMetrics) throws SQLException {
        List<RawEntry> rawEntries = new ArrayList<>();
        CanalEntry.RowChange rowChange = null;
        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

        } catch (Exception e) {
            throw new SQLException(String.format("parse entry value failed, [%s], [%s]", entry.getStoreValue(), e));
        }
        CanalEntry.EventType eventType = rowChange.getEventType();
        if (supportEventType(eventType)) {
            //  遍历RowData
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                //  获取一条完整的Row，只关心变化后的数据
                List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                //  check need sync
                //  由于主库同步后会在最后commit时再更新一次commit_id，所以对于binlog同步来说，
                //  只需同步commit_id小于Long.MAX_VALUE的row
                if (needSyncRow(columns)) {
                    //  更新
                    cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(getLongFromColumn(columns, COMMITID));
                    RawEntry rawEntry = new RawEntry(
                            entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList());
                    rawEntries.add(rawEntry);
                } else {
                    addPrefEntityValue(columns, cdcMetrics);
                }
            }
        }

        return rawEntries;
    }

    /*
        当存在子类时,将父类信息缓存在蓄水池中，等待子类进行合并
        蓄水池在每一次事务结束时进行判断，必须为空(代表一个事务中的父子类已全部同步完毕)
        父类会扔自己的EntityValue进去,子类会取出自己父类的EntityValue进行合并
    */
    private void addPrefEntityValue(List<CanalEntry.Column> columns, CDCMetrics cdcMetrics) throws SQLException {
        //  有子类, 将父类的EntityValue存入的relationMap中
        if (getLongFromColumn(columns, CREF) > ZERO) {
            long id = getLongFromColumn(columns, ID);

            IEntityValue entityValue = buildEntityValue(
                    id, getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));


            cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues().put(id, entityValue);
        }
    }


    private long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    private String getStringFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
    }

    private boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
        return booleanValue.equals("true");
    }

    private CanalEntry.Column getColumnWithoutNull(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        CanalEntry.Column column = existsColumn(columns, oqsBigEntityColumns);
        if (null == column || column.getValue().isEmpty()) {
            throw new SQLException(String.format("%s must not be null.", oqsBigEntityColumns.name()));
        }
        return column;
    }

    private CanalEntry.Column existsColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns compare) {
        CanalEntry.Column column = null;
        try {
            //  通过下标找一次，如果名字相同，则返回当前column
            column = columns.get(compare.ordinal());
            if (column.getName().toLowerCase().equals(compare.name().toLowerCase())) {
                return column;
            }
        } catch (Exception e) {
            //  out of band, logger error?
        }

        //  binlog记录在columns中顺序不对，需要遍历再找一次(通过名字)
        for (CanalEntry.Column value : columns) {
            if (compare.name().toLowerCase().equals(value.getName().toLowerCase())) {
                return value;
            }
        }

        return null;
    }

    //  只同步小于MAX_VALUE数据
    private boolean needSyncRow(List<CanalEntry.Column> columns) throws SQLException {
        if (null == columns || columns.size() == EMPTY_COLUMN_SIZE) {
            throw new SQLException("columns must not be null");
        }

        CanalEntry.Column column = existsColumn(columns, COMMITID);
        if (null == column) {
            throw new SQLException("sync row failed, unknown column commitid.");
        }
        return Long.parseLong(column.getValue()) < Long.MAX_VALUE;
    }

    /*
        由于OQS主库的删除都是逻辑删除，实际上是进行了UPDATE操作
     */
    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT) ||
                eventType.equals(CanalEntry.EventType.UPDATE);
    }

    private void sphinxConsume(RawEntry rawEntry, CDCMetrics cdcMetrics) throws SQLException {
        if (isDelete(rawEntry.getColumns())) {
            doDelete(rawEntry.getColumns());
        } else {
            doReplace(rawEntry.getColumns(), cdcMetrics);
        }

        syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
    }

    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    private void doDelete(List<CanalEntry.Column> columns) throws SQLException {
        sphinxQLIndexStorage.delete(getLongFromColumn(columns, ID));
    }

    private boolean isDelete(List<CanalEntry.Column> columns) throws SQLException {
        return getBooleanFromColumn(columns, DELETED);
    }

    private void doReplace(List<CanalEntry.Column> columns, CDCMetrics cdcMetrics) throws SQLException {

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
            entityValue = entityValueGet(id, cdcMetrics);
        } else {
            entityValue = buildEntityValue(
                    storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));
        }

        /*
            有父类, 合并父类entityValue
        */
        if (pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = entityValueGet(pref, cdcMetrics);
            entityValue.addValues(entityValueF.values());
        }

        /*
            replacement is always true, 所有的OQS同步对于CDC来说都是replace
         */
        sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, true);
    }

    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

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

    private IEntityValue entityValueGet(long pref, CDCMetrics cdcMetrics) throws SQLException {
        IEntityValue entityValue = cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues().get(pref);
        if (null == entityValue) {
            throw new SQLException("pref's entityValue could not be null in relation pool when have cref.");
        }
        return entityValue;
    }

    private class SyncSphinxCallable implements Callable<Boolean> {
        private CountDownLatch latch;
        private RawEntry rawEntry;
        private CDCMetrics cdcMetrics;

        public SyncSphinxCallable(RawEntry rawEntry, CDCMetrics cdcMetrics, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
            this.cdcMetrics = cdcMetrics;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                sphinxConsume(rawEntry, cdcMetrics);
            } finally {
                latch.countDown();
            }
            return true;
        }
    }
}
