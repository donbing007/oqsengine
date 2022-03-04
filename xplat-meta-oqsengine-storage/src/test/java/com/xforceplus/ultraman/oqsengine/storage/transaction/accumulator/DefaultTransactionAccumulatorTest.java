package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DefaultTransactionAccumulator Tester.
 *
 * @author dongbin
 * @version 1.0 12/11/2020
 * @since <pre>Dec 11, 2020</pre>
 */
public class DefaultTransactionAccumulatorTest {

    /**
     * Method: accumulateBuild().
     */
    @Test
    public void testAccumulate() throws Exception {
        DefaultTransactionAccumulator accumulator =
            new DefaultTransactionAccumulator(1);
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(1).build());
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(2).build());
        accumulator.accumulateBuild(Entity.Builder.anEntity().withId(3).build());
        Assertions.assertEquals(3, accumulator.getBuildNumbers());

        accumulator.accumulateDelete(Entity.Builder.anEntity().withId(4).build());
        accumulator.accumulateDelete(Entity.Builder.anEntity().withId(5).build());
        Assertions.assertEquals(2, accumulator.getDeleteNumbers());

        accumulator.accumulateReplace(Entity.Builder.anEntity().withId(6).build());
        Assertions.assertEquals(1, accumulator.getReplaceNumbers());
    }

    /**
     * 重复操作.
     */
    @Test
    public void testRepeat() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1);
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build());
        acc.accumulateDelete(Entity.Builder.anEntity().withId(10).build());
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build());

        Assertions.assertEquals(2, acc.getReplaceNumbers());
        Assertions.assertEquals(1, acc.getUpdateIds().size());
        Assertions.assertEquals(10, acc.getUpdateIds().stream().findFirst().get().longValue());
    }

    @Test
    public void testNoBuild() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1);
        acc.accumulateBuild(Entity.Builder.anEntity().withId(100).build());
        Assertions.assertEquals(0, acc.getUpdateIds().size());

        Assertions.assertEquals(Collections.emptySet(), acc.getUpdateIds());
    }

    @Test
    public void testReset() throws Exception {
        DefaultTransactionAccumulator acc = new DefaultTransactionAccumulator(1);
        acc.reset();
        acc.accumulateReplace(Entity.Builder.anEntity().withId(10).build());
        acc.accumulateDelete(Entity.Builder.anEntity().withId(20).build());
        acc.accumulateReplace(Entity.Builder.anEntity().withId(30).build());

        Assertions.assertEquals(2, acc.getReplaceNumbers());
        Assertions.assertEquals(1, acc.getDeleteNumbers());
        long[] expectedIds = new long[] {10, 20, 30};
        long[] actualIds = acc.getUpdateIds().stream().mapToLong(i -> i.longValue()).toArray();
        Arrays.sort(expectedIds);
        Arrays.sort(actualIds);
        Assertions.assertArrayEquals(expectedIds, actualIds);
    }
} 
