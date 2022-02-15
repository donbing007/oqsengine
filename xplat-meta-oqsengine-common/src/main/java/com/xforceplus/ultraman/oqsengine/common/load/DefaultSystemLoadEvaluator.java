package com.xforceplus.ultraman.oqsengine.common.load;

import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.LoadFactor;
import java.util.Collection;
import javax.annotation.Resource;

/**
 * 默认的系统负载评价器.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 14:33
 * @since 1.8
 */
public class DefaultSystemLoadEvaluator implements SystemLoadEvaluator {

    private static double MAX_VALUE = 100.0D;
    private static double MIN_VALUE = 0.0D;
    // 所有最大负载.
    private double max;

    @Resource
    private Collection<LoadFactor> loadFactors;

    /**
     * 设置负载因子.
     */
    public void setLoadFactors(Collection<LoadFactor> loadFactors) {
        this.loadFactors = loadFactors;

        max = loadFactors.stream().mapToDouble(loadFactor -> MAX_VALUE * loadFactor.weight()).sum();
    }

    @Override
    public double evaluate() {
        if (loadFactors == null) {
            return (int) MIN_VALUE;
        }

        double value = loadFactors.stream()
            .filter(
                loadFactor ->
                    loadFactor.weight() >= LoadFactor.MIN_WEIGHT && loadFactor.weight() <= LoadFactor.MAX_WEIGHT)
            .mapToDouble(loadFactor -> {
                double load = loadFactor.now();
                if (load < MIN_VALUE) {
                    return MIN_VALUE;
                } else if (load > MAX_VALUE) {
                    return MAX_VALUE * loadFactor.weight();
                } else {
                    return loadFactor.now() * loadFactor.weight();
                }
            })
            .sum();

        double load = (value / max) * 100D;
        if (Double.isNaN(load)) {
            return MIN_VALUE;
        } else if (Double.isInfinite(load)) {
            return MIN_VALUE;
        } else {
            return load;
        }
    }
}
