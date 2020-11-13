package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CDC listener
 */
@Component
public class CDCListener {

    @Autowired
    private StatusService statusService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("redis.cdc.key:cdcmetrics")
    private String key;

    @EventListener
    public void invalidateCommitIds(CDCAckMetrics cdcAckMetrics) {
        statusService.invalidateIds(cdcAckMetrics.getCommitList());
    }

    @EventListener
    public void saveCDCMetrics(CDCMetrics cdcMetrics) throws JsonProcessingException {
        String s = objectMapper.writeValueAsString(cdcMetrics);
        statusService.saveCDCMetrics(key, s);
    }
}
