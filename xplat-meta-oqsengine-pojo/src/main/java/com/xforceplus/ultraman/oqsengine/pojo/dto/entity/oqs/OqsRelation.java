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
     * 是PrimaryKey还是UniqueKey
     */
    private boolean identity;

    /**
     * 数据owner信息
     */
    private long entityClassId;

    private IEntityField entityField;

    private boolean belongToOwner;

    private Function<Long, Optional<IEntityClass>> entityClassLoader;

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

    public boolean isBelongToOwner() {
        return belongToOwner;
    }

    public void setBelongToOwner(boolean belongToOwner) {
        this.belongToOwner = belongToOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OqsRelation)) return false;
        OqsRelation relation = (OqsRelation) o;
        return getEntityClass().id() == relation.getEntityClass().id() &&
                isIdentity() == relation.isIdentity() &&
                Objects.equals(getName(), relation.getName()) &&
                Objects.equals(getRelationType(), relation.getRelationType()) &&
                getEntityField().id() == relation.getEntityField().id();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEntityClass().id(), getRelationType(), isIdentity(), getEntityField().id(), isBelongToOwner());
    }

    @Override
    public String toString() {
        return "Relation{" +
                "name='" + name + '\'' +
                ", entityClassId=" + getEntityClass().id() +
                ", relationType='" + relationType + '\'' +
                ", identity=" + identity +
                ", entityFieldId =" + getEntityField().id() +
                '}';
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
        private boolean belongToOwner;

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

        public OqsRelation.Builder withBelongToOwner(boolean belongToOwner) {
            this.belongToOwner = belongToOwner;
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
            oqsRelation.belongToOwner = this.belongToOwner;
            return oqsRelation;
        }
    }
}
