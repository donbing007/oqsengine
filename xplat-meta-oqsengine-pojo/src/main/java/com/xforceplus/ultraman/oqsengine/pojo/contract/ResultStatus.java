package com.xforceplus.ultraman.oqsengine.pojo.contract;

/**
 * 响应状态.
 *
 * @author dongbin
 * @version 0.1 2020/6/23 14:25
 * @since 1.8
 */
public enum ResultStatus {

    /**
     * 未知.
     */
    UNKNOWN(0),
    /**
     * 成功.
     */
    SUCCESS(1),
    /**
     * 竞争冲突.
     */
    CONFLICT(2),
    /**
     * 未找到.
     */
    NOT_FOUND(3),
    /**
     * 创建失败.
     */
    UNCREATED(4),
    /**
     * 累加器调用失败.
     */
    UNACCUMULATE(5),
    /**
     * 转换公式、自增编号失败.
     */
    ELEVATEFAILED(6);

    private int value;

    private ResultStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
