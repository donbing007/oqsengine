package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

/**
 * 抽像负载因子,定义了权重.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 15:29
 * @since 1.8
 */
public abstract class AbstractLoadFactor implements LoadFactor {

    private double weight;

    /**
     * 构造新的负载因子实例.
     *
     * @param weight 权重.
     */
    public AbstractLoadFactor(double weight) {
        if (weight > 1D) {
            this.weight = 1D;
        } else {
            this.weight = weight;
        }
    }
}
