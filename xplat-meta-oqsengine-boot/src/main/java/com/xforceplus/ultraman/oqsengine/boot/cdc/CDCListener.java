package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * CDC listener
 */
@Component
public class CDCListener {

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @EventListener
    public void invalidateCommitIds(CDCAckMetrics cdcAckMetrics) {
        List<Long> idList = cdcAckMetrics.getCommitList();
        long[] ids = idList.stream().mapToLong(id -> id).toArray();
        commitIdStatusService.obsolete(ids);
    }

    @EventListener
    public void saveCDCMetrics(CDCMetrics cdcMetrics) {
        cdcStatusService.save(cdcMetrics);
    }

    @EventListener
    public void heartBeat(long heartBeat) {
        cdcStatusService.heartBeat(heartBeat);
    }
}
