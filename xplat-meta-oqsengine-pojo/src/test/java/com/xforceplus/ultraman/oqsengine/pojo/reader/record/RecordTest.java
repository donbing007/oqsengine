package com.xforceplus.ultraman.oqsengine.pojo.reader.record;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.pojo.reader.EntityHelperTest;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RecordTest extends EntityHelperTest {

    @Test
    public void test(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        Record record = new GeneralRecord(reader.columns());

        IEntityField column = reader.column("rel1.id").get();

        record.set(column, "12345");
        assertEquals(record.get(column).get(), "12345");

        assertEquals(record.get("rel1.id").get(), "12345");

        /**
         * add typed
         */
        record.set("rel1.id", 12345L);

        assertEquals(record.get("rel1.id", Long.TYPE).get().intValue(), 12345);

        assertEquals(record.get(column, Long.TYPE).get().intValue(), 12345L);


        record.setTypedValue(new LongValue(column, 23456L));

        assertEquals(record.getTypedValue("rel1.id").get(), new LongValue(column, 23456L));

        assertEquals(record.getTypedValue(column).get(), new LongValue(column, 23456L));

    }

    @Test
    public void toNestedTest(){
        IEntityClassReader reader = new IEntityClassReader(entityClass);
        Record record = new GeneralRecord(reader.columns());

        IEntityField column = reader.column("rel1.id").get();

        record.set(column, "12345");
        assertEquals(record.get(column).get(), "12345");

        assertEquals(record.get("rel1.id").get(), "12345");

        /**
         * add typed
         */
        record.set("rel1.id", 12345L);

        assertEquals(record.get("rel1.id", Long.TYPE).get().intValue(), 12345);

        assertEquals(record.get(column, Long.TYPE).get().intValue(), 12345L);


        record.setTypedValue(new LongValue(column, 23456L));

        assertEquals(record.getTypedValue("rel1.id").get(), new LongValue(column, 23456L));

        assertEquals(record.getTypedValue(column).get(), new LongValue(column, 23456L));


        System.out.println(record.toNestedMap(Collections.emptySet()));

    }
}
