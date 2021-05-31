package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/21/21 5:29 PM
 * @since 1.8
 */
public class TestSegmentId {

    private ApplicationContext applicationContext;

    private ExecutorService executorService;


    @Before
    public void before() {
        executorService = Executors.newFixedThreadPool(30);

        PatternParserManager manager = new PatternParserManager();
        NumberPatternParser parser = new NumberPatternParser();
        DatePatternParser datePattenParser = new DatePatternParser();
        manager.registVariableParser(parser);
        manager.registVariableParser(datePattenParser);
        applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(PatternParserManager.class)).thenReturn(manager);
        ReflectionTestUtils.setField(PatternParserUtil.class,"applicationContext",applicationContext);

    }

    @Test
    public void testNextId() throws InterruptedException, IOException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        SegmentId segmentId  = new SegmentId();
        segmentId.setMaxId(1000);
        segmentId.setLoadingId(300);
        segmentId.setPattern("{yyyy}-{MM}:{0000}");
        PatternValue value = new PatternValue(0,"1");
        segmentId.setCurrentId(value);
        for(int j=0;j<20;j++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("prepare for execute");
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for(int i=0;i<100;i++) {
                       System.out.println(Thread.currentThread().getName()+segmentId.nextId().getId());
                    }
                }
            });
        }
        System.out.println("start execute");
        countDownLatch.countDown();
        Thread.sleep(2000);
        LocalDateTime time = LocalDateTime.now();
        String ext = time.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Assert.assertEquals(ext+":2001",segmentId.nextId().getId());
    }
}
