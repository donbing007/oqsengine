package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType.DATA_FORMAT_ERROR;
import static com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType.DATA_INSERT_ERROR;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.*;
import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToList;

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

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator seqNoGenerator;

    @Resource
    private MetaManager metaManager;


    //  执行同步到Sphinx操作
    @Override
    public int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        int synced = 0;
        List<OriginalEntity> storageEntityList = new ArrayList<>();
        long startTime = 0;
        for (RawEntry rawEntry : rawEntries) {
            try {
                //  获取记录
                OriginalEntity entity = prepareForUpdateDelete(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId());

                //  加入更新列表
                storageEntityList.add(entity);
            } catch (Exception e) {
                //  组装数据失败，记录错误日志
                logger.warn("add to storageEntityList error, message : {}", e.toString());
                errorHandle(rawEntry.getColumns(), cdcMetrics.getBatchId(), e.getMessage());
            }
        }

        if (!storageEntityList.isEmpty()) {
            try {
                //  执行更新
                sphinxQLIndexStorage.saveOrDeleteOriginalEntities(storageEntityList);
            } catch (Exception e) {
                //  是否修复
                if (!recordOrRecover(cdcMetrics.getBatchId(), UN_KNOW_ID, UN_KNOW_ID, UN_KNOW_VERSION, UN_KNOW_OP, UN_KNOW_ID
                                                    , DATA_INSERT_ERROR, e.getMessage(), storageEntityList)) {
                    throw e;
                }
            }

            synced += storageEntityList.size();
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - startTime));
        }
        return synced;
    }

    @Override
    public boolean errorHandle(List<CanalEntry.Column> columns, Long batchId, String message) throws SQLException {
        Long id = getLongFromColumn(columns, ID, UN_KNOW_ID);
        Long commitId = getLongFromColumn(columns, COMMITID);

        Integer version = getIntegerFromColumn(columns, VERSION, UN_KNOW_VERSION);
        Integer op = getIntegerFromColumn(columns, OP, UN_KNOW_OP);

        Long entity = UN_KNOW_ID;
        try {
            entity = getEntity(columns);
        } catch (Exception e) {
            //  ignore

        }
        recordOrRecover(batchId, id, entity, version, op, commitId, DATA_FORMAT_ERROR, message, new ArrayList<>());

        return true;
    }

    /**
     * @param batchId
     * @param id
     * @param commitId
     * @param errorType
     * @param message
     * @param entities
     * @return 当返回为true时代表已经修复了错误数据，此时则cdc继续往下执行
     * 目前修复仅支持批量写index时的报错由运维工具修改OriginalEntity列表后重新提交(ErrorType = DATA_INSERT_ERROR)，
     * 单条格式错误不支持修复，即不支持(ErrorType = DATA_FORMAT_ERROR)
     * @throws SQLException
     */
    private boolean recordOrRecover(long batchId, long id, long entity, int version, int op, long commitId
                            , ErrorType errorType, String message, List<OriginalEntity> entities) throws SQLException {
        logger.warn("[cdc-sync-executor] batchId : {}, sphinx consume error will be record in cdcErrors,  id : {}, commitId : {}, message : {}"
                , batchId, id, commitId, null == message ? "unKnow" : message);

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();

        cdcErrorQueryCondition.setBatchId(batchId)
                .setId(id).setCommitId(commitId).setType(errorType.getType()).setStatus(FixedStatus.FIXED.getStatus()).setEqualStatus(false);

        try {
            Collection<CdcErrorTask> errorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
            if (null == errorTasks || errorTasks.isEmpty()) {
                cdcErrorStorage.buildCdcError(
                        CdcErrorTask.buildErrorTask(seqNoGenerator.next(), batchId, id, entity, version, op, commitId,
                                errorType.getType(), (null == entities) ? "{}" :  OriginalEntityUtils.toOriginalEntityStr(entities)
                                , null == message ? errorType.name() : message)
                );
                return false;
            }

            //  处理批量写入卡住
            if (errorType.equals(DATA_INSERT_ERROR)) {
                CdcErrorTask cdcErrorTask = errorTasks.iterator().next();

                //  状态为SUBMIT_FIX_REQ, 说明才能触发修复已被人工修复attribute
                if (cdcErrorTask.getStatus() == FixedStatus.SUBMIT_FIX_REQ.getStatus()) {

                    try {
                        //  将数据反序列为originalEntities
                        List<OriginalEntity> originalEntities = OriginalEntityUtils.toOriginalEntity(metaManager, cdcErrorTask.getOperationObject());

                        //  触发修复, 写index
                        sphinxQLIndexStorage.saveOrDeleteOriginalEntities(originalEntities);
                    } catch (Exception e) {
                        logger.warn("[cdc-sync-executor] fixed error, seqNo : [{}], batchId : [{}], message : [{}]"
                                , cdcErrorTask.getSeqNo(), cdcErrorTask.getBatchId(), e.getMessage());
                        //  失败需要将状态置为失败
                        cdcErrorStorage.submitRecover(cdcErrorTask.getSeqNo(), FixedStatus.FIX_ERROR, OriginalEntityUtils.toOriginalEntityStr(entities));
                        return false;
                    }

                    //  将error表中状态修改为Fixed,此时CDC数据已经写入，即使这里设置状态失败也忽略
                    try {
                        cdcErrorStorage.updateCdcError(cdcErrorTask.getSeqNo(), FixedStatus.FIXED);
                    } catch (Exception e) {
                        //  ignore
                    }

                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }


    //  同步时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    private long getEntity(List<CanalEntry.Column> columns) throws SQLException {
        for (int o = ENTITYCLASSL4.ordinal(); o >= ENTITYCLASSL0.ordinal(); o--) {
            Optional<OqsBigEntityColumns> op = getByOrdinal(o);
            if (op.isPresent()) {
                long entity = getLongFromColumn(columns, op.get());

                if (entity > ZERO) {
                    return entity;
                }
            }
        }
        return UN_KNOW_ID;
    }

    private IEntityClass getEntityClass(long id, List<CanalEntry.Column> columns) throws SQLException {
        long entityId = getEntity(columns);

        if (entityId > ZERO) {
            Optional<IEntityClass> entityClassOptional = metaManager.load(entityId);
            if (entityClassOptional.isPresent()) {
                return entityClassOptional.get();
            }
            logger.warn("[cdc-sync-executor] id [{}], entityClassId [{}] has no entityClass in meta.", id, entityId);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> attrCollection(long id, List<CanalEntry.Column> columns) throws SQLException {
        String attrStr = getStringFromColumn(columns, ATTRIBUTE);
        if (null == attrStr || attrStr.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return attributesToList(attrStr);
        } catch (Exception e) {
            String error = String.format("[cdc-sync-executor] id [%d], jsonToObject error, message : [%s], attrStr [%s] ", id, attrStr, e.getMessage());
            logger.warn(error);
            throw new SQLException(error);
        }
    }

    private OriginalEntity prepareForUpdateDelete(List<CanalEntry.Column> columns, long id, long commitId) throws SQLException {
        //  通过解析binlog获取

        IEntityClass entityClass = getEntityClass(id, columns);
        if (null == entityClass) {
            throw new SQLException(String.format("[cdc-sync-executor] id [%d], commitId [%d] has no entityClass...", id, commitId));
        }
        Collection<Object> attributes = attrCollection(id, columns);
        if (attributes.isEmpty()) {
            throw new SQLException(String.format("[cdc-sync-executor] id [%d], commitId [%d] has no attributes...", id, commitId));
        }

        boolean isDelete = getBooleanFromColumn(columns, DELETED);

        return OriginalEntity.Builder.anOriginalEntity()
                .withId(id)
                .withDeleted(isDelete)
                .withOp(isDelete ? OperationType.DELETE.getValue() : OperationType.UPDATE.getValue())
                .withVersion(getIntegerFromColumn(columns, VERSION))
                .withOqsMajor(getIntegerFromColumn(columns, OQSMAJOR))
                .withCreateTime(getLongFromColumn(columns, CREATETIME))
                .withUpdateTime(getLongFromColumn(columns, UPDATETIME))
                .withTx(getLongFromColumn(columns, TX))
                .withCommitid(commitId)
                .withEntityClass(entityClass)
                .withAttributes(attributes)
                .build();
    }
}
