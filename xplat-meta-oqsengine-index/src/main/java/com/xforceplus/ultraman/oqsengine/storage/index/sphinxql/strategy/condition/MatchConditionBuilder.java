package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;

/**
 * 用于 match 函数中的匹配条件构造器.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 10:13
 * @since 1.8
 */
public class MatchConditionBuilder extends SphinxQLConditionBuilder implements TokenizerFactoryAble {

    private TokenizerFactory tokenizerFactory;

    public MatchConditionBuilder(
        StorageStrategyFactory storageStrategyFactory, FieldType fieldType, ConditionOperator operator, boolean useGroupName) {
        super(storageStrategyFactory, fieldType, operator, true, useGroupName);
    }

    @Override
    protected String doBuild(Condition condition) {
        IValue logicValue = condition.getFirstValue();
        StorageStrategy storageStrategy = getStorageStrategyFactory().getStrategy(logicValue.getField().type());
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        StringBuilder buff = new StringBuilder();

        String symbol;
        FieldConfig.FuzzyType fuzzyType = FieldConfig.FuzzyType.NOT;
        switch (operator()) {
            case NOT_EQUALS:
                symbol = "-";
                break;
            case EQUALS:
                symbol = "";
                break;
            case LIKE:
                symbol = "";
                fuzzyType = logicValue.getField().config().getFuzzyType();
                break;
            default:
                throw new IllegalStateException(String.format("Unsupported operator.[%s]", operator().getSymbol()));
        }

        int conditonSize = 0;
        while (storageValue != null) {

            String query = "";
            switch (fuzzyType) {
                case SEGMENTATION: {
                    query = SphinxQLHelper.buildSegmentationQuery(
                        storageValue, this.tokenizerFactory.getTokenizer(logicValue.getField()));
                    break;
                }
                case WILDCARD: {
                    query = SphinxQLHelper.buildWirdcardQuery(storageValue);
                    break;
                }
                case NOT: {
                    query = SphinxQLHelper.buildPreciseQuery(storageValue, isUseStorageGroupName());
                    break;
                }
                default: {
                    query = SphinxQLHelper.buildWirdcardQuery(storageValue);
                }
            }

            if (buff.length() > 0) {
                buff.append(' ');
            }

            buff.append(query);

            conditonSize++;

            storageValue = storageValue.next();
        }

        final int onlyOneValue = 1;
        if (conditonSize > onlyOneValue) {
            buff.insert(0, "(").insert(0, symbol);
            buff.append(")");
        } else {
            buff.insert(0, symbol);
        }

        return buff.toString();
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }
}
