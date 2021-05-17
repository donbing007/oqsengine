create table oqsbigentity
(
    id             bigint                not null comment '数据主键',
    entityclassl0  bigint  default 0     not null comment '数据家族中在0层的entityclass标识',
    entityclassl1  bigint  default 0     not null comment '数据家族中在1层的entityclass标识',
    entityclassl2  bigint  default 0     not null comment '数据家族中在2层的entityclass标识',
    entityclassl3  bigint  default 0     not null comment '数据家族中在3层的entityclass标识',
    entityclassl4  bigint  default 0     not null comment '数据家族中在4层的entityclass标识',
    entityclassver int     default 0     not null comment '产生数据的entityclass版本号.',
    tx             bigint  default 0     not null comment '提交事务号',
    commitid       bigint  default 0     not null comment '提交号',
    op             tinyint default 0     not null comment '最后操作类型,0(插入),1(更新),2(删除)',
    version        int     default 0     not null comment '当前数据版本.',
    createtime     bigint  default 0     not null comment '数据创建时间.',
    updatetime     bigint  default 0     not null comment '数据操作最后时间.',
    deleted        boolean default false not null comment '是否被删除.',
    attribute      json                  not null comment '当前 entity 的属性集合.',
    oqsmajor       int     default 0     not null comment '产生数据的oqs主版本号',
    primary key (id),
    KEY commitid_entity_index (commitid, entityclassl0, entityclassl1, entityclassl2, entityclassl3, entityclassl4),
    KEY tx_index (tx),
    KEY update_time_index (updatetime)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE oqsunique
(
    `id`            bigint(20)                                             NOT NULL COMMENT '数据主键',
    `entityclassl0` bigint(20)                                             NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl1` bigint(20)                                             NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl2` bigint(20)                                             NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl3` bigint(20)                                             NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl4` bigint(20)                                             NOT NULL DEFAULT 0 COMMENT '数据家族中在0层的entityclass标识',
    `unique_key`    varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '业务主键1',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `IDX_U1` (`unique_key`, `entityclassl0`, `entityclassl1`, `entityclassl2`, `entityclassl3`,
                           `entityclassl4`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
