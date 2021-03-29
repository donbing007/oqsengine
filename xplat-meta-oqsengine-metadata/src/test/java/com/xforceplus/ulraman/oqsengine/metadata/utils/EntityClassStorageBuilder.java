package com.xforceplus.ulraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.RelationStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;

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

    public static RelationStorage relationLong(long id, long fieldId) {

        RelationStorage r = new RelationStorage();
        r.setId(id);
        r.setCode(id + "_test");
        r.setRelationType(1);
        r.setIdentity(false);
        r.setLeftEntityClassId(id);
        r.setRightEntityClassId(id + 1000);
        r.setEntityField(entityFieldLong(fieldId));

        return r;
    }

    public static RelationStorage relationString(long id, long fieldId) {
        RelationStorage r = new RelationStorage();
        r.setId(id);
        r.setCode(id + "_test");
        r.setRelationType(1);
        r.setIdentity(false);
        r.setLeftEntityClassId(id);
        r.setRightEntityClassId(id + 1000);
        r.setEntityField(entityFieldString(fieldId));

        return r;
    }

    public static EntityField entityFieldLong(long id) {
        return new EntityField(id, "id" + id, FieldType.LONG, FieldConfig.build().searchable(true).identifie(false));
    }

    public static EntityField entityFieldString(long id) {
        return new EntityField(id, "id" + id, FieldType.STRING, FieldConfig.build().searchable(true).identifie(false));
    }

    public static EntityClassStorage prepareEntity(ExpectedEntityStorage expectedEntityStorage) {
        IEntityField[] entityFields = new IEntityField[2];
        entityFields[0] = entityFieldLong(expectedEntityStorage.getSelf());
        entityFields[1] = entityFieldString(expectedEntityStorage.getSelf() + 1);

        RelationStorage[] relations = new RelationStorage[2];
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
        private List<Long> relationIds;

        public ExpectedEntityStorage(Long self, Long father, List<Long> ancestors, List<Long> relationIds) {
            this.self = self;
            this.father = father;
            this.ancestors = ancestors;
            this.relationIds = relationIds;
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

        public List<Long> getRelationIds() {
            return relationIds;
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
                    entityClassInfos.add(entityClassInfo(e.getSelf(), e.getFather(), e.getRelationIds(),
                            null != e.getAncestors() ? e.getAncestors().size() : 0));
                }
        );

        return EntityClassSyncRspProto.newBuilder()
                .addAllEntityClasses(entityClassInfos)
                .build();
    }

    public static EntityClassInfo entityClassInfo(long id, long father, List<Long> relationEntityId, int level) {
        List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();

        entityFieldInfos.add(entityFieldInfo(id, EntityFieldInfo.FieldType.LONG));
        entityFieldInfos.add(entityFieldInfo(id + 1, EntityFieldInfo.FieldType.STRING));

        List<RelationInfo> relationInfos = new ArrayList<>();
        if (null != relationEntityId) {
            for (int i = 0; i < relationEntityId.size(); i++) {
                RelationInfo relationInfo = relationInfo(id + i, relationEntityId.get(i), id, 1, id + i);
                relationInfos.add(relationInfo);
            }
        }

        return EntityClassInfo.newBuilder()
                .setId(id)
                .setVersion(1)
                .setCode(id + "_level" + level + "_code")
                .setName(id + "_level" + level + "_name")
                .setFather(father)
                .setLevel(level)
                .addAllEntityFields(entityFieldInfos)
                .addAllRelations(relationInfos)
                .build();
    }

    public static EntityFieldInfo entityFieldInfo(long id, EntityFieldInfo.FieldType fieldType) {
        return EntityFieldInfo.newBuilder()
                .setId(id)
                .setName(id + "_name")
                .setCname(id + "_cname")
                .setFieldType(fieldType)
                .setDictId(id + "_dictId")
                .setFieldConfig(fieldConfig(true, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense.NORMAL))
                .build();
    }

    public static RelationInfo relationInfo(long id, long entityId, long ownerId, int relationType, long fieldId) {
        return RelationInfo.newBuilder()
                .setId(id)
                .setCode(id + "_name")
                .setRightEntityClassId(entityId)
                .setLeftEntityClassId(ownerId)
                .setRelationType(relationType)
                .setEntityField(EntityFieldInfo.newBuilder()
                        .setId(fieldId)
                        .setFieldType(EntityFieldInfo.FieldType.LONG)
                        .setName(fieldId + "_name")
                        .setFieldConfig(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.newBuilder().setSearchable(true).build())
                        .build())
                .setBelongToOwner(id % 2 == 0)
                .build();

    }

    public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig
                fieldConfig(boolean searchable, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense systemFieldType) {
        return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.newBuilder()
                .setSearchable(searchable)
                .setIsRequired(true)
                .setMetaFieldSense(systemFieldType)
                .build();

    }

    public static List<EntityClassStorageBuilder.ExpectedEntityStorage> mockSelfFatherAncestorsGenerate(long id) {
        List<EntityClassStorageBuilder.ExpectedEntityStorage> expectedEntityStorages = new ArrayList<>();

        long father = getFather(id);
        long anc = getAnc(id);

        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(id, father, Arrays.asList(father, anc), Collections.singletonList(father)));
        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(father, anc, Collections.singletonList(anc), Collections.singletonList(id)));
        expectedEntityStorages.add(new EntityClassStorageBuilder.ExpectedEntityStorage(anc, 0L, null, Collections.singletonList(anc)));

        return expectedEntityStorages;
    }


    public static long getFather(long id) {
        return id + 100;
    }

    public static long getAnc(long id) {
        return id + 100 + 1000;
    }
}
