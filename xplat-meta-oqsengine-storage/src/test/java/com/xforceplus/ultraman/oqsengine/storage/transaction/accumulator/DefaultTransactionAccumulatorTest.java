package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;

import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.DoNothingCacheEventHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        DefaultTransactionAccumulator accumulator = new DefaultTransactionAccumulator(1, new DoNothingCacheEventHandler());
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(1).build());
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(2).build());
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(3).build());
        Assert.assertEquals(3, accumulator.getBuildNumbers());

        accumulator.accumulateDelete(Entity.Builder.anEntity().withId(4).build());
        accumulator.accumulateDelete(Entity.Builder.anEntity().withId(5).build());
        Assert.assertEquals(2, accumulator.getDeleteNumbers());

        accumulator.accumulateReplace(Entity.Builder.anEntity().withId(6).build(), Entity.Builder.anEntity().withId(6).build());
        Assert.assertEquals(1, accumulator.getReplaceNumbers());
    }

    /**
     * 重复操作.
     * @throws Exception
     */
    @Test
    public void testRepeat() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1, new DoNothingCacheEventHandler());
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build(), Entity.Builder.anEntity().withId(10).build());
        acc.accumulateDelete(Entity.Builder.anEntity().withId(10).build());
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build(), Entity.Builder.anEntity().withId(10).build());

        Assert.assertEquals(2, acc.getReplaceNumbers());
        Assert.assertEquals(1, acc.getUpdateIds().size());
        Assert.assertEquals(10, acc.getUpdateIds().stream().findFirst().get().longValue());
    }

    @Test
    public void testNoBuild() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1, new DoNothingCacheEventHandler());
        acc.accumulateBuild(Entity.Builder.anEntity().withId(100).build());
        Assert.assertEquals(0, acc.getUpdateIds().size());

        Assert.assertEquals(Collections.emptySet(), acc.getUpdateIds());
    }

    @Test
    public void testReset() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1, new DoNothingCacheEventHandler());
        acc.reset();
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build(), Entity.Builder.anEntity().withId(10).build());
        acc.accumulateDelete(Entity.Builder.anEntity().withId(20).build());
        acc.accumulateReplace(Entity.Builder.anEntity().withId(30).build(), Entity.Builder.anEntity().withId(30).build());

        Assert.assertEquals(2, acc.getReplaceNumbers());
        Assert.assertEquals(1, acc.getDeleteNumbers());
        long[] expectedIds = new long[] {10, 20, 30};
        long[] actualIds = acc.getUpdateIds().stream().mapToLong(i -> i.longValue()).toArray();
        Arrays.sort(expectedIds);
        Arrays.sort(actualIds);
        Assert.assertArrayEquals(expectedIds, actualIds);
    }

} 
