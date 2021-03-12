package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 5:03 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLConstant {
    /**
     * %s 顺序为 where 条件, 排序.
     * NO_ENTITY表示不会增加entity的过滤.
     */
    public static final String SELECT_SQL =
        "SELECT " + FieldDefine.ID + ", " + FieldDefine.OQSMAJOR + " %s FROM %s WHERE %s %s LIMIT ?,? " +
            "OPTION max_matches=?,max_query_time=?,ranker=none";
    public static final String FILTER_IDS = FieldDefine.ID + " NOT IN (%s)";

    public static final String FILTER_COMMIT = FieldDefine.COMMITID + " < %s";

    public static final String SELECT_COUNT_SQL = "show meta";

}
