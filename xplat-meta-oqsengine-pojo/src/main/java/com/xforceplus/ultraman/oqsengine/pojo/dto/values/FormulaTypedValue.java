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

    @Override
    Map<String, Object> fromString(String value) {
        return Collections.emptyMap();
    }

    @Override
    public long valueToLong() {
        return 0;
    }

    @Override
    protected void checkType(IEntityField newFiled) {
        return;
    }

    @Override
    public IValue<Map<String, Object>> copy(IEntityField newField) {
        checkType(newField);

        return new FormulaTypedValue(newField, getValue());
    }
}
