package com.xforceplus.ultraman.oqsengine.pojo.dto;

import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class EntityClass implements IEntityClass {
    /**
     * 元数据boId
     */
    private Long id;
    /**
     * 关系信息
     */
    private String relaton;
    /**
     * 子对象结构信息
     */
    private List<EntityClass> entityClasss;
    /**
     * 对象属性信息
     */
    private List<Field> fields;

    @Override
    public Long id() {
        return null;
    }

    @Override
    public String relation() {
        return null;
    }

    @Override
    public List<IEntityClass> entityClasss() {
        return null;
    }

    @Override
    public List<Field> fields() {
        return null;
    }

    public EntityClass() {
    }

    public EntityClass(Long id, String relaton, List<EntityClass> entityClasss, List<Field> fields) {
        this.id = id;
        this.relaton = relaton;
        this.entityClasss = entityClasss;
        this.fields = fields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRelaton() {
        return relaton;
    }

    public void setRelaton(String relaton) {
        this.relaton = relaton;
    }

    public List<EntityClass> getEntityClasss() {
        return entityClasss;
    }

    public void setEntityClasss(List<EntityClass> entityClasss) {
        this.entityClasss = entityClasss;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityClass)) return false;
        EntityClass that = (EntityClass) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getRelaton(), that.getRelaton()) &&
                Objects.equals(getEntityClasss(), that.getEntityClasss()) &&
                Objects.equals(getFields(), that.getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRelaton(), getEntityClasss(), getFields());
    }

    @Override
    public String toString() {
        return "EntityClass{" +
                "id=" + id +
                ", relaton='" + relaton + '\'' +
                ", entityClasss=" + entityClasss +
                ", fields=" + fields +
                '}';
    }
}
