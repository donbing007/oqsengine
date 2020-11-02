package com.xforceplus.ultraman.oqsengine.storage.master.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/25/2020 5:04 PM
 * 功能描述:
 * 修改历史:
 */
public class SQLConstant {
    public static final String BUILD_SQL =
        "insert into %s (id, entity, version, time, pref, cref, deleted, attribute) values(?,?,?,?,?,?,?,?)";
    public static final String REPLACE_SQL =
        "update %s set version = version + 1, time = ?, attribute = ? where id = ? and version = ?";
    public static final String REPLACE_VERSION_TIME_SQL =
        "update %s set version = ?, time = ? where id = ?";
    public static final String DELETE_SQL =
        "update %s set version = version + 1, deleted = ?, time = ? where id = ? and version = ?";
    public static final String DELETE_SQL_WITHOUT_VERSION_SQL =
        "update %s set version = ?, deleted = ?, time = ? where id = ?";
    public static final String SELECT_SQL =
        "select id, entity, version, time, pref, cref, deleted, attribute from %s where id = ? and deleted = false";
    public static final String SELECT_IN_SQL =
        "select id, entity, version, time, pref, cref, deleted, attribute from %s where id in (%s) and deleted = false";
    public static final String SELECT_VERSION_TIME_SQL =
        "select version, time from %s where id = ?";

    public static final String UNDO_BUILD_SQL = "delete from %s where id = ?";
    public static final String UNDO_DELETE_SQL =
        "update %s set version = version - 1, deleted = ?, time = ? where id = ? and version = ?";
    public static final String UNDO_REPLACE_SQL =
        "update %s set version = version - 1, time = ?, attribute = ? where id = ? and version = ?";
}
