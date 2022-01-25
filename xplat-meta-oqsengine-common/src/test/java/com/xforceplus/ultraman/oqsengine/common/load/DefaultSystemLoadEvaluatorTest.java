package com.xforceplus.ultraman.oqsengine.common.load;

import com.xforceplus.ultraman.oqsengine.common.load.loadfactor.LoadFactor;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 系统负载评价测试.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 13:58
 * @since 1.8
 */
public class DefaultSystemLoadEvaluatorTest {

    /**
     * 非异常情况.
     */
    @Test
    public void testEvaluate() throws Exception {

        Collection<LoadFactor> loadFactors = Arrays.asList(
            new MockLoadFactory(100D, 0.1D),
            new MockLoadFactory(20D, 0.9D)
        );

        DefaultSystemLoadEvaluator evaluator = new DefaultSystemLoadEvaluator();
        evaluator.setLoadFactors(loadFactors);

        double max = loadFactors.stream().mapToDouble(l -> 100D * l.weight()).sum();

        double expectedLoad = ((100D * 0.1D + 20D * 0.9D) /  max) * 100D;
        Assertions.assertEquals(expectedLoad, evaluator.evaluate());
    }

    /**
     * 错误的权重.
     */
    @Test
    public void testErrorWeightEvaluate() throws Exception {

        Collection<LoadFactor> loadFactors = Arrays.asList(
            new MockLoadFactory(100D, 0.1D),
            new MockLoadFactory(20D, 0.9D),
            new MockLoadFactory(30D, 1.2D)
        );

        DefaultSystemLoadEvaluator evaluator = new DefaultSystemLoadEvaluator();
        evaluator.setLoadFactors(loadFactors);

        double max = loadFactors.stream().mapToDouble(l -> 100D * l.weight()).sum();
        double expectedLoad = ((100D * 0.1D + 20D * 0.9D) / max) * 100D;
        Assertions.assertEquals(expectedLoad, evaluator.evaluate());


        loadFactors = Arrays.asList(
            new MockLoadFactory(100D, 1.1D),
            new MockLoadFactory(20D, 3.9D),
            new MockLoadFactory(30D, 1.2D)
        );

        evaluator = new DefaultSystemLoadEvaluator();
        evaluator.setLoadFactors(loadFactors);

        expectedLoad = 0.0D;
        Assertions.assertEquals(expectedLoad, evaluator.evaluate());
    }

    /**
     * 错误的权重.
     */
    @Test
    public void testErrorLoadEvaluate() throws Exception {
        Collection<LoadFactor> loadFactors = Arrays.asList(
            new MockLoadFactory(101D, 0.1D),
            new MockLoadFactory(20D, 0.9D),
            new MockLoadFactory(30D, 0.2D)
        );

        DefaultSystemLoadEvaluator evaluator = new DefaultSystemLoadEvaluator();
        evaluator.setLoadFactors(loadFactors);

        double max = loadFactors.stream().mapToDouble(l -> 100D * l.weight()).sum();
        double expectedLoad = ((100D * 0.1D + 20D * 0.9D + 30D * 0.2D) / max) * 100D;
        Assertions.assertEquals(expectedLoad, evaluator.evaluate());
    }

    static class MockLoadFactory implements LoadFactor {

        private double load;
        private double weight;

        public MockLoadFactory(double load, double weight) {
            this.load = load;
            this.weight = weight;
        }

        @Override
        public double now() {
            return this.load;
        }

        @Override
        public double weight() {
            return this.weight;
        }
    }
}