package com.xforceplus.ultraman.oqsengine.meta.common.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class RequestStatusTest {

    @Test
    public void getInstanceTest() {
        check(RequestStatus.HEARTBEAT);
        check(RequestStatus.REGISTER);
        check(RequestStatus.REGISTER_OK);
        check(RequestStatus.SYNC);
        check(RequestStatus.SYNC_OK);
        check(RequestStatus.SYNC_FAIL);
        check(RequestStatus.DATA_ERROR);
        check(RequestStatus.RESET);
    }

    private void check(RequestStatus requestStatus) {
        RequestStatus r =
            RequestStatus.getInstance(requestStatus.ordinal());

        Assertions.assertEquals(requestStatus, r);
    }
}
