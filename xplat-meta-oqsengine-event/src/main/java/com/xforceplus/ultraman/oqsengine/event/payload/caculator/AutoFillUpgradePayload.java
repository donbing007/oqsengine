package com.xforceplus.ultraman.oqsengine.event.payload.caculator;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.io.Serializable;
import java.util.Objects;

/**
 * AutoFill类型的payload.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/13
 * @since 1.8
 */
public class AutoFillUpgradePayload implements Serializable {

    private IEntityField entityField;

    public AutoFillUpgradePayload(IEntityField entityField) {
        this.entityField = entityField;
    }

    public IEntityField getEntityField() {
        return entityField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AutoFillUpgradePayload that = (AutoFillUpgradePayload) o;
        return Objects.equals(entityField, that.entityField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityField);
    }

    @Override
    public String toString() {
        return "AutoFillUpgradePayload{"
            + "entityField=" + entityField
            + '}';
    }
}
