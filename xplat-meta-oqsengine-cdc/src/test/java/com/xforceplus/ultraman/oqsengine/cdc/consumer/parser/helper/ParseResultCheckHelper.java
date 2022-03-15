package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.utils.DevOpsUtils;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import org.junit.jupiter.api.Assertions;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ParseResultCheckHelper {

    public static void dynamicCheck(DynamicCanalEntryCase expected, OriginalEntity actual) throws JsonProcessingException {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.isDeleted(), actual.isDeleted());
        Assertions.assertEquals(expected.isDeleted() ? OperationType.DELETE.getValue() : OperationType.UPDATE.getValue(), actual.getOp());
        Assertions.assertEquals(expected.getVersion(), actual.getVersion());
        Assertions.assertEquals(expected.getOqsmajor(), actual.getOqsMajor());
        Assertions.assertEquals(expected.getCreate(), actual.getCreateTime());
        Assertions.assertEquals(expected.getUpdate(), actual.getUpdateTime());
        Assertions.assertEquals(expected.getTx(), actual.getTx());
        Assertions.assertEquals(expected.getCommitId(), actual.getCommitid());
        Assertions.assertEquals(expected.getEntityId(), actual.getEntityClass().id());
        Assertions.assertEquals(expected.getAttr(), JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(actual.getAttributes()));

        if (DevOpsUtils.isMaintainRecord(expected.getCommitId())) {
            Assertions.assertEquals(expected.getTx(), actual.getMaintainid());
        }
    }
}
