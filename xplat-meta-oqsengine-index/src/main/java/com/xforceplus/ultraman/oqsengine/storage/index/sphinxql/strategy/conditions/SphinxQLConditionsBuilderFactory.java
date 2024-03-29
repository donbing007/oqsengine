package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLWhere;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.search.SearchConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.HaveOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.HaveOrNoRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.NoOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.select.NoOrNoRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 根据条件中是否需要 OR 查询,各属性是否需要范围查询来决定生成器.
 * 最优的情况是,没有范围查询全部等值查询.
 * 最坏的情况是,有范围查询,同时有多条件使用 OR 连接.
 *
 * @author dongbin
 * @version 0.1 2020/4/21 11:04
 * @since 1.8
 */
public class SphinxQLConditionsBuilderFactory implements StorageStrategyFactoryAble, TokenizerFactoryAble {

    private Map<Integer, ConditionsBuilder> builderMap;

    private ConditionsBuilder<Conditions, SphinxQLWhere> emptyConditionsBuilder;

    private ConditionsBuilder<SearchConfig, SphinxQLWhere> searchConditionsBuilder;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "tokenizerFactory")
    private TokenizerFactory tokenizerFactory;

    @PostConstruct
    public void init() {
        builderMap = new HashMap<>();
        builderMap.put(0, new NoOrNoRanageConditionsBuilder());     // 0000
        builderMap.put(1, new NoOrHaveRanageConditionsBuilder());   // 0001
        builderMap.put(2, new HaveOrNoRanageConditionsBuilder());   // 0010
        builderMap.put(3, new HaveOrHaveRanageConditionsBuilder()); // 0011

        emptyConditionsBuilder = new EmptyConditionsBuilder();

        builderMap.values().forEach(b -> {
            if (StorageStrategyFactoryAble.class.isInstance(b)) {
                ((StorageStrategyFactoryAble) b).setStorageStrategyFactory(storageStrategyFactory);
            }
            if (TokenizerFactoryAble.class.isInstance(b)) {
                ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
            }

            if (Lifecycle.class.isInstance(b)) {
                try {
                    ((Lifecycle) b).init();
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            }
        });

        this.searchConditionsBuilder = new SearchConditionsBuilder();
        if (TokenizerFactoryAble.class.isInstance(this.searchConditionsBuilder)) {
            ((TokenizerFactoryAble) this.searchConditionsBuilder).setTokenizerFacotry(tokenizerFactory);
        }
    }

    /**
     * 获取实例.
     *
     * @param conditions 条件.
     * @return 实例.
     */
    public ConditionsBuilder<Conditions, SphinxQLWhere> getBuilder(Conditions conditions) {
        if (conditions.isEmtpy()) {
            return emptyConditionsBuilder;
        }

        return getBuilder(conditions.haveOrLink(), conditions.haveRangeCondition());
    }

    /**
     * 由外部指定是否含有or和范围查询.
     *
     * @param or    true 含有or连接符.
     * @param range true 含有范围查询.
     * @return 条件构造器.
     */
    public ConditionsBuilder<Conditions, SphinxQLWhere> getBuilder(boolean or, boolean range) {

        /*
         * or 字节低位开始第2位.
         * ranage 字节低位开始第1位.
         */
        int o = or ? 2 : 0;
        int r = range ? 1 : 0;
        return builderMap.get(o | r);
    }

    /**
     * 获得搜索条件构造器.
     *
     * @return 条件构造器实例.
     */
    public ConditionsBuilder<SearchConfig, SphinxQLWhere> getSearchBuilder() {
        return this.searchConditionsBuilder;
    }


    @Override
    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }
}
