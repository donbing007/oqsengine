package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import java.io.Serializable;
import java.util.Objects;

/**
 * 实例类型信息,只是作为一个标识用以标记出实例的实际 IEntityClass 信息.
 *
 * @author dongbin
 * @version 0.1 2021/2/20 14:20
 * @since 1.8
 */
public class EntityClassRef implements Serializable, Comparable<EntityClassRef> {

    private long id;
    private String code;
    private String profile;

    /**
     * EntityClassRef.
     */
    public EntityClassRef(long id, String code) {
        this.id = id;
        this.code = code;
        this.profile = OqsProfile.UN_DEFINE_PROFILE;
    }

    /**
     * EntityClassRef.
     */
    public EntityClassRef(long id, String code, String profile) {
        this.id = id;
        this.code = code;
        this.profile = profile;
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

    public String getProfile() {
        return profile;
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
        return getId() == that.getId()
            && Objects.equals(getCode(), that.getCode())
            && Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, profile);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EntityClassRef{");
        sb.append("code='").append(code).append('\'');
        sb.append(", id=").append(id);
        sb.append(", profile='").append(profile).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(EntityClassRef o) {
        return Long.compare(id, o.id);
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long entityClassId;
        private String entityClassCode;
        private String profile;

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


        public Builder withEntityClassProfile(String profile) {
            this.profile = profile;
            return this;
        }

        /**
         * build.
         */
        public EntityClassRef build() {
            if (null == profile) {
                profile = OqsProfile.UN_DEFINE_PROFILE;
            }
            return new EntityClassRef(entityClassId, entityClassCode, profile);
        }
    }
}
