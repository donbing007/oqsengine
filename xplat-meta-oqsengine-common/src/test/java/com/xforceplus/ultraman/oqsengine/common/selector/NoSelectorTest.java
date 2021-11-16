package com.xforceplus.ultraman.oqsengine.common.selector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * NoSelector Tester.
 *
 * @author dongbin
 * @version 1.0 11/02/2020
 * @since <pre>Nov 2, 2020</pre>
 */
public class NoSelectorTest {
    /**
     * Method: select(String key)
     */
    @Test
    public void testSelect() throws Exception {
        NoSelector<String> noSelector = new NoSelector<>("fix value");
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals("fix value", noSelector.select(Long.toString(i)));
        }
    }


} 
