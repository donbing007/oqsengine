package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import org.junit.Assert;
import org.junit.Test;

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
        StorageValue storageValue = storageStrategy.convertIndexStorageValue("123S", "[teal][fuchsia][cyan]");
        StorageValue firstStorageValue = storageValue;
        StorageValue secondStorageValue = storageValue.next();
        StorageValue thirdStorageValue = secondStorageValue.next();

        Assert.assertEquals("teal", firstStorageValue.value());
        Assert.assertEquals("fuchsia", secondStorageValue.value());
        Assert.assertEquals("cyan", thirdStorageValue.value());

        Assert.assertEquals(0, firstStorageValue.location());
        Assert.assertEquals(1, secondStorageValue.location());
        Assert.assertEquals(2, thirdStorageValue.location());
    }
}