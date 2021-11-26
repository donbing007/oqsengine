package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试Strings的转换.
 *
 * @author dongbin
 * @version 0.1 2021/05/25 14:53
 * @since 1.8
 */
public class SphinxQLStringsStorageStrategyTest {

    /**
     * 测试转换来自主库的数据.
     */
    @Test
    public void testConvert() throws Exception {
        SphinxQLStringsStorageStrategy storageStrategy = new SphinxQLStringsStorageStrategy();
        StorageValue storageValue = storageStrategy.convertIndexStorageValue(
            "123S", "[teal][fuchsia][cyan]", false);
        StorageValue firstStorageValue = storageValue;
        StorageValue secondStorageValue = storageValue.next();
        StorageValue thirdStorageValue = secondStorageValue.next();

        Assertions.assertEquals("teal", firstStorageValue.value());
        Assertions.assertEquals("fuchsia", secondStorageValue.value());
        Assertions.assertEquals("cyan", thirdStorageValue.value());

        Assertions.assertEquals(0, firstStorageValue.location());
        Assertions.assertEquals(1, secondStorageValue.location());
        Assertions.assertEquals(2, thirdStorageValue.location());
    }
}