package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ID;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.ColumnsUtils;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.CommonUtils;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class StaticBinLogParser implements BinLogParser {

    @Override
    public void parse(List<CanalEntry.Column> columns, ParserContext parserContext, ParseResult parseResult) {
        long id = UN_KNOW_ID;
        long commitId = UN_KNOW_ID;
        try {
            //  获取ID
            id = getLongFromColumn(columns, ID);

            OqsEngineEntity oqsEngineEntity = findOriginalEntity(id, parserContext, parseResult);
            if (null == oqsEngineEntity) {
                //  未获取originalEntity,判定为非法操作.
                return;
            }
            commitId = oqsEngineEntity.getCommitid();

            IEntityClass entityClass =
                CommonUtils.getEntityClass(id, oqsEngineEntity.getEntityClassRef(), parserContext);

            oqsEngineEntity.setEntityClass(entityClass);
            //  构造attributes
            businessParse(columns, oqsEngineEntity);

            parseResult.getFinishEntries().put(id, oqsEngineEntity);
        } catch (Exception e) {
            if (commitId != CommitHelper.getUncommitId()) {
                //  加入错误列表.
                parseResult.addError(id, commitId,
                    String.format("batch : %d, pos : %d, static-parse columns failed, message : %s",
                        parserContext.getCdcMetrics().getBatchId(), parseResult.getPos(), e.getMessage()));
            }
        }

        //  自增游标
        parseResult.finishOne(id);
    }

    private OqsEngineEntity findOriginalEntity(long id, ParserContext parserContext, ParseResult parseResult)
        throws SQLException {
        OqsEngineEntity oqsEngineEntity = parseResult.getOperationEntries().remove(id);
        if (null == oqsEngineEntity) {
            Optional<OqsEngineEntity> optionalOriginalEntity =
                parserContext.getMasterStorage().selectOrigin(id, true);

            if (optionalOriginalEntity.isPresent() &&
                null != optionalOriginalEntity.get().getEntityClassRef()) {
                oqsEngineEntity = optionalOriginalEntity.get();
            }
        }

        return oqsEngineEntity;
    }

    private void businessParse(List<CanalEntry.Column> columns, OqsEngineEntity oqsEngineEntity) {
        Map<String, Object> attrs = new HashMap<>();
        oqsEngineEntity.getEntityClass().fields().forEach(
            f -> {
                Object value = ColumnsUtils.execute(columns, f.name(), f.type());
                if (null != value) {
                    attrs.put(toStorageKey(f), value);
                }
            }
        );

        oqsEngineEntity.setAttributes(attrs);
    }

    private static String toStorageKey(IEntityField field) {
        return "F" + field.id() + toStorageSuffix(field.type());
    }

    private static String toStorageSuffix(FieldType fieldType) {
        switch (fieldType) {
            case BOOLEAN:
            case DATETIME:
            case LONG:
                return "L";
            default:
                return "S";
        }
    }
}
