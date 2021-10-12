package com.xforceplus.ultraman.oqsengine.task.mock;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class KVDbScript {
    public static final String DROP_KV = "drop table if exists `kv`;";
    public static final String CREATE_KV =
            "create table if not exists `kv` (" +
            "`k` varchar(255) not null, " +
            "`v` blob, " +
            "primary key (`k`) " +
            ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;";
}
