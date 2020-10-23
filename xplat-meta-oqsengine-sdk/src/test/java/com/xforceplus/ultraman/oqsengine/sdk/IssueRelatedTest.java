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
import org.junit.runner.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableAsync
//@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
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

        //contextService.set(TRANSACTION_KEY, "1234");
        CountDownLatch latch = new CountDownLatch(concurrent);

        IntStream.range(0, concurrent).mapToObj(x -> new Thread(() -> {
            //contextService.set(TRANSACTION_KEY, "1234");
            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("field1", "123-" + Thread.currentThread().getName());
            System.out.println("1111111111" + entityService.retryExecute("a", () -> entityService
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
    public void testRetry(){

        Either<String, Object> objects = entityService.retryExecute("a", () -> {
            return Either.left("CONFLICT");
        });



    }

    @Test
    public void testCasAutoRetry() throws InterruptedException {
        metadataRepository.save(manyToOneNew(), "1", "1");

        int concurrent = 100;

        IEntityClass entityClass = entityService.load("1").get();
        Map<String, Object> maps = new HashMap<>();
        maps.put("field1", "123");
        Long id = entityService.create(entityClass, maps).get();

        CountDownLatch latch = new CountDownLatch(concurrent);

        IntStream.range(0, concurrent).mapToObj(x -> new Thread(() -> {
            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("field1", "123-" + Thread.currentThread().getName());
            System.out.println("1212121212" + plainEntityService.updateById(entityClass, id, updateBody));
            latch.countDown();
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



        entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("decimalField", ConditionOp.gt_lt, "150", "160.21")
                        .pageSize(10).pageNo(1).build()).get()._2().forEach(x -> entityService.deleteOne(entityClass, Long.parseLong(x.get("id").toString())));


        Map<String, Object> mapsfirst = new HashMap<>();
        mapsfirst.put("field1", "1");

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField", "150.23");

        Map<String, Object> maps2 = new HashMap<>();
        maps2.put("decimalField", "160.10");

        Long id = entityService.create(entityClass, maps).get();
        Long id2 = entityService.create(entityClass, maps2).get();
        Long id3 = entityService.create(entityClass, mapsfirst).get();

        Integer rows = entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("decimalField", ConditionOp.gt_lt, "150", "160.21")
                        .pageSize(10).pageNo(1).build()).get()._1();

        assertEquals(rows, (Integer)2);


        Integer rows2 = entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("decimalField", ConditionOp.gt_lt, "150", "160.21")
                        .field("field1", ConditionOp.eq, "1")
                        .pageSize(10).pageNo(1).build()).get()._1();

        assertEquals(rows2, (Integer)0);

        entityService.deleteOne(entityClass, id);
        entityService.deleteOne(entityClass, id2);
        entityService.deleteOne(entityClass, id3);
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
            IEntityClass wrong = plainEntityService.load("200");
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        IEntityClass entityClass = plainEntityService.load("1");

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField22", "15.23");

        Long id = plainEntityService.create(entityClass, maps);

        System.out.println(plainEntityService.deleteOne(entityClass, id));
    }

    @Test
    public void currentTransaction(){

        metadataRepository.save(manyToOneNew(), "1", "1");

        Map<String, Object> maps = new HashMap<>();
        maps.put("decimalField", "100000");


        IEntityClass entityClass = plainEntityService.load("1");


        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder().field("decimalField", ConditionOp.eq, "100000").pageSize(10).pageNo(1).build()));
        /**
         *
         */
        Either<String, List<Long>> lists = entityService.transactionalExecute(() -> {

            Thread.sleep(2500);

            return IntStream.range(1, 100)
                    .boxed()
                    .map(i -> {
                        return entityService.create(entityClass, maps).getOrElse((Long) null);
                    }).filter(Objects::nonNull).collect(Collectors.toList());
        });

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder().field("decimalField", ConditionOp.eq, "100000").pageSize(10).pageNo(1).build()));

        lists.map(r -> { r.forEach(x -> {
            entityService.deleteOne(entityClass, x);

        });
        return "";}).orElseRun(System.out::println);

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder().field("decimalField", ConditionOp.eq, "100000").pageSize(10).pageNo(1).build()));
    }


    @Test
    public void testForceDelete() throws InterruptedException {
            //try delete
            Optional<IEntityClass> load = entityService.load("1250257809316302850");

            IEntityClass entityClass = load.get();

            Map<String, Object> body = new HashMap<>();

            body.put("amout", "12.5");

            Either<String, Long> id = entityService.create(entityClass, body);

            System.out.println(id);

            Long realId = id.get();

            CountDownLatch latch = new CountDownLatch(1);

            int i = 20;
            while(i -- > 0) {
                new Thread(() -> {
                    int count = 100;
                    while (count-- > 0) {

                        Map<String, Object> bodyUpdate = new HashMap<>();
                        body.put("amout", "12.6");
                        Either<String, Integer> integers = entityService.updateById(entityClass, realId, bodyUpdate);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            new Thread(() -> {
                int count = 1000;
                while(count -- > 0){

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Either<String, Integer> integers = entityService.forceDeleteOne(entityClass, realId);

                    if(integers.isRight()){
                        Integer tried = 1000 - count;
                        System.out.println("Try " + (1000 - count));

                        assertTrue(tried == 1);
                        count = 0;
                    }
                }

                latch.countDown();
            }).start();

            latch.await();
    }
}
