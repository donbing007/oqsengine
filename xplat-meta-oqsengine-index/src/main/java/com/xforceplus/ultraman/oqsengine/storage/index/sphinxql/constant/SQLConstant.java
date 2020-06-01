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

    public static final String WRITER_SQL = "%s into %s (%s, %s, %s, %s, %s, %s) values(?,?,?,?,?,?)";
    public static final String DELETE_SQL = "delete from %s where id = ?";
    /**
     * %s 顺序为 where 条件, 排序.
     */
    public static final String SELECT_SQL = "select id, pref, cref from %s where entity = ? %s %s limit ?,? option max_matches=?";
    public static final String SELECT_COUNT_SQL = "select count(*) as " + FieldDefine.COUNT + " from %s where entity = ? %s";
    public static final String SELECT_FROM_ID_SQL = "select id, pref, cref, entity, jsonfields from %s where id = ?";

}
