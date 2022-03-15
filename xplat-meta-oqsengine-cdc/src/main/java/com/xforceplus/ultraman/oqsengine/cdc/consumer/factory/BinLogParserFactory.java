package com.xforceplus.ultraman.oqsengine.cdc.consumer.factory;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.BinLogParser;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.DynamicBinLogParser;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class BinLogParserFactory {
    private Map<EntityClassType, BinLogParser> binLogParser;

    private static final class BinLogParserFactoryHolder {
        static final BinLogParserFactory FACTORY = new BinLogParserFactory();
    }

    public static BinLogParserFactory getInstance() {
        return BinLogParserFactoryHolder.FACTORY;
    }

    public BinLogParserFactory() {
        binLogParser = new HashMap<>();
        binLogParser.put(EntityClassType.DYNAMIC, new DynamicBinLogParser());
    }

    public BinLogParser getParser(EntityClassType entityClassType) {
        return binLogParser.get(entityClassType);
    }


    public BinLogParser dynamicParser() {
        return  binLogParser.get(EntityClassType.DYNAMIC);
    }

}
