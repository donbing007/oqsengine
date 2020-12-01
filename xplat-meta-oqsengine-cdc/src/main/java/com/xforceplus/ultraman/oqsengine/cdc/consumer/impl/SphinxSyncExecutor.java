package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.devops.cdcerror.CdcErrorStorage;
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

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
import static com.xforceplus.ultraman.oqsengine.common.error.CommonErrors.INVALID_ENTITY_ID;
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
public class SphinxSyncExecutor implements SyncExecutor {

    final Logger logger = LoggerFactory.getLogger(SphinxSyncExecutor.class);

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

    @Resource(name = "masterStorage")
    private MasterStorage masterStorage;

    @Resource(name = "cdcErrorStorage")
    private CdcErrorStorage cdcErrorStorage;

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator seqNoGenerator;

    //  执行同步到Sphinx操作
    public int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        int synced = 0;
        List<StorageEntity> storageEntityList = new ArrayList<>();
        long startTime = 0;
        for (RawEntry rawEntry : rawEntries) {
            try {
                boolean isDelete = getBooleanFromColumn(rawEntry.getColumns(), DELETED);
                if (isDelete) {
                    synced += doDelete(rawEntry.getId(), rawEntry.getCommitId());
                    syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - rawEntry.getExecuteTime()));
                } else {
                    //  加入批量更新Map中
                    startTime = rawEntry.getExecuteTime();
                    storageEntityList.add(
                            prepareForReplace(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId()));
                }
            } catch (Exception e) {
                errorRecord(rawEntry.getId(), rawEntry.getCommitId(), e.getMessage());
            }
        }

        if (!storageEntityList.isEmpty()) {
            synced += sphinxQLIndexStorage.batchSave(storageEntityList, true, true);
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - startTime));
        }
        return synced;
    }

    public void errorRecord(long id, long commitId, String message) throws SQLException {
        logger.warn("sphinx consume error will be record in cdcerrors,  id : {}, commitId : {}, message : {}", id, commitId, message);
        cdcErrorStorage.buildCdcError(CdcErrorTask.buildErrorTask(seqNoGenerator.next(), id, commitId, message));
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

    //  同步时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    private StorageEntity prepareForReplace(List<CanalEntry.Column> columns, long id, long commitId) throws SQLException {
        //  通过解析binlog获取

        long cref = getLongFromColumn(columns, CREF);
        long pref = getLongFromColumn(columns, PREF);

        int oqsMajor = getIntegerFromColumn(columns, OQSMAJOR);

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
        storageEntity.setOqsmajor(oqsMajor);

        //  取自己的entityValue
        IEntityValue entityValue = buildEntityValue(
                storageEntity.getId(), getStringFromColumn(columns, META), getStringFromColumn(columns, ATTRIBUTE));

         /*
            老数据，有父类
         */
        if (oqsMajor != OqsVersion.MAJOR && pref > 0) {
            //  通过pref拿到父类的EntityValue
            IEntityValue entityValueF = queryEntityValue(pref);
            entityValue.addValues(entityValueF.values());
        }

        //  将entityValue转换为JsonFields和FullFields并写入storageEntity
        sphinxQLIndexStorage.entityValueToStorage(storageEntity, entityValue);

        return storageEntity;
    }

    //  IEntityValue build
    private IEntityValue buildEntityValue(Long id, String meta, String attribute) throws SQLException {
        return entityValueBuilder.build(id, metaToFieldTypeMap(meta), attribute);
    }

    //  通过pref获得IEntityValue，当拉取不到数据时从主库拉取
    private IEntityValue queryEntityValue(long pref) {
        while (true) {
            try {
                return masterStorage.selectEntityValue(pref).orElse(null);
            } catch (Exception e) {
                logger.warn("entityValueGet from master db error, will retry..., id : {}, " +
                        "message : {}", pref, e.getMessage());
                sleepNoInterrupted(SECOND);
            }
        }
    }

    private void sleepNoInterrupted(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException ex) {
            //  ignore
        }
    }
}
