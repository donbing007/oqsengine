package com.xforceplus.ultraman.oqsengine.sdk.dispatcher;

import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.GeneralResponse;
import com.xforceplus.ultraman.oqsengine.sdk.dispatcher.messaging.GenericQueryMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    public <R> R querySync(Object command, Class<R> responseType){

        List<QueryHandlerAdapter> adapters = retrieveAdapter(ResolvableType.forClass(command.getClass())
                        , ResolvableType.forClass(responseType));
        GeneralResponse gr = null;
        for(QueryHandlerAdapter adapter : adapters){
            try{
                gr = adapter.processMsg(new GenericQueryMessage(command, ResolvableType.forClass(command.getClass())));
                if(gr == null){
                    continue;
                }else{
                    break;
                }
            }catch(Exception ex){
                continue;
            }
        }

        if(gr != null){
            return (R)gr.getT();
        }

        return null;
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
