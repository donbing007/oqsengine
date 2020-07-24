package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.metadata.grpc.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.configuration.TestApplicationContextInitializer;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.PlainEntityService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;
import static org.junit.Assert.assertEquals;

@EnableAsync
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
public class IssueRelatedTest extends ContextWareBaseTest {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private EntityService entityService;

    @Autowired
    private PlainEntityService plainEntityService;

    @Autowired
    private ContextService contextService;

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
                                .setCode("sortf")
                                .setSearchable("1")
                                .setId("1000002")
                                .setFieldType("Long")
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
                                .setCode("decimalField")
                                .setSearchable("1")
                                .setId("100466")
                                .setFieldType(FieldType.DECIMAL.name())
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

        IEntityClass entityClass = entityService.load("1").get();
        Map<String, Object> maps = new HashMap<>();
        maps.put("field1", "123");
        Long id = entityService.create(entityClass, maps).get();

        contextService.set(TRANSACTION_KEY, "1234");
        CountDownLatch latch = new CountDownLatch(concurrent);

        IntStream.range(0, concurrent).mapToObj(x -> new Thread(() -> {
            contextService.set(TRANSACTION_KEY, "1234");
            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("field1", "123-" + Thread.currentThread().getName());
            System.out.println(entityService.retryExecute("a", () -> entityService
                    .updateById(entityClass, id, updateBody)));
            latch.countDown();
            contextService.clear();
        })).forEach(Thread::start);

        latch.await();

        System.out.println(entityService.findOne(entityClass, id));

        entityService.deleteOne(entityClass, id);

        Thread.sleep(10000);
    }


    @Test
    public void testSort() {
        metadataRepository.save(manyToOneNew(), "1", "1");
        IEntityClass entityClass = entityService.load("1").get();

        Map<String, Object> maps = new HashMap<>();
        maps.put("sortf", "1");


        Map<String, Object> maps2 = new HashMap<>();
        maps2.put("sortf", "2");


        entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .pageSize(10).pageNo(1).build()).get()._2().forEach(x -> {
            entityService.deleteOne(entityClass, Long.parseLong((String) x.get("id")));
        });

        Long id = entityService.create(entityClass, maps).get();
        Long id2 = entityService.create(entityClass, maps2).get();


        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .sort("sortf", "asc").pageSize(10).pageNo(1).build()));

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .sort("sortf", "desc").pageSize(10).pageNo(1).build()));

    }

    @Test
    public void testCompareDecimal() {
        metadataRepository.save(manyToOneNew(), "1", "1");
        IEntityClass entityClass = entityService.load("1").get();

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField", "15.23");

        Map<String, Object> maps2 = new HashMap<>();
        maps2.put("decimalField", "16.10");

        Long id = entityService.create(entityClass, maps).get();
        Long id2 = entityService.create(entityClass, maps2).get();

        Integer rows = entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("decimalField", ConditionOp.gt_lt, "14", "16.21")
                        .pageSize(10).pageNo(1).build()).get()._1();

        assertEquals(rows, (Integer)2);

        entityService.deleteOne(entityClass, id);
        entityService.deleteOne(entityClass, id2);
    }

    @Test
    public void testConditionSearch(){
        metadataRepository.save(manyToOneNew(), "1", "1");
        IEntityClass entityClass = entityService.load("1").get();

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField", "15.23");

        Map<String, Object> maps2 = new HashMap<>();
        maps2.put("decimalField", "16.10");

        Long id = entityService.create(entityClass, maps).get();
        Long id2 = entityService.create(entityClass, maps2).get();

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result = entityService
                .findByCondition(entityClass
                        , new RequestBuilder()
                                .field("decimalField", ConditionOp.in, Collections.emptyList()).pageNo(1).pageSize(10).build());


        Either<String, Tuple2<Integer, List<Map<String, Object>>>> searchByIds = entityService
                .findByConditionWithIds(entityClass
                        , Collections.emptyList()
                        , new RequestBuilder()
                                .pageNo(1).pageSize(10).build());

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result2 = entityService
                .findByCondition(entityClass
                        , new RequestBuilder()
                                .pageNo(1).pageSize(10).build());

        entityService.deleteOne(entityClass, id);
        entityService.deleteOne(entityClass, id2);

        System.out.println(result);
        System.out.println(result2);
        System.out.println(searchByIds);

    }

    @Test
    public void testPlain(){
        metadataRepository.save(manyToOneNew(), "1", "1");

        try {
            IEntityClass wrong = plainEntityService.load("2");
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        IEntityClass entityClass = plainEntityService.load("1");

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField22", "15.23");

        Long id = plainEntityService.create(entityClass, maps);

        System.out.println(plainEntityService.deleteOne(entityClass, id));
    }
}
