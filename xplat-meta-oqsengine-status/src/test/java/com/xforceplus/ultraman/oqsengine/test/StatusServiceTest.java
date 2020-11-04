package com.xforceplus.ultraman.oqsengine.test;


import com.xforceplus.ultraman.oqsengine.status.StatusMetrics;
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
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        tableCleaner = new TableCleaner(redisClient, 1L, 1L, 1000L);
    }

    @After
    public void shutDown(){
        redisClient.connect().sync().del("testTable");
        redisClient.connect().sync().del("testLocalTable");
        redisClient.connect().sync().del("testKey");
        redisClient.shutdown();
    }

    @Test
    public void test(){
        System.out.println(localStatusService.getCurrentStatusMetrics());
    }

    @Test
    public void testTableCleaner() throws InterruptedException {

        localStatusService.saveCommitId(1L);
        localStatusService.saveCommitId(2L);
        StatusMetrics currentStatusMetrics = localStatusService.getCurrentStatusMetrics();
        assertEquals(2L , (long)currentStatusMetrics.getSize());
        Thread.sleep(1000);
        tableCleaner.clean();
        Thread.sleep(1000);
        currentStatusMetrics = localStatusService.getCurrentStatusMetrics();
        assertEquals(0, (long)currentStatusMetrics.getSize());

        localStatusService.saveCommitId(1L);
        localStatusService.saveCommitId(2L);
        localStatusService.saveCommitId(3L);
        localStatusService.saveCommitId(4L);

        tableCleaner.clean();
        currentStatusMetrics = localStatusService.getCurrentStatusMetrics();

        assertEquals(4, (long)currentStatusMetrics.getSize());

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

        assertTrue("there is no duplicated id",
                list.stream()
                        .distinct()
                        .collect(Collectors.toList()).size() == 50000);


        redisClient.connect().sync().del("tempKey");
    }

    @Test
    public void insertSequenceLocal() throws InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        Instant instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointA = instant.toEpochMilli();
        localStatusService.saveCommitIdWithLocalTime(1L, pointA);
        StatusMetrics currentStatusMetrics = localStatusService.getCurrentStatusMetrics();

        assertEquals(1L , (long)currentStatusMetrics.getSize());
        assertEquals(Long.toString(pointA), currentStatusMetrics.getLowBound());
        assertEquals(Long.toString(pointA), currentStatusMetrics.getUpBound());

        System.out.println(pointA);
        Thread.sleep(500);
        now = LocalDateTime.now();
        instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointB = instant.toEpochMilli();
        localStatusService.saveCommitIdWithLocalTime(2L, pointB);
        Thread.sleep(500);
        System.out.println(pointB);
        //time elapsed 1000

        now = LocalDateTime.now();
        instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointC = instant.toEpochMilli();
        System.out.println(pointC);

        assertEquals(1L , (long)localStatusService.getCurrentCommitLowBoundWithLocalTime(pointC - 1500, pointC));


        now = LocalDateTime.now();
        instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointD = instant.toEpochMilli();
        System.out.println(pointD);

        localStatusService.saveCommitIdWithLocalTime(3L, pointD);
        Thread.sleep(500);

        now = LocalDateTime.now();
        instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointE = instant.toEpochMilli();
        localStatusService.saveCommitIdWithLocalTime(4L, pointE);
        Thread.sleep(500);

        now = LocalDateTime.now();
        instant = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
        long pointF = instant.toEpochMilli();

        assertEquals(3L , (long)localStatusService.getCurrentCommitLowBoundWithLocalTime(pointF - 1500, pointF));
    }

    @Test
    public void insertSequenceRemote() throws InterruptedException {

        remoteStatusService.saveCommitId(1L);
        Thread.sleep(500);
        remoteStatusService.saveCommitId(2L);
        Thread.sleep(500);
        assertEquals(1L , (long)remoteStatusService.getCurrentCommitLowBound(1500L));
        remoteStatusService.saveCommitId(3L);
        Thread.sleep(500);
        remoteStatusService.saveCommitId(4L);
        Thread.sleep(500);
        assertEquals(3L , (long)remoteStatusService.getCurrentCommitLowBound(1500L));
    }

    @Test
    public void invalidateTest() throws InterruptedException {
        remoteStatusService.saveCommitId(1L);
        remoteStatusService.saveCommitId(2L);
        remoteStatusService.saveCommitId(3L);

        assertTrue(remoteStatusService.getCurrentStatusMetrics().getTransIds().contains(2L));

        remoteStatusService.invalidateIds(Arrays.asList(1L, 2L));

        Thread.sleep(2000);

        assertTrue(!remoteStatusService.getCurrentStatusMetrics().getTransIds().contains(2L));
        assertTrue(!remoteStatusService.getCurrentStatusMetrics().getTransIds().contains(1L));
    }




    @Test
    public void insertAndQueryWithLocalTime() {

        CountDownLatch latch = new CountDownLatch(1500);

        int readerNum = 100;

        CopyOnWriteArrayList<Long> concurrentList = new CopyOnWriteArrayList<>();

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
                    //System.out.println("start:" + start + ", end:" + (current + 1));
                    Long currentCommitLowBound = localStatusService.getCurrentCommitLowBoundWithLocalTime(start, current + 1000);
                   // System.out.println("current:" + System.nanoTime() + ":" + currentCommitLowBound);
                    //System.out.println(localStatusService.getCurrentStatusMetrics());
                    concurrentList.add(currentCommitLowBound);
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

        StatusMetrics currentStatusMetrics = localStatusService.getCurrentStatusMetrics();
        //assert
        assertEquals(500L, (long) currentStatusMetrics.getSize());
        concurrentList.stream().forEach(System.out::println);
    }

    @Test
    public void insertAndQueryWithRemoteTime() {

        CountDownLatch latch = new CountDownLatch(1500);

        int readerNum = 100;

        CopyOnWriteArrayList<Long> concurrentList = new CopyOnWriteArrayList<>();

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
                    //System.out.println("current:" + System.nanoTime() + ":" + currentCommitLowBound);
                    concurrentList.add(currentCommitLowBound);
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

        StatusMetrics currentStatusMetrics = remoteStatusService.getCurrentStatusMetrics();
        assertEquals(500L, (long) currentStatusMetrics.getSize());
        concurrentList.stream().forEach(System.out::println);
    }
}
