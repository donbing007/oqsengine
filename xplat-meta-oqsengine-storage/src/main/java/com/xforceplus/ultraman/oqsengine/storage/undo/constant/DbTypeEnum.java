package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 12:29 AM
 * 功能描述:
 * 修改历史:
 */
public enum DbTypeEnum {
    MASTOR("1"), INDEX("2");

    String value;

    DbTypeEnum(String value) {
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
