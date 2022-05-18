package com.xforceplus.ultraman.oqsengine.cdc.consumer.factory;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.BinLogParser;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.DynamicBinLogParser;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class BinLogParserFactoryTest {

    @Test
    public void getParserByTableTest() {
        BinLogParser parser = BinLogParserFactory.getInstance().dynamicParser();
        Assertions.assertEquals(DynamicBinLogParser.class, parser.getClass());
    }

    @Test
    public void getParserByEntityClassType() {
        BinLogParser parser = BinLogParserFactory.getInstance().getParser(EntityClassType.DYNAMIC);
        Assertions.assertEquals(DynamicBinLogParser.class, parser.getClass());
    }
}
