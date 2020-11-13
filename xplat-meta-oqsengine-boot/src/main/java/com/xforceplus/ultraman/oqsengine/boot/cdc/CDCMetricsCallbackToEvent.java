package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.StatusService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

/**
 * cdc metrics callback
 */
public class CDCMetricsCallbackToEvent implements CDCMetricsCallback {

    private ApplicationEventPublisher publisher;

    private StatusService statusService;

    private String key;

    private ObjectMapper mapper;

    public CDCMetricsCallbackToEvent(ApplicationEventPublisher publisher, StatusService statusService, String key, ObjectMapper mapper) {
        this.publisher = publisher;
        this.statusService = statusService;
        this.key = key;
        this.mapper = mapper;
    }

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        publisher.publishEvent(ackMetrics);
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        publisher.publishEvent(cdcMetrics);
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        String rawStr = statusService.getCDCMetrics(key);
        if(!StringUtils.isEmpty(rawStr)) {
            try {

                return mapper.readValue(rawStr, CDCMetrics.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
