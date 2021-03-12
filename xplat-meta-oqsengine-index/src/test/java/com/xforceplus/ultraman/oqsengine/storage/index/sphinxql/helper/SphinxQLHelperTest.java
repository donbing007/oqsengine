package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * SphinxQLHelper Tester.
 *
 * @author <Authors name>
 * @version 1.0 04/10/2020
 * @since <pre>Apr 10, 2020</pre>
 */
public class SphinxQLHelperTest {

    private static final int[] IGNORE_SYMBOLS = {
        '\'', '\"', '\n', '\r', '\0', '\\', '+', '-', '#', '%', '.', '~', '_', '±', '×', '÷', '=', '≠', '≡', '≌', '≈',
        '<', '>', '≮', '≯', '≤', '≥', '‰', '∞', '∝', '√', '∵', '∴', '∷', '∠', '⌒', '⊙', '○', 'π', '△', '⊥', '∪', '∩',
        '∫', '∑', '°', '′', '″', '℃', '{', '}', '(', ')', '[', ']', '|', '‖', '*', '/', ':', ';', '?', '!', '&', '～',
        '§', '→', '^', '$', '@', '`', '❤', '❥', '︼', '﹄', '﹂', 'ˉ', '︾', '︺', '﹀', '︸', '︶', '︻', '﹃', '﹁',
    };

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testEncodeFullSearchCharset() throws Exception {
        StringBuilder buff = new StringBuilder();
        Arrays.stream(IGNORE_SYMBOLS).forEach(c -> {
            buff.append((char) c);
        });
        buff.insert(0, "before");
        buff.append("after");

        Assert.assertEquals("beforeafter", SphinxQLHelper.encodeFullSearchCharset(buff.toString()));
    }

    @Test
    public void testEncodeJsonCharset() throws Exception {
        StringBuilder buff = new StringBuilder();
        Arrays.stream(IGNORE_SYMBOLS).forEach(c -> {
            buff.append((char) c);
        });
        String value = buff.toString();

        buff.delete(0, buff.length());
        Arrays.stream(IGNORE_SYMBOLS).forEach(c -> {
            if ('\'' == c) {
                buff.append('\\');
            }
            buff.append((char) c);
        });
        String expected = buff.toString();

        Assert.assertEquals(expected, SphinxQLHelper.encodeJsonCharset(value));
    }

    @Test
    public void testBuildFullPreciseQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assert.assertEquals("aZl8N0testy58M7S", SphinxQLHelper.buildFullPreciseQuery(storageValue, false));
        Assert.assertEquals("aZl8N0testy58M7S*", SphinxQLHelper.buildFullPreciseQuery(storageValue, true));

        storageValue = new LongStorageValue("9223372036854775807", 100, true);
        Assert.assertEquals("aZl8N0100y58M7S", SphinxQLHelper.buildFullPreciseQuery(storageValue, false));
        Assert.assertEquals("aZl8N0100y58M7S*", SphinxQLHelper.buildFullPreciseQuery(storageValue, true));
    }

    @Test
    public void testBuildFullFuzzyQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assert.assertEquals("(aZl8N0 << *test* << y58M7S)", SphinxQLHelper.buildFullFuzzyQuery(storageValue, false));
    }
} 
