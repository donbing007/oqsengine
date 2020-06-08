package com.xforceplus.ultraman.oqsengine.sdk.service;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.command.ConditionExportCmd;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;

import java.util.Map;

/**
 * service to export
 */
public interface ExportSource {

    /**
     * a export Source
     * @return
     */
    Source<Record, NotUsed> source(IEntityClass entityclass, ConditionQueryRequest request);

}
