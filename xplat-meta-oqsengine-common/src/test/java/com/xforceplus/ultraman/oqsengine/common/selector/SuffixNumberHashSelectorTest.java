package com.xforceplus.ultraman.oqsengine.common.selector;

import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * NumberIndexTableNameHashSelector Tester.
 *
 * @author dongbin
 * @version 1.0 02/20/2020
 * @since <pre>Feb 20, 2020</pre>
 */
public class SuffixNumberHashSelectorTest {
    /**
     * Method: select(String key).
     */
    @Test
    public void testSelect() throws Exception {
        List<String> keys = buildKeys(10);

        String base = "test";
        int len = 100; // 分布区域
        SuffixNumberHashSelector selector = new SuffixNumberHashSelector(base, len);
        for (String key : keys) {
            int address = Math.abs(Time33Hash.build().hash(key) % len);
            Assertions.assertEquals(base + address, selector.select(key));
        }
    }

    private List<String> buildKeys(int size) {
        List<String> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keys.add(randomString(10));
        }
        return keys;
    }

    private String randomString(int len) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

} 
