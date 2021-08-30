package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;


import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.auth.Authorization;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元数据聚合解析器.
 *
 * @className: MetaAggregationParse
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse
 * @author: wangzheng
 * @date: 2021/8/30 12:04
 */
public class MetaAggregationParse implements AggregationParse {

    @Resource
    private CacheExecutor cacheExecutor;

    @Resource
    private ParseTree parseTree;

    @Override
    public List<ParseTree> build(String appId) {
        List<EntityClassStorage> entityClassStorageList = cacheExecutor.read(appId);
        if (entityClassStorageList.isEmpty()) {
            return Collections.emptyList();
        }
        entityClassStorageList.stream().map(ecs -> {
            return null;
        }).collect(Collectors.toList());
        return null;
    }

    @Override
    public ParseTree find(String code) {
        return null;
    }

    /**
     * 当元数据版本不一致时，重新构建元数据解析树.
     *
     * @param appId 应用id.
     * @return
     */
    private List<ParseTree> reBuild(String appId) {
        List<EntityClassStorage> entityClassStorageList = cacheExecutor.read(appId);
        return null;
    }

}
