package com.xforceplus.ultraman.oqsengine.storage.master.strategy;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.common.StringStorageStrategy;

/**
 * master 中关于分数的储存策略.
 * @author dongbin
 * @version 0.1 2020/3/4 18:08
 * @since 1.8
 */
public class DecimalStorageStrategy extends StringStorageStrategy {
    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }
}
