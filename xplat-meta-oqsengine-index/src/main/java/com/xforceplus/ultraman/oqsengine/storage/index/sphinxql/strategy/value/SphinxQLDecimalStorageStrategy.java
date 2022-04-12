package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.SphinxQLManticoreIndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.AttachmentStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 索引的高精度浮点转换策略.
 * 会将数字拆分成整数部分和小数部份.
 * 小数部份会自动补0到18位.如下.
 * 123.123会被转换成  123 和 123000000000000000
 * 123.0123会被转换成 123 和 12300000000000000   由于小数从左开始有一个连续的0,所以在最后补0需要扣除.
 *
 * @author dongbin
 * @author luye
 * @version 0.1 2020/3/5 17:33
 * @since 1.8
 */
public class SphinxQLDecimalStorageStrategy implements StorageStrategy {

    final Logger logger = LoggerFactory.getLogger(SphinxQLManticoreIndexStorage.class);

    private static final String DIVIDE = ".";

    private static final String NEG = "-";

    private static final int FIXED = 18;

    @Override
    public FieldType fieldType() {
        return FieldType.DECIMAL;
    }

    @Override
    public StorageType storageType() {
        return StorageType.LONG;
    }

    @Override
    public IValue toLogicValue(IEntityField field, StorageValue storageValue, String attachment) {
        String firstStr = storageValue.value().toString();
        String secondStr = storageValue.next().value().toString();

        boolean isNeg = false;
        if (firstStr.trim().startsWith(NEG) || secondStr.trim().startsWith(NEG)) {
            isNeg = true;

            firstStr = firstStr.trim().startsWith(NEG) ? firstStr.substring(1) : firstStr;
            secondStr = secondStr.trim().startsWith(NEG) ? secondStr.substring(1) : secondStr;
        }


        String paddingStr = leftPaddingZero(secondStr, FIXED - secondStr.length());

        String value = isNeg ? NEG + firstStr + DIVIDE + paddingStr : firstStr + DIVIDE + paddingStr;
        return new DecimalValue(field, new BigDecimal(value), attachment);
    }

    @Override
    public StorageValue toStorageValue(IValue value) {
        return doBuildStorageValue(Long.toString(value.getField().id()), value.valueToString());
    }

    @Override
    public StorageValue toEmptyStorageValue(IEntityField field) {
        return doBuildStorageValue(Long.toString(field.id()), "0.0");
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
    public Collection<String> toStorageNames(IEntityField field, boolean shortName) {
        String logicName = shortName ? Long.toString(field.id(), 36) : Long.toString(field.id());

        return Arrays.asList(
            logicName + storageType().getType() + "0",
            logicName + storageType().getType() + "1"
        );
    }

    @Override
    public boolean isMultipleStorageValue() {
        return true;
    }

    /**
     * 预期是一个浮点数的字符串.
     */
    @Override
    public StorageValue convertIndexStorageValue(String storageName, Object storageValue, boolean attachment, boolean attrF) {
        String logicName = AnyStorageValue.getInstance(storageName).logicName();
        if (attachment) {
            return new AttachmentStorageValue(logicName, (String) storageValue, true);
        }
        return doBuildStorageValue(logicName, (String) storageValue);
    }

    private StorageValue doBuildStorageValue(String logicName, String value) {
        String number = value;

        String[] numberArr = number.split("\\" + DIVIDE);


        String firstNumStr = numberArr[0];

        boolean isNeg = false;
        if (firstNumStr.trim().startsWith("-")) {
            isNeg = true;
        }

        /*
         * 14.0303503943
         * 0303503943
         * 030450303400000
         */
        String secondStr;
        if (numberArr.length > 2) {
            secondStr = numberArr[1];
        } else {

            if (logger.isWarnEnabled()) {
                logger.warn("The decimal is assumed to be 0.[{}]", value);
            }

            secondStr = "0";
        }

        int i = bitLength(secondStr);

        long first = Long.parseLong(numberArr[0]);
        long second = Long.parseLong(paddingZero(secondStr.substring(0, i), FIXED - i));

        if (first < 0 || isNeg) {
            second = 0 - second;
        }

        StorageValue<Long> storageValue = new LongStorageValue(logicName, first, true);
        storageValue.locate(0);
        storageValue.stick(new LongStorageValue(logicName, second, true));

        return storageValue;
    }

    private int bitLength(String longStr) {
        int bitLength = longStr.length();

        /*
         * omit all tail zeros
         */
        for (int i = longStr.length() - 1; i > 0; i--) {
            char c = longStr.charAt(i);
            if (c == '0') {
                bitLength--;
            } else {
                break;
            }
        }

        return bitLength;
    }

    private String leftPaddingZero(String longStr, int padding) {
        StringBuffer sb = new StringBuffer();

        while (padding-- > 0) {
            sb.append(0);
        }

        sb.append(longStr);

        return sb.toString();
    }

    private String paddingZero(String longStr, int padding) {

        StringBuffer sb = new StringBuffer();

        sb.append(longStr);

        while (padding-- > 0) {
            sb.append(0);
        }
        return sb.toString();
    }
}
