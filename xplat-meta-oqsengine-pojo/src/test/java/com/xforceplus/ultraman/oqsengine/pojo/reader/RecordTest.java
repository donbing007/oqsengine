package com.xforceplus.ultraman.oqsengine.pojo.reader;

import com.xforceplus.ultraman.oqsengine.pojo.reader.record.GeneralRecord;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import org.junit.Test;

public class RecordTest extends EntityHelperTest{

    @Test
    public void test(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        Record record = new GeneralRecord(reader.columns());

//        record.set();
    }
}
