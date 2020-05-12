use oqsengine;
create table oqsbigentity
(
    id        bigint                not null comment '数据主键',
    entity    bigint  default 0     not null comment 'entity 的类型 id.',
    version   int     default 0     not null comment '当前数据版本.',
    time      bigint  default 0     not null comment '数据操作最后时间.',
    pref      bigint  default 0     not null comment '指向当前类型继承的父类型数据实例id.',
    cref      bigint  default 0     not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted   boolean default false not null comment '是否被删除.',
    attribute json                  not null comment '当前 entity 的属性集合.',
    constraint oqsengine_pk primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

/**
  如果需要 mysql 进行分区,推荐以下配置.
  单表控制在500万数据量,以2亿数据量来说即是40张表.
create table oqsbigentity
(
    id bigint not null comment '数据主键',
    entity bigint default 0 not null comment 'entity 的类型 id.',
    version int default 0 not null comment '当前数据版本.',
    time bigint default 0 not null comment '数据操作最后时间.',
    pref bigint default 0 not null comment '指向当前类型继承的父类型数据实例id.',
    cref bigint default 0 not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted boolean default false not null comment '是否被删除.',
    attribute json not null comment '当前 entity 的属性集合.',
    constraint oqsengine_pk primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 PARTITION BY HASH(id) PARTITIONS 40;
 */