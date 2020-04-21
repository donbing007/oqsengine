package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;

import java.util.function.BiConsumer;

/**
 *
 * @author admin
 */
public interface RecordOperationHandler extends Comparable<FieldOperationHandler>
        , BiConsumer<Record, EntityUp> {

    @Override
    default int compareTo(FieldOperationHandler fieldOperationHandler) {
        return 0;
    }
}
