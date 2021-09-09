package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
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
     * @param fieldId 字段id.
     * @param profileCode 租户信息.
     * @return 聚合树
     */
    ParseTree find(Long entityClassId, Long fieldId, String profileCode);

    /**
     * 查找这个对象下有多少个需要更新的聚合树.
     *
     * @param entityClassId 元信息标识.
     * @param profileCode 租户信息.
     * @return 聚合树集合.
     */
    List<ParseTree> find(Long entityClassId, String profileCode);

    /**
     * 追加最新聚合树.
     */
    void appendTree(ParseTree parseTree);

    /**
     * 构建解析器.
     *
     * @param appId 应用id.
     * @param version 应用版本.
     * @param entityClasses 对象列表.
     */
    void builder(String appId, int version, List<IEntityClass> entityClasses);

    /**
     * 构建解析器.
     *
     * @param appId 应用id.
     * @param version 应用版本.
     * @param entityClasses 对象列表.
     */
    void builderTrees(String appId, int version, List<IEntityClass> entityClasses);

}
