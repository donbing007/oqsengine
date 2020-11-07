package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CDCListener {

    @Autowired
    private StatusService statusService;

    @Async
    @EventListener
    public void invalidateCommitIds(CDCAckMetrics cdcAckMetrics){
        statusService.invalidateIds(cdcAckMetrics.getCommitList());
    }
}
