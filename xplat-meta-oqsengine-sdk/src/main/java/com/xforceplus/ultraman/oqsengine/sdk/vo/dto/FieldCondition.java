package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;

/**
 *   *             fields: [
 *  *      *                 {
 *  *      *                     code: string,
 *  *      *                     operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
 *  *      *                     value: Array
 *  *      *                 }
 *  *      *             ],
 */
public class FieldCondition {

    String code;

    ConditionOp operation;

    List<String> values;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ConditionOp getOperation() {
        return operation;
    }

    public void setOperation(ConditionOp operation) {
        this.operation = operation;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
