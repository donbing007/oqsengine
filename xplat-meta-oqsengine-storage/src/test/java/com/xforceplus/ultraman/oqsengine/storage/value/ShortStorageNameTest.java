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
        ShortStorageName shortStorageName = new ShortStorageName("P0", "123", "678", "L0");
        Assertions.assertEquals("P0", shortStorageName.getHead());
        Assertions.assertEquals("123", shortStorageName.getPrefix());
        Assertions.assertEquals("678", shortStorageName.getOriginSuffix());
        Assertions.assertEquals("678L0", shortStorageName.getSuffix());
        Assertions.assertEquals("L0", shortStorageName.getTail());
    }

} 
