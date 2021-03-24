package com.xforceplus.ultraman.oqsengine.tokenizer.segmentation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * JcsegTokenizer Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/15/2021
 * @since <pre>Mar 15, 2021</pre>
 */
public class JcsegTokenizerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testTokeniz() throws Exception {
        JcsegTokenizer tokenizer = new JcsegTokenizer();
        buildCases().stream().forEach(c -> {
            Iterator<String> words = tokenizer.tokenize(c.value);

            List<String> wordList = new ArrayList<>();
            while (words.hasNext()) {
                wordList.add(words.next());
            }

            Assert.assertEquals(
                String.format("expected:%s, actual:%s", c.expectedWords, wordList), wordList.size(), c.expectedWords.size());

            for (String word : wordList) {
                Assert.assertTrue(String.format("expected:%s, actual:%s", c.expectedWords, wordList), c.expectedWords.contains(word));
            }
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                "abc",
                Arrays.asList()
            )
            ,
            new Case(
                "abcd",
                Arrays.asList()
            )
            ,
            new Case(
                "中英文hello world混合",
                Arrays.asList(
                    "中",
                    "英文",
                    "hello",
                    "world",
                    "混合"
                )
            )
            ,
            new Case(
                "UUID-123,这个的上海云砺有限公司",
                Arrays.asList(
                    "uuid-123",
                    "这个",
                    "上海",
                    "云",
                    "砺",
                    "有限公司"
                )
            )
            ,
            new Case(
                "我的名称叫董斌",
                Arrays.asList(
                    "我的",
                    "名称",
                    "叫",
                    "董斌"
                )
            )
            ,
            new Case(
                "Apple pay",
                Arrays.asList(
                    "apple",
                    "pay"
                )
            )
            ,
            new Case(
                "测试test",
                Arrays.asList(
                    "测试",
                    "test"
                )
            )
            ,
            new Case(
                "我test",
                Arrays.asList("test")
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
