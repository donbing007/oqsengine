package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.HaveOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.HaveOrNoRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.NoOrHaveRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.builder.NoOrNorRanageConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据条件中是否需要 OR 查询,各属性是否需要范围查询来决定生成器.
 * 最优的情况是,没有范围查询全部等值查询.
 * 最坏的情况是,有范围查询,同时有多条件使用 OR 连接.
 *
 * @author dongbin
 * @version 0.1 2020/2/22 16:59
 * @since 1.8
 */
public class DefaultSphinxQLQueryOptimizer implements SphinxQLQueryOptimizer {

    private Map<Integer, ConditionsBuilder> builderMap;

    public DefaultSphinxQLQueryOptimizer() {
        builderMap = new HashMap<>();
        builderMap.put(0, new NoOrNorRanageConditionsBuilder());
        builderMap.put(1, new NoOrHaveRanageConditionsBuilder());
        builderMap.put(2, new HaveOrNoRanageConditionsBuilder());
        builderMap.put(3, new HaveOrHaveRanageConditionsBuilder());
    }

    @Override
    public ConditionsBuilder<String> optimizeConditions(Conditions conditions) {
        /**
         * or 字节低位开始第2位.
         * ranage 字节低位开始第1位.
         */
        int or = conditions.haveOrLink() ? 2 : 0;
        int range = conditions.haveRangeCondition() ? 1 : 0;
        return builderMap.get(or | range);
    }
}
