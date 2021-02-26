package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;

import java.util.*;

/**
 * desc :
 * name : OqsEntityClass
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class OqsEntityClass implements IEntityClass {

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
    private List<OqsRelation> relations;

    /**
     * 继承的对象类型.
     */
    private IEntityClass father;
    /**
     * 对象属性信息
     */
    private Collection<IEntityField> fields = Collections.emptyList();


    private OqsEntityClass() {
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
        return null;
    }

    @Override
    public Collection<OqsRelation> oqsRelations() {
        return relations;
    }

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
        List<IEntityClass> familyList = new ArrayList<>();
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
        if (null != father) {
            List<IEntityField> entityFields = new ArrayList<>();
            entityFields.addAll(fields);
            entityFields.addAll(father.fields());
            return entityFields;
        } else {
            return fields;
        }
    }

    @Override
    public Optional<IEntityField> field(String name) {
        Optional<IEntityField> entityFieldOp =
            fields.stream().filter(f -> name.equals(f.name())).findFirst();

        //  找到或者没有父类
        if (entityFieldOp.isPresent() || null == father) {
            return entityFieldOp;
        }
        //  从父类找
        return father.field(name);
    }

    @Override
    public Optional<IEntityField> field(long id) {
        Optional<IEntityField> entityFieldOp =
            fields.stream().filter(f -> id == f.id()).findFirst();

        //  找到或者没有父类
        if (entityFieldOp.isPresent() || null == father) {
            return entityFieldOp;
        }
        //  从父类找
        return father.field(id);
    }

    /**
     * Builder
     */
    public static final class Builder {
        private long id;
        private String name;
        private String code;
        private int version;
        private int level;
        private List<OqsRelation> relations;
        private IEntityClass father;
        private Collection<IEntityField> fields = Collections.emptyList();

        private Builder() {
        }

        public static OqsEntityClass.Builder anEntityClass() {
            return new OqsEntityClass.Builder();
        }

        public OqsEntityClass.Builder withId(long id) {
            this.id = id;
            return this;
        }

        public OqsEntityClass.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public OqsEntityClass.Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public OqsEntityClass.Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public OqsEntityClass.Builder withLevel(int level) {
            this.level = level;
            return this;
        }

        public OqsEntityClass.Builder withRelations(List<OqsRelation> relations) {
            this.relations = relations;
            return this;
        }

        public OqsEntityClass.Builder withFather(IEntityClass father) {
            this.father = father;
            return this;
        }

        public OqsEntityClass.Builder withFields(Collection<IEntityField> fields) {
            this.fields = fields;
            return this;
        }

        public OqsEntityClass.Builder withField(IEntityField field) {
            if (Collections.emptyList().getClass().equals(this.fields.getClass())) {
                this.fields = new ArrayList<>(fields);
            }

            this.fields.add(field);

            return this;
        }

        public OqsEntityClass build() {
            OqsEntityClass entityClass = new OqsEntityClass();
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
