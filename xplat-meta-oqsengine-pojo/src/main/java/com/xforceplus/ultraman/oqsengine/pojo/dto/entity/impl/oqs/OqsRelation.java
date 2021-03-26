package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.Optional;
import java.util.function.Function;

/**
 * 表示一个对象和另一个对象的关联关系.
 * A -> B
 * 这里有两个对象,分别定义为"左"对象和"右对象.
 * 这里左边是关系的持有者(owner),右边是被关联对象.
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class OqsRelation {

    /**
     * 关系唯一标识.
     */
    private long id;

    /**
     * 关系名称 - 目前采用关系ID来填入
     */
    private String code;

    /**
     * 关联中"左"对象的元信息标识.
     */
    private long leftEntityClassId;

    /**
     * 关系中"左"对象的元信息名称.
     */
    private String leftEntityClassCode;

    /**
     * 关系中"右"对象的元信息名称.
     */
    private long rightEntityClassId;

    /**
     * 关系类型 - 使用关系的code填入
     */
    private String relationType;

    /**
     * 是PrimaryKey还是UniqueKey
     */
    private boolean identity;

    /**
     * 根据belongToOwner的值决定关系字段表示.
     * belongToOwner = true 此字段表示在当前EntityClass中的某个字段.
     * false 表示记录在关联的对象上.
     */
    private IEntityField entityField;

    /**
     * true 表示实际关系字段属于"左"对象.
     * false 表示实际关系字段属于"右"对象.
     */
    private boolean belongToOwner;

    /**
     * "右"对象元信息定义的延迟加载方法.
     */
    private Function<Long, Optional<IEntityClass>> rightEntityClassLoader;



    /**
     * 是否强关系
     */
    private boolean isStrong;

    /**
     * 是否是伴生关系
     */
    private boolean isCompanion;

    private long companionRelation;

    public OqsRelation() {

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

    public String getRelationType() {
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

    public void setBelongToOwner(boolean belongToOwner) {
        this.belongToOwner = belongToOwner;
    }

    public boolean isStrong() {
        return isStrong;
    }

    public void setStrong(boolean strong) {
        isStrong = strong;
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

    /**
     * Builder
     */
    public static final class Builder {
        private Long id;
        private String code;
        private Long leftEntityClassId;
        private String leftEntityClassCode;
        private Long rightEntityClassId;
        private String relationType;
        private boolean identity;
        private long entityClassId;
        private Function<Long, Optional<IEntityClass>> entityClassLoader;
        private IEntityField entityField;
        private boolean belongToOwner;
        private boolean isStrong;
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

        public Builder withRelationType(String relationType) {
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

        public OqsRelation.Builder withStrong(boolean isStrong){
            this.isStrong = isStrong;
            return this;
        }

        public OqsRelation.Builder withCompanion(boolean isCompanion){
            this.isCompanion = isCompanion;
            return this;
        }

        public OqsRelation.Builder withCompanionRelation(long relationId){
            this.companionRelation = relationId;
            return this;
        }


        public Builder withRightEntityClassLoader(Function<Long, Optional<IEntityClass>> entityClassLoader) {
            this.entityClassLoader = entityClassLoader;
            return this;
        }

        public OqsRelation build() {
            OqsRelation oqsRelation = new OqsRelation();
            oqsRelation.belongToOwner = this.belongToOwner;
            oqsRelation.relationType = this.relationType;
            oqsRelation.id = this.id;
            oqsRelation.code = this.code;
            oqsRelation.leftEntityClassId = this.leftEntityClassId;
            oqsRelation.rightEntityClassId = this.rightEntityClassId;
            oqsRelation.identity = this.identity;
            oqsRelation.rightEntityClassLoader = this.entityClassLoader;
            oqsRelation.entityField = this.entityField;
            oqsRelation.leftEntityClassCode = this.leftEntityClassCode;
            oqsRelation.belongToOwner = this.belongToOwner;
            oqsRelation.isStrong = this.isStrong;
            oqsRelation.isCompanion = this.isCompanion;
            oqsRelation.companionRelation = this.companionRelation;
            return oqsRelation;
        }
    }
}
