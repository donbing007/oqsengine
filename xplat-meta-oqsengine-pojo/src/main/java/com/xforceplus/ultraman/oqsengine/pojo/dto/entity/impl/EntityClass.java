package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.*;

public class EntityClass implements IEntityClass {
    /**
     * 元数据boId
     */
    private long id;

    private String code;
    /**
     * 关系信息
     */
    private List<Relation> relations;
    /**
     * 子对象结构信息
     */
    private List<IEntityClass> entityClasss = Collections.emptyList();

    /**
     * 继承的对象类型.
     */
    private IEntityClass extendEntityClass;
    /**
     * 对象属性信息
     */
    private List<IEntityField> fields = Collections.emptyList();

    public EntityClass() {
    }

    public EntityClass(Long id,
                       String code,
                       List<Relation> relations,
                       List<IEntityClass> entityClasss,
                       IEntityClass extendEntityClass,
                       List<Field> fields) {
        this.id = id;
        this.code = code;
        this.relations = relations;
        this.entityClasss = entityClasss;
        this.extendEntityClass = extendEntityClass;
        this.fields = new ArrayList<>(fields);
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public List<Relation> relations() {
        return relations;
    }

    @Override
    public List<IEntityClass> entityClasss() {
        return new ArrayList<>(entityClasss);
    }

    @Override
    public IEntityClass extendEntityClass() {
        return extendEntityClass;
    }

    @Override
    public List<IEntityField> fields() {
        return new ArrayList<>(fields);
    }

    @Override
    public Optional<IEntityField> field(String name) {
        return fields.stream().filter(f -> name.equals(f.name())).findFirst();
    }

    @Override
    public Optional<IEntityField> field(long id) {
        return fields.stream().filter(f -> id == f.id()).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityClass)) {
            return false;
        }
        EntityClass that = (EntityClass) o;
        return id == that.id &&
            Objects.equals(relations, that.relations) &&
            Objects.equals(entityClasss, that.entityClasss) &&
            Objects.equals(extendEntityClass, that.extendEntityClass) &&
            Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, relations, entityClasss, extendEntityClass, fields);
    }

    @Override
    public String toString() {
        return "EntityClass{" +
            "id=" + id +
            ", relations='" + relations + '\'' +
            ", entityClasss=" + entityClasss +
            ", extendEntityClass=" + extendEntityClass +
            ", fields=" + fields +
            '}';
    }
}
