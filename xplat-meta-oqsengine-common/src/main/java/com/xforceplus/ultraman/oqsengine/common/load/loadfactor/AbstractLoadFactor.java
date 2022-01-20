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

    public AbstractLoadFactor(double weight) {
        this.weight = weight;
    }
}
