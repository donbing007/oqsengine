package com.xforceplus.ultraman.oqsengine.meta.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ClientIdUtilsTest {

    @Test
    public void clientIdUtilsTest() {
        String clientId = ClientIdUtils.generate();
        Assertions.assertTrue(clientId.contains("/"));

        Assertions.assertTrue(clientId.length() > 1);
    }
}
