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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private EntityClass enumEntity() {
        FieldConfig fieldConfig = new FieldConfig().searchable(true);

        fieldConfig.required(true);
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.ENUM, fieldConfig)));

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
}
