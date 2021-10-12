package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse.AggregationParse;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse.MetaAggregationParse;
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

    // @Bean
    public AggregationParse aggregationParse() {
        return new MetaAggregationParse();
    }
}
