package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;

/**
 * SQL 模板.
 */
public class SQLConstant {
    /**
     * %s 顺序为 where 条件, 排序.
     * NO_ENTITY表示不会增加entity的过滤.
     * 占位序号意义.
     * <ul>
     *     <li>1 分页的开始行数</li>
     *     <li>2 分页的结束行数</li>
     *     <li>3 最大匹配量</li>
     *     <li>4 最大查询时间</li>
     * </ul>
     */
    public static final String SELECT_SQL =
        "SELECT " + FieldDefine.ID
            + " %s FROM %s WHERE %s %s LIMIT ?,? OPTION max_matches=?,max_query_time=?,ranker=none";

    /**
     * 继承于 SELECT_SQL 模板,追加了查询线程的限制.
     * 占位序号为 5.
     */
    public static final String SELECT_SQL_LIMIT_THREADS = SELECT_SQL + ",threads=?";

    public static final String SEARCH_SQL =
        "SELECT " + FieldDefine.ID + " FROM %s WHERE %s LIMIT ?,? OPTION max_matches=?,max_query_time=?,ranker=%s";

    public static final String SELECT_COUNT_SQL = "show meta";

}
