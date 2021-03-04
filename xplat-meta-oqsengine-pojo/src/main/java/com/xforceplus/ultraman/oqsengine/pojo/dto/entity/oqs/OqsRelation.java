package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * desc :
 * name : OqsRelation
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class OqsRelation {

    private Long id;

    /**
     * 关系名称 - 目前采用关系ID来填入
     */
    private String name;

    private long relOwnerClassId;

    private String relOwnerClassName;

    /**
     * 关系类型 - 使用关系的code填入
     */
    private String relationType;

    /**
     * 是否使用主键作为关系字段 - true代表是。- 目前只支持主键模式
     * false表示使用对象的其他唯一属性来定义
     */
    private boolean identity;

    /**
     * 关联对象Id
     */
    private long entityClassId;

    private long fieldOwner;

    private IEntityField entityField;

    private Function<Long, Optional<IEntityClass>> entityClassLoader;

    /**
     * 是否强关系
     */
    private boolean isStrong;

    /**
     * 是否是伴生关系
     */
    private boolean isCompanion;

    private long companionRelation;

    public OqsRelation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityClassName() {
        IEntityClass entityClass = getEntityClass();
        return null != entityClass ? entityClass.name() : "";
    }

    public long getFieldOwner() {
        return fieldOwner;
    }

    public void setFieldOwner(long fieldOwner) {
        this.fieldOwner = fieldOwner;
    }

    public long getRelOwnerClassId() {
        return relOwnerClassId;
    }

    public void setRelOwnerClassId(long relOwnerClassId) {
        this.relOwnerClassId = relOwnerClassId;
    }

    public String getRelOwnerClassName() {
        return relOwnerClassName;
    }

    public void setRelOwnerClassName(String relOwnerClassName) {
        this.relOwnerClassName = relOwnerClassName;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public IEntityClass getEntityClass() {
        Optional<IEntityClass> entityClassOp = entityClassLoader.apply(entityClassId);
        return entityClassOp.orElse(null);
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public IEntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(IEntityField entityField) {
        this.entityField = entityField;
    }

    public Function<Long, Optional<IEntityClass>> getEntityClassLoader() {
        return entityClassLoader;
    }

    public void setEntityClassLoader(Function<Long, Optional<IEntityClass>> entityClassLoader) {
        this.entityClassLoader = entityClassLoader;
    }

    public boolean isStrong() {
        return isStrong;
    }

    public void setStrong(boolean strong) {
        isStrong = strong;
    }

    public boolean isCompanion() {
        return isCompanion;
    }

    public void setCompanion(boolean companion) {
        isCompanion = companion;
    }

    public long getCompanionRelation() {
        return companionRelation;
    }

    public void setCompanionRelation(long companionRelation) {
        this.companionRelation = companionRelation;
    }

    /**
     * a relation is differ from id
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OqsRelation that = (OqsRelation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Builder
     */
    public static final class Builder {
        private Long id;
        private String name;
        private long relOwnerClassId;
        private String relOwnerClassName;
        private String relationType;
        private boolean identity;
        private long entityClassId;
        private Function<Long, Optional<IEntityClass>> entityClassLoader;
        private IEntityField entityField;
        private boolean isStrong;
        private boolean isCompanion;
        private long companionRelation;
        private long fieldOwner;

        private Builder() {
        }

        public static OqsRelation.Builder anOqsRelation() {
            return new OqsRelation.Builder();
        }

        public OqsRelation.Builder withId(long id) {
            this.id = id;
            return this;
        }

        public OqsRelation.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public OqsRelation.Builder withRelOwnerClassId(long relOwnerClassId) {
            this.relOwnerClassId = relOwnerClassId;
            return this;
        }

        public OqsRelation.Builder withRelOwnerClassName(String relOwnerClassName) {
            this.relOwnerClassName = relOwnerClassName;
            return this;
        }

        public OqsRelation.Builder withRelationType(String relationType) {
            this.relationType = relationType;
            return this;
        }

        public OqsRelation.Builder withIdentity(boolean identity) {
            this.identity = identity;
            return this;
        }

        public OqsRelation.Builder withFunction(Function<Long, Optional<IEntityClass>> entityClassLoader) {
            this.entityClassLoader = entityClassLoader;
            return this;
        }

        public OqsRelation.Builder withEntityClassId(long entityClassId) {
            this.entityClassId = entityClassId;
            return this;
        }

        public OqsRelation.Builder withEntityField(IEntityField entityField) {
            this.entityField = entityField;
            return this;
        }

        public OqsRelation.Builder withStrong(boolean isStrong){
            this.isStrong = isStrong;
            return this;
        }

        public OqsRelation.Builder withCompanion(boolean isCompanion){
            this.isCompanion = isCompanion;
            return this;
        }

        public OqsRelation.Builder withCompanionRelation(long relationId){
            this.companionRelation = relationId;
            return this;
        }

        public OqsRelation.Builder withFieldOwner(long fieldOwner){
            this.fieldOwner = fieldOwner;
            return this;
        }

        public OqsRelation build() {
            OqsRelation oqsRelation = new OqsRelation();
            oqsRelation.id = this.id;
            oqsRelation.name = this.name;
            oqsRelation.relOwnerClassId = this.relOwnerClassId;
            oqsRelation.relOwnerClassName = this.relOwnerClassName;
            oqsRelation.relationType = this.relationType;
            oqsRelation.identity = this.identity;
            oqsRelation.entityClassId = this.entityClassId;
            oqsRelation.entityClassLoader = entityClassLoader;
            oqsRelation.entityField = this.entityField;
            oqsRelation.isStrong = this.isStrong;
            oqsRelation.isCompanion = this.isCompanion;
            oqsRelation.companionRelation = this.companionRelation;
            oqsRelation.fieldOwner = this.fieldOwner;
            return oqsRelation;
        }
    }
}
