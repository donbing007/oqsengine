package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Optional;

/**
 * 实际不会读取任何字段的读取器.
 *
 * @author dongbin
 * @version 0.1 2022/2/28 15:59
 * @since 1.8
 */
public class DoNothingOriginalFieldReader implements OriginalFieldReader {

    private static OriginalFieldReader READER = new DoNothingOriginalFieldReader();

    public static OriginalFieldReader getInstance() {
        return READER;
    }


    @Override
    public Optional<Object> read(IEntityField field) {
        return Optional.empty();
    }
}
