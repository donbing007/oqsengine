package com.xforceplus.ultraman.oqsengine.lock.utils;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 状态Key测试.
 */
public class StateKeysTest {

    @Test
    public void testGetCompleteKeys() throws Exception {
        String[] keys = new String[1000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        StateKeys stateKeys = new StateKeys(keys);
        String[] expectedCompleteKeys = new String[0];
        String[] completeKeys = stateKeys.getCompleteKeys();
        Assertions.assertArrayEquals(expectedCompleteKeys, completeKeys);

        stateKeys.move();
        stateKeys.move();

        expectedCompleteKeys = Arrays.stream(keys).limit(2).toArray(String[]::new);
        completeKeys = stateKeys.getCompleteKeys();
        Assertions.assertArrayEquals(expectedCompleteKeys, completeKeys);
    }

    @Test
    public void testGetNotCompleteKeys() throws Exception {
        String[] keys = new String[1000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "test.new.key." + (Long.MAX_VALUE - i);
        }

        StateKeys stateKeys = new StateKeys(keys);
        String[] expectedNotCompleteKeys = keys;
        String[] notCompleteKeys = stateKeys.getNoCompleteKeys();
        Assertions.assertArrayEquals(expectedNotCompleteKeys, notCompleteKeys);

        stateKeys.move();
        stateKeys.move();

        expectedNotCompleteKeys = Arrays.stream(keys).skip(2).toArray(String[]::new);
        notCompleteKeys = stateKeys.getNoCompleteKeys();
        Assertions.assertArrayEquals(expectedNotCompleteKeys, notCompleteKeys);
    }

}