package com.xforceplus.ultraman.oqsengine.changelog;

import com.xforceplus.ultraman.oqsengine.changelog.command.AddChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogConfiguration;
import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample;
import com.xforceplus.ultraman.oqsengine.changelog.domain.*;
import com.xforceplus.ultraman.oqsengine.changelog.entity.ChangelogStatefulEntity;
import com.xforceplus.ultraman.oqsengine.changelog.event.ChangelogEvent;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.*;

@ExtendWith({RedisContainer.class, SpringExtension.class})
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

    @Test
    public void testReplayEntity(){
        long startTime = System.currentTimeMillis();
        Optional<ChangelogStatefulEntity> changelogStatefulEntity = replayService.replayStatefulEntity(1, 1000001L);
        System.out.println(System.currentTimeMillis() - startTime);
        ChangelogStatefulEntity statefulEntity = changelogStatefulEntity.get();
        System.out.println(statefulEntity);


        Changelog changelog = example.genAChangelog();

        ChangedEvent changedEvent = genChangedEventA(changelog);

        List<ChangelogEvent> receive = statefulEntity.receive(new AddChangelog(1000001L, 1, changedEvent), new HashMap<>());

        receive.forEach(System.out::println);
        System.out.println(statefulEntity);


        Changelog changelogRel = example.addRelABChangelog();
        ChangedEvent changedEvent1 = genChangedEventAB(changelogRel);

        List<ChangelogEvent> receive2 = statefulEntity.receive(new AddChangelog(1000001L, 1, changedEvent1), new HashMap<>());
        System.out.println(statefulEntity);

        receive2.forEach(System.out::println);
    }

    //TODO
    private ChangedEvent genChangedEventA(Changelog changelog){
        ChangedEvent changedEvent = new ChangedEvent();
        changedEvent.setCommitId(changelog.getVersion());
        changedEvent.setEntityClassId(changelog.getEntityClass());
        changedEvent.setOperationType(OperationType.UPDATE);
        changedEvent.setId(changelog.getId());
        changedEvent.setTimestamp(changelog.getCreateTime());
        Map<Long, ValueWrapper> mapValue = new HashMap<>();
//        Optional<IEntityField> field = example.A.field(A_B_OTO);
        Optional<IEntityField> field1 = example.A.field(A_Field1);
//        mapValue.put(A_B_OTO, new LongValue(field.get(), 22222L));
        mapValue.put(A_Field1, new ValueWrapper("12312312", field1.get().type(), field1.get().id()));
        changedEvent.setValueMap(mapValue);
        changedEvent.setUsername("luye");
        changedEvent.setComment("Test Outer");
        return changedEvent;
    }

    private ChangedEvent genChangedEventAB(Changelog changelog){
        ChangedEvent changedEvent = new ChangedEvent();
        changedEvent.setCommitId(changelog.getVersion());
        changedEvent.setEntityClassId(changelog.getEntityClass());
        changedEvent.setOperationType(OperationType.UPDATE);
        changedEvent.setId(changelog.getId());
        changedEvent.setTimestamp(changelog.getCreateTime());
        Map<Long, ValueWrapper> mapValue = new HashMap<>();
//        Optional<IEntityField> field = example.A.field(A_B_OTO);
        Optional<IEntityField> field1 = example.A.field(A_Field1);
//        mapValue.put(A_B_OTO, new LongValue(field.get(), 22222L));
        mapValue.put(example.A_B_OTO, new ValueWrapper("8292032", FieldType.LONG, field1.get().id()));
        changedEvent.setValueMap(mapValue);
        changedEvent.setUsername("luye");
        changedEvent.setComment("Test Reference");
        return changedEvent;
    }
}
