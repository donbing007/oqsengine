package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.dto.CDCMetrics;
import org.junit.Test;

import static org.springframework.test.util.AssertionErrors.assertTrue;

public class CDCCallBackTest {

    @Test
    public void testCDCMetricsToJson() throws JsonProcessingException {

        CDCMetrics cdcMetrics = new CDCMetrics();

        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(cdcMetrics);
        System.out.println(s);

        CDCMetrics cdcMetrics1 = mapper.readValue(s, CDCMetrics.class);
        System.out.println(cdcMetrics1);
    }


    @Test
    public void testEnum() throws JsonProcessingException {

        CDCStatus cdcConsumerStatus = CDCStatus.CONNECTED;

        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(cdcConsumerStatus);

        CDCStatus cdcStatus = mapper.readValue(s, CDCStatus.class);

        assertTrue("OK", cdcStatus.equals(cdcConsumerStatus));
    }
}
