package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import static com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType.FORMULA;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
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
        if (o1.calculationType().equals(FORMULA) && o1.calculationType().equals(o2.calculationType())) {
            return ((Formula) o1.config().getCalculation()).getLevel() - ((Formula) o2.config().getCalculation()).getLevel();
        }
        return o1.calculationType().getPriority() - o2.calculationType().getPriority();
    }
}
