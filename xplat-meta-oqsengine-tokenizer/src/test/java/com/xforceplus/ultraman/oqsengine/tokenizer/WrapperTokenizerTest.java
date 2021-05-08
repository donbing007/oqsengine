package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.wildcard.WildcardTokenizer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * WrapperTokenizer Tester.
 *
 * @author dongbin
 * @version 1.0 03/17/2021
 * @since <pre>Mar 17, 2021</pre>
 */
public class WrapperTokenizerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testIterator() throws Exception {
        WrapperTokenizer tokenizer = new WrapperTokenizer(Arrays.asList(
            new WildcardTokenizer(3), new WildcardTokenizer(4)
        ), FieldConfig.FuzzyType.WILDCARD);

        Assert.assertEquals(FieldConfig.FuzzyType.WILDCARD, tokenizer.support());

        List<String> expectedWords = Arrays.asList(
            "155", "558", "585", "856", "565", "657", "572", "728", "282",
            "1558", "5585", "5856", "8565", "5657", "6572", "5728", "7282"
        );
        Iterator<String> words = tokenizer.tokenize("15585657282");

    }


} 
