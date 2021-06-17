package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.wildcard.WildcardTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
            new WildcardTokenizer(3),
            new WildcardTokenizer(4),
            new WildcardTokenizer(5),
            new WildcardTokenizer(6)
        ), FieldConfig.FuzzyType.WILDCARD);

        Assert.assertEquals(FieldConfig.FuzzyType.WILDCARD, tokenizer.support());


        Iterator<String> words = tokenizer.tokenize("scan1");
        List<String> expectedWords = Arrays.asList(
            "sca", "can", "an1",
            "scan", "can1",
            "scan1"
        );

        List<String> wildCardWords = new ArrayList<>();
        while(words.hasNext()) {
            wildCardWords.add(words.next());
        }

        Collections.sort(expectedWords);
        Collections.sort(wildCardWords);

        Assert.assertEquals(expectedWords.size(), wildCardWords.size());
        Assert.assertArrayEquals(expectedWords.toArray(new String[0]), wildCardWords.toArray(new String[0]));

        words = tokenizer.tokenize("scan");
        expectedWords = Arrays.asList(
            "sca", "can",
            "scan"
        );
        wildCardWords = new ArrayList<>();
        while(words.hasNext()) {
            wildCardWords.add(words.next());
        }
        Collections.sort(expectedWords);
        Collections.sort(wildCardWords);

        Assert.assertEquals(expectedWords.size(), wildCardWords.size());
        Assert.assertArrayEquals(expectedWords.toArray(new String[0]), wildCardWords.toArray(new String[0]));

    }


} 
