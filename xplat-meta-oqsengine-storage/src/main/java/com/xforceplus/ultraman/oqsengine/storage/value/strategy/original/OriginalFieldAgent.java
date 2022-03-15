package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * 非OQS托管数据的原生字段读取器.
 *
 * @author dongbin
 * @version 0.1 2022/2/28 15:57
 * @since 1.8
 */
public interface OriginalFieldAgent<S, W> {

    /**
     * 读取OQS托管属性物理表示.
     *
     * @return 原生字段值.
     * @throws Exception 发生异常.
     */
    public StorageValue read(IEntityField field, S originalData) throws Exception;

    /**
     * 将OQS原生属性值的物理表示写入静态储存.
     *
     * @param field        目标字段.
     * @param data         属性值.
     * @param originalData 静态数据表示.
     * @throws Exception 发生异常.
     */
    public void write(IEntityField field, StorageValue data, W originalData) throws Exception;
}
