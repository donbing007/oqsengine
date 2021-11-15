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
    private String code;

    /*
     * 条件值集合
     */
    private String[] value;

    /*
     * 操作符.
     */
    private String operation;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
