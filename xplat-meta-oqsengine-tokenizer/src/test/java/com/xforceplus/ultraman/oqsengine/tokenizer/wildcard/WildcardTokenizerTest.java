package com.xforceplus.ultraman.oqsengine.tokenizer.wildcard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * WildcardTokenizer Tester.
 *
 * @author dongbin
 * @version 1.0 03/15/2021
 * @since <pre>Mar 15, 2021</pre>
 */
public class WildcardTokenizerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testTokeniz() throws Exception {
        WildcardTokenizer tokenizer = new WildcardTokenizer(3);
        buildCases().stream().forEach(c -> {
            Iterator<String> words = tokenizer.tokenize(c.value);

            List<String> wordList = new ArrayList<>();
            while (words.hasNext()) {
                wordList.add(words.next());
            }

            Assert.assertEquals(
                String.format("expected:%s, actual:%s", c.expectedWords, wordList), wordList.size(),
                c.expectedWords.size());

            for (String word : wordList) {
                Assert.assertTrue("", c.expectedWords.contains(word));
            }
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                "ab",
                Arrays.asList()
            ),
            new Case(
                "abc",
                Arrays.asList("abc")
            ),
            new Case(
                "abcd",
                Arrays.asList("abc", "bcd")
            ),
            new Case(
                "中英文hello world混合",
                Arrays.asList(
                    "中英文",
                    "英文h",
                    "文he",
                    "hel",
                    "ell",
                    "llo",
                    "lo ",
                    "o w",
                    " wo",
                    "wor",
                    "orl",
                    "rld",
                    "ld混",
                    "d混合")
            )
        );
    }

    private static class Case {
        private String value;
        private List<String> expectedWords;

        public Case(String value, List<String> expectedWords) {
            this.value = value;
            this.expectedWords = expectedWords;
        }
    }

} 
