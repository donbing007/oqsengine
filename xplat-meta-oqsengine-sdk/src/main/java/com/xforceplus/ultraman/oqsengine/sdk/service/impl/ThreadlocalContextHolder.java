package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
public class ThreadlocalContextHolder {

    private ThreadLocal<Map<String, Object>> contextThreadLocal = new ThreadLocal<>();

    public Map<String, Object> context(){
        Map<String, Object> context = contextThreadLocal.get();
        if(context == null){
            context = new TreeMap<>();
            contextThreadLocal.set(context);
        }

        return context;
    }

    public void putContext(Map<String, Object> context){
        contextThreadLocal.set(context);
    }

    public void clearContext(){
        contextThreadLocal.remove();
    }

}
