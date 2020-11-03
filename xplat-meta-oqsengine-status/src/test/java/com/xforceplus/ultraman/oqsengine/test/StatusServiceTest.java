package com.xforceplus.ultraman.oqsengine.test;


import com.xforceplus.ultraman.oqsengine.status.StatusService;
import com.xforceplus.ultraman.oqsengine.status.StatusServiceImpl;
import com.xforceplus.ultraman.oqsengine.status.id.RedisIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.table.TableCleaner;
import com.xforceplus.ultraman.oqsengine.status.table.TimeTable;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatusServiceTest {

    StatusService remoteStatusService;
    StatusService localStatusService;
    RedisClient redisClient;
    TableCleaner tableCleaner;

    private RedisIdGenerator redisIdGenerator;

    @Before
    public void setUp(){

        redisClient = RedisClient.create(RedisURI.Builder.redis("localhost", 6379).build());

        redisIdGenerator = new RedisIdGenerator(redisClient, "testKey");
        TimeTable remoteTimeTable = new TimeTable(redisClient, "testTable");
        TimeTable localTimeTable = new TimeTable(redisClient, "testLocalTable");
        remoteStatusService = new StatusServiceImpl(redisIdGenerator, remoteTimeTable);
        localStatusService = new StatusServiceImpl(redisIdGenerator, localTimeTable);
        tableCleaner = new TableCleaner(redisClient, 1L, 1L);
    }

    @After
    public void shutDown(){
        redisClient.connect().sync().del("testTable");
        redisClient.connect().sync().del("testLocalTable");
        redisClient.connect().sync().del("testKey");
        redisClient.shutdown();
    }

    @Test
    public void testTableCleaner(){
        tableCleaner.clean();
    }


    @Test
    public void testGenID() throws InterruptedException {

        RedisIdGenerator tempIdGenerator = new RedisIdGenerator(redisClient, "tempKey");

        CountDownLatch latch = new CountDownLatch(50000);

        CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<>();

        IntStream.range(0 , 50000)
                .mapToObj(i -> new Thread(() -> {
                    Long id = tempIdGenerator.next();
                    list.add(id);
                    latch.countDown();
                }))
                .forEach(Thread::start);

        latch.await();

        Assert.assertTrue("there is no duplicated id",
                list.stream()
                        .distinct()
                        .collect(Collectors.toList()).size() == 50000);


        redisClient.connect().sync().del("tempKey");
    }


    @Test
    public void insertAndQueryWithLocalTime() {

        CountDownLatch latch = new CountDownLatch(1500);

        int readerNum = 100;

        //start up a query task

        while(readerNum --> 0){
            new Thread(() -> {

                Random random = new Random();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int readTask = 10;

                while(readTask --> 0){
                    LocalDateTime now = LocalDateTime.now();
                    Instant instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
                    long current = instant.toEpochMilli();
                    long start = current - 2000;
                    System.out.println("start:" + start + ", end:" + (current + 1));
                    Long currentCommitLowBound = localStatusService.getCurrentCommitLowBoundWithLocalTime(start, current + 1000);
                    System.out.println("current:" + System.nanoTime() + ":" + currentCommitLowBound);
                    //System.out.println(localStatusService.getCurrentStatusMetrics());
                    latch.countDown();
                    try {
                        Thread.sleep(random.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }).start();
        }


        int num = 50;

        while(num --> 0) {

            //mock task worker
            new Thread(() -> {
                int taskNum = 10;

                Random random = new Random();

                while(taskNum --> 0) {
                    //get id
                    Long commitId = localStatusService.getCommitId();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //get
                    LocalDateTime now = LocalDateTime.now();
                    Instant instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
                    long current = instant.toEpochMilli();
                    localStatusService.saveCommitIdWithLocalTime(commitId, current);
                    latch.countDown();
                    try {
                        Thread.sleep(random.nextInt(500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void insertAndQueryWithRemoteTime() {

        CountDownLatch latch = new CountDownLatch(1500);

        int readerNum = 100;

        //start up a query task

        while(readerNum --> 0){
            new Thread(() -> {

                Random random = new Random();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int readTask = 10;

                while(readTask --> 0){
                    Long currentCommitLowBound = remoteStatusService.getCurrentCommitLowBound(2000L);
                    System.out.println("current:" + System.nanoTime() + ":" + currentCommitLowBound);
                    //System.out.println(remoteStatusService.getCurrentStatusMetrics());
                    latch.countDown();
                    try {
                        Thread.sleep(random.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }).start();
        }


        int num = 50;

        while(num --> 0) {

            //mock task worker
            new Thread(() -> {
                int taskNum = 10;

                Random random = new Random();

                while(taskNum --> 0) {
                    //get id
                    Long commitId = remoteStatusService.getCommitId();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //get
                    remoteStatusService.saveCommitId(commitId);
                    latch.countDown();
                    try {
                        Thread.sleep(random.nextInt(500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
