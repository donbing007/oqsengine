package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 实例类型信息,只是作为一个标识用以标记出实例的实际 IEntityClass 信息.
 *
 * @author dongbin
 * @version 0.1 2021/2/20 14:20
 * @since 1.8
 */
public class EntityClassRef implements Serializable {

    private long id;
    private String code;

    public EntityClassRef(long id, String code) {
        this.id = id;
        this.code = code;
    }

    /**
     * 实例类型信息标识.
     *
     * @return 标识.
     */
    public long getId() {
        return id;
    }

    /**
     * 实例类型信息代码.
     *
     * @return 代码.
     */
    public String getCode() {
        return code;
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
        return getId() == that.getId() && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCode());
    }

    /**
     * builder.
     */
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
