drop table if exists oqsbigentity;
create table oqsbigentity
(
    id        bigint                not null comment '数据主键',
    entity    bigint  default 0     not null comment 'entity 的类型 id.',
    tx        bigint  default 0     not null comment '提交事务号',
    commitid  bigint  default 0     not null comment '提交号',
    op        tinyint default 0     not null comment '最后操作类型,0(插入),1(更新),2(删除)',
    version   int     default 0     not null comment '当前数据版本.',
    time      bigint  default 0     not null comment '数据操作最后时间.',
    pref      bigint  default 0     not null comment '指向当前类型继承的父类型数据实例id.',
    cref      bigint  default 0     not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted   boolean default false not null comment '是否被删除.',
    attribute json                  not null comment '当前 entity 的属性集合.',
    meta      json                  not null comment '数据产生时的元信息摘要',
    oqsmajor  int     default 0     not null comment '产生数据的oqs主版本号',
    constraint oqsengine_pk primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

drop table if exists oqsbigentity0;
create table oqsbigentity0
(
    id        bigint                not null comment '数据主键',
    entity    bigint  default 0     not null comment 'entity 的类型 id.',
    version   int     default 0     not null comment '当前数据版本.',
    time      bigint  default 0     not null comment '数据操作最后时间.',
    pref      bigint  default 0     not null comment '指向当前类型继承的父类型数据实例id.',
    cref      bigint  default 0     not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted   boolean default false not null comment '是否被删除.',
    attribute LONGTEXT              not null comment '当前 entity 的属性集合.',
    constraint oqsengine_pk0 primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

drop table if exists oqsbigentity1;
create table oqsbigentity1
(
    id        bigint                not null comment '数据主键',
    entity    bigint  default 0     not null comment 'entity 的类型 id.',
    version   int     default 0     not null comment '当前数据版本.',
    time      bigint  default 0     not null comment '数据操作最后时间.',
    pref      bigint  default 0     not null comment '指向当前类型继承的父类型数据实例id.',
    cref      bigint  default 0     not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted   boolean default false not null comment '是否被删除.',
    attribute LONGTEXT              not null comment '当前 entity 的属性集合.',
    constraint oqsengine_pk1 primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

drop table if exists oqsbigentity2;
create table oqsbigentity2
(
    id        bigint                not null comment '数据主键',
    entity    bigint  default 0     not null comment 'entity 的类型 id.',
    version   int     default 0     not null comment '当前数据版本.',
    time      bigint  default 0     not null comment '数据操作最后时间.',
    pref      bigint  default 0     not null comment '指向当前类型继承的父类型数据实例id.',
    cref      bigint  default 0     not null comment '当前父类数据实例指向子类数据实例的 id.',
    deleted   boolean default false not null comment '是否被删除.',
    attribute LONGTEXT              not null comment '当前 entity 的属性集合.',
    constraint oqsengine_pk2 primary key (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;