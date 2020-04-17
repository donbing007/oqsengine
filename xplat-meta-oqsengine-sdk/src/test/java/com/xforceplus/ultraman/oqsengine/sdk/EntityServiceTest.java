package com.xforceplus.ultraman.oqsengine.sdk;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
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

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * entity service test
 */
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

public class EntityServiceTest {

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Autowired
    ContextService contextService;

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
                , Arrays.asList(new Field(123L, "defaultfield", FieldType.ENUM, fieldConfig, "abc", "Happy")));

        return entityClass;
    }


    private EntityClass regexSampleEntity() {
        /**
         * long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue
         */
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.validateRegexString("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$");
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }

    private EntityClass expressionSampleEntity() {
        FieldConfig fieldConfig = new FieldConfig();
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }


    private EntityClass requiredSample() {
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
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
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRINGS, fieldConfig)));

        return entityClass;
    }

    @Test
    public void testStrings(){

        EntityClass entityClass = entity();
        Map<String, Object> map3 = new HashMap<>();
        map3.put("defaultfield", "1,2,3,4");

        Either<String, Long> ret2 = entityService.create(entityClass, map3);
        System.out.println(entityService.findOne(entityClass, ret2.get()));

        System.out.println(entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.in
                , "1", "2").build()));

        System.out.println(entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.ne
                , "1").build()));

        System.out.println(entityService.findByCondition(entityClass, new RequestBuilder().field("defaultfield", ConditionOp.eq
                , "1").build()));
    }
}