package com.xforceplus.ultraman.oqsengine.event.payload.meta;

import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class MetaChangePayLoad implements Serializable {
    /**
     * appId.
     */
    private String appId;

    /**
     * version.
     */
    private int version;

    /**
     * 当前app下发生变化的entityClass集合.
     * 可以通过EntityChange->entityClassId 获取(metadata.Load) EntityClass.
     * 可以通过EntityChange->entityClassId 获取(metadata.ProfileLoads) ProfileEntityClass列表.
     */
    private List<EntityChange> entityChanges;

    /**
     * construct fx.
     */
    public MetaChangePayLoad(String appId, int version) {
        this.appId = appId;
        this.version = version;
        this.entityChanges = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public String getAppId() {
        return appId;
    }

    public List<EntityChange> getEntityChanges() {
        return entityChanges;
    }

    /**
     * entity的change对象.
     */
    public static class EntityChange {
        /**
         * entityClassId.
         */
        private Long entityClassId;
        /**
         * 变更的Field列表.
         */
        private List<FieldChange> fieldChanges;
        /**
         * 变更的Relation列表.
         */
        private List<RelationChange> relationChanges;

        /**
         * construct fx.
         */
        public EntityChange(Long entityClassId) {
            this.entityClassId = entityClassId;
            this.fieldChanges = new ArrayList<>();
            this.relationChanges = new ArrayList<>();
        }

        public Long getEntityClassId() {
            return entityClassId;
        }

        public List<FieldChange> getFieldChanges() {
            return fieldChanges;
        }

        public List<RelationChange> getRelationChanges() {
            return relationChanges;
        }

    }

    /**
     * field的change对象.
     */
    public static class FieldChange {
        /**
         * fieldId.
         */
        private Long fieldId;
        /**
         * 操作类型, 新增/修改/删除.
         */
        private OperationType op;
        /**
         * 该field属于哪个profile.
         */
        private String profile;

        /**
         * construct fx.
         */
        public FieldChange(Long fieldId, OperationType op, String profile) {
            this.fieldId = fieldId;
            this.op = op;
            this.profile = profile;
        }

        public Long getFieldId() {
            return fieldId;
        }

        public OperationType getOp() {
            return op;
        }

        public String getProfile() {
            return profile;
        }
    }

    /**
     * relation的change对象.
     */
    public static class RelationChange {
        /**
         * relationId.
         */
        protected Long relationId;
        /**
         * 操作类型, 新增/修改/删除.
         */
        protected OperationType op;
        /**
         * 该relation属于哪个profile.
         */
        protected String profile;

        /**
         * construct fx.
         */
        public RelationChange(Long relationId, OperationType op, String profile) {
            this.relationId = relationId;
            this.op = op;
            this.profile = profile;
        }

        public Long getRelationId() {
            return relationId;
        }

        public OperationType getOp() {
            return op;
        }

        public String getProfile() {
            return profile;
        }
    }
}
