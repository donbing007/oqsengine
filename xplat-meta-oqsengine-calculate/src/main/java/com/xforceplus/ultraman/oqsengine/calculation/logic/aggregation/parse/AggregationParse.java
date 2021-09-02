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
     * 查找聚合树.
     *
     * @param entityClassId 元信息标识.
     * @param fieldId 字段id
     * @return 聚合树
     */
    ParseTree find(Long entityClassId, Long fieldId, String profileCode);

    /**
     * 追加最新聚合树.
     */
    void appendTree(ParseTree parseTree);

}
