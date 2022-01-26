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
        //  check一个普通字段
        check("123S", "[teal][fuchsia][cyan]", new String[]{"teal", "fuchsia", "cyan"});

        //  check一个包含超长字段的
        check("123S",
            "[aaaabbbbccccddddeeeeffffgggg!AAAABBBBCCCCDDDDEEEEFFFFGGGG!CDMA][fuchsia][cyan]",
            new String[]{"aaaabbbbccccddddeeeeffffg", "ggg!AAAABBBBCCCCDDDDEEEEF", "FFFGGGG!CDMA", "fuchsia", "cyan"});
    }

    private void check(String storageName, String value, String[] expectedValue) {
        SphinxQLStringsStorageStrategy storageStrategy = new SphinxQLStringsStorageStrategy();
        StorageValue storageValue = storageStrategy.convertIndexStorageValue(
            storageName, value, false, false);

        int i = 0;
        while (true) {
            Assertions.assertEquals(expectedValue[i], storageValue.value());
            Assertions.assertEquals(i++, storageValue.location());

            if (storageValue.haveNext()) {
                storageValue = storageValue.next();
            } else {
                break;
            }
        }

        Assertions.assertEquals(i, expectedValue.length);

    }
}