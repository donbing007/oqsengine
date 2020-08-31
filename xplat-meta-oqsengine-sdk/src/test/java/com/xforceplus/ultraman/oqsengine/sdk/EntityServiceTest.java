package com.xforceplus.ultraman.oqsengine.sdk;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.configuration.TestApplicationContextInitializer;
import com.xforceplus.ultraman.oqsengine.sdk.controller.EntityController;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityExportService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.xplat.galaxy.framework.configuration.AsyncTaskExecutorAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceDispatcherAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceInvokerAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.control.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * entity service test
 */
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
public class EntityServiceTest extends ContextWareBaseTest{

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Autowired
    ContextService contextService;

    @Autowired
    EntityController entityController;

    @Autowired
    EntityExportService entityExportService;

//    @Test
//    public void testPagenation() throws InterruptedException {
//        Thread.sleep(10000);
//
//        Optional<EntityClass> entityOpt = entityService.loadByCode("image");
//
//        System.out.println(entityService.findByCondition(entityOpt.get(), new RequestBuilder().pageNo(7)
//                .pageSize(10).build()).get()._2().size());
//    }


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
    }



    private EntityClass sampleEntity() {
        /**
         * long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue
         */

        FieldConfig fieldConfig = new FieldConfig();
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield", FieldType.ENUM, fieldConfig, "abc", "Happy")));

        return entityClass;
    }


    private EntityClass regexSampleEntity() {
        /**
         * long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue
         */
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.validateRegexString("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$");
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }

    private EntityClass expressionSampleEntity() {
        FieldConfig fieldConfig = new FieldConfig();
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }


    private EntityClass requiredSample() {
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }


    @Test
    public void testRegx() {
        EntityClass entityClass = regexSampleEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("defaultfield", "luye66@163.com");

        Long id = entityService.create(entityClass, map).get();
        assertTrue("默认值正确", entityService.findOne(entityClass, id).get().get("defaultfield").equals("luye66@163.com"));
    }

    @Test
    public void testExpression() {

        setupContext();

        EntityClass entityClass = expressionSampleEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("defaultfield", "{{id}}");

        Long id = entityService.create(entityClass, map).get();
        assertTrue("默认值正确", entityService.findOne(entityClass, id).get().get("defaultfield").equals("123454"));
    }

    @Test
    public void testDefaultValue() throws InterruptedException {

        EntityClass sampleEntity = sampleEntity();

        Map<String, Object> map = new HashMap<>();

        Long id = entityService.create(sampleEntity, map).get();

        assertTrue("默认值正确", entityService.findOne(sampleEntity, id).get().get("defaultfield").equals("Happy"));
    }

    @Test
    public void testRequiredValue() throws InterruptedException {

        EntityClass sampleEntity = requiredSample();

        Map<String, Object> map = new HashMap<>();

        try {
            entityService.create(sampleEntity, map).get();
        } catch (Exception ex){
            ex.printStackTrace();
            assertTrue("has ex", true);
        }
    }

    /**
     *
     * @return
     */
    private EntityClass entity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);
        fieldConfig.required(true);

        FieldConfig nonRequiredfieldConfig = new FieldConfig().searchable(true);


        FieldConfig fieldConfig1 = new FieldConfig().identifie(true);

        EntityClass entityClass = new EntityClass(123666L, "TestDefault"
                , Arrays.asList(new EntityField(123L, "defaultfield"
                , FieldType.STRINGS, fieldConfig)
                , new EntityField(10000L, "id", FieldType.LONG, fieldConfig1)
                , new EntityField(234L, "hello", FieldType.STRING, nonRequiredfieldConfig)
        ));

        return entityClass;
    }

    @Test
    public void testStrings(){

        EntityClass entityClass = entity();

        Integer count = entityService.count(entityClass, new RequestBuilder().pageNo(1).pageSize(100).field("defaultfield", ConditionOp.in
                , "1", "2" , "3", "4" , "5").build());

        System.out.println(count);

        entityService.findByCondition(entityClass, new RequestBuilder().pageNo(1).pageSize(100).field("defaultfield", ConditionOp.in
                , "1", "2" , "3", "4" , "5").build()).map(x -> x._2()).forEach(
                x -> x.stream().forEach(y -> {
                    Either<String, Integer> id = entityService.deleteOne(entityClass, Long.parseLong((String) y.get("id")));
                    System.out.println(id);
                })
        );


        Map<String, Object> map3 = new HashMap<>();
        map3.put("defaultfield", "1,2,3");

        Either<String, Long> ret2 = entityService.create(entityClass, map3);
//        System.out.println(entityService.findOne(entityClass, ret2.get()));

        Integer row = entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.in
                , "1", "2" , "3", "4" , "5").pageNo(1).pageSize(10).build()).get()._1();
        assertEquals("Got", row, 1);

//        System.out.println(entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.ne
//                , "1").pageNo(1).pageSize(10).build()));
//
        Integer row2 = entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.eq
                , "1").pageNo(1).pageSize(10).build()).get()._1();
        assertEquals("Got", row2, 1);
    }


    @Test
    public void updateByCondition(){
//        IntStream.of(0,4).forEach(x ->{
            EntityClass entityClass = entity();

            Map<String, Object> map3 = new HashMap<>();
            map3.put("hello", "1");
            map3.put("defaultfield", "0");

            Either<String, Long> saveResult = entityService.create(entityClass, map3);

            System.out.println(saveResult);

            Long id = saveResult.get();

            //change by another
            Map<String, Object> firstUpdateBody = new HashMap<>();
            firstUpdateBody.put("hello", "100");
            entityService.updateById(entityClass, id, firstUpdateBody);

            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("hello", "2");

            Either<String, Integer> updateResult = entityService.updateByCondition(entityClass, new RequestBuilder().field("hello", ConditionOp.eq, "1").build(), updateBody);

            assertTrue("update is not ok", updateResult.isRight());
            assertEquals("hello is still 100", entityService.findOne(entityClass, id).get().get("hello"), "100");

            entityService.deleteOne(entityClass, id);
//        });

    }


//
    @Test
    public void updateByConditionTransTest(){

        AtomicLong atomicLong = new AtomicLong(0);
        EntityClass entityClass = entity();

        Either<String, Object> objects = entityService.transactionalExecute(() -> {


            Map<String, Object> map3 = new HashMap<>();
            map3.put("hello", "1");
            map3.put("defaultfield", "0");

            Either<String, Long> saveResult = entityService.create(entityClass, map3);
            Long id = saveResult.get();

            atomicLong.set(id);

            Map<String, Object> map4 = new HashMap<>();
            map4.put("hello", "1");
            map4.put("defaultfield", "0");

            Either<String, Long> saveResult2 = entityService.create(entityClass, map4);

            //change by another
            Map<String, Object> firstUpdateBody = new HashMap<>();
            firstUpdateBody.put("hello", "100");
            entityService.updateById(entityClass, id, firstUpdateBody);

            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("hello", "2");

            Either<String, Integer> updateResult = entityService.updateByCondition(entityClass, new RequestBuilder().field("hello", ConditionOp.eq, "1").build(), updateBody);



            System.out.println(updateResult);
            assertTrue("update is not ok", updateResult.isRight());
            //assertEquals("hello is still 100", entityService.findOne(entityClass, id).get().get("hello"), "100");

            throw new RuntimeException("Roll");
        });

        System.out.println(objects.getLeft());
        System.out.println(atomicLong.get());
        System.out.println(entityService.findOne(entityClass, atomicLong.get()));
    }


    @Test
    public void timeoutTransTest(){

        AtomicLong id1 = new AtomicLong(0);
        EntityClass entityClass = entity();

        Either<String, Object> objects = entityService.transactionalExecute(() -> {


            Map<String, Object> map3 = new HashMap<>();
            map3.put("hello", "1");
            map3.put("defaultfield", "0");

            Either<String, Long> saveResult = entityService.create(entityClass, map3);
            Long id = saveResult.get();

            id1.set(id);

            Thread.sleep(10000);

            Map<String, Object> map4 = new HashMap<>();
            map4.put("hello", "1");
            map4.put("defaultfield", "0");

            Either<String, Long> saveResult2 = entityService.create(entityClass, map4);

            assertTrue("Failed", saveResult2.isLeft());

            throw new RuntimeException("Roll");
        });

        System.out.println(objects.getLeft());
        System.out.println(id1.get());
        System.out.println(entityService.findOne(entityClass, id1.get()));
    }



    @Test
    public void selectInTransTest(){

        AtomicLong atomicLong = new AtomicLong(0);
        EntityClass entityClass = entity();

        Either<String, Object> objects = entityService.transactionalExecute(() -> {


            Map<String, Object> map3 = new HashMap<>();
            map3.put("hello", "1");
            map3.put("defaultfield", "0");

            Either<String, Long> saveResult = entityService.create(entityClass, map3);
            Long id = saveResult.get();


            assertEquals("hello is 1", entityService.findOne(entityClass, id).get().get("hello"), "1");

            atomicLong.set(id);

            //change by another
            Map<String, Object> firstUpdateBody = new HashMap<>();
            firstUpdateBody.put("hello", "100");
            entityService.updateById(entityClass, id, firstUpdateBody);


            assertEquals("hello is 100", entityService.findOne(entityClass, id).get().get("hello"), "100");

            Map<String, Object> updateBody = new HashMap<>();
            updateBody.put("hello", "2");

            //Either<String, Integer> updateResult = entityService.updateByCondition(entityClass, new RequestBuilder().field("hello", ConditionOp.eq, "1").build(), updateBody);


            assertEquals("hello is still 100", entityService.findOne(entityClass, id).get().get("hello"), "100");

            //System.out.println(updateResult);
            //assertTrue("update is not ok", updateResult.isRight());

            throw new RuntimeException("Roll");
        });

        System.out.println(objects.getLeft());
        System.out.println(atomicLong.get());
        System.out.println(entityService.findOne(entityClass, atomicLong.get()));
    }

    @Test
    public void testExport(){

        EntityClass entityClass = entity();

//        Map<String, Object> body = new HashMap<>();
//        IntStream.range(0, 50000).forEach(x -> {
//            body.put("hello", "" + x);
//            body.put("defaultfield", "");
//            entityService.create(entityClass, body);
//        });

        ConditionQueryRequest build = new RequestBuilder().build();




        Either<String, String> join = entityExportService.export(entityClass, build
                , "ok", "ok", null, "sync", null).join();

        System.out.println(join);
    }
}