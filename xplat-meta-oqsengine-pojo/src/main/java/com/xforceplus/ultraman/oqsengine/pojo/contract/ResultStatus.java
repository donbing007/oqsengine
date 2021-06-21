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
    ELEVATEFAILED(6),
    /**
     * 公式字段存在部分计算失败，使用默认值的情况.
     */
    HALF_SUCCESS(7);

    private final int value;

    ResultStatus(int value) {
        this.value = value;
    }

    public int getSymbol() {
        return value;
    }

    /**
     * 根据字面量获得实例.
     *
     * @param symbol 字面量.
     * @return 实例.
     */
    public static ResultStatus getInstance(int symbol) {
        for (ResultStatus r : ResultStatus.values()) {
            if (r.getSymbol() == symbol) {
                return r;
            }
        }

        return UNKNOWN;
    }

}
