package com.xforceplus.ultraman.oqsengine.boot.cdc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.status.CDCStatusService;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * cdc metrics callback
 */
public class DefaultCDCMetricsCallback implements CDCMetricsCallback {

    final Logger logger = LoggerFactory.getLogger(DefaultCDCMetricsCallback.class);

    @Resource
    private CDCStatusService cdcStatusService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Override
    public void cdcAck(CDCAckMetrics ackMetrics) {
        try {
            List<Long> idList = ackMetrics.getCommitList();
            long[] ids = idList.stream().mapToLong(id -> id).toArray();
            commitIdStatusService.obsolete(ids);

            cdcStatusService.saveAck(ackMetrics);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void heartBeat() {
        cdcStatusService.heartBeat();

        if (logger.isDebugEnabled()) {
            logger.debug("CDC heartBeat.");
        }
    }

    @Override
    public void cdcSaveLastUnCommit(CDCMetrics cdcMetrics) {
        cdcStatusService.saveUnCommit(cdcMetrics);

        if (logger.isDebugEnabled()) {
            String json = null;
            try {
                json = objectMapper.writeValueAsString(cdcMetrics);
            } catch (JsonProcessingException e) {
                json = "{}";
            }
            logger.debug("Save cdc status {}", json);
        }
    }

    @Override
    public CDCMetrics queryLastUnCommit() {
        Optional<CDCMetrics> cdcMetricsOp = cdcStatusService.getUnCommit();
        if (cdcMetricsOp.isPresent()) {
            return cdcMetricsOp.get();
        } else {
            return null;
        }
    }
}
