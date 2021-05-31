package com.xforceplus.ultraman.oqsengine.idgenerator.common.constant;

import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import java.util.Arrays;

/**
 * IDModel.
 *
 * @author leo
 */
public enum IDModel {

    TREND_INC(1, "趋势递增"),
    LINEAR_INC(2, "线性递增");

    private int value;
    private String desc;

    IDModel(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer value() {
        return this.value;
    }

    /**
     * Get IDModel from value.
     *
     * @param value value
     * @return IDModel
     */
    public static IDModel fromValue(Integer value) {
        return Arrays.stream(IDModel.values())
            .filter(item -> item.value().equals(value))
            .findFirst().orElseThrow(() -> new IDGeneratorException("不存在的函数类型！"));
    }

}
