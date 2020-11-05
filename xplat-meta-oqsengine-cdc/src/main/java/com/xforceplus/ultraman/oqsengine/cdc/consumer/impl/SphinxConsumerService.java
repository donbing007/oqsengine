package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.collect.Maps;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;


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

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    private int executionTimeout = 30 * 1000;

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries) throws SQLException {

        CDCMetrics currentMetrics = new CDCMetrics(CDCStatus.CONNECTED);

        // 1.数据清洗
        Map<Long, RawEntry> syncDataList = filterSyncData(entries, currentMetrics);
        if (!syncDataList.isEmpty()) {
            //  2.数据消费, 由于数据清洗后保证不会存在同一行记录重复的情况,所以可以采用多线程的方式进行同步
            multiConsume(syncDataList, currentMetrics);
        }
        return currentMetrics;
    }

    /*  数据清洗
    * */
    private Map<Long, RawEntry> filterSyncData(List<CanalEntry.Entry> entries,  CDCMetrics currentMetrics) throws SQLException {
        long commitId = 0;
        Map<Long, RawEntry> datas = Maps.newLinkedHashMap();
        for (CanalEntry.Entry entry : entries) {
            //  不是RowData类型数据,将被过滤
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND && commitId > 0) {
                    //  事务结束时，必须将上一个事务的Commit_id加入到successCommitIds中
                    currentMetrics.getCommitList().add(commitId);
                }

                continue;
            }

            CanalEntry.RowChange rowChange = null;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            } catch (Exception e) {
                throw new SQLException(String.format("parse entry value failed, [%s], [%s]", entry.getStoreValue(), e));
            }

            CanalEntry.EventType eventType = rowChange.getEventType();

            //  支持的EventType类型为: [INSERT/UPDATE/DELETE]
            if (supportEventType(eventType)) {
                //  遍历RowData
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    //  获取一条完整的Row，只关心变化后的数据
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                    //  check need sync
                    //  由于主库同步后会在最后commit时再更新一次commit_id，所以对于binlog同步来说，
                    //  只需同步commit_id小于Long.MAX_VALUE的row
                    if(needSyncRow(columns)) {
                        //  更新
                        commitId = getLongFromColumn(columns, COMMITID);

                        RawEntry rawEntry = new RawEntry(
                                entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList());

                        //  由于Binlog数据是全量有序同步, 同一个ID多次更新, 只会取最后一次的数据进行同步
                        datas.put(getLongFromColumn(columns, ID), rawEntry);
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
        return booleanValue.isEmpty() ? null : (booleanValue.equals("true") || !booleanValue.equals("0"));
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

    private void multiConsume(Map<Long, RawEntry> batches, CDCMetrics currentMetrics) throws SQLException {
        CountDownLatch latch = new CountDownLatch(batches.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(batches.size());

        batches.forEach((key, value) -> futures.add(consumerPool.submit(
                new syncCallable(value, currentMetrics, latch))));

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

    private class syncCallable implements Callable<Boolean> {

        private CountDownLatch latch;
        private RawEntry rawEntry;
        private CDCMetrics currentMetrics;

        public syncCallable(RawEntry rawEntry, CDCMetrics currentMetrics, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
            this.currentMetrics = currentMetrics;
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
            if (currentMetrics.getMaxSyncUseTime() < useTime) {
                currentMetrics.setMaxSyncUseTime(useTime);
            }
        }

        private void doDelete(List<CanalEntry.Column> columns) throws SQLException {
            long id = Long.parseLong(columns.get(0).getValue());
            sphinxQLIndexStorage.delete(id);
        }

        private void doBuildOrReplace(List<CanalEntry.Column> columns, boolean isReplace) throws SQLException {

            StorageEntity storageEntity = columnsToStorageEntity(columns);

            IEntityValue entityValue = entityValueBuilder.build(storageEntity.getId(), metaToFieldTypeMap(columns),
                    getStringFromColumn(columns, ATTRIBUTE));

            sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, isReplace);
        }

        private StorageEntity columnsToStorageEntity(List<CanalEntry.Column> columns) throws SQLException {

            return new StorageEntity(
                    getLongFromColumn(columns, ID),                   //  id
                    getLongFromColumn(columns, ENTITY),               //  entity
                    getLongFromColumn(columns, PREF),                 //  pref
                    getLongFromColumn(columns, CREF),                 //  cref
                    getLongFromColumn(columns, TX),                   //  tx
                    getLongFromColumn(columns, COMMITID),             //  commitid
                    null,                                   //  由sphinxQLIndexStorage内部转换  entityValue
                    null                                     //  由sphinxQLIndexStorage内部转换  entityValue
            );
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
