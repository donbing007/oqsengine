package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Optional;

/**
 * 非OQS托管数据的原生字段读取器.
 *
 * @author dongbin
 * @version 0.1 2022/2/28 15:57
 * @since 1.8
 */
public interface OriginalFieldReader<T> {

    /**
     * 读取原生字段的值.
     * 注意: 返回值有可能为null.
     *
     * @return 原生字段值.
     */
    public Optional<T> read(IEntityField field);
}
