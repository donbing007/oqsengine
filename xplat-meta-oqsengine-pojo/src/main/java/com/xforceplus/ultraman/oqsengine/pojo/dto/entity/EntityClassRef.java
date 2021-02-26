package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.util.Objects;

/**
 * 实例类型信息,只是作为一个标识用以标记出实例的实际 IEntityClass 信息.
 *
 * @author dongbin
 * @version 0.1 2021/2/20 14:20
 * @since 1.8
 */
public class EntityClassRef {

    private long entityClassId;
    private String entityClassCode;

    public EntityClassRef(long entityClassId, String entityClassCode) {
        this.entityClassId = entityClassId;
        this.entityClassCode = entityClassCode;
    }

    /**
     * 实例类型信息标识.
     *
     * @return 标识.
     */
    public long entityClassId() {
        return entityClassId;
    }

    /**
     * 实例类型信息代码.
     *
     * @return 代码.
     */
    public String entityClassCode() {
        return entityClassCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityClassRef)) {
            return false;
        }
        EntityClassRef that = (EntityClassRef) o;
        return entityClassId == that.entityClassId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClassId);
    }

    public static final class Builder {
        private long entityClassId;
        private String entityClassCode;

        private Builder() {
        }

        public static Builder anEntityClassRef() {
            return new Builder();
        }

        public Builder withEntityClassId(long entityClassId) {
            this.entityClassId = entityClassId;
            return this;
        }

        public Builder withEntityClassCode(String entityClassCode) {
            this.entityClassCode = entityClassCode;
            return this;
        }

        public EntityClassRef build() {
            return new EntityClassRef(entityClassId, entityClassCode);
        }
    }
}
