package com.xforceplus.ultraman.oqsengine.cdc.cdcerror.tools;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CdcErrorUtilsTest {

    @Test
    public void uniKeyGenerateTest() {
        String expectedUniKeyPrefix = "abc";
        int expectedPos = 12345;
        ErrorType expectedErrorType = ErrorType.DATA_FORMAT_ERROR;

        String res =
            CdcErrorUtils.uniKeyGenerate(expectedUniKeyPrefix, expectedPos, expectedErrorType);

        Assertions.assertEquals("abc-12345-" + expectedErrorType.getType(), res);
    }
}
