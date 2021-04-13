package com.xforceplus.ultraman.oqsengine.storage.transaction.cache;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.Event;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.payload.CachePayload;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * desc :
 * name : CacheEventHelper
 *
 * @author : xujia
 * date : 2021/4/13
 * @since : 1.8
 */
public class CacheEventHelper {

    public static final long EXPIRE_BUFFER_SECONDS = 3 * 60;
    public static final long CLOSE_WAIT_MAX_LOOP = 60;
    public static final long WAIT_DURATION = 1000;
    public static String CUD_PAYLOAD_KEY_PREFIX = "com.xforceplus.ultraman.oqsengine.event.payload";

    public static Event<CachePayload> generate(EventType eventType, long txId, long number, IEntity entity) {
        return new ActualEvent<>(eventType,
                toCachePayload(txId, number, entity, null),
                System.currentTimeMillis()
        );
    }

    public static Event<CachePayload> generate(EventType eventType, long txId, long number, IEntity entity, IEntity old) {
        return new ActualEvent<>(eventType,
                toCachePayload(txId, number, entity, old),
                System.currentTimeMillis()
        );
    }

    public static String eventFieldGenerate(long id, long version, int eventType) {
        return  String.format("%d.%d.%d", id, version, eventType);
    }

    public static String eventKeyGenerate(long txId) {
        return eventKeyGenerate(Long.toString(txId));
    }

    public static String eventKeyGenerate(String txIdString) {
        return String.format("%s.%s", CUD_PAYLOAD_KEY_PREFIX, txIdString);
    }


    private static CachePayload toCachePayload(long txId, long number, IEntity entity, IEntity old) {
        CachePayload.Builder builder = CachePayload.Builder.anCacheValue()
                .withTxId(txId)
                .withId(entity.id())
                .withVersion(entity.version())
                .withNumber(number)
                .withFieldValueMapping(toFieldValueMapping(entity));
        if (null != old) {
            builder.withOldFieldValueMapping(toFieldValueMapping(old));
        }

        return builder.build();
    }

    private static Map<Long, String> toFieldValueMapping(IEntity entity) {

        if (null != entity.entityValue()) {
            Collection<IValue> values = entity.entityValue().values();
            if (null != values) {
                return values.stream().collect(Collectors.toMap(f1 -> f1.getField().id(), IValueUtils::serialize, (f1, f2) -> f1));
            }
        }
        return new HashMap<>();
    }
}
