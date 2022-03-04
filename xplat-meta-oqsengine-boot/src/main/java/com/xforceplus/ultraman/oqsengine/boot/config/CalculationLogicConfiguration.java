package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculation.Calculation;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse.AggregationParse;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse.MetaAggregationParse;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.CalculationInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.DefaultCalculationInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.DefaultInitCalculationManager;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.InitCalculationManager;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.AggregationInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.AutoFillInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.FormulaInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueLogic;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * 计算字段配置.
 *
 * @author dongbin
 * @version 0.1 2021/08/24 14:59
 * @since 1.8
 */
@Configuration
public class CalculationLogicConfiguration {

    @Bean
    public CalculationLogicFactory calculationLogicFactory() {
        return new CalculationLogicFactory();
    }

    @Bean
    public Calculation calculation() {
        return new DefaultCalculationImpl();
    }


    @Bean
    public AggregationParse aggregationParse() {
        return new MetaAggregationParse();
    }

    /**
     * 计算字段初始化工厂.
     */
    @Bean
    public InitIvalueFactory initIvalueFactory(List<InitIvalueLogic> initIvalueLogics) {
        InitIvalueFactory initIvalueFactory = new InitIvalueFactory();
        Map<CalculationType, InitIvalueLogic> map = new HashMap<>();
        for (InitIvalueLogic initIvalueLogic : initIvalueLogics) {
            map.put(initIvalueLogic.getCalculationType(), initIvalueLogic);
        }
        map.put(CalculationType.FORMULA, new FormulaInitLogic());
        initIvalueFactory.setInitIvalueLogicMap(map);
        return initIvalueFactory;
    }

    @Bean ("aggregationInitLogic")
    public InitIvalueLogic aggregationInitLogic() {
        return new AggregationInitLogic();
    }

    @Bean ("autoFillInitLogic")
    public InitIvalueLogic autoFillInitLogic() {
        return new AutoFillInitLogic();
    }

    /**
     * 计算初始化逻辑执行.
     */
    @Bean
    public CalculationInitLogic calculationInitLogic() {
        return new DefaultCalculationInitLogic();
    }

    /**
     * 计算字段初始化管理者.
     */
    @Bean
    public InitCalculationManager initCalculationManager() {
        return new DefaultInitCalculationManager();
    }

}
