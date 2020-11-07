package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.collect.Maps;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.RawEntry;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
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

    @Resource(name = "masterStorage")
    private MasterStorage masterStorage;

    @Resource(name = "cdcConsumerPool")
    private ExecutorService consumerPool;

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource
    private CDCMetricsService cdcMetricsService;

    private int executionTimeout = 30 * 1000;

    private static long commitId = INIT_ID;

    //  保证蓄水池中以事务分割成不同的键值对。每个健值对以PREF为Key,以PREF对应的EntityValue为值,在一个批次结束时必须对该Map进行清理，
    //  删除当前所有的TransactionEnd commitId所对应的健值对
    private static Map<Long, Map<Long, IEntityValue>> relationPool = new ConcurrentHashMap<>();

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries, CDCUnCommitMetrics cdcUnCommitMetrics) throws SQLException, CloneNotSupportedException {
        CDCMetrics cdcMetrics = init(cdcUnCommitMetrics);
        // 1.数据清洗
        Map<Long, RawEntry> syncDataList = filterSyncData(entries, cdcMetrics);
        if (!syncDataList.isEmpty()) {
            //  2.数据消费, 由于数据清洗后保证不会存在同一行记录重复的情况,所以可以采用多线程的方式进行同步
            multiConsume(syncDataList, cdcMetrics);
        }
        return cdcMetrics;
    }

    private CDCMetrics init(CDCUnCommitMetrics cdcUnCommitMetrics) throws CloneNotSupportedException {
        CDCMetrics cdcMetrics = new CDCMetrics(cdcUnCommitMetrics);

        //  在TRANSACTIONEND标志位升级为CommitId
        commitId = INIT_ID;

        if (cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId() > INIT_ID &&
                cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValueFs().size() > EMPTY_BATCH_SIZE) {
            relationPool.putIfAbsent(cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId(),
                                        cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValueFs());
        }

        return cdcMetrics;
    }

    /*
        数据清洗
    * */
    private Map<Long, RawEntry> filterSyncData(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        Map<Long, RawEntry> datas = Maps.newLinkedHashMap();

        for (CanalEntry.Entry entry : entries) {
            //  不是RowData类型数据,将被过滤
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND &&
                                    cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId() > INIT_ID) {
                    //  事务结束时，必须将上一个事务的Commit_id加入到successCommitIds中
                    cdcMetrics.getCdcAckMetrics().getCommitList().add(cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId());

                    commitId = cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId();

                    //这里必须使用putIfAbsent，因为可能存在上个批次未结束的
                    relationPool.putIfAbsent(commitId, new ConcurrentHashMap<>());
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
                    if (needSyncRow(columns)) {
                        //  更新
                        cdcMetrics.getCdcUnCommitMetrics().setLastUnCommitId(getLongFromColumn(columns, COMMITID));

                        RawEntry rawEntry = new RawEntry(
                                entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList());

                        //  由于Binlog数据是全量有序同步, 同一个ID多次更新, 只会取最后一次的数据进行同步
                        datas.put(getLongFromColumn(columns, ID), rawEntry);
                    }
                }
            }
        }

        //  保证最后一个unCommitId也加入到relationPool中
        if (cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId() > commitId) {
            relationPool.put(cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId(), new ConcurrentHashMap<>());
        }
        return datas;
    }


    private long getLongFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return Long.parseLong(getColumnWithoutNull(columns, oqsBigEntityColumns).getValue());
    }

    private String getStringFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        return getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
    }

    private boolean getBooleanFromColumn(List<CanalEntry.Column> columns, OqsBigEntityColumns oqsBigEntityColumns) throws SQLException {
        String booleanValue = getColumnWithoutNull(columns, oqsBigEntityColumns).getValue();
        return booleanValue.equals("true") || !booleanValue.equals("0");
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

    private void multiConsume(Map<Long, RawEntry> batches, CDCMetrics cdcMetrics) throws SQLException {
        CountDownLatch latch = new CountDownLatch(batches.size());
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(batches.size());

        batches.forEach((key, value) -> futures.add(consumerPool.submit(
                new syncCallable(value, cdcMetrics, latch))));

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
        private CDCMetrics cdcMetrics;

        public syncCallable(RawEntry rawEntry, CDCMetrics cdcMetrics, CountDownLatch latch) {
            this.rawEntry = rawEntry;
            this.latch = latch;
            this.cdcMetrics = cdcMetrics;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                if (isDelete(rawEntry.getColumns())) {
                    doDelete(rawEntry.getColumns());
                } else {
                    doBuildOrReplace(rawEntry.getColumns(), true);
                }

                syncMetrics(Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
                return true;
            } finally {
                latch.countDown();
            }
        }

        private synchronized void syncMetrics(long useTime) {
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

        private void doBuildOrReplace(List<CanalEntry.Column> columns, boolean isReplace) throws SQLException {

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

            IEntityValue entityValue = entityValueBuilder.build(storageEntity.getId(), metaToFieldTypeMap(columns),
                    getStringFromColumn(columns, ATTRIBUTE));

            /*
              当存在子类时,将父类信息缓存在蓄水池中，等待子类进行合并
              蓄水池在每一次事务结束时进行判断，必须为空(代表一个事务中的父子类已全部同步完毕)
              父类会扔自己的EntityValue进去,子类会取出自己父类的EntityValue进行合并
             */
            if (cref > 0) {
                entityValuePrefAdd(commitid, id, entityValue);
            }

            /*
                取父类ID所对应的entityValueF，进行合并
             */
            if (pref > 0) {
                IEntityValue entityValueF = entityValuePrefGet(commitid, pref);
                entityValue.addValues(entityValueF.values());
            }

            sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, isReplace);

            if (pref > 0) {
                relationPool.remove(id);
            }
        }

        private void entityValuePrefAdd(long commitid, long id, IEntityValue entityValue) throws SQLException {
            Map<Long, IEntityValue> entityValueMap = relationPool.get(commitid);
            if (null == entityValueMap) {
                throw new SQLException("pref-cref entityValue pool must not be init during add.");
            }

            if (null != entityValueMap.putIfAbsent(id, entityValue)) {
                throw new SQLException("pref-cref entityValue not be duplicated in relation pool");
            }
        }

        private IEntityValue entityValuePrefGet(long commitid, long pref) throws SQLException {
            Map<Long, IEntityValue> entityValueMap = relationPool.get(commitid);
            //  代表宕机或重启
            if (null == entityValueMap) {
                throw new SQLException("pref-cref entityValue pool must not be init during query.");
            }
            IEntityValue entityValue = entityValueMap.get(pref);
            if (null == entityValue) {
                throw new SQLException("pref's entityValue could not be null in relation pool when have cref.");
            }
            return entityValue;
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
