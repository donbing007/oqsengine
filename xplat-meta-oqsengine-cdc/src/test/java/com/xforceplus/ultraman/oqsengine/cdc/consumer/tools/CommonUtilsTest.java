package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;


import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
class CommonUtilsTest {

    @Test
    void isMaintainRecord() {
        long expectedCommitId = CDCConstant.MAINTAIN_COMMIT_ID;

        Assertions.assertTrue(CommonUtils.isMaintainRecord(expectedCommitId));
        Assertions.assertFalse(CommonUtils.isMaintainRecord(Long.MAX_VALUE - 1));
    }
}