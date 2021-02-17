package com.xforceplus.ultraman.oqsengine.metadata.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

/**
 * desc :
 * name : RelationStorage
 *
 * @author : xujia
 * date : 2021/2/16
 * @since : 1.8
 */
public class RelationStorage {

    private Long id;

    private String name;

    private long entityClassId;

    private String entityClassName;

    private long relOwnerClassId;

    private String relOwnerClassName;

    private String relationType;

    private boolean identity;

    private EntityField entityField;

    public Relation toRelation() {
        Relation relation = new Relation();
        relation.setId(id);
        relation.setName(name);
        relation.setEntityClassId(entityClassId);
        relation.setName(entityClassName);
        relation.setRelOwnerClassId(relOwnerClassId);
        relation.setRelOwnerClassName(relOwnerClassName);
        relation.setRelationType(relationType);
        relation.setIdentity(identity);
        relation.setEntityField(entityField);

        return relation;
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

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
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
}
