package com.xforceplus.ultraman.oqsengine.common.selector;

import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TakeTurnsSelector Tester.
 *
 * @author dongbin
 * @version 1.0 11/02/2020
 * @since <pre>Nov 2, 2020</pre>
 */
public class TakeTurnsSelectorTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: select(String key).
     */
    @Test
    public void testSelect() throws Exception {
        TakeTurnsSelector selector = new TakeTurnsSelector(Arrays.asList(1, 2, 3, 4, 5));
        Assert.assertEquals(1, selector.select(null));
        Assert.assertEquals(2, selector.select(null));
        Assert.assertEquals(3, selector.select(null));
        Assert.assertEquals(4, selector.select(null));
        Assert.assertEquals(5, selector.select(null));
    }


} 
