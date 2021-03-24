package com.xforceplus.ultraman.oqsengine.meta.common.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import java.util.Objects;

/**
 * desc :
 * name : RelationStorage
 *
 * @author : xujia
 * date : 2021/2/26
 * @since : 1.8
 */
public class RelationStorage {

    private long id;
    private String name;
    private long entityClassId;
    private long relOwnerClassId;
    private String relOwnerClassName;
    private String relationType;
    private boolean identity;
    private EntityField entityField;
    private boolean belongToOwner;

    public RelationStorage() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
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

    public EntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(EntityField entityField) {
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
        if (o == null || getClass() != o.getClass()) return false;
        RelationStorage that = (RelationStorage) o;
        return id == that.id &&
                entityClassId == that.entityClassId &&
                relOwnerClassId == that.relOwnerClassId &&
                identity == that.identity &&
                Objects.equals(name, that.name) &&
                Objects.equals(relOwnerClassName, that.relOwnerClassName) &&
                Objects.equals(relationType, that.relationType) &&
                Objects.equals(entityField, that.entityField) &&
                Objects.equals(belongToOwner, that.belongToOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, entityClassId, relOwnerClassId, relOwnerClassName, relationType, identity, entityField, belongToOwner);
    }
}
