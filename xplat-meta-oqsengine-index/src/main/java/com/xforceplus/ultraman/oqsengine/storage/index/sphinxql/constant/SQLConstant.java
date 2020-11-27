package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 5:03 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLConstant {

    public static final String WRITER_SQL = "%s into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) values(?,?,?,?,?,?,?,?,?,?,?,?)";
    public static final String DELETE_SQL = "delete from %s where id = ?";
    /**
     * %s 顺序为 where 条件, 排序.
     * NO_ENTITY表示不会增加entity的过滤.
     */
    public static final String SELECT_SQL = "select id, pref, cref, oqsmajor %s from %s where %s %s limit ?,? option max_matches=?,max_query_time=?,ranker=none";
    public static final String FILTER_IDS = "id not in (%s)";

    public static final String FILTER_COMMIT = "commitid < %s";

    public static final String SELECT_COUNT_SQL = "show meta";
    public static final String SELECT_FROM_ID_SQL = "select id, pref, cref, entity, tx, commitid, jsonfields, maintainid, time from %s where id = ? option max_matches=1,ranker=none";

}
