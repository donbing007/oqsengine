package com.xforceplus.ultraman.oqsengine.changelog.sql;

public class SQL {

    public static final String SAVE_SQL =
            "insert into %s (cid, id, entity, comment, changes, version, create_time)" +
                    "values ( ?, ?, ?, ?, ?, ?, ?)";

    public static final String SAVE_SNAPSHOT =
            "insert into %s (sid, id, entity, changes, reference, version, create_time)" +
                    "values ( ?, ? , ? , ? , ?, ?, ?)";

    public static final String FIND_SNAPSHOT_SQL =
            "select * from %s where id = %s order by version desc limit 1";

    public static final String FIND_SNAPSHOT_SQL_VERSION =
            "select * from %s where id = %s where version <= %s order by version desc limit 1";

    public static final String SAVE_VERSION_SQL =
            "insert into %s (vid, id, comment, version, timestamp, user, source)" +
                    "values (?, ? , ?, ?, ?, ?, ?)";

    public static final String FIND_SQL =
            "select * from %s where id = %s and version <= %s order by version desc";

    public static final String FIND_SQL_VERSION =
            "select * from %s where id = %s and (version >= %s and version <= %s) order by version desc";

    public static final String FIND_GROUPED_VERSION =
            "select id, count(*) as count from %s where id in (%s) group by id";

    public static final String FIND_GROUPED_VERSION_SELF =
            "select id, count(*) as count from %s where id in (%s) and source = id group by id";

    /**
     * only support mysql
     */
    public static final String FIND_VERSION =
            "select * from %s where id = %s order by version desc limit %s, %s";

    public static final String FIND_VERSION_SELF =
            "select * from %s where id = %s and source = id order by version desc limit %s, %s";
}
