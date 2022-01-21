package com.xforceplus.ultraman.oqsengine.calculation.event.executor;

import com.xforceplus.ultraman.oqsengine.calculation.dto.CalculationEvent;
import com.xforceplus.ultraman.oqsengine.calculation.event.helper.CalculationEventResource;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CachedEntityClass;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.CalculationInitStatus;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public abstract class AbstractEventExecutor implements CalculationEventExecutor {

    public void deleteTypeExecute(Map<Long, List<Long>> deletes, CalculationEventResource resource) {
        //  todo delete
    }

    protected boolean kvProcess(CalculationEvent calculationEvent, CalculationEventResource resource) {
        final String INIT_FLAG = "calculationInitField-";
        Map<OperationType, Collection<CalculationEvent.CalculationField>> map = new HashMap<>();
        calculationEvent.getCalculationFields().values().forEach(calculationFields -> calculationFields.forEach(calculationField -> {
            switch (calculationField.getOp()) {
                case CREATE:
                    if (map.containsKey(OperationType.CREATE)) {
                        map.get(OperationType.CREATE).add(calculationField);
                    } else {
                        map.put(OperationType.CREATE, Stream.of(calculationField).collect(Collectors.toList()));
                    }
                    break;
                case DELETE:
                    if (map.containsKey(OperationType.DELETE)) {
                        map.get(OperationType.DELETE).add(calculationField);
                    } else {
                        map.put(OperationType.DELETE, Stream.of(calculationField).collect(Collectors.toList()));
                    }
                    break;
                case UPDATE:
                    if (map.containsKey(OperationType.UPDATE)) {
                        map.get(OperationType.UPDATE).add(calculationField);
                    } else {
                        map.put(OperationType.UPDATE, Stream.of(calculationField).collect(Collectors.toList()));
                    }
                    break;
                default:
                    break;
            }
        }));
        Map<String, byte[]> needInit = new HashMap<>();
        map.get(OperationType.CREATE).forEach(calculationField -> needInit.put(INIT_FLAG + calculationField.getEntityField().id(),
                resource.getSerializeStrategy().serialize(CalculationInitStatus.UN_INIT)));
        map.get(OperationType.UPDATE).forEach(calculationField -> needInit.put(INIT_FLAG + calculationField.getEntityField().id(),
                resource.getSerializeStrategy().serialize(CalculationInitStatus.UN_INIT)));
        long save = resource.getKeyValueStorage().save(needInit.entrySet());
        return save == needInit.size();
    }
}
