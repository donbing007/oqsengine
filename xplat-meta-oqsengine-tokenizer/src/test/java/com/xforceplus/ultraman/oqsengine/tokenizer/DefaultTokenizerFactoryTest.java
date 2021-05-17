package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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
 * DefaultTokenizerFactory Tester.
 *
 * @author dongbin
 * @version 1.0 03/16/2021
 * @since <pre>Mar 16, 2021</pre>
 */
public class DefaultTokenizerFactoryTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testWildcardTokenizer() throws Exception {
        DefaultTokenizerFactory factory = new DefaultTokenizerFactory();

        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                    FieldConfig.Builder.aFieldConfig()
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3)
                    .withWildcardMaxWidth(7).build()).build();

        Tokenizer tokenizer = factory.getTokenizer(field);
        Iterator<String> words = tokenizer.tokenize("abc");
        Assert.assertFalse(words.hasNext());

        Assert.assertEquals(field.config().getWildcardMaxWidth() - field.config().getWildcardMinWidth() + 1,
            factory.getWildcardTokenizerCache().size());

        field = EntityField.Builder.anEntityField()
            .withConfig(
                    FieldConfig.Builder.aFieldConfig()
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3)
                    .withWildcardMaxWidth(9).build()).build();
        tokenizer = factory.getTokenizer(field);
        words = tokenizer.tokenize("abcd");
        List<String> wordList = new ArrayList<>();
        while (words.hasNext()) {
            wordList.add(words.next());
        }
        Collections.sort(wordList);
        List<String> expectedList = Arrays.asList("abc", "bcd");
        Collections.sort(expectedList);
        Assert.assertEquals(expectedList, wordList);

        Assert.assertEquals(field.config().getWildcardMaxWidth() - field.config().getWildcardMinWidth() + 1,
            factory.getWildcardTokenizerCache().size());

        field = EntityField.Builder.anEntityField()
            .withConfig(
                    FieldConfig.Builder.aFieldConfig()
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3)
                    .withWildcardMaxWidth(9).build()).build();
        tokenizer = factory.getTokenizer(field);
        words = tokenizer.tokenize("15585657282");
        wordList = new ArrayList<>();
        while (words.hasNext()) {
            wordList.add(words.next());
        }
        Collections.sort(wordList);
        expectedList = Arrays.asList(
            "155", "558", "585", "856", "565", "657", "572", "728", "282",
            "1558", "5585", "5856", "8565", "5657", "6572", "5728", "7282",
            "15585", "55856", "58565", "85657", "56572", "65728", "57282",
            "155856", "558565", "585657", "856572", "565728", "657282",
            "1558565", "5585657", "5856572", "8565728", "5657282", "15585657",
            "55856572", "58565728", "85657282",
            "155856572", "558565728", "585657282"
        );
        Collections.sort(expectedList);
        Assert.assertEquals(expectedList, wordList);

        Assert.assertEquals(field.config().getWildcardMaxWidth() - field.config().getWildcardMinWidth() + 1,
            factory.getWildcardTokenizerCache().size());
    }


} 
