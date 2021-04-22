package com.xforceplus.ultraman.oqsengine.changelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogConfiguration;
import com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ValueWrapper;
import com.xforceplus.ultraman.oqsengine.changelog.entity.ChangelogStatefulEntity;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.impl.DefaultChangelogGateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.impl.RedisChangelogHandler;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.changelog.config.ChangelogExample.*;

/**
 * redis test
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = ChangelogConfiguration.class)
//public class RedisTest {
//
//    @Autowired
//    private ChangelogExample example;
//
//    @Resource
//    private List<ChangelogCommandHandler> changelogCommandHandlerList;
//
//    @Resource
//    private List<ChangelogEventHandler> changelogEventHandlerList;
//
//    @Autowired
//    private ReplayService replayService;
//
//    /**
//     * TODO T is due to new Structure
//     */
//    @Test
//    public void multiConsumerTest() throws InterruptedException {
//        ObjectMapper mapper = new ObjectMapper();
//
//        String redisIp = System.getProperty("REDIS_HOST");
//        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
//
//        RedisClient redisClient1 = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
//        DefaultChangelogGateway gateway1 = new DefaultChangelogGateway();
//        gateway1.setChangelogCommandHandlerList(changelogCommandHandlerList);
//        gateway1.setChangelogEventHandlerList(changelogEventHandlerList);
//        RedisChangelogHandler<String> handler1 = new RedisChangelogHandler("A", "test", redisClient1, gateway1, mapper);
//
//        RedisClient redisClient2 = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
//        DefaultChangelogGateway gateway2 = new DefaultChangelogGateway();
//        gateway2.setChangelogCommandHandlerList(changelogCommandHandlerList);
//        gateway2.setChangelogEventHandlerList(changelogEventHandlerList);
//        RedisChangelogHandler<String> handler2 = new RedisChangelogHandler("B", "test", redisClient2, gateway2, mapper);
//
//        RedisClient redisClient3 = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
//        DefaultChangelogGateway gateway3 = new DefaultChangelogGateway();
//        gateway3.setChangelogCommandHandlerList(changelogCommandHandlerList);
//        gateway3.setChangelogEventHandlerList(changelogEventHandlerList);
//        RedisChangelogHandler<String> handler3 = new RedisChangelogHandler("C", "test", redisClient3, gateway3, mapper);
//
//        handler1.prepareConsumer();
//        handler2.prepareConsumer();
//        handler3.prepareConsumer();
//
//        //send
//
//        new Thread(() -> {
//            while (true) {
//                TransactionalChangelogEvent transactionalChangelogEvent = new TransactionalChangelogEvent();
//
//
//                Changelog changelog = example.genAChangelog();
//
//                transactionalChangelogEvent.setCommitId(changelog.getVersion());
//
//                transactionalChangelogEvent.setChangedEventList(Arrays.asList(genChangedEventA(changelog)));
//                handler1.handle(transactionalChangelogEvent);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//        new Thread(() -> {
//
//            while(true){
//                Optional<ChangelogStatefulEntity> changelogStatefulEntity = replayService.replayStatefulEntity(A_Class, A_ObjId);
//                ChangelogStatefulEntity statefulEntity = changelogStatefulEntity.get();
//                System.out.println(statefulEntity.getState().getVersion());
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();
//
//        Thread.sleep(Integer.MAX_VALUE);
//    }
//
//    private ChangedEvent genChangedEventA(Changelog changelog){
//        ChangedEvent changedEvent = new ChangedEvent();
//        changedEvent.setCommitId(changelog.getVersion());
//        changedEvent.setEntityClassId(changelog.getEntityClass());
//        changedEvent.setOperationType(OperationType.UPDATE);
//        changedEvent.setId(changelog.getId());
//        changedEvent.setTimestamp(changelog.getCreateTime());
//        Map<Long, ValueWrapper> mapValue = new HashMap<>();
//        Optional<IEntityField> field1 = example.A.field(A_Field1);
//        mapValue.put(A_Field1, new ValueWrapper("abc", FieldType.STRING, field1.get().id()));
//        changedEvent.setValueMap(mapValue);
//        changedEvent.setUsername("luye");
//        changedEvent.setComment("Test Outer");
//        return changedEvent;
//    }
//}
