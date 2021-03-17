package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ValueConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.SphinxQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;

/**
 * 没有范围查询,没有or 条件.主要利用全文搜索字段进行搜索.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 17:27
 * @since 1.8
 */
public class NoOrNoRanageConditionsBuilder implements ConditionsBuilder<String>, StorageStrategyFactoryAble, TokenizerFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;

    private SphinxQLConditionQueryBuilderFactory conditionQueryBuilderFactory;

    private TokenizerFactory tokenizerFactory;

    @Override
    public void init() {
        this.conditionQueryBuilderFactory = new SphinxQLConditionQueryBuilderFactory(this.storageStrategyFactory);
        this.conditionQueryBuilderFactory.setTokenizerFacotry(tokenizerFactory);
        this.conditionQueryBuilderFactory.init();
    }

    /**
     * 没有 or 只有 and 不需要关注连接符.
     */
    @Override
    public String build(IEntityClass entityClass, Conditions conditions) {

        StringBuilder idBuff = new StringBuilder();
        StringBuilder buff = new StringBuilder();
        buff.append("MATCH('(@").append(FieldDefine.ATTRIBUTEF).append(" ");
        // 用以判断是否还没有条件,方便条件之间的空格.
        int idEmptyLen = idBuff.length();
        int emtpyLen = buff.length();

        boolean allIdentifie = true;
        SphinxQLConditionBuilder conditionQueryBuilder;

        for (ConditionNode node : conditions.collect()) {
            if (Conditions.isValueNode(node)) {
                Condition condition = ((ValueConditionNode) node).getCondition();

                if (condition.getField().config().isIdentifie()) {
                    conditionQueryBuilder = conditionQueryBuilderFactory.getQueryBuilder(condition, false);

                    if (idBuff.length() > idEmptyLen) {
                        idBuff.append(" ").append(SqlKeywordDefine.AND).append(" ");
                    }
                    idBuff.append(conditionQueryBuilder.build(condition));

                } else {
                    allIdentifie = false;
                    conditionQueryBuilder = conditionQueryBuilderFactory.getQueryBuilder(condition, true);
                    if (buff.length() > emtpyLen) {
                        buff.append(" ");
                    }
                    buff.append(conditionQueryBuilder.build(condition));
                }
            }
        }

        // 如果全是主键id查询,那不需要以下处理.
        if (!allIdentifie) {

            buff.append(") (@")
                .append(FieldDefine.ENTITYCLASSF)
                .append(" =\"").append(entityClass.id()).append("\")");
            buff.append("')");

            StringBuilder temp = new StringBuilder();
            if (idBuff.length() > 0) {
                temp.append(idBuff.toString()).append(" ").append(SqlKeywordDefine.AND).append(" ");
                buff.insert(0, temp.toString());
            }

        } else {

            buff.delete(0, buff.length());
            if (idBuff.length() > 0) {
                buff.append(idBuff.toString());
            }

        }

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
}
