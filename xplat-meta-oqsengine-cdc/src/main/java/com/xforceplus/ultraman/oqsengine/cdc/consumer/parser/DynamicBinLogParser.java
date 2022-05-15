package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getBooleanFromColumn;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getIntegerFromColumn;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getStringFromColumn;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getStringWithoutNullCheck;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.NO_TRANSACTION_COMMIT_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ATTRIBUTE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.COMMITID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.CREATETIME;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.DELETED;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ENTITYCLASSL0;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ENTITYCLASSL4;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.OQSMAJOR;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.PROFILE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.TX;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.UPDATETIME;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.VERSION;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.getByOrdinal;
import static com.xforceplus.ultraman.oqsengine.storage.master.utils.OriginalEntityUtils.attributesToMap;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.CDCConstant;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.CommonUtils;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.DevOpsUtils;
import com.xforceplus.ultraman.oqsengine.metadata.constant.Constant;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.devops.DevOpsCdcMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class DynamicBinLogParser implements BinLogParser {

    final Logger logger = LoggerFactory.getLogger(DynamicBinLogParser.class);

    @Override
    public void parse(List<CanalEntry.Column> columns, ParserContext parserContext, ParseResult parseResult) {
        long id = UN_KNOW_ID;
        long commitId = UN_KNOW_ID;
        try {
            //  获取CommitID
            commitId = getLongFromColumn(columns, COMMITID);

            //  获取ID
            id = getLongFromColumn(columns, ID);
            if (parseResult.getStartId() == CDCConstant.NOT_INIT_START_ID) {
                parseResult.setStartId(id);
            }

            IEntityClass entityClass = getEntityClass(id, columns, parserContext);

            //  生成执行对象
            OqsEngineEntity oqsEngineEntity =
                toOriginalEntity(entityClass, id, commitId, columns, parserContext);

            //  动态结构直接加入到结果对象中
            parseResult.getFinishEntries().put(id, oqsEngineEntity);
        } catch (Exception e) {
            if (commitId != CommitHelper.getUncommitId()) {
                if (commitId != UN_KNOW_ID) {
                    parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().add(commitId);
                }

                //  加入错误列表.
                parseResult.addError(id, commitId,
                    String.format("batch : %d, pos : %d, parse columns failed, message : %s",
                        parserContext.getCdcMetrics().getBatchId(), parseResult.getPos(), e.getMessage()));
            }

            return;
        }

        //  判断当前的commitId是否需要readyCheck
        addToReadyChecks(commitId, id, parserContext, parseResult);

        //  自增游标
        parseResult.finishOne();
    }

    /**
     * 按照当前的columns生成一条最终的索引记录.
     *
     * @param entityClass   entityClass.
     * @param id            数据主键ID.
     * @param commitId      提交号.
     * @param columns       原始数据.
     * @param parserContext 上下文.
     */
    private OqsEngineEntity toOriginalEntity(IEntityClass entityClass, long id, long commitId,
                                             List<CanalEntry.Column> columns, ParserContext parserContext)
        throws SQLException {

        OqsEngineEntity.Builder builder = OqsEngineEntity.Builder.anOriginalEntity();

        //  主键ID
        builder.withId(id);
        //  entityClass
        builder.withEntityClass(entityClass);
        //  entityClassRef
        builder.withEntityClassRef(entityClass.ref());

        //  动态结构需要直接加入attr
        Map<String, Object> attributes = attrCollection(id, columns);
        if (attributes.isEmpty()) {
            throw new SQLException(
                String.format("[dynamic-binlog-parser] id [%d], commitId [%d] has no attributes...", id, commitId));
        }
        builder.withAttributes(attributes);

        //  删除标记withId
        boolean isDelete = getBooleanFromColumn(columns, DELETED);
        builder.withDeleted(isDelete);
        builder.withOp(isDelete ? OperationType.DELETE.getValue() : OperationType.UPDATE.getValue());

        //  txId
        long txId = getLongFromColumn(columns, TX);
        builder.withTx(txId);

        //  如果是维护的commitId，需要设置devOps指标逻辑.
        if (DevOpsUtils.isMaintainRecord(commitId)) {
            parserContext.getCdcMetrics().getDevOpsMetrics()
                .computeIfAbsent(txId, f -> new DevOpsCdcMetrics()).incrementByStatus(true);
            //  运维时提交号为0
            builder.withCommitid(NO_TRANSACTION_COMMIT_ID);
            builder.withMaintainid(txId);
        } else {
            //  提交号
            builder.withCommitid(commitId);
        }

        //  other info
        builder.withVersion(getIntegerFromColumn(columns, VERSION))
            .withOqsMajor(getIntegerFromColumn(columns, OQSMAJOR))
            .withCreateTime(getLongFromColumn(columns, CREATETIME))
            .withUpdateTime(getLongFromColumn(columns, UPDATETIME));

        return builder.build();
    }

    /**
     * 转换attribute.
     *
     * @param id      主键id.
     * @param columns 原始数据集.
     * @return 对象键值对.
     */
    private Map<String, Object> attrCollection(long id, List<CanalEntry.Column> columns) throws SQLException {

        String attrStr = getStringFromColumn(columns, ATTRIBUTE);
        if (attrStr.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return attributesToMap(attrStr);
        } catch (Exception e) {
            String error = String
                .format("[dynamic-binlog-parser] id : %d, jsonToObject error, message : %s, attrStr %s.", id,
                    e.getMessage(), attrStr);
            logger.warn(error);
            throw new SQLException(error);
        }
    }

    /**
     * 获取当前的entityClass.
     *
     * @param id            主键id.
     * @param columns       原始数据集.
     * @param parserContext 上下文.
     */
    private IEntityClass getEntityClass(long id, List<CanalEntry.Column> columns, ParserContext parserContext)
        throws SQLException {
        long entityId = entityClassId(columns);
        if (entityId < 0) {
            throw new SQLException(
                String.format("[dynamic-binlog-parser] id : %d has no entityClass...", id));
        }
        String profile = getStringWithoutNullCheck(columns, PROFILE);

        return CommonUtils.getEntityClass(new EntityClassRef(entityId, "", profile), parserContext);
    }


    private long entityClassId(List<CanalEntry.Column> columns) {
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


    /**
     * 检查commitId是否ready，将数据加入到结果集中.
     *
     * @param commitId      commitId.
     * @param id            主键Id.
     * @param parserContext 上下文.
     */
    private void addToReadyChecks(long commitId, long id, ParserContext parserContext, ParseResult parseResult) {

        //  是否MAX_VALUE
        if (commitId != CommitHelper.getUncommitId()) {
            /*
             *  检查是否为跳过不处理的commitId满足commitId > skipCommitId || (commitId == 0 && skipCommitId != 0)
             * 否则跳过.
             */

            //  维护id不需要等待isReady 或者 没有transaction的commitId & 跳过的commitId
            if (DevOpsUtils.isMaintainRecord(commitId) || commitId == NO_TRANSACTION_COMMIT_ID) {
                return;
            }

            //  commitId需要check
            //  commitId > skipId,或者当skipId不启用(<= 0)
            if (parserContext.isCheckCommitReady()
                && (commitId > parserContext.getSkipCommitId() || parserContext.getSkipCommitId() <= NO_TRANSACTION_COMMIT_ID)) {
                parseResult.isReadyCommitIds().add(commitId);
            }

            //  加入未同步列表
            parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().add(commitId);
        }
    }
}
