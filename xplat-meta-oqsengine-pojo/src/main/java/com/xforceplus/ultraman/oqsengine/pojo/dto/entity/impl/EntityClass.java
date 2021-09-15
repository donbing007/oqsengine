package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 一个元信息定义,OQS内部使用的对象元信息定义.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class EntityClass implements IEntityClass {

    /*
     * 元数据boId
     */
    private long id;

    /*
     * 对象名称
     */
    private String name;

    /*
     * 对象code
     */
    private String code;
    /*
     * 元数据版本.
     */
    private int version;

    /*
     * 元信息处于的继承层级
     */
    private int level;

    /*
     * 关系信息
     */
    private Collection<Relationship> relations;

    /*
     * 继承的对象类型.
     */
    private IEntityClass father;
    /*
     * 对象属性信息
     */
    private Collection<IEntityField> fields = Collections.emptyList();


    private EntityClass() {
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
    public String name() {
        return name;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public Collection<Relationship> relationship() {

        List<Relationship> relations = new ArrayList<>();

        if (this.relations != null) {
            relations.addAll(this.relations);
        }

        if (null != father) {
            relations.addAll(father.relationship());
        }

        return relations;
    }

    @Deprecated
    @Override
    public Collection<IEntityClass> relationsEntityClasss() {
        return null;
    }

    @Override
    public Optional<IEntityClass> father() {
        return Optional.ofNullable(father);
    }

    @Override
    public Collection<IEntityClass> family() {
        List<IEntityClass> familyList = new ArrayList<>(level);
        Optional<IEntityClass> current = Optional.of(this);
        while (current.isPresent()) {
            familyList.add(0, current.get());
            current = current.get().father();
        }

        return familyList;
    }

    @Override
    public Collection<IEntityField> fields() {
        //  获取自己 + 父类的所有IEntityField
        List<IEntityField> entityFields = new ArrayList<>(fields);

        // TODO: 不知道为什么有这段,先注解.半年后将被确认删除. by donbing 2021-8-24
        //if (null != relations) {
        //    relations.forEach(
        //        r -> {
        //            if (null != r && r.isSelfRelation(id)) {
        //                entityFields.add(r.getEntityField());
        //            }
        //        }
        //    );
        //}

        if (null != father) {
            entityFields.addAll(father.fields());
        }

        return entityFields;
    }

    @Override
    public Optional<IEntityField> field(String name) {
        Optional<IEntityField> entityFieldOp =
            fields.stream().filter(f -> name.equals(f.name())).findFirst();

        //  找到
        if (entityFieldOp.isPresent()) {
            return entityFieldOp;
        }

        // TODO: 不知道为什么有这段,先注解.半年后将被确认删除. by donbing 2021-8-24
        //else {
        //    if (relations != null) {
        //        //  从关系中找
        //        for (Relationship relation : relations) {
        //            if (null != relation
        //                && relation.isSelfRelation(this.id)
        //                && relation.getEntityField().name().equals(name)) {
        //                return Optional.of(relation.getEntityField());
        //            }
        //        }
        //    }
        //}

        //  从父类找
        if (null != father) {
            return father.field(name);
        }
        return entityFieldOp;
    }

    @Override
    public Optional<IEntityField> field(long id) {
        Optional<IEntityField> entityFieldOp =
            fields.stream().filter(f -> id == f.id()).findFirst();

        if (entityFieldOp.isPresent()) {
            return entityFieldOp;
        } else {
            if (relations != null) {
                //  从关系中找
                for (Relationship relation : relations) {
                    if (null != relation
                        && relation.isSelfRelation(this.id)
                        && relation.getEntityField().id() == id) {
                        return Optional.of(relation.getEntityField());
                    }
                }
            }
        }

        //  从父类找
        if (null != father) {
            return father.field(id);
        }
        return entityFieldOp;
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
        return id == that.id
            && version == that.version
            && level == that.level
            && Objects.equals(name, that.name)
            && Objects.equals(code, that.code)
            && Objects.equals(father, that.father)
            && Objects.equals(relations, that.relations)
            && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, version, level, relations, father, fields);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EntityClass{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", version=").append(version);
        sb.append(", level=").append(level);
        sb.append(", relations=").append(relations);
        sb.append(", father=").append(father);
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private long id;
        private String name;
        private String code;
        private int version;
        private int level;
        private Collection<Relationship> relations = Collections.emptyList();
        private IEntityClass father;
        private Collection<IEntityField> fields = Collections.emptyList();

        private Builder() {
        }

        public static EntityClass.Builder anEntityClass() {
            return new EntityClass.Builder();
        }

        public EntityClass.Builder withId(long id) {
            this.id = id;
            return this;
        }

        public EntityClass.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public EntityClass.Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public EntityClass.Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public EntityClass.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public EntityClass.Builder withRelations(Collection<Relationship> relations) {
            this.relations = relations;
            return this;
        }

        public EntityClass.Builder withFather(IEntityClass father) {
            this.father = father;
            return this;
        }

        public EntityClass.Builder withFields(Collection<IEntityField> fields) {
            this.fields = fields;
            return this;
        }

        /**
         * 增加新的字段.
         *
         * @param field 目标字段.
         * @return 当前构造器.
         */
        public EntityClass.Builder withField(IEntityField field) {
            if (Collections.emptyList().getClass().equals(this.fields.getClass())) {
                this.fields = new ArrayList<>(fields);
            }

            this.fields.add(field);

            return this;
        }

        /**
         * 构造一个OqsEntityClass 实例.
         *
         * @return 实例.
         */
        public EntityClass build() {
            EntityClass entityClass = new EntityClass();
            entityClass.id = id;
            entityClass.code = code;
            entityClass.name = this.name;
            entityClass.level = this.level;
            entityClass.version = this.version;
            entityClass.father = father;
            entityClass.fields = fields;
            entityClass.relations = this.relations;
            return entityClass;
        }
    }
}
