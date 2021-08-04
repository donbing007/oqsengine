package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.Comparator;

/**
 * Created by justin.xu on 07/2021.
 * 这是一个对IValues重新排序的Comparator，
 * 按照
 * 1.calculationType.priority
 * 2.type都为formula时按formula.level
 * 从低到高排序.
 *
 * @since 1.8
 */
public class CalculationComparator implements Comparator<IEntityField> {

    @Override
    public int compare(IEntityField o1, IEntityField o2) {
        //  当优先级相同时，按照level从低到高进行排序
        if (o1.calculationType().getPriority() == o2.calculationType().getPriority()) {
            return o1.config().getCalculation().getLevel() - o2.config().getCalculation().getLevel();
        }
        //  按优先级从低到高排序
        return o1.calculationType().getPriority() - o2.calculationType().getPriority();
    }
}
