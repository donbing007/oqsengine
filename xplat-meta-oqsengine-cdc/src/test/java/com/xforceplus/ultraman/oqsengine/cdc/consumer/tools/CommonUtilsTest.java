package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;


import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
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
        long expectedCommitId = CommitHelper.getMaintainCommitId();

        Assertions.assertTrue(CommonUtils.isMaintainRecord(expectedCommitId));
        Assertions.assertFalse(CommonUtils.isMaintainRecord(Long.MAX_VALUE - 1));
    }
}