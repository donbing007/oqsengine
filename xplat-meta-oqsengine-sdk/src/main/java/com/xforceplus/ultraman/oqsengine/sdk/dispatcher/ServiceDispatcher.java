package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;

import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.GeneralResponse;
import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.GenericQueryMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.control.Either.left;

/**
 * service dispatcher
 */
@Component
public class ServiceDispatcher implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private List<QueryHandlerAdapter> querys = new ArrayList<>();


    public void addQueryAdapter(QueryHandlerAdapter queryHandlerAdapter){
        querys.add(queryHandlerAdapter);
    }

    private List<QueryHandlerAdapter> retrieveAdapter(ResolvableType input, ResolvableType output){
        return querys.stream()
                .filter(x -> x.supportsQueryType(input, output))
                .collect(Collectors.toList());
    }

    private Optional<GeneralResponse> invokeInner(List<QueryHandlerAdapter> queryHandlerAdapters, Object command){
        return queryHandlerAdapters.stream()
                .sorted(Comparator.comparingInt(QueryHandlerAdapter::getOrder))
                .map(x ->
                {
                    try {
                        return x.processMsg(new GenericQueryMessage(command, ResolvableType.forClass(command.getClass())));
                    } catch (Exception ex){
                        return null;
                    }
                }).filter(Objects::nonNull)
                .findFirst();
    }

    public <R> R querySync(Object command, Class cls, String queryName){
         Optional<ResolvableType> typeOp = Stream.of(cls.getMethods())
                .filter(method ->  isMatch(method, queryName, command.getClass()))
                .findFirst()
                .map(ResolvableType::forMethodReturnType);
        return typeOp.<R>map(resolvableType -> querySync(command, resolvableType))
                .orElse(null);
    }


    private boolean isMatch(Method method, String queryName, Class cmdCls ){
        return queryName.equalsIgnoreCase(method.getName())
                && method.getParameterCount() == 1
                && ResolvableType.forMethodParameter(method, 0).isAssignableFrom(ResolvableType.forClass(cmdCls));
    }


    public <R> R querySync(Object command, ResolvableType responseType){

        List<QueryHandlerAdapter> adapters = retrieveAdapter(ResolvableType.forClass(command.getClass())
                , responseType);

        Map<Boolean, List<QueryHandlerAdapter>> listMap =
                adapters.stream().collect(Collectors.groupingBy(QueryHandlerAdapter::isDefault));

        /**
         * invoke
         */
        Optional<GeneralResponse> grOp = invokeInner(Optional.ofNullable(listMap.get(false))
                .orElseGet(Collections::emptyList), command);

        if(!grOp.isPresent()){
            grOp = invokeInner(Optional.ofNullable(listMap.get(true))
                    .orElseGet(Collections::emptyList), command);
        }


        return grOp.map(x -> (R)x.getT()).orElse(null);
    }

    public <R> R querySync(Object command, Class<R> responseType){
        return querySync(command, ResolvableType.forClass(responseType));
    }

    //fire
    public <R> CompletableFuture<R> send(Object command){
        return null;
    }

    //req-request query side
    public <R, Q> CompletableFuture<R> query(Q request, Class<R> responseType){
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
