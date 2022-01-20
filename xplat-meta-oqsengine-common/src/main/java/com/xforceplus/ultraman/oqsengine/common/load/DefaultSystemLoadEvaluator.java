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

    @Resource
    private Collection<LoadFactor> loadFactors;

    public void setLoadFactors(
        Collection<LoadFactor> loadFactors) {
        this.loadFactors = loadFactors;
    }

    @Override
    public double evaluate() {
        if (loadFactors == null) {
            return MIN_VALUE;
        }

        return loadFactors.stream()
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
    }
}
