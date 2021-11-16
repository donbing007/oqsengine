package com.xforceplus.ultraman.oqsengine.changelog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;
import org.junit.jupiter.api.Test;

public class JsonTest {

    @Test
    public void testDomain(){
        ObjectMapper mapper = new ObjectMapper();
        TransactionalChangelogEvent te = new TransactionalChangelogEvent();
        te.setCommitId(1L);

        try {
            String s = mapper.writeValueAsString(te);
            String value =  "{\"commitId\":10002,\"changedEventList\":[{\"entityClassId\":1,\"id\":1000001,\"valueMap\":{\"1001\":{\"value\":\"abc\",\"type\":\"STRING\",\"fieldId\":1001,\"ownerEntityId\":null}},\"commitId\":10002,\"comment\":\"Test Outer\",\"timestamp\":1616062656897,\"operationType\":\"UPDATE\",\"username\":\"luye\"}]}";
            TransactionalChangelogEvent transactionalChangelogEvent = mapper.readValue(value, new TypeReference<TransactionalChangelogEvent>() {
            });

            String value2 = "{\"entityClassId\":1,\"id\":1000001,\"valueMap\":{\"1001\":{\"value\":\"abc\",\"type\":\"STRING\",\"fieldId\":1001,\"ownerEntityId\":null,\"ivalue\":{\"field\":null,\"value\":\"abc\"}}},\"commitId\":10002,\"comment\":\"Test Outer\",\"timestamp\":1616059175889,\"operationType\":\"UPDATE\",\"username\":\"luye\"}";

            ChangedEvent changedEvent = mapper.readValue(value2, new TypeReference<ChangedEvent>(){});
            System.out.println("evt:" + changedEvent);

            System.out.println(transactionalChangelogEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
