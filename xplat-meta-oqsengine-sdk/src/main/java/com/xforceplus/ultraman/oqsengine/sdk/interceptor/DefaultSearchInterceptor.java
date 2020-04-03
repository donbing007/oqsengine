package com.xforceplus.ultraman.oqsengine.sdk.interceptor;

import com.xforceplus.ultraman.oqsengine.sdk.command.ConditionSearchCmd;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;
import com.xforceplus.xplat.galaxy.framework.dispatcher.interceptor.MessageDispatcherInterceptor;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.GenericQueryMessage;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.function.BiFunction;

/**
 * change search condition interceptor
 * @param <R>
 */
public class DefaultSearchInterceptor<R> implements MessageDispatcherInterceptor<QueryMessage<ConditionSearchCmd, R>> {

    private MatchRouter<String, ConditionQueryRequest> searchCondition;

    public DefaultSearchInterceptor(MatchRouter<String, ConditionQueryRequest> searchCondition) {
        this.searchCondition = searchCondition;
    }

    private QueryMessage<com.xforceplus.ultraman.oqsengine.sdk.command.ConditionSearchCmd, R> withNewPayload(QueryMessage oldMsg
            , ConditionSearchCmd oldCmd, ConditionQueryRequest request) {
        return new GenericQueryMessage<>(new ConditionSearchCmd(oldCmd.getBoId(), request), oldMsg.getQueryName(), oldMsg.getResponseType(), oldMsg.getMetaData());
    }

    private boolean isEmptyCondition(ConditionQueryRequest request) {
        Conditions conditions =  request.getConditions();
        return conditions == null ||
                ((conditions.getFields() == null) || (conditions.getFields().isEmpty()) &&
                        (conditions.getEntities() == null || conditions.getEntities().isEmpty())
                );
    }

    @Override
    public BiFunction<Integer, QueryMessage<ConditionSearchCmd, R>, QueryMessage<ConditionSearchCmd, R>> handle(List<? extends QueryMessage<ConditionSearchCmd, R>> list) {
        return (index, message) -> {
            ConditionSearchCmd cmd = message.getPayload();
            ConditionQueryRequest request = cmd.getConditionQueryRequest();
            if (isEmptyCondition(request)) {
                Object code = message.getMetaData().get("code");
                if (code != null) {
                    if (searchCondition.route(code.toString()).isPresent()) {
                        return withNewPayload(message, cmd, searchCondition.route(code.toString()).get());
                    }
                }
            }
            return message;
        };
    }

    @Override
    public boolean isSupport(ResolvableType inputType) {
        return ResolvableType.forClass(ConditionSearchCmd.class).isAssignableFrom(inputType);
    }
}
