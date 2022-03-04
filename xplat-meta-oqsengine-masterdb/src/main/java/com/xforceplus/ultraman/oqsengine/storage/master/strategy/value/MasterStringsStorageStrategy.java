package com.xforceplus.ultraman.oqsengine.storage.master.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 * 多字符串值字段储存策略.
 * 可以接收两种格式.
 * 1. [v1][v2][v3][v4]
 * 2. v1,v2,v3,v4        此格式是为兼容处理.
 * 但只会输出第一种格式.
 *
 * @author dongbin
 * @version 0.1 2020/11/9 16:52
 * @since 1.8
 */
public class MasterStringsStorageStrategy implements StorageStrategy {

    private static final char OLD_STYLE_SEPARATOR = ',';
    private static final char START = '[';
    private static final char END = ']';

    @Override
    public FieldType fieldType() {
        return FieldType.STRINGS;
    }

    @Override
    public StorageType storageType() {
        return StorageType.STRING;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachemnt) {
        StringStorageValue stringStorageValue = (StringStorageValue) storageValue;
        String value = stringStorageValue.value();

        if (isNewStyle(value)) {
            return doNewStyleToLogicValue(field, value, attachemnt);
        } else {
            return doOldStyleToLogicValue(field, value, attachemnt);
        }
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        StringsValue stringsValue = (StringsValue) value;
        StringBuilder buff = new StringBuilder();
        for (String v : stringsValue.getValue()) {
            buff.append(START).append(v).append(END);
        }
        return new StringStorageValue(Long.toString(value.getField().id()), buff.toString(), true);
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        return new StringStorageValue(Long.toString(field.id()), "", true);
    }

    @Override
    public boolean isMultipleStorageValue() {
        return false;
    }

    private IValue doNewStyleToLogicValue(IEntityField field, String value, String attachemnt) {
        List<String> list = new ArrayList<>();
        StringBuffer buff = new StringBuffer();
        boolean watch = false;
        for (char v : value.toCharArray()) {
            if (v == START) {
                watch = true;
            } else if (v == END && watch) {
                list.add(buff.toString());
                buff.delete(0, buff.length());
                watch = false;
            } else {
                buff.append(v);
            }
        }

        return new StringsValue(field, list.toArray(new String[0]), attachemnt);
    }

    private IValue doOldStyleToLogicValue(IEntityField field, String value, String attachemnt) {
        List<String> list = new ArrayList<>();
        StringBuilder buff = new StringBuilder();
        for (char v : value.toCharArray()) {
            if (OLD_STYLE_SEPARATOR == v) {
                list.add(buff.toString());
                buff.delete(0, buff.length());
            } else {
                buff.append(v);
            }
        }
        return new StringsValue(field, list.toArray(new String[0]), attachemnt);
    }

    private boolean isNewStyle(String value) {
        return value.startsWith(String.valueOf(START)) && value.endsWith(String.valueOf(END));
    }
}
