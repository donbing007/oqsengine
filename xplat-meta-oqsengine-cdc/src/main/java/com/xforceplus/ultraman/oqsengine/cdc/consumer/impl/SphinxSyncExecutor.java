package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.*;

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

    final ObjectMapper jsonMapper = new ObjectMapper();

    //  执行同步到Sphinx操作
    public int execute(Collection<RawEntry> rawEntries, CDCMetrics cdcMetrics) throws SQLException {
        int synced = 0;
        List<OriginalEntity> storageEntityList = new ArrayList<>();
        long startTime = 0;
        for (RawEntry rawEntry : rawEntries) {
            try {
                storageEntityList.add(prepareForUpdateDelete(rawEntry.getColumns(), rawEntry.getId(), rawEntry.getCommitId()));
            } catch (Exception e) {
                //  组装数据失败，记录错误日志
                e.printStackTrace();
                errorRecord(cdcMetrics.getBatchId(), rawEntry.getId(), rawEntry.getCommitId(), e.getMessage());
            }
        }

        if (!storageEntityList.isEmpty()) {
            sphinxQLIndexStorage.saveOrDeleteOriginalEntities(storageEntityList);

            synced += storageEntityList.size();
            syncMetrics(cdcMetrics, Math.abs(System.currentTimeMillis() - startTime));
        }
        return synced;
    }

    public void errorRecord(long batchId, long id, long commitId, String message) throws SQLException {
        logger.warn("[cdc-sync-executor] batch : {}, sphinx consume error will be record in cdcerrors,  id : {}, commitId : {}, message : {}"
                , batchId, id, commitId, null == message ? "unKnow" : message);
        cdcErrorStorage.buildCdcError(CdcErrorTask.buildErrorTask(seqNoGenerator.next(), id, commitId, null == message ? "unKnow" : message));
    }

    //  同步时间
    private synchronized void syncMetrics(CDCMetrics cdcMetrics, long useTime) {
        if (cdcMetrics.getCdcAckMetrics().getMaxSyncUseTime() < useTime) {
            cdcMetrics.getCdcAckMetrics().setMaxSyncUseTime(useTime);
        }
    }

    private IEntityClass getEntityClass(List<CanalEntry.Column> columns) throws SQLException {
        for (int o = ENTITYCLASSL4.ordinal(); o >= ENTITYCLASSL0.ordinal(); o--) {
            Optional<OqsBigEntityColumns> op = getByOrdinal(o);
            if (op.isPresent()) {
                long id = getLongFromColumn(columns, op.get());
                /**
                 * 从大到小找到的第一个entityClass > 0的id即为selfEntityClassId
                 */
                if (id > ZERO) {
                    Optional<IEntityClass> entityClassOptional = metaManager.load(id);
                    if (entityClassOptional.isPresent()) {
                        return entityClassOptional.get();
                    }
                    logger.warn("entityId [{}] has no entityClass in meta.", id);
                    break;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> attrCollection(List<CanalEntry.Column> columns) throws SQLException {
        List<Object> attributes = new ArrayList<>();
        String attrStr = getStringFromColumn(columns, ATTRIBUTE);
        if (null == attrStr || attrStr.isEmpty()) {
            return attributes;
        }
        try {
            Map<String, Object> keyValues = jsonMapper.readValue(attrStr, Map.class);
            keyValues.forEach(
                    (k, v) -> {
                        attributes.add(k);
                        attributes.add(v);
                    }
            );
            return attributes;
        } catch (JsonProcessingException e) {
            throw new SQLException("attrStr Json to object error");
        }
    }

    private OriginalEntity prepareForUpdateDelete(List<CanalEntry.Column> columns, long id, long commitId) throws SQLException {
        //  通过解析binlog获取

        IEntityClass entityClass = getEntityClass(columns);
        if (null == entityClass) {
            throw new SQLException(String.format("id [%d], commitId [%d] has no entityClass...", id, commitId));
        }
        Collection<Object> attributes = attrCollection(columns);
        if (attributes.isEmpty()) {
            throw new SQLException(String.format("id [%d], commitId [%d] has no attributes...", id, commitId));
        }

        return OriginalEntity.Builder.anOriginalEntity()
                .withId(id)
                .withDeleted(getBooleanFromColumn(columns, DELETED))
                .withOp(getIntegerFromColumn(columns, OP))
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
