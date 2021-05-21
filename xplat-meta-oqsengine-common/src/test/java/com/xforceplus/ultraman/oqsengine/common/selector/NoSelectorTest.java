package com.xforceplus.ultraman.oqsengine.common.selector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NoSelector Tester.
 *
 * @author dongbin
 * @version 1.0 11/02/2020
 * @since <pre>Nov 2, 2020</pre>
 */
public class NoSelectorTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: select(String key)
     */
    @Test
    public void testSelect() throws Exception {
        NoSelector<String> noSelector = new NoSelector<>("fix value");
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("fix value", noSelector.select(Long.toString(i)));
        }
    }


} 
