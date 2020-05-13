package com.xforceplus.ultraman.oqsengine.sdk.interceptor;

import com.xforceplus.ultraman.oqsengine.sdk.command.MetaDataLikeCmd;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;

import java.util.List;
import java.util.function.BiFunction;

/**
 * disable version
 * @param <R>
 * @param <T>
 */
public class VersionInterceptor<T, R> implements MessageDispatcherInterceptor<QueryMessage<T, R>> {

    @Override
    public BiFunction<Integer, QueryMessage<T, R>, QueryMessage<T, R>> handle(List<? extends QueryMessage<T, R>> list) {
        return (index, queryMessage) -> {
            if (MetaDataLikeCmd.class.isAssignableFrom(queryMessage.getPayloadType())) {
                ((MetaDataLikeCmd) queryMessage.getPayload()).clearVersion();
            }
            return queryMessage;
        };
    }
}