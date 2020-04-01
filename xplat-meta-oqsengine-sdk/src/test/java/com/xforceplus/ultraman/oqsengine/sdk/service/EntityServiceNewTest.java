package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.metadata.grpc.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
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
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.management.relation.RelationType;
import java.util.*;

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
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("1")
                        .setCode("main")
                        .addRelations(Relation.newBuilder()
                                .setId("1001")
                                .setRelationType("ManyToOne")
                                .setJoinBoId("2")
                                .setBoId("1")
                                .build())
                        .build())
                .addBoUps(BoUp
                        .newBuilder()
                        .setId("2")
                        .setCode("rel1")
                        .build())
                .build();
    }



    private EntityClass enumEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.ENUM, fieldConfig)));

        return entityClass;
    }

    private EntityClass longEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.LONG, fieldConfig)));

        return entityClass;
    }


    private EntityClass stringEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

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

        entityService.create(s, value);


        System.out.println(entityService.findByCondition(s,
                new RequestBuilder().field("defaultfield", ConditionOp.eq, "{{tenant_id}}").build()));
    }


}
