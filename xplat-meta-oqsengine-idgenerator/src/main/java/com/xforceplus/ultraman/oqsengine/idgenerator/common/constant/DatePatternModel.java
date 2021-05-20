package com.xforceplus.ultraman.oqsengine.idgenerator.common.constant;

import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import java.util.Arrays;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/18/21 11:36 AM
 * @since 1.8
 */
public enum  DatePatternModel {

    YYYY(1,"yyyy模式"),
    YYYYMMDD(2,"yyyy-MM-dd模式"),
    YYYYMM(3,"yyyy-MM");


    private int value;
    private String desc;

     DatePatternModel(int model,String desc) {
        this.value = model;
        this.desc = desc;
    }

    public Integer value() {
         return this.value;
    }

    public static DatePatternModel fromValue(Integer value) {
        return Arrays.stream(DatePatternModel.values())
            .filter(item->item.value().equals(value))
            .findFirst().orElseThrow(() -> new IDGeneratorException("不存在的日期类型！"));
    }
}
