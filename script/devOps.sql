use oqsengine;
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
     checkpoint varchar(512) default null comment '任务坐标信息',
     constraint devopstasks_pk0 primary key (maintainid),
     key devopstasks_k0 (entity)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4;

create table cdcerrors
(
    seqno           bigint                      not null comment '数据主键',
    unikey          varchar(512)                not null comment '唯一约束',
    batchid         bigint                      not null comment '批次ID',
    id              bigint                               comment '业务主键ID',
    entity          bigint                               comment 'entity 的类型 id.',
    version         int                                  comment '当前数据版本.',
    op              tinyint                              comment '最后操作类型,0(插入),1(更新),2(删除)',
    commitid        bigint                               comment '提交号',
    type            tinyint                     not null comment '错误类型,1-单条数据格式错误,2-批次数据插入失败',
    status          tinyint                     not null comment '处理状态,1(未处理),2(已提交处理),3(处理成功),4(处理失败)',
    operationobject json                                 comment '当前操作的对象(已序列化)',
    message         varchar(1024)                        comment '出错信息',
    executetime     bigint                      not null comment '出错时间戳',
    fixedtime       bigint                      not null comment '修复时间.',
    constraint cdcerror_pk primary key (seqno),
    unique key unikey_upk (unikey),
    key cdcerrors_k0 (batchid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;