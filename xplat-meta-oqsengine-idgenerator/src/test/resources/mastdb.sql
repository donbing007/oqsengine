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


CREATE TABLE `oqsunique`
(
    `id`            bigint(20)  NOT NULL COMMENT '数据主键',
    `entityclassl0` bigint(20)  NOT NULL COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl1` bigint(20)  NOT NULL COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl2` bigint(20)  NOT NULL COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl3` bigint(20)  NOT NULL COMMENT '数据家族中在0层的entityclass标识',
    `entityclassl4` bigint(20)  NOT NULL COMMENT '数据家族中在0层的entityclass标识',
    `unique_key`    varchar(128) NOT NULL COMMENT '业务主键1',
    constraint IDX_T1 primary key (id),
    constraint IDX_U1 unique key (`unique_key`, `entityclassl0`, `entityclassl1`, `entityclassl2`, `entityclassl3`,
                                  `entityclassl4`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC;

CREATE TABLE `segment`
(
    `id`          bigint(20)                                                   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_type`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务标签，例如可以用来标识业务序列号的的对象字段,objectCode:fieldName',
    `begin_id`    bigint(20)                                                   NOT NULL DEFAULT 1 COMMENT '号段起始ID',
    `max_id`      bigint(20)                                                   NOT NULL DEFAULT 0 COMMENT '当前号段最大ID',
    `step`        int(11)                                                      NOT NULL DEFAULT 1000 COMMENT '号段增加的步长',
    `pattern`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '自定义模式',
    `pattern_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '需要重置的模式记录上一次重置的key值',
    `resetable`   tinyint(4)                                                   NOT NULL DEFAULT 0 COMMENT '是否需要根据pattern_key重置编号 0:不需要 1:需要',
    `mode`        tinyint(4)                                                   NOT NULL DEFAULT 2 COMMENT '1：顺序递增 2: 趋势递增',
    `version`     bigint(20)                                                   NOT NULL DEFAULT 1 COMMENT '版本号',
    `create_time` timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` timestamp(3)                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic;
