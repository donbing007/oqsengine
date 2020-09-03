package com.xforceplus.ultraman.oasengine.common.id.test;

import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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


    @Test
    public void testGenSeqID() throws InterruptedException {
        SnowflakeLongIdGenerator generator = new SnowflakeLongIdGenerator(new StaticNodeIdGenerator(1));

        List<Long> list = new ArrayList<>();

//        for(int i = 0 ; i < 200000; i ++){
//            Long id = generator.next();
//        }
        IntStream.range(0 , 200000)
                .forEach(i -> {
                    Long id = generator.next();
                    list.add(id);

                });

        int size = list.stream()
                .distinct()
                .collect(Collectors.toList()).size();

//        list.stream().collect(Collectors.groupingBy(x -> x)).entrySet()
//                .stream().filter(x -> x.getValue().size() > 1).forEach(System.out::println);

        assertTrue(size == 200000);
    }

    @Test
    public void testLong() {
        Long x = 1L << 22;
        System.out.println(x);
    }
}
