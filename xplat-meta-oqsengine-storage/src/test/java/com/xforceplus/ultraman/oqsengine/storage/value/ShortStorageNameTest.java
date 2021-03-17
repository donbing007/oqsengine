package com.xforceplus.ultraman.oqsengine.storage.value;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ShortStorageName Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/16/2021
 * @since <pre>Mar 16, 2021</pre>
 */
public class ShortStorageNameTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testNoLocationSuffix() {
        ShortStorageName shortStorageName = new ShortStorageName("123", "678L0");
        Assert.assertEquals("678L", shortStorageName.getNoLocationSuffix());
    }

} 
