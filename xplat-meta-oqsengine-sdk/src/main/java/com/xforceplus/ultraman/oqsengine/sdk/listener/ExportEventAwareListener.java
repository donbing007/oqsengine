package com.xforceplus.ultraman.oqsengine.sdk.listener;

import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * listener
 */
public interface ExportEventAwareListener {

    @Async
    @EventListener(EntityErrorExported.class)
    void errorListener(EntityErrorExported entityExported);


    @Async
    @EventListener(EntityExported.class)
    void messageListener(EntityExported entityExported);
}
