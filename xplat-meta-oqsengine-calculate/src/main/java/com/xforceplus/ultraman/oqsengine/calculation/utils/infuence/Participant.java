package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;
import java.util.Optional;

/**
 * 影响树参与者.
 *
 * @author dongbin
 * @version 0.1 2021/10/14 09:57
 * @since 1.8
 */
public class Participant {

    private IEntityClass entityClass;
    private IEntityField field;
    private Object attachment;

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public IEntityField getField() {
        return field;
    }

    public Optional<Object> getAttachment() {
        return Optional.ofNullable(attachment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Participant{");
        sb.append("entityClass=").append(entityClass);
        sb.append(", field=").append(field);
        sb.append(", attachment=").append(attachment);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Participant that = (Participant) o;
        return Objects.equals(entityClass, that.entityClass) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityClass, field);
    }


    /**
     * 构造器.
     */
    public static final class Builder {
        private IEntityClass entityClass;
        private IEntityField field;
        private Object attachment;

        private Builder() {
        }

        public static Builder anParticipant() {
            return new Builder();
        }

        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withField(IEntityField field) {
            this.field = field;
            return this;
        }

        public Builder withAttachment(Object attachment) {
            this.attachment = attachment;
            return this;
        }

        /**
         * 构造.
         *
         * @return Participant.
         */
        public Participant build() {
            Participant participant = new Participant();
            participant.entityClass = this.entityClass;
            participant.attachment = this.attachment;
            participant.field = this.field;
            return participant;
        }
    }
}
