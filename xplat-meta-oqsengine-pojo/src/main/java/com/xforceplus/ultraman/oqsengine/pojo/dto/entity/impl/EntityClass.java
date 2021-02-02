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
     * 关系信息
     */
    private List<Relation> relations;
    /**
     * 子对象结构信息
     */
    private Set<IEntityClass> entityClasses;

    /**
     * 继承的对象类型.
     */
    private IEntityClass extendEntityClass;
    /**
     * 对象属性信息
     */
    private Collection<IEntityField> fields = Collections.emptyList();

    public EntityClass() {
    }

    public EntityClass(long id) {
        this.id = id;
    }

    public EntityClass(long id, String code, Collection<IEntityField> fields) {
        this(id, code, null, null, null, fields);
    }

    public EntityClass(long id, String code, IEntityField... fields) {
        this(id, code, null, null, null, Arrays.asList(fields));
    }

    /**
     * 构造一个新的entity 类型信息.
     *
     * @param id                类型 id.
     * @param code              类型 code.
     * @param relations         关联对象信息.
     * @param entityClasses     类型关联对象类型信息.
     * @param extendEntityClass 继承对象信息.
     * @param fields            属性列表.
     */
    public EntityClass(long id,
                       String code,
                       Collection<Relation> relations,
                       Collection<IEntityClass> entityClasses,
                       IEntityClass extendEntityClass,
                       Collection<IEntityField> fields) {
        this.id = id;
        this.code = code;
        if (relations == null) {
            this.relations = Collections.emptyList();
        } else {
            this.relations = new ArrayList<>(relations);
        }
        if (entityClasses == null) {
            this.entityClasses = Collections.emptySet();
        } else {
            this.entityClasses = new HashSet<>(entityClasses);
        }
        if (fields == null) {
            this.fields = Collections.emptyList();
        } else {
            this.fields = new ArrayList<>(fields);
        }
        this.extendEntityClass = extendEntityClass;
    }

    /**
     * 构造一个新的entity 类型信息.
     *
     * @param id                类型 id.
     * @param code              类型 code.
     * @param relations         关联对象信息.
     * @param entityClasss      类型关联对象类型信息.
     * @param extendEntityClass 继承对象信息.
     * @param fields            属性列表.
     */
    public EntityClass(Long id,
                       String code,
                       String name,
                       Collection<Relation> relations,
                       Collection<IEntityClass> entityClasss,
                       IEntityClass extendEntityClass,
                       Collection<IEntityField> fields) {
        this(id, code, relations, entityClasss, extendEntityClass, fields);
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
        return this.name;
    }

    @Override
    public Collection<Relation> relations() {
        return relations;
    }

    @Override
    public Set<IEntityClass> relationsEntityClasss() {
        return Collections.unmodifiableSet(entityClasses);
    }

    @Override
    public IEntityClass father() {
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
    public boolean isAny() {
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                Objects.equals(entityClasses, that.entityClasses) &&
                Objects.equals(extendEntityClass, that.extendEntityClass) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, relations, entityClasses, extendEntityClass, fields);
    }

    @Override
    public String toString() {
        return "EntityClass{" +
                "id=" + id +
                ", relations=" + relations +
                ", entityClasss=" + entityClasses +
                ", extendEntityClass=" + extendEntityClass +
                ", fields=" + fields +
                '}';
    }
}
