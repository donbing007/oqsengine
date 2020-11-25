package com.xforceplus.ultraman.oqsengine.devops.task.sql;

/**
 * desc :
 * name : SQL
 *
 * @author : xujia
 * date : 2020/8/24
 * @since : 1.8
 */
public class SQL {
    public static final String BUILD_SQL =
            "insert into %s (maintainid, entity, starts, ends, " +
                    "batchsize, finishsize, status, createtime) values ( ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_SQL_ACTIVE =
            "select maintainid, entity, starts, ends, batchsize, " +
                    "finishsize, status, createtime, updatetime, message from %s " +
                    "where entity = ? and status in (0, 1)";

    public static final String SELECT_SQL_TASK_ID =
            "select maintainid, entity, starts, ends, batchsize, " +
                    "finishsize, status, createtime, updatetime, message, checkpoint from %s " +
                    "where maintainid = ?";

    public static final String LIST_ALL =
            "select maintainid, entity, starts, ends, batchsize, " +
                    "finishsize, status, createtime, updatetime, message from %s order by maintainid desc limit ?, ?";

    public static final String LIST_ACTIVES =
            "select maintainid, entity, starts, ends, batchsize, " +
                    "finishsize, status, createtime, updatetime, message from %s where status in (0, 1) order by maintainid desc limit ?, ?";

    public static final String COUNT_ACTIVES =
            "select count(1) from %s where status in (0, 1)";

    public static final String COUNT_ALL =
            "select count(1) from %s";


    public static final String STATUS_SQL = "update %s set updatetime = ?, status = ?, message = ? where maintainid = ? and status not in (2, 3, 4)";

    public static final String UPDATE_SQL = "update %s set updatetime = ?, finishsize = ?, status = ?, message = ?, checkpoint = ? " +
            "where maintainid = ? and status not in (2, 3, 4)";

    public static final String ERROR_SQL = "update %s set updatetime = ?, finishsize = ?, status = ?, message = ?, checkpoint = ? " +
            "where maintainid = ? and status != 2";

    public static final String INCREASED_FINISH = "update %s set updatetime = ?, finishsize = ?, checkpoint = ? " +
            "where maintainid = ? and status != 2";

    public static final String RESUME_SQL = "update %s set updatetime = ?, status = ?, message = ? " +
            "where maintainid = ? and status not in (0, 1, 2)";
}
