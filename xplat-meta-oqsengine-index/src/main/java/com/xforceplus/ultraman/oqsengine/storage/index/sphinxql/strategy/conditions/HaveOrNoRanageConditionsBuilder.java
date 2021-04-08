package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;

import java.sql.SQLException;

/**
 * 含有OR但是没有范围查询.
 * 最终会尽量会都使用 SphinxQL中的match来达成查询目标.
 *
 * @author dongbin
 * @version 0.1 2021/4/7 13:53:29
 * @since 1.8
 */
public class HaveOrNoRanageConditionsBuilder
    implements ConditionsBuilder<String>, StorageStrategyFactoryAble, TokenizerFactoryAble, Lifecycle {

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionQueryBuilderFactory conditionQueryBuilderFactory;

    private TokenizerFactory tokenizerFactory;

    @Override
    public void init() throws SQLException {
        this.conditionQueryBuilderFactory = new SphinxQLConditionQueryBuilderFactory(this.storageStrategyFactory);
        this.conditionQueryBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        this.conditionQueryBuilderFactory.init();
    }

    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {
        if (conditions.isEmtpy()) {
            return "";
        }

        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('(");
        conditions.scan(
            link -> {
                switch (link.getLink()) {
                    case AND: {
                        buff.append(' ');
                        break;
                    }
                    case OR: {
                        buff.append(" | ");
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unexpected conditional operation symbol.");
                    }
                }
            },
            value -> {
                SphinxQLConditionBuilder builder =
                    conditionQueryBuilderFactory.getQueryBuilder(value.getCondition(), true);

                if (value.getCondition().getOperator() == ConditionOperator.NOT_EQUALS) {

                    String source = builder.build(value.getCondition());

                    buff.append(processNotEquals(entityClass, source));

                } else {

                    buff.append("(@").append(FieldDefine.ATTRIBUTEF).append(' ')
                        .append(builder.build(value.getCondition()))
                        .append(')');
                }
            },
            parenthese -> {
                buff.append(parenthese.toString());
            }
        );

        // 追加entityClass限定.
        buff.append(") (@").append(FieldDefine.ENTITYCLASSF).append(" =").append(entityClass.id()).append(")");
        buff.append("')");


        return buff.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    public StorageStrategyFactory getStorageStrategyFactory() {
        return storageStrategyFactory;
    }

    public SphinxQLConditionQueryBuilderFactory getConditionQueryBuilderFactory() {
        return conditionQueryBuilderFactory;
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }

    public TokenizerFactory getTokenizerFactory() {
        return tokenizerFactory;
    }

    /**
     * 不等于需要特殊处理,在含有OR中不能直接排除需要在一个范围内排除.
     * 1. ( key0 | -key1)   错误的,在OR连接中不能直接使用排除.
     * 2. ( key0 | (key3 -key1) 正确,排除只能在一个已有范围内排除.
     * 这里会将 -key1 转换成 (key3 -key1), key3即是entityClass.
     *
     * @param source 已经处理成 -key1 这样的格式.
     */
    private String processNotEquals(IEntityClass entityClass, String source) {
        StringBuilder buff = new StringBuilder();
        buff.append("(@").append(FieldDefine.ENTITYCLASSF).append(" =").append(entityClass.id()).append(' ')
            .append("@").append(FieldDefine.ATTRIBUTEF).append(' ').append(source)
            .append(')');
        return buff.toString();
    }
}
