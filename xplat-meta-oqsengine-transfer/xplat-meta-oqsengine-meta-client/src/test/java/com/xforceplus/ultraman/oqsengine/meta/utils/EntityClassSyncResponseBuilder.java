package com.xforceplus.ultraman.oqsengine.meta.utils;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.MD5Utils.getMD5;

/**
 * desc :
 * name : EntityClassSyncResponseBuilder
 *
 * @author : xujia
 * date : 2021/2/24
 * @since : 1.8
 */
public class EntityClassSyncResponseBuilder {
    /**
     * 将生成随机的3层父子类结构[爷爷、父亲、儿子]
     * 每层有2个随机的EntityField [String、Long] 各一
     * 每层存在2条关系
     * @param appId
     * @param version
     * @return
     */
    public static EntityClassSyncResponse entityClassSyncResponseGenerator(String appId, int version,
                                                                           boolean withMD5, List<ExpectedEntityStorage> expectedEntityStorages) {

        EntityClassSyncRspProto entityClassSyncRspProto = entityClassSyncRspProtoGenerator(expectedEntityStorages);
        EntityClassSyncResponse.Builder builder = EntityClassSyncResponse.newBuilder()
                .setAppId(appId)
                .setVersion(version + 1)
                .setEntityClassSyncRspProto(entityClassSyncRspProto);
        if (withMD5) {
            builder.setMd5(getMD5(entityClassSyncRspProto.toByteArray()));
        }
        return builder.build();
    }

    public static EntityClassSyncRspProto entityClassSyncRspProtoGenerator(List<ExpectedEntityStorage> expectedEntityStorages) {
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
                .setEntityFieldCode(fieldId + "_name")
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
