package com.xforceplus.ultraman.oqsengine.storage.master.mock;


/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class MasterDbScript {
    public static final String DROP_OQS_BIG_ENTITY = "drop table if exists oqsbigentity";
    public static final String CREATE_OQS_BIG_ENTITY =
        "create table if not exists oqsbigentity\n" +
        "(\n" +
        "    id             bigint                not null comment '数据主键',\n" +
        "    entityclassl0  bigint  default 0     not null comment '数据家族中在0层的entityclass标识',\n" +
        "    entityclassl1  bigint  default 0     not null comment '数据家族中在1层的entityclass标识',\n" +
        "    entityclassl2  bigint  default 0     not null comment '数据家族中在2层的entityclass标识',\n" +
        "    entityclassl3  bigint  default 0     not null comment '数据家族中在3层的entityclass标识',\n" +
        "    entityclassl4  bigint  default 0     not null comment '数据家族中在4层的entityclass标识',\n" +
        "    entityclassver int     default 0     not null comment '产生数据的entityclass版本号.',\n" +
        "    tx             bigint  default 0     not null comment '提交事务号',\n" +
        "    commitid       bigint  default 0     not null comment '提交号',\n" +
        "    op             tinyint default 0     not null comment '最后操作类型,0(插入),1(更新),2(删除)',\n" +
        "    version        int     default 0     not null comment '当前数据版本.',\n" +
        "    createtime     bigint  default 0     not null comment '数据创建时间.',\n" +
        "    updatetime     bigint  default 0     not null comment '数据操作最后时间.',\n" +
        "    deleted        boolean default false not null comment '是否被删除.',\n" +
        "    attribute      json                  not null comment '当前 entity 的属性集合.',\n" +
        "    oqsmajor       int     default 0     not null comment '产生数据的oqs主版本号',\n" +
        "    profile        varchar(64) default '' not null comment '替身',\n" +
        "    primary key (id),\n" +
        "    KEY commitid_entity_index (commitid, entityclassl0, entityclassl1),\n" +
        "    KEY tx_index (tx),\n" +
        "    KEY update_time_index (updatetime)\n" +
        ") ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;";

    public static final String DROP_OQS_UNIQUE = "drop table if exists oqsunique";
    public static final String CREATE_OQS_UNIQUE =
        "create table if not exists oqsunique\n" +
            "(\n" +
            "    `id`            bigint(20)     NOT NULL COMMENT '数据主键',\n" +
            "    `entityclassl0` bigint(20)     NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',\n" +
            "    `entityclassl1` bigint(20)     NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',\n" +
            "    `entityclassl2` bigint(20)     NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',\n" +
            "    `entityclassl3` bigint(20)     NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',\n" +
            "    `entityclassl4` bigint(20)     NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',\n" +
            "    `unique_key`    varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '业务主键1',\n" +
            "    PRIMARY KEY (`id`) USING BTREE,\n" +
            "    UNIQUE INDEX `IDX_U1` (`unique_key`, `entityclassl0`, `entityclassl1`, `entityclassl2`, `entityclassl3`,\n" +
            "                           `entityclassl4`) USING BTREE\n" +
            ") ENGINE = InnoDB\n" +
            "  DEFAULT CHARSET = utf8;";

    public static final String DROP_OQS_ENTITY_FAILS = "drop table if exists entityfaileds";
    public static final String CREATE_OQS_ENTITY_FAILS =
        "create table if not exists entityfaileds\n" +
            "(\n" +
            "    id          bigint                      not null comment '业务主键ID',\n" +
            "    entity      bigint                      not null comment 'entity 的类型 id.',\n" +
            "    errors      text                                 comment '错误字段摘要',\n" +
            "    executetime bigint default 0            not null comment '出错时间戳',\n" +
            "    fixedtime   bigint default 0            not null comment '修复时间.',\n" +
            "    status      tinyint default 1           not null comment '处理状态,1(未处理),2(已提交处理),3(处理成功),4(处理失败)',\n" +
            "    primary key (id),\n" +
            "    key entityfaileds_k1 (entity),\n" +
            "    key entityfaileds_k2 (executetime)\n" +
            ") ENGINE = InnoDB\n" +
            "  DEFAULT CHARSET = utf8mb4;";
}
