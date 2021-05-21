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
    primary key (id),
    KEY commitid_entity_index (commitid, entityclassl0, entityclassl1, entityclassl2),
    KEY tx_index (tx),
    KEY update_time_index (updatetime)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table devopstasks
(
     maintainid bigint not null comment '任务批次ID',
     entity bigint not null comment 'entity 的类型 id.',
     starts bigint default 0 not null comment '任务范围起始时间',
     ends bigint default 0 not null comment '任务范围终止时间',
     batchsize int default 0 not null comment '任务总数',
     finishsize int default 0 not null comment '任务已处理数',
     status int default 0 not null comment '状态',
     createtime bigint not null comment '任务创建时间',
     updatetime bigint default null comment '任务结束时间',
     message varchar(512) default null comment '任务摘要',
     startid bigint default 0 not null comment '起始ID',
     constraint devopstasks_pk0 primary key (maintainid),
     key devopstasks_k0 (entity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

