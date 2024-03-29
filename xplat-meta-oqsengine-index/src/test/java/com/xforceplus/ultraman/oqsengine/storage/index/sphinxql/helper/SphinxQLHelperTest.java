package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
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
        Assertions.assertEquals("1y2p0itestj32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, FieldType.STRING, false)._1);
        Assertions.assertEquals("1y2p0itestj32e8e7S", SphinxQLHelper.buildPreciseQuery(storageValue, FieldType.STRING, true)._1);

        storageValue = new LongStorageValue("9223372036854775807", 100, true);
        Assertions.assertEquals("1y2p0i100j32e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, FieldType.STRING, false)._1);
        Assertions.assertEquals("1y2p0i100j32e8e7L", SphinxQLHelper.buildPreciseQuery(storageValue, FieldType.STRING, true)._1);
    }

    @Test
    public void testBuildFuzzyQuery() throws Exception {
        Tokenizer tokenizer = new JcsegTokenizer();
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assertions.assertEquals("(1y2p0itestwj32e8e7S)",
            SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));

        storageValue = new StringStorageValue("9223372036854775807", "测试test", true);
        Assertions.assertEquals("(1y2p0i测试wj32e8e7S << 1y2p0itestwj32e8e7S)",
            SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));

        storageValue = new StringStorageValue("9223372036854775807", "上海", true);
        Assertions.assertEquals("(1y2p0i上海wj32e8e7S)", SphinxQLHelper.buildSegmentationQuery(storageValue, tokenizer));
    }

    @Test
    public void testBuildWirdcardQuery() throws Exception {
        StorageValue storageValue = new StringStorageValue("9223372036854775807", "test", true);
        Assertions.assertEquals("1y2p0itestwj32e8e7S", SphinxQLHelper.buildWirdcardQuery(storageValue));
    }

    /**
     * 测试转换一个超长的String condition.
     */
    @Test
    public void testStringConditionFormat() {
        String longOverString = "aaaabbbbccccddddeeeeffffggggEAAAABBBBCCCCDDDDEEEEFFFFGGGGECDMA";
        ShortStorageName shortStorageName = new ShortStorageName("", "123p", "s456", "S");

        //  测试ConditionOperator.EQUALS
        Tuple2<String, Boolean> format =
            SphinxQLHelper.stringConditionFormat(longOverString, shortStorageName, true, false);

        Assertions.assertEquals(
            "(P0123paaaabbbbccccddddeeeeffffgs456S P1123pgggEAAAABBBBCCCCDDDDEEEEFs456S P2123pFFFGGGGECDMAs456S)",
            format._1);

        String needFilterString = "CERTIFICATE_INVOICE_PIECES";
        format = SphinxQLHelper.stringConditionFormat(needFilterString, shortStorageName, false, false);
        Assertions.assertFalse(format._2());
        Assertions.assertEquals("123pCERTIFICATEINVOICEPIECESs456S", format._1());

        format = SphinxQLHelper.stringConditionFormat(needFilterString, shortStorageName, true, false);
        Assertions.assertTrue(format._2());
        Assertions.assertEquals("(P0123pCERTIFICATEINVOICEPIECEs456S P1123pSs456S)", format._1());
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
        Assertions.assertEquals("[aaaabbbbccccddddeeeeffffggggEAAAABBBBCCCCDDDDEEEEFFFFGGGGECDMA]", format);

        StorageValue storageValue =
            SphinxQLHelper.stringsStorageConvert("123ps456S", format, false, false);

        StorageValue one = storageValue;
        StorageValue two = one.next();
        StorageValue three = two.next();

        Assertions.assertEquals("aaaabbbbccccddddeeeeffffg", one.value());
        Assertions.assertEquals("gggEAAAABBBBCCCCDDDDEEEEF", two.value());
        Assertions.assertEquals("FFFGGGGECDMA", three.value());

        Assertions.assertEquals(0, one.location());
        Assertions.assertEquals(1, two.location());
        Assertions.assertEquals(2, three.location());
    }


}
