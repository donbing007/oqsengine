package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;

/**
 * 实际不会读取任何字段的读取器.
 *
 * @author dongbin
 * @version 0.1 2022/2/28 15:59
 * @since 1.8
 */
public class DoNothingOriginalFieldAgent implements OriginalFieldAgent {

    private static OriginalFieldAgent READER = new DoNothingOriginalFieldAgent();

    public static OriginalFieldAgent getInstance() {
        return READER;
    }


    @Override
    public StorageValue read(IEntityField field, Object originalData) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(IEntityField field, StorageValue data, Object originalData) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String plainText(IEntityField field, StorageValue data) throws Exception {
        throw new UnsupportedOperationException();
    }
}
