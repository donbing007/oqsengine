package com.xforceplus.ultraman.oqsengine.calculation.utils.infuence;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 * 初始化参与者.
 */
public class InitCalculationParticipant extends AbstractParticipant {

    private IEntityClass sourceEntityClass;
    private IEntityField sourceField;

    public InitCalculationParticipant() {
        super();
    }

    public IEntityClass getSourceEntityClass() {
        return sourceEntityClass;
    }

    public IEntityField getSourceField() {
        return sourceField;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InitCalculationParticipant{");
        sb.append("sourceEntityClass=").append(sourceEntityClass);
        sb.append(", sourceField=").append(sourceField);
        sb.append('}');
        return sb.toString();
    }
}
