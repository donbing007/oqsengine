package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;

/**
 * SQL 模板.
 */
public class SQLConstant {
    /**
     * %s 顺序为 where 条件, 排序.
     * NO_ENTITY表示不会增加entity的过滤.
     */
    public static final String SELECT_SQL =
        "SELECT " + FieldDefine.ID + " %s FROM %s WHERE %s %s LIMIT ?,? OPTION max_matches=?,max_query_time=?,ranker=none";

    public static final String SEARCH_SQL =
        "SELECT " + FieldDefine.ID + " FROM %s WHERE %s LIMIT ?,? OPTION max_matches=?,max_query_time=?,ranker=?";

    public static final String SELECT_COUNT_SQL = "show meta";

}
