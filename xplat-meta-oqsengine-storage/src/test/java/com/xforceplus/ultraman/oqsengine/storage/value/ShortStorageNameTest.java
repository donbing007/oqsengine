package com.xforceplus.ultraman.oqsengine.storage.value;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ShortStorageName Tester.
 *
 * @author dongbin
 * @version 1.0 03/16/2021
 * @since <pre>Mar 16, 2021</pre>
 */
public class ShortStorageNameTest {

    @Test
    public void testNoLocationSuffix() {
        ShortStorageName shortStorageName = new ShortStorageName("123", "678L0");
        Assertions.assertEquals("678L", shortStorageName.getNoLocationSuffix());
    }

} 
