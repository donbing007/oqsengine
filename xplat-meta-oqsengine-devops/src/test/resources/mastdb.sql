use oqsengine;
create table cdcerrors
(
    seqno           bigint                      not null comment '数据主键',
    id              bigint                      not null comment '业务主键ID',
    commitid        bigint          default 0   not null comment '提交号',
    status          tinyint                     not null comment '处理状态，0(未处理),1(已处理),2(处理失败)',
    message         varchar(1024)                        comment '出错信息',
    executetime     bigint                      not null comment '出错时间戳',
    fixedtime       bigint                      not null comment '修复时间.',
    constraint cdcerror_pk primary key (seqno)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
