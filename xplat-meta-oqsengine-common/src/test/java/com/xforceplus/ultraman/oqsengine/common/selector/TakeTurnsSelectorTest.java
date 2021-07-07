package com.xforceplus.ultraman.oqsengine.common.selector;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TakeTurnsSelector Tester.
 *
 * @author dongbin
 * @version 1.0 11/02/2020
 * @since <pre>Nov 2, 2020</pre>
 */
public class TakeTurnsSelectorTest {
    /**
     * Method: select(String key).
     */
    @Test
    public void testSelect() throws Exception {
        TakeTurnsSelector selector = new TakeTurnsSelector(Arrays.asList(1, 2, 3, 4, 5));
        Assertions.assertEquals(1, selector.select(null));
        Assertions.assertEquals(2, selector.select(null));
        Assertions.assertEquals(3, selector.select(null));
        Assertions.assertEquals(4, selector.select(null));
        Assertions.assertEquals(5, selector.select(null));
    }
}
