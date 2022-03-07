package com.xforceplus.ultraman.oqsengine.cdc.consumer.error;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import java.util.Map;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public interface ErrorRecorder {
    /**
     * 错误记录器.
     *
     * @param batchId 批次ID.
     * @param errors 错误集合.
     */
    void record(long batchId, Map<String, ParseResult.Error> errors);
}
