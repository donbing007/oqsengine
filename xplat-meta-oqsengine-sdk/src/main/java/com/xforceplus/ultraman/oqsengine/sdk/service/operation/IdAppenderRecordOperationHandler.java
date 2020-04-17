package com.xforceplus.ultraman.oqsengine.sdk.service.operation;

import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;

/**
 * id appender
 *
 * @author admin
 */
public class IdAppenderRecordOperationHandler implements RecordOperationHandler {

    @Override
    public void accept(Record record, EntityUp entityUp) {
        if (entityUp.getId() > 0) {
            record.set("id", entityUp.getId());
            record.setId(entityUp.getObjId());
        }
    }
}
