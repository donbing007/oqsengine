package com.xforceplus.ultraman.oqsengine.sdk.store;

import com.xforceplus.ultraman.metadata.grpc.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.MetadataRepositoryInMemoryImpl;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.util.AssertionErrors.assertTrue;

public class StoreTest {

    /**
     *
     * @return
     */
    private ModuleUpResult mockModuleUpResult(){
        return ModuleUpResult.newBuilder()
                .addBoUps(boupA())
                .build();
    }

    private BoUp boupA(){
        return BoUp.newBuilder()
                .setId("111111")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("12322")
                        .setCode("abcOld")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp boupANew(){
        return BoUp.newBuilder()
                .setId("111111")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("12322")
                        .setCode("abcd")
                        .setFieldType("string")
                        .setName("fieldANew")
                        .build())
                .build();
    }

    private BoUp boupSub(){
        return BoUp.newBuilder()
                .setId("111111")
                .setCode("sub")
                .setCreateType("2")
                .setParentBoId("1111")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("12322")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp boupParent(){
        return BoUp.newBuilder()
                .setId("1111")
                .setCode("parent")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("1111234")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp boupMultiRelation(){
        return BoUp.newBuilder()
                .setId("1111")
                .setCode("main")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addRelations(Relation.newBuilder()
                        .setBoId("1111")
                        .setJoinBoId("2222")
                        .setId("22221")
                        .setRelationType("OneToOne")
                        .build())
                .addRelations(Relation.newBuilder()
                        .setBoId("1111")
                        .setJoinBoId("22221")
                        .setId("22222")
                        .setRelationType("ManyToOne")
                        .build())
                .addRelations(Relation.newBuilder()
                        .setBoId("1111")
                        .setJoinBoId("22222")
                        .setId("22223")
                        .setRelationType("OneToMany")
                        .build())
                .addBoUps(relationBo())
                .addBoUps(relationBo1())
                .addBoUps(relationBo2())
                .addFields(Field.newBuilder()
                        .setId("1111234")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }


    private BoUp boupMultiExtendRelation(){
        return BoUp.newBuilder()
                .setId("11112")
                .setCode("main")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addRelations(Relation.newBuilder()
                        .setBoId("11112")
                        .setJoinBoId("111111")
                        .setId("22221")
                        .setRelationType("OneToOne")
                        .build())
                .addBoUps(boupSub())
                .addFields(Field.newBuilder()
                        .setId("1111234")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp boupRelation(){
        return BoUp.newBuilder()
                .setId("1111")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addRelations(Relation.newBuilder()
                        .setBoId("1111")
                        .setJoinBoId("2222")
                        .setId("22221")
                        .setRelationType("OneToOne")
                        .build())
                .addBoUps(relationBo())
                .addFields(Field.newBuilder()
                        .setId("1111234")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp relationBo(){
        return BoUp.newBuilder()
                .setId("2222")
                .setCode("sub")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("11112234")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp relationBo1(){
        return BoUp.newBuilder()
                .setId("22221")
                .setCode("sub1")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("111122341")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    private BoUp relationBo2(){
        return BoUp.newBuilder()
                .setId("22222")
                .setCode("sub2")
                .addApis(Api.newBuilder().setCode("a").setMethod("b").build())
                .addFields(Field.newBuilder()
                        .setId("111122342")
                        .setCode("abc")
                        .setFieldType("string")
                        .setName("fieldA")
                        .build())
                .build();
    }

    /**
     * save bo test
     */
    @Test
    public void simpleSaveAndLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();
        repository.save(mockModuleUpResult(), "1", "1");
        Optional<EntityClass> entityclass = repository.load("1", "1", "111111");
        assertTrue("插入读取成功", entityclass.isPresent());
    }


    @Test
    public void doubleSaveAndLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();
        /**
         * 相同记录插入
         */
        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupA())
                .addBoUps(boupA())
                .build();


        repository.save(result, "1", "1");
        Optional<EntityClass> entityclass = repository.load("1", "1", "111111");

        System.out.println(entityclass.get());

    }

    @Test
    public void updateSaveAndLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();
        /**
         * 相同记录插入
         */
        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupA())
                .addBoUps(boupA())
                .build();


        repository.save(result, "1", "1");
        Optional<EntityClass> entityclass = repository.load("1", "1", "111111");

        System.out.println(entityclass.get());


        ModuleUpResult resultNew = ModuleUpResult.newBuilder()
                .addBoUps(boupANew())
                .build();

        repository.save(resultNew, "1", "1");
        Optional<EntityClass> entityclassNew = repository.load("1", "1", "111111");

        System.out.println(entityclassNew.get());
    }


    @Test
    public void saveSingleExtendedLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();
        /**
         * 相同记录插入
         */
        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupParent())
                .addBoUps(boupSub())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclass = repository.load("1", "1", "111111");

        System.out.println(entityclass.get());

        /**
         * update
         */
        ModuleUpResult resultNoParent = ModuleUpResult.newBuilder()
                .addBoUps(boupA())
                .build();

        repository.save(resultNoParent, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "111111");

        System.out.println("A:" + entityclassA.get());
    }

    @Test
    public void saveRelationAndLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();

        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupRelation())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "1111");

        System.out.println("A:" + entityclassA.get());

    }

    @Test
    public void saveMultiRelationAndLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();

        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupMultiRelation())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "1111");

        System.out.println("A:" + entityclassA.get());
    }

    @Test
    public void saveMultiRelationWithExtendLoad(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();

        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupMultiExtendRelation())
                .addBoUps(boupParent())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "11112");
        Optional<EntityClass> entityclassB = repository.load("1", "1", "1111");
        Optional<EntityClass> entityclassC = repository.load("1", "1", "111111");


        System.out.println("A:" + entityclassA.get());
        System.out.println("B:" + entityclassB.get());
        System.out.println("C:" + entityclassC.get());
    }

    @Test
    public void loadDetailsFor(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();

        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupMultiExtendRelation())
                .addBoUps(boupParent())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "11112");
        Optional<EntityClass> entityclassB = repository.load("1", "1", "1111");
        Optional<EntityClass> entityclassC = repository.load("1", "1", "111111");


        BoItem boItem = repository.getBoDetailById("11112");

        System.out.println(boItem);
    }


    @Test
    public void testCreate(){
        MetadataRepository repository = new MetadataRepositoryInMemoryImpl();

        ModuleUpResult result = ModuleUpResult.newBuilder()
                .addBoUps(boupMultiExtendRelation())
                .addBoUps(boupParent())
                .build();

        repository.save(result, "1", "1");
        Optional<EntityClass> entityclassA = repository.load("1", "1", "11112");
        Optional<EntityClass> entityclassB = repository.load("1", "1", "1111");
        Optional<EntityClass> entityclassC = repository.load("1", "1", "111111");

        EntityService entityService = new EntityService(repository, null, null);

        Map<String, Object> data = new HashMap<>();

        data.put("", "");

        EntityUp entity = entityService.toEntityUp(entityclassA.get(), null, data);

        System.out.println(entity);
    }
}
