package com.xforceplus.ultraman.oqsengine.idgenerator.common.constant;

import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import java.util.Arrays;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/19/21 6:43 PM
 * @since 1.8
 */
public enum ResetModel {
    RESETABLE(1,"计数重置模式"),
    UNRESETABLE(0,"计数不可重置模式");


    private int value;
    private String desc;

    ResetModel(int model,String desc) {
        this.value = model;
        this.desc = desc;
    }

    public Integer value() {
        return this.value;
    }

    public static ResetModel fromValue(Integer value) {
        return Arrays.stream(ResetModel.values())
            .filter(item->item.value().equals(value))
            .findFirst().orElseThrow(() -> new IDGeneratorException("不存在的计数重置类型！"));
    }
}
