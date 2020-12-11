package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DefaultTransactionAccumulator Tester.
 *
 * @author <Authors name>
 * @version 1.0 12/11/2020
 * @since <pre>Dec 11, 2020</pre>
 */
public class DefaultTransactionAccumulatorTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: accumulateBuild()
     */
    @Test
    public void testAccumulate() throws Exception {
        DefaultTransactionAccumulator accumulator = new DefaultTransactionAccumulator();
        accumulator.accumulateBuild();
        accumulator.accumulateBuild();
        accumulator.accumulateBuild();
        Assert.assertEquals(3, accumulator.getBuildTimes());

        accumulator.accumulateDelete();
        accumulator.accumulateDelete();
        Assert.assertEquals(2, accumulator.getDeleteTimes());

        accumulator.accumulateReplace();
        Assert.assertEquals(1, accumulator.getReplaceTimes());
    }


} 
