package com.xforceplus.ultraman.oqsengine.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试保留字检测.
 */
public class ReservedFieldNameWordTest {

    @Test
    public void testDetect() throws Exception {
        Assertions.assertTrue(ReservedFieldNameWord.isReservedWorkd("id"));
        Assertions.assertTrue(ReservedFieldNameWord.isReservedWorkd("iD"));
        Assertions.assertTrue(ReservedFieldNameWord.isReservedWorkd("Id"));
        Assertions.assertTrue(ReservedFieldNameWord.isReservedWorkd("ID"));

        Assertions.assertFalse(ReservedFieldNameWord.isReservedWorkd("ida"));
    }
}