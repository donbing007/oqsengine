package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.segmentation.JcsegTokenizer;
import io.vavr.Tuple2;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * SphinxQLHelper Tester.
 *
 * @author dongbin
 * @version 1.0 04/10/2020
 * @since <pre>Apr 10, 2020</pre>
 */
public class SphinxQLHelperTest {


    @Test
    public void testFilterSymbols() throws Exception {
        StringBuilder buff = new StringBuilder();
        Arrays.stream(SphinxQLHelper.IGNORE_SYMBOLS).forEach(c -> {
            buff.append((char) c);
        });
        SphinxQLHelper.REPLACE_SYMBOLS.keySet().forEach(c -> {
            buff.append((char) c);
        });
        buff.insert(0, "before");
        buff.append("after");

        Assertions.assertEquals("beforeMDafter", SphinxQLHelper.filterSymbols(buff.toString()));
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

        Assertions.assertEquals(expected, SphinxQLHelper.encodeJsonCharset(value));
    }

    @Test
    public void testBuildPreciseQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assertions.assertEquals("1y2p0ijtest32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, false)._1);
        Assertions.assertEquals("1y2p0ijtest32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, true)._1);

        storageValue = new LongStorageValue("9223372036854775807", 100, true);
        Assertions.assertEquals("1y2p0ij10032e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, false)._1);
        Assertions.assertEquals("1y2p0ij10032e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, true)._1);
    }

    @Test
    public void testBuildFuzzyQuery() throws Exception {
        Tokenizer tokenizer = new JcsegTokenizer();
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assertions.assertEquals("(1y2p0ijtestw32e8e7S)", SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));

        storageValue = new StringStorageValue("9223372036854775807", "测试test", true);
        Assertions.assertEquals("(1y2p0ij测试w32e8e7S << 1y2p0ijtestw32e8e7S)",
            SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));

        storageValue = new StringStorageValue("9223372036854775807", "上海", true);
        Assertions.assertEquals("(1y2p0ij上海w32e8e7S)", SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));
    }

    @Test
    public void testBuildWirdcardQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assertions.assertEquals("1y2p0ijtestw32e8e7S", SphinxQLHelper.buildWirdcardQuery(storageValue));
    }

    /**
     * 测试转换一个超长的String condition.
     */
    @Test
    public void testStringConditionFormat() {
        String longOverString = "aaaabbbbccccddddeeeeffffggggEAAAABBBBCCCCDDDDEEEEFFFFGGGGECDMA";
        ShortStorageName shortStorageName = new ShortStorageName("123p", "s456S");

        //  测试ConditionOperator.EQUALS
        Tuple2<String, Boolean> format =
            SphinxQLHelper.stringConditionFormat(longOverString, shortStorageName, false);

        Assertions.assertEquals("(123paaaabbbbccccddddeeeeffffggggEs456S << 123pAAAABBBBCCCCDDDDEEEEFFFFGGGGEs456S << 123pCDMAs456S)", format._1);
    }

    /**
     * 测试转换一个超长的String value.
     * 将按照strings的逻辑进行转换.
     */
    @Test
    public void testStringValueFormat() {
        String longOverString = "aaaabbbbccccddddeeeeffffggggEAAAABBBBCCCCDDDDEEEEFFFFGGGGECDMA";
        String format =
            SphinxQLHelper.stringValueFormat(longOverString);
        Assertions.assertEquals("[aaaabbbbccccddddeeeeffffggggE][AAAABBBBCCCCDDDDEEEEFFFFGGGGE][CDMA]", format);

        StorageValue storageValue =
            SphinxQLHelper.stringsStorageConvert("123ps456S", format, false);

        StorageValue one = storageValue;
        StorageValue two = one.next();
        StorageValue three = two.next();

        Assertions.assertEquals("aaaabbbbccccddddeeeeffffggggE", one.value());
        Assertions.assertEquals("AAAABBBBCCCCDDDDEEEEFFFFGGGGE", two.value());
        Assertions.assertEquals("CDMA", three.value());

        Assertions.assertEquals(0, one.location());
        Assertions.assertEquals(1, two.location());
        Assertions.assertEquals(2, three.location());
    }


} 
