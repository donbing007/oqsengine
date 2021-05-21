package com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc;

import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import java.util.Collection;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

/**
 * hash分片算法.
 *
 * @author dongbin
 * @version 0.1 2020/11/6 13:18
 * @since 1.8
 */
public class HashPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        String key = shardingValue.getValue().toString();
        HashSelector<String> hashSelector = new HashSelector(availableTargetNames);
        return hashSelector.select(key);
    }
}
