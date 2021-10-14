package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Objects;

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

    public Participant(IEntityClass entityClass, IEntityField field, Object attachment) {
        this.entityClass = entityClass;
        this.field = field;
        this.attachment = attachment;
    }

    public IEntityClass getEntityClass() {
        return entityClass;
    }

    public IEntityField getField() {
        return field;
    }

    public Object getAttachment() {
        return attachment;
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
}
