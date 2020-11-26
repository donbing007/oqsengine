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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table cdcerrors
(
    seqno           bigint                      not null comment '数据主键',
    id              bigint                      not null comment '业务主键ID',
    commitid        bigint          default 0   not null comment '提交号',
    status          tinyint                     not null comment '处理状态，0(未处理),1(已处理),2(处理失败)',
    message         varchar(1024)                        comment '出错信息',
    executetime     bigint                      not null comment '出错时间戳',
    fixedtime       bigint                      not null comment '修复时间.',
    constraint cdcerror_pk primary key (seqno),
    key cdcerrors_k0 (id),
    key cdcerrors_k1 (commitid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
