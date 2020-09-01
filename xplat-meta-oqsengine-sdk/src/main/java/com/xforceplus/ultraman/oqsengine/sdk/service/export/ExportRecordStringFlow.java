package com.xforceplus.ultraman.oqsengine.sdk.service.export;

import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.NameMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Record to String Flow
 */
public interface ExportRecordStringFlow {

    /**
     *
     * @param entityClass
     * @param isFirstLine isfirstline
     * @param nameMapping
     * @param columns
     * @param filterColumns
     * @param transformer real transformer
     * @param context    context
     * @return
     */
    Flow<Record, String, NotUsed> getFlow(IEntityClass entityClass, AtomicBoolean isFirstLine
            , List<NameMapping> nameMapping
            , List<String> columns
            , Set<String> filterColumns
            , ExportStringTransformer transformer
            , Map<String, Object> context);
}
