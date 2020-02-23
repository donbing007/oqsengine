package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.sdk.service.ContextService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * TODO to use framework
 */
@Component
public class SDKContextService implements ContextService {

    final
    private ThreadlocalContextHolder threadlocalContextHolder;

    public SDKContextService(ThreadlocalContextHolder threadlocalContextHolder) {
        this.threadlocalContextHolder = threadlocalContextHolder;
    }

    @Override
    public <T> void set(ContextKey<T> key, T value) {
        threadlocalContextHolder.context().put(key.name(), value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(ContextKey<T> key) {
        try {
            return (T) threadlocalContextHolder.context().get(key.name());
        }catch(RuntimeException ex){
            //in normal way here is dead code
            throw new RuntimeException("Error to cast object to expected Class");
        }
    }
}