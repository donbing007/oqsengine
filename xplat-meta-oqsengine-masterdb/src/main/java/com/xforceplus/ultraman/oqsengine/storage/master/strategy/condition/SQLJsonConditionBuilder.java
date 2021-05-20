package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator.MULTIPLE_EQUALS;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

/**
 * json的条件查询语句构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:56
 * @since 1.8
 */
public class SQLJsonConditionBuilder implements ConditionBuilder<Condition, String> {

    private FieldType fieldType;
    private ConditionOperator operator;
    /*
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

    /**
     * 构造基于JSON的条件查询构造器实例.
     *
     * @param fieldType              字段逻辑类型.
     * @param operator               操作符.
     * @param storageStrategyFactory 逻辑物理字段转换策略工厂实例.
     */
    public SQLJsonConditionBuilder(
        FieldType fieldType, ConditionOperator operator, StorageStrategyFactory storageStrategyFactory) {
        this.fieldType = fieldType;
        this.operator = operator;
        this.storageStrategyFactory = storageStrategyFactory;
    }

    @Override
    public FieldType fieldType() {
        return fieldType;
    }

    @Override
    public ConditionOperator operator() {
        return operator;
    }

    @Override
    public String build(Condition condition) {
        IEntityField field = condition.getField();
        StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(field.type());
        StringBuilder sql = new StringBuilder();

        // id查询.
        if (field.config().isIdentifie()) {

            sql.append("id ").append(condition.getOperator().getSymbol()).append(' ');

            if (condition.getOperator().getSymbol().equals(MULTIPLE_EQUALS.getSymbol())) {
                sql.append("(");
                boolean isFirst = true;
                for (IValue value : condition.getValues()) {
                    if (!isFirst) {
                        sql.append(",");
                    }
                    StorageValue idStorageValue = storageStrategy.toStorageValue(value);
                    sql.append(idStorageValue.value());
                    isFirst = false;
                }
                sql.append(")");
            } else {
                StorageValue idStorageValue = storageStrategy.toStorageValue(condition.getFirstValue());
                sql.append(idStorageValue.value());
            }
            return sql.toString();
        }

        /*
         * 为了突破JSON中长整形的上限,这里查询时需要转换成有符号数.
         */
        if (storageStrategy.storageType() == StorageType.LONG) {
            sql.append("CAST(");
        }
        // attr->>'$.attribute_name'
        sql.append(FieldDefine.ATTRIBUTE)
            .append("->>'$.")
            .append(AnyStorageValue.ATTRIBUTE_PREFIX)
            .append(storageStrategy.toStorageNames(field).stream().findFirst().get())
            .append("\'");
        if (storageStrategy.storageType() == StorageType.LONG) {
            sql.append(" AS SIGNED)");
        }
        sql.append(' ');

        sql.append(condition.getOperator().getSymbol()).append(' ');

        StorageValue storageValue = null;
        if (MULTIPLE_EQUALS == condition.getOperator()) {

            sql.append("(");
            final int emptySize = sql.length();
            for (IValue value : condition.getValues()) {
                if (sql.length() > emptySize) {
                    sql.append(",");
                }
                storageValue = storageStrategy.toStorageValue(value);
                appendValue(sql, storageValue);
            }

            sql.append(")");

        } else if (ConditionOperator.LIKE == condition.getOperator()) {
            // like 不会操作数值,一定是字符串.
            storageValue = storageStrategy.toStorageValue(condition.getFirstValue());
            String likeValue = storageValue.value().toString();

            // 如果模糊模式为NOT,那表示不能进行模糊搜索.输出恒否的表达式.
            if (FieldConfig.FuzzyType.NOT == condition.getField().config().getFuzzyType()) {
                return "2 = 1";
            }

            // 如果是通配符模糊类型,那么查询字串不能小于最小值大于最大值.
            if (FieldConfig.FuzzyType.WILDCARD == condition.getField().config().getFuzzyType()
                && likeValue.length() < condition.getField().config().getWildcardMinWidth()
                || likeValue.length() > condition.getField().config().getWildcardMaxWidth()) {

                // 返回恒不为真的表达式.
                return "2 = 1";
            } else {
                sql.append("\"%").append(likeValue).append("%\"");
            }
        } else {
            storageValue = storageStrategy.toStorageValue(condition.getFirstValue());
            appendValue(sql, storageValue);
        }

        return sql.toString();
    }

    private void appendValue(StringBuilder sql, StorageValue value) {
        if (value.type() == StorageType.STRING) {
            sql.append("\"").append(encode((String) value.value())).append("\"");
        } else {
            sql.append(value.value());
        }
    }

    private static final char[] ESCAPE_CHARACTER = {
        '\n',
        '\t',
        '\r',
        '\b',
        '\'',
        '\"',
        '\\',
        '%',
    };

    /**
     * 处理sql中的需要转义字符.
     */
    private String encode(String value) {
        StringBuilder buff = new StringBuilder();
        boolean needEncode = false;
        for (char c : value.toCharArray()) {
            needEncode = false;
            for (char escapeC : ESCAPE_CHARACTER) {
                if (c == escapeC) {
                    needEncode = true;
                    buff.append("\\").append(c);
                    break;
                }
            }

            if (!needEncode) {
                buff.append(c);
            }
        }
        return buff.toString();
    }
}
