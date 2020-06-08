package com.xforceplus.ultraman.oqsengine.sdk.interceptor;

import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.function.BiFunction;

/**
 * move context service value into QueryMessage metadata
 * @param <T>
 * @param <R>
 */
public class ContextInterceptor<T, R> implements MessageDispatcherInterceptor<QueryMessage<T, R>> {

    private ContextService contextService;

    public ContextInterceptor(ContextService contextService){
        this.contextService = contextService;
    }

    @Override
    public BiFunction<Integer, QueryMessage<T, R>, QueryMessage<T, R>> handle(List<? extends QueryMessage<T, R>> list) {
        return (index, queryMessage) -> {
            if (contextService != null) {
                return (QueryMessage<T, R>) queryMessage
                        .withMetaData(queryMessage.getMetaData()
                                .and(ContextKeys.LongKeys.TENANT_ID.name(), contextService.get(ContextKeys.LongKeys.TENANT_ID))
                                .and(ContextKeys.LongKeys.ACCOUNT_ID.name(), contextService.get(ContextKeys.LongKeys.ACCOUNT_ID))
                                .and(ContextKeys.LongKeys.ID.name(), contextService.get(ContextKeys.LongKeys.ID)));
            } else {
                return queryMessage;
            }
        };
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

