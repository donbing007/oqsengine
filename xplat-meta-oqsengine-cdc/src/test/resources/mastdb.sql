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
    KEY commitid_entity_index (entityclassl0, entityclassl1, entityclassl2, entityclassl3, entityclassl4, commitid),
    KEY tx_index (tx),
    KEY update_time_index (updatetime)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table cdcerrors
(
    seqno           bigint                      not null comment '数据主键',
    batchid         bigint                      not null comment '批次ID',
    id              bigint                               comment '业务主键ID',
    commitid        bigint                               comment '提交号',
    type            tinyint                     not null comment '错误类型,1-单条数据格式错误,2-批次数据插入失败',
    status          tinyint                     not null comment '处理状态,0(未处理),1(已提交处理),2(处理成功),3(处理失败)',
    operationobject json                                 comment '当前操作的对象(已序列化)',
    message         varchar(1024)                        comment '出错信息',
    executetime     bigint                      not null comment '出错时间戳',
    fixedtime       bigint                      not null comment '修复时间.',
    constraint cdcerror_pk primary key (seqno)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
