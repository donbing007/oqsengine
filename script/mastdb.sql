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
    profile        varchar(64) default '' not null comment '替身',
    primary key (id),
    KEY commitid_entity_index (commitid, entityclassl0, entityclassl1),
    KEY tx_index (tx),
    KEY update_time_index (updatetime, entityclassl0, entityclassl1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

/*
* kv 储存.
*/
create table kv (
  k  varchar(255)          not null comment 'key',
  h  bigint                not null comment 'key的哈希值,主要用以分区',
  v  blob                           comment '值',
  unique key unique_key(k, h)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

/**
* 自增编号
*/
CREATE TABLE `segment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(64) NOT NULL DEFAULT '' COMMENT ',objectCode:fieldName',
  `begin_id` bigint(20) NOT NULL DEFAULT '1' COMMENT 'ID',
  `max_id` bigint(20) NOT NULL DEFAULT '0' COMMENT 'ID',
  `step` int(11) NOT NULL DEFAULT '1000',
  `pattern` varchar(64) NOT NULL DEFAULT '',
  `pattern_key` varchar(64) NOT NULL DEFAULT '' COMMENT 'key',
  `resetable` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'pattern_key 0: 1:',
  `mode` tinyint(4) NOT NULL DEFAULT '2' COMMENT '1 2: ',
  `version` bigint(20) NOT NULL DEFAULT '1',
  `create_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

