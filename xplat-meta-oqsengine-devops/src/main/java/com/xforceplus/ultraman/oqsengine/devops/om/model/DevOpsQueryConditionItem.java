package com.xforceplus.ultraman.oqsengine.devops.om.model;

/**
 * @copyright： 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsQueryConditionItem {

    /*
     * 字段信息
     */
    private String field;

    /*
     * 条件值集合
     */
    private String[] values;

    /*
     * 操作符.
     */
    private String operator;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
