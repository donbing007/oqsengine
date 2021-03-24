package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * UnsupportTokenizer Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/16/2021
 * @since <pre>Mar 16, 2021</pre>
 */
public class UnsupportTokenizerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test() throws Exception {
        UnsupportTokenizer unsupportTokenizer = (UnsupportTokenizer) UnsupportTokenizer.getInstance();

        Assert.assertEquals(FieldConfig.FuzzyType.UNKNOWN, unsupportTokenizer.support());

        unsupportTokenizer.tokenize("test");
    }

} 
