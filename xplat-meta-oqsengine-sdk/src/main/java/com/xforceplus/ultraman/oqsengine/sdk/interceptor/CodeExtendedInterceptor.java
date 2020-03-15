package com.xforceplus.ultraman.oqsengine.sdk.interceptor;

import com.xforceplus.ultraman.oqsengine.sdk.command.MetaDataLikeCmd;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.SimpleBoItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import com.xforceplus.xplat.galaxy.framework.context.ContextKeys;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.function.BiFunction;

public class CodeExtendedInterceptor<T,R> implements MessageDispatcherInterceptor<QueryMessage<T, R>> {

    private final MetadataRepository metadataRepository;

    private final ContextService contextService;

    public CodeExtendedInterceptor(MetadataRepository metadataRepository, ContextService contextService) {
        this.metadataRepository = metadataRepository;
        this.contextService = contextService;
    }

    @Override
    public BiFunction<Integer, QueryMessage<T, R>, QueryMessage<T, R>> handle(List<? extends QueryMessage<T, R>> list) {
        return (index, queryMessage) -> {
            if(MetaDataLikeCmd.class.isAssignableFrom(queryMessage.getPayloadType())){

                SimpleBoItem boItem = metadataRepository.findOneById(((MetaDataLikeCmd)queryMessage.getPayload()).getBoId());
                if(boItem != null) {
                    return (QueryMessage)queryMessage.withMetaData(queryMessage.getMetaData().and("code", boItem.getCode()));
                }else{
                    return (QueryMessage)queryMessage.withMetaData(queryMessage.getMetaData().and("code", ""));
                }
            }
            return queryMessage;
        };
    }

    @Override
    public int getOrder(){
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
