package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
public class SphinxQLStringStorageStrategyTest {
    /**
     * 测试超长字段的转换.
     */
    @Test
    public void testConvert() {
        SphinxQLStringStorageStrategy storageStrategy = new SphinxQLStringStorageStrategy();
        StorageValue storageValue = storageStrategy.convertIndexStorageValue(
            String.valueOf(Long.MAX_VALUE - 1), "aaaabbbbccccddddeeeeffffgggg!AAAABBBBCCCCDDDDEEEEFFFFGGGG!CDMA", false, true);
        StorageValue firstStorageValue = storageValue;
        StorageValue secondStorageValue = storageValue.next();
        StorageValue thirdStorageValue = secondStorageValue.next();

        Assertions.assertEquals("aaaabbbbccccddddeeeeffffg", firstStorageValue.value());
        Assertions.assertEquals("ggg!AAAABBBBCCCCDDDDEEEEF", secondStorageValue.value());
        Assertions.assertEquals("FFFGGGG!CDMA", thirdStorageValue.value());

        Assertions.assertEquals(0, firstStorageValue.location());
        Assertions.assertEquals(1, secondStorageValue.location());
        Assertions.assertEquals(2, thirdStorageValue.location());
    }

    /**
     * 测试不进行转换.
     */
    @Test
    public void testNoConvert() {
        SphinxQLStringStorageStrategy storageStrategy = new SphinxQLStringStorageStrategy();
        StorageValue storageValue = storageStrategy.convertIndexStorageValue(
            "123S", "aaaabbbbccccddddeeeeffffgggg!AAAABBBBCCCCDDDDEEEEFFFFGGGG!CDMA", false, false);

        Assertions.assertEquals("aaaabbbbccccddddeeeeffffgggg!AAAABBBBCCCCDDDDEEEEFFFFGGGG!CDMA", storageValue.value());

        Assertions.assertEquals(-1, storageValue.location());

        Assertions.assertFalse(storageValue.haveNext());
    }
}
