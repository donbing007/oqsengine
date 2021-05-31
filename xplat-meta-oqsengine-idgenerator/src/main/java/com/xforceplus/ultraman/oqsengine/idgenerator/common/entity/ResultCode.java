package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

/**
 * Result code.
 *
 * @author leo
 */
public enum ResultCode {

    NORMAL(1, "正常使用"),
    LOADING(2, "加载下一区段编号"),
    OVER(3, "编号超出缓冲最大编号"),
    RESET(4, "重置编号");

    private int code;
    private String desc;

    ResultCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
