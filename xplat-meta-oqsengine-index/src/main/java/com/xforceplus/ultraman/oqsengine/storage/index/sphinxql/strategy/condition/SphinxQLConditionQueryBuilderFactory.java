package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.AttachmentConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.MatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.MeqMatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.MeqNotMatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.NotMatchConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal.GtEqNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal.GtNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal.LtEqNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.decimal.LtNotMatchDecimalConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactoryAble;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 查询条件构造器工厂.
 *
 * @author dongbin
 * @version 0.1 2020/3/26 10:22
 * @since 1.8
 */
public class SphinxQLConditionQueryBuilderFactory implements TokenizerFactoryAble, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private ConcurrentMap<String, ConditionBuilder<Condition, String>> builders;
    private Map<String, ConditionBuilder<Condition, String>> nullCondtitonBuilders;
    private TokenizerFactory tokenizerFactory;

    private static final String NORMAL_PREIFX = "normal";
    private static final String ATTACHMENT_PREFIX = "attachemnt";

    private static String buildKey(String prefix, FieldType fieldType, ConditionOperator operator, boolean match,
                                   boolean id) {
        StringBuilder buff = new StringBuilder();
        buff.append(prefix)
            .append('-')
            .append(fieldType.getType())
            .append('-')
            .append(operator.getSymbol())
            .append('-')
            .append(match)
            .append('-')
            .append(id);
        return buff.toString();
    }

    private static String buildNormalKey(FieldType fieldType, ConditionOperator operator, boolean match, boolean id) {
        return buildKey(NORMAL_PREIFX, fieldType, operator, match, id);
    }

    private static String buildAttachmentKey(
        FieldType fieldType, ConditionOperator operator, boolean match, boolean id) {
        return buildKey(ATTACHMENT_PREFIX, fieldType, operator, match, id);
    }

    /**
     * 初始化.
     */
    public void init() {
        builders = new ConcurrentHashMap<>();

        builders.put(
            buildAttachmentKey(FieldType.STRING, ConditionOperator.EQUALS, true, false),
            new AttachmentConditionBuilder(ConditionOperator.EQUALS)
        );

        builders.put(
            buildAttachmentKey(FieldType.STRING, ConditionOperator.NOT_EQUALS, true, false),
            new AttachmentConditionBuilder(ConditionOperator.NOT_EQUALS)
        );

        // decimal
        builders.put(
            buildNormalKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN, false, false),
            new GtNotMatchDecimalConditionBuilder()
        );
        builders.put(
            buildNormalKey(FieldType.DECIMAL, ConditionOperator.GREATER_THAN_EQUALS, false, false),
            new GtEqNotMatchDecimalConditionBuilder()
        );
        builders.put(
            buildNormalKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN, false, false),
            new LtNotMatchDecimalConditionBuilder()
        );
        builders.put(
            buildNormalKey(FieldType.DECIMAL, ConditionOperator.LESS_THAN_EQUALS, false, false),
            new LtEqNotMatchDecimalConditionBuilder()
        );

        // meq
        builders.put(
            buildNormalKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(FieldType.LONG, false)
        );
        builders.put(
            buildNormalKey(FieldType.STRING, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(FieldType.STRING, false)
        );
        builders.put(
            buildNormalKey(FieldType.BOOLEAN, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(FieldType.BOOLEAN, false)
        );
        builders.put(
            buildNormalKey(FieldType.ENUM, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(FieldType.ENUM, false)
        );

        // long
        builders.put(
            buildNormalKey(FieldType.LONG, ConditionOperator.MULTIPLE_EQUALS, false, true),
            new MeqNotMatchConditionBuilder(FieldType.LONG)
        );

        // strings
        builders.put(
            buildNormalKey(FieldType.STRINGS, ConditionOperator.EQUALS, true, false),
            new MatchConditionBuilder(FieldType.STRINGS, ConditionOperator.EQUALS, true)
        );
        builders.put(
            buildNormalKey(FieldType.STRINGS, ConditionOperator.NOT_EQUALS, true, false),
            new MatchConditionBuilder(FieldType.STRINGS, ConditionOperator.NOT_EQUALS,
                true)
        );
        builders.put(
            buildNormalKey(FieldType.STRINGS, ConditionOperator.MULTIPLE_EQUALS, true, false),
            new MeqMatchConditionBuilder(FieldType.STRINGS, true)
        );

        builders.values().stream().forEach(b -> {
            if (TokenizerFactoryAble.class.isInstance(b)) {
                ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
            }

            if (StorageStrategyFactoryAble.class.isInstance(b)) {
                ((StorageStrategyFactoryAble) b).setStorageStrategyFactory(storageStrategyFactory);
            }
        });

        // 所有字段准备空查询.
        nullCondtitonBuilders = new HashMap<>();
        Arrays.stream(FieldType.values()).filter(f -> FieldType.UNKNOWN != f)
            .forEach(f -> {
                String key = buildNormalKey(f, ConditionOperator.IS_NULL, false, false);
                nullCondtitonBuilders.put(key, new NullQueryConditionBuilder(f, ConditionOperator.IS_NULL));

                key = buildNormalKey(f, ConditionOperator.IS_NOT_NULL, false, false);
                nullCondtitonBuilders.put(key, new NullQueryConditionBuilder(f, ConditionOperator.IS_NOT_NULL));
            });

        nullCondtitonBuilders.values().stream().forEach(b -> {
            if (TokenizerFactoryAble.class.isInstance(b)) {
                ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
            }

            if (StorageStrategyFactoryAble.class.isInstance(b)) {
                ((StorageStrategyFactoryAble) b).setStorageStrategyFactory(storageStrategyFactory);
            }
        });
    }

    /**
     * 获取条件查询条件构造器实例.
     *
     * @param condition 条件.
     * @param match     true 全文,false非全文.
     * @return 实例.
     */
    public ConditionBuilder<Condition, String> getQueryBuilder(Condition condition, boolean match) {

        if (condition.isNullQuery()) {
            return getNullQueryBuilder(condition);
        } else {
            return getBuilder(condition, match);
        }
    }

    @Override
    public void setTokenizerFacotry(TokenizerFactory tokenizerFacotry) {
        this.tokenizerFactory = tokenizerFacotry;
    }

    @Override
    public void setStorageStrategyFactory(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private ConditionBuilder<Condition, String> getNullQueryBuilder(Condition condition) {
        String key = buildNormalKey(
            condition.getField().type(), condition.getOperator(), false, false);

        return nullCondtitonBuilders.get(key);
    }

    private ConditionBuilder<Condition, String> getBuilder(Condition condition, boolean match) {
        ConditionBuilder<Condition, String> builder = null;
        if (AttachmentCondition.class.isInstance(condition)) {

            String key = buildAttachmentKey(FieldType.STRING, condition.getOperator(), true, false);
            builder = builders.get(key);

            if (builder == null) {
                throw new IllegalArgumentException(
                    "Unable to construct a valid attachment query condition constructor.");
            }

        } else {
            String key = buildNormalKey(
                condition.getField().type(), condition.getOperator(), match,
                condition.getField().config().isIdentifie());

            builder = builders.computeIfAbsent(key, k -> {
                AbstractSphinxQLConditionBuilder b;
                if (match) {
                    b = new MatchConditionBuilder(condition.getField().type(), condition.getOperator(), false);
                } else {

                    b = new NotMatchConditionBuilder(condition.getField().type(), condition.getOperator());
                }

                if (TokenizerFactoryAble.class.isInstance(b)) {
                    ((TokenizerFactoryAble) b).setTokenizerFacotry(tokenizerFactory);
                }

                if (StorageStrategyFactoryAble.class.isInstance(b)) {
                    ((StorageStrategyFactoryAble) b).setStorageStrategyFactory(storageStrategyFactory);
                }
                return b;
            });
        }

        return builder;
    }
}