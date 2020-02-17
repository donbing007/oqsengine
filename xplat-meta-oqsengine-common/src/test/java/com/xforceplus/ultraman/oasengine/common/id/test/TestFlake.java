package com.xforceplus.ultraman.oasengine.common.id.test;

import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeIdGenerator;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

public class TestFlake {

    @Test
    public void testGenID() throws InterruptedException {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);

        CountDownLatch latch = new CountDownLatch(5000);

        IntStream.range(0 , 5000)
                .mapToObj(i -> new Thread(() -> {
                    System.out.println(generator.next());
                    latch.countDown();
                }))
                .forEach(Thread::start);

       latch.await();
    }
}
