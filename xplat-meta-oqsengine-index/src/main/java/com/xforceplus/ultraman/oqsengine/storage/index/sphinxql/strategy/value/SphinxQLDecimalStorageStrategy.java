package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author dongbin
 * @version 0.1 2020/3/5 17:33
 * @since 1.8
 */
public class SphinxQLDecimalStorageStrategy implements StorageStrategy {

    private static final String DIVIDE = ".";

    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }

    @Override
    public StorageType storageType() {
        return StorageType.LONG;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue) {
        String value = storageValue.value().toString() + DIVIDE + storageValue.next().value().toString();
        return new DecimalValue(field, new BigDecimal(value));
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String number = value.valueToString();

        String[] numberArr = number.split("\\" + DIVIDE);

        long first = Long.parseLong(numberArr[0]);
        long second = Long.parseLong(numberArr[1]);

        StorageValue<Long> storageValue = new LongStorageValue(Long.toString(value.getField().id()), first, true);
        storageValue.locate(0);
        storageValue.stick(new LongStorageValue(Long.toString(value.getField().id()), second, true));

        return storageValue;
    }

    @Override
    public Collection<String> toStorageNames(IEntityField field) {
        String logicName = field.name();
        return Arrays.asList(
            logicName + storageType().getType() + "0",
            logicName + storageType().getType() + "1"
        );
    }

    @Override
    public boolean isMultipleStorageValue() {
        return true;
    }
}
