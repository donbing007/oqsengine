package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogConfiguration;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.domain.EntityAggDomain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.A_Class;
import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.A_ObjId;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChangelogConfiguration.class)
public class ReplayServiceTest {

    @Autowired
    private ReplayService replayService;

    @Autowired
    private ChangelogService changelogService;

    @Test
    public void testReplayRelation(){
        replayService.replayDomain(1,1,1);
    }

    @Test
    public void testChangelogTestList(){

        List<ChangeVersion> changeLog = changelogService.getChangeLog(A_ObjId, A_Class);

        changeLog.forEach(System.out::println);

        EntityAggDomain entityAggDomain_10003 = changelogService.replayEntity(A_Class, A_ObjId, 10003);
        EntityAggDomain entityAggDomain_10008 = changelogService.replayEntity(A_Class, A_ObjId, 10008);
        EntityAggDomain entityAggDomain_10009 = changelogService.replayEntity(A_Class, A_ObjId, 10009);

        System.out.println("10003 -> " + entityAggDomain_10003);
        System.out.println("10008 -> " + entityAggDomain_10008);
        System.out.println("10009 -> " + entityAggDomain_10009);
    }
}
