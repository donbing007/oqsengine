package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;
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

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource
    private CDCMetricsService cdcMetricsService;

    //  保证蓄水池中以事务分割成不同的键值对。每个健值对以PREF为Key,以PREF对应的EntityValue为值,在一个批次结束时必须对该Map进行清理，
    //  删除当前所有的TransactionEnd commitId所对应的健值对
    private Map<Long, IEntityValue> relationPool;

    @Override
    public void consume(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        init(cdcMetrics);

        filterSyncData(entries, cdcMetrics);
    }

    //  将上一次的剩余信息设置回来
    private CDCMetrics init(CDCMetrics cdcMetrics) {

        reset();

        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues().size() > EMPTY_BATCH_SIZE) {
            relationPool.putAll(cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues());
        }

        return cdcMetrics;
    }

    /*
        数据清洗
    * */
    private void filterSyncData(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        for (CanalEntry.Entry entry : entries) {
            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND:
                    syncCommitListAndRestRelation(cdcMetrics); break;
                case ROWDATA:
                    internalDataSync(entry, cdcMetrics); break;
            }
        }

        //  当所有的数据都处理完，需要将relationPool同步到cdcMetrics中
        if (relationPool.size() > 0) {
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitEntityValues(relationPool);
        }
    }

    private void syncCommitListAndRestRelation(CDCMetrics cdcMetrics) {
        if (cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId() > INIT_ID) {
            //  事务结束时，将上一个事务的Commit_id加入到successCommitIds中
            cdcMetrics.getCdcAckMetrics().getCommitList().add(cdcMetrics.getCdcUnCommitMetrics().getLastUnCommitId());
        }

        reset();
    }

    //  每个Transaction的开始需要将relationPool清空
    private void reset() {
        relationPool = new HashMap<>();
    }

    private void internalDataSync(CanalEntry.Entry entry, CDCMetrics cdcMetrics) throws SQLException {
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
                    cdcMetrics.getCdcUnCommitMetrics().setLastUnCommitId(getLongFromColumn(columns, COMMITID));
                    RawEntry rawEntry = new RawEntry(
                            entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList());

                    singleConsume(rawEntry, cdcMetrics);
                } else {
                    addPrefEntityValue(columns);
                }
            }
        }
    }

    /*
        当存在子类时,将父类信息缓存在蓄水池中，等待子类进行合并
        蓄水池在每一次事务结束时进行判断，必须为空(代表一个事务中的父子类已全部同步完毕)
        父类会扔自己的EntityValue进去,子类会取出自己父类的EntityValue进行合并
    */
    private void addPrefEntityValue(List<CanalEntry.Column> columns) throws SQLException {
        //  有子类, 将父类的EntityValue存入的relationMap中
        if (getLongFromColumn(columns, CREF) > ZERO) {
            long id = getLongFromColumn(columns, ID);

            IEntityValue entityValue = entityValueBuilder.build(id, metaToFieldTypeMap(columns),
                    getStringFromColumn(columns, ATTRIBUTE));

            relationPool.put(id, entityValue);
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

    private void singleConsume(RawEntry rawEntry, CDCMetrics cdcMetrics) throws SQLException {
        if (isDelete(rawEntry.getColumns())) {
            doDelete(rawEntry.getColumns());
        } else {
            doBuildOrReplace(rawEntry.getColumns(), true);
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

        IEntityValue entityValue = null;

        //  是父类
        if (cref > 0) {
            //  通过自己的ID拿到对应的EntityValue
            entityValue = entityValueGet(id);
        } else {
            entityValue = entityValueBuilder.build(storageEntity.getId(), metaToFieldTypeMap(columns),
                    getStringFromColumn(columns, ATTRIBUTE));
        }
//        entityValue = entityValueBuilder.build(storageEntity.getId(), metaToFieldTypeMap(columns),
//                getStringFromColumn(columns, ATTRIBUTE));

        /*
            有父类, 合并父类entityValue
        */
        if (pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = entityValueGet(pref);
            entityValue.addValues(entityValueF.values());
        }

        sphinxQLIndexStorage.buildOrReplace(storageEntity, entityValue, isReplace);
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

    private IEntityValue entityValueGet(long pref) throws SQLException {
        IEntityValue entityValue = relationPool.get(pref);
        if (null == entityValue) {
            throw new SQLException("pref's entityValue could not be null in relation pool when have cref.");
        }
        return entityValue;
    }
}
