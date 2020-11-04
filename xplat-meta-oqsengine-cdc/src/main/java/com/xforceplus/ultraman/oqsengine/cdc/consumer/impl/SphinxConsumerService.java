package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;


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

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService consumerPool;

    @Resource(name = "masterStorage")
    private MasterStorage sqlMasterStorage;

    private int executionTimeout = 30 * 1000;

    private static final List<Long> successCommitIds = new ArrayList<>();
    private static Long maxUseTime = 0L;

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public boolean consume(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        clear();

        Set<RawEntry> syncDataList = filterSyncData(entries);
        if (!syncDataList.isEmpty()) {
            multiConsume(syncDataList);
        }
        sync(cdcMetrics);
        return true;
    }

    private void clear() {
        successCommitIds.clear();
        maxUseTime = 0L;
    }

    private void sync(CDCMetrics cdcMetrics) {
        if (!successCommitIds.isEmpty()) {
            cdcMetrics.setCommitList(successCommitIds);
        }
        if (maxUseTime > 0) {
            cdcMetrics.setMaxSyncUseTime(maxUseTime);
        }
    }

    private Set<RawEntry> filterSyncData(List<CanalEntry.Entry> entries) throws SQLException {
        long commitId = 0;
        Set<RawEntry> datas = Sets.newLinkedHashSet();
        for (CanalEntry.Entry entry : entries) {
            //  当前为一个事务的开始或结束
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN) {
                continue;
            } else if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND && commitId > 0) {
                successCommitIds.add(commitId);
                continue;
            }

            CanalEntry.RowChange rowChange = null;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            } catch (Exception e) {
                throw new SQLException(String.format("parse entry value failed, [%s], [%s]", entry.getStoreValue(), e));
            }

            CanalEntry.EventType eventType = rowChange.getEventType();

            //  只能支持INSERT/UPDATE/DELETE
            if (supportEventType(eventType)) {
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {

                    //  check need sync
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                    if(needSyncRow(columns)) {
                        //  更新
                        commitId = getLongFromColumn(columns, COMMITID);

                        RawEntry rawEntry = new RawEntry(getLongFromColumn(columns, ID),
                                entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList());

                        datas.add(rawEntry);
                    }
                }
            }
        }

        return datas;
    }

    private long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    private String getStringFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
    }

    private Boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
        return !booleanValue.isEmpty() && (booleanValue.equals("true") || !booleanValue.equals("0"));
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
            //  out of band, logger error
        }

        //  binlog记录在columns中顺序不对，需要遍历再找一次(通过名字)
        for (CanalEntry.Column value : columns) {
            if (compare.name().toLowerCase().equals(value.getName().toLowerCase())) {
                return column;
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

    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT) ||
                eventType.equals(CanalEntry.EventType.UPDATE) ||
                eventType.equals(CanalEntry.EventType.DELETE);
    }

    private void multiConsume(Set<RawEntry> batches) throws SQLException {
        CountDownLatch latch = new CountDownLatch(batches.size());
        List<Future> futures = new ArrayList(batches.size());

        for (RawEntry rawEntry : batches) {
            futures.add(consumerPool.submit(
                    new syncCallable(rawEntry, latch)));
        }

        try {
            if (!latch.await(executionTimeout, TimeUnit.MILLISECONDS)) {

                for (Future f : futures) {
                    f.cancel(true);
                }

                throw new SQLException("Query failed, timeout.");
            }
        } catch (InterruptedException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private class syncCallable implements Callable<Boolean> {

        private CountDownLatch latch;
        private RawEntry rawEntry;

        public syncCallable(RawEntry rawEntry, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                switch (rawEntry.getEventType()) {
                    case INSERT:
                        doBuildOrReplace(rawEntry.getColumns(), false);
                        break;
                    case UPDATE:
                        doBuildOrReplace(rawEntry.getColumns(), true);
                        break;
                    case DELETE:
                        doDelete(rawEntry.getColumns());
                        break;
                }

                syncMetrics(Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
                return true;
            } finally {
                latch.countDown();
            }
        }

        private synchronized void syncMetrics(long useTime) {
            if (maxUseTime < useTime) {
                maxUseTime = useTime;
            }
        }

        private void doDelete(List<CanalEntry.Column> columns) throws SQLException {
            long id = Long.parseLong(columns.get(0).getValue());
            sphinxQLIndexStorage.delete(id);
        }

        private void doBuildOrReplace(List<CanalEntry.Column> columns, boolean isReplace) throws SQLException {

            StorageEntity storageEntity = columnsToStorageEntity(columns);
            IEntityValue entityValue = sqlMasterStorage.toEntityValue(storageEntity.getId(), metaToFieldTypeMap(columns),
                                                getStringFromColumn(columns, ATTRIBUTE));

            sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, isReplace);
        }

        private StorageEntity columnsToStorageEntity(List<CanalEntry.Column> columns) throws SQLException {

            StorageEntity storageEntity = new StorageEntity(
                    getLongFromColumn(columns, ID),                   //  id
                    getLongFromColumn(columns, ENTITY),               //  entity
                    getLongFromColumn(columns, PREF),                 //  pref
                    getLongFromColumn(columns, CREF),                 //  cref
                    getLongFromColumn(columns, TX),                   //  tx
                    getLongFromColumn(columns, COMMITID),             //  commitid
                    null,                                   //  由sphinxQLIndexStorage内部转换  entityValue
                    null                                    //  由sphinxQLIndexStorage内部转换  entityValue
            );
            return storageEntity;
        }

        private Map<String, IEntityField> metaToFieldTypeMap(List<CanalEntry.Column> columns) throws SQLException {
            String meta = getStringFromColumn(columns, META);

            Map<String, IEntityField> results = new HashMap<>();
            for (String metas : JSON.parseArray(meta, String.class)) {
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
    }
}
