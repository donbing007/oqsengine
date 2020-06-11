package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforceplus.ultraman.oqsengine.sdk.event.config.ConfigChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConfigListener {

    @Async
//    @EventListener(condition = "#event.type.equals('PAGE')")
    @EventListener
    public void pageChangeListener(ConfigChangeEvent event){
        System.out.println(event);
    }
}
