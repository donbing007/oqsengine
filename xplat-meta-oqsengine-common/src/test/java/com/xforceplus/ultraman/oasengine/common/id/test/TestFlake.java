package com.xforceplus.ultraman.oasengine.common.id.test;

import static org.junit.Assert.assertTrue;

import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * snowflake 测试.
 */
public class TestFlake {

    @Test
    public void testGenID() throws InterruptedException {
        SnowflakeLongIdGenerator generator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));

        CountDownLatch latch = new CountDownLatch(500);

        CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<>();

        IntStream.range(0, 500)
            .mapToObj(i -> new Thread(() -> {
                Long id = generator.next();
                list.add(id);
                latch.countDown();
            }))
            .forEach(Thread::start);

        latch.await();

        assertTrue("there is no duplicated id",
            list.stream()
                .distinct()
                .collect(Collectors.toList()).size() == 500);
    }


    @Test
    public void testGenSeqID() throws InterruptedException {
        SnowflakeLongIdGenerator generator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));

        List<Long> list = new ArrayList<>();

        IntStream.range(0, 2000)
            .forEach(i -> {
                Long id = generator.next();
                list.add(id);

            });

        int size = list.stream()
            .distinct()
            .collect(Collectors.toList()).size();


        assertTrue(size == 2000);
    }
}
