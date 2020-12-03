package com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;

import java.util.Arrays;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator.MULTIPLE_EQUALS;

/**
 * @author dongbin
 * @version 0.1 2020/11/4 15:56
 * @since 1.8
 */
public class SQLJsonConditionBuilder implements ConditionBuilder<String> {

    private FieldType fieldType;
    private ConditionOperator operator;
    /**
     * 物理逻辑转换策略工厂.
     */
    private StorageStrategyFactory storageStrategyFactory;

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
                for(IValue value : condition.getValues()) {
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
//            sql.append(idStorageValue.value());
            return sql.toString();
        }

        sql.append(FieldDefine.ATTRIBUTE)
            .append("->>'$.")
            .append(FieldDefine.ATTRIBUTE_PREFIX).append(
            storageStrategy.toStorageNames(field).stream().findFirst().get())
            .append("\' ")
            .append(condition.getOperator().getSymbol())
            .append(' ');

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
            sql.append("\"%").append(storageValue.value()).append("%\"");
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

    private static char[] ESCAPE_CHARACTER = {
        '\n',
        '\t',
        '\r',
        '\b',
        '\'',
        '\"',
        '\\',
        '%',
        '_',
    };

    /**
     * 处理sql中的需要转义字符.
     *
     * @param value
     * @return
     */
    private String encode(String value) {
        StringBuilder buff = new StringBuilder();
        boolean needEncode = false;
        for (char c : value.toCharArray()) {
            needEncode = false;
            for (char escapeC : ESCAPE_CHARACTER) {
                if (c == escapeC) {
                    needEncode = true;
                    buff.append('\\').append(c);
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
