package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.Map;

/**
 * 计算字段初始化逻辑工厂.
 *
 * @version 0.1 2021/12/2 14:55
 * @Auther weikai
 * @since 1.8
 */
public class InitIvalueFactory {
    private Map<CalculationType, InitIvalueLogic> initIvalueLogicMap;

    public Map<CalculationType, InitIvalueLogic> getInitIvalueLogicMap() {
        return initIvalueLogicMap;
    }

    public void setInitIvalueLogicMap(Map<CalculationType, InitIvalueLogic> initIvalueLogicMap) {
        this.initIvalueLogicMap = initIvalueLogicMap;
    }

    /**
     * 获取计算字段计算逻辑.
     */
    public InitIvalueLogic getLogic(CalculationType calculationType) {
        if (!initIvalueLogicMap.containsKey(calculationType)) {
            throw new UnsupportedOperationException(String.format("not support calculationType %s init yet.", calculationType));
        }
        return initIvalueLogicMap.get(calculationType);
    }
}
