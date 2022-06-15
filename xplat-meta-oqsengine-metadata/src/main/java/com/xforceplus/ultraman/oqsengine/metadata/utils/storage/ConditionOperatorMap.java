package com.xforceplus.ultraman.oqsengine.metadata.utils.storage;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.DomainCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import java.util.Arrays;

/**
 * Created by justin.xu on 06/2022.
 *
 * @since 1.8
 */
public enum ConditionOperatorMap {
    LIKE(DomainCondition.Operator.LIKE, ConditionOperator.LIKE),
    EQUALS(DomainCondition.Operator.EQUALS, ConditionOperator.EQUALS),
    NOT_EQUALS(DomainCondition.Operator.NOT_EQUALS, ConditionOperator.NOT_EQUALS),
    GREATER_THAN(DomainCondition.Operator.GREATER_THAN, ConditionOperator.GREATER_THAN),
    GREATER_THAN_EQUALS(DomainCondition.Operator.GREATER_THAN_EQUALS, ConditionOperator.GREATER_THAN_EQUALS),
    LESS_THAN(DomainCondition.Operator.LESS_THAN, ConditionOperator.LESS_THAN),
    LESS_THAN_EQUALS(DomainCondition.Operator.LESS_THAN_EQUALS, ConditionOperator.LESS_THAN_EQUALS),
    MULTIPLE_EQUALS(DomainCondition.Operator.MULTIPLE_EQUALS, ConditionOperator.MULTIPLE_EQUALS),
    UN_KNOWN(null, ConditionOperator.UNKNOWN);

    private DomainCondition.Operator domainCondition;
    private ConditionOperator conditionOperator;

    ConditionOperatorMap(
        DomainCondition.Operator domainCondition,
        ConditionOperator conditionOperator) {
        this.domainCondition = domainCondition;
        this.conditionOperator = conditionOperator;
    }

    /**
     * 实例化.
     *
     * @param operator 操作.
     * @return 操作哈希映射.
     */
    public static ConditionOperatorMap instance(DomainCondition.Operator operator) {
        return null == operator ? UN_KNOWN :
            Arrays.stream(ConditionOperatorMap.values())
                .filter(c -> c.domainCondition.equals(operator)).findFirst()
                .orElse(UN_KNOWN);
    }

    public DomainCondition.Operator getDomainCondition() {
        return domainCondition;
    }

    public ConditionOperator getConditionOperator() {
        return conditionOperator;
    }
}
