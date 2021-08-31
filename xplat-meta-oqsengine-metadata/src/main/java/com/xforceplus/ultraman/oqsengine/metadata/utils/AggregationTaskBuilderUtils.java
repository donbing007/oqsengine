package com.xforceplus.ultraman.oqsengine.metadata.utils;

import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;

import javax.annotation.Resource;
import java.util.List;

/**
 * 聚合字段初始化字段任务.
 *
 * @className: AggregationTaskBuilderUtils
 * @package: com.xforceplus.ultraman.oqsengine.metadata.utils
 * @author: wangzheng
 * @date: 2021/8/31 16:24
 */
public class AggregationTaskBuilderUtils {
    @Resource
    private CacheExecutor cacheExecutor;

    private static boolean buildTask(String appId, int version, List<EntityClassStorage> storageList){
        return false;
    }
}
