package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import java.util.List;

/**
 * 聚合解析器.
 *
 * @className: AggregationParse
 * @package: com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse
 * @author: wangzheng
 * @date: 2021/8/30 12:04
 */
public interface AggregationParse {

    /**
     * 构建聚合解析器信息.
     * @param appId 应用id.
     * @return
     */
    List<ParseTree> build(String appId);

    /**
     * 查找聚合树.
     *
     * @param id 对象id
     * @return 聚合树
     */
    ParseTree find(String id);

}
