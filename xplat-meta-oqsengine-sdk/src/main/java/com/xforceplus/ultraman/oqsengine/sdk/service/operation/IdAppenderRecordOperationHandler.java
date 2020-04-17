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
        long id = entityUp.getObjId();
        if (id > 0) {
            record.set("id", id);
            record.setId(id);
        }
    }
}
