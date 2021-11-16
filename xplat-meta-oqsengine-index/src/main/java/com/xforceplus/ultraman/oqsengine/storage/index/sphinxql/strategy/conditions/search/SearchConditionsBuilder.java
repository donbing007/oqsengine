package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.search;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.tokenizer.NothingTokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;

/**
 * 处理搜索.
 * 可以跨元信息进行字段查询.
 * 支持模糊搜索,需要在搜索时选择模糊搜索的类型.
 *
 * @author dongbin
 * @version 0.1 2021/05/17 17:54
 * @since 1.8
 */
public class SearchConditionsBuilder implements ConditionsBuilder<SearchConfig, SphinxQLWhere>, TokenizerFactoryAble {

    private TokenizerFactory tokenizerFacotry;

    @Override
    public SphinxQLWhere build(SearchConfig config, IEntityClass... entityClasses) {
        SphinxQLWhere where = new SphinxQLWhere();

        switch (config.getFuzzyType()) {
            case NOT:
            case WILDCARD: {
                doBuildEq(config, where);
                break;
            }
            case SEGMENTATION: {
                doBuildSegmentation(config, where);
                break;
            }
            default: {
                throw new IllegalArgumentException("Incorrect search ambiguity type.");
            }
        }

        return where;
    }

    private void doBuildEq(SearchConfig config, SphinxQLWhere where) {
        where.addMatch(SphinxQLHelper.buildSearch(config.getValue(), config.getCode(), NothingTokenizer.getInstance()));
    }

    private void doBuildSegmentation(SearchConfig config, SphinxQLWhere where) {
        Tokenizer tokenizer = this.tokenizerFacotry.getSegmentationTokenizer();
        where.addMatch(SphinxQLHelper.buildSearch(config.getValue(), config.getCode(), tokenizer));
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFacotry = tokenizerFacotry;
    }
}
