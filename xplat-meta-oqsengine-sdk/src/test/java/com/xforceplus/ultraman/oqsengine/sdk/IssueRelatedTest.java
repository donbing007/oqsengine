package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.metadata.grpc.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.configuration.TestApplicationContextInitializer;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@EnableAsync
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
public class IssueRelatedTest extends ContextWareBaseTest {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private EntityService entityService;


    private ModuleUpResult manyToOneNew() {
        return ModuleUpResult
                .newBuilder()
                .setVersion("0.0.1")
                .setId(111111111111L)
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("1")
                        .setCode("main")
                        .addRelations(Relation.newBuilder()
                                .setId("10001")
                                .setRelationType("ManyToOne")
                                .setRelName("rel1")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .addRelations(Relation.newBuilder()
                                .setId("10002")
                                .setRelationType("ManyToOne")
                                .setRelName("rel2")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("id")
                                .setSearchable("1")
                                .setId("1000001")
                                .setFieldType("Long")
                                .setIdentifier("1")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field1")
                                .setSearchable("1")
                                .setId("1003")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field2")
                                .setSearchable("0")
                                .setId("1004")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field3")
                                .setSearchable("0")
                                .setId("1005")
                                .build())
                        .build())
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("2")
                        .setCode("rel1")
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field21")
                                .setSearchable("1")
                                .setFieldType("String")
                                .setId("2001")
                                .build())
                        .build())
                .build();
    }

    @Test
    public void testCasConflict() throws InterruptedException {
        metadataRepository.save(manyToOneNew(), "1", "1");


        int concurrent = 10;

        EntityClass entityClass = entityService.load("1").get();
        Map<String, Object> maps = new HashMap<>();
        maps.put("field1", "123");
        Long id = entityService.create(entityClass, maps).get();

        CountDownLatch latch = new CountDownLatch(concurrent);


        IntStream.range(0, concurrent).mapToObj(x -> new Thread(() -> {

            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("field1", "123-" + Thread.currentThread().getName());
            System.out.println(entityService.retryExecute("a", () ->entityService.updateById(entityClass, id, updateBody)));
            latch.countDown();
        })).forEach(Thread::start);

        latch.await();

        System.out.println(entityService.findOne(entityClass, id));

        entityService.deleteOne(entityClass, id);
    }
}
