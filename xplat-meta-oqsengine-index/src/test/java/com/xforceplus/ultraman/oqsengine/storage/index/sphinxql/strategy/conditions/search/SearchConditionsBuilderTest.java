package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.search;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.tokenizer.DefaultTokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

/**
 * 搜索条件构造器测试.
 *
 * @author dongbin
 * @version 0.1 2021/05/18 14:13
 * @since 1.8
 */
public class SearchConditionsBuilderTest {

    @Test
    public void testBuild() throws Exception {
        SearchConditionsBuilder builder = new SearchConditionsBuilder();
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        builder.setTokenizerFacotry(tokenizerFactory);

        buildCase().stream().forEach(c -> {
            String where = builder.build(c.config, c.entityClasses).toString();
            Assert.assertEquals(c.expected, where);
        });
    }

    private Collection<Case> buildCase() {
        return Arrays.asList(
            new Case(
                SearchConfig.Builder.anSearchConfig()
                    .withCode("name")
                    .withFuzzyType(FieldConfig.FuzzyType.NOT)
                    .withPage(Page.newSinglePage(10))
                    .withValue("test").build(),
                "MATCH('(@searchattrf nametestname)')"
            ),
            new Case(
                SearchConfig.Builder.anSearchConfig()
                    .withCode("name")
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withPage(Page.newSinglePage(10))
                    .withValue("test").build(),
                "MATCH('(@searchattrf nametestname)')"
            ),
            new Case(
                SearchConfig.Builder.anSearchConfig()
                    .withCode("name")
                    .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                    .withPage(Page.newSinglePage(10))
                    .withValue("有限公").build(),
                "MATCH('(@searchattrf name有限name << name公name)')"
            )
        );
    }

    static class Case {
        private SearchConfig config;
        private IEntityClass[] entityClasses;
        private String expected;

        public Case(SearchConfig config, IEntityClass[] entityClasses, String expected) {
            this.config = config;
            this.entityClasses = entityClasses;
            this.expected = expected;
        }

        public Case(SearchConfig config, String expected) {
            this(config, new IEntityClass[0], expected);
        }
    }
}