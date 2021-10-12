package com.xforceplus.ultraman.oqsengine.tokenizer.segmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JcsegTokenizer Tester.
 *
 * @author dongbin
 * @version 1.0 03/15/2021
 * @since <pre>Mar 15, 2021</pre>
 */
public class JcsegTokenizerTest {

    @Test
    public void testTokeniz() throws Exception {
        JcsegTokenizer tokenizer = new JcsegTokenizer();
        buildCases().stream().forEach(c -> {
            Iterator<String> words = tokenizer.tokenize(c.value);

            List<String> wordList = new ArrayList<>();
            while (words.hasNext()) {
                wordList.add(words.next());
            }

            Assertions.assertEquals(wordList.size(),
                c.expectedWords.size(),
                String.format("expected:%s, actual:%s", c.expectedWords, wordList));

            for (String word : wordList) {
                Assertions.assertTrue(
                    c.expectedWords.contains(word), String.format("expected:%s, actual:%s", c.expectedWords, wordList));
            }
        });
    }

    private Collection<Case> buildCases() {
        return Arrays.asList(
            new Case(
                "abc",
                Arrays.asList("abc")
            ),
            new Case(
                "abcd",
                Arrays.asList("abcd")
            ),
            new Case(
                "中英文hello world混合",
                Arrays.asList(
                    "中",
                    "英文",
                    "hello",
                    "world",
                    "混合"
                )
            ),
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
            ),
            new Case(
                "我的名称叫董斌",
                Arrays.asList(
                    "我的",
                    "名称",
                    "叫",
                    "董斌"
                )
            ),
            new Case(
                "Apple pay",
                Arrays.asList(
                    "apple",
                    "pay"
                )
            ),
            new Case(
                "测试test",
                Arrays.asList(
                    "测试",
                    "test"
                )
            ),
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
