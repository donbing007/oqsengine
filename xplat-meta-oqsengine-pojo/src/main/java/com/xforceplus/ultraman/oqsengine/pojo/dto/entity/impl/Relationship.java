package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 表示一个对象和另一个对象的关联关系.
 * A -> B
 * 这里有两个对象,分别定义为"左"对象和"右对象.
 * 这里左边是关系的持有者(owner),右边是被关联对象.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class Relationship {

    /**
     * 关系类型.
     */
    public static enum RelationType {
        UNKNOWN(0),
        ONE_TO_ONE(1),
        ONE_TO_MANY(2),
        MANY_TO_ONE(3),
        MANY_TO_MANY(4),
        MULTI_VALUES(5);

        private int value;

        private RelationType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /**
         * 获得实例.
         *
         * @param value 字面量.
         * @return 实例.
         */
        public static RelationType getInstance(int value) {
            for (RelationType t : RelationType.values()) {
                if (t.getValue() == value) {
                    return t;
                }
            }

            return RelationType.UNKNOWN;
        }

    }

    /*
     * 关系唯一标识.
     */
    private long id;

    /*
     * 关系名称 - 目前采用关系ID来填入
     */
    private String code;

    /*
     * 关联中"左"对象的元信息标识.
     */
    private long leftEntityClassId;

    /*
     * 关系中"左"对象的元信息名称.
     */
    private String leftEntityClassCode;

    /*
     * 关系中"右"对象的元信息名称.
     */
    private long rightEntityClassId;

    /*
     * 关系类型 - 使用关系的code填入
     */
    private RelationType relationType;

    /*
     * 是PrimaryKey还是UniqueKey
     */
    private boolean identity;

    /*
     * 根据belongToOwner的值决定关系字段表示.
     * belongToOwner = true 此字段表示在当前EntityClass中的某个字段.
     * false 表示记录在关联的对象上.
     */
    private IEntityField entityField;

    /*
     * true 表示实际关系字段属于"左"对象.
     * false 表示实际关系字段属于"右"对象.
     */
    private boolean belongToOwner;

    /*
     * true 表示为强关系.
     * false 表示为弱关系.
     */
    private boolean strong;

    /*
     * "右"对象元信息定义的延迟加载方法.
     */
    private Function<Long, Optional<IEntityClass>> rightEntityClassLoader;

    /*
     * 是否是伴生关系
     */
    private boolean isCompanion;

    private long companionRelation;

    public Relationship() {

    }

    /**
     * 获取到关系中相关联的entityClass实例.即关系"右"对象的元信息定义实例.
     *
     * @return entityClass 实例.
     */
    public IEntityClass getRightEntityClass() {
        Optional<IEntityClass> entityClassOp = rightEntityClassLoader.apply(rightEntityClassId);
        return entityClassOp.orElse(null);
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public long getLeftEntityClassId() {
        return leftEntityClassId;
    }

    public String getLeftEntityClassCode() {
        return leftEntityClassCode;
    }

    public long getRightEntityClassId() {
        return rightEntityClassId;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public boolean isIdentity() {
        return identity;
    }

    public IEntityField getEntityField() {
        return entityField;
    }

    public boolean isBelongToOwner() {
        return belongToOwner;
    }

    public boolean isSelfRelation(long entityClassId) {
        return entityClassId == leftEntityClassId && belongToOwner;
    }

    public boolean isStrong() {
        return strong;
    }

    public void setBelongToOwner(boolean belongToOwner) {
        this.belongToOwner = belongToOwner;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Relationship that = (Relationship) o;
        return id == that.id
            && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    /**
     * Builder.
     */
    public static final class Builder {
        private Long id;
        private String code;
        private Long leftEntityClassId;
        private String leftEntityClassCode;
        private Long rightEntityClassId;
        private RelationType relationType;
        private Boolean identity;
        private IEntityField entityField;
        private Boolean belongToOwner;
        private boolean strong;
        private long entityClassId;
        private Function<Long, Optional<IEntityClass>> entityClassLoader;
        private boolean isCompanion;
        private long companionRelation;

        private Builder() {
        }

        public static Builder anOqsRelation() {
            return new Builder();
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withLeftEntityClassId(long leftEntityClassId) {
            this.leftEntityClassId = leftEntityClassId;
            return this;
        }

        public Builder withLeftEntityClassCode(String leftEntityClassCode) {
            this.leftEntityClassCode = leftEntityClassCode;
            return this;
        }

        public Builder withRightEntityClassId(long rightEntityClassId) {
            this.rightEntityClassId = rightEntityClassId;
            return this;
        }

        public Builder withRelationType(RelationType relationType) {
            this.relationType = relationType;
            return this;
        }

        public Builder withIdentity(boolean identity) {
            this.identity = identity;
            return this;
        }

        public Builder withEntityField(IEntityField entityField) {
            this.entityField = entityField;
            return this;
        }

        public Builder withBelongToOwner(boolean belongToOwner) {
            this.belongToOwner = belongToOwner;
            return this;
        }

        public Relationship.Builder withCompanion(boolean isCompanion) {
            this.isCompanion = isCompanion;
            return this;
        }

        public Relationship.Builder withCompanionRelation(long relationId) {
            this.companionRelation = relationId;
            return this;
        }


        public Builder withStrong(boolean strong) {
            this.strong = strong;
            return this;
        }

        public Builder withRightEntityClassLoader(Function<Long, Optional<IEntityClass>> entityClassLoader) {
            this.entityClassLoader = entityClassLoader;
            return this;
        }

        /**
         * 构造 OqsRelation 实例.
         *
         * @return 实例.
         */
        public Relationship build() {
            Relationship relationship = new Relationship();
            relationship.belongToOwner = this.belongToOwner;
            relationship.relationType = this.relationType;
            relationship.id = this.id;
            relationship.code = this.code;
            relationship.leftEntityClassId = this.leftEntityClassId;
            relationship.rightEntityClassId = this.rightEntityClassId;
            relationship.identity = this.identity;
            relationship.rightEntityClassLoader = this.entityClassLoader;
            relationship.entityField = this.entityField;
            relationship.leftEntityClassCode = this.leftEntityClassCode;
            relationship.strong = this.strong;
            relationship.belongToOwner = this.belongToOwner;
            relationship.isCompanion = this.isCompanion;
            relationship.companionRelation = this.companionRelation;
            return relationship;
        }
    }
}
