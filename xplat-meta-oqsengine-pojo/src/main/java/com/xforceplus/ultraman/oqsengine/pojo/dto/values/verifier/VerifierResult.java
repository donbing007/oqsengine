package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

/**
 * 校验结果.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 14:14
 * @since 1.8
 */
public enum VerifierResult {

    /**
     * 未知.
     */
    UNKNOWN(0),
    /**
     * 成功,表示校验通过.
     */
    OK(1),
    /**
     * 表示超出最大长度.
     */
    TOO_LONG(2),
    /**
     * 精度过高.
     */
    HIGH_PRECISION(3),
    /**
     * 表示字段为必须含有值.
     */
    REQUIRED(4);


    private int symbol;

    private VerifierResult(int symbol) {
        this.symbol = symbol;
    }

    public int getSymbol() {
        return symbol;
    }

    /**
     * 根据字面量获得实例.
     *
     * @param symbol 字面量.
     * @return 实例.
     */
    public static VerifierResult getInstance(int symbol) {
        for (VerifierResult r : VerifierResult.values()) {
            if (r.getSymbol() == symbol) {
                return r;
            }
        }

        return UNKNOWN;
    }
}
