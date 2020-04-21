package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 查询条件配置对象
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public class Condition implements Serializable {

    /**
     * 条件字段属于的 entityClass.
     */
    private IEntityClass entityClass;
    /**
     * 字段信息
     */
    private IEntityField field;

    /**
     * 条件值集合
     */
    private IValue[] values;

    /**
     * 操作符
     */
    private ConditionOperator operator;

    /**
     * 是否范围查询
     */
    private boolean range;

    /**
     * 不指定 entityClass 来构造一个条件.实际的 entityClass 将由搜索执行器来假定.
     * @param field 字段信息.
     * @param operator 条件操作符.
     * @param values 条件比较值列表.
     */
    public Condition(IEntityField field, ConditionOperator operator, IValue... values) {
        this(null, field, operator, values);
    }

    /**
     * 构造一个新的查询条件.
     * @param entityClass 字段所属于的 entity 类型信息.
     * @param field 字段.
     * @param operator 比较符号.
     * @param values 条件比较值列表.
     */
    public Condition(IEntityClass entityClass, IEntityField field, ConditionOperator operator, IValue... values) {
        this.entityClass = entityClass;
        this.field = field;
        this.operator = operator;
        this.values = values;
        if (this.values == null || this.values.length == 0) {
            throw new IllegalArgumentException("Invalid query condition, must have at least one value.");
        }

        checkRange();
    }

    /**
     * 昨到 entity class 信息.
     * @return entityClass 实例.
     */
    public Optional<IEntityClass> getEntityClass() {
        return Optional.ofNullable(entityClass);
    }

    /**
     * 条件字段信息.
     * @return 条件字段.
     */
    public IEntityField getField() {
        return field;
    }

    /**
     * 条件的首个值.所有条件都至少有一个值.
     * @return 首个值.
     */
    public IValue getFirstValue() {
        return values[0];
    }

    /**
     * 返回所有条件值.
     * @return 条件值列表.
     */
    public IValue[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * 返回条件比较操作符.
     * @return 操作符.
     */
    public ConditionOperator getOperator() {
        return operator;
    }

    /**
     * 条件查询是否为范围查询.
     * @return true 是, false 不是.
     */
    public boolean isRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Condition)) {
            return false;
        }
        Condition condition = (Condition) o;
        return isRange() == condition.isRange() &&
            Objects.equals(getEntityClass(), condition.getEntityClass()) &&
            Objects.equals(getField(), condition.getField()) &&
            Arrays.equals(getValues(), condition.getValues()) &&
            getOperator() == condition.getOperator();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getEntityClass(), getField(), getOperator(), isRange());
        result = 31 * result + Arrays.hashCode(getValues());
        return result;
    }

    @Override
    public String toString() {
        if (entityClass != null) {
            return "Condition{" +
                "entityClass=" + entityClass +
                ", field=" + field +
                ", values=" + Arrays.toString(values) +
                ", operator=" + operator +
                ", range=" + range +
                '}';
        } else {
            return "Condition{" +
                ", field=" + field +
                ", values=" + Arrays.toString(values) +
                ", operator=" + operator +
                ", range=" + range +
                '}';
        }
    }

    /**
     * 判断是否含有范围查询符号.
     */
    private void checkRange() {

        switch (getOperator()) {
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_EQUALS:
            case GREATER_THAN_EQUALS:
                range = true;
                break;
            default:
                range = false;
        }

    }
}
