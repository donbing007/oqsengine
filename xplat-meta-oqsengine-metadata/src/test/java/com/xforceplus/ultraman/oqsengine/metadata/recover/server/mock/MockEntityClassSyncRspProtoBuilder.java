package com.xforceplus.ultraman.oqsengine.metadata.recover.server.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * desc :.
 * name : MockEntityClassSyncRspProtoBuilder
 *
 * @author : xujia 2021/3/1
 * @since : 1.8
 */
public class MockEntityClassSyncRspProtoBuilder {


    /**
     * 生成同步响应.
     */
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

    /**
     * 生成.
     */
    public static EntityClassInfo entityClassInfo(long id, long father, int level) {
        List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();
        entityFieldInfos.add(entityFieldInfo(id, "LONG"));
        entityFieldInfos.add(entityFieldInfo(id + 1, "STRING"));

        List<RelationInfo> relationInfos = new ArrayList<>();

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

    /**
     * 生成.
     */
    public static EntityFieldInfo entityFieldInfo(long id, String fieldType) {
        return EntityFieldInfo.newBuilder()
            .setId(id)
            .setName(id + "_name")
            .setCname(id + "_cname")
            .setFieldType(fieldType)
            .setDictId(id + "_dictId")
            .setFieldConfig(fieldConfig(true, 1))
            .build();
    }

    /**
     * 生成.
     */
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
                .setFieldType("LONG")
                .setName(fieldId + "_name")
                .setFieldConfig(FieldConfig.newBuilder().setSearchable(true).build())
                .build())
            .setBelongToOwner(id % 2 == 0)
            .build();

    }

    /**
     * 生成.
     */
    public static FieldConfig fieldConfig(boolean searchable, int systemFieldType) {
        return FieldConfig.newBuilder()
            .setSearchable(searchable)
            .setIsRequired(true)
            .setMetaFieldSense(systemFieldType)
            .build();

    }

    /**
     * 生成.
     */
    public static List<ExpectedEntityStorage> mockSelfFatherAncestorsGenerate(long id) {
        List<MockEntityClassSyncRspProtoBuilder.ExpectedEntityStorage> expectedEntityStorages = new ArrayList<>();

        long father = getFather(id);
        long anc = getAnc(id);

        expectedEntityStorages
            .add(new MockEntityClassSyncRspProtoBuilder.ExpectedEntityStorage(id, father, Arrays.asList(father, anc)));
        expectedEntityStorages.add(
            new MockEntityClassSyncRspProtoBuilder.ExpectedEntityStorage(father, anc, Collections.singletonList(anc)));
        expectedEntityStorages.add(new MockEntityClassSyncRspProtoBuilder.ExpectedEntityStorage(anc, 0L, null));

        return expectedEntityStorages;
    }


    public static long getFather(long id) {
        return id + 100;
    }

    public static long getAnc(long id) {
        return id + 100 + 1000;
    }


    /**
     * 储存表示.
     */
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
