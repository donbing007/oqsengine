package com.xforceplus.ulraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * desc :
 * name : EntityClassStorageBuilder
 *
 * @author : xujia
 * date : 2021/2/16
 * @since : 1.8
 */
public class EntityClassStorageBuilder {

    public static OqsRelation relationLong(long id, long fieldId) {
        return new OqsRelation("test" + id, id, "order", false, fieldId);
    }

    public static OqsRelation relationString(long id, long fieldId) {
        return new OqsRelation("test" + id, id, "order", false, fieldId);
    }

    public static IEntityField entityFieldLong(long id) {
        return new EntityField(id, "id" + id, FieldType.LONG, FieldConfig.build().searchable(true).identifie(false));
    }

    public static IEntityField entityFieldString(long id) {
        return new EntityField(id, "id" + id, FieldType.STRING, FieldConfig.build().searchable(true).identifie(false));
    }

    public static EntityClassStorage prepareEntity(ExpectedEntityStorage expectedEntityStorage) {
        IEntityField[] entityFields = new IEntityField[2];
        entityFields[0] = entityFieldLong(expectedEntityStorage.getSelf());
        entityFields[1] = entityFieldString(expectedEntityStorage.getSelf() + 1);

        OqsRelation[] relations = new OqsRelation[2];
        relations[0] = relationLong(expectedEntityStorage.getSelf(), expectedEntityStorage.getSelf() - 1);
        relations[1] = relationString(expectedEntityStorage.getSelf(), expectedEntityStorage.getSelf() - 2);

        EntityClassStorage entityClassStorage = new EntityClassStorage();
        entityClassStorage.setId(expectedEntityStorage.getSelf());
        entityClassStorage.setName(expectedEntityStorage.getSelf() + "_name");
        entityClassStorage.setCode(expectedEntityStorage.getSelf() + "_code");
        entityClassStorage.setVersion(1);


        if (null != expectedEntityStorage.getFather()) {
            entityClassStorage.setFatherId(expectedEntityStorage.getFather());
        } else {
            entityClassStorage.setFatherId(0L);
        }

        if (null != expectedEntityStorage.getAncestors()) {
            entityClassStorage.setAncestors(expectedEntityStorage.getAncestors());
            entityClassStorage.setLevel(expectedEntityStorage.getAncestors().size());
        } else {
            entityClassStorage.setLevel(0);
        }

        entityClassStorage.setFields(Arrays.asList(entityFields));
        entityClassStorage.setRelations(Arrays.asList(relations));

        return entityClassStorage;
    }


    public static class ExpectedEntityStorage {
        private List<Long> ancestors;
        private Long self;
        private Long father;

        public ExpectedEntityStorage(Long self, Long father, List<Long> ancestors) {
            this.self = self;
            this.father = father;
            this.ancestors = ancestors;
        }

        public List<Long> getAncestors() {
            return ancestors;
        }

        public Long getSelf() {
            return self;
        }

        public Long getFather() {
            return father;
        }
    }


    /**
     * 将生成随机的3层父子类结构[爷爷、父亲、儿子]
     * 每层有2个随机的EntityField [String、Long] 各一
     * 每层存在2条关系
     * @param appId
     * @param version
     * @return
     */
    public static EntityClassSyncResponse entityClassSyncResponseGenerator(String appId, int version,
                                        List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorages) {


        return EntityClassSyncResponse.newBuilder()
                .setAppId(appId)
                .setVersion(version + 1)
                .setEntityClassSyncRspProto(entityClassSyncRspProtoGenerator(expectedEntityStorages))
                .build();
    }

    public static EntityClassSyncRspProto entityClassSyncRspProtoGenerator(List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorages) {
        /**
         * 生成爷爷
         */
        List<EntityClassInfo> entityClassInfos = new ArrayList<>();
        expectedEntityStorages.forEach(
                e -> {
                    entityClassInfos.add(entityClassInfo(e.getSelf(), e.getFather(),
                            null != e.getAncestors() ? e.getAncestors().size() : 0));
                }
        );

        return EntityClassSyncRspProto.newBuilder()
                .addAllEntityClasses(entityClassInfos)
                .build();
    }

    public static EntityClassInfo entityClassInfo(long id, long father, int level) {
        List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();
        entityFieldInfos.add(entityFieldInfo(id, EntityFieldInfo.FieldType.LONG));
        entityFieldInfos.add(entityFieldInfo(id + 1, EntityFieldInfo.FieldType.STRING));

        List<RelationInfo> relationInfos = new ArrayList<>();
        relationInfos.add(relationInfo(id, id + 2, id, "toOne", id));
        relationInfos.add(relationInfo(id + 1, id + 2, id + 1, "toOne", id + 1));

        return EntityClassInfo.newBuilder()
                .setId(id)
                .setVersion(1)
                .setCode(id + "_level" + level + "_code")
                .setName(id + "_level" + level + "_name")
                .setFather(father)
                .setLevel(level)
                .addAllEntityFields(entityFieldInfos)
                .addAllRelations(relationInfos)
                .setIsAny(false)
                .setIsDynamic(false)
                .build();
    }

    public static EntityFieldInfo entityFieldInfo(long id, EntityFieldInfo.FieldType fieldType) {
        return EntityFieldInfo.newBuilder()
                .setId(id)
                .setName(id + "_name")
                .setCname(id + "_cname")
                .setFieldType(fieldType)
                .setDictId(id + "_dictId")
                .setFieldConfig(fieldConfig(true, com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig.MetaFieldSense.NORMAL))
                .build();
    }

    public static RelationInfo relationInfo(long id, long entityId, long ownerId, String relationType, long fieldId) {
        return RelationInfo.newBuilder()
                .setId(id)
                .setName(id + "_name")
                .setEntityClassId(entityId)
                .setRelOwnerClassId(ownerId)
                .setRelationType(relationType)
                .setEntityFieldId(fieldId)
                .build();

    }

    public static com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig
                fieldConfig(boolean searchable, com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig.MetaFieldSense systemFieldType) {
        return com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig.newBuilder()
                .setSearchable(searchable)
                .setIsRequired(true)
                .setMetaFieldSense(systemFieldType)
                .build();

    }

    public static List<EntityClassStorageBuilder.ExpectedEntityStorage> mockSelfFatherAncestorsGenerate(long id) {
        List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorages = new ArrayList<>();

        long father = getFather(id);
        long anc = getAnc(id);

        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(id, father, Arrays.asList(father, anc)));
        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(father, anc, Collections.singletonList(anc)));
        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(anc, 0L, null));

        return expectedEntityStorages;
    }


    public static long getFather(long id) {
        return id + 100;
    }

    public static long getAnc(long id) {
        return id + 100 + 1000;
    }
}
