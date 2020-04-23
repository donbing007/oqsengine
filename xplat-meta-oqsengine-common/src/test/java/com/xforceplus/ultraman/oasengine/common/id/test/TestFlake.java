package com.xforceplus.ultraman.oasengine.common.id.test;

import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

/**
 * snowflake 测试.
 */
public class TestFlake {

    @Test
    public void testGenID() throws InterruptedException {
        SnowflakeLongIdGenerator generator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));

        CountDownLatch latch = new CountDownLatch(5000);

        CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<>();

        IntStream.range(0 , 5000)
                .mapToObj(i -> new Thread(() -> {
                    Long id = generator.next();
                    System.out.println();
                    list.add(id);
                    latch.countDown();
                }))
                .forEach(Thread::start);

       latch.await();

        assertTrue("there is no duplicated id",
                list.stream()
                        .distinct()
                        .collect(Collectors.toList()).size() == 5000);
    }
}
