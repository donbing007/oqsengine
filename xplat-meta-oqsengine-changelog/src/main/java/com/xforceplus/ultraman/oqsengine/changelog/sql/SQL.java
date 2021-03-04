package com.xforceplus.ultraman.oqsengine.changelog.sql;

public class SQL {

    public static final String SAVE_SQL =
            "insert into %s (cid, id, entity, comment, changes, version, create_time)" +
                   "values ( ?, ?, ?, ?, ?, ?, ?)";

    public static final String FIND_SQL =
            "select * from %s where id = %s order by version desc";


    public static final String FIND_SQL_VERSION =
            "select * from %s where id = %s and version < %s order by version desc";
}
