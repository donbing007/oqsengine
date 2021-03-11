package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogConfiguration;
import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.A_Class;
import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.A_ObjId;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChangelogConfiguration.class)
public class ReplayServiceTest {

    @Autowired
    private ReplayService replayService;

    @Autowired
    private ChangelogService changelogService;

    @Autowired
    private ChangelogExample example;

    @Test
    public void testReplayRelation(){
        replayService.replayAggDomain(1,1,1);
    }

    @Test
    public void testChangelogTestList(){

        long start = System.currentTimeMillis();
        System.out.println("Current" + start);
        List<ChangeVersion> changeLog = changelogService.getChangeLog(A_ObjId, A_Class);
        System.out.println("During" + (System.currentTimeMillis() - start));

        //changeLog.forEach(System.out::println);

        long load = System.currentTimeMillis();
        EntityAggDomain entityAggDomain_10003 = changelogService.replayEntity(A_Class, A_ObjId, 20000);
        System.out.println("load 1 During" + (System.currentTimeMillis() - load));

        long load2 = System.currentTimeMillis();
        EntityAggDomain entityAggDomain_10008 = changelogService.replayEntity(A_Class, A_ObjId, 40008);
        System.out.println("load 2 During" + (System.currentTimeMillis() - load2));

        long load3 = System.currentTimeMillis();
        EntityAggDomain entityAggDomain_10009 = changelogService.replayEntity(A_Class, A_ObjId, 90009);
        System.out.println("load 3 During" + (System.currentTimeMillis() - load3));

        System.out.println("10003 -> " + entityAggDomain_10003);
        System.out.println("10008 -> " + entityAggDomain_10008);
        System.out.println("10009 -> " + entityAggDomain_10009);
    }

    @Test
    public void testTime(){
        AtomicLong atomicLong = new AtomicLong(0);
        AtomicLong reduce = example.changelogs.stream().reduce(atomicLong, (a, b) -> {
            a.incrementAndGet();
            return a;
        }, (a, b) -> {
            a.getAndAdd(b.get());
            return a;
        });
        System.out.println(reduce.get());
    }
}
