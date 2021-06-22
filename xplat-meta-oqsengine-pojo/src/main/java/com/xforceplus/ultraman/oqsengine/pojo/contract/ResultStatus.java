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
    UNKNOWN((byte) 0),
    /**
     * 成功.
     */
    SUCCESS((byte) 1),
    /**
     * 竞争冲突.
     */
    CONFLICT((byte) 2),
    /**
     * 未找到.
     */
    NOT_FOUND((byte) 3),
    /**
     * 创建失败.
     */
    UNCREATED((byte) 4),
    /**
     * 累加器调用失败.
     */
    UNACCUMULATE((byte) 5),

    //===================公式计算=================
    /**
     * 转换公式、自增编号失败.
     */
    ELEVATEFAILED((byte) 6),
    /**
     * 公式字段存在部分计算失败，使用默认值的情况.
     */
    HALF_SUCCESS((byte) 7),

    //===================字段校验===================
    /**
     * 必须的字段没有值.
     */
    FIELD_MUST((byte) 8),
    /**
     * 字段值超出字段定义上限.
     */
    FIELD_TOO_LONG((byte) 9),
    /**
     * 浮点字段值精度超过限制.
     */
    FIELD_HIGH_PRECISION((byte) 10);

    private byte symbol;

    ResultStatus(byte symbol) {
        this.symbol = symbol;
    }

    public byte getSymbol() {
        return symbol;
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
