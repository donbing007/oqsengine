package com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc;

import java.util.Collection;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

/**
 * 所有分片策略.
 *
 * @author xujia 2020/11/25
 * @since 1.8
 */
public class CommonRangeShardingAlgorithm implements RangeShardingAlgorithm<Long> {
    //  由于不采用range进行分片,在主键range查询时,需要进行广播，将所有的sharding key都返回出去
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return collection;
    }
}
