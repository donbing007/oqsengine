package com.xforceplus.ultraman.oqsengine.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;

/**
 * 计算结果提示.
 *
 * @author dongbin
 * @version 0.1 2021/07/08 14:13
 * @since 1.8
 */
public class CalculationHint {

    private IEntityField field;
    private String hint;

    public CalculationHint(IEntityField field, String hint) {
        this.field = field;
        this.hint = hint;
    }

    public IEntityField getField() {
        return field;
    }

    public String getHint() {
        return hint;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CalculationHint{");
        sb.append("field=").append(field);
        sb.append(", hint='").append(hint).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalculationHint that = (CalculationHint) o;
        return Objects.equals(getField(), that.getField()) && Objects.equals(getHint(), that.getHint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getField(), getHint());
    }
}
