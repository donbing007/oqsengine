package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/23/2020 12:04 PM
 * 功能描述:
 * 修改历史:
 */
public enum OpTypeEnum {

    BUILD("1"), REPLACE("2"), DELETE("3");

    String value;

    OpTypeEnum(String value) {
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
