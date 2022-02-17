package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

/**
 * 一个虚假的负载因子,主要是占位.
 * 并不会对负载计算产生任何影响.
 *
 * @author dongbin
 * @version 0.1 2022/2/15 16:20
 * @since 1.8
 */
public class FakeLoadLoadFactor implements LoadFactor {

    @Override
    public double now() {
        return 0.0D;
    }

    @Override
    public double weight() {
        return LoadFactor.MIN_WEIGHT;
    }
}
