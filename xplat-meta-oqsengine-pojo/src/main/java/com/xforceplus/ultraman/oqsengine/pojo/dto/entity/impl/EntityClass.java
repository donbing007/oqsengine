package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.*;

/**
 * 关联对象的定义是由 relations 和 entityClass 共同承担的.
 * 两者使用 entityClassId 标识进行联系.
 * relations 表示关联对象的本地字段信息,关联类型.
 * entityClasss 表示联系对象的类型.
 *
 * @author wangzheng
 * @version 1.0 2020/3/26 15:10
 */
public class EntityClass implements IEntityClass {

    /**
     * 元数据boId
     */
    private long id;

    /**
     * 对象名称
     */
    private String name;

    /**
     * 对象code
     */
    private String code;
    /**
     * 元数据版本.
     */
    private int version;

    /**
     * 元信息处于的继承层级
     */
    private int level;

    /**
     * 关系信息
     */
    private List<Relation> relations;
    /**
     * 子对象结构信息
     */
    private Set<IEntityClass> relationsEntityClasses;

    /**
     * 继承的对象类型.
     */
    private IEntityClass father;
    /**
     * 对象属性信息
     */
    private Collection<IEntityField> fields = Collections.emptyList();

    private EntityClass() {
    }

    /**
     * @deprecated 优先使用build模式生成.
     */
    @Deprecated
    public EntityClass(long id) {
        this.id = id;
    }

    /**
     * @deprecated 优先使用build模式生成.
     */
    @Deprecated
    public EntityClass(long id, String code, Collection<IEntityField> fields) {
        this(id, code, null, null, null, fields);
    }

    /**
     * @deprecated 优先使用build模式生成.
     */
    @Deprecated
    public EntityClass(long id, String code, IEntityField... fields) {
        this(id, code, null, null, null, Arrays.asList(fields));
    }

    /**
     * 构造一个新的entity 类型信息.
     *
     * @param id                     类型 id.
     * @param code                   类型 code.
     * @param relations              关联对象信息.
     * @param relationsEntityClasses 类型关联对象类型信息.
     * @param father                 继承对象信息.
     * @param fields                 属性列表.
     * @deprecated 优先使用build模式生成.
     */
    @Deprecated
    public EntityClass(long id,
                       String code,
                       Collection<Relation> relations,
                       Collection<IEntityClass> relationsEntityClasses,
                       IEntityClass father,
                       Collection<IEntityField> fields) {
        this.id = id;
        this.code = code;
        if (relations == null) {
            this.relations = Collections.emptyList();
        } else {
            this.relations = new ArrayList<>(relations);
        }
        if (relationsEntityClasses == null) {
            this.relationsEntityClasses = Collections.emptySet();
        } else {
            this.relationsEntityClasses = new HashSet<>(relationsEntityClasses);
        }
        if (fields == null) {
            this.fields = Collections.emptyList();
        } else {
            this.fields = new ArrayList<>(fields);
        }
        this.father = father;
    }

    /**
     * 构造一个新的entity 类型信息.
     *
     * @param id           类型 id.
     * @param code         类型 code.
     * @param relations    关联对象信息.
     * @param entityClasss 类型关联对象类型信息.
     * @param father       继承对象信息.
     * @param fields       属性列表.
     */
    public EntityClass(Long id,
                       String code,
                       String name,
                       Collection<Relation> relations,
                       Collection<IEntityClass> entityClasss,
                       IEntityClass father,
                       Collection<IEntityField> fields) {
        this(id, code, relations, entityClasss, father, fields);
        this.name = name;
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
    public Collection<Relation> relations() {
        return relations;
    }

    @Override
    public Collection<IEntityClass> relationsEntityClasss() {
        return relationsEntityClasses;
    }

    @Override
    public IEntityClass father() {
        return father;
    }

    @Override
    public Collection<IEntityField> fields() {
        return fields;
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
            version == that.version &&
            level == that.level &&
            Objects.equals(name, that.name) &&
            Objects.equals(code, that.code) &&
            Objects.equals(relations, that.relations) &&
            Objects.equals(relationsEntityClasses, that.relationsEntityClasses) &&
            Objects.equals(father, that.father) &&
            Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, version, level, relations, relationsEntityClasses, father, fields);
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
        sb.append(", relationsEntityClasses=").append(relationsEntityClasses);
        sb.append(", father=").append(father);
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }


    public static final class Builder {
        private long id;
        private String name;
        private String code;
        private int version;
        private int level;
        private List<Relation> relations;
        private Set<IEntityClass> relationsEntityClasses;
        private IEntityClass father;
        private Collection<IEntityField> fields = Collections.emptyList();

        private Builder() {
        }

        public static Builder anEntityClass() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder withRelations(List<Relation> relations) {
            this.relations = relations;
            return this;
        }

        public Builder withRelationsEntityClasses(Set<IEntityClass> relationsEntityClasses) {
            this.relationsEntityClasses = relationsEntityClasses;
            return this;
        }

        public Builder withFather(IEntityClass father) {
            this.father = father;
            return this;
        }

        public Builder withFields(Collection<IEntityField> fields) {
            this.fields = fields;
            return this;
        }

        public Builder withField(IEntityField field) {
            if (Collections.emptyList().getClass().equals(this.fields)) {
                this.fields = new ArrayList<>(fields);
            } else {
                this.fields.add(field);
            }
            return this;
        }

        public EntityClass build() {
            EntityClass entityClass = new EntityClass();
            entityClass.id = id;
            entityClass.code = code;
            entityClass.name = this.name;
            entityClass.level = this.level;
            entityClass.version = this.version;
            entityClass.father = father;
            entityClass.fields = fields;
            entityClass.relationsEntityClasses = this.relationsEntityClasses;
            entityClass.relations = this.relations;
            return entityClass;
        }
    }
}
