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

    private static final String NEG = "-";

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

        String firstStr = storageValue.value().toString();
        String secondStr = storageValue.next().value().toString();

        boolean isNeg = false;
        if(firstStr.trim().startsWith(NEG) || secondStr.trim().startsWith(NEG)){
            isNeg = true;

            firstStr = firstStr.trim().startsWith(NEG) ? firstStr.substring(1) : firstStr;
            secondStr = secondStr.trim().startsWith(NEG) ? secondStr.substring(1) : secondStr;
        }


        String value = isNeg ? NEG + firstStr + DIVIDE + secondStr : firstStr + DIVIDE + secondStr;
        return new DecimalValue(field, new BigDecimal(value));
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        String number = value.valueToString();

        String[] numberArr = number.split("\\" + DIVIDE);


        String firstNumStr = numberArr[0];

        boolean isNeg = false;
        if(firstNumStr.trim().startsWith("-")){
            isNeg = true;
        }

        long first = Long.parseLong(numberArr[0]);
        long second = Long.parseLong(numberArr[1]);

        if(first < 0 || isNeg){
            second = 0 - second;
        }

        StorageValue<Long> storageValue = new LongStorageValue(Long.toString(value.getField().id()), first, true);
        storageValue.locate(0);
        storageValue.stick(new LongStorageValue(Long.toString(value.getField().id()), second, true));

        return storageValue;
    }

    @Override
    public Collection<String> toStorageNames(IEntityField field) {
        String logicName = Long.toString(field.id());

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
