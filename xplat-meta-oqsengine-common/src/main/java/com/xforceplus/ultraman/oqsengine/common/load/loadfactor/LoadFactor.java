package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

/**
 * 负载因子.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 13:58
 * @since 1.8
 */
public interface LoadFactor {

    /**
     * 最大权重.
     */
    static final double MAX_WEIGHT = 1.0D;

    /**
     * 最小权重.
     */
    static final double MIN_WEIGHT = 0.0D;

    /**
     * 负载因子的当前值.不能小于0.
     * 表示此负载当前的占用百分比.
     * 最大为100.0D,最小为0.0D
     *
     * @return 当前值.
     */
    public double now();

    /**
     * 权重.
     * 体现当前因子对于最终负载的影响.
     * 此返回值小于等于0的将被忽略.
     * 大于1将认为是1.
     *
     * @return 权重.
     */
    public default double weight() {
        return MAX_WEIGHT;
    }

}
