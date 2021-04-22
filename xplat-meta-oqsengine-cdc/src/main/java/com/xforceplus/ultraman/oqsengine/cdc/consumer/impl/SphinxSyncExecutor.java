package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.common.cdc.SkipRow;
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
    public int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics, Map<String, String> skips) throws SQLException {
        int synced = 0;
        List<OriginalEntity> storageEntityList = new ArrayList<>();
        long startTime = 0;
        for (RawEntry rawEntry : rawEntries) {
            OriginalEntity entity = null;
            String ret = null;

            try {
                //  获取记录
                entity = prepareForUpdateDelete(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId());
                //  是否跳过
                ret = skips.get(SkipRow.toSkipRow(entity.getCommitid(), entity.getId(), entity.getVersion(), entity.getOp()));
            } catch (Exception e) {
                //  组装数据失败，记录错误日志
                e.printStackTrace();
                doErrRecordOrRecover(cdcMetrics.getBatchId(), rawEntry.getId(), rawEntry.getCommitId(), DATA_FORMAT_ERROR, e.getMessage(), null);
                continue;
            }


            //  检查是否需要跳过该记录
            if (null != ret && !ret.isEmpty()) {
                try {
                    boolean errRec = ret.equals(SkipRow.Status.ERROR_RECORD.getStatus());

                    if (errRec) {
                        //  将跳过的Row记录到错误日志
                        doErrRecordOrRecover(cdcMetrics.getBatchId(), rawEntry.getId()
                                , rawEntry.getCommitId(), DATA_FORMAT_ERROR, "手动跳过", null);
                    }

                    logger.warn("[cdc-sync-executor] 设置手动跳过记录, id : {}, commitId : {}, version : {}, op : {}, errorRecord : {}"
                            , rawEntry.getId(), rawEntry.getCommitId(), entity.getVersion(), entity.getOp(), errRec);
                } catch (Exception e) {
                    //  ignore
                }
                continue;
            }

            //  加入更新列表
            storageEntityList.add(entity);
        }

        if (!storageEntityList.isEmpty()) {
            try {
                //  执行更新
                sphinxQLIndexStorage.saveOrDeleteOriginalEntities(storageEntityList);
            } catch (Exception e) {
                //  是否修复
                if (!doErrRecordOrRecover(cdcMetrics.getBatchId(), UN_KNOW_ID, UN_KNOW_ID, DATA_INSERT_ERROR, e.getMessage(), storageEntityList)) {
                    throw e;
                }
            }

            synced += storageEntityList.size();
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - startTime));
        }
        return synced;
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
    public boolean doErrRecordOrRecover(Long batchId, Long id, Long commitId, ErrorType errorType, String message, List<OriginalEntity> entities) throws SQLException {
        logger.warn("[cdc-sync-executor] batchId : {}, sphinx consume error will be record in cdcErrors,  id : {}, commitId : {}, message : {}"
                , batchId, id, commitId, null == message ? "unKnow" : message);

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();

        cdcErrorQueryCondition.setBatchId(batchId)
                .setId(id).setCommitId(commitId).setType(errorType.ordinal()).setStatus(FixedStatus.FIXED.ordinal()).setEqualStatus(false);

        try {
            Collection<CdcErrorTask> errorTasks = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);

            switch (errorType) {
                case DATA_FORMAT_ERROR:
                    if (null == errorTasks || errorTasks.isEmpty()) {
                        cdcErrorStorage.buildCdcError(
                                CdcErrorTask.buildErrorTask(seqNoGenerator.next(), batchId, id, commitId,
                                        DATA_FORMAT_ERROR.ordinal(), "{}", null == message ? "dataFormatError" : message)
                        );
                    }
                    return false;
                case DATA_INSERT_ERROR:
                    if (null == errorTasks || errorTasks.isEmpty()) {
                        cdcErrorStorage.buildCdcError(
                                CdcErrorTask.buildErrorTask(seqNoGenerator.next(), batchId, id, commitId,
                                        DATA_INSERT_ERROR.ordinal(), OriginalEntityUtils.toOriginalEntityStr(entities), null == message ? "dataInsertError" : message)
                        );
                        return false;
                    } else {
                        CdcErrorTask cdcErrorTask = errorTasks.iterator().next();

                        //  状态被设置为SUBMIT_FIX_REQ, 才能触发修复
                        if (cdcErrorTask.getStatus() == FixedStatus.SUBMIT_FIX_REQ.ordinal()) {

                            List<OriginalEntity> originalEntities = OriginalEntityUtils.toOriginalEntity(metaManager, cdcErrorTask.getOperationObject());
                            try {
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
            }
            return true;
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

    private IEntityClass getEntityClass(long id, List<CanalEntry.Column> columns) throws SQLException {
        for (int o = ENTITYCLASSL4.ordinal(); o >= ENTITYCLASSL0.ordinal(); o--) {
            Optional<OqsBigEntityColumns> op = getByOrdinal(o);
            if (op.isPresent()) {
                long entityId = getLongFromColumn(columns, op.get());
                /**
                 * 从大到小找到的第一个entityClass > 0的id即为selfEntityClassId
                 */
                if (entityId > ZERO) {
                    Optional<IEntityClass> entityClassOptional = metaManager.load(entityId);
                    if (entityClassOptional.isPresent()) {
                        return entityClassOptional.get();
                    }
                    logger.warn("[cdc-sync-executor] id [{}], entityClassId [{}] has no entityClass in meta.", id, entityId);
                    break;
                }
            }
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
            e.printStackTrace();
            throw new SQLException(String.format("[cdc-sync-executor] id [%d], attrStr [%s] JsonToObject error.", id, attrStr));
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
                .withOp(isDelete ? OperationType.DELETE.ordinal() : OperationType.UPDATE.ordinal())
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
