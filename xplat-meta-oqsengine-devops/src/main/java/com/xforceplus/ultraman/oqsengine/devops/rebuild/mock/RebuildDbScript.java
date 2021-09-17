package com.xforceplus.ultraman.oqsengine.devops.rebuild.mock;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class RebuildDbScript {
    public static final String DROP_REBUILD = "drop table if exists devopstasks;";
    public static final String CREATE_REBUILD =
        "create table devopstasks\n" +
            "(\n" +
            "     maintainid bigint not null comment '任务批次ID',\n" +
            "     entity bigint not null comment 'entity 的类型 id.',\n" +
            "     starts bigint default 0 not null comment '任务范围起始时间',\n" +
            "     ends bigint default 0 not null comment '任务范围终止时间',\n" +
            "     batchsize int default 0 not null comment '任务总数',\n" +
            "     finishsize int default 0 not null comment '任务已处理数',\n" +
            "     status int default 0 not null comment '状态',\n" +
            "     createtime bigint not null comment '任务创建时间',\n" +
            "     updatetime bigint default null comment '任务结束时间',\n" +
            "     message varchar(512) default null comment '任务摘要',\n" +
            "     startid bigint default 0 not null comment '起始ID',\n" +
            "     constraint devopstasks_pk0 primary key (maintainid),\n" +
            "     key devopstasks_k0 (entity)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;";
}
