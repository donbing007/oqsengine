package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Collections;
import java.util.Map;

/**
 * 定义公式类型的数据.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/11
 * @since 1.8
 */
public class FormulaTypedValue extends AbstractValue<Map<String, Object>> {

    public FormulaTypedValue(IEntityField field, Map<String, Object> params) {
        super(field, params);
    }

    public FormulaTypedValue(IEntityField field, Map<String, Object> value, String attachment) {
        super(field, value, attachment);
    }

    @Override
    Map<String, Object> fromString(String value) {
        return Collections.emptyMap();
    }

    @Override
    public long valueToLong() {
        return 0;
    }

    @Override
    protected IValue<Map<String, Object>> doCopy(IEntityField newField, String attachment) {
        return new FormulaTypedValue(newField, getValue(), attachment);
    }

    @Override
    protected IValue<Map<String, Object>> doCopy(Map<String, Object> value) {
        return new FormulaTypedValue(getField(), value, getAttachment().orElse(null));
    }

    @Override
    protected boolean skipTypeCheckWithCopy() {
        return true;
    }

    @Override
    public int compareTo(IValue o) {
        return 0;
    }
}
