create database oqsengine;
use oqsengine;
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
    profile        varchar(64)           not null comment '替身',
    primary key (id),
    KEY commitid_entity_index (commitid, entityclassl0, entityclassl1),
    KEY tx_index (tx),
    KEY update_time_index(updatetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 PARTITION BY HASH(id) PARTITIONS 40;

create table kv (
  k       varchar(255)          not null comment 'key',
  v     blob                           comment '值',
  primary key (k)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 PARTITION BY KEY() PARTITIONS 40;