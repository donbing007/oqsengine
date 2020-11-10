package com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc;

import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.selector.SuffixNumberHashSelector;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 后辍hash分片算法.
 *
 * @author dongbin
 * @version 0.1 2020/11/6 13:32
 * @since 1.8
 */
public class SuffixNumberHashPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        String key = shardingValue.getValue().toString();
        Selector<String> selector = new SuffixNumberHashSelector(shardingValue.getLogicTableName(), availableTargetNames.size());
        return selector.select(key);
    }
}
