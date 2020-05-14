package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.metadata.grpc.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.xplat.galaxy.framework.configuration.AsyncTaskExecutorAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceDispatcherAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceInvokerAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.TENANT_ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestConfiguration.class
        , InitServiceAutoConfiguration.class
        , AuthSearcherConfig.class
        , ServiceInvokerAutoConfiguration.class
        , AsyncTaskExecutorAutoConfiguration.class
        , ServiceDispatcherAutoConfiguration.class
        , com.xforceplus.xplat.galaxy.framework.configuration.ContextConfiguration.class
        , RestTemplateAutoConfiguration.class
})
@EnableAsync
public class EntityServiceNewTest {

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Autowired
    ContextService contextService;

    @Autowired
    MetadataRepository metadataRepository;

    private ModuleUpResult mockModuleUpResult(){
        return ModuleUpResult
                .newBuilder()
                .setVersion("0.0.1")
                .setId(111111111111L)
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("1")
                        .setCode("main")
                        .addRelations(Relation.newBuilder()
                                .setId("1001")
                                .setRelationType("MultiValues")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .build())
                .addBoUps(BoUp
                        .newBuilder()
                        .setParentBoId("1")
                        .setId("2")
                        .setCode("rel1")
                        .build())
                .build();
    }

    /**
     * Long id, String name
     * , long entityClassId
     * , String entityClassName
     * , String ownerClassName
     * , String relationType
     * @return
     */
    private ModuleUpResult manyToOne() {
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
                                .setCode("field1")
                                .setSearchable("1")
                                .setFieldType("String")
                                .setId("1002")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field2")
                                .setSearchable("1")
                                .setId("1003")
                                .build())
                        .addFields(com.xforceplus.ultraman.metadata.grpc.Field
                                .newBuilder()
                                .setCode("field3")
                                .setSearchable("0")
                                .setId("1004")
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


    /**
     * Long id, String name
     * , long entityClassId
     * , String entityClassName
     * , String ownerClassName
     * , String relationType
     * @return
     */
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

    private EntityClass enumEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.ENUM, fieldConfig)));

        return entityClass;
    }

    private EntityClass longEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.LONG, fieldConfig)));

        return entityClass;
    }

    private EntityClass stringEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }

    private EntityClass booleanEntity(){
        FieldConfig fieldConfig = new FieldConfig().searchable(true);
        fieldConfig.required(true);

        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.BOOLEAN, fieldConfig)));

        return entityClass;
    }

    @Test
    public void testMultiValueService(){

        metadataRepository.save(mockModuleUpResult(), "1", "1");

        Optional<EntityClass> entityClass = metadataRepository.load("1", "1", "1");

        EntityClass entityClassReal = entityClass.get();

        System.out.println(entityClassReal);

        Map<String, Object> maps = new HashMap<>();
        maps.put("rel1.ids", "1,2");

        Long id = entityService.create(entityClassReal, maps).get();

        System.out.println(entityService.findOne(entityClassReal, id));

        System.out.println(entityService.findByCondition(entityClassReal
                , new RequestBuilder()
                    .field("rel1.ids", ConditionOp.in, "1")
                    .build()));
    }

    @Test
    public void testEnumFieldIn(){

        Map<String, Object> maps = new HashMap<>();
        maps.put("defaultfield", "1");

        EntityClass entityClass = enumEntity();

        Long id = entityService.create(entityClass, maps).get();

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                      .field("defaultfield", ConditionOp.in, "1")
                      .build()));

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("defaultfield", ConditionOp.in, "2")
                        .build()));
    }


    @Test
    public void testFieldLike(){

        Map<String, Object> maps = new HashMap<>();
        maps.put("defaultfield", "helloworld");

        EntityClass entityClass = stringEntity();

        Long id = entityService.create(entityClass, maps).get();

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("defaultfield", ConditionOp.like, "low")
                        .build()));
    }

    @Test
    public void testFieldTypeCheckWhenUpdate(){

        Map<String, Object> maps = new HashMap<>();
        maps.put("defaultfield", "123456");

        EntityClass entityClass = longEntity();

        Long id = entityService.create(entityClass, maps).get();

        System.out.println(entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("defaultfield", ConditionOp.eq, "123456")
                        .build()));

        Map<String, Object> maps2 = new HashMap<>();
        maps2.put("defaultfield", "N345");

        try {
            entityService.updateById(entityClass, id, maps2);
        }catch (Exception ex){

        }
    }

    @Test
    public void testManyToOne() throws InterruptedException {

        metadataRepository.save(mockModuleUpResult(), "1", "1");

        Optional<EntityClass> entityClass2 = metadataRepository.load("1", "1", "1");

        metadataRepository.save(manyToOne(), "1", "1");

        metadataRepository.save(manyToOne(), "1", "1");
        metadataRepository.save(manyToOne(), "1", "1");


        Optional<EntityClass> entityClass = metadataRepository.load("1", "1", "1");

        EntityClass entityClassReal = entityClass.get();

        System.out.println(entityClassReal);

        Map<String, Object> maps = new HashMap<>();
        maps.put("rel1.id", "{{tenant_id}}");

        Long id = entityService.create(entityClassReal, maps).get();

        System.out.println(entityService.findOne(entityClassReal, id));

        System.out.println(entityService.findByCondition(entityClassReal
                , new RequestBuilder()
                        .field("rel1.id", ConditionOp.eq, "1235")
                        .build()));
    }

    private void setupContext() {

        /**
         *         fixed.put("tenant_id", () -> contextService.get(TENANTID_KEY));
         *         fixed.put("create_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
         *         fixed.put("create_user_name", () -> contextService.get(USERNAME));
         *         fixed.put("create_user_id", () -> contextService.get(ID));
         *         fixed.put("delete_flag", () -> "1");
         *         fixed.put("update_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
         *         fixed.put("update_user_id", () -> contextService.get(ID));
         *         fixed.put("update_user_name", () -> contextService.get(USERNAME));
         */
        contextService.set(ID, 123454L);
        contextService.set(USERNAME, "created");
        contextService.set(USER_DISPLAYNAME, "created");
        contextService.set(TENANTID_KEY, "12312312312");
        contextService.set(TENANT_ID, 1111111L);
    }

    @Test
    public void testQuery(){

        setupContext();

        EntityClass s = longEntity();

        Map<String, Object> value = new HashMap<>();
        value.put("defaultfield", "{{tenant_id}}");

        Long id = entityService.create(s, value).get();


        System.out.println(entityService.findByCondition(s,
                new RequestBuilder().field("defaultfield", ConditionOp.eq, "{{tenant_id}}").build()));

        entityService.deleteOne(s, id);
    }

    @Test
    public void testConcurrentCurrentVersion() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(20);

        //SAVE Threads
        List<Thread> collect1 = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {

            metadataRepository.save(manyToOne(), "1", "1");
            latch.countDown();

        })).collect(Collectors.toList());

        //READ Threads
        List<Thread> collect2 = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {

            System.out.println(metadataRepository.load("1", "1", "1"));
            latch.countDown();
        })).collect(Collectors.toList());

        collect1.forEach(Thread::start);
        collect2.forEach(Thread::start);

        latch.await();
    }

    @Test
    public void testConcurrent() throws InterruptedException {

//        ScheduledExecutorService scheduledExecutorService =
//                Executors.newScheduledThreadPool(10);
//
//
//        ScheduledFuture scheduledFuture =
//                scheduledExecutorService.scheduleAtFixedRate(() -> {
//                    metadataRepository.save(manyToOne(), "1", "1");
//                },5, 5, TimeUnit.SECONDS);
//
//        ScheduledFuture loadedFuture =
//                scheduledExecutorService.scheduleAtFixedRate(() -> {
//                    System.out.println(metadataRepository.load("1", "1", "1"));
//                },5, 5, TimeUnit.SECONDS);

        CountDownLatch latch = new CountDownLatch(20);

        //SAVE Threads
        List<Thread> collect1 = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {

            metadataRepository.save(manyToOne(), "1", "1");
            latch.countDown();

        })).collect(Collectors.toList());


        //READ Threads
        List<Thread> collect2 = IntStream.range(0, 10).mapToObj(i -> new Thread(() -> {

            System.out.println(metadataRepository.load("1", "1", "1", "0.0.2"));
            latch.countDown();
        })).collect(Collectors.toList());

        collect1.forEach(Thread::start);
        collect2.forEach(Thread::start);

        latch.await();
    }

    @Test
    public void testUpdate(){
        metadataRepository.save(manyToOne(), "1", "1");
        metadataRepository.save(manyToOneNew(), "1", "1");
    }

    @Test
    public void returnBooleanTyped(){

        EntityClass boolEntity = booleanEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("defaultfield", true);

        Long result = entityService.create(boolEntity, map).get();

        boolean retValue = (boolean)entityService
                .findOne(boolEntity, result)
                .get().get("defaultfield");

        entityService.deleteOne(boolEntity, result);
    }

    @Test
    public void testLeftJoinTest(){
        metadataRepository.save(manyToOneNew(), "1", "1");

        EntityClass entityClass = entityService.load("1").get();

        EntityClass entityClass2 = entityService.load("2").get();

        Map<String, Object> one = new HashMap<>();
        one.clear();
        one.put("field21", "haha1");

        Long id1 = entityService.create(entityClass2, one).get();

        one.put("field21", "haha2");
        Long id2 = entityService.create(entityClass2, one).get();

        one.clear();
        one.put("field1", "nogood");
        one.put("rel1.id", id1);
        one.put("rel2.id", id2);

        Long id = entityService.create(entityClass, one).get();

        System.out.println("Id is " + id);

        entityService.findByCondition(entityClass
                , new RequestBuilder()
                        .field("id", ConditionOp.eq, id)
                        .item("field1")
                        .subItem("rel1", "field21")
                        .subItem("rel2", "field21")
                        .build())
                .forEach(System.out::println);

        entityService.deleteOne(entityClass2, id1);
        entityService.deleteOne(entityClass2, id2);
        entityService.deleteOne(entityClass, id);
    }

    @Test
    public void testLoadNoExist(){

        metadataRepository.save(manyToOneNew(), "1", "1");
        System.out.println(metadataRepository.load("1", "2", "10001"));
        System.out.println(metadataRepository.load("1", "2", "1", "0.0.5"));
    }
}
