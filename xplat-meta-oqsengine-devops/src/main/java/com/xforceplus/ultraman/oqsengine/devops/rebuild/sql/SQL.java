package com.xforceplus.ultraman.oqsengine.devops.rebuild.sql;

/**
 * SQL 定义.
 *
 * @author xujia 2020/8/24
 * @since 1.8
 */
public class SQL {
    public static final String BUILD_SQL =
        "insert into %s (maintainid, entity, starts, ends, "
            + "batchsize, finishsize, status, createtime, startid) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_SQL_ACTIVE =
        "select maintainid, entity, starts, ends, batchsize, "
            + "finishsize, status, createtime, updatetime, message, startid from %s "
            + "where entity = ? and status in (0, 1)";

    public static final String SELECT_SQL_TASK_ID =
        "select maintainid, entity, starts, ends, batchsize, "
            + "finishsize, status, createtime, updatetime, message, startid from %s "
            + "where maintainid = ?";

    public static final String LIST_ALL =
        "select maintainid, entity, starts, ends, batchsize, "
            + "finishsize, status, createtime, updatetime, message, startid from %s order by maintainid desc limit ?, ?";

    public static final String LIST_ACTIVES =
        "select maintainid, entity, starts, ends, batchsize, "
            + "finishsize, status, createtime, updatetime, message, startid from %s where status in (0, 1) order by maintainid desc limit ?, ?";

    public static final String COUNT_ACTIVES =
        "select count(1) from %s where status in (0, 1)";

    public static final String COUNT_ALL =
        "select count(1) from %s";

    public static final String STATUS_SQL =
        "update %s set updatetime = ?, status = ?, message = ? where maintainid = ? and status not in (2, 3, 4)";

    public static final String ERROR_SQL =
        "update %s set updatetime = ?, finishsize = ?, status = ?, message = ?, startid = ? "
            + "where maintainid = ? and status != 2";

    public static final String RESUME_SQL = "update %s set updatetime = ?, status = ?, message = ? "
        + "where maintainid = ? and status not in (0, 1, 2)";
}
