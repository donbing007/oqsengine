package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper;

import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Method: encodeSpecialCharset(String value)
     */
    @Test
    public void testEncodeSpecialCharset() throws Exception {
        String data = "!\"$'()-/<@\\^|~";
        Assert.assertEquals("！＂＄＇（）－／＜＠＼＾｜～", SphinxQLHelper.encodeSpecialCharset(data));

        data = "'@带有符号的中文@'";
        Assert.assertEquals("＇＠带有符号的中文＠＇", SphinxQLHelper.encodeSpecialCharset(data));
    }

    /**
     * Method: serializableJson(Map<String, Object> data)
     */
    @Test
    public void testSerializableJson() throws Exception {
        Map<String, Object> data = new HashMap();
        data.put("c1", "string value");
        data.put("c2", 100);

        Assert.assertEquals("{\"c1\":\"string value\",\"c2\":100}", SphinxQLHelper.serializableJson(data));
    }

    /**
     * Method: deserializeJson(String json)
     */
    @Test
    public void testDeserializeJson() throws Exception {
        String json = "{\"c1\":\"string value\",\"c2\":200}";
        Map<String, Object> data = SphinxQLHelper.deserializeJson(json);
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("string value", data.get("c1"));
        Assert.assertEquals(200L, data.get("c2"));
    }

    /**
     * Method: buildFullPreciseQuery(StorageValue value, boolean useGroupName)
     */
    @Test
    public void testBuildFullPreciseQuery() throws Exception {
        LongStorageValue longStorageValue = new LongStorageValue("1L", 100, false);
        Assert.assertEquals("\"100F1L\"",
            SphinxQLHelper.buildFullPreciseQuery(longStorageValue, false));
        Assert.assertEquals("\"100F1L*\"",
            SphinxQLHelper.buildFullPreciseQuery(longStorageValue, true));

        StringStorageValue stringStorageValue = new StringStorageValue("1L", "string value", false);
        Assert.assertEquals("\"string　valueF1S\"",
            SphinxQLHelper.buildFullPreciseQuery(stringStorageValue, false));
        Assert.assertEquals("\"string　valueF1S*\"",
            SphinxQLHelper.buildFullPreciseQuery(stringStorageValue, true));
    }

    /**
     * Method: buildFullFuzzyQuery(StorageValue value, boolean useGroupName)
     */
    @Test
    public void testBuildFullFuzzyQuery() throws Exception {
        LongStorageValue longStorageValue = new LongStorageValue("1L", 100, false);
        Assert.assertEquals("(ZONESPAN:F1L \"*100*\")",
            SphinxQLHelper.buildFullFuzzyQuery(longStorageValue, false));
        Assert.assertEquals("(ZONESPAN:F1L \"*100*\")",
            SphinxQLHelper.buildFullFuzzyQuery(longStorageValue, true));

        StringStorageValue stringStorageValue = new StringStorageValue("1L", "string value", false);
        Assert.assertEquals("(ZONESPAN:F1S \"*string　value*\")",
            SphinxQLHelper.buildFullFuzzyQuery(stringStorageValue, false));
        Assert.assertEquals("(ZONESPAN:F1S \"*string　value*\")",
            SphinxQLHelper.buildFullFuzzyQuery(stringStorageValue, true));
    }


} 
