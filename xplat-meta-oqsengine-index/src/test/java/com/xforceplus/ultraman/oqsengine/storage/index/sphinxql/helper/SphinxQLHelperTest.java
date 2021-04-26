package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.segmentation.JcsegTokenizer;
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

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testFilterSymbols() throws Exception {
        StringBuilder buff = new StringBuilder();
        Arrays.stream(SphinxQLHelper.IGNORE_SYMBOLS).forEach(c -> {
            buff.append((char) c);
        });
        buff.insert(0, "before");
        buff.append("after");

        Assert.assertEquals("beforeafter", SphinxQLHelper.filterSymbols(buff.toString()));
    }

    @Test
    public void testEncodeJsonCharset() throws Exception {
        StringBuilder buff = new StringBuilder();
        Arrays.stream(SphinxQLHelper.IGNORE_SYMBOLS).forEach(c -> {
            buff.append((char) c);
        });
        String value = buff.toString();

        buff.delete(0, buff.length());
        Arrays.stream(SphinxQLHelper.IGNORE_SYMBOLS).forEach(c -> {
            if ('\'' == c) {
                buff.append('`');
            } else if ('"' == c) {
                buff.append("``");
            } else {
                buff.append((char) c);
            }
        });
        String expected = buff.toString();

        Assert.assertEquals(expected, SphinxQLHelper.encodeJsonCharset(value));
    }

    @Test
    public void testBuildPreciseQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assert.assertEquals("1y2p0ijtest32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, false));
        Assert.assertEquals("1y2p0ijtest32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, true));

        storageValue = new LongStorageValue("9223372036854775807", 100, true);
        Assert.assertEquals("1y2p0ij10032e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, false));
        Assert.assertEquals("1y2p0ij10032e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, true));
    }

    @Test
    public void testBuildFuzzyQuery() throws Exception {
        Tokenizer tokenizer = new JcsegTokenizer();
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assert.assertEquals("(1y2p0ijtest32e8e7S)", SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));

        storageValue = new StringStorageValue("9223372036854775807", "测试test", true);
        Assert.assertEquals("(1y2p0ij测试32e8e7S << 1y2p0ijtest32e8e7S)",
            SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));
    }

    @Test
    public void testBuildWirdcardQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assert.assertEquals("1y2p0ijtest32e8e7S", SphinxQLHelper.buildWirdcardQuery(storageValue));
    }
} 
