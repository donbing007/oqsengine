package com.xforceplus.ultraman.oqsengine.meta.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * desc :
 * name : MockEntityClassSyncRspProtoBuilder
 *
 * @author : xujia
 * date : 2021/3/1
 * @since : 1.8
 */
public class MockEntityClassSyncRspProtoBuilder {


    public static EntityClassSyncRspProto entityClassSyncRspProtoGenerator(long expectedId) {

        List<ExpectedEntityStorage> storages = mockSelfFatherAncestorsGenerate(expectedId);
        List<EntityClassInfo> entityClassInfos = new ArrayList<>();
        storages.forEach(
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
        relationInfos.add(relationInfo(id, id + 2, id, 0, id));
        relationInfos.add(relationInfo(id + 1, id + 2, id + 1, 0, id + 1));

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
                .setStrong(true)
                .setEntityField(EntityFieldInfo.newBuilder()
                        .setId(fieldId)
                        .setFieldType(EntityFieldInfo.FieldType.LONG)
                        .setName(fieldId + "_name")
                        .setFieldConfig(FieldConfig.newBuilder().setSearchable(true).build())
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

    public static List<ExpectedEntityStorage> mockSelfFatherAncestorsGenerate(long id) {
        List<ExpectedEntityStorage> expectedEntityStorages = new ArrayList<>();

        long father = getFather(id);
        long anc = getAnc(id);

        expectedEntityStorages.add(new ExpectedEntityStorage(id, father, Arrays.asList(father, anc)));
        expectedEntityStorages.add(new ExpectedEntityStorage(father, anc, Collections.singletonList(anc)));
        expectedEntityStorages.add(new ExpectedEntityStorage(anc, 0L, null));

        return expectedEntityStorages;
    }


    public static long getFather(long id) {
        return id + 100;
    }

    public static long getAnc(long id) {
        return id + 100 + 1000;
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
}
