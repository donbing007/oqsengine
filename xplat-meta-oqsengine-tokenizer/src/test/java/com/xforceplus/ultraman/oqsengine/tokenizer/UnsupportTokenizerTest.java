package com.xforceplus.ultraman.oqsengine.tokenizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * UnsupportTokenizer Tester.
 *
 * @author dongbin
 * @version 1.0 03/16/2021
 * @since <pre>Mar 16, 2021</pre>
 */
public class UnsupportTokenizerTest {

    @Test
    public void test() throws Exception {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            UnsupportTokenizer unsupportTokenizer = (UnsupportTokenizer) UnsupportTokenizer.getInstance();

            Assertions.assertEquals(FieldConfig.FuzzyType.UNKNOWN, unsupportTokenizer.support());

            unsupportTokenizer.tokenize("test");
        });
    }

} 
